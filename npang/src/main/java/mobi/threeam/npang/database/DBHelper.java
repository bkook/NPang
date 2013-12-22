package mobi.threeam.npang.database;

import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.database.dao.PaymentDao;
import mobi.threeam.npang.database.dao.PaymentGroupDao;
import mobi.threeam.npang.database.model.PayAttRelation;
import mobi.threeam.npang.database.model.Attendee;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DBHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "npang.db";
	private static final int DATABASE_VERSION = 1;
	
	private Context context;
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, PaymentGroup.class);
			TableUtils.createTable(connectionSource, Payment.class);
			TableUtils.createTable(connectionSource, Attendee.class);
			TableUtils.createTable(connectionSource, PayAttRelation.class);
			
			createDefaultPaymentGroup();
		} catch (Exception e) {
			Logger.e(e);
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
//		try {
//			TableUtils.dropTable(connectionSource, Category.class, true);
//			TableUtils.dropTable(connectionSource, PushLog.class, true);
//			onCreate(db, connectionSource);
//		} catch (Exception e) {
//			Logger.e(e);
//		}
	}
	
	private void createDefaultPaymentGroup() throws java.sql.SQLException {
		PaymentGroupDao groupDao = getDao(PaymentGroup.class);
		PaymentGroup group = groupDao.createDefault(context);
		
		PaymentDao paymentDao = getDao(Payment.class);
		paymentDao.addPaymentTo(group);
		if (group.payments == null) {
			groupDao.refresh(group);
		}
		
	}
}
