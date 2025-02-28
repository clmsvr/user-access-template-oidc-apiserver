package cms.api.advice;


import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;

import cms.api.controller.UserController;
import cms.api.exceptions.ValidationException;
import cms.exceptions.BusinessException;
import cms.exceptions.InUseException;
import cms.exceptions.NotFoundChildException;
import cms.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UnsupportedUserStateException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;


//8.15) Extender a classe ResponseEntityExceptionHandler objetiva 
//customizar o corpo das respostas para as exceções internas do Spring.
//ResponseEntityExceptionHandler eh uma classe abstrata que podemos extender para 
//customizar estas exceptions do spring.
//Nao eha implementacao default do String. É somente uma classe auxiliar.

@Slf4j
@ControllerAdvice(basePackageClasses = UserController.class) //Another way to specify a package via the basePackageClasses property which will enable @ControllerAdvice to all controllers inside the package that the class (or interface) lives in.
public class ApiExceptionHandlerControllerAdvice extends ResponseEntityExceptionHandler{

	//9.11
	@Autowired
	private MessageSource messageSource;

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
			Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) 
	{
		//Implementando o padrão da RFC7807 na resposta
		
		if (body == null || body instanceof String) 
		{
			String title = status instanceof HttpStatus httpstatus ? 
				                           httpstatus.getReasonPhrase() : status.toString();
			body = new Problem(title, status.value());
		} 
		
		return super.handleExceptionInternal(ex, body, headers, status, request);
	}	
	
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<?> handleDomainException(BusinessException e, WebRequest request) 
	{
		ProblemTypeTitleStatus type = ProblemTypeTitleStatus.BUSINESS_ERROR;
		
		if (e instanceof InUseException)          type = ProblemTypeTitleStatus.IN_USE;
		if (e instanceof NotFoundException)       type = ProblemTypeTitleStatus.NOT_FOUND;
		if (e instanceof NotFoundChildException)  type = ProblemTypeTitleStatus.NOT_FOUND_CHILD;
		
		Problem problem = new Problem(type, e.getMessage());
		
	    return handleExceptionInternal(e, problem, new HttpHeaders(), type.getStatus(), request);
	}	
	
	@ExceptionHandler(CognitoIdentityProviderException.class)
	public ResponseEntity<?> handleCognitoException(CognitoIdentityProviderException e, WebRequest request) 
	{
		ProblemTypeTitleStatus type = ProblemTypeTitleStatus.INTERNAL;
		String detail = "Falha na operação com o pool de usuários.";
		
		if (e instanceof UsernameExistsException) {
			type = ProblemTypeTitleStatus.INVALID_DATA;
			detail = "usuário com este email já existe.";
		}
		if (e instanceof InvalidParameterException) {
			type = ProblemTypeTitleStatus.INVALID_DATA;
			detail = "formato de email inválido.";
		}
		if (e instanceof UserNotFoundException) {
			type = ProblemTypeTitleStatus.NOT_FOUND;
			detail = "usuário nao encontrado no pool de usuários.";
		}
		if (e instanceof UnsupportedUserStateException) {
			type = ProblemTypeTitleStatus.NOT_FOUND;
			detail = "Não é mais permitido redefinir o Usuario.";
		}
		
		Problem problem = new Problem(type, detail);
		
	    return handleExceptionInternal(e, problem, new HttpHeaders(), type.getStatus(), request);
	}	
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) 
	{
		Throwable rootCause = ExceptionUtils.getRootCause(ex);
		
		//8.21) melhorando o tratamento de erro para quando a exceção raiz for InvalidFormatException
		if (rootCause instanceof InvalidFormatException e) {
			return handleInvalidFormatException(e, headers, status, request);
		}
		//8.22 23)
		else if (rootCause instanceof PropertyBindingException e) {
			return handlePropertyBindingException(e, headers, status, request);
		}
			
		ProblemTypeTitleStatus type = ProblemTypeTitleStatus.MSG_UNKNOW;
		String detail = "O corpo da requisição está inválido. Verifique erro de sintaxe.";
		
		Problem problem = new Problem(type, detail);
		problem.setStatus(status.value());
		
		return handleExceptionInternal(ex, problem, headers, status , request);
	}
	
	//8.22 23)
	private ResponseEntity<Object> handlePropertyBindingException(PropertyBindingException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) 
	{
		String path = joinPath(ex.getPath());
		
		ProblemTypeTitleStatus type = ProblemTypeTitleStatus.MSG_UNKNOW;
		String detail = String.format("O uso da proriedade '%s' não é permitido.",path);//ex.getPropertyName());
		
		Problem problem = new Problem(type, detail);
		problem.setStatus(status.value());
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	private ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) 
	{
		ProblemTypeTitleStatus type = ProblemTypeTitleStatus.MSG_UNKNOW;
		
		//descobriu isso depurando a exceção
	    // Criei o método joinPath para reaproveitar em todos os métodos que precisam
	    // concatenar os nomes das propriedades (separando por ".")
	    String path = joinPath(ex.getPath());
		
		String detail = String.format("A propriedade '%s' recebeu o valor '%s', "
				+ "que é de um tipo inválido. Corrija e informe um valor compatível com o tipo %s.",
				path, ex.getValue(), ex.getTargetType().getSimpleName());
		
		Problem problem = new Problem(type, detail);
		problem.setStatus(status.value());
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	private String joinPath(List<Reference> references) {
	    return references.stream()
	        .map(ref -> ref.getFieldName())
	        .collect(Collectors.joining("."));
	}
	
	//8.25
	@Override
	protected ResponseEntity<Object> handleTypeMismatch(
				TypeMismatchException ex, HttpHeaders headers,
				HttpStatusCode status, WebRequest request) 
	{
		
		String detail = "O parâmetro de URL '%s' recebeu o valor '%s', que é de um tipo inválido. Corrija e informe um valor compatível com o tipo %s.";
		detail = String.format(detail, ex.getPropertyName(), ex.getValue(), ex.getRequiredType().getSimpleName());

		ProblemTypeTitleStatus type = ProblemTypeTitleStatus.INVALID_URL_PARM;
		
		Problem problem = new Problem(type, detail);
		problem.setStatus(status.value());
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}	
	
	//Erro gerado ao se acessar uma url de recurso inexistente.
	//Por default, o spring trata o problema e nao lança uma exception.
	//Para tratar esta exception foi preciso adicionar 2 chaves no application.properties.
	// - spring.mvc.throw-exception-if-no-handler-found=true
	// - spring.web.resources.add-mappings=false
	//
	//Deprecated: spring.mvc.throw-exception-if-no-handler-found 
	// nao precisa mais. agora eh comportamento padrao lancar a exceção ao invéz do 404.
	// https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.content-negotiation
    // https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4.0-M1-Configuration-Changelog
	// OLD: https://docs.spring.io/spring-boot/docs/3.2.5/reference/html/web.html
	//8.26
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
				NoHandlerFoundException ex, HttpHeaders headers,
				HttpStatusCode status, WebRequest request) 
	{
		ProblemTypeTitleStatus type = ProblemTypeTitleStatus.NOT_FOUND;
	    String detail = String.format("O recurso %s, que você tentou acessar, é inexistente.", 
	            ex.getRequestURL());
	    
	    Problem problem = new Problem(type, detail);
	    
	    return handleExceptionInternal(ex, problem, headers, type.getStatus(), request);
	}
	
	//9.19. Executando processo de validação programaticamente
	//Exception lancada na validacao manual do metodo PATH de restaurante
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<Object> handleValidacaoException(
			ValidationException ex, WebRequest request) 
	{
		return handleValidationInternal(ex, ex.getBindingResult(), 
				new HttpHeaders(), request);
	}
	
	//Erros de validacao das anotações do Bean Validation
	//9.2 3
	//9.11
	//13.7. Tratando BindException ao enviar parâmetros de URL inválidos
	//13.7 nao temos mais handleBindException() DEPRECADO. Este tratamento substitui.
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) 
	{
		BindingResult bindingResult = ex.getBindingResult();
		return handleValidationInternal(ex, bindingResult, headers, request);
	}
	
	private ResponseEntity<Object> handleValidationInternal(
			Exception ex, BindingResult bindingResult, HttpHeaders headers, WebRequest request) 
	{
		ProblemTypeTitleStatus type = ProblemTypeTitleStatus.INVALID_DATA;
		String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.";
        		
	    List<Problem.Field> problemFields = bindingResult.getFieldErrors().stream()
	    		.map(fieldError -> {
	    			//9.11
	    			//9.18 mais detalhes: nao essta pegando os erros globais: bindingResult.getAllErrors()
	    			//9.18. Ajustando Exception Handler para adicionar mensagens de validação em nível de classe
	    			String message = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
    				return new Problem.Field( fieldError.getField(), message);
					//9.11
					//fieldError.getDefaultMessage() //nao pega as mensagens do resource bundle: message.properties
					                                 //mas pega as mensagens customizadas diretamente na anotação.
    				
	    		}).collect(Collectors.toList());
	    
	    Problem problem = new Problem(type, detail, problemFields);
	    
	    return handleExceptionInternal(ex, problem, headers, type.getStatus(), request);		
	}	

	//acho que foi uma iniciativa de implementar minha.
	//Este handler nao existe na versão 3.1 do spring boot. 
	//Metodo novo na classe pai para manipular MaxUploadSizeExceededException
	@Override
	protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
			MaxUploadSizeExceededException e, HttpHeaders headers, 
			HttpStatusCode status, WebRequest request) 
	{
		Throwable rootCause = ExceptionUtils.getRootCause(e);
		
		ProblemTypeTitleStatus type = ProblemTypeTitleStatus.INVALID_DATA;
		String detail = "Tamanho máximo do upload excedido.";
		
		if (rootCause instanceof FileSizeLimitExceededException fe) {
			detail = String.format(
					"Excedido o tamanho máximo de %d Kb para upload do arquivo [%s].",
					fe.getPermittedSize()/1024,fe.getFileName());
		}
		
		Problem problem = new Problem(type, detail);
		problem.setStatus(status.value());
		
	    return handleExceptionInternal(e, problem, new HttpHeaders(), status, request);
	}	
	
	//23.22
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<?> handleAccessDeniedException( 
			AccessDeniedException e, WebRequest request)//, HttpHeaders paradarerro) 
	{
		//nao consegui capturar o header "WWW-Authenticate" que eh gerado na resosta default com uma otima mensagem de erro.
		ProblemTypeTitleStatus type = ProblemTypeTitleStatus.ACCESS_DENIED;
		Problem problem = new Problem(type, e.getMessage());
		
	    return handleExceptionInternal(e, problem, new HttpHeaders(), type.getStatus(), request);
	}	
	
	//14.16 - nao podemos retornar um objeto para ser serializado em JSON pois o cliente 
	//provavelmente nao aceita json, ou está pedindo outro meia type com por ex uma imagem.
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
			HttpMediaTypeNotAcceptableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) 
	{
		return ResponseEntity.status(status).headers(headers).build();
	}
	
	//8.27) Para todas as Exceptions nao Tratadas  
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) 
	{
	    ProblemTypeTitleStatus type = ProblemTypeTitleStatus.INTERNAL;
	    String detail = "Ocorreu um erro interno inesperado no sistema. "
	            + "Tente novamente e se o problema persistir, entre em contato "
	            + "com o administrador do sistema.";

	    // Importante colocar o printStackTrace (pelo menos por enquanto, que não estamos
	    // fazendo logging) para mostrar a stacktrace no console
	    // Se não fizer isso, você não vai ver a stacktrace de exceptions que seriam importantes
	    // para você durante a fase de desenvolvimento
	    log.error(ex.getMessage(), ex);
	    
	    Problem problem = new Problem(type, detail);

	    return handleExceptionInternal(ex, problem, new HttpHeaders(), type.getStatus(), request);
	}  		
}