package com.duowei.tvshow.helper;


public class VersionUpdate {

    /**
     * 请求服务器，检查版本是否可以更新
     *
     * @param versionUpdate
     */
     public static void checkVersion(final VersionUpdateImpl versionUpdate,String url) {
         //从网络请求获取到下载路径，此处是随便找的链接
         versionUpdate.bindService(url);
     }
}
