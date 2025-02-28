package cms.api.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cms.api.model.RoleModel;
import cms.api.model.input.RoleInput;
import cms.domain.model.Role;
import cms.domain.service.RoleService;
import cms.repository.RoleRepository;
import jakarta.validation.Valid;

@RestController()
@RequestMapping("/api/roles")
public class RoleController {

	@Autowired
	RoleRepository roleRep;
	@Autowired
	ModelMapper modelMapper;
	@Autowired
	RoleService roleService;
	
	@GetMapping
	public List<RoleModel>  list()
	{
		List<Role> list = roleRep.findAll();
		
		return list.stream()
				.map(u -> modelMapper.map(u, RoleModel.class))
				.collect(Collectors.toList()); 
	}
	
	@GetMapping("/{id}")
	public RoleModel  recover(@PathVariable long id){

		Role role = roleService.get(id);
		return modelMapper.map(role, RoleModel.class);
	}
	
//	@GetMapping("/{id}")
//	public ResponseEntity<RoleModel>  buscar(@PathVariable long id){
//		
//		Optional<Role> op = rep.findById(id);
//		
//		if (op.isEmpty()) 
//			return  ResponseEntity.notFound().build();
//		
//		return ResponseEntity.ok(modelMapper.map(op.get(), RoleModel.class));
//	}	
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RoleModel create(@RequestBody @Valid RoleInput input) {
		
		Role domainRole = modelMapper.map(input, Role.class);
		return roleService.create(domainRole);
	}
	
	
	@PutMapping("/{id}")
	public RoleModel update(@PathVariable long id,
			                @RequestBody @Valid RoleInput input) 
	{
		Role domainRole = roleService.get(id);
		modelMapper.map(input, domainRole);
		
		domainRole = roleService.update(domainRole);
		
		return modelMapper.map(domainRole, RoleModel.class);
	}

	
// Eliminado na refatoracao com exceções anotadas com ResponseStatus.		
//	@PutMapping("/{id}")
//	public ResponseEntity<?> atualizar(@PathVariable long id,
//			                   @RequestBody @Valid RoleInput input) 
//	{
//		Optional<Role> op = rep.findById(id);
//		
//		if (op.isEmpty()) 
//			return  ResponseEntity
//					.status(HttpStatus.NOT_FOUND)
//					.body(String.format(MSG_NOT_FOUND, id));
//
//		Role domainRole = op.get();
//		
//		modelMapper.map(input, domainRole);
//		
//		domainRole = userRep.save(domainRole);	
//		
//		return ResponseEntity.ok( 
//				modelMapper.map(domainRole, RoleModel.class) );
//	}
	
	
	@DeleteMapping("/{id}")
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long  id) 
	{
		roleService.delete(id);
	}
}
