package com.duowei.tvshow;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


import com.duowei.tvshow.bean.KDSCall;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.dialog.CallDialog;
import com.duowei.tvshow.event.CallEvent;
import com.duowei.tvshow.httputils.Post6;
import com.duowei.tvshow.httputils.Post7;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class VideoFullActivity extends AppCompatActivity{
    private ArrayList<String> videoPath;
    private JCVideoPlayer mJcVideoPlayer;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private CallDialog mCallDialog;
    private Handler mHandler;
    private Runnable mRun;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_full);
        Intent intent = getIntent();
        if(intent==null){
            Toast.makeText(this,"找到不到视频",Toast.LENGTH_LONG).show();
            return;
        }
        videoPath = intent.getStringArrayListExtra("selectPaths");

        EventBus.getDefault().register(this);
        //开启呼叫轮询
        startCall();
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(VideoFullActivity.this, null);
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
    public void onEvent(final CallEvent event){
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
                        mJcVideoPlayer.continuePlay();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mJcVideoPlayer = (JCVideoPlayer) findViewById(R.id.jcvideoplayer);
        //从第一部开始播放
//        mJcVideoPlayer.setUp(videoPath.get(0), MyJVCPlayer.SCREEN_LAYOUT_NORMAL, "");
//        mJcVideoPlayer.setUp(videoPath.get(0),videoPath.get(0),"视频",true);
        mJcVideoPlayer.setUp(videoPath.get(0),"","");
    }

    @Override
    protected void onStop() {
        super.onStop();
        JCVideoPlayer.releaseAllVideos();
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
