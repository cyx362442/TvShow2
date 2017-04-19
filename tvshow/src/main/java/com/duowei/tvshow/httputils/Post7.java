package com.duowei.tvshow.httputils;

import android.util.Log;

import com.android.volley.VolleyError;

/**
 * Created by Administrator on 2017-04-19.
 */

public class Post7 {
    private static Post7 post=null;
    private Post7(){}
    public static Post7 Instance(){
        if(post==null){
            post=new Post7();
        }
        return post;
    }
    public synchronized void updateCall(String sql){
        DownHTTP.postVolley7("http://192.168.1.78:2233/server/ServerSvlt?", sql, new VolleyResultListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
            @Override
            public void onResponse(String response) {
            }
        });
    }
}
