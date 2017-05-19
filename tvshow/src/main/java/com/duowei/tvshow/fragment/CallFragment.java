package com.duowei.tvshow.fragment;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.duowei.tvshow.R;
import com.duowei.tvshow.adapter.CallListAdapter;
import com.duowei.tvshow.bean.KDSCall;
import com.duowei.tvshow.event.BrushCall;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CallFragment extends Fragment {

    private GridView mGvWait;
    private ListView mLvCall;
    private List<KDSCall> listWait=new ArrayList<>();
    private List<KDSCall> listCall=new ArrayList<>();
    private CallListAdapter mWaitAdapter;
    private CallListAdapter mCallAdatper;
    private TextView mTvServer;

    public CallFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_call, container, false);

        mTvServer = (TextView) inflate.findViewById(R.id.tv_server);
        mGvWait = (GridView) inflate.findViewById(R.id.gv_wait);
        mLvCall = (ListView) inflate.findViewById(R.id.lv_call);
        mWaitAdapter = new CallListAdapter(getActivity(), listWait);
        mGvWait.setAdapter(mWaitAdapter);

//        mWaitAdapter = new CallListAdapter(getActivity(), listWait);
        mCallAdatper = new CallListAdapter(getActivity(), listCall);
        mLvCall.setAdapter(mCallAdatper);

        setViewWeight(inflate);
        return inflate;
    }
    public void setListWait(BrushCall event){
        listCall.clear();
        listWait.clear();
        KDSCall[] arrayCall = event.arrayCall;
        for(int i=0;i<arrayCall.length;i++){
            KDSCall kdsCall = arrayCall[i];
            if(kdsCall.getYhj().equals("0")){
                listWait.add(kdsCall);
            }else if(kdsCall.getYhj().equals("2")){
                listCall.add(kdsCall);
            }
        }
        mWaitAdapter.notifyDataSetChanged();
        mCallAdatper.notifyDataSetChanged();
    }

    /**设置屏占比*/
    private void setViewWeight(View inflate) {
        SharedPreferences preferences = getActivity().getSharedPreferences("Users", Context.MODE_PRIVATE);
        String viewWeight = preferences.getString("view_weight", "1:2");
        View callView = inflate.findViewById(R.id.ll_coming);
        View image = inflate.findViewById(R.id.ll_server);
        LinearLayout.LayoutParams paramsWeight = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams paramsWeight2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        if(viewWeight.equals("2:1")){
            paramsWeight.weight = 2;
            paramsWeight2.weight=1;
            mGvWait.setNumColumns(2);
            mTvServer.setSingleLine();
        }else{
            paramsWeight.weight = 1;
            paramsWeight2.weight=1;
            mGvWait.setNumColumns(1);
        }
        callView.setLayoutParams(paramsWeight2);
        image.setLayoutParams(paramsWeight);
    }
}
