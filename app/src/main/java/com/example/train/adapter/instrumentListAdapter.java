package com.example.train.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.train.R;
import com.example.train.constants.message;

import java.util.List;

public class instrumentListAdapter extends BaseAdapter {
    private List<List> mData = null;
    private Context context;
    private LayoutInflater myLayoutInflater;
    public instrumentListAdapter(Context context){
        context= context;
        myLayoutInflater=LayoutInflater.from(context);
    }
    public instrumentListAdapter(Context context,List<List> data){
        context= context;
        mData=data;
        myLayoutInflater=LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        if(mData==null || mData.size() <=0 )
            return 0;
        else return mData.size() ;
    }

    @Override
    public Object getItem(int position) {
            return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    class viewHolder{
        public TextView tv_instrumentCode,tv_instrumentName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder=new viewHolder();
        List instrument=null;
        if(convertView == null) {
            convertView =myLayoutInflater.inflate(R.layout.activity_bluetooth_to_list,null);
            holder = new viewHolder();
            holder.tv_instrumentCode= (TextView)convertView.findViewById(R.id.tv_name);
            holder.tv_instrumentName= (TextView)convertView.findViewById(R.id.tv_address);
            convertView.setTag(holder);
        }
        else {
            holder =(viewHolder) convertView.getTag();
        }
        if(mData != null) {
            instrument=(List)mData.get(position);
            holder.tv_instrumentCode.setText(message.message+"："+instrument.get(0).toString());
            holder.tv_instrumentName.setText(message.message2+"："+instrument.get(1).toString());
        }
        else {
            holder.tv_instrumentCode.setText("无设备");
        }
        return convertView;
    }
    public void bindData(List<List> data){
        mData =data;
    }
}
