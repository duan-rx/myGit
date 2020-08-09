package com.example.train.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.train.R;

import java.util.List;

public class histroyListAdapter extends BaseAdapter {
    private List<List<String>> mData = null;
    private Context context;
    private LayoutInflater myLayoutInflater;
    public histroyListAdapter(Context context){
        context= context;
        myLayoutInflater=LayoutInflater.from(context);
    }
    public histroyListAdapter(Context context,List<List<String>> data){
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
        public TextView histroy,histroy2,histroy3;
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
            convertView =myLayoutInflater.inflate(R.layout.history_adapter,null);
            holder =new viewHolder();
            holder.histroy= (TextView)convertView.findViewById(R.id.tv_histroy);
            holder.histroy2= (TextView)convertView.findViewById(R.id.tv_histroy2);
            holder.histroy3= (TextView)convertView.findViewById(R.id.tv_histroy3);
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
            holder.histroy.setText(instrument.get(0).toString());
            holder.histroy.getPaint().setFakeBoldText(true);

            holder.histroy2.setText(instrument.get(1).toString());
            switch (instrument.get(2).toString()){
                case "偏高":
                    holder.histroy3.setTextColor(Color.parseColor("#FF0000"));
                    break;
                case "偏低":
                    holder.histroy3.setTextColor(Color.parseColor("#0000FF"));
                    break;
                case "错误":
                    holder.histroy3.setTextColor(Color.parseColor("#8B0000"));
                    break;
                case "异常":
                    holder.histroy3.setTextColor(Color.parseColor("#FF4500"));
                    break;
                case "正确":
                    holder.histroy3.setTextColor(Color.parseColor("#000000"));
                    break;
                    default:
                        break;
            }
            holder.histroy3.setText(instrument.get(2).toString());

        }
        else {

        }
        return convertView;
    }
    public void bindData(List<List<String>> data){
         mData =data;

    }
}
