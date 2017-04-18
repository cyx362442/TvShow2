package com.duowei.tvshow;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.duowei.tvshow.bean.LoadFile;
import com.duowei.tvshow.bean.OneDataBean;
import com.duowei.tvshow.bean.ZoneTime;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.contact.FileDir;
import com.duowei.tvshow.helper.VersionUpdate;
import com.duowei.tvshow.helper.VersionUpdateImpl;
import com.duowei.tvshow.httputils.AsyncUtils;
import com.duowei.tvshow.httputils.DownHTTP;
import com.duowei.tvshow.httputils.VolleyResultListener;
import com.duowei.tvshow.httputils.ZipExtractorTask;
import com.duowei.tvshow.service.DownloadService;
import com.duowei.tvshow.widget.NumberProgressBar;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.duowei.tvshow.service.DownloadService.BUNDLE_KEY_DOWNLOAD_FILE;
import static com.duowei.tvshow.service.DownloadService.BUNDLE_KEY_DOWNLOAD_NAME;

public class WelcomeActivity extends AppCompatActivity implements VersionUpdateImpl {

    private SharedPreferences.Editor mEdit;
    private String currentVersion="";//当前版本
    private String mZoneNum="";
    private String url;
    private String mWeid;
    private String mWurl;
    private String mStoreid;//门店ID
    private Intent mIntent;
    private List<LoadFile>listFile=new ArrayList<>();
    private List<OneDataBean>mOneDataBeanList=new ArrayList<>();
    private int loadPosition=0;

    /**接收广播*/
    private NumberProgressBar bnp;
    private boolean isBindService;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
            DownloadService downloadService = binder.getService();
            //接口回调，下载进度
            downloadService.setOnProgressListener(new DownloadService.OnProgressListener() {
                @Override
                public void onProgress(float fraction) {
                    bnp.setProgress((int)(fraction * 100));
                    //判断是否真的下载完成，以及是否注册绑定过服务
                    if (fraction == DownloadService.UNBIND_SERVICE && isBindService) {
                        mLl_loading.setVisibility(View.GONE);
                        unbindService(conn);
                        isBindService = false;
                        //解压、删除文件
                        deleteDir();
                    }
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private LinearLayout mLl_loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        bnp = (NumberProgressBar) findViewById(R.id.number_bar);
        mLl_loading = (LinearLayout) findViewById(R.id.ll_loading);
        if(!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
            Toast.makeText(this,"当前内存卡不可用",Toast.LENGTH_LONG).show();
            return;
        }
        if (getPreferData()) return;
        Http_contents();
    }

    private boolean getPreferData() {
        SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        mEdit = preferences.edit();
        currentVersion = preferences.getString("version", "");
        mWurl = preferences.getString("wurl", "");
        mWeid = preferences.getString("weid", "");
        mStoreid=preferences.getString("storeid","");
        mZoneNum=preferences.getString("zoneNum","");
        if(TextUtils.isEmpty(mWurl)||TextUtils.isEmpty(mWeid)||TextUtils.isEmpty(mStoreid)||TextUtils.isEmpty(mZoneNum)){
            mIntent=new Intent(this,SettingActivity.class);
            startActivity(mIntent);
            finish();
            return true;
        }
        url ="http://"+mWurl+"/mobile.php?act=module&weid="+mWeid+"&name=light_box_manage&do=GetZoneTime&storeid="+mStoreid;
        return false;
    }

    int num=0;
    private void Http_contents() {
        HashMap<String, String> map = new HashMap<>();
        DownHTTP.postVolley(this.url, map,new VolleyResultListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(WelcomeActivity.this,"网络连接失败",Toast.LENGTH_LONG).show();
                try {
                    Thread.sleep(3000);
                    num++;
                    if(num<3){
                        Http_contents();
                    }
                    if(num==2){
                        toMainActivity();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                ZoneTime zoneTime = gson.fromJson(response, ZoneTime.class);
                String version = zoneTime.getVersion();//新版本号
                if(currentVersion.equals(version)){//版本号相同直接登录
                    toMainActivity();
                }else{//版本号不同更新
                    listFile.clear();
                    mOneDataBeanList.clear();

                    Consts.version=version;
                    String down_data = zoneTime.getDown_data();//压缩包下载地址
                    List<ZoneTime.ZoneTimeBean> list_zone = zoneTime.getZone_time();//电视区域信息
                    DataSupport.deleteAll(OneDataBean.class);
                    /**找到该电视区号对应的数据信息集*/
                    for(int i=0;i<list_zone.size();i++){
                        ZoneTime.ZoneTimeBean.ZoneBean zone = list_zone.get(i).getZone();//电视区号
                        if(mZoneNum.equals(zone.getZone())){//如果区号等于当前电视区号
                            List<ZoneTime.ZoneTimeBean.OneDataBean> one_data = list_zone.get(i).getOne_data();
                            for(int j=0;j<one_data.size();j++){
                                String time = one_data.get(j).getTime();//起始跟结束时间
                                String ad = one_data.get(j).getAd();//动态广告词
                                String color = one_data.get(j).getColor();
                                String video_palce = one_data.get(j).getVideo_palce( );//视频的位置
                                String image_name = one_data.get(j).getFile_name().getImage_name();//图片名称
                                String video_name = one_data.get(j).getFile_name().getVideo_name();//视频名称
                                String image_url = one_data.get(j).getFile_url().getImage_url();
                                String video_url = one_data.get(j).getFile_url().getVideo_url();
                                //数据库资源
                                mOneDataBeanList.add(new OneDataBean(time, ad, video_palce, image_name, video_name,image_url,video_url,color));
                                //查询本地数据库
//                                List<OneDataBean> listImage = DataSupport.select("image_name").where("image_name=?",image_name).find(OneDataBean.class);
//                                List<OneDataBean> listVideo = DataSupport.select("video_name").where("video_name=?",video_name).find(OneDataBean.class);
//                                //图片名称不存在，添加图片URL地址
//                                if(listImage.size()<=0){
//                                    listFile.add(new LoadFile(image_url,image_name));
//                                }
//                                //视频名称不存在，添加视频URL地址
//                                if(listVideo.size()<=0){
//                                    listFile.add(new LoadFile(image_url,image_name));
//                                }
                                /**插入数据库*/
                                OneDataBean oneDataBean = new OneDataBean(time, ad, video_palce, image_name, video_name,image_url,video_url,color);
                                oneDataBean.save();
                            }
                        }
                    }
//                    /**插入数据库*/
//                    DataSupport.deleteAll(OneDataBean.class);
//                    DataSupport.saveAll(mOneDataBeanList);
                    /**下载图片、视频*/
//                    Http_File("http://7xpj8w.com1.z0.glb.clouddn.com/video15.zip");
//                    startDownLoad("http://7xpj8w.com1.z0.glb.clouddn.com/video15.zip");
//                    Http_File(down_data);
//                    startDownLoad(down_data);
                    VersionUpdate.checkVersion(WelcomeActivity.this,down_data);
//                    VersionUpdate.checkVersion(WelcomeActivity.this,"http://7xpj8w.com1.z0.glb.clouddn.com/video15.zip");
                }
            }
        });
    }
    private void Http_File(String url) {
        AsyncUtils asyncUtils = new AsyncUtils(WelcomeActivity.this);
        asyncUtils.execute(url);
    }
    private void toMainActivity(){
        mIntent = new Intent(this, MainActivity.class);
        startActivity(mIntent);
        finish();
    }

    //删除文件夹和文件夹里面的文件
    public  void deleteDir() {
        File dir = new File(FileDir.getVideoName());
        if (dir == null || !dir.exists() || !dir.isDirectory()){
            /**解压出新文件*/
            ZipExtractorTask task = new ZipExtractorTask(FileDir.getZipVideo(), FileDir.getVideoName(), this, true);
            task.execute();
        }else{
            for (File file : dir.listFiles()) {
                if (file.isFile())
                    file.delete(); // 删除所有文件
                else if (file.isDirectory())
                    deleteDir(); // 递规的方式删除文件夹
            }
            /**解压出新文件*/
            ZipExtractorTask task = new ZipExtractorTask(FileDir.getZipVideo(), FileDir.getVideoName(), this, true);
            task.execute();
        }
    }

    @Override
    public void bindService(String url) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(DownloadService.BUNDLE_KEY_DOWNLOAD_URL, url);
//        intent.putExtra(BUNDLE_KEY_DOWNLOAD_NAME,name);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(DownloadService.BUNDLE_KEY_DOWNLOAD_FILE, (Serializable) listFile);
//        intent.putExtras(bundle);
        isBindService = bindService(intent, conn, BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mIntent=new Intent(this,MainActivity.class);
        startActivity(mIntent);
        finish();
    }
}
