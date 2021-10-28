package test;
// This custom RuntimeException is used in the DataSample's split() method.
public class InvalidInputException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidInputException(String message) {
		super(message);
	}

}
