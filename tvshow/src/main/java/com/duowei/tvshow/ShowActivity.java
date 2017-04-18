package com.duowei.tvshow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.duowei.tvshow.bean.OneDataBean;
import com.duowei.tvshow.contact.ConstsCode;
import com.duowei.tvshow.contact.FileDir;
import com.duowei.tvshow.fragment.VideoFragment;
import com.duowei.tvshow.utils.CurrentTime;
import com.duowei.tvshow.view.TextSurfaceView;
import com.squareup.picasso.Picasso;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class ShowActivity extends AppCompatActivity {
    private ServiceBroadCast mBroadCast;
    private ImageView mImageView;
    private int[] mId;
    private File mFile;
    private VideoFragment mFragment;
    private int mLastTime=0;
    private TextSurfaceView mTsfv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        mImageView = (ImageView) findViewById(R.id.image);
        mTsfv = (TextSurfaceView) findViewById(R.id.textView);
        mId = new int[]{R.id.frame01,R.id.frame02,R.id.frame03,
                R.id.frame04,R.id.frame05,R.id.frame06,
                R.id.frame07,R.id.frame08,R.id.frame09,};
        startShow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLastTime=0;
        IntentFilter intentFilter = new IntentFilter(ConstsCode.ACTION_START_HEART);
        mBroadCast = new ServiceBroadCast();
        registerReceiver(mBroadCast,intentFilter);
    }

    @Override
    protected void onStop() {
        JCVideoPlayer.releaseAllVideos();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadCast);
    }
    /**每分钟收一次广播 */
    public class ServiceBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mLastTime>CurrentTime.getTime()){//上次时间段还未结束,返回，继续之前播放
                return;
              }
            String action = intent.getAction();
            if(action.equals(ConstsCode.ACTION_START_HEART)){
                startShow();
            }
        }
    }

    private void startShow() {
        List<OneDataBean> list = DataSupport.findAll(OneDataBean.class);
        for(OneDataBean bean:list){
            String time = bean.time.trim();
            boolean newTime = isNewTime(time);
            if(newTime==true){//发现新的时间段
                JCVideoPlayer.releaseAllVideos();
                removeFragment();//删除上次视频
                    mFile=new File(FileDir.getDir()+bean.image_name);//拼接图片路径
                    if(mFile.exists()){//文件存在则读取
                        Picasso.with(ShowActivity.this).load(mFile).fit().centerInside().into(mImageView);
//                                Glide.with(ShowActivity.this).load(mFile).fitCenter().placeholder(R.mipmap.bg).into(mImageView);
                    }else{//不存在设一张默认的图片
                        Picasso.with(ShowActivity.this).load(R.mipmap.bg).fit().centerInside().into(mImageView);
//                                Glide.with(ShowActivity.this).load(mFile).fitCenter().placeholder(R.mipmap.bg).into(mImageView);
                    }
                    /**视频文件存在*/
                    if(!bean.video_name.equals("null")){
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
