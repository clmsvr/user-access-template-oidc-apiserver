package cms.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.web.context.WebApplicationContext;

import cms.test.mock.MockCognito;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) 

//@TestPropertySource("/application-test.properties")
@ActiveProfiles("test")

@TestInstance(Lifecycle.PER_CLASS) //exigencia do @BeforaAll - melhor assim
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

@Sql(scripts = "classpath:schema.sql", executionPhase=ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:data-test.sql", executionPhase=ExecutionPhase.BEFORE_TEST_CLASS) 

public class Web_IT {

    @Autowired
    private WebApplicationContext context;

    private WebClient webClient;

	@Autowired
    private MockCognito cmock;
    

    @BeforeAll
    public void setup() throws Exception {

		cmock.setupFor("5418d448-7081-70b4-9b0f-7b96f1a9f1be", "Claudio", "cl.silveira@gmail.com","");

		
        webClient = MockMvcWebClientBuilder
                .webAppContextSetup(context, SecurityMockMvcConfigurers.springSecurity())
                .build();
    }
    
    @Test
    public void web_consultarPaginaRaiz_NaoDeveAtivarSeguraca_E_RetornaPaginaWelcome() throws Exception {

        try {
			HtmlPage page = webClient.getPage("http://localhost:8080/");

			System.out.println("##################\n");
			System.out.println(page.asXml());
			System.out.println("##################\n");
			System.out.println(page.asNormalizedText());
			
            assertTrue(page.asXml().contains("Welcome Page. Aberta.") || 
            		   page.asXml().contains("Bem vindo ao nosso sistema!") );//resposta depende se o usuario logou em um teste anterior.
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void web_consultarPaginaAdmin_DeveAtivarSeguraca_E_RetornaPaginaAdmin() throws Exception {

        try {
			HtmlPage page = webClient.getPage("http://localhost:8080/admin");

//			System.out.println("##################\n");
//			System.out.println(page.asXml());
//			System.out.println("##################\n");
//			System.out.println(page.asNormalizedText());
			
            assertTrue(page.asXml().contains("Pagina exclusiva para usuários com privilégios de Administrador."));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
