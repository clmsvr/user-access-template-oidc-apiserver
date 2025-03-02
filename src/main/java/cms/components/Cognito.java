package cms.components;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;

import cms.domain.Consts;
import cms.domain.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.PasswordResetRequiredException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UnsupportedUserStateException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@Component
public class Cognito {

	private CognitoProperties             properties;
	private CognitoIdentityProviderClient client;
	
	public Cognito(CognitoProperties securityProperties) {
		
		properties = securityProperties;		
	}
	
    @PostConstruct
    public void init() 
    {
		client = CognitoIdentityProviderClient.builder()
				//https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
				// The default credential provider chain, implemented by the DefaultCredentialsProvider class, checks sequentially each of places where you can set default credentials and selects the ﬁrst one you set.
                //.credentialsProvider(ProfileCredentialsProvider.create()) 
				.credentialsProvider(
						StaticCredentialsProvider
						    .create(AwsBasicCredentials.create(
						    		properties.getIdChaveAcesso(),
						    		properties.getChaveAcessoSecreta())))
				.region(Region.of(properties.getRegiao()))				
                .build();   
    }
    
    @PreDestroy
    public void destroy() 
    {
    	System.out.println("DESTROY Cognito...");
		close();
    }	
    
	public void close() 
	{
		try 
		{
			if (client != null) client.close();
		} 
		catch (Exception e) { }
	}	
	
	/**
	 * Cria um novo usuario no Cognito. 
	 * O Cognito deverá gerar uma senha temporário para o usuario e informa-la por email.
	 * 
	 * @param email
	 * @param nome
	 * 
	 * @return  OIDC Id do novo usuário.
	 * 
	 * @throws UsernameExistsException    usuário com este email já existe.
	 * @throws InvalidParameterException  formato de email inválido
	 * @throws CognitoIdentityProviderException  outros erros  inesperados do cógnito.
	 */
	public String createUser(String email, String nome)
	throws UsernameExistsException, 
           InvalidParameterException,
           CognitoIdentityProviderException		
	{
			//atributos do request
			AttributeType userAttrs = AttributeType.builder()
			        .name("name")
			        .value(nome)
			        .build();

			//request
			var builder = AdminCreateUserRequest.builder()
			        .userPoolId(properties.getPoolId())
			        .username(email)
			        .userAttributes(userAttrs);
			
			AdminCreateUserRequest request = builder.build();
			AdminCreateUserResponse response = null ;
			
			//Execução
			response = client.adminCreateUser(request);
			
			var user = response.user();
			return user.username();
	}
	
	/**
	 * Só para usuario novo, existente, e em estado de "Force change password"
	 * @param oidcId
	 * @param newPassword   se a senha nao for fornecida o Cognito gerará a senha,  e ela precisará ser informada por email.
	 * @throws UserNotFoundException    usuário nao encontrado
	 * @throws UnsupportedUserStateException  Não é mais permitido redefinir o Usuario. Não está mais no estado FORCE_CHANGE_PASSWORD.
	 * @throws InvalidParameterException  formato de email inválido
	 * @throws CognitoIdentityProviderException   outros erros nao esperado.
	 */
	public void resetNewUserPassword(String oidcId, String newPassword)
	throws UserNotFoundException, 
	       UnsupportedUserStateException,
	       InvalidParameterException,
	       CognitoIdentityProviderException		
	{
		//Creaate Reques
        var builder = AdminCreateUserRequest.builder()
                .userPoolId(properties.getPoolId())
                .username(oidcId)
                .messageAction("RESEND");
        
        //se a senha nao for fornecida o Cognito gerará a senha, 
        //e ela precisará ser informada por email.
        if (StringUtils.hasText(newPassword)) {
        	builder.temporaryPassword(newPassword);
        }
        
        AdminCreateUserRequest request = builder.build();
        
        //execução
       client.adminCreateUser(request);
	}	
	
	public void removeUser(String iodcId)
	throws UserNotFoundException, CognitoIdentityProviderException		
	{
        AdminDeleteUserRequest request = AdminDeleteUserRequest.builder()
                .userPoolId(properties.getPoolId())
                .username(iodcId)
                .build();

        //AdminDeleteUserResponse response = 
        client.adminDeleteUser(request);
	}	
	
	public void chantePassword(String accessToken, String senhaAntiga, String novaSenha) 
	throws NotAuthorizedException,
	       InvalidPasswordException, 
	       PasswordResetRequiredException,
	       CognitoIdentityProviderException  //base
	{
    	ChangePasswordRequest userRequest = ChangePasswordRequest.builder()
                .accessToken(accessToken)
                .previousPassword(senhaAntiga)
                .proposedPassword(novaSenha)
                .build() ; 
        
    	client.changePassword(userRequest);
	}
	
	
	public HashMap<String, User> mapUsers(){
		
		return mapUsers(null);
	}
	
	public HashMap<String,User> mapUsers(String email)
	throws CognitoIdentityProviderException		
	{
		ListUsersRequest.Builder usersRequest = ListUsersRequest.builder()
                .userPoolId(properties.getPoolId());
				
		if (StringUtils.hasText(email)) 
		{
	        usersRequest.filter(String.format("email = \"%s\"", email));
		}
		
        ListUsersResponse response = client.listUsers(usersRequest.build());
        
        HashMap<String,User> map = new HashMap<>();
        
        response.users().forEach(user -> {
        	var domainUser = new User();
        	
        	OffsetDateTime off = user.userCreateDate().atOffset(ZoneOffset.of("-00:00"));
        	//ZonedDateTime zoned = off.atZoneSameInstant(ZoneId.of("-00:00"));
        	domainUser.setCreationDate(off.toLocalDateTime());
        	
        	off = user.userLastModifiedDate().atOffset(ZoneOffset.of("-00:00"));
        	//zoned = off.atZoneSameInstant(ZoneId.systemDefault());
        	domainUser.setUpdateDate(off.toLocalDateTime());
        	
        	domainUser.setProviderName(Consts.LOCAL_PROVIDER_NAME);
        	if (user.userStatus() == null || user.userStatus().equals("EXTERNAL_PROVIDER"))
        	{
        		domainUser.setProviderName("EXTERNAL");
        	}
        	
            user.attributes().stream().forEach( att -> {
            	if (att.name().equals("name")) domainUser.setName(att.value());
            	if (att.name().equals("email")) domainUser.setEmail(att.value());
            	if (att.name().equals("sub")) domainUser.setOidcId(att.value());
            	if (att.name().equals("identities")) domainUser.setProviderName("EXTERNAL");
            });
            
            map.put(domainUser.getOidcId(), domainUser);
        });
        
        return map;
	}
	
	public User getUser(String cognitoUserId)
	throws UserNotFoundException, CognitoIdentityProviderException		
	{
        AdminGetUserRequest userRequest = AdminGetUserRequest.builder()
                .username(cognitoUserId)
                .userPoolId(properties.getPoolId())
                .build();

        AdminGetUserResponse user = client.adminGetUser(userRequest);
        
       	var domainUser = new User();
    	
    	OffsetDateTime off = user.userCreateDate().atOffset(ZoneOffset.of("-00:00"));
    	//ZonedDateTime zoned = off.atZoneSameInstant(ZoneId.of("-00:00"));
    	domainUser.setCreationDate(off.toLocalDateTime());
    	
    	off = user.userLastModifiedDate().atOffset(ZoneOffset.of("-00:00"));
    	//zoned = off.atZoneSameInstant(ZoneId.systemDefault());
    	domainUser.setUpdateDate(off.toLocalDateTime());
    	
    	domainUser.setProviderName(Consts.LOCAL_PROVIDER_NAME);
    	if (user.userStatus() == null || user.userStatus().equals("EXTERNAL_PROVIDER"))
    	{
    		domainUser.setProviderName("EXTERNAL");
    	}
    	
        user.userAttributes().stream().forEach( att -> {
        	if (att.name().equals("name")) domainUser.setName(att.value());
        	if (att.name().equals("email")) domainUser.setEmail(att.value());
        	if (att.name().equals("sub")) domainUser.setOidcId(att.value());
        	if (att.name().equals("identities")) domainUser.setProviderName("EXTERNAL");
        });
        
        return domainUser;
	}	

	public static String getProviderName(OAuth2User oauthUser)
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
}
