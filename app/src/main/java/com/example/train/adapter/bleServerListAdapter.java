package com.example.train.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.train.R;

import java.util.List;

public class bleServerListAdapter extends BaseAdapter {
    private List<List<String>> mData = null;
    private Context context;
    private LayoutInflater myLayoutInflater;
    public bleServerListAdapter(Context context){
        context= context;
        myLayoutInflater=LayoutInflater.from(context);
    }
    public bleServerListAdapter(Context context,List<List<String>> data){
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
        public TextView tv_uuid,tv_content;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder=new viewHolder();
        List<String> bleServer=null;
        if(convertView == null) {
            convertView =myLayoutInflater.inflate(R.layout.activity_bluetooth_to_list,null);
            holder = new viewHolder();
            holder.tv_uuid= (TextView)convertView.findViewById(R.id.tv_name);
            holder.tv_content= (TextView)convertView.findViewById(R.id.tv_address);
            convertView.setTag(holder);
        }
        else {
            holder =(viewHolder) convertView.getTag();
        }
        if(mData != null) {
            bleServer=(List)mData.get(position);
            holder.tv_uuid.setText("UUUID:"+bleServer.get(0).toString());
            holder.tv_content.setText("内容："+bleServer.get(1).toString());
        }
        else {
            holder.tv_uuid.setText("无服务");
            holder.tv_content.setText("");
        }
        return convertView;
    }
    public void bindData(List<List<String >> data){
        mData =data;
    }
}