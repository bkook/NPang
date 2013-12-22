package mobi.threeam.npang.database.model;

import java.util.Date;

import mobi.threeam.npang.database.dao.AttendeeDao;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(daoClass = AttendeeDao.class)
public class Attendee {
	// arREcordId
	
	@DatabaseField(generatedId=true)
	public long id;
	
	@DatabaseField
	public int amount;
	
	@DatabaseField
	public Date createdAt;

	@DatabaseField
	public String name;

	@DatabaseField
	public String photoUri;
	
	@DatabaseField
	public Date paidAt;
	
	@DatabaseField(foreign=true)
	public PaymentGroup paymentGroup;

	@ForeignCollectionField(eager=false)
	public ForeignCollection<PayAttRelation> payments;
}
