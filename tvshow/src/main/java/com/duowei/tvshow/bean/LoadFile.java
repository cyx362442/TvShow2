package com.duowei.tvshow.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017-04-17.
 */

public class LoadFile implements Serializable{
   public String url;
    public String fileName;

    public LoadFile(String url, String fileName) {
        this.url = url;
        this.fileName = fileName;
    }
}
