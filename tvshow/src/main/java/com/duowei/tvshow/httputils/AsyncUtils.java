package com.duowei.tvshow.httputils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.duowei.tvshow.contact.FileDir;
import com.duowei.tvshow.event.ReConnect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017-01-09.
 */

public class AsyncUtils extends AsyncTask<String, Integer, Integer> {
    Context context;
    private ProgressDialog mProgressDialog;

    public AsyncUtils(Context context) {
        this.context = context;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("提示");
        mProgressDialog.setMessage("文件下载中……");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        mProgressDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        mProgressDialog.show();
    }
    @Override
    protected Integer doInBackground(String... params) {
        int count;
        int result = 0;
        long total = 0;
        int lenghtOfFile = 0;
        File dir = new File(FileDir.getDir());//路径视频
        if (!dir.exists()) {//路径不存在则创建
            dir.mkdir();
        }
        File fileZip = new File(FileDir.getZipVideo());//下载保存的位置
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            URL url = new URL(params[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();//开启连接
            urlConnection.connect();
            urlConnection.setConnectTimeout(5*1000);  //设置超时时间
            lenghtOfFile = urlConnection.getContentLength();//获取下载文件的总长度
            is = urlConnection.getInputStream();// 开启流
            fos = new FileOutputStream(fileZip);// 开启写的流
            byte[] bytes = new byte[1024*10];
            while ((count = is.read(bytes)) != -1) {
                fos.write(bytes, 0, count);
                total += count;
                publishProgress((int)(total*50/lenghtOfFile));
            }
            fos.flush();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(fos!=null){
                    fos.close();
                }
                if(is!=null){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (lenghtOfFile == 0 || total < lenghtOfFile) {
            result = -1;
        } else {
            result = 1;
        }
        return result;
    }
    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
        mProgressDialog.incrementProgressBy(progress[0]);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        switch (integer) {
            case 0:
                mProgressDialog.dismiss();
                break;
            case -1:
                mProgressDialog.dismiss();
                EventBus.getDefault().post(new ReConnect());
//                Toast.makeText(context,"下载连接失败",Toast.LENGTH_SHORT).show();
                break;
            case 1:
                mProgressDialog.setMessage("下载成功，正在解压中……");
                mProgressDialog.dismiss();
                deleteDir();//删除原文件，解压出新文件
                break;
            default:
                break;
        }
    }
    //删除文件夹和文件夹里面的文件
    public  void deleteDir() {
        File dir = new File(FileDir.getVideoName());
        if (dir == null || !dir.exists() || !dir.isDirectory()){
            /**解压出新文件*/
            ZipExtractorTask task = new ZipExtractorTask(FileDir.getZipVideo(), FileDir.getVideoName(), context, true);
            task.execute();
        }else{
            for (File file : dir.listFiles()) {
                if (file.isFile())
                    file.delete(); // 删除所有文件
                else if (file.isDirectory())
                    deleteDir(); // 递规的方式删除文件夹
            }
            /**解压出新文件*/
            ZipExtractorTask task = new ZipExtractorTask(FileDir.getZipVideo(), FileDir.getVideoName(), context, true);
            task.execute();
        }
    }
}
