package com.duowei.tvshow.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTarget;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.core.AbsFragment;
import com.duowei.tvshow.MainActivity;
import com.duowei.tvshow.R;
import com.duowei.tvshow.bean.LoadFile;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.contact.FileDir;
import com.duowei.tvshow.widget.NumberProgressBar;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoadFragment extends AbsFragment {

    private NumberProgressBar mPb;
    private TextView mSize;
    private TextView mComplete;
    private TextView mSpeed;
    private List<LoadFile> listFile;
    private int num=0;

    public LoadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle bundle = getArguments();
        listFile = (List<LoadFile>) bundle.getSerializable("listfile");
    }

    private void toMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_load, container, false);
        mPb = (NumberProgressBar) inflate.findViewById(R.id.progress);
        mSize = (TextView) inflate.findViewById(R.id.tv_size);
        mComplete = (TextView) inflate.findViewById(R.id.tv_complete);
        mSpeed = (TextView) inflate.findViewById(R.id.tv_speed);
        return inflate;
    }
    @Override
    public void onStart() {
        super.onStart();
        if(listFile.size()<=0){
            Toast.makeText(getActivity(),"下载失败，请检查网络连接设置",Toast.LENGTH_SHORT).show();
            toMainActivity();
            return;
        }
        Aria.download(getActivity())
                .load(listFile.get(num).url)
                .setDownloadPath(FileDir.getVideoName()+listFile.get(num).fileName)
                .start();
    }

    @Override
    public void onResume() {
        super.onResume();
        //注删下载监听
        Aria.download(this).addSchedulerListener(new LoadFragment.MyDialogDownloadCallback());
    }
    @Override
    protected void init(Bundle savedInstanceState) {
        if(listFile.size()<=0){
            return;
        }
        if (Aria.download(getActivity()).taskExists(listFile.get(num).url)) {
            DownloadTarget target = Aria.download(this).load(listFile.get(num).url);
            int p = (int) (target.getCurrentProgress() * 100 / target.getFileSize());
            mPb.setProgress(p);
            mComplete.setText("己完成：0/"+listFile.size());
        }
        DownloadEntity entity = Aria.download(this).getDownloadEntity(listFile.get(num).url);
        if (entity != null) {
            mSize.setText(CommonUtil.formatFileSize(entity.getFileSize()));
            int state = entity.getState();
//            setBtState(state != DownloadEntity.STATE_RUNNING);
        } else {
//            setBtState(true);
        }
    }
    @Override
    protected void onDelayLoad() {
    }
    @Override
    protected int setLayoutId() {
        return 0;
    }
    @Override
    protected void dataCallback(int result, Object obj) {
    }
    /**下载情况监听*/
    private class MyDialogDownloadCallback extends Aria.DownloadSchedulerListener {
        @Override public void onTaskPre(DownloadTask task) {
            super.onTaskPre(task);
            mSize.setText(CommonUtil.formatFileSize(task.getFileSize()));
//            setBtState(false);
        }

        @Override public void onTaskStop(DownloadTask task) {
            super.onTaskStop(task);
//            setBtState(true);
            mSpeed.setText("0.0kb/s");
        }

        @Override public void onTaskCancel(DownloadTask task) {
            super.onTaskCancel(task);
//            setBtState(true);
            mPb.setProgress(0);
            mSpeed.setText("0.0kb/s");
        }

        @Override public void onTaskRunning(DownloadTask task) {
            super.onTaskRunning(task);
            long current = task.getCurrentProgress();
            long len = task.getFileSize();
            if (len == 0) {
                mPb.setProgress(0);
            } else {
                mPb.setProgress((int) ((current * 100) / len));
            }
            mSpeed.setText(task.getSpeed()/1024+"kb/s");
        }

        @Override
        public void onTaskComplete(DownloadTask task) {
            super.onTaskComplete(task);
            num++;
            mPb.setProgress(0);
            mComplete.setText("己完成："+num+"/"+listFile.size());
            if(num<listFile.size()){
                Aria.download(getActivity())
                        .load(listFile.get(num).url)
                        .setDownloadPath(FileDir.getVideoName()+listFile.get(num).fileName)
                        .start();
            }else{
                SharedPreferences preferences = getActivity().getSharedPreferences("Users", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = preferences.edit();
                edit.putString("version", Consts.version);
                edit.commit();
                toMainActivity();
            }
        }
    }
}
