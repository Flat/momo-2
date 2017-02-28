package io.ph.bot.exception;

public class BadCharacterException extends Exception {
	private static final long serialVersionUID = -2441190545348546517L;

	public BadCharacterException() {
		super();
	}

	public BadCharacterException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public BadCharacterException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public BadCharacterException(String arg0) {
		super(arg0);
	}

	public BadCharacterException(Throwable arg0) {
		super(arg0);
	}

}
