package mobi.threeam.npang.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.RootContext;
import com.googlecode.androidannotations.annotations.SystemService;

import java.util.Date;

import mobi.threeam.npang.database.DBHelper;
import mobi.threeam.npang.database.dao.PaymentGroupDao;
import mobi.threeam.npang.database.model.PaymentGroup;
import mobi.threeam.npang.receiver.AlarmReceiver_;

/**
 * Created by jangc on 2013. 12. 31..
 */

@EBean
public class AlarmUtils {

    @RootContext
    Context context;

    @SystemService
    AlarmManager alarmManager;

    @OrmLiteDao(helper = DBHelper.class, model = PaymentGroup.class)
    PaymentGroupDao paymentGroupDao;

    PendingIntent buildOperation(PaymentGroup group) {
        Intent intent = new Intent(context, AlarmReceiver_.class);
        if (group != null) {
            intent.putExtra("paymentGroup_id", group.id);
            intent.putExtra("paymentGroup_title", TextViewUtils.receiptTitle(group));
        }
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public void refresh() {
        PaymentGroup group = paymentGroupDao.getNextAlarm();
        alarmManager.cancel(buildOperation(group));
        if (group != null) {
            setAlarm(group);
        }
    }


    private void setAlarm(PaymentGroup group) {
        Date alarmTime = group.getNextAlarmTime();
        PendingIntent operation = buildOperation(group);

        alarmManager.set(AlarmManager.RTC, alarmTime.getTime(), operation);
    }
}
