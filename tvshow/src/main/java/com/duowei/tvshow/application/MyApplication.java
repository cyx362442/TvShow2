package com.duowei.tvshow.application;
import com.duowei.tvshow.httputils.MyVolley;
import com.squareup.leakcanary.LeakCanary;

import org.litepal.LitePalApplication;

/**
 * Created by Administrator on 2017-01-19.
 */

public class MyApplication extends LitePalApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        MyVolley.init(this);
        //检测内存泄露
//        LeakCanary.install(this);
    }
}
