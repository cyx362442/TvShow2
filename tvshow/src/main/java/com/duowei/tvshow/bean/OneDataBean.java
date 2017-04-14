package com.duowei.tvshow.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017-03-09.
 */

public class OneDataBean extends DataSupport {
    /**
     * time : 15:50-16:00
     * ad :
     * video_palce : 2
     */
    public String time;//时间段
    public String ad;//动态广告词
    public String video_palce;//视频空间位置
    public String image_name;//图片名称
    public String video_name;//视频名称
    public String color;

    public OneDataBean(String time, String ad, String video_palce, String image_name, String video_name,String color) {
        this.time = time;
        this.ad = ad;
        this.video_palce = video_palce;
        this.image_name = image_name;
        this.video_name = video_name;
        this.color=color;
    }
}
