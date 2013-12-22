package mobi.threeam.npang.event;

import mobi.threeam.npang.database.model.PaymentGroup;

public class SetPaymentGroupEvent {
	public PaymentGroup group;

	public SetPaymentGroupEvent(PaymentGroup group) {
		this.group = group;
	}
}
