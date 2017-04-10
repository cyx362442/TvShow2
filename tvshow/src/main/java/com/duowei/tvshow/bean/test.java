package com.duowei.tvshow.bean;

import java.util.List;

/**
 * Created by Administrator on 2017-03-10.
 */

public class test {

    /**
     * zone_time : [{"zone":{"zone":"发发"},"one_data":[{"time":"15:50-16:00","ad":"","video_palce":"2","file_name":{"image_name":"1489040365201.png","video_name":"null"}},{"time":"15:50-16:05","ad":"发情啊围清风","video_palce":"5","file_name":{"image_name":"null","video_name":"null"}},{"time":"01:05-14:10","ad":"fqwrqwr","video_palce":"1","file_name":{"image_name":"null","video_name":"null"}}]}]
     * down_data : http://ai.wxdw.top/resource/attachment/light_box_manage/175/20/zip.zip
     * version : 12
     */

    private String down_data;
    private String version;
    private List<ZoneTimeBean> zone_time;

    public String getDown_data() {
        return down_data;
    }

    public void setDown_data(String down_data) {
        this.down_data = down_data;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<ZoneTimeBean> getZone_time() {
        return zone_time;
    }

    public void setZone_time(List<ZoneTimeBean> zone_time) {
        this.zone_time = zone_time;
    }

    public static class ZoneTimeBean {
        /**
         * zone : {"zone":"发发"}
         * one_data : [{"time":"15:50-16:00","ad":"","video_palce":"2","file_name":{"image_name":"1489040365201.png","video_name":"null"}},{"time":"15:50-16:05","ad":"发情啊围清风","video_palce":"5","file_name":{"image_name":"null","video_name":"null"}},{"time":"01:05-14:10","ad":"fqwrqwr","video_palce":"1","file_name":{"image_name":"null","video_name":"null"}}]
         */

        private ZoneBean zone;
        private List<OneDataBean> one_data;

        public ZoneBean getZone() {
            return zone;
        }

        public void setZone(ZoneBean zone) {
            this.zone = zone;
        }

        public List<OneDataBean> getOne_data() {
            return one_data;
        }

        public void setOne_data(List<OneDataBean> one_data) {
            this.one_data = one_data;
        }

        public static class ZoneBean {
            /**
             * zone : 发发
             */

            private String zone;

            public String getZone() {
                return zone;
            }

            public void setZone(String zone) {
                this.zone = zone;
            }
        }

        public static class OneDataBean {
            /**
             * time : 15:50-16:00
             * ad :
             * video_palce : 2
             * file_name : {"image_name":"1489040365201.png","video_name":"null"}
             */

            private String time;
            private String ad;
            private String video_palce;
            private FileNameBean file_name;

            public String getTime() {
                return time;
            }

            public void setTime(String time) {
                this.time = time;
            }

            public String getAd() {
                return ad;
            }

            public void setAd(String ad) {
                this.ad = ad;
            }

            public String getVideo_palce() {
                return video_palce;
            }

            public void setVideo_palce(String video_palce) {
                this.video_palce = video_palce;
            }

            public FileNameBean getFile_name() {
                return file_name;
            }

            public void setFile_name(FileNameBean file_name) {
                this.file_name = file_name;
            }

            public static class FileNameBean {
                /**
                 * image_name : 1489040365201.png
                 * video_name : null
                 */

                private String image_name;
                private String video_name;

                public String getImage_name() {
                    return image_name;
                }

                public void setImage_name(String image_name) {
                    this.image_name = image_name;
                }

                public String getVideo_name() {
                    return video_name;
                }

                public void setVideo_name(String video_name) {
                    this.video_name = video_name;
                }
            }
        }
    }
}
