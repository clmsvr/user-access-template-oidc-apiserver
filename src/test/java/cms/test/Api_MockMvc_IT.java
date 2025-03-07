package cms.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.nimbusds.jose.JOSEException;

import cms.test.mock.MockAuthentication;
import cms.test.mock.MockCognito;
import io.restassured.RestAssured;

/*
https://spring.io/guides/gs/testing-web

Another useful approach is to not start the server at all but to test only the layer below that, 
where Spring handles the incoming HTTP request and hands it off to your controller. 
That way, almost all of the full stack is used, and your code will be called in exactly the same way 
as if it were processing a real HTTP request but without the cost of starting the server. 
To do that, use Spring’s 
 - @MockMvc 
and ask for that to be injected for you  and use the 
 - @AutoConfigureMockMvc 
annotation on the test case.

In this test, the full Spring application context is started but without the server. 
We can narrow the tests to only the web layer by using 
 - @WebMvcTest

With @WebMvcTest, Spring Boot instantiates only the web layer rather than the whole context. 
In an application with multiple controllers, you can even ask for only one to be instantiated by using, 
for example, @WebMvcTest(HomeController.class).

Se usar @WebMvcTest , temos de mocar as dependencias com:
 - @MockitoBean
 */

@SpringBootTest
@AutoConfigureMockMvc

//@TestPropertySource("/application-test.properties")   //10.13 ambiente de teste - outra base de dados
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS) //exigencia do @BeforaAll - melhor assim
@Sql(scripts = "classpath:schema.sql", executionPhase=ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:data-test.sql", executionPhase=ExecutionPhase.BEFORE_TEST_CLASS) 
public class Api_MockMvc_IT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MockAuthentication authTool;

//	@MockitoBean
//	private UserRepository rep;

    String oidcid = "5418d448-7081-70b4-9b0f-7b96f1a9f1be";
	
    private Authentication authentication;
    
    @BeforeAll
    public void setUpUser() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, JOSEException, IOException 
    {
    	
    	List<String> list  = Arrays.asList("ROLE_Admin");
    	//List<String> places = Collections.singletonList("Buenos Aires");
    	//List<String> strings = List.of("foo", "bar", "baz");
    	
    	authentication = MockAuthentication.getJwtAuthenticationToken(
                oidcid, "Claudio M S",  "cl.silveira@gmail.com", list);
    }

    @Test
    public void web_consultarPaginaAdmin_comUsuarioDeRoleAdmin_deveRetornarPaginaAdmin() throws Exception {

    	assertThat(mvc).isNotNull();//exemplo
    	
        MvcResult result = mvc.perform(get("/api/users").with(authentication(authentication)))
           //.andDo(print())
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("cl.silveira@gmail.com")))
           .andExpect(content().string(containsString(oidcid)))
           .andReturn();
        
        MockHttpServletResponse response = result.getResponse();
        String json = response.getContentAsString();
        System.out.println(json);
        
    }

}
