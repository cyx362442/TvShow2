package com.duowei.tvshow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.duowei.tvshow.bean.KDSCall;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.dialog.CallDialog;
import com.duowei.tvshow.event.BrushCall;
import com.duowei.tvshow.event.CallEvent;
import com.duowei.tvshow.httputils.Post6;
import com.duowei.tvshow.httputils.Post7;
import com.duowei.tvshow.sound.KeySound;
import com.duowei.tvshow.view.RecyclerBanner;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class ImageFullActivity extends AppCompatActivity {
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private CallDialog mCallDialog;
    private Handler mHandler;
    private Runnable mRun;
    private String mSoundStytle;
    private KeySound mSound;
    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(ImageFullActivity.this,"初始化失败，错误代码:"+code,Toast.LENGTH_LONG).show();
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_full);
        Intent intent = getIntent();
        if(intent==null){
            Toast.makeText(this,"找到不到图片",Toast.LENGTH_LONG).show();
            return;
        }
        ArrayList<String> imgPaths = intent.getStringArrayListExtra("selectPaths");
        RecyclerBanner rb = (RecyclerBanner) findViewById(R.id.recyclebanner);
        rb.setDatas(imgPaths);

        EventBus.getDefault().register(this);
        //开启呼叫轮询
        startCall();

        SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        // 初始化在线语音合成对象
        mSoundStytle = preferences.getString("soundstytle", getString(R.string.onLine));
        if(mSoundStytle.equals(getString(R.string.onLine))){
            mTts = SpeechSynthesizer.createSynthesizer(ImageFullActivity.this, mTtsInitListener);
        }else if(mSoundStytle.equals(getString(R.string.offLine))){
            // 初始化离线语音合成对象
            mSound = KeySound.getContext(this);
        }
        //初呼叫界面
        mCallDialog = CallDialog.getInstance();
    }
    /**呼叫轮询*/
    private synchronized void startCall() {
        //呼叫显示时间
        if(Consts.callTime<=0){
            return;
        }
        if(mHandler!=null){
            mHandler.removeCallbacks(mRun);
        }
        mHandler = new Handler();
        mHandler.postDelayed(mRun=new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(this,1000);
                Post6.instance().getCall();
            }
        },5000);
    }
    @Subscribe
    public void showCall(final CallEvent event){
        final KDSCall call = event.call;
        //停止轮询
        mHandler.removeCallbacks(mRun);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (call !=null){
                    //暂停视频
                    /**显示呼叫界面*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //显示
                            mCallDialog.callShow(ImageFullActivity.this, call.getTableno());
                            if(mSoundStytle.equals(getString(R.string.onLine))){
                                //设置合成语速
                                mTts.setParameter(SpeechConstant.SPEED, "30");
                                //设置合成音调
                                mTts.setParameter(SpeechConstant.PITCH, "50");
                                //设置合成音量
                                mTts.setParameter(SpeechConstant.VOLUME, "100");
                                mTts.startSpeaking("请"+call.getTableno()+"号到前台取餐。", null);
                            }else if(mSoundStytle.equals(getString(R.string.offLine))){
                                soundOffLine(call);
                            }
                        }
                    });
                    /**删除服务器上这条记录*/
                    String xh = call.getXh();
                    String sql="delete from KDSCall where xh='"+xh+"'|";
                    /**呼叫显示时长*/
                    try {
                        Thread.sleep(Consts.callTime*1000);
                        //继续轮询
                        mHandler.postDelayed(mRun,1000);
                        //播报完毕删除本条记录
                        Post7.Instance().updateCall(sql);
                        mCallDialog.cancel();
                        //继续视频播放
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private synchronized void soundOffLine(KDSCall call) {
        try {
            mSound.playSound('f',0);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i=0;i<call.getTableno().length();i++){
            char c = call.getTableno().charAt(i);
            try {
                Thread.sleep(400);
                mSound.playSound(c,0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(300);
            mSound.playSound('s',0);
            Thread.sleep(300);
            mSound.playSound('l',0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler!=null){
            mHandler.removeCallbacks(mRun);
        }
    }
}
