package cms.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import cms.test.mock.MockCognito;
import io.restassured.RestAssured;


//precisa fixar a porta por causa das urls de resource server configuradas no application.properties para acesso ao autentication server
//In the SpringBootTest, we'll use the DEFINED_PORT for the embedded web server
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) 

//@TestPropertySource("/application-test.properties")   //10.13 ambiente de teste - outra base de dados
@ActiveProfiles("test")

@TestInstance(Lifecycle.PER_CLASS) //exigencia do @BeforaAll - melhor assim
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "classpath:schema.sql", executionPhase=ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:data-test.sql", executionPhase=ExecutionPhase.BEFORE_TEST_CLASS) 
public class Api_IT {

	@LocalServerPort
	private int port;
	
	@Autowired
    private MockCognito cmock;
    
	@BeforeAll   //1 vez , antes do inicio dos testes da classe. Precisa declarar na classe: @TestInstance(Lifecycle.PER_CLASS)
	public void setUp() 
	throws Exception 
	{
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		RestAssured.port = port;
		//RestAssured.basePath = "";
		RestAssured.baseURI = "http://localhost:8080";
		
		cmock.setupFor("5418d448-7081-70b4-9b0f-7b96f1a9f1be", "Claudio", "cl.silveira@gmail.com", "read write openid");
	}

	
	@Test
	public void api_deveRetornarStatus200_QuandoListarUsuarios() {
		
		RestAssured.given()
		    .header("Authorization", "Bearer " + cmock.getAccessToken().getTokenValue())
			.basePath("/api/users")
			.baseUri("http://localhost:8080")
			.port(port)
			.accept("application/json")
		.when()
			.get()
		.then()
			.statusCode(200);
	}
}