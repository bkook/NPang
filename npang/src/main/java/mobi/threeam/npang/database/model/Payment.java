package mobi.threeam.npang.database.model;

import java.util.Date;
import java.util.List;

import mobi.threeam.npang.database.dao.PaymentDao;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(daoClass = PaymentDao.class)
public class Payment {
	@DatabaseField(generatedId=true)
	public long id;
	@DatabaseField
	public String place = "";
	@DatabaseField
	public int amount = 0;

	//	@DatabaseField
//	public int attendeeCount = 0;
//	@DatabaseField
//	public String attendeeNames ;

	@DatabaseField
	public Date createdAt = new Date();

	@DatabaseField(foreign=true, canBeNull=false)
	public PaymentGroup paymentGroup;

	@ForeignCollectionField(eager=false)
	public ForeignCollection<PayAttRelation> attendees;
}