package io.ph.bot.events;

public class CustomEventDispatcher {
	public void dispatch(MomoEvent e) {
		e.handle();
	}
}
