package com.duowei.tvshow;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.duowei.tvshow.bean.KDSCall;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.dialog.CallDialog;
import com.duowei.tvshow.event.BrushCall;
import com.duowei.tvshow.event.CallEvent;
import com.duowei.tvshow.event.FinishEvent;
import com.duowei.tvshow.event.FinishMain;
import com.duowei.tvshow.fragment.CallFragment;
import com.duowei.tvshow.httputils.Post6;
import com.duowei.tvshow.httputils.Post7;
import com.duowei.tvshow.sound.KeySound;
import com.duowei.tvshow.view.TextSurfaceView;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class VideoFullActivity extends AppCompatActivity{
    private JCVideoPlayer mJcVideoPlayer;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private CallDialog mCallDialog;
    private Handler mHandler;
    private Runnable mRun;

    private CallFragment mCallFragment;
    private BroadcastReceiver mHomeReceiver;

    private String mSoundStytle;
    private KeySound mSound;
    private String mViewWeight;
    private String mShowStytle;
    private ArrayList<String> mVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,WindowManager.LayoutParams. FLAG_FULLSCREEN);
        //禁止修眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_full);
        Intent intent = getIntent();
        EventBus.getDefault().register(this);

        mJcVideoPlayer = (JCVideoPlayer) findViewById(R.id.jcvideoplayer);
        TextSurfaceView tsf1 = (TextSurfaceView) findViewById(R.id.textsurfaceview);
        TextSurfaceView tsf2 = (TextSurfaceView) findViewById(R.id.textsurfaceview2);
        View frame = findViewById(R.id.frame_call);
        SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        mShowStytle = preferences.getString("callvalue", "关闭");
        if(mShowStytle.equals("关闭")){//纯广告播放模式
            tsf2.setVisibility(View.VISIBLE);
            tsf1.setVisibility(View.GONE);
            frame.setVisibility(View.GONE);
            showText(intent,tsf2);
        }else{//开启呼叫模式
            tsf2.setVisibility(View.GONE);
            tsf1.setVisibility(View.VISIBLE);
            frame.setVisibility(View.VISIBLE);
            showText(intent,tsf1);//广告词

            initFragment();

            mSoundStytle = preferences.getString("soundstytle", getString(R.string.onLine));
            mViewWeight = preferences.getString("view_weight", "1:2");
            setViewWeight();//设置呼叫占比
            // 初始化合成对象
            if(mSoundStytle.equals(getString(R.string.onLine))){
                mTts = SpeechSynthesizer.createSynthesizer(VideoFullActivity.this, null);
            }else if(mSoundStytle.equals(getString(R.string.offLine))){
                // 初始化离线语音合成对象
                mSound = KeySound.getContext(this);
            }
            //初呼叫界面
            mCallDialog = CallDialog.getInstance();
            //开启呼叫轮询
            startCall();
        }
        //视频文件件路径
        mVideoPath = intent.getStringArrayListExtra("selectPaths");

        mHomeReceiver = new HomeReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeReceiver,filter);
    }

    private void initFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mCallFragment = new CallFragment();
        ft.replace(R.id.frame_call, mCallFragment);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mVideoPath ==null|| mVideoPath.size()<=0){
            Toast.makeText(this,"找到不到视频",Toast.LENGTH_LONG).show();
        }else{
            mJcVideoPlayer.setUp(mVideoPath.get(0),"","");
        }
    }

    /**设置屏占比*/
    private void setViewWeight() {
        View callView = findViewById(R.id.frame_call);
        View image = findViewById(R.id.linearLayout);
        LinearLayout.LayoutParams paramsWeight = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams paramsWeight2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        paramsWeight.weight = Integer.parseInt(mViewWeight.substring(0,1));
        paramsWeight2.weight=Integer.parseInt(mViewWeight.substring(2,3));
        callView.setLayoutParams(paramsWeight2);
        image.setLayoutParams(paramsWeight);
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
                    mJcVideoPlayer.stopPlay();
                    /**显示呼叫界面*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //显示
                            mCallDialog.callShow(VideoFullActivity.this, call.getTableno());
                            /**播音*/
                            if(mSoundStytle.equals(getString(R.string.onLine))){
                                //设置合成语速
                                mTts.setParameter(SpeechConstant.SPEED, "20");
                                //设置合成音调
                                mTts.setParameter(SpeechConstant.PITCH, "50");
                                //设置合成音量
                                mTts.setParameter(SpeechConstant.VOLUME, "100");
                                mTts.startSpeaking(call.getTableno(), null);
                            }else if(mSoundStytle.equals(getString(R.string.onLine))){
                                soundOffLine(call);
                            }
                        }
                    });
                    /**删除服务器上这条记录*/
                    String xh = call.getXh();
                    String sql="update KDSCall set YHJ='2' where xh='"+xh+"'|";
                    /**呼叫显示时长*/
                    try {
                        Thread.sleep(Consts.callTime*1000);
                        //继续轮询
                        mHandler.postDelayed(mRun,1000);
                        //播报完毕删除本条记录
                        Post7.Instance().updateCall(sql);
                        mCallDialog.cancel();
                        //继续视频播放
                        mJcVideoPlayer.continuePlay();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void soundOffLine(KDSCall call) {
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

    @Subscribe
    public void FinishActivity(FinishEvent event){
        finish();
    }
    @Subscribe
    public void BrushData(BrushCall event){
        mCallFragment.setListWait(event);
    }


    private void showText(Intent intent,TextSurfaceView tsf) {
        /**滚动文字内容*/
        if(!TextUtils.isEmpty(intent.getStringExtra("ad"))){
            tsf.setMove(true);
            tsf.setContent("    "+intent.getStringExtra("ad"));
        }else{
            tsf.setMove(false);
            tsf.setContent("");
        }
        /**滚动文字颜色*/
        if(!TextUtils.isEmpty(intent.getStringExtra("color"))){
            tsf.setFontColor(intent.getStringExtra("color"));
        }else{
            tsf.setFontColor("#ffffff");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        JCVideoPlayer.releaseAllVideos();
        EventBus.getDefault().unregister(this);
        if(mHandler!=null){
            mHandler.removeCallbacks(mRun);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHomeReceiver);
    }

    class HomeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                EventBus.getDefault().post(new FinishMain());
                finish();
            }
        }
    }
}
