package cms.domain.service;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cms.components.Cognito;
import cms.domain.Consts;
import cms.domain.model.User;
import cms.exceptions.InvalidProviderException;
import cms.exceptions.NotFoundException;
import cms.repository.UserRepository;
import cms.web.model.PwdChange;
import cms.web.model.UserApi;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentity.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.PasswordResetRequiredException;

@Slf4j
@Service
public class UserMngService
{
	@Autowired
	UserRepository userRep;
	@Autowired
	private Cognito cognito;
	
	public void changeUserPassword(String accessToken, PwdChange pwd, User domainUser) 
	throws NotFoundException, InvalidProviderException, 
	       NotAuthorizedException, InvalidPasswordException, PasswordResetRequiredException,
	       CognitoIdentityProviderException
	{
        if (domainUser == null)
        {
            throw new NotFoundException("tentativa de alterar senha de usuario nao encontrado.");
        }

        if (domainUser.getProviderName().equals(Consts.LOCAL_PROVIDER_NAME) == false)
        	throw new InvalidProviderException("Inconsistencia: não é possivel alterar a senha de um usuário logado através de um provedor Federado: ["+domainUser.getProviderName()+"]");
        
        cognito.chantePassword(accessToken, pwd.getPwd(), pwd.getNewpwd1());
	}

	public void updateUser(@Valid UserApi userModel, User domainUser) {

        //User userdb = userRep.getByEmail(userModel.getEmail()); //brecha de seguranca
        //modelMapper.map(userModel, userdb);
        BeanUtils.copyProperties(userModel, domainUser, "id", "oidcId", "providerName", "email", "creationDate"); //iguinorar props
                                 // o formulario de usuario nao atualiza sennha 
        domainUser.setUpdateDate(LocalDateTime.now());
    	userRep.save(domainUser);
    	userRep.flush();
	}
}
