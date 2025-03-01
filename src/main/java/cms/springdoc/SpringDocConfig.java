package cms.springdoc;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cms.api.advice.Problem;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

/*
Swagger - SpringDoc

	http://server:port/context-path/swagger-ui.html 
	http://server:port/context-path/v3/api-docs
	http://server:port/context-path/v3/api-docs.yaml
	
	http://localhost:8080/swagger-ui.html
	http://localhost:8080/swagger-ui/index.html
	http://localhost:8080/v3/api-docs
	http://localhost:8080/v3/api-docs.yaml
	
	Url de redirect OAUTH2 do Swagger UI: 
		http://localhost:8080/swagger-ui/oauth2-redirect.html
	
	For custom path of the swagger documentation in HTML format, 
	add a custom springdoc property, in your spring-boot configuration file:
	
	# swagger-ui custom path
	springdoc.swagger-ui.path=/swagger-ui.html
	
	# /api-docs endpoint custom path
	springdoc.api-docs.path=/api-docs

*/



//26.5 Configuracao acesso do Swagger UI à aplicacao
// Precisa tambem anotar as interfaces ...OpenApi dos Controlers com : 
//     @SecurityRequirement(name = "security_auth")

@SecurityScheme(name = "security_auth",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(authorizationCode = @OAuthFlow(
                authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
                tokenUrl = "${springdoc.oAuthFlow.tokenUrl}",
                scopes = {
                        @OAuthScope(name = "profile", description = "profile scope"),
                        @OAuthScope(name = "email", description = "email scope"),
                        @OAuthScope(name = "openid", description = "openid scope")
                }
        )))
//26.3
@Configuration
public class SpringDocConfig {

    private static final String badRequestResponse = "BadRequestResponse";
    private static final String notFoundResponse = "NotFoundResponse";
    private static final String notAcceptableResponse = "NotAcceptableResponse";
    private static final String internalServerErrorResponse = "InternalServerErrorResponse";
    private static final String unsupportedMediaType = "UnsupportedMediaTypeResponse";
    private static final String conflictResponse = "ConflictTypeResponse";
    @Bean
    public OpenAPI openAPI() {
    	
    	return new OpenAPI()
                .info(new Info()
                        .title("User Access Template API")
                        .version("v1")
                        .description("API para gerenciamento de usuarios.")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.com")
                        )
                ).externalDocs(new ExternalDocumentation()
                        .description("GitHub")
                        .url("https://github.com/clmsvr/user-access-template-oidc-apiserver")
                        
                )
                .components(new Components()
			            .schemas(gerarSchemas())
			            .responses(gerarResponses())
			    )
                ;
    }
    
//entra para customizar apos o carregamentos de todos os beans e paths    
//adiciona resposta para todos os endpoints    
//    public OpenApiCustomiser openApiCustomiser() {
//        return openApi -> {
//            openApi.getPaths()
//                    .values()
//                    .stream()
//                    .flatMap(pathItem -> pathItem.readOperations().stream())
//                    .forEach(operation -> {
//                        ApiResponses responses = operation.getResponses();
//
//                        ApiResponse apiResponseNaoEncontrado = new ApiResponse().description("Recurso não encontrado");
//                        ApiResponse apiResponseErroInterno = new ApiResponse().description("Erro interno no servidor");
//                        ApiResponse apiResponseSemRepresentacao = new ApiResponse()
//                                .description("Recurso não possui uma representação que poderia ser aceita pelo consumidor");
//
//                        responses.addApiResponse("404", apiResponseNaoEncontrado);
//                        responses.addApiResponse("406", apiResponseSemRepresentacao);
//                        responses.addApiResponse("500", apiResponseErroInterno);
//                    });
//        };
//    }
    
   
    
    //Após a criação do Bean OpenApi, e o carregamentos de todos os paths,
    //o OpenApiCustomizer (interface funcional) é usado para customizar. 
    //Adiciona respoatas de erro para todas a operações, por Verbo http   
    @Bean
    public OpenApiCustomizer openApiCustomiser() {
        return openApi -> {
        	
        	//ordenar os Schemas na apresentacao 
        	//fonte: https://stackoverflow.com/questions/62473023/how-to-sort-the-schemas-on-swagger-ui-springdoc-open-ui
        	//@SuppressWarnings("rawtypes")
			Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            openApi.getComponents().setSchemas(new TreeMap<>(schemas));
        	
            openApi.getPaths()
            .values()
            .forEach(pathItem -> pathItem.readOperationsMap()
        	     .forEach((httpMethod, operation) -> {
        	    	
        	    	//Estas respostas tem precedencia sobre as definições no Controler.
                	ApiResponses responses = operation.getResponses();
                    switch (httpMethod) {
                        case GET:
                        	responses.addApiResponse("404", new ApiResponse().$ref(notFoundResponse));
                        	responses.addApiResponse("400", new ApiResponse().$ref(badRequestResponse));

                        	responses.addApiResponse("406", new ApiResponse().$ref(notAcceptableResponse));//
                            responses.addApiResponse("500", new ApiResponse().$ref(internalServerErrorResponse));//
                            break;
                        case POST:
                            responses.addApiResponse("400", new ApiResponse().$ref(badRequestResponse));
                            responses.addApiResponse("406", new ApiResponse().$ref(notAcceptableResponse));
                            responses.addApiResponse("415", new ApiResponse().$ref(unsupportedMediaType));
                            responses.addApiResponse("500", new ApiResponse().$ref(internalServerErrorResponse));
                            break;
                        case PUT:
                        	responses.addApiResponse("404", new ApiResponse().$ref(notFoundResponse));
                            
                        	responses.addApiResponse("400", new ApiResponse().$ref(badRequestResponse));
                            responses.addApiResponse("406", new ApiResponse().$ref(notAcceptableResponse));
                            responses.addApiResponse("415", new ApiResponse().$ref(unsupportedMediaType));
                            responses.addApiResponse("500", new ApiResponse().$ref(internalServerErrorResponse));
                            break;
                        case DELETE:
                        	responses.addApiResponse("404", new ApiResponse().$ref(conflictResponse));
                        	
                        	responses.addApiResponse("409", new ApiResponse().$ref(badRequestResponse));
                        	responses.addApiResponse("400", new ApiResponse().$ref(badRequestResponse));
                            responses.addApiResponse("500", new ApiResponse().$ref(internalServerErrorResponse));
                            break;
                        default:
                            responses.addApiResponse("500", new ApiResponse().$ref(internalServerErrorResponse));
                            break;
                    }
                })
            );
        };
    }
    
    // só declarar o schema, no springdoc, nao eh suficiente para o Swagger reconhecer.
    // Ele tem que ser usado/referenciado nos controlers.
    @SuppressWarnings("rawtypes")
	private Map<String, Schema> gerarSchemas() {
        final Map<String, Schema> schemaMap = new HashMap<>();

        Map<String, Schema> problemSchema = ModelConverters.getInstance().read(Problem.class);
        Map<String, Schema> problemFieldsSchema = ModelConverters.getInstance().read(Problem.Field.class);
        
        schemaMap.putAll(problemSchema);
        schemaMap.putAll(problemFieldsSchema);
        
        return schemaMap;
    }

    private Map<String, ApiResponse> gerarResponses() 
    {
        final Map<String, ApiResponse> apiResponseMap = new HashMap<>();

        Content content = new Content().addMediaType(
        		org.springframework.http.MediaType.APPLICATION_JSON_VALUE, // "application/json"
                new MediaType().schema(new Schema<Problem>().$ref(SchemaNames.PROBLEM)) );

        apiResponseMap.put(badRequestResponse, new ApiResponse() //400
                .description("Requisição inválida")
                .content(content));

        apiResponseMap.put(notFoundResponse, new ApiResponse() //404
                .description("Recurso não encontrado")
                .content(content));

        apiResponseMap.put(notAcceptableResponse, new ApiResponse() //406
                .description("Recurso não possui representação que poderia ser aceita pelo consumidor")
                );//.content(content));

        apiResponseMap.put(conflictResponse, new ApiResponse() //409
                .description("Recurso não pode ser removido pois está em uso.")
                );//.content(content));
        
        apiResponseMap.put(unsupportedMediaType, new ApiResponse() //415
                .description("Requisição recusada porque o corpo está em um formato não suportado")
                .content(content));
        
        apiResponseMap.put(internalServerErrorResponse, new ApiResponse() //500
                .description("Erro interno no servidor")
                .content(content));

        return apiResponseMap;
    }
    
}