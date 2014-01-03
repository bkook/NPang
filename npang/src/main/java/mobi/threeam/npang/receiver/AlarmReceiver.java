package mobi.threeam.npang.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EReceiver;
import com.googlecode.androidannotations.annotations.SystemService;

import mobi.threeam.npang.R;
import mobi.threeam.npang.common.AlarmUtils;
import mobi.threeam.npang.ui.activity.MainActivity_;

/**
 * Created by jangc on 2013. 12. 31..
 */

@EReceiver
public class AlarmReceiver extends BroadcastReceiver {
    @SystemService
    NotificationManager notificationManager;

    @Bean
    AlarmUtils alarmUtils;

//    @OrmLiteDao(helper = DBHelper.class, model = PaymentGroup.class)
//    PaymentGroupDao paymentGroupDao;

    @Override
    public void onReceive(Context context, Intent intent) {
        long paymentGroupId = intent.getLongExtra("paymentGroup_id", -1);
        String paymentGroupTitle = intent.getStringExtra("paymentGroup_title");
        if (paymentGroupId == -1) {
            return;
        }

        Intent activityIntent = MainActivity_.intent(context).paymentGroupId(paymentGroupId).flags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP).get();
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);

//        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(String.format("[%s] %s", context.getString(R.string.app_name), paymentGroupTitle))
                .setContentText(context.getString(R.string.noti_desc))
                .setContentIntent(pIntent)
                .setOngoing(false)
                .setAutoCancel(true);
        notificationManager.notify(0, builder.build());

        alarmUtils.refresh();
    }
}
