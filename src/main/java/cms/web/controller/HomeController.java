package cms.web.controller;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cms.domain.model.User;
import cms.web.advicce.ModelControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/")
public class HomeController 
{
    @GetMapping
    public String root(Model model, 
    		@AuthenticationPrincipal OAuth2User oauthUser, 
    		HttpServletRequest request) 
    {
    	User u = (User)model.getAttribute(ModelControllerAdvice.DOMAIN_USER);
    	
    	//verificar se o cookie de sessao recebido ainda tem um usuario cadastrado no banco.
    	//condições de problemas com sessão,restarts,etc
    	if (oauthUser != null)
    	{
    		if (u == null) { // existe sessão mas sem usuario cadastrado
    			try{request.logout();}catch(Exception e) {} //remover a sessao.
    			System.out.println("SESSAO removida.");
    		}
    	}
    	
    	if (u == null )
    		return welcome(model);
    	else
    		return home(model);
    }
    
    private String welcome(Model model)
    { 	
        return "welcome";
    }

    private String home(Model model) 
    {
        return "home";
    }
    
    
    //test
	@GetMapping("/tt")
	@ResponseBody
	public String avaliaSeguranca(Principal p , @AuthenticationPrincipal Object ap) 
	{
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
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

}
