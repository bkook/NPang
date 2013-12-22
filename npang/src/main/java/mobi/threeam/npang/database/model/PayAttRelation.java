package mobi.threeam.npang.database.model;

import mobi.threeam.npang.database.dao.PayAttRelationDao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(daoClass=PayAttRelationDao.class)
public class PayAttRelation {
	@DatabaseField(generatedId = true)
	public long id;
	@DatabaseField(foreign = true, canBeNull = false)
	public Payment payment;
	@DatabaseField(foreign = true, canBeNull = false)
	public Attendee attendee;
	
	public PayAttRelation() {
	}
	
	public PayAttRelation(Payment payment, Attendee attendee) {
		this.payment = payment;
		this.attendee = attendee;
	}
}
