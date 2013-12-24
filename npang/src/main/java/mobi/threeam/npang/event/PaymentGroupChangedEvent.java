package mobi.threeam.npang.event;

import mobi.threeam.npang.database.model.PaymentGroup;

/**
 * Created by jangc on 2013. 12. 24..
 */
public class PaymentGroupChangedEvent {
    public PaymentGroup group;

    public PaymentGroupChangedEvent(PaymentGroup group) {
        this.group = group;
    }
}
