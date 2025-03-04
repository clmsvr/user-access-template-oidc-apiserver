package cms.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.transaction.annotation.Transactional;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;

import cms.components.CognitoLogoutSuccessHandler;
import cms.components.CognitoProperties;
import cms.domain.Consts;
import cms.domain.model.Authority;
import cms.domain.model.Role;
import cms.domain.model.UserRole;
import cms.repository.UserRoleRepository;

@Configuration //nao eh necessario pq a anotacao @EnableWebSecurity deriva de @Configuration
@EnableWebSecurity //permite que nossa configuracao substitua as configurações default de seguranca dos Starters do Spring Security - https://stackoverflow.com/questions/44671457/what-is-the-use-of-enablewebsecurity-in-spring
public class SecurityConfig {

	@Autowired
	UserRoleRepository userRoleRepository;
	@Autowired
	CognitoProperties cognitoProperties;
	
	@Bean
	public SecurityFilterChain securityFilterChain1(HttpSecurity http) throws Exception 
	{
		http
			.authorizeHttpRequests((requests) -> 
				requests
					.requestMatchers("/","/error","/swagger-ui/**", "/v3/api-docs/**","/bootstrap/**","/css/**","/fonts/**","/image/**","/js/**")
					.permitAll()
					.requestMatchers("/user/mng/*").authenticated()
					.requestMatchers("/api/**").hasRole("Admin")
					.requestMatchers("/admin/**").hasRole("Admin") //usando "/admin/*" '/admin' NAO estará incluso, somente paths abaixo.
					.anyRequest().authenticated()
			)
			//importante desativar CSRF para as uris da API Rest.
			.csrf(c -> c.ignoringRequestMatchers(new AntPathRequestMatcher("/api/**")))
            .logout(logout -> 
  		    	logout.logoutSuccessHandler(new CognitoLogoutSuccessHandler(cognitoProperties))
  		    	//logout.logoutSuccessUrl("https://us-east-1fssjl3xir.auth.us-east-1.amazoncognito.com/logout?client_id=j5ifq77iqp38769eun48lat3&logout_uri=http://localhost:8080")           
			)
            .oauth2Login(Customizer.withDefaults())
            .oauth2ResourceServer(cust -> {
				//cust.opaqueToken(t -> {}); //Considera todos os Tokens como OPACO, e faz a introspecção (mesmo se for um token JWT).
				cust.jwt(jwtCust ->     
				    //ler as authorities
					jwtCust.jwtAuthenticationConverter(jwtAuthenticationConverter()));
			});

		
		return http.build();
	}

//	@Autowired
//	private ClientRegistrationRepository clientRegistrationRepository;
//    //Nao funciona com Cognito. Os parametros da url esperados pelo Cognito sao diferentes da Especificação: https://openid.net/specs/openid-connect-rpinitiated-1_0.html
//    //https://docs.spring.io/spring-security/reference/servlet/oauth2/login/logout.html#configure-client-initiated-oidc-logout
//	private LogoutSuccessHandler oidcLogoutSuccessHandler() 
//	{
//		// Este handler obtem a uri "end_session_endpoint" e passa os parametros conforme:
//		// https://docs.aws.amazon.com/cognito/latest/developerguide/logout-endpoint.html
//		OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
//				new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
//
//		// Sets the location that the End-User's User Agent will be redirected to
//		// after the logout has been performed at the Provider
//		oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
//
//		return oidcLogoutSuccessHandler;
//	}
	
	
	
    /**
     * Fluxo OAuth
     * Interceptar o usuario logado, validar/verificar base local, e adicionar authorities.
     */
    // https://docs.spring.io/spring-security/reference/servlet/oauth2/login/advanced.html#oauth2login-advanced-map-authorities-oauth2userservice
    // https://spring.io/guides/tutorials/spring-boot-oauth2
	@Bean
	public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() 
	{
		final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
		return request -> {
			
			System.out.println("### Building MY OAuth2User .....");
			
			OAuth2User oauthUser = delegate.loadUser(request);
			
			return updateUser(request, oauthUser);
		};
	}

    /**
     * Fluxo OIDC
     * Interceptar o usuario logado, validar/verificar base local, e adicionar authorities.
     */
	@Bean
	public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() 
	{
		final OidcUserService delegate = new OidcUserService();
		return request -> {
			
			System.out.println("### Building MY OidcUser .....");
			
			OidcUser oauthUser = delegate.loadUser(request);
			
			return (OidcUser) updateUser(request, oauthUser);
		};
	}

	@Transactional
	private DefaultOAuth2User updateUser(OAuth2UserRequest request, OAuth2User oauthUser) 
	{
		OAuth2AccessToken accessToken = request.getAccessToken();
		
		Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
		
		//Buscar ou Criar usuario no cadastrado no banco
		UserRole domainUser = findUserInDatabase(oauthUser);
		addAuthorities(domainUser.getRoles(), accessToken.getScopes(), mappedAuthorities);
		
		//executar ações com base no Client ID
		if ("github".equals(request.getClientRegistration().getRegistrationId())) {

		}
		
		// podemos gerar erro e abortara o login do usuario na aplicação.
		//throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Not in Spring Team", ""));
		
		//Criar copia do OAuth2User com "mappedAuthorities" .
		//Definimos que a claim "sub" vai nomear e identificar o OAuth2User (nosso oidcId)
		if (oauthUser instanceof OidcUser oidcuser) 
		{
			return new DefaultOidcUser(mappedAuthorities, oidcuser.getIdToken(), oidcuser.getUserInfo(), StandardClaimNames.SUB);
		}
		else 
		{
			return new DefaultOAuth2User(mappedAuthorities, oauthUser.getAttributes(),StandardClaimNames.SUB);
		}
	}
	
	@Transactional
	private UserRole findUserInDatabase(OAuth2User oauthUser) 
	{
		String oidcId = oauthUser.getName();  //getAttribute(IdTokenClaimNames.SUB);
		UserRole domainUser = userRoleRepository.findByOidcId(oidcId);
		if (domainUser == null) {
			domainUser = new UserRole();
			domainUser.setOidcId(oidcId);
			domainUser.setName(oauthUser.getAttribute("name"));
			domainUser.setEmail(oauthUser.getAttribute("email"));
			domainUser.setProviderName(getProviderName(oauthUser));
			userRoleRepository.save(domainUser);
			userRoleRepository.flush();
		}
		return domainUser;
	}
	
	@Transactional
	private void addAuthorities(Set<Role> roles, Set<String> scopes,
			Collection<GrantedAuthority> mappedAuthorities) 
	{
		for (Role role : roles) {
			mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_"+role.getName()));
			for (Authority p : role.getAuthorities()) {
				mappedAuthorities.add(new SimpleGrantedAuthority(p.getName()));
			}
		}
		
		scopes.stream().forEach(
				scope -> mappedAuthorities.add(new SimpleGrantedAuthority("SCOPE_"+scope)));
	}
	
	private static String getProviderName(OAuth2User oauthUser)
	{
		String providerName = "";
		if (oauthUser != null) {
			providerName = Consts.LOCAL_PROVIDER_NAME;
			
			Object identities = oauthUser.getAttribute("identities");
			if (identities instanceof ArrayList atts) 
			{
				Object object = atts.get(0);
				if(object instanceof LinkedTreeMap map) {
					providerName = map.get("providerName").toString();
				}
			}
		}
		return providerName;
	}
	
	//27.15
	//Esta classe permite o spring identificar as autorities que estao no Token jwt
    Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() 
    {
    	//Implementação Padrão, que usa por padrao um outro Converter: 
    	//  JwtGrantedAuthoritiesConverter  -> Converter<Jwt, Collection<GrantedAuthority>>
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        
                                                    //Converter<Jwt, Collection<GrantedAuthority>>
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
        	
        	//Ler os SCOPES com a implementacao padrao, que lê somente os escopos.
            //Vamos substituila com outro converter (este), para ler tambem nossa AUTHOTITIES. 
            //Converte os SCOPES do token em Authorities com prefixo "SCOPE_"
            Collection<GrantedAuthority> defaultAuthorities = new JwtGrantedAuthoritiesConverter().convert(jwt);

            
            //LER nossas Authorities 
    		
    		//Verificar se o usuario do token tem Roles cadastradas no banco.
//    		Set<Role> roles = findRolesInDatabase(jwt.getClaimAsString("sub"));
//    		
//    		for (Role role : roles) {
//    			defaultAuthorities.add(new SimpleGrantedAuthority("ROLE_"+role.getName()));
//    			for (Authority p : role.getAuthorities()) {
//    				defaultAuthorities.add(new SimpleGrantedAuthority(p.getName()));
//    			}
//    		}
    		
    		List<String> myAuthorities = findAuthoritiesInDatabase(jwt.getClaimAsString("sub"));
    		
    		for (String auth : myAuthorities) {
    			defaultAuthorities.add(new SimpleGrantedAuthority(auth));
    		}
    		
            return defaultAuthorities;
        });

        return converter;
    }
    
	@Transactional
	private Set<Role> findRolesInDatabase(String oidcId) 
	{
		UserRole domainUser = userRoleRepository.findByOidcId(oidcId);
		if (domainUser != null)
			return domainUser.getRoles();
		else
			return new HashSet<Role>();
	}
	
	@Transactional
	private List<String> findAuthoritiesInDatabase(String oidcId) 
	{
		return userRoleRepository.listAuthoritiesByOidcId(oidcId);
	}
}