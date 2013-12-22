package mobi.threeam.npang.event;

import mobi.threeam.npang.database.model.Payment;

public class AddPaymentEvent {
	public Payment payment;

	public AddPaymentEvent(Payment payment) {
		this.payment = payment;
	}
}
