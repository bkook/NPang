package mobi.threeam.npang.database.dao;


import java.sql.SQLException;
import java.util.List;

import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class PaymentDao extends BaseDaoImpl<Payment, Long> {

	public PaymentDao(ConnectionSource connectionSource, Class<Payment> dataClass)
			throws SQLException {
		super(connectionSource, dataClass);
	}

	public PaymentDao(ConnectionSource connectionSource,
			DatabaseTableConfig<Payment> tableConfig) throws SQLException {
		super(connectionSource, tableConfig);
	}
	
	public Payment addPaymentTo(PaymentGroup group) throws SQLException {
		Payment payment = new Payment();
		payment.paymentGroup = group;
		
		create(payment);
		return payment;
	}
	
	public List<Payment> queryForGorup(PaymentGroup group) throws SQLException {
		QueryBuilder<Payment, Long> builder = queryBuilder();
		Where<Payment, Long> where = builder.where();
		where.eq("paymentGroup_id", group);
		builder.orderBy("createdAt", true);
		return builder.query();
	}

}
