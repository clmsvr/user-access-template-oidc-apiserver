package cms.web.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cms.domain.model.User;
import cms.domain.service.UserMngService;
import cms.exceptions.InvalidProviderException;
import cms.exceptions.NotFoundException;
import cms.web.advicce.ModelControllerAdvice;
import cms.web.exceptions.BadRequestException;
import cms.web.model.PwdChange;
import cms.web.model.UserApi;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.PasswordResetRequiredException;

@Slf4j
@Controller
@RequestMapping("/user/mng")
public class UserMngController 
{ 
	@Autowired
	private UserMngService userService;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	ModelControllerAdvice modelAdvice;
	
    @GetMapping({"/account"})
    public String account(Model model) 
    {
        //Usuario TEM que estar logado. Acesso restrito
    	User user = (User)model.getAttribute(ModelControllerAdvice.DOMAIN_USER);
        
    	if (user == null) return "redirect:/"; //condições de problemas com sessão,restarts,etc
    	
        UserApi userModel = new UserApi();
        modelMapper.map(user, userModel);
        
        model.addAttribute("user", userModel);
        return "user/account-form";
    }
    
    @PostMapping({"/account"})
    @Transactional
    public String accountPost(
    		Model model, 
    		@Valid @ModelAttribute("user") UserApi userModel, 
    		BindingResult result, 
    		@ModelAttribute(ModelControllerAdvice.DOMAIN_USER) User doaminUser) 
    {
        if (result.hasErrors())
        {
            log.debug("erro no formulario");
            return "user/account-form";
        }

    	userService.updateUser(userModel,doaminUser);
    	modelAdvice.invalidate();
    	
        return "redirect:/user/mng/account?message=1";
    }    
    
    @GetMapping({"/change-pwd"})
    public String changePwd(Model model) 
    {
        model.addAttribute("pwd", new PwdChange());
        return "user/change-pwd-form";
    }
    
    @PostMapping({"/change-pwd"})
    public String changePwdPost(Model model,
    		@Valid @ModelAttribute("pwd") PwdChange pwd, 
    		BindingResult result, 
    		@AuthenticationPrincipal OAuth2User oauthUser) 
    {
        if (result.hasErrors())
        {
            pwd.reset();
            log.debug("erro no formulario");
            return "user/change-pwd-form";
        }
        
        //Com a anotação "@ValidaSenhasIguais" no Bean, nao precisa deste teste.
        if (pwd.getNewpwd1().equals(pwd.getNewpwd2()) == false)
        {
            pwd.reset();
            //result.rejectValue("newpwd2", "newpwd2", "Senhas não conferem."); 
            result.reject("senhas-diff", "Senhas não conferem."); 
            return "user/change-pwd-form";
        }
        
        if (oauthUser ==  null)
        {
            log.warn("usuario NAO logado tentando alterar senha.");
            throw new BadRequestException();
        }
        
        try {
        	String asscessToken = (String)model.getAttribute(ModelControllerAdvice.ACCESS_TOKEN);
        	User domainUser = (User)model.getAttribute(ModelControllerAdvice.DOMAIN_USER);
			
        	userService.changeUserPassword(asscessToken,pwd, domainUser);
			
        	return "redirect:/user/mng/change-pwd?message=1"; 
		} 
        catch(InvalidProviderException e) {
        	throw new BadRequestException(e);
        }
        catch (NotFoundException e) {
        	throw new BadRequestException("Inconsistencia: usuario logado nao encontrado ");
		} 
        catch (NotAuthorizedException e) {
            //enviar erro de senha invalida
            pwd.reset();
            result.rejectValue("pwd", "pwd", "Operação de mudança de Senha não autorizada."); 
            return "user/change-pwd-form";
		}
        catch (InvalidPasswordException e) {
            //enviar erro de senha invalida
            pwd.reset();
            result.rejectValue("pwd", "pwd", "Senha Incorreta."); 
            return "user/change-pwd-form";
		}
        catch (PasswordResetRequiredException e) {
            //enviar erro de senha invalida
            pwd.reset();
            result.rejectValue("pwd", "pwd", "Não Autorizado. Operação de reset de senha pendente. "); 
            return "user/change-pwd-form";
		}  
        catch (CognitoIdentityProviderException e) {
            //enviar erro de senha invalida
            pwd.reset();
            result.rejectValue("pwd", "pwd", "Falha interna executando operação."); 
            return "user/change-pwd-form";
		}  
    }      
}
