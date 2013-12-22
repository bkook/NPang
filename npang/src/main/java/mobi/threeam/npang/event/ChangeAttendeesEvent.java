package mobi.threeam.npang.event;

import mobi.threeam.npang.database.model.Payment;

public class ChangeAttendeesEvent {
	public Payment payment;

	public ChangeAttendeesEvent(Payment payment) {
		this.payment = payment;
	}
}
