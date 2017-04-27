package com.duowei.tvshow.fragment;


import android.app.Fragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

    private ListView mLvWait;
    private ListView mLvCall;
    private List<KDSCall> listWait=new ArrayList<>();
    private List<KDSCall> listCall=new ArrayList<>();
    private CallListAdapter mWaitAdapter;
    private CallListAdapter mCallAdatper;

    public CallFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_call, container, false);
        mLvWait = (ListView) inflate.findViewById(R.id.lv_wait);
        mLvCall = (ListView) inflate.findViewById(R.id.lv_call);
        mWaitAdapter = new CallListAdapter(getActivity(), listWait);
        mCallAdatper = new CallListAdapter(getActivity(), listCall);
        mLvWait.setAdapter(mWaitAdapter);
        mLvCall.setAdapter(mCallAdatper);
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
}
