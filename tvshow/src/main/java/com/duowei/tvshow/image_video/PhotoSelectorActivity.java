package com.duowei.tvshow.image_video;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.duowei.tvshow.ImageFullActivity;
import com.duowei.tvshow.R;
import com.duowei.tvshow.VideoFullActivity;
import com.duowei.tvshow.contact.FileDir;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class PhotoSelectorActivity extends AppCompatActivity {
    /**
     * 通过键值对存储图片或视频的路径
     */
    private HashMap<String, ImageDir> imageDirsMap = new HashMap<String, ImageDir>();
    /**
     * 显示系统图片的视图
     */
    GridView gvPhotos;
    /**
     * 定义图片路径的实体
     */
    ImageDir currentDir;
    /**
     * 定义一个弹出popupwindow变量名
     */
    ImageFolderPopWindow popDir;
    /**
     * 最外层布局的变量
     */
    View lyTopBar;
    /**
     * 定义一个Button的成员变量
     */
    Button btnNext;
    int maxPicSize;
    /**
     * 记录图片的最大选择
     */
    private int maxCount = 10;

    private File cameraFile;

    /**
     * 在图片选择点击后请求码
     */
    public static final int REQUEST_CODE_IMAGE_SWITCHER = 2000;
    private ArrayList<String> selectedFath;
    /**
     * 上传类型
     */
    private ImageDir.Type loadType = ImageDir.Type.IMAGE;
    /**
     * 上传大小
     */
    private long sizeLimit=5*1024*1024;//5m
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_selector);
        initData();
        init();
    }
    /**
     * 初始化数据
     */
    private void initData() {
        //接收从DemoActivity传来的图片选择和录像选择路径
        selectedFath = getIntent().getStringArrayListExtra("selectedPaths");
        //判断是否存在loadType和sizeLimit，如果有，取出值  （图片没有，视频有）
        if (getIntent().hasExtra("loadType")) {
            loadType = ImageDir.Type.valueOf(getIntent().getStringExtra("loadType"));
        }
        if(getIntent().hasExtra("sizeLimit")){
            sizeLimit=getIntent().getIntExtra("sizeLimit", 1024);
        }
    }
    /**
     * 判断类型为图像
     */
    private boolean isImageType(){
        return loadType== ImageDir.Type.IMAGE;
    }
    /**
     * 判断类型为视频
     */
    private boolean isVedioType(){
        return loadType== ImageDir.Type.VEDIO;
    }

    /**
     * 初始化控件
     */
    private void init() {
        gvPhotos = (GridView) findViewById(R.id.gv_photos);
        btnNext = (Button) findViewById(R.id.btn_next);
        //如果是图片类型，显示图片列表，否则显示视频列表
        if(isImageType()){
            loadImagesList();
        }
        if(isVedioType()){
            loadVedioImagesList();
        }
        //传入到ImageFolderPopWindow构造方法中 loadType 加载的类型（图片或视频）
        popDir = new ImageFolderPopWindow(this,
                PhoneStateUtils.getScreenWidth(this),PhoneStateUtils.getScreenHeight(this) / 2);
        //设置外部可触摸
        popDir.setOutsideTouchable(true);
        lyTopBar = findViewById(R.id.ly_top_bar);
        popDir.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
        //下拉框的点击事件
        popDir.setOnPopClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //取出路径Tag
                ImageDir dir = (ImageDir) v.getTag();
                currentDir = dir;
                loadImages(currentDir);
                popDir.dismiss();
            }
        });
    }

    /**
     * 开启一个子线程加载下拉框图片列表
     */
    private void loadImagesList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //查询所下载图片的地址
                ArrayList<String> imgPath = FileDir.getImgPath();
                for(int i=0;i<imgPath.size();i++){
                    String filePath=imgPath.get(i);
                    File imageFile = new File(filePath);
                    ImageDir dir = addToDir(imageFile);
                    // 文件中图片的长度
                    if (dir.files.size() > maxPicSize) {
                        maxPicSize = dir.files.size();
                        currentDir = dir;
                    }

                    if (selectedFath.contains(filePath)) {
                        dir.selectedFiles.add(filePath);
                    }
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //加载图片列表
                        loadImages(currentDir);
                    }
                });
            }
        }).start();
    }

    /**
     * 加载下拉框视频列表
     */
    private void loadVedioImagesList() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                //查询所下载视频的地址
                ArrayList<String> imgPath = FileDir.getVideoPath();
                for(int i=0;i<imgPath.size();i++){
                    String filePath=imgPath.get(i);
                    File imageFile = new File(filePath);
                    ImageDir dir = addToDir(imageFile);
                    // 文件中图片的长度
                    if (dir.files.size() > maxPicSize) {
                        maxPicSize = dir.files.size();
                        currentDir = dir;
                    }

                    if (selectedFath.contains(filePath)) {
                        dir.selectedFiles.add(filePath);
                    }
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //loadImages(currentDir);
                        loadVedioImages(currentDir);
                    }
                });
            }
        }).start();
    }

    /**
     * 添加图片或视频地址
     *
     * @param imageFile
     */
    private ImageDir addToDir(File imageFile) {
        ImageDir imageDir;
        File parentDirFile = imageFile.getParentFile();
        String parentFilePath = parentDirFile.getPath();
        if (!imageDirsMap.containsKey(parentFilePath)) {
            imageDir = new ImageDir(parentFilePath);

            imageDir.dirName = parentDirFile.getName();
            imageDirsMap.put(parentFilePath, imageDir);
            imageDir.firstImagePath = imageFile.getPath();
            imageDir.addFile(imageFile.toString());
        } else {
            imageDir = imageDirsMap.get(parentFilePath);
            imageDir.addFile(imageFile.toString());
        }
        return imageDir;
    }

    /**
     * 加载图片
     *
     * @param imageDir 图片路径实体
     */
    private void loadImages(final ImageDir imageDir) {
        final PhotoSelectorAdapter adapter = new PhotoSelectorAdapter(PhotoSelectorActivity.this, imageDir);
        gvPhotos.setAdapter(adapter);
        gvPhotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    return;
                }
                if(adapter.isCheck[i]==false){
                    imageDir.selectedFiles.add(imageDir.getFiles().get(i-1));
                    adapter.isCheck[i]=!adapter.isCheck[i];
                }else if(adapter.isCheck[i]==true){
                    imageDir.selectedFiles.remove(imageDir.getFiles().get(i-1));
                    adapter.isCheck[i]=!adapter.isCheck[i];
                }
                adapter.notifyDataSetChanged();
                updateNext();
            }
        });
    }

    //加载视频
    private void loadVedioImages(final ImageDir imageDir) {
        final PhotoSelectorAdapter adapter = new PhotoSelectorAdapter(PhotoSelectorActivity.this, imageDir);
        gvPhotos.setAdapter(adapter);

        gvPhotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    return;
                }
                if(adapter.isCheck[i]==false){
                    imageDir.selectedFiles.add(imageDir.getFiles().get(i-1));
                    adapter.isCheck[i]=!adapter.isCheck[i];
                }else if(adapter.isCheck[i]==true){
                    imageDir.selectedFiles.remove(imageDir.getFiles().get(i-1));
                    adapter.isCheck[i]=!adapter.isCheck[i];
                }
                adapter.notifyDataSetChanged();
                updateNext();
            }
        });
    }


    /**
     * 更新下一步
     */
    public void updateNext() {
        //如果选择了图片,btnNext可以选择，记录选择的数量，设置字体为白色。否则不能选择，设置字体为黑色
        if (getSelectedPictureCont() > 0) {
            btnNext.setSelected(true);
            btnNext.setText("开始播放(" + getSelectedPictureCont() + ")");
            btnNext.setTextColor(Color.WHITE);
        } else {
            btnNext.setSelected(false);
            btnNext.setText("开始播放");
            btnNext.setTextColor(Color.BLACK);
        }
    }

    /**
     * 选择按钮
     * @param view
     */
    public void popImageDir(View view) {
        if (popDir.isShowing()) {
            popDir.dismiss();
            view.setSelected(false);
        } else {
            popDir.popWindow(imageDirsMap, lyTopBar);
            view.setSelected(true);
        }
    }

    /**
     * 下一步按钮
     * @param view
     */
    public void goNext(View view) {
        if(isImageType()){//图片
            mIntent = new Intent(this, ImageFullActivity.class);
        }else if(isVedioType()){//视频
            mIntent=new Intent(this, VideoFullActivity.class);
        }
        mIntent.putExtra("selectPaths", getSelectedPicture());
        startActivity(mIntent);
        finish();
    }

    /**
     * 取消按钮
     * @param view
     */
    public void goBack(View view) {
        finish();
    }

    /**
     * 发广播更新图册
     * @param path
     */
    public void updateGalleray(String path){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        //在图片选择点击后返回事件
        if (requestCode == REQUEST_CODE_IMAGE_SWITCHER) {
            String[] paths = data.getStringArrayExtra("selectPaths");
            for (int i = 0; i < paths.length; i++) {
                currentDir.selectedFiles.add(paths[i]);
            }
            loadImages(currentDir);
            updateNext();
        }
    }

    /**
     * 获取现在的图片数
     * @return 选择数
     */
    public int getSelectedPictureCont() {
        int count = 0;
        for (String name : imageDirsMap.keySet()) {
            count += imageDirsMap.get(name).selectedFiles.size();
        }
        return count;
    }

    /**
     * 获取图片的选择路径
     * @return 路径集合
     */
    public ArrayList<String> getSelectedPicture() {
        ArrayList<String> paths = new ArrayList<String>();
        for (String name : imageDirsMap.keySet()) {
            paths.addAll(imageDirsMap.get(name).selectedFiles);
        }
        return paths;
    }
}
