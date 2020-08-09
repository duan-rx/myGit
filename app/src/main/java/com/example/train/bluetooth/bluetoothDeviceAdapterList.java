package com.example.train.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.train.R;

import java.util.List;

public class bluetoothDeviceAdapterList extends BaseAdapter {
    private List<BluetoothDevice> mData = null;
    private Context context;
    private LayoutInflater myLayoutInflater;
    public bluetoothDeviceAdapterList(Context context){
        context = context;
        myLayoutInflater=LayoutInflater.from(context);
    }
    public bluetoothDeviceAdapterList (Context context,List<BluetoothDevice> data){
        mData = data;
        context = context;
        myLayoutInflater=LayoutInflater.from(context);
    }
    public void bindData(List<BluetoothDevice> data){
        mData =data;
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
        public TextView tv_name,tv_address;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder = null;
        BluetoothDevice device = null;
        //   if (convertView == null)
        //     convertView = LayoutInflater.from(context).inflate(R.layout.activity_blue_tooth2_item, null);
        //TextView tv_bluetoothName = (TextView) convertView.findViewById(R.id.tv_name);
        //TextView tv_bluetoothAdress = (TextView) convertView.findViewById(R.id.tv_address);

        //tv_bluetoothAdress.setText(device.getAddress());
        //tv_bluetoothName.setText(device.getName());

        if(convertView == null) {
            convertView =myLayoutInflater.inflate(R.layout.activity_bluetooth_to_list,null);
            holder = new viewHolder();
            holder.tv_address = (TextView)convertView.findViewById(R.id.tv_address);
            holder.tv_name = (TextView)convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        }
        else {
            holder =(viewHolder) convertView.getTag();
        }
        if(mData != null) {
            device = (BluetoothDevice) mData.get(position);
            if(device.getName()==null){
                holder.tv_name.setText("设备名称无:NULL");
            }
            else holder.tv_name.setText(device.getName().toString());
            if(device.getAddress()==null){
                 holder.tv_address.setText("设备地址无：NULL");
            }else holder.tv_address.setText(device.getAddress().toString());
        }
        else {
            holder.tv_name.setText("正在查找名称");
            holder.tv_address.setText("正在查找地址");
        }
        return convertView;
    }
}
