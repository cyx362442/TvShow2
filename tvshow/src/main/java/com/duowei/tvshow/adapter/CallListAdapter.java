package com.duowei.tvshow.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.duowei.tvshow.R;
import com.duowei.tvshow.bean.KDSCall;

import java.util.List;

/**
 * Created by Administrator on 2017-04-26.
 */

public class CallListAdapter extends BaseAdapter {
    Context context;
   List<KDSCall>listCall;
    private final SharedPreferences mPreferences;

    public CallListAdapter(Context context, List<KDSCall>listCall) {
        this.context = context;
        this.listCall = listCall;
        mPreferences = context.getSharedPreferences("Users", Context.MODE_PRIVATE);
    }

    @Override
    public int getCount() {
        return listCall.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.list_item, null);
        TextView text = (TextView) inflate.findViewById(R.id.tv_show);
        //呼叫中
        if(listCall.get(i).getYhj().equals("2")){
            text.setText(listCall.get(i).getTableno());
//            text.setTextSize(context.getResources().getDimension(R.dimen.calltext));
            text.setTextSize(Float.parseFloat(mPreferences.getString("calltext","30")));
            text.setTextColor(context.getResources().getColor(R.color.call_red));
        }else if(listCall.get(i).getYhj().equals("0")){//等待中
            text.setText(listCall.get(i).getTableno());
            text.setTextSize(Float.parseFloat(mPreferences.getString("waittext","20")));
//            text.setTextSize(context.getResources().getDimension(R.dimen.waittext));
            text.setTextColor(context.getResources().getColor(R.color.white));
        }
        return inflate;
    }
}
