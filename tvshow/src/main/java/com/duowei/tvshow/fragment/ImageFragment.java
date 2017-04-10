package com.duowei.tvshow.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.duowei.tvshow.R;
import com.duowei.tvshow.contact.FileDir;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment {
    private ViewPager mViewPager;
    private BannerAdapter bannerAdapter;
    private Timer mTimer = new Timer();
    private TextView mTitle;
    private int mBannerPosition = 0;
    private final int DEFAULT_BANNER_SIZE = 5;
    private boolean mIsUserTouched = false;

    private ArrayList<String> mListPath;
    private Handler mHandler;
    private Runnable mRunnable;
    private File mDir;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_image, container, false);

        mListPath= FileDir.getImgPath();
        mDir = Environment.getExternalStorageDirectory();

        mViewPager = (ViewPager) inflate.findViewById(R.id.view_pager);
        mTitle = (TextView) inflate.findViewById(R.id.title);
        bannerAdapter = new BannerAdapter(getActivity(),mListPath);
        mViewPager.setAdapter(bannerAdapter);
        mHandler = new Handler();
        return inflate;
    }

    @Override
    public void onStart() {
        mHandler.postDelayed(mRunnable=new Runnable() {
            @Override
            public void run() {
                if (!mIsUserTouched){
                    mBannerPosition = (mBannerPosition + 1) % mListPath.size();
                    /**
                     * Android在子线程更新UI的几种方法
                     * Handler，AsyncTask,view.post,runOnUiThread
                     */
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mBannerPosition == mListPath.size() - 1){
                                mViewPager.setCurrentItem(DEFAULT_BANNER_SIZE - 1,false);
                            }else {
                                mViewPager.setCurrentItem(mBannerPosition);
                            }
                        }
                    });
                }
                mHandler.postDelayed(mRunnable,8000);
            }
        },1000);
        super.onStart();
    }

    File imgPath;
    private class BannerAdapter extends PagerAdapter {
        private Context context;
        private ArrayList<String> newsList;
        public BannerAdapter(Context context, ArrayList<String> newsList) {
            this.context = context;
            this.newsList = newsList;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position %= newsList.size();

            View view = LayoutInflater.from(context).inflate(R.layout.item_banner, container, false);
            ImageView image = (ImageView) view.findViewById(R.id.image);
            TextView tvPosition=(TextView)view.findViewById(R.id.textView1);
//            imgPath = new File(mDir + "/Dw/image/" + newsList.get(position));
            imgPath=new File(newsList.get(position));
            Picasso.with(getContext())
                    .load(imgPath)
                    .placeholder(R.mipmap.bg)
                    .error(R.mipmap.bg)
                    .fit()
                    .centerInside()
                    .into(image);
//            Glide.with(getContext()).load(imgPath).placeholder(R.mipmap.bg).into(image);
            tvPosition.setText((position+1)+"/"+newsList.size());
            container.addView(view);
            return view;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
        @Override
        public int getCount() {
            return newsList.size();
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        @Override
        public void finishUpdate(ViewGroup container) {
            int position = mViewPager.getCurrentItem();
            if (position == 0){
                position = DEFAULT_BANNER_SIZE;
                mViewPager.setCurrentItem(position,false);
            }else if (position == newsList.size() - 1){
                position = DEFAULT_BANNER_SIZE - 1;
                mViewPager.setCurrentItem(position,false);
            }
        }
    }
    @Override
    public void onDestroy() {
        if(mHandler!=null){
            mHandler.removeCallbacks(mRunnable);
        }
        super.onDestroy();
    }
}
