package io.ph.bot.exception;

public class InvalidHistoricalInputException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4139408648519385511L;
	
	public InvalidHistoricalInputException() {
		super();
	}

	public InvalidHistoricalInputException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public InvalidHistoricalInputException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public InvalidHistoricalInputException(String arg0) {
		super(arg0);
	}

	public InvalidHistoricalInputException(Throwable arg0) {
		super(arg0);
	}
}
