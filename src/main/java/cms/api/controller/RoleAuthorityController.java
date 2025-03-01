package cms.api.controller;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cms.api.doc.RoleAuthorityControllerDoc;
import cms.api.model.AuthorityModel;
import cms.domain.model.Authority;
import cms.domain.model.Role;
import cms.domain.service.RoleService;
import cms.repository.RoleRepository;

@RestController
@RequestMapping(value = "/api/roles/{roleId}/authorities")
public class RoleAuthorityController implements RoleAuthorityControllerDoc{

	@Autowired
	RoleService roleService;
	@Autowired
	RoleRepository roleRep;
	@Autowired
	ModelMapper modelMapper;
	
	@Override
	@GetMapping
	public List<AuthorityModel> list(@PathVariable Long roleId) {
		
		Role role = roleService.get(roleId);
		Set<Authority> authorities = role.getAuthorities();
		
		return authorities.stream()
			.map(a -> modelMapper.map(a, AuthorityModel.class))
			.collect(Collectors.toList());
	}
	
	@Override
	@DeleteMapping("/{authorityId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void unlinkAuthority(
			@PathVariable Long roleId, 
			@PathVariable Long authorityId)
	{
		roleService.unlinkAuthority(roleId, authorityId);
	}
	
	@Override
	@PutMapping("/{authorityId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void linkAuthority(
			@PathVariable Long roleId, 
			@PathVariable Long authorityId)
	{
		roleService.linkAuthority(roleId, authorityId);
	}

}