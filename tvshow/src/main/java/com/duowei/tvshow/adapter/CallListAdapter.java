package com.duowei.tvshow.adapter;

import android.content.Context;
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

    public CallListAdapter(Context context, List<KDSCall>listCall) {
        this.context = context;
        this.listCall = listCall;
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
        text.setText(listCall.get(i).getTableno()+"号");
        if(listCall.get(i).getYhj().equals("1")){
            text.setTextColor(Color.WHITE);
        }else{
            text.setTextColor(context.getResources().getColor(R.color.call_yellow));
        }
        return inflate;
    }
}
