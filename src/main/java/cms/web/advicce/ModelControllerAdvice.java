package cms.web.advicce;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import cms.domain.Consts;
import cms.repository.UserRepository;
import cms.web.controller.HomeController;
import jakarta.servlet.http.HttpSession;

@ControllerAdvice(basePackageClasses = HomeController.class) //Another way to specify a package via the basePackageClasses property which will enable @ControllerAdvice to all controllers inside the package that the class (or interface) lives in.
public class ModelControllerAdvice {

	boolean needUpdate = false;
	
    @Autowired
    OAuth2AuthorizedClientService authorizedClientService;
    @Autowired
    UserRepository userRep;

    public static final String DOMAIN_USER         = "domainUser";
    public static final String ACCESS_TOKEN        = "accessToken";
    public static final String STATES              = "states";
    public static final String LOCAL_PROVIDER_NAME = "localProviderName";
    
    private static HashMap<String, String> states = new HashMap<>();
    static{
        states.put("AC","Acre");
        states.put("AL","Alagoas");
        states.put("AP","Amapá");
        states.put("AM","Amazonas");
        states.put("BA","Bahia");
        states.put("CE","Ceará");
        states.put("DF","Distrito Federal");
        states.put("ES","Espirito Santo");
        states.put("GO","Goiás");
        states.put("MA","Maranhão");
        states.put("MS","Mato Grosso do Sul");
        states.put("MT","Mato Grosso");
        states.put("MG","Minas Gerais");
        states.put("PA","Pará");
        states.put("PB","Paraíba");
        states.put("PR","Paraná");
        states.put("PE","Pernambuco");
        states.put("PI","Piauí");
        states.put("RJ","Rio de Janeiro");
        states.put("RN","Rio Grande do Norte");
        states.put("RS","Rio Grande do Sul");
        states.put("RO","Rondônia");
        states.put("RR","Roraima");
        states.put("SC","Santa Catarina");
        states.put("SP","São Paulo");
        states.put("SE","Sergipe");
        states.put("TO","Tocantins");
    }

	@ModelAttribute
	public void addAttributes(Model model, HttpSession session , OAuth2AuthenticationToken principal) 
	{
		model.addAttribute(STATES, states);
		
		if(principal != null) 
		{
			OAuth2User oauthUser = principal.getPrincipal();
			
			Object userDomain = session.getAttribute(DOMAIN_USER);
			if (userDomain == null || needUpdate) {
				userDomain = userRep.findByOidcId(oauthUser.getName());
				session.setAttribute(DOMAIN_USER, userDomain);
				model.addAttribute(DOMAIN_USER, userDomain);
				needUpdate = false;
			}
			else {
				model.addAttribute(DOMAIN_USER, userDomain);
			}
		    
			var oAuth2AuthorizedClient = authorizedClientService.loadAuthorizedClient(
					principal.getAuthorizedClientRegistrationId(), 
					oauthUser.getName());
			
			if (oAuth2AuthorizedClient != null) {//condições de problemas com sessão,restarts,etc
				String accessToken = oAuth2AuthorizedClient.getAccessToken().getTokenValue();
		    	model.addAttribute(ACCESS_TOKEN, accessToken );
			}
			
			model.addAttribute(LOCAL_PROVIDER_NAME, Consts.LOCAL_PROVIDER_NAME);
		}
	}

	/**
	 * Para indicar ao ModelAdvice que houve alguma mudança em um dos objetos que gerencia.
	 * Portanto, se o objeto é guardado na sessão, ele deve ser atualizado.
	 */
	public void invalidate() {
		needUpdate = true;
	}
	
}
