package com.duowei.tvshow.httputils;

import android.util.Log;

import com.android.volley.VolleyError;
import com.duowei.tvshow.bean.KDSCall;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.event.CallEvent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2017-04-19.
 */

public class Post6 {
    private Post6(){}
    private static Post6 post=null;
    public static Post6 instance(){
        if(post==null){
            post=new Post6();
        }
        return post;
    }
    String sql="select top 1 xh,tableno from KDSCall WHERE DATEADD(mi,-2,GETDATE()) < XDSJ|";
    List<KDSCall>listCall;
    public synchronized void getCall(){
        listCall=new ArrayList<>();
        DownHTTP.postVolley6(Consts.ip, sql, new VolleyResultListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
            @Override
            public void onResponse(String response) {
               if(response.equals("]")){
                   return;
               }
                Gson gson = new Gson();
                KDSCall[] calls = gson.fromJson(response, KDSCall[].class);
                EventBus.getDefault().post(new CallEvent(calls[0]));
            }
        });
    }
}
