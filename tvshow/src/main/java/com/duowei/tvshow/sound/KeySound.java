package com.duowei.tvshow.sound;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

import com.duowei.tvshow.R;

public class KeySound {
	private static Context context;
	private static SoundPool sp = null;// 声明一个SoundPool的引用
	private static HashMap<Character, Integer> hm;// 声明一个HashMap来存放声音资源
	private static int currentStreamId;// 当前播放的StreamId
	private static Boolean isFinishedLoad = false;// 查看音乐文件是否加载完毕
	private static KeySound ks=new KeySound();
	public static KeySound getContext(Context context){
		KeySound.context=context;
		initSoundPool();
		return ks;
		}
	public static void playSound(char sound, int loop) {
		String log;
		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		float currentStreamVolume = am
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxStreamVolume = am
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float setVolume = (float) currentStreamVolume / maxStreamVolume;
		if (isFinishedLoad)
			currentStreamId = sp.play(hm.get(sound), 1, 1, 1,
					loop, 1.0f);
		log = "playSound currentStreamId:" + String.valueOf(currentStreamId);
	}

	public static void initSoundPool() {
		// 创建了一个最多支持4个流同时播放的，类型标记为音乐的SoundPool。采样率转化质量，当前无效果，使用0作为默认值
		sp = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
		sp.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				// TODO Auto-generated method stub
				isFinishedLoad = true;
			}
		});
		hm = new HashMap<Character, Integer>();// 创建HashMap对象
		hm.put('0', sp.load(context, R.raw.s0, 0));//加载资源文件中音源到容器，最好为.wav格式的！
		hm.put('1', sp.load(context, R.raw.s1, 0));
		hm.put('2', sp.load(context, R.raw.s2, 0));
		hm.put('3', sp.load(context, R.raw.s3, 0));
		hm.put('4', sp.load(context, R.raw.s4, 0));
		hm.put('5', sp.load(context, R.raw.s5, 0));
		hm.put('6', sp.load(context, R.raw.s6, 0));
		hm.put('7', sp.load(context, R.raw.s7, 0));
		hm.put('8', sp.load(context, R.raw.s8, 0));
		hm.put('9', sp.load(context, R.raw.s9, 0));
		hm.put('f',sp.load(context,R.raw.first,0));
		hm.put('s',sp.load(context,R.raw.ss,0));
		hm.put('l',sp.load(context,R.raw.last,0));
	}
}
