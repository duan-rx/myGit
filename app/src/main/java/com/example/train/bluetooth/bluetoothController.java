package com.example.train.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;


import java.util.ArrayList;
import java.util.List;



public class bluetoothController {
     private BluetoothAdapter bluetoothAdapter;
     public bluetoothController(){bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();}

     //是否支持蓝牙
    public boolean isSupportBluetooth(){
         if(bluetoothAdapter!= null)
             return true;
         else return false;
    }

    //获取蓝牙
    public BluetoothAdapter getBluetoothAdapter(){
         return bluetoothAdapter;
    }

    //打开 蓝牙设备
        //异步
    public boolean openBluetooth(){
        if(isSupportBluetooth()){
            bluetoothAdapter.enable();
            return true;
        }
        else return false;
    }
        //同步
    public void openBluetooth(Activity activity,int requestCode){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent,requestCode);
    }

    //关闭蓝牙
    public boolean closeBluetooth(){
        try{
            bluetoothAdapter.disable();
            return true;
        }catch (Exception e){
            return false;
        }

    }

    //设备可见
    public boolean discoveryBluetooth(Context context){
         try {
             Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
             intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
             context.startActivity(intent);
             return true;
         }
         catch (Exception e){
             return false;
         }

    }

    //判断蓝牙状态
    public boolean getBluetoothStatus(){
         return  isSupportBluetooth()&&bluetoothAdapter.enable();
     }

     //扫描设备
    public boolean scanBluetooth(Context context){
         if(!getBluetoothStatus()) return false;//蓝牙没有打开
         //discoveryBluetooth(context);
         if(bluetoothAdapter.isDiscovering()){
             bluetoothAdapter.cancelDiscovery();
         }
         return bluetoothAdapter.startDiscovery();
    }

    //取消扫描
    public boolean closeScanBluetooth(){
         if(isSupportBluetooth())return bluetoothAdapter.cancelDiscovery();
         return false;
    }

    //查找设备
    public void findDeviceBlueTooth(){
         assert (bluetoothAdapter!=null);
         bluetoothAdapter.startDiscovery();
    }

    //获取绑定设备
    public List<BluetoothDevice> getBondDeviceList(){
         return new ArrayList<>(bluetoothAdapter.getBondedDevices());
    }


}

