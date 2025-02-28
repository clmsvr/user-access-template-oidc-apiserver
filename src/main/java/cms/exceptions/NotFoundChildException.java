package cms.exceptions;

//@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class NotFoundChildException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public NotFoundChildException(String message) {
		super(message);
	}
}
