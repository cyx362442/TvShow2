package com.duowei.tvshow.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.duowei.tvshow.contact.ConstsCode;

import org.litepal.util.Const;

public class BroadService extends Service {
    /**
     * 心跳间隔一分钟
     */
    private static final long HEARTBEAT_INTERVAL = 10 * 1000L;

    private AlarmManager mAlarmManager;

    private PendingIntent mPendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mPendingIntent = PendingIntent.getBroadcast(this, 10000, new Intent(
                ConstsCode.ACTION_START_HEART), PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 发送启动推送任务的广播
//        Intent startIntent = new Intent(Const.ACTION_START_HEART);
//        sendBroadcast(startIntent);
        // 启动心跳定时器
        long triggerAtTime = SystemClock.elapsedRealtime() + HEARTBEAT_INTERVAL;
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                triggerAtTime, HEARTBEAT_INTERVAL, mPendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Intent startIntent = new Intent(ConstsCode.ACTION_STOP_HEART);
        sendBroadcast(startIntent);
        //取消心跳定时器
        mAlarmManager.cancel(mPendingIntent);
        Log.e("====","停止心跳");
        super.onDestroy();
    }
}
