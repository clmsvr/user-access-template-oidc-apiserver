package cms.exceptions;

public class InvalidProviderException extends BusinessException {

	private static final long serialVersionUID = 4880206906470884782L;

	public InvalidProviderException()
    {
    }
    
    public InvalidProviderException(Exception e)
    {
        super(e);
    }
    
    public InvalidProviderException(String msg) {
        super(msg);
    }
    
    public InvalidProviderException(String msg, Exception e)
    {
        super(msg,e);
    }
}