package cms.domain.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cms.components.Cognito;
import cms.domain.Consts;
import cms.domain.model.User;
import cms.domain.model.UserRole;
import cms.domain.model.UserUpdateDto;
import cms.exceptions.InUseException;
import cms.exceptions.NotFoundException;
import cms.repository.UserRepository;
import cms.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@Slf4j
@Service
public class UserService
{
	public static final String MSG_NOT_FOUND = "Não existe uma Usuário com id  %s";
	private static final String MSG_IN_USE =  "Usuário com código %s não pode ser removida, pois está em uso.";

	@Autowired
	UserRepository userRep;
	@Autowired
	UserRoleRepository userRoleRep;
	@Autowired
	private Cognito cognito;
	@Autowired
	ModelMapper modelMapper;
	
	@Transactional
	public User recoverUser(String oidcId) 
	throws NotFoundException
	{
		User user = userRep.findByOidcId(oidcId);
		
		if (user == null) throw new NotFoundException( String.format(MSG_NOT_FOUND, oidcId));
		return user;
	}

	@Transactional
	public UserRole recoverUserRole(String oidcId) 
	throws NotFoundException
	{
		UserRole user = userRoleRep.findByOidcId(oidcId);
		
		if (user == null) throw new NotFoundException( String.format(MSG_NOT_FOUND, oidcId));
		return user;
	}
	
	@Transactional
	public User create(User domainUser) 
	throws UsernameExistsException, 
           InvalidParameterException,
           CognitoIdentityProviderException	
	{
		String oidcId = cognito.createUser(domainUser.getEmail(), domainUser.getName());
		
		domainUser.setId(null);
		domainUser.setOidcId(oidcId);
		domainUser.setProviderName(Consts.LOCAL_PROVIDER_NAME);
		if(domainUser.getState() != null) domainUser.setState(domainUser.getState().toUpperCase());
		
		domainUser = userRep.save(domainUser);
		
		return domainUser;
	}

	@Transactional
	public User update(UserUpdateDto dto, String oidcId) 
	throws NotFoundException
	{
		User domainUser = recoverUser(oidcId);
		
		modelMapper.map(dto, domainUser);
		if(domainUser.getState() != null) domainUser.setState(domainUser.getState().toUpperCase());
		
		domainUser = userRep.save(domainUser);		
		return domainUser;
	}

	@Transactional
	public void delete(String oidcId) 
    throws UserNotFoundException, CognitoIdentityProviderException,
           NotFoundException, InUseException
	{
		UserRole domainUser = userRoleRep.findByOidcId(oidcId); //tem que ser o objeto completo, pois o delete precisa conhecer os relacionamentos para limpar.
		
		if(domainUser == null)
			throw new NotFoundException( String.format(MSG_NOT_FOUND, oidcId));
			
		try {
			
			//If the entity is not found in the persistence store it is silently ignored.
			userRoleRep.deleteById(domainUser.getId());
			
			//11.21
			//por causa do agora estendido contexto transacional, nao ha garantias de que a 
			//operação vai ser executada agora para capturarmos as exceptions. 
			//Nao estamos capturando as exceptions. operaçoes estao enfileiradas no EntityManager
			//Precisamos usar o comit() para executar as operacoes e capturarmos as exceptions.
			userRoleRep.flush();
		} 
		catch (EmptyResultDataAccessException e) //nao é mais lancada
		{ 
			throw new NotFoundException(String.format(MSG_NOT_FOUND, oidcId));
		} 
		catch (DataIntegrityViolationException e) 
		{
			throw new InUseException(String.format(MSG_IN_USE, oidcId));
		}

		//a transação vai garantir que se houver erro, o usuario nao será removido do banco.
		cognito.removeUser(oidcId);
	}	
}
