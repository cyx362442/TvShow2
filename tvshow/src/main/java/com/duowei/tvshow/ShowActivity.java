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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.duowei.tvshow.service.BroadService;
import com.duowei.tvshow.view.TextSurfaceView;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.squareup.picasso.Picasso;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import java.io.File;
import java.util.ArrayList;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class ShowActivity extends AppCompatActivity {
    public static final int RESULT=100;
    private ImageView mImageView;
    private int[] mId;
    private File mFile;
    private VideoFragment mFragment;
    private int mLastTime=0;
    private TextSurfaceView mTsfv;
    private CallDialog mCallDialog;
    private Handler mHandler;
    private Runnable mRun;
    private ArrayList<String>listUrl=new ArrayList<>();
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private CallFragment mCallFragment;
    private BroadcastReceiver mHomeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        setViewWeight();

        EventBus.getDefault().register(this);
        mImageView = (ImageView) findViewById(R.id.image);
        mTsfv = (TextSurfaceView) findViewById(R.id.textView);

        initCallView();

        mId = new int[]{R.id.frame01,R.id.frame02,R.id.frame03,
                R.id.frame04,R.id.frame05,R.id.frame06,
                R.id.frame07,R.id.frame08,R.id.frame09,};
        Intent intent = getIntent();
        startShow(intent);
        //开启呼叫轮询
        startCall();
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(ShowActivity.this, mTtsInitListener);
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
        View image = findViewById(R.id.relative);
        LinearLayout.LayoutParams paramsWeight = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams paramsWeight2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        paramsWeight.weight = Integer.parseInt(viewWeight.substring(0,1));
        paramsWeight2.weight=Integer.parseInt(viewWeight.substring(2,3));
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

    private void startShow(Intent intent) {
        if(intent.getStringExtra("image_name")!=null&&intent.getStringExtra("video_name")==null){
            mFile=new File(FileDir.getVideoName()+intent.getStringExtra("image_name"));//拼接图片路径
            Picasso.with(this).load(mFile).fit().centerInside().into(mImageView);
        }else if(intent.getStringExtra("video_name")!=null&&intent.getStringExtra("image_name")!=null){
            mFile=new File(FileDir.getVideoName()+intent.getStringExtra("image_name"));//拼接图片路径
            Picasso.with(ShowActivity.this).load(mFile).fit().centerInside().into(mImageView);
            mFragment=new VideoFragment();
            int place = Integer.parseInt(intent.getStringExtra("video_palce"));//视频位置
            Bundle bundle = new Bundle();
            bundle.putString("videoname",intent.getStringExtra("video_name"));
            mFragment.setArguments(bundle);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(mId[place-1],mFragment);
            transaction.commit();
        }
        /**滚动文字内容*/
        if(!TextUtils.isEmpty(intent.getStringExtra("ad"))){
            mTsfv.setMove(true);
            mTsfv.setContent("    "+intent.getStringExtra("ad"));
        }else{
            mTsfv.setMove(false);
            mTsfv.setContent("");
        }
        /**滚动文字颜色*/
        if(!TextUtils.isEmpty(intent.getStringExtra("color"))){
            mTsfv.setFontColor(intent.getStringExtra("color"));
        }else{
            mTsfv.setFontColor("#ffffff");
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
            public void run() {
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
                            //播音
                            //设置合成语速
                            mTts.setParameter(SpeechConstant.SPEED, "30");
                            //设置合成音调
                            mTts.setParameter(SpeechConstant.PITCH, "50");
                            //设置合成音量
                            mTts.setParameter(SpeechConstant.VOLUME, "100");
                            int code = mTts.startSpeaking("请"+call.getTableno()+"号到前台取餐", null);
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
        mLastTime=0;
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
                mHandler.postDelayed(this,1000);
                Post6.instance().getCall();
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
