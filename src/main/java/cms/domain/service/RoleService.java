package cms.domain.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cms.api.model.RoleModel;
import cms.domain.model.Authority;
import cms.domain.model.Role;
import cms.exceptions.InUseException;
import cms.exceptions.NotFoundException;
import cms.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RoleService
{
	public static final String MSG_NOT_FOUND = "Não existe uma ROLE com id  %d";
	private static final String MSG_IN_USE =  "ROLE de código %d não pode ser removida, pois está em uso";
	
	
	@Autowired
	private RoleRepository roleRep;
	@Autowired
	private AuthorityService authService;
	@Autowired
	ModelMapper modelMapper;
	
	@Transactional
	public Role get(Long id) 
	throws NotFoundException
	{
		return roleRep.findById(id).orElseThrow(() -> 
					new NotFoundException(String.format(MSG_NOT_FOUND, id) ) );
	}

	@Transactional
	public RoleModel create(Role domainRole) 
	{
		domainRole.setId(null);		
		domainRole = roleRep.save(domainRole);
		
		return modelMapper.map(domainRole, RoleModel.class);
	}

	@Transactional
	public Role update(Role domainRole) 
	{
		return roleRep.save(domainRole);		
	}

	@Transactional
	public void delete(long id) 
	{
		Role domainRole = get(id);

		try {
			
			//If the entity is not found in the persistence store it is silently ignored.
			roleRep.deleteById(domainRole.getId());
			
			//11.21
			//por causa do agora estendido contexto transacional, nao ha garantias de que a 
			//operação vai ser executada agora para capturarmos as exceptions. 
			//Nao estamos capturando as exceptions. operaçoes estao enfileiradas no EntityManager
			//Precisamos usar o comit() para executar as operacoes e capturarmos as exceptions.
			roleRep.flush();
			
		} 
		catch (EmptyResultDataAccessException e) //nao é mais lancada
		{ 
			throw new NotFoundException(String.format(MSG_NOT_FOUND, id));
		} 
		catch (DataIntegrityViolationException e) 
		{
			throw new InUseException(String.format(MSG_IN_USE, id));
		}
	}
	
	@Transactional
	public void unlinkAuthority(Long roleId, Long authorityId) 
	throws NotFoundException
	{
		Authority authority = authService.get(authorityId);
		Role role = get(roleId);
		
		role.removeAuthority(authority);
	}

	@Transactional
	public void linkAuthority(Long roleId, Long authorityId) 
	throws NotFoundException
	{
		Authority authority = authService.get(authorityId);
		Role role = get(roleId);
		
		role.addAuthority(authority);		
	}
}
