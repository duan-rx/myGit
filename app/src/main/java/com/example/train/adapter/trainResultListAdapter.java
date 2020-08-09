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

public class trainResultListAdapter extends BaseAdapter {
    private List<List<String>> mData = null;
    private Context context;
    private LayoutInflater myLayoutInflater;
    public trainResultListAdapter(Context context){
        context= context;
        myLayoutInflater=LayoutInflater.from(context);
    }
    public trainResultListAdapter(Context context,List<List<String>> data){
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
        public TextView itemName,itemResult,itemUnit,itemReference,itemResultItemJudge;
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
            convertView =myLayoutInflater.inflate(R.layout.train_result_adapter,null);
            holder =new viewHolder();
            holder.itemName= (TextView)convertView.findViewById(R.id.tv_trainResultItemName);
            holder.itemResult= (TextView)convertView.findViewById(R.id.tv_trainResultItemResult);
            holder.itemUnit= (TextView)convertView.findViewById(R.id.tv_trainResultItemUnit);
            holder.itemReference= (TextView)convertView.findViewById(R.id.tv_trainResultItemReference);
            holder.itemResultItemJudge= (TextView)convertView.findViewById(R.id.tv_trainResultItemJudge);
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
            instrument=(List)mData.get(position);
            holder.itemName.setText(instrument.get(0).toString());//项目
            holder.itemResult.getPaint().setFakeBoldText(true);

            if("".equals(instrument.get(2).toString())){
                holder.itemUnit.setText("无");//项目单位
            }
           else  holder.itemUnit.setText(instrument.get(2).toString());//项目单位

            if("".equals(instrument.get(3).toString())){
                holder.itemReference.setText(instrument.get(3).toString());//项目参考
            }
            else  holder.itemReference.setText(instrument.get(3).toString());//项目参考
            switch (instrument.get(4).toString()){
                case "偏高":
                    holder.itemResult.setTextColor(Color.parseColor("#FF0000"));
                    holder.itemResultItemJudge.setTextColor(Color.parseColor("#FF0000"));
                    break;
                case "偏低":
                    holder.itemResult.setTextColor(Color.parseColor("#0000FF"));
                    holder.itemResultItemJudge.setTextColor(Color.parseColor("#0000FF"));
                    break;
                case "错误":
                    holder.itemResult.setTextColor(Color.parseColor("#8B0000"));
                    holder.itemResultItemJudge.setTextColor(Color.parseColor("#8B0000"));
                    break;
                case "异常":
                    holder.itemResult.setTextColor(Color.parseColor("#FF4500"));
                    holder.itemResultItemJudge.setTextColor(Color.parseColor("#FF4500"));
                    break;
                case "正确":
                    holder.itemResult.setTextColor(Color.parseColor("#000000"));
                    holder.itemResultItemJudge.setTextColor(Color.parseColor("#000000"));
                    break;
                default:
                    break;
            }
            holder.itemResult.setText(instrument.get(1).toString());//结果
            holder.itemResultItemJudge.setText(instrument.get(4).toString());//结果判断

        }
        else {

        }
        return convertView;
    }
    public void bindData(List<List<String>> data){
        if(mData!=null&&mData.size()>0){
            mData.clear();
        }
        mData =data;

    }
}
