package io.ph.bot.exception;

import java.net.URL;

public class FileTooLargeException extends Exception {
	private static final long serialVersionUID = 1077370676532367238L;
	private URL urlRequested;
	
	public FileTooLargeException(URL url) {
		super();
		this.urlRequested = url;
	}
	public URL getUrlRequested() {
		return this.urlRequested;
	}
}
