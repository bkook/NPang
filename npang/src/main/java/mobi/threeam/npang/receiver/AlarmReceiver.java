package mobi.threeam.npang.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EReceiver;
import com.googlecode.androidannotations.annotations.SystemService;

import java.util.Date;

import mobi.threeam.npang.R;
import mobi.threeam.npang.common.AlarmUtils;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.common.TimeUtils;

/**
 * Created by jangc on 2013. 12. 31..
 */

@EReceiver
public class AlarmReceiver extends BroadcastReceiver {
    @SystemService
    NotificationManager notificationManager;

    @Bean
    AlarmUtils alarmUtils;

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.w("AlarmReceiver onReceive " + TimeUtils.format(new Date()));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("test")
                .setLights(Color.RED, 500, 0)
                .setAutoCancel(true);
        notificationManager.notify(0, builder.build());

        alarmUtils.refresh();
    }
}
