package com.duowei.tvshow.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duowei.tvshow.R;

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
    public void callShow(Context context,String msg){
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
        TextView tvMsg = (TextView) mLayout.findViewById(R.id.tv_msg);
        tvMsg.setText(msg);
    }
    public void cancel(){
        mDialog.dismiss();
    }
}
