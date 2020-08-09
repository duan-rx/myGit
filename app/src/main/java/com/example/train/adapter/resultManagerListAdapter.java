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

public class resultManagerListAdapter extends BaseAdapter {
    private List<List<String>> mData = null;
    private Context context;
    private LayoutInflater myLayoutInflater;
    public resultManagerListAdapter(Context context){
        context= context;
        myLayoutInflater=LayoutInflater.from(context);
    }
    public resultManagerListAdapter(Context context,List<List<String>> data){
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
        public TextView userName,number,titile,ischeck,commitTime;
    }
    private static int number=0;
    public View getView(){
        return myLayoutInflater.inflate(R.layout.history_adapter,null);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder=new viewHolder();
        List instrument=null;
        if(convertView == null) {
            convertView =myLayoutInflater.inflate(R.layout.result_manager,null);
            holder =new viewHolder();
            holder.userName= (TextView)convertView.findViewById(R.id.result_userName);
            holder.number= (TextView)convertView.findViewById(R.id.result_number);
            holder.titile= (TextView)convertView.findViewById(R.id.result_title);
            holder.ischeck= (TextView)convertView.findViewById(R.id.result_isCheck);
            holder.commitTime= (TextView)convertView.findViewById(R.id.result_commitTime);
            convertView.setTag(holder);
        }
        else {
            holder =(viewHolder) convertView.getTag();
        }
        if(position%2==0){
            convertView.setBackgroundColor(Color.parseColor("#F8F8F8"));
        }
        else convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));

        if(mData != null) {
            instrument=(List)mData.get(position);
            holder.userName.setText(instrument.get(0).toString());
            holder.userName.getPaint().setFakeBoldText(true);
            holder.userName.setTextColor(Color.parseColor("#0000FF"));
            holder.number.setText(instrument.get(1).toString());
            holder.number.getPaint().setFakeBoldText(true);
            holder.titile.setText(instrument.get(2).toString());

            switch (instrument.get(3).toString()){
                case "1":
                   // holder.ischeck.setTextColor(Color.parseColor("#FF0000"));
                    holder.ischeck.setText("已检查");
                    break;
                case "0":
                   // holder.ischeck.setTextColor(Color.parseColor("#0000FF"));
                    holder.ischeck.setTextColor(Color.parseColor("#FF0000"));
                    holder.ischeck.setText("未检查");
                    break;
                default:
                    holder.ischeck.setText(instrument.get(3).toString());
                    break;
            }
            holder.commitTime.setText(instrument.get(4).toString());

        }
        else {
            holder.titile.setText("无数据");
        }
        return convertView;
    }
    public void bindData(List<List<String>> data){
        mData =data;
    }
}
