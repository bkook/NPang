package mobi.threeam.npang.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EReceiver;

import mobi.threeam.npang.common.AlarmUtils;
import mobi.threeam.npang.common.Logger;

/**
 * Created by jangc on 2014. 1. 1..
 */
@EReceiver
public class RestartReceiver extends BroadcastReceiver {
    @Bean
    AlarmUtils alarmUtils;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            Logger.w("replace boot complete ");
            alarmUtils.refresh();
            return;
        }
        if ("android.intent.action.PACKAGE_REPLACED".equals(action) && intent.getData().getSchemeSpecificPart().equals(context.getPackageName())) {
            Logger.w("replace " + context.getPackageName());
            alarmUtils.refresh();
            return;
        }
    }
}
