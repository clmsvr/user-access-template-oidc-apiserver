package cms.test.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class MockAuthentication {
	
	@Autowired
	KeyStoreProperties properties;
	
	public static final String KEY_ID = "mykeyid";

/* Spring AuthServer
{
  "sub": "joao.ger@algafood.com.br",
  "aud": "postman1",
  "nbf": 1741214839,
  "scope": [
    "read",
    "openid",
    "write"
  ],
  "iss": "http://localhost:8080",
  "exp": 1741225639,
  "userName": "João da Silva",
  "iat": 1741214839,
  "userId": "1",
  "jti": "f098342f-224d-48ee-b1e6-99d5cdb4be3d",
  "authorities": [
    "GERAR_RELATORIOS",
    "EDITAR_COZINHAS",
    "CONSULTAR_USUARIOS_GRUPOS_PERMISSOES",
    "EDITAR_CIDADES",
    "EDITAR_FORMAS_PAGAMENTO",
    "EDITAR_RESTAURANTES",
    "GERENCIAR_PEDIDOS",
    "EDITAR_USUARIOS_GRUPOS_PERMISSOES",
    "CONSULTAR_PEDIDOS",
    "EDITAR_ESTADOS"
  ]
}	
 */
/* Access Token Cognito
	{
	  "sub": "5418d448-7081-70b4-9b0f-7b96f1a9f1be",
	  "iss": "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_FSSjl3xir",
	  "version": 2,
	  "client_id": "j5ifq77iqp38769eun48lat3",
	  "origin_jti": "6d1987ad-c283-44ac-ba71-a1ce2194a70f",
	  "event_id": "d2e8bad0-66c7-486f-b985-97306abadc4f",
	  "token_use": "access",
	  "scope": "phone openid profile email",
	  "auth_time": 1741259578,
	  "exp": 1741263178,
	  "iat": 1741259578,
	  "jti": "0b34e11d-af2d-442b-982c-af04ada41502",
	  "username": "5418d448-7081-70b4-9b0f-7b96f1a9f1be"
	}

	ID Token
	{
	  "at_hash": "jOHz4aj9qk-_u0LCXiAP1A",
	  "sub": "5418d448-7081-70b4-9b0f-7b96f1a9f1be",
	  "email_verified": true,
	  "iss": "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_FSSjl3xir",
	  "cognito:username": "5418d448-7081-70b4-9b0f-7b96f1a9f1be",
	  "origin_jti": "6d1987ad-c283-44ac-ba71-a1ce2194a70f",
	  "aud": "j5ifq77iqp38769eun48lat3",
	  "event_id": "d2e8bad0-66c7-486f-b985-97306abadc4f",
	  "token_use": "id",
	  "auth_time": 1741259578,
	  "name": "Claudio Silveira",
	  "exp": 1741263178,
	  "iat": 1741259578,
	  "jti": "3d9e5210-62fc-44e6-bcfe-a97bb8111956",
	  "email": "cl.silveira@gmail.com"
	}
*/	
	
    public static OAuth2AuthenticationToken getOAuth2AuthenticationToken(String oidcId, String name, String email, List<String> authorities)
    {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", oidcId);
        attributes.put("name", name);
        attributes.put("email", email);
        
        Set<GrantedAuthority> mappedAuthorities = authorities.stream()
        		.map(a -> new SimpleGrantedAuthority(a))
        		.collect(Collectors.toSet());
        
        DefaultOAuth2User defaultOAuth2User = new DefaultOAuth2User(mappedAuthorities, attributes, "sub");

        return new OAuth2AuthenticationToken(defaultOAuth2User, defaultOAuth2User.getAuthorities(), 
        		"mockClientId");
    }
    
    public static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(
    		String userName, String password, List<String> authorities)
    {
        Set<GrantedAuthority> mappedAuthorities = authorities.stream()
        		.map(a -> new SimpleGrantedAuthority(a))
        		.collect(Collectors.toSet());
        
        User user = new User(userName, password, mappedAuthorities);
        
        return  new UsernamePasswordAuthenticationToken(
        	 user, password, mappedAuthorities);
    }
    
	public static JwtAuthenticationToken getJwtAuthenticationToken(String oidcid, String name, String email, 
			List<String> authorities) 
	{
		Date expirationDate = new Date(new Date().getTime() + 1000*60*60*24);
		long exp = expirationDate.getTime() /1000; //em segundos
		
		Map<String, Object> jwtHeaders = Map.of(
			    "alg", "RS256",
			    "kid", KEY_ID
		);
		Map<String, Object> claims = Map.of(
			    "sub", oidcid,
			    "username", oidcid,
			    "name", name,
			    "email", email,	 
			    "iss", "http://localhost:8090",
			    "exp", ""+exp,
			    "jti", UUID.randomUUID().toString()   
		);
		
        Set<GrantedAuthority> mappedAuthorities = authorities.stream()
        		.map(a -> new SimpleGrantedAuthority(a))
        		.collect(Collectors.toSet());
        
		//Construir Token OAuth2
		Jwt jwt = new Jwt("FakeToken",Instant.now(), expirationDate.toInstant(), jwtHeaders, claims);
		
		return new JwtAuthenticationToken(jwt,mappedAuthorities);
		
    }
	
	//accesss token mock
	public Jwt getToken(String oidcid, String name, String email, String scope) 
	throws JOSEException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException 
	{
    	// RSA signatures require a public and private RSA key pair, the public key
    	// must be made known to the JWS recipient in order to verify the signatures
    	RSAKey rsaJWK = getRSAKey(properties);
    	
    	//RSAKey rsaPublicJWK = rsaJWK.toPublicJWK();

    	//Date expirationDate = new Date(new Date().getTime() + 1000*60*60*24);
    	Instant expiration = Instant.now().plusSeconds(60*60*24);
    	
    	// Prepare JWT with claims set
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
			  .subject(oidcid) //sub
			  .claim("username", oidcid)
			  .claim("name", name)
			  .claim("email", email)
		      .issuer("http://localhost:8090") //iss
		      .claim("scope", scope)
		      .issueTime( Date.from(Instant.now()) )
		      .expirationTime( Date.from(expiration) )
		      .jwtID(UUID.randomUUID().toString()) //jti			  
			  .build();

		//JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
		JWSHeader header = new JWSHeader
				.Builder(JWSAlgorithm.RS256)
				.keyID(KEY_ID) //rsaJWK.getKeyID())  // !!!! deve bater com o valor informado no JWKS
				.build();
		
    	SignedJWT signedJWT = new SignedJWT(header, claimsSet);

    	// Create RSA-signer with the private key
    	JWSSigner signer = new RSASSASigner(rsaJWK);
    	// Compute the RSA signature
    	signedJWT.sign(signer);

    	
    	// To serialize to compact form, produces something like
    	// eyJhbGciOiJSUzI1NiJ9.SW4gUlNBIHdlIHRydXN0IQ.IRMQENi4nJyp4er2L
    	// mZq3ivwoAjqa1uUkSBKFIX7ATndFF5ivnt-m8uApHO4kfIFOrW7w2Ezmlg3Qd
    	// maXlS9DhN0nUk_hGI3amEjkKd0BWYCB8vfUbUv0XGjQip78AI4z1PrFRNidm7
    	// -jPDm5Iq0SZnjKjCNS5Q15fokXZc8u0A
    	String token = signedJWT.serialize();
		System.out.println("Token Geerado: \n"+token+"\n---------------------------------------");
		
		Map<String, Object> jwtHeaders = Map.of(
			    "alg", "RS256",
			    "kid", KEY_ID
		);
		//Construir Token OAuth2
		Jwt jwt = new Jwt(token,Instant.now(), expiration, jwtHeaders, claimsSet.getClaims());
		
    	return jwt;
    }
	
	//Informa as Chaves RSA Publica e Privada para o AuthorizationServer gerar e assinar Tokens JWK.
    private RSAKey getRSAKey(KeyStoreProperties properties) 
    throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, JOSEException 
    {
    	//23.9
    	//obter o keystore (do classpath)
//		var jksResource = new ClassPathResource("keystores/authserver.jks");
//		char[] keyStorePass = "authserver".toCharArray();
//		var keypairAlias = "authserver";
		
    	
    	//## base64:  protocolo implementado na versao 3.4 : org.springframework.boot.io.Base64ProtocolResolver
    	//https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes#base64-resource-support-and-automatic-protocolresolver-registration
    	//ver Base64ProtocolResolver.class criada paraconverter os dados em "Resource"
    	
    	//obter o keystore (do aplication.properties - armazenado no formato BASE64)
    	char[] keyStorePass = properties.getPassword().toCharArray();
        String keypairAlias = properties.getKeypairAlias(); 
        Resource jksResource = properties.getJksLocation();
        

        //ler o keystore (solucao 1)
        InputStream inputStream = jksResource.getInputStream();
        KeyStore keyStore = KeyStore.getInstance("JKS");//java key store
        keyStore.load(inputStream, keyStorePass); 

        //projeto Nimbus :  RSAKey extends JWK
        //!!!! 'keypairAlias' irá no "kid" do header do Token e deve bater com o valor informado no JWKS !!!
        //Desta forma o "kid" será definido com o valor de 'keypairAlias'.
        RSAKey rsaKey = RSAKey.load(keyStore, keypairAlias, keyStorePass); 
        rsaKey = new RSAKey.Builder(rsaKey)
        		.keyUse(KeyUse.SIGNATURE)  //opcional
        		.algorithm(JWSAlgorithm.RS256) //opcional
        		.keyID(KEY_ID)
        		.build();   //redefino o 'kid'     
        
//      //ler o keystore (solucao 2)
//		var factory = new KeyStoreKeyFactory(properties.getJksLocation(), keyStorePass, "JKS");
//		KeyPair keypair = factory.getKeyPair(keypairAlias);
//		
//		//projeto Nimbus :  RSAKey extends JWK (nimbus)
//		RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keypair.getPublic())
//				.privateKey((RSAPrivateKey) keypair.getPrivate())
//				.keyUse(KeyUse.SIGNATURE) //opcional
//				.algorithm(JWSAlgorithm.RS256) //opcional
//				.keyID(KEY_ID) //posso definir o 'kid'
//				.build();       

        return rsaKey;
    }
    

    
    
    
    
    
    
    //Somente para guardar como Documentacao
    
    class Docs {
        //doc
        void decodeJwt(RSAKey rsaPublicJWK, String jwt) 
    	throws ParseException, JOSEException 
    	{
    		// On the consumer side, parse the JWS and verify its RSA signature
    		SignedJWT signedJWT = SignedJWT.parse(jwt);

    		JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
    		assertTrue(signedJWT.verify(verifier));

    		// Retrieve / verify the JWT claims according to the app requirements
    		assertEquals("alice", signedJWT.getJWTClaimsSet().getSubject());
    		assertEquals("https://c2id.com", signedJWT.getJWTClaimsSet().getIssuer());
    		assertTrue(new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime()));
    	}
        
            
        //doc
    	String avaliaSeguranca(Principal p , /*@AuthenticationPrincipal*/ Object ap) 
    	{
    		Authentication a = SecurityContextHolder.getContext().getAuthentication();
    		@SuppressWarnings("unused")
    		Object p2 = a != null ? a.getPrincipal() : null;
    		
    		return "ok";
    		/*
    		ReseourceServer(JWT): autenticado com Jwt token
    			- p e a   -> JwtAuthenticationToken 
    			- ap e p2 -> Jwt
    			
    		OAuth Client (OIDC): autenticado com login de usuario via fluxo OAuth com OIDC
    			- p e a   -> OAuth2AuthenticationToken 
    			- ap e p2 -> DefaultOidcUser (OAuth2User e OidcUser)	
    			
    		Form Login :
    			- p e a : UsernamePasswordAuthenticationToken 
    			- ap e p2 : User (UserDetails , org.springframework.security.core.userdetails.User)

    		*/
    	} 
    	
    	
    	
        //Informa as Chaves RSA Publica e Privada para o AuthorizationServer gerar e assinar Tokens JWT.
        //@Bean
        public JWKSource<SecurityContext> jwkSource(KeyStoreProperties properties) 
        throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, JOSEException 
        {
        	RSAKey rsaKey = getRSAKey(properties);
            return new ImmutableJWKSet<>(new JWKSet(rsaKey));      
        }
        
    	//@Bean
    	JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwks) {
    		return new NimbusJwtEncoder(jwks);
    	}
    	
    	
    	public Jwt token(Authentication authentication, JwtEncoder encoder) 
    	{
    		Instant now = Instant.now();
    		long expiry = 36000L; //10horas
    		
    		String scope = authentication.getAuthorities().stream()
    				.map(GrantedAuthority::getAuthority)
    				.collect(Collectors.joining(" "));
    		
    		JwtClaimsSet claims = JwtClaimsSet.builder()
    				.issuer("self")
    				.issuedAt(now)
    				.expiresAt(now.plusSeconds(expiry))
    				.subject(authentication.getName())
    				.claim("scope", scope)
    				.build();
    		return encoder.encode(JwtEncoderParameters.from(claims)); //.getTokenValue();
    	}
    	
    	//accesss token mock
    	public Jwt token(JwtEncoder encoder, 
    			         String oidcid, String name, String email, String scope) 
    	throws JOSEException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException 
    	{
    		JwtClaimsSet claims = JwtClaimsSet.builder()
    				.subject(oidcid)
    				.claim("username", oidcid)
    				.claim("name", name)
    				.claim("email", email)
    				.issuer("http://localhost:8090") //iss
    				.claim("scope", scope)
    				.issuedAt(Instant.now())
    				.expiresAt(Instant.now().plusSeconds(60*60*24))
    				.claim("jti", UUID.randomUUID().toString())
    				.build();
    		return encoder.encode(JwtEncoderParameters.from(claims)); //.getTokenValue();
    	}		
    	
    }

}
