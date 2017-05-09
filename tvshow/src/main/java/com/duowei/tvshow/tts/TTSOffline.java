package com.duowei.tvshow.tts;

import android.content.Context;

import com.duowei.tvshow.contact.Config;
import com.duowei.tvshow.contact.FileDir;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;
import com.unisound.client.SpeechSynthesizerListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017-05-09.
 * 离线语音
 */

public class TTSOffline {
    private TTSOffline() {}
    private static TTSOffline tt=null;
    public static TTSOffline instance(){
        if(tt==null){
            tt=new TTSOffline();
        }
        return tt;
    }

    private static boolean TTS_PLAY_FLAGE = false;
    private static String DB_NAME = "frontend_model";
    private static String ASSETS_NAME = "frontend_model";//这个就是assets下的一个文件
    private static String DB_NAME2 = "backend_lzl";
    private static String ASSETS_NAME2 = "backend_lzl";//这个就是assets下的一个文件
    private SpeechSynthesizer mTTSPlayer;
    public void copyBigDataBase(Context context){
        File dir = new File(FileDir.getDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dbf = new File(FileDir.getDir() + DB_NAME);
        File dbf2 = new File(FileDir.getDir() + DB_NAME2);
        if (!dbf.exists()) {
            InputStream myInput;
            String outFileName = FileDir.getDir() + DB_NAME;
            try {
                FileOutputStream myOutput = new FileOutputStream(outFileName);
                myInput = context.getAssets().open(ASSETS_NAME);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
                myInput.close();
                myOutput.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!dbf2.exists()) {
            InputStream myInput;
            String outFileName = FileDir.getDir() + DB_NAME2;
            try {
                FileOutputStream myOutput = new FileOutputStream(outFileName);
                myInput = context.getAssets().open(ASSETS_NAME2);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
                myInput.close();
                myOutput.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initTts(Context context){
        // 初始化语音合成对象
        mTTSPlayer = new SpeechSynthesizer(context, Config.appKey, Config.secret);
        // 设置本地合成
        mTTSPlayer.setOption(SpeechConstants.TTS_SERVICE_MODE, SpeechConstants.TTS_SERVICE_MODE_LOCAL);
//        File _FrontendModelFile = new File(FileDir.getDir() + DB_NAME);
//        if (!_FrontendModelFile.exists()) {
//            toastMessage("文件：" + mFrontendModel + "不存在，请将assets下相关文件拷贝到SD卡指定目录！");
//        }
//        File _BackendModelFile = new File(DB_PATH + DB_NAME2);
//        if (!_BackendModelFile.exists()) {
//            toastMessage("文件：" + mBackendModel + "不存在，请将assets下相关文件拷贝到SD卡指定目录！");
//        }
        // 设置前端模型
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_FRONTEND_MODEL_PATH, FileDir.getDir() + DB_NAME);
        // 设置后端模型
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_BACKEND_MODEL_PATH, FileDir.getDir() + DB_NAME2);
        // 设置回调监听
        mTTSPlayer.setTTSListener(new SpeechSynthesizerListener() {

            @Override
            public void onEvent(int type) {
                switch (type) {
                    case SpeechConstants.TTS_EVENT_INIT:
                        // 初始化成功回调
                        break;
                    case SpeechConstants.TTS_EVENT_SYNTHESIZER_START:
                        // 开始合成回调
                        break;
                    case SpeechConstants.TTS_EVENT_SYNTHESIZER_END:
                        // 合成结束回调
                        break;
                    case SpeechConstants.TTS_EVENT_BUFFER_BEGIN:
                        // 开始缓存回调
                        break;
                    case SpeechConstants.TTS_EVENT_BUFFER_READY:
                        // 缓存完毕回调
                        break;
                    case SpeechConstants.TTS_EVENT_PLAYING_START:
                        // 开始播放回调
                        break;
                    case SpeechConstants.TTS_EVENT_PLAYING_END:
                        // 播放完成回调
                        setTTSButtonReady();
                        break;
                    case SpeechConstants.TTS_EVENT_PAUSE:
                        // 暂停回调
                        break;
                    case SpeechConstants.TTS_EVENT_RESUME:
                        // 恢复回调
                        break;
                    case SpeechConstants.TTS_EVENT_STOP:
                        // 停止回调
                        break;
                    case SpeechConstants.TTS_EVENT_RELEASE:
                        // 释放资源回调
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onError(int type, String errorMSG) {
                // 语音合成错误回调
                setTTSButtonReady();
            }
        });
        // 初始化合成引擎
        mTTSPlayer.init("");
    }

    public void TTSPlay(String msg) {
        if (!TTS_PLAY_FLAGE) {
            mTTSPlayer.playText(msg);
            setTTSButtonStop();
        } else {
            mTTSPlayer.stop();
            setTTSButtonReady();
        }
    }

    private void setTTSButtonStop() {
        TTS_PLAY_FLAGE = true;
    }

    private void setTTSButtonReady() {
        TTS_PLAY_FLAGE = false;
    }

    public void setRelease(){
        // 主动释放离线引擎
        if (mTTSPlayer != null) {
            mTTSPlayer.release(SpeechConstants.TTS_RELEASE_ENGINE, null);
        }
    }
}
