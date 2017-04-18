package com.duowei.tvshow.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.android.volley.VolleyError;
import com.duowei.tvshow.R;
import com.duowei.tvshow.httputils.DownHTTP;
import com.duowei.tvshow.httputils.VolleyResultListener;

/**
 * Created by Administrator on 2017-04-18.
 */

public class CallDialog {
    private CallDialog() {
    }
    private static CallDialog dialog=null;
    public static CallDialog getInstance(){
        if(dialog==null){
            dialog=new CallDialog();
        }
        return dialog;
    }
    private  LinearLayout mLayout;
    private AlertDialog mDialog;
    public void callShow(Context context){
        if(mDialog!=null){
            mDialog.dismiss();
        }

        mDialog = new AlertDialog.Builder(context).create();
        //必须先setView，否则在dialog\popuwindow中无法自动弹出软健盘
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = (LinearLayout)inflater.inflate(R.layout.dialog_item, null);
        mDialog.setView(mLayout);
        mDialog.show();

        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        params.width = dm.widthPixels;
        params.height = dm.heightPixels ;
        mDialog.getWindow().setAttributes(params);
    }
    public void cancel(){
        mDialog.dismiss();
    }
}
