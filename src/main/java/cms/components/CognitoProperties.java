package cms.components;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@Validated
@ConfigurationProperties("cognito")
public class CognitoProperties {

    @NotBlank
    private String poolId;
    @NotBlank
    private String clientId;    
    @NotBlank
	private String idChaveAcesso;
    @NotBlank
	private String chaveAcessoSecreta;
    @NotBlank
	private String regiao;    
    @NotBlank
    private String endSessionEndpoint;

}