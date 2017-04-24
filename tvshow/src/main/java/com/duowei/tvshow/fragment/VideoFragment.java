package com.duowei.tvshow.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.duowei.tvshow.R;
import com.duowei.tvshow.contact.FileDir;

import java.io.File;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment{

    private JCVideoPlayer mJcVideoPlayer;
    private String mVideoname;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle!=null){
            mVideoname = bundle.getString("videoname");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_main, container, false);
        mJcVideoPlayer = (JCVideoPlayer) inflate.findViewById(R.id.jcvideoplayer_full);
        return inflate;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!TextUtils.isEmpty(mVideoname)){
            File file = new File(FileDir.getVideoName() + mVideoname);
            if(!file.exists()){
                Toast.makeText(getActivity(),"视频文件不存在",Toast.LENGTH_LONG).show();
            }else{
                mJcVideoPlayer.setUp(FileDir.getVideoName()+mVideoname,"", "");
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        JCVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void stopPlay(){
      mJcVideoPlayer.stopPlay();
    }

    public void continuePlay(){
        mJcVideoPlayer.continuePlay();
    }
}
