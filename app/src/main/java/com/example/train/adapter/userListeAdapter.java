package com.example.train.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.train.R;
import com.example.train.constants.message;

import java.util.List;

public class userListeAdapter extends BaseAdapter {
    private List<List<String>> mData = null;
    private Context context;
    private LayoutInflater myLayoutInflater;
    public userListeAdapter(Context context){
        context= context;
        myLayoutInflater=LayoutInflater.from(context);
    }
    public userListeAdapter(Context context,List<List<String>> data){
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
        public TextView userId,userName,userPhone,userStatus,userPower;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder=new viewHolder();
        List instrument=null;
        if(convertView == null) {
            convertView =myLayoutInflater.inflate(R.layout.user_adapter,null);
            holder = new viewHolder();
            holder.userId= (TextView)convertView.findViewById(R.id.userId);
            holder.userName= (TextView)convertView.findViewById(R.id.userName);
            holder.userPower= (TextView)convertView.findViewById(R.id.userPower);
            holder.userPhone= (TextView)convertView.findViewById(R.id.userPhone);
            holder.userStatus= (TextView)convertView.findViewById(R.id.userStatus);
            convertView.setTag(holder);
        }
        else {
            holder =(viewHolder) convertView.getTag();
        }
        if(mData != null) {
            instrument=(List)mData.get(position);
            holder.userId.setText(instrument.get(0).toString());
            holder.userName.getPaint().setFakeBoldText(true);
            holder.userName.setText(instrument.get(1).toString());
            holder.userPhone.setText(instrument.get(2).toString());
            switch (instrument.get(3).toString()){
                case "0":
                    holder.userStatus.setText("用户已停用");
                    holder.userStatus.setTextColor(Color.parseColor("#90EE90"));
                    break;
                case "1":
                    holder.userStatus.setText("正在使用");
                    holder.userStatus.setTextColor(Color.parseColor("#000000"));
                    break;
                case "2":
                    holder.userStatus.setText("正在申请管理员");
                    holder.userStatus.setTextColor(Color.parseColor("#FF0000"));
                    break;
                case "3":
                    holder.userStatus.setText("允许修改密码");
                    holder.userStatus.setTextColor(Color.parseColor("#0000FF"));
                    break;
                default:
                    holder.userStatus.setText("状态");
                    break;
            }
            switch(instrument.get(4).toString()){
                case "1":
                    holder.userPower.setText("管理员");
                    holder.userPower.getPaint().setFakeBoldText(true);
                    break;
                case "0":
                    holder.userPower.setText("普通用户");

                    break;
                default:
                    holder.userPower.setText("角色");
                    break;
            }

        }
        else {
            holder.userId.setText("无用户");
        }
        return convertView;
    }
    public void bindData(List<List<String>> data){
        mData =data;
    }
}
