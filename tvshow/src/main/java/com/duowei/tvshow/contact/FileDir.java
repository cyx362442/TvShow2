package com.duowei.tvshow.contact;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017-03-01.
 */

public class FileDir {
   public static ArrayList<String>mVideoList;
   public static ArrayList<String>mImgList;
   public static final String SDPATH = Environment.getExternalStorageDirectory() + "/";

   public static final String getDir() {
      return SDPATH + "duowei/";
   }

   public static final String getZipVideo(){
      return SDPATH+"duowei/video.zip";
   }

   public static String getVideoName(){
      return SDPATH+"duowei/video/";
   }

   /**获取所有视频文件路径*/
   public static ArrayList<String> getVideoPath(){
      File scanFile = new File(getVideoName());
      mVideoList=new ArrayList();
      if(scanFile.isDirectory()){
         for(File file:scanFile.listFiles()){
            String path = file.getAbsolutePath();
            if (path.endsWith(".mp4") || path.endsWith(".avi") || path.endsWith(".mkv")||path.endsWith(".rmvb")) {
               mVideoList.add(path);
            }
         }
      }else{
         Log.e("=====","scanFile is no direct");
      }
      return mVideoList;
   }
   /**获取所有图片的路径*/
   public static ArrayList<String> getImgPath(){
      mImgList=new ArrayList<>();
      File scanFile = new File(getVideoName());
      if(scanFile.isDirectory()){
         for(File file:scanFile.listFiles()){
            String path = file.getAbsolutePath();
            if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")) {
               mImgList.add(path);
            }
         }
      }else{
         Log.e("=====","scanFile is no direct");
      }
      return mImgList;
   }
}
