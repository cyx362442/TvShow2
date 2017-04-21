package com.duowei.tvshow.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    private  RelativeLayout mLayout;
    private AlertDialog mDialog;
    public synchronized void callShow(Context context,String msg){
        if(mDialog!=null){
            mDialog.dismiss();
        }
        mDialog = new AlertDialog.Builder(context).create();
        //必须先setView，否则在dialog\popuwindow中无法自动弹出软健盘
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = (RelativeLayout)inflater.inflate(R.layout.dialog_item, null);
        mDialog.show();
        Window window = mDialog.getWindow();
        window.setContentView(R.layout.dialog_item);
        //尺寸适应屏幕大小
        WindowManager.LayoutParams params = window.getAttributes();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        params.width = dm.widthPixels;
        params.height = dm.heightPixels;
        window.setAttributes(params);

        TextView tvMsg = (TextView) window.findViewById(R.id.tv_msg);
        tvMsg.setText(msg+"号");
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale_call);
        tvMsg.startAnimation(animation);
    }
    public void cancel(){
        mDialog.dismiss();
    }
}
