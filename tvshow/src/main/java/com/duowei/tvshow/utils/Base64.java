package com.duowei.tvshow.utils;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2016-11-05.
 */

public class Base64 {
    public static String getBase64(String str){
        byte[] b = null;
        String s = null;
        try{
            b = str.getBytes("utf-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        if(b != null){
            s= android.util.Base64.encodeToString(str.getBytes(), android.util.Base64.DEFAULT);
        }
        return s;
    }
}
