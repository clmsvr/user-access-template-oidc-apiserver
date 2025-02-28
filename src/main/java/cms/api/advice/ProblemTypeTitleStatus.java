package cms.api.advice;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ProblemTypeTitleStatus {

	BUSINESS_ERROR(   HttpStatus.BAD_REQUEST, "/erro-negocio", "Violação de regra de negócio"), 
	NOT_FOUND(        HttpStatus.NOT_FOUND, "/recurso-nao-encontrado", "Recurso não encontrado"),  
	NOT_FOUND_CHILD(  HttpStatus.BAD_REQUEST, "/entidade-associada-nao-encontrada", "entidade associada não encontrada"),
	IN_USE(           HttpStatus.CONFLICT, "/entidade-em-uso", "Entidade em uso"),
	ACCESS_DENIED(    HttpStatus.FORBIDDEN, "/acesso-negado","Acesso Negado"),
	INTERNAL(         HttpStatus.INTERNAL_SERVER_ERROR, "/erro-de-sistema", "Erro de sistema"),
	MSG_UNKNOW(       HttpStatus.BAD_REQUEST, "/mensagem-incompreensivel", "Mensagem incompreensível"),
    INVALID_DATA(     HttpStatus.BAD_REQUEST, "/dados-invalidos", "Dados inválidos"),
	INVALID_URL_PARM( HttpStatus.BAD_REQUEST,"/parametro-invalido", "Parâmetro Invalido");
	
	private String        title;
	private String        type;
	private HttpStatus    status;
	
	ProblemTypeTitleStatus(HttpStatus status, String type, String title) {
		this.type = "https://problem.deteil.org" + type;
		this.title = title;
		this.status = status;
	}
}