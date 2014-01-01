package mobi.threeam.npang.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import mobi.threeam.npang.database.dao.PayAttRelationDao;

@DatabaseTable(daoClass=PayAttRelationDao.class)
public class PayAttRelation {
	@DatabaseField(generatedId = true)
	public long id;
    @DatabaseField(foreign = true, canBeNull = false)
    public PaymentGroup paymentGroup;
	@DatabaseField(foreign = true, canBeNull = false)
	public Payment payment;
	@DatabaseField(foreign = true, canBeNull = false)
	public Attendee attendee;

	public PayAttRelation() {
	}
	
	public PayAttRelation(Payment payment, Attendee attendee) {
        this.paymentGroup = payment.paymentGroup;
		this.payment = payment;
		this.attendee = attendee;
	}
}
