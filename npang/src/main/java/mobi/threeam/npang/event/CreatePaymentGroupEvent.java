package mobi.threeam.npang.event;

import mobi.threeam.npang.database.model.PaymentGroup;

public class CreatePaymentGroupEvent {
	public PaymentGroup group;

	public CreatePaymentGroupEvent(PaymentGroup group) {
		this.group = group;
	}
}
