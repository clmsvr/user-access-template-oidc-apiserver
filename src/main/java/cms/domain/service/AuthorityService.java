package cms.domain.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cms.domain.model.Authority;
import cms.exceptions.NotFoundException;
import cms.repository.AuthorityRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthorityService
{
	public static final String MSG_NOT_FOUND = "Não existe uma 'Authority' com id  %d";
	
	
	@Autowired
	private AuthorityRepository authRep;
	@Autowired
	ModelMapper modelMapper;
	
	@Transactional
	public Authority get(Long id) 
	throws NotFoundException
	{
		return authRep.findById(id).orElseThrow(() -> 
					new NotFoundException(String.format(MSG_NOT_FOUND, id) ) );
	}
}
