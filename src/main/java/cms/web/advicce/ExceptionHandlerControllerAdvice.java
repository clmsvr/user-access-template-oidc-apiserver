package cms.web.advicce;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import cms.web.controller.HomeController;
import cms.web.exceptions.BadRequestException;
import cms.web.exceptions.InternalErrorException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice(basePackageClasses = {HomeController.class})  //Another way to specify a package via the basePackageClasses property which will enable @ControllerAdvice to all controllers inside the package that the class (or interface) lives in.
public class ExceptionHandlerControllerAdvice {

	/**
	 * Throwable
	 * Clausula para pegar qualquer exception nao tratada.
	 */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String exception(final Throwable throwable, final Model model) 
    {
    	log.error(throwable.toString(),throwable);
        String errorMessage = (throwable != null ? throwable.toString() : "Unknown error");
        model.addAttribute("errorMessage", "Desculpe! Ocorreu um erro inesperado: "+errorMessage);
        
        return "error";
    }
    
    /**
     * NoResourceFoundException
     * exception lançada quando o usuario tenta uma url inexistente.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String noresource(final NoResourceFoundException e, final Model model) 
    {
    	log.warn(e.toString());
        model.addAttribute("errorMessage", "Recusro Inesistente: "+e.getMessage());
        
        return "error";
    }
    
    /**
     * BadRequestException
     * Api exception
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badRequessst(final BadRequestException e, final Model model) 
    {
    	log.warn(e.toString());
        model.addAttribute("errorMessage", "Requisição Inválida: "+e.getMessage());
        
        return "error";
    }
    
    /**
     * InternalErrorException
     * Api exception
     */    
    @ExceptionHandler(InternalErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String badRequessst(final InternalErrorException e, final Model model) 
    {
    	log.error(e.toString());
        model.addAttribute("errorMessage", "Desculpe! Ocorreu uma falha inesperada: "+e.getMessage());
        
        return "error";
    }
}