package mobi.threeam.npang.database.model;


import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import mobi.threeam.npang.database.dao.PaymentGroupDao;

@DatabaseTable(daoClass = PaymentGroupDao.class)
public class PaymentGroup {
	public static final int STATE_NONE = 0;
	public static final int STATE_CALCULATED = 1;

	@DatabaseField(generatedId=true)
	public long id;
	
	@DatabaseField
	public String title;
	
	@DatabaseField
	public int totalAmount;
	
	@DatabaseField
	public String bankName;
	
	@DatabaseField
	public String bankAccount;
	
	@DatabaseField
	public boolean completed;
	
	@DatabaseField
	public Date createdAt;
	
	@DatabaseField
	public int state;
	
	@DatabaseField
	public String locale;

    @DatabaseField
    public int alarmPeriod;

    @DatabaseField
    public Date alarmTime;

    @DatabaseField
    public boolean alarmEnabled;

	@ForeignCollectionField(eager=false, orderAscending=true)
	public ForeignCollection<Payment> payments;

	@ForeignCollectionField(eager=false, orderAscending=true)
	public ForeignCollection<Attendee> attendees;

	public Payment getLastPayment() {
		if (payments == null || payments.size() == 0) {
			return null;
		}
		Payment lastPayment = null;
		for (Payment payment: payments) {
			lastPayment = payment;
		}
		return lastPayment;
	}
}
