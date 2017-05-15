package com.duowei.tvshow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.duowei.tvshow.bean.OneDataBean;
import com.duowei.tvshow.contact.ConstsCode;
import com.duowei.tvshow.contact.FileDir;
import com.duowei.tvshow.event.FinishEvent;
import com.duowei.tvshow.event.FinishMain;
import com.duowei.tvshow.image_video.ImageDir;
import com.duowei.tvshow.image_video.PhotoSelectorActivity;
import com.duowei.tvshow.service.BroadService;
import com.duowei.tvshow.utils.CurrentTime;

import org.greenrobot.eventbus.Subscribe;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_GET_PHOTOS = 1000;
    private ArrayList<String> selectedImagesPaths = new ArrayList<String>();
    /**
     * 存储视频路径的集合
     */
    private ArrayList<String> selectedVedioPaths = new ArrayList<String>();
    /**
     * 常量，标识录像选择请求码
     */
    private static final int REQUEST_CODE_GET_VEDIOS = 2000;
    private ArrayList<File> files;

    private Intent mIntent;
    private Intent mIntentService;
    private ServiceBroadCast mBroadCast;
    private int mLastTime=0;
    private ArrayList<String>listUrl=new ArrayList<>();

    private final int REQUEST_CODE=0;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        initUI();
        //开启时间段轮询
        mIntentService = new Intent(this, BroadService.class);
        startService(mIntentService);
        //节目伦询
        mIntentFilter = new IntentFilter(ConstsCode.ACTION_START_HEART);
        mBroadCast = new ServiceBroadCast();
        registerReceiver(mBroadCast, mIntentFilter);

    }

    /**启用广播 */
    public class ServiceBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /**节目更新*/
            if(mLastTime> CurrentTime.getTime()){//上次时间段还未结束,返回，继续之前播放
                return;
            }
            if(action.equals(ConstsCode.ACTION_START_HEART)){
                startShow();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE){
            if(mIntentService!=null){
                stopService(mIntentService);
            }
//            unregisterReceiver(mBroadCast);
        }
    }

    @Subscribe
    public void FinishActivity(FinishMain event){
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLastTime=0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mIntentService!=null){
            stopService(mIntentService);
        }
        unregisterReceiver(mBroadCast);
        EventBus.getDefault().unregister(this);
    }

    private void initUI() {
        findViewById(R.id.view_start).setOnClickListener(this);
        findViewById(R.id.view_image).setOnClickListener(this);
        findViewById(R.id.view_movie).setOnClickListener(this);
        findViewById(R.id.view_setting).setOnClickListener(this);
        findViewById(R.id.view_exit).setOnClickListener(this);
    }

    /**时间段节目轮询*/
    private void startShow() {
        List<OneDataBean> list = DataSupport.findAll(OneDataBean.class);
        for(OneDataBean bean:list){
            String time = bean.time.trim();
            boolean newTime = isNewTime(time);
            if(newTime==true){//发现新的时间段
                Log.e("=======","发现新的时间段……");
                EventBus.getDefault().post(new FinishEvent());
                /**纯图片播放模式*/
                if(!bean.image_name.equals("null")&&bean.video_name.equals("null")){
                    Intent intent = new Intent(this, ShowActivity.class);
                    intent.putExtra("image_name",bean.image_name);
                    intent.putExtra("ad",bean.ad);
                    intent.putExtra("color",bean.color);
                    startActivityForResult(intent,REQUEST_CODE);
                }
                /**纯视频播放模式*/
                else if(!bean.video_name.equals("null")&&bean.image_name.equals("null")){
                    listUrl.clear();
                    listUrl.add(FileDir.getVideoName()+bean.video_name);
                    Intent intent = new Intent(this, VideoFullActivity.class);
                    intent.putStringArrayListExtra("selectPaths",listUrl);
                    intent.putExtra("ad",bean.ad);
                    intent.putExtra("color",bean.color);
                    startActivityForResult(intent,REQUEST_CODE);
                }
                /**图片、视频混合模式*/
                else if(!bean.video_name.equals("null")&&!bean.image_name.equals("null")){
                    Intent intent = new Intent(this, ShowActivity.class);
                    intent.putExtra("image_name",bean.image_name);
                    intent.putExtra("video_name",bean.video_name);
                    intent.putExtra("video_palce",bean.video_palce);
                    intent.putExtra("ad",bean.ad);
                    intent.putExtra("color",bean.color);
                    startActivityForResult(intent,REQUEST_CODE);
                }
                break;
            }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.view_start:
                startService(mIntentService);
                registerReceiver(mBroadCast,mIntentFilter);
                startShow();
                break;
            case R.id.view_movie:
                ArrayList<String> videoPath = FileDir.getVideoPath();
                if(videoPath.size()<=0){
                    Toast.makeText(this,"暂无视频",Toast.LENGTH_LONG).show();
                    return;
                }
                mIntent = new Intent(this, PhotoSelectorActivity.class);
                //若传入已选中的路径则在选择页面会呈现选中状态
                mIntent.putStringArrayListExtra("selectedPaths", selectedVedioPaths);
                mIntent.putExtra("loadType", ImageDir.Type.VEDIO.toString());
                mIntent.putExtra("sizeLimit", 1 * 1024 * 1024);
                startActivityForResult(mIntent, REQUEST_CODE_GET_VEDIOS);
                break;
            case R.id.view_image:
                ArrayList<String> imgPath = FileDir.getImgPath();
                if(imgPath.size()<=0){
                    Toast.makeText(this,"暂无图片",Toast.LENGTH_LONG).show();
                    return;
                }
                mIntent = new Intent(this, PhotoSelectorActivity.class);
                //若传入已选中的路径则在选择页面会呈现选中状态
                mIntent.putStringArrayListExtra("selectedPaths", selectedImagesPaths);
                startActivityForResult(mIntent, REQUEST_CODE_GET_PHOTOS);
                break;
            case R.id.view_setting:
                mIntent=new Intent(this,SettingActivity.class);
                startActivity(mIntent);
                finish();
                break;
            case R.id.view_exit:
                finish();
                break;
        }
    }
}
