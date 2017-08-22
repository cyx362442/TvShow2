package com.duowei.tvshow;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadReceiver;
import com.duowei.tvshow.bean.LoadFile;
import com.duowei.tvshow.bean.OneDataBean;
import com.duowei.tvshow.bean.ZoneTime;
import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.contact.FileDir;
import com.duowei.tvshow.fragment.LoadFragment;
import com.duowei.tvshow.httputils.DownHTTP;
import com.duowei.tvshow.httputils.VolleyResultListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity{
    private String currentVersion="";//当前版本
    private String mZoneNum="";
    private String url;
    private String mWeid;
    private String mWurl;
    private String mStoreid;//门店ID
    private Intent mIntent;
    private List<LoadFile>listFile=new ArrayList<>();
    private List<OneDataBean>mOneDataBeanList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        if(!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
            Toast.makeText(this,"当前内存卡不可用",Toast.LENGTH_LONG).show();
            return;
        }
        if (getPreferData()) return;
        Http_contents();
    }
    private boolean getPreferData() {
        SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        currentVersion = preferences.getString("version", "");
        mWurl = preferences.getString("wurl", "");
        mWeid = preferences.getString("weid", "");
        mStoreid=preferences.getString("storeid","");
        mZoneNum=preferences.getString("zoneNum","");
        String times = preferences.getString("callvalue", "关闭");
        if(times.equals("关闭")){
            Consts.callTime = 0;
        }else{
            Consts.callTime = Integer.parseInt(times.substring(0, 1));
        }
        Consts.ip="http://"+preferences.getString("ip","")+":2233/server/ServerSvlt?";
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
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement =  jsonParser.parse(response);
                boolean jsonObject = jsonElement.isJsonObject();
                if(jsonObject==false){
                    return;
                }
                Gson gson = new Gson();
                ZoneTime zoneTime = gson.fromJson(response, ZoneTime.class);
                String version = zoneTime.getVersion();//新版本号
                if(currentVersion.equals(version)){//版本号相同直接登录
                    toMainActivity();
                }else{//版本号不同更新
                    listFile.clear();
                    mOneDataBeanList.clear();
                    Consts.version=version;
                    List<ZoneTime.ZoneTimeBean> list_zone = zoneTime.getZone_time();//电视区域信息
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
                                if(!image_url.equals("null")){
                                    listFile.add(new LoadFile(image_url,image_name));
                                }
                                if(!video_url.equals("null")){
                                    listFile.add(new LoadFile(video_url,video_name));
                                }
                            }
                        }
                    }
                    if(listFile.size()<=0){
                        Toast.makeText(WelcomeActivity.this,"下载失败",Toast.LENGTH_LONG).show();
                        toMainActivity();
                        return;
                    }
                    deleteDir();
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("listfile",(Serializable) listFile);
                    LoadFragment loadFragment = new LoadFragment();
                    loadFragment.setArguments(bundle);
                    ft.replace(R.id.frame, loadFragment);
                    ft.commit();
//                    /**插入数据库*/
                    DataSupport.deleteAll(OneDataBean.class);
                    DataSupport.saveAll(mOneDataBeanList);
                }
            }
        });
    }
    private void toMainActivity(){
        mIntent = new Intent(this, MainActivity.class);
        startActivity(mIntent);
        finish();
    }

    //删除文件夹和文件夹里面的文件
    public  void deleteDir() {
        File dir = new File(FileDir.getVideoName());
        if (dir == null || !dir.exists() || !dir.isDirectory()||dir.listFiles()==null){
        }else{
            for (File file : dir.listFiles()) {
                if (file.isFile())
                    file.delete(); // 删除所有文件
                else if (file.isDirectory())
                    deleteDir(); // 递规的方式删除文件夹
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        toMainActivity();
    }
}
