package cms.exceptions;

//@ResponseStatus(code = HttpStatus.NOT_FOUND ) //, reason = "Entidade nao encontrada")
public class NotFoundException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public NotFoundException(String message) {
		super(message);
	}
}
