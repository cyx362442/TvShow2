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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


import com.duowei.tvshow.adapter.CallListAdapter;
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
import com.duowei.tvshow.view.TextSurfaceView;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class VideoFullActivity extends AppCompatActivity{
    private ArrayList<String> videoPath;
    private JCVideoPlayer mJcVideoPlayer;
    private TextSurfaceView mTsfv;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private CallDialog mCallDialog;
    private Handler mHandler;
    private Runnable mRun;
    private Intent mIntent;

    private CallFragment mCallFragment;
    private BroadcastReceiver mHomeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_full);
        setViewWeight();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mCallFragment = new CallFragment();
        ft.replace(R.id.frame_call, mCallFragment);
        ft.commit();

        mIntent = getIntent();
        if(mIntent ==null){
            Toast.makeText(this,"找到不到视频",Toast.LENGTH_LONG).show();
            return;
        }
        videoPath = mIntent.getStringArrayListExtra("selectPaths");
        EventBus.getDefault().register(this);
        //开启呼叫轮询
        startCall();
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(VideoFullActivity.this, null);
        //初呼叫界面
        mCallDialog = CallDialog.getInstance();

        mHomeReceiver = new HomeReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeReceiver,filter);
    }

    /**设置屏占比*/
    private void setViewWeight() {
        SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        String viewWeight = preferences.getString("view_weight", "1:2");
        View callView = findViewById(R.id.frame_call);
        View image = findViewById(R.id.jcvideoplayer);
        LinearLayout.LayoutParams paramsWeight = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams paramsWeight2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        paramsWeight.weight = Integer.parseInt(viewWeight.substring(0,1));
        paramsWeight2.weight=Integer.parseInt(viewWeight.substring(2,3));
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
                            //设置合成语速
                            mTts.setParameter(SpeechConstant.SPEED, "20");
                            //设置合成音调
                            mTts.setParameter(SpeechConstant.PITCH, "50");
                            //设置合成音量
                            mTts.setParameter(SpeechConstant.VOLUME, "100");
                            int code = mTts.startSpeaking("请"+call.getTableno()+"号到柜台取餐", null);
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

    @Subscribe
    public void FinishActivity(FinishEvent event){
        finish();
    }
    @Subscribe
    public void BrushData(BrushCall event){
        mCallFragment.setListWait(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mJcVideoPlayer = (JCVideoPlayer) findViewById(R.id.jcvideoplayer);
        mTsfv = (TextSurfaceView) findViewById(R.id.textsurfaceview);
        mJcVideoPlayer.setUp(videoPath.get(0),"","");
//        mJcVideoPlayer.setUpForFullscreen(videoPath.get(0),"","");
        /**滚动文字内容*/
        if(!TextUtils.isEmpty(mIntent.getStringExtra("ad"))){
            mTsfv.setMove(true);
            mTsfv.setContent("    "+mIntent.getStringExtra("ad"));
        }else{
            mTsfv.setMove(false);
            mTsfv.setContent("");
        }
        /**滚动文字颜色*/
        if(!TextUtils.isEmpty(mIntent.getStringExtra("color"))){
            mTsfv.setFontColor(mIntent.getStringExtra("color"));
        }else{
            mTsfv.setFontColor("#ffffff");
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
