package cms.api.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cms.api.model.AuthorityModel;
import cms.domain.model.Authority;
import cms.repository.AuthorityRepository;

@RestController()
@RequestMapping("/api/authorities")
public class AuthorityController {

	@Autowired
	AuthorityRepository rep;
	@Autowired
	ModelMapper modelMapper;
	
	@GetMapping
	public List<AuthorityModel>  list()
	{
		List<Authority> list = rep.findAll();
		
		return list.stream()
				.map(u -> modelMapper.map(u, AuthorityModel.class))
				.collect(Collectors.toList()); 
	}
}
