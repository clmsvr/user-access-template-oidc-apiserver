package cms.api.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cms.api.doc.UserControllerDoc;
import cms.api.model.UserModel;
import cms.api.model.UserRoleModel;
import cms.api.model.input.UserInputCreate;
import cms.api.model.input.UserInputUpdate;
import cms.domain.model.User;
import cms.domain.model.UserRole;
import cms.domain.model.UserUpdateDto;
import cms.domain.service.UserService;
import cms.repository.UserRepository;
import cms.repository.UserRoleRepository;
import jakarta.validation.Valid;

@RestController()
@RequestMapping("/api/users")
public class UserController implements UserControllerDoc {

	@Autowired
	UserRepository userRep;
	@Autowired
	UserRoleRepository userRoleRep;
	@Autowired
	UserService userService;
	@Autowired
	private ModelMapper modelMapper;
	
	
	@Override
	@GetMapping
	public List<UserModel>  list(@RequestParam(required = false) String email)
	{
		List<User> list = null;
		if (StringUtils.hasText(email)) 
		{
			list = userRep.findByEmail(email);
		}
		else 
		{
			list = userRep.findAll();
		}
		return list.stream()
				.map(u -> modelMapper.map(u, UserModel.class))
				.collect(Collectors.toList()); 
	}
	
	@Override
	@GetMapping("/{oidcId}")
	public UserRoleModel  recover(@PathVariable String oidcId){
		
		UserRole user = userRoleRep.findByOidcId(oidcId);
		
		user = userService.recoverUserRole(oidcId);
		
		return modelMapper.map(user, UserRoleModel.class);
	}
	
	@Override
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserModel create(@RequestBody @Valid UserInputCreate user) 
	{
		User domainUser = modelMapper.map(user, User.class);
		
		domainUser = userService.create(domainUser);
		
		return modelMapper.map(domainUser, UserModel.class);
	}
	
	
	@Override
	@PutMapping("/{oidcId}")
	public UserModel update(@PathVariable String oidcId,
			                @RequestBody @Valid UserInputUpdate userInput) 
	{
		UserUpdateDto dto = modelMapper.map(userInput, UserUpdateDto.class);
		
		User domainUser = userService.update(dto, oidcId);
		
		return modelMapper.map(domainUser, UserModel.class);
	}

	
	@Override
	@DeleteMapping("/{oidcId}")
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String oidcId) 
	{
		userService.delete(oidcId);
	}
}
