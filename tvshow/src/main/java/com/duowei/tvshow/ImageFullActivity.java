package com.duowei.tvshow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.duowei.tvshow.view.RecyclerBanner;

import java.util.ArrayList;

public class ImageFullActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full);
        Intent intent = getIntent();
        if(intent==null){
            Toast.makeText(this,"找到不到图片",Toast.LENGTH_LONG).show();
            return;
        }
        ArrayList<String> imgPaths = intent.getStringArrayListExtra("selectPaths");
        RecyclerBanner rb = (RecyclerBanner) findViewById(R.id.recyclebanner);
        rb.setDatas(imgPaths);
    }
}
