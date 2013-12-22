package mobi.threeam.npang.database.dao;


import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;

import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.database.model.Attendee;
import mobi.threeam.npang.database.model.PayAttRelation;
import mobi.threeam.npang.database.model.Payment;

public class PayAttRelationDao extends BaseDaoImpl<PayAttRelation, Long> {

	public PayAttRelationDao(ConnectionSource connectionSource, Class<PayAttRelation> dataClass)
			throws SQLException {
		super(connectionSource, dataClass);
	}

	public PayAttRelationDao(ConnectionSource connectionSource,
			DatabaseTableConfig<PayAttRelation> tableConfig) throws SQLException {
		super(connectionSource, tableConfig);
	}
	
	public boolean delete(Payment payment, Attendee attendee) {
		DeleteBuilder<PayAttRelation, Long> builder = this.deleteBuilder();
		Where<PayAttRelation, Long> where = builder.where();
		try {
			where.eq("payment_id", payment.id);
			where.and();
			where.eq("attendee_id", attendee.id);
			delete(builder.prepare());
		} catch (SQLException e) {
			Logger.e(e);
			return false;
		}
		
		return true;
	}
	
	public boolean copyAttendees(Payment fromPayment, Payment toPayment) {
		boolean copied = false;
		if (fromPayment.attendees != null && fromPayment.attendees.size() > 0) {
			for (PayAttRelation relation : fromPayment.attendees) {
				PayAttRelation newItem = new PayAttRelation(toPayment, relation.attendee);
				try {
					create(newItem);
					copied = true;
				} catch (SQLException e) {
					Logger.e(e);
				}					
			}
		}
		return copied;

	}
}
