package com.duowei.tvshow.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017-03-11.
 */

public class CurrentTime {
    public static int getTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
        Date curDate = new Date(System.currentTimeMillis());
        String format = dateFormat.format(curDate);
        return Integer.parseInt(format);
    }
}
