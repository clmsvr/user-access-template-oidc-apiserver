package cms.exceptions;

//@ResponseStatus(code = HttpStatus.CONFLICT)
public class InUseException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public InUseException(String message) {
		super(message);
	}
}
