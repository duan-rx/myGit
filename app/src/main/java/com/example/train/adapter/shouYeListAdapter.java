package com.example.train.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.train.R;

import java.util.List;

public class shouYeListAdapter extends BaseAdapter {
    private List<String> mData = null;
    private Context context;
    private LayoutInflater myLayoutInflater;
    public shouYeListAdapter(Context context){
        context= context;
        myLayoutInflater=LayoutInflater.from(context);
    }
    public shouYeListAdapter(Context context,List<String> data){
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
        public TextView shouYeMsg;
    }
    private static int number=0;
    public View getView(){
        return myLayoutInflater.inflate(R.layout.shou_ye_adapter,null);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder=new viewHolder();
        List instrument=null;
        if(convertView == null) {
            convertView =myLayoutInflater.inflate(R.layout.shou_ye_adapter,null);
            holder =new viewHolder();
            holder.shouYeMsg= (TextView)convertView.findViewById(R.id.tv_shouYeMsg);
            convertView.setTag(holder);
        }
        else {
            holder =(viewHolder) convertView.getTag();
        }

        if(position%2==0){
            convertView.setBackgroundColor(Color.parseColor("#F8F8F8"));
        }
        else convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));

        if(mData.get(position)!=null) {
            holder.shouYeMsg.setText(mData.get(position));

        }
        else {
            holder.shouYeMsg.setText("");
        }
        return convertView;
    }
    public void bindData(List<String> data){
        mData =data;

    }
}
