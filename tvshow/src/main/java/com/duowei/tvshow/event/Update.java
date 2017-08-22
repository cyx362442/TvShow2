package com.duowei.tvshow.event;

/**
 * Created by Administrator on 2017-08-22.
 */

public class Update {
    public String versionCode;
    public String message;
    public String url;
    public String name;

    public Update(String versionCode, String message,String url, String name) {
        this.versionCode = versionCode;
        this.message=message;
        this.url = url;
        this.name = name;
    }
}
