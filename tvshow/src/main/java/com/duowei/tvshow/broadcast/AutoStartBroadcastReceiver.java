package com.duowei.tvshow.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;

import com.duowei.tvshow.contact.Consts;

/**
 * Created by Administrator on 2017-04-12.
 */

public class AutoStartBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    private SharedPreferences mPreferences = null;
    @Override
    public void onReceive(Context context, Intent intent) {
        mPreferences = context.getSharedPreferences("Users", Context.MODE_PRIVATE);
            if (intent.getAction().equals(ACTION)) {
                if (mPreferences.getBoolean(Consts.CHECKOUT_KEY, true)) {
                    // 启动应用，参数为需要自动启动的应用的包名，默认开机启动
                    Intent newIntent = context.getPackageManager()
                            .getLaunchIntentForPackage("com.duowei.tvshow");
                    context.startActivity(newIntent);
                }
            }
    }
}
