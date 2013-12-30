package mobi.threeam.npang.event;

import mobi.threeam.npang.database.model.Payment;

/**
 * Created by jangc on 2013. 12. 28..
 */
public class DeletePaymentEvent {
    public Payment payment;

    public DeletePaymentEvent(Payment payment) {
        this.payment = payment;
    }
}
