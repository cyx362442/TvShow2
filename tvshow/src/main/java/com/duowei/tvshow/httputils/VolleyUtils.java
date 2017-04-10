package com.duowei.tvshow.httputils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by Administrator on 2016-12-16.
 */

public class VolleyUtils {
    private static final String TAG = "VolleyUtils";

    private static VolleyUtils mInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;

    private VolleyUtils(Context context) {
        mContext = context;

    }

    public static synchronized VolleyUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyUtils(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static void get(String url, Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(url, listener, errorListener);
        getInstance(mContext).addToRequestQueue(request);
    }

    public static void post(String url, final HashMap<String, String> map,
                            Response.Listener<String> listener, Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(Request.Method.POST, url, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        };
        getInstance(mContext).addToRequestQueue(request);
    }

    public void postQuerySql6(String url, final String sql, Response.Listener<String> listener,
                              Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(Request.Method.POST, url, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("State", "6");
                hashMap.put("Ssql", sql);
                return hashMap;
            }
        };
        getInstance(mContext).addToRequestQueue(request);
    }

    public void postQuerySql6(String url, final String sql, Response.Listener<String> listener) {
        postQuerySql6(url, sql, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "onErrorResponse: " + volleyError.getMessage());
            }
        });
    }

    public void postQuerySql7(String url, final String sql, Response.Listener<String> listener,
                              Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(Request.Method.POST, url, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("State", "7");
                hashMap.put("Ssql", sql);
                return hashMap;
            }
        };
        getInstance(mContext).addToRequestQueue(request);
    }

    public void postQuerySql7(String url, final String sql, Response.Listener<String> listener) {
        postQuerySql7(url, sql, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "onErrorResponse: " + volleyError.getMessage());
            }
        });
    }
}
