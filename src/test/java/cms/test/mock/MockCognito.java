package cms.test.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nimbusds.jose.JOSEException;

import jakarta.annotation.PreDestroy;

@Validated
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MockCognito {
	

	@Autowired
	MockAuthentication authTool;
	

    private WireMockServer wireMockServer;
    
    private Jwt accessToken;
    
    /**
     * Stop Wiremock server after all tests
     */
    @PreDestroy
    void clean() {
        if (wireMockServer != null)wireMockServer.stop();
        System.out.println("DESTROY   COGNITO MOCK.");
    }

    public MockCognito() {

        System.out.println("INIT   COGNITO MOCK !");
        
        
    	WireMockConfiguration config = new WireMockConfiguration()
    			.extensions(CaptureStateTransformer.class)
    	    	.port(8090);
        	
        wireMockServer = new WireMockServer(config);
        wireMockServer.start();
        
        wireMockServer.stubFor(get(urlPathMatching("/issuer.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                        		"""
								{
								    "authorization_endpoint": "http://0.0.0.0:8090/authorize",
								    "end_session_endpoint": "http://0.0.0.0:8090/logout",
								    "id_token_signing_alg_values_supported": [
								        "RS256"
								    ],
								    "issuer": "http://0.0.0.0:8090/issuer",
								    "jwks_uri": "http://0.0.0.0:8090/jwks.json",
								    "response_types_supported": [
								        "code"
								    ],
								    "scopes_supported": [
								        "openid"
								    ],
								    "subject_types_supported": [
								        "public"
								    ],
								    "token_endpoint": "http://0.0.0.0:8090/token",
								    "token_endpoint_auth_methods_supported": [
								        "client_secret_basic"
								    ],
								    "userinfo_endpoint": "http://0.0.0.0:8090/userinfo"
								}
                        		"""
                                )
                )
        );
        
        wireMockServer.stubFor(
    		WireMock.get(WireMock.urlPathMatching("/jwks.json"))
            		.willReturn(
            				WireMock.aResponse()
            				.withStatus(200)
            				.withHeader("Content-Type", "application/json")
	                        .withBody("""
									{
									  "keys": [
									    {
									      "kty": "RSA",
									      "n": "0IzjcCY_o51soshBNJvnc0Zo-iKITHN741gk-_F8h-8xB1AfqcDK-EUQw_CrMZWbb9gdPoFaWiAVL6VPKL0k7ApdX3j1-SYj6knKmTuWEt94BAIMpMAk00UTM-lhUlzhDqS-y1AHteSpLCTfxplGbI2-B2gcDzz4xQm5cZVlL4zYgcBoq8SuUVKn_raUgm69Ca1sTDHRfmcATkEBJq7xafTyCFgGeNTXMyvIFDr0umfp_HRPDJ9x327TQsjpsMU3C2iUnmKo0hjP4CCPUtWLvzXF7UV2NtGWSWNM1i31G500pjcKa8V71yDY2t71SXP9chl81uVKQx7ntb1xNk_KssTRGJEkOUcmkGJK89U5WcBLRu57KseAVTD8oWrpd8VRxkZ7wX-7hP0xO2rleGifEoEp5VBGzRBZRZw-wekuPNhpdlnJa_EmxhZbvc3f1PuCzvR9OFWJk3tQGWs_LvJwHm9Rc3MEs4Jb-sNuPlG26m7O30FEX45XZK1XKhvEJEZns10aUuMfXF8xBgo7SSVunaMmWwJYYaoe_YsE6MLpoM8JsD8vctpLU9yweM7MeN6eM9T-dg53VM001w6yZCOX-hOuhoVTKyGRdAvKSbfKW0KiraoTi6LgIONILct_RqoLvdbKT_LlES3wHwkkpp0fKV56DnDqzdF1KCld_xvc-pE",
									      "e": "AQAB",
									      "ext": true,
									      "kid": "%s",
									      "alg": "RS256",
									      "use": "sig"
									    }
									  ]
									}		                        		
	                        		""".formatted(MockAuthentication.KEY_ID)  ) // 'kid' deve ser igual ao 'kid' que vai no header do token.
            				)
        );

        
        wireMockServer.stubFor(get(urlPathMatching("/authorize.*"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", "http://localhost:8080/login/oauth2/code/cognito?code=my-acccess-code&state=${state}")
                        .withTransformers("CaptureStateTransformer")
                )
        );

        wireMockServer.stubFor(post(urlPathMatching("/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                        		"""
                        		{
                        			"access_token" : "my-access-token" ,
                                    "token_type" : "Bearer" ,
                                    "expires_in" : 8640 
                                }
                        		"""
                                )
                )
        );

    }
    
    public void setupFor(String oidcId, String name, String email, String scope) 
    throws NoSuchAlgorithmException, CertificateException, KeyStoreException, JOSEException, IOException 
    {
        wireMockServer.stubFor(get(urlPathMatching("/userinfo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                        		"""
								{
								    "sub" : "%s",
								    "email_verified" : "true",
								    "name" : "%s",
								    "email" : "%s",
								    "username" : "%s"
								}                      		
                        		""".formatted(oidcId,name,email,oidcId)
                        		)
                )
        );
        
        if (StringUtils.hasText(scope) == false) scope="";
        
        accessToken = authTool.getToken(oidcId, name, email, scope);
    }
    
    public Jwt getAccessToken() 
    {
    	return accessToken;
    }
    
    public Jwt getAccessToken(String oidcId, String name, String email, String scope) 
	throws NoSuchAlgorithmException, CertificateException, KeyStoreException, JOSEException, IOException 
    {
    	if (StringUtils.hasText(scope) == false) scope="";
    	return authTool.getToken(oidcId, name, email, scope);
    }
}
