package mobi.threeam.npang.database.dao;


import android.content.Context;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.common.NPangPreference_;
import mobi.threeam.npang.database.model.PaymentGroup;
import mobi.threeam.npang.event.CreatePaymentGroupEvent;

public class PaymentGroupDao extends BaseDaoImpl<PaymentGroup, Long> {

	public PaymentGroupDao(ConnectionSource connectionSource, Class<PaymentGroup> dataClass)
			throws SQLException {
		super(connectionSource, dataClass);
	}

	public PaymentGroupDao(ConnectionSource connectionSource,
			DatabaseTableConfig<PaymentGroup> tableConfig) throws SQLException {
		super(connectionSource, tableConfig);
	}
	
	public PaymentGroup createDefault(Context context) throws SQLException {
		NPangPreference_ prefs = new NPangPreference_(context);

		PaymentGroup group = new PaymentGroup();
		group.bankName = prefs.bankName().get();
		group.bankAccount = prefs.accountNumber().get();
		group.completed = false;
		group.createdAt = new Date();
		group.locale = Locale.getDefault().toString();
		group.totalAmount = 0;
        group.alarmEnabled = true;
        group.alarmTime = new Date();
        group.alarmPeriod = 3;
		group.state = PaymentGroup.STATE_NONE;
		
		create(group);
		
		EventBus.getDefault().post(new CreatePaymentGroupEvent(group));
		return group;
		
	}

	public List<PaymentGroup> queryForAllSorted() {
        QueryBuilder<PaymentGroup, Long> builder = null;
        try {
            builder = queryBuilder();
            builder.orderBy("completed", true);
            builder.orderBy("createdAt", false);
            return builder.query();
        } catch (SQLException e) {
            Logger.e(e);
        }
        return null;
	}

    public void setPaymentGroupState(PaymentGroup group, int state) {
        group.state = state;

        try {
            update(group);
        } catch (SQLException e) {
            Logger.e(e);
        }
    }

    public PaymentGroup getNextAlarm() {
        List<PaymentGroup> paymentGroupList = null;
        QueryBuilder<PaymentGroup, Long> builder = queryBuilder();
        Where<PaymentGroup, Long> where = builder.where();
        try {
            where.eq("completed", false).and().eq("alarmEnabled", true).and().eq("state", PaymentGroup.STATE_CALCULATED);
            paymentGroupList = builder.query();
        } catch (SQLException e) {
            Logger.e(e);
        }

        if (paymentGroupList == null || paymentGroupList.size() == 0) {
            return null;
        }

        PaymentGroup nextPaymentGroup = null;
        Date nextAlarmTime = null;
        for (PaymentGroup group : paymentGroupList) {
            Date alarmTime = group.getNextAlarmTime();
            if (nextAlarmTime == null) {
                nextAlarmTime = alarmTime;
                nextPaymentGroup = group;
                continue;
            }
            if (alarmTime.before(nextAlarmTime)) {
                nextAlarmTime = alarmTime;
                nextPaymentGroup = group;
                continue;
            }
        }
        return nextPaymentGroup;
    }

}
