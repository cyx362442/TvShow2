package com.duowei.tvshow.httputils;

import com.android.volley.VolleyError;
import com.duowei.tvshow.bean.KDSCall;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.event.BrushCall;
import com.duowei.tvshow.event.CallEvent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

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
    String sql="select xh,tableno,isnull(yhj,'0')yhj from kdscall order by xdsj desc|";
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
                   KDSCall[] calls=new KDSCall[0];
                   EventBus.getDefault().post(new BrushCall(calls));
                   return;
               }
                Gson gson = new Gson();
                KDSCall[] calls = gson.fromJson(response, KDSCall[].class);
                for(int i=0;i<calls.length;i++){
                    if(calls[i].getYhj().equals("1")){
                        EventBus.getDefault().post(new CallEvent(calls[i]));
                        break;
                    }
                }
                EventBus.getDefault().post(new BrushCall(calls));
            }
        });
    }
}
