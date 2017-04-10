package com.duowei.tvshow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.duowei.tvshow.contact.FileDir;
import com.duowei.tvshow.image_video.ImageDir;
import com.duowei.tvshow.image_video.PhotoSelectorActivity;
import com.duowei.tvshow.service.BroadService;

import java.io.File;
import java.util.ArrayList;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.view_start).setOnClickListener(this);
        findViewById(R.id.view_image).setOnClickListener(this);
        findViewById(R.id.view_movie).setOnClickListener(this);
        findViewById(R.id.view_setting).setOnClickListener(this);
        findViewById(R.id.view_exit).setOnClickListener(this);

        mIntent = new Intent(this, ShowActivity.class);
        startActivity(mIntent);
        mIntentService = new Intent(this, BroadService.class);
        startService(mIntentService);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mIntentService!=null){
            stopService(mIntentService);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.view_start:
                mIntent = new Intent(this, ShowActivity.class);
                startActivity(mIntent);
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
