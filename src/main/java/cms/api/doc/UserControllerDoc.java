package cms.api.doc;

import java.util.List;

import cms.api.model.UserModel;
import cms.api.model.UserRoleModel;
import cms.api.model.input.UserInputCreate;
import cms.api.model.input.UserInputUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Usuários")
public interface UserControllerDoc {

	//Na minha opnião não precisamos definir NENHUMA resposta, como feito para exemplificar abaixo.
	//As respostas de SUCESSO são montadas corretamente.
	//As respostas de ERRO, definidas Globalmente, são sufucientes.
	//Inclusive, as definições Globais tem precedencia.
	
	//!!! MAS !!!!!! deve haver no projeto ao menos uma definição de respostas assim para
	// que o SCHEMA 'Problema' seja reconhecido pelo Swagger.!!!
	// só declarar o schema, no springdoc, nao eh suficiente para o Swagger.
		//OBS: nao precisa mais na versão 2.8.5 do spring Doc

	// Se na anotação @RequestMapping do Controller indicarmos o "produces" ,
	// o SpringDoc vai identificar o media type correto para todas as resoistas 2XX OK. 
	// EX: @RequestMapping(value = "/cidades" , produces = MediaType.APPLICATION_JSON_VALUE)
	
	/*
	 * 26.21  exemplo de como podemos definir parametros mesmo nao existindo nao operação.
 
		@Operation(parameters = {
					@Parameter(name = "projecao",
							   description = "Nome da projeção",
							   example = "apenas-nome",
							   in = ParameterIn.QUERY,
							   required = false )
		})
		 
	 */
	
	
	@Operation(summary = "Lista Usuários")
	List<UserModel> list(
			@Parameter(description = "Email para filtrar o resultado (OPCIONAL).") //'required = false' nao funciona: https://github.com/springdoc/springdoc-openapi/issues/252
			String email);

	@Operation(summary = "Busca um Usuário pelo ser OIDC Id (OpenId Connect ID)")
	UserRoleModel recover(
			@Parameter(description = "Identificação do Usuário (OidcId).")
			String oidcId);

	@Operation(summary = "Cria um Usuário")
	UserModel create(
			@RequestBody(description = "Representação de um novo Usuário.") 
			UserInputCreate user);

	@Operation(summary = "Atualiza um Usuário")
	UserModel update(String oidcId, UserInputUpdate userInput);

	@Operation(summary = "Deleta um Usuário")
	void delete(String oidcId);

}