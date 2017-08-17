package com.duowei.tvshow;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.duowei.tvshow.bean.KDSCall;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.contact.FileDir;
import com.duowei.tvshow.dialog.CallDialog;
import com.duowei.tvshow.event.BrushCall;
import com.duowei.tvshow.event.CallEvent;
import com.duowei.tvshow.event.FinishEvent;
import com.duowei.tvshow.event.FinishMain;
import com.duowei.tvshow.fragment.CallFragment;
import com.duowei.tvshow.fragment.VideoFragment;
import com.duowei.tvshow.httputils.Post6;
import com.duowei.tvshow.httputils.Post7;
import com.duowei.tvshow.sound.KeySound;
import com.duowei.tvshow.view.TextSurfaceView;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.squareup.picasso.Picasso;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import java.io.File;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class ShowActivity extends AppCompatActivity {
    public static final int RESULT=100;
    private ImageView mImageView;
    private int[] mId;
    private File mFile;
    private VideoFragment mFragment;
    private CallDialog mCallDialog;
    private Handler mHandler;
    private Runnable mRun;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private CallFragment mCallFragment;
    private BroadcastReceiver mHomeReceiver;
    private String mSoundStytle;
    private KeySound mSound;
    private String mViewWeight;
    private Intent mIntent;

    private boolean isFinish=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,WindowManager.LayoutParams. FLAG_FULLSCREEN);
        //禁止修眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_show);

        EventBus.getDefault().register(this);
        mIntent = getIntent();
        mImageView = (ImageView) findViewById(R.id.image);
        View frame = findViewById(R.id.frame_call);
        TextSurfaceView tsv1 = (TextSurfaceView) findViewById(R.id.textView);
        TextSurfaceView tsv2 = (TextSurfaceView) findViewById(R.id.textView2);

        SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        String showStytle = preferences.getString("callvalue", "关闭");
        mId = new int[]{R.id.frame01,R.id.frame02,R.id.frame03,
                R.id.frame04,R.id.frame05,R.id.frame06,
                R.id.frame07,R.id.frame08,R.id.frame09,};
        if(showStytle.equals("关闭")){//纯广告播放式
            frame.setVisibility(View.GONE);
            tsv1.setVisibility(View.GONE);
            tsv2.setVisibility(View.VISIBLE);
            startShow(tsv2);
        }else{//显示呼叫取餐
            frame.setVisibility(View.VISIBLE);
            tsv1.setVisibility(View.VISIBLE);
            tsv2.setVisibility(View.GONE);
            mSoundStytle = preferences.getString("soundstytle", getString(R.string.onLine));
            mViewWeight = preferences.getString("view_weight", "1:2");
            setViewWeight();//设置呼叫显示占比

            initCallView();//初始化叫呼叫屏幕
            //开启呼叫轮询
            startCall();
            // 初始化在线语音合成对象
            if(mSoundStytle.equals(getString(R.string.onLine))){
                mTts = SpeechSynthesizer.createSynthesizer(ShowActivity.this, mTtsInitListener);
            }else if(mSoundStytle.equals(getString(R.string.offLine))){
                // 初始化离线语音合成对象
                mSound = KeySound.getContext(this);
            }
            mCallDialog = CallDialog.getInstance();
            startShow(tsv1);
        }

        mHomeReceiver = new HomeReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeReceiver,filter);

    }
    /**设置屏占比*/
    private void setViewWeight() {
        View callView = findViewById(R.id.frame_call);
        View image = findViewById(R.id.relative);
        LinearLayout.LayoutParams paramsWeight = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams paramsWeight2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        paramsWeight.weight = Integer.parseInt(mViewWeight.substring(0,1));
        paramsWeight2.weight=Integer.parseInt(mViewWeight.substring(2,3));
        callView.setLayoutParams(paramsWeight2);
        image.setLayoutParams(paramsWeight);
    }

    private void initCallView() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mCallFragment = new CallFragment();
        ft.replace(R.id.frame_call, mCallFragment);
        ft.commit();
    }

    private void startShow(TextSurfaceView tsfv) {
        if(mIntent.getStringExtra("image_name")!=null&&mIntent.getStringExtra("video_name")==null){
            mFile=new File(FileDir.getVideoName()+mIntent.getStringExtra("image_name"));//拼接图片路径
            Picasso.with(this).load(mFile).fit().centerInside().into(mImageView);
        }else if(mIntent.getStringExtra("video_name")!=null&&mIntent.getStringExtra("image_name")!=null){
            mFile=new File(FileDir.getVideoName()+mIntent.getStringExtra("image_name"));//拼接图片路径
            Picasso.with(ShowActivity.this).load(mFile).fit().centerInside().into(mImageView);
            mFragment=new VideoFragment();
            int place = Integer.parseInt(mIntent.getStringExtra("video_palce"));//视频位置
            Bundle bundle = new Bundle();
            bundle.putString("videoname",mIntent.getStringExtra("video_name"));
            mFragment.setArguments(bundle);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(mId[place-1],mFragment);
            transaction.commit();
        }
        /**滚动文字内容*/
        if(!TextUtils.isEmpty(mIntent.getStringExtra("ad"))){
            tsfv.setMove(true);
            tsfv.setContent("    "+mIntent.getStringExtra("ad"));
        }else{
            tsfv.setMove(false);
            tsfv.setContent("");
        }
        /**滚动文字颜色*/
        if(!TextUtils.isEmpty(mIntent.getStringExtra("color"))){
            tsfv.setFontColor(mIntent.getStringExtra("color"));
        }else{
            tsfv.setFontColor("#ffffff");
        }
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(ShowActivity.this,"初始化失败，错误代码:"+code,Toast.LENGTH_LONG).show();
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    @Subscribe
    public void showCall(final CallEvent event){
        final KDSCall call = event.call;
        //停止轮询
        mHandler.removeCallbacks(mRun);
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                if (call !=null){
                    //暂停视频
                    if(mFragment!=null){
                        mFragment.stopPlay();
                    }
                    /**显示呼叫界面*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //显示
                            mCallDialog.callShow(ShowActivity.this, call.getTableno());
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
                    /**更新服务器上这条记录*/
                    String xh = call.getXh();
                    String sql="update KDSCall set YHJ='2' where xh='"+xh+"'|";
                    /**呼叫显示时长*/
                    try {
                        Thread.sleep(Consts.callTime*1000);
                        //继续轮询
                        mHandler.postDelayed(mRun,1000);
                        Post7.Instance().updateCall(sql);
                        mCallDialog.cancel();
                        //继续视频播放
                        if(mFragment!=null){
                            mFragment.continuePlay();
                        }
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

    @Subscribe
    public void FinishActivity(FinishEvent event){
        finish();
    }
    @Subscribe
    public void BrushData(BrushCall event){
        mCallFragment.setListWait(event);
        isFinish=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onStop() {
        JCVideoPlayer.releaseAllVideos();
        if(mHandler!=null){
            mHandler.removeCallbacks(mRun);
        }
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHomeReceiver);
    }

    /**呼叫轮询*/
    private void startCall() {
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
                mHandler.postDelayed(this,2000);
                if(isFinish){
                    Post6.instance().getCall();
                    isFinish=false;
                }
            }
        },5000);
    }

    class HomeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                EventBus.getDefault().post(new FinishMain());
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT);
        finish();
    }
}
