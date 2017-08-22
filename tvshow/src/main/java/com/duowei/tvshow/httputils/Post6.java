package com.duowei.tvshow.httputils;

import android.util.Log;

import com.android.volley.VolleyError;
import com.duowei.tvshow.bean.KDSCall;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.event.BrushCall;
import com.duowei.tvshow.event.CallEvent;
import com.duowei.tvshow.event.Update;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public synchronized void getCall(){
        DownHTTP.postVolley6(Consts.ip, sql, new VolleyResultListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                KDSCall[] calls=new KDSCall[0];
                EventBus.getDefault().post(new BrushCall(calls));
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
    public synchronized void getVersion(){
        String url="http://ouwtfo4eg.bkt.clouddn.com/tvshow.txt";
        DownHTTP.getVolley(url, new VolleyResultListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String versionCode = jsonObject.getString("versionCode");
                    String msg = jsonObject.getString("msg");
                    String url = jsonObject.getString("url");
                    String name = jsonObject.getString("name");
                    EventBus.getDefault().post(new Update(versionCode,msg,url,name));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
