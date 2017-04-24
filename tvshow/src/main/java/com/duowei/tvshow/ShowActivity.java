package com.duowei.tvshow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.duowei.tvshow.bean.KDSCall;
import com.duowei.tvshow.bean.OneDataBean;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.contact.ConstsCode;
import com.duowei.tvshow.contact.FileDir;
import com.duowei.tvshow.dialog.CallDialog;
import com.duowei.tvshow.event.CallEvent;
import com.duowei.tvshow.event.FinishEvent;
import com.duowei.tvshow.fragment.VideoFragment;
import com.duowei.tvshow.httputils.Post6;
import com.duowei.tvshow.httputils.Post7;
import com.duowei.tvshow.utils.CurrentTime;
import com.duowei.tvshow.view.TextSurfaceView;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.FullScreenActivity;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class ShowActivity extends AppCompatActivity {
//    private ServiceBroadCast mBroadCast;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        EventBus.getDefault().register(this);
        mImageView = (ImageView) findViewById(R.id.image);
        mTsfv = (TextSurfaceView) findViewById(R.id.textView);
        mId = new int[]{R.id.frame01,R.id.frame02,R.id.frame03,
                R.id.frame04,R.id.frame05,R.id.frame06,
                R.id.frame07,R.id.frame08,R.id.frame09,};
        Intent intent = getIntent();
        if(intent.getStringExtra("image_name")!=null&&intent.getStringExtra("video_name")==null){
            mFile=new File(FileDir.getVideoName()+intent.getStringExtra("image_name"));//拼接图片路径
            Picasso.with(this).load(mFile).fit().centerInside().into(mImageView);
        }else if(intent.getStringExtra("video_name")!=null&&intent.getStringExtra("image_name")!=null){
            mFile=new File(FileDir.getVideoName()+intent.getStringExtra("image_name"));//拼接图片路径
            JCVideoPlayer.releaseAllVideos();
            removeFragment();//删除上次视频
            Picasso.with(ShowActivity.this).load(mFile).fit().centerInside().into(mImageView);
            mFragment=new VideoFragment();
            int place = Integer.parseInt(intent.getStringExtra("video_palce"));//视频位置
            Bundle bundle = new Bundle();
            bundle.putString("videoname",intent.getStringExtra("video_name"));
            mFragment.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
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

        //开启时间段轮询
//        startShow();
        //开启呼叫轮询
        startCall();
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(ShowActivity.this, mTtsInitListener);
        mCallDialog = CallDialog.getInstance();
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
    public void onEvent(final CallEvent event){
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
                    /**删除服务器上这条记录*/
                    String xh = call.getXh();
                    String sql="delete from KDSCall where xh='"+xh+"'|";
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

    public void onEventMainThread(FinishEvent event){
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLastTime=0;
        //节目伦询
        IntentFilter intentFilter = new IntentFilter(ConstsCode.ACTION_START_HEART);
        //呼叫伦询
//        IntentFilter intentFilter2 = new IntentFilter(ConstsCode.ACTION_START_CALL);
//        mBroadCast = new ServiceBroadCast();
//        registerReceiver(mBroadCast,intentFilter);
//        registerReceiver(mBroadCast,intentFilter2);
    }

    @Override
    protected void onStop() {
//        JCVideoPlayer.releaseAllVideos();
        EventBus.getDefault().unregister(this);
        if(mHandler!=null){
            mHandler.removeCallbacks(mRun);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mBroadCast);
    }

//    /**启用广播 */
//    public class ServiceBroadCast extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            /**节目更新*/
//            if(mLastTime>CurrentTime.getTime()){//上次时间段还未结束,返回，继续之前播放
//                return;
//              }
//            if(action.equals(ConstsCode.ACTION_START_HEART)){
//                startShow();
//            }
//        }
//    }

    /**时间段节目轮询*/
    private void startShow() {
        List<OneDataBean> list = DataSupport.findAll(OneDataBean.class);
        for(OneDataBean bean:list){
            String time = bean.time.trim();
            boolean newTime = isNewTime(time);
            if(newTime==true){//发现新的时间段
                EventBus.getDefault().post(new FinishEvent());
                    mFile=new File(FileDir.getVideoName()+bean.image_name);//拼接图片路径
                    /**纯图片播放模式*/
                    if(!bean.image_name.equals("null")&&bean.video_name.equals("null")){
                        Picasso.with(ShowActivity.this).load(mFile).fit().centerInside().into(mImageView);
                    }
                    /**纯视频播放模式*/
                    else if(!bean.video_name.equals("null")&&bean.image_name.equals("null")){
                        listUrl.clear();
                        listUrl.add(FileDir.getVideoName()+bean.video_name);
                        Intent intent = new Intent(this, VideoFullActivity.class);
                        intent.putStringArrayListExtra("selectPaths",listUrl);
                        startActivity(intent);
                    }
                    /**图片、视频混合模式*/
                    else if(!bean.video_name.equals("null")&&!bean.image_name.equals("null")){
                        JCVideoPlayer.releaseAllVideos();
                        removeFragment();//删除上次视频
                        Picasso.with(ShowActivity.this).load(mFile).fit().centerInside().into(mImageView);
                        mFragment=new VideoFragment();
                        int place = Integer.parseInt(bean.video_palce);//视频位置
                        Bundle bundle = new Bundle();
                        bundle.putString("videoname",bean.video_name);
                        mFragment.setArguments(bundle);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(mId[place-1],mFragment);
                        transaction.commit();
                    }

                    /**滚动文字内容*/
                    if(!TextUtils.isEmpty(bean.ad)){
                        mTsfv.setMove(true);
                        mTsfv.setContent("    "+bean.ad);
                    }else{
                        mTsfv.setMove(false);
                        mTsfv.setContent("");
                    }
                    /**滚动文字颜色*/
                    if(!TextUtils.isEmpty(bean.color)){
                        mTsfv.setFontColor(bean.color);
                    }else{
                        mTsfv.setFontColor("#ffffff");
                    }
                break;
            }
        }
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
                Log.e("呼叫===","开始……");
            }
        },5000);
    }

    private void removeFragment() {
        if(mFragment!=null){//删除上一次的视频
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(mFragment);
            transaction.commit();
        }
    }

    /**当前系统时间是否在某个时间段内*/
    private boolean isNewTime(String time) {
        boolean b;
        String firstTime = time.substring(0, time.indexOf("-")).trim().replace(":","");
        String lastTime = time.substring(time.indexOf("-") + 1, time.length()).trim().replace(":","");
        int first = Integer.parseInt(firstTime);
        int last = Integer.parseInt(lastTime);
        if(CurrentTime.getTime()>=first&&CurrentTime.getTime()<last){
            b=true;
            mLastTime=last;
        }else{
            b=false;
        }
        return b;
    }
}
