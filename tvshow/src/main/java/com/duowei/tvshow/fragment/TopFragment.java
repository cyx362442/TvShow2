package com.duowei.tvshow.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.duowei.tvshow.R;
import com.duowei.tvshow.bean.CityCode;
import com.duowei.tvshow.bean.CityCodes;
import com.duowei.tvshow.bean.Weather;
import com.duowei.tvshow.httputils.DownHTTP;
import com.duowei.tvshow.httputils.VolleyResultListener;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopFragment extends Fragment {
    private Handler mHandler;
    private Runnable runnable;
    private String mCode;
    private TextView mTv;
    private TextView mTvDatetime;
    private TextView mTvTime;
    private TextView mTvTempter;
    private TextView mTvWeather;
    private ImageView mImgWeather;

    @Override
    public void onAttach(Context context) {
        List<CityCodes> city = DataSupport.where("name=?", "厦门").find(CityCodes.class);
        mCode = city.get(0).code;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_top, container, false);
        mTvDatetime = (TextView) inflate.findViewById(R.id.tv_dateTime);
        mTvTime = (TextView) inflate.findViewById(R.id.tv_time);
        mTvTempter = (TextView) inflate.findViewById(R.id.tv_tempterature);
        mTvWeather = (TextView) inflate.findViewById(R.id.tv_weather);
        mImgWeather = (ImageView) inflate.findViewById(R.id.img_weather);
        return inflate;
    }

    @Override
    public void onStart() {
        DownHTTP.getVolley("http://www.weather.com.cn/data/cityinfo/"+mCode+".html", new VolleyResultListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                Weather weather = gson.fromJson(response, Weather.class);
                Weather.WeatherinfoBean weatherinfo = weather.getWeatherinfo();
                mTvTempter.setText(weatherinfo.getTemp1()+"~"+weatherinfo.getTemp2());
                mTvWeather.setText(weatherinfo.getWeather());
            }
        });
        super.onStart();
    }

    @Override
    public void onResume() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd EEEE");
        Date curDate = new Date(System.currentTimeMillis());
        String time = dateFormat.format(curDate);
        mTvDatetime.setText(time);
        final SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
        mHandler = new Handler();
        mHandler.postDelayed(runnable=new Runnable() {
            @Override
            public void run() {
                Date curDate = new Date(System.currentTimeMillis());
                String time = dateFormat2.format(curDate);
                mTvTime.setText(time);
                mHandler.postDelayed(this,1000);
            }
        },0);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if(mHandler!=null){
            mHandler.removeCallbacks(runnable);
        }
        super.onDestroy();
    }
}
