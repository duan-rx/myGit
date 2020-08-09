package com.example.train.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.train.R;
import com.example.train.bluetooth.bluetoothController;
import com.example.train.bluetooth.bluetoothDeviceAdapterList;

import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends AppCompatActivity {
    private IntentFilter filterScan;
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private ListView lv_items;
    private bluetoothDeviceAdapterList bluetoothDeviceAdapterList;
    private bluetoothController bluetoothController=new bluetoothController();
    private Button bt_power,bt_isSupport,bt_open,bt_discovery,bt_scan,bt_closeScan,bt_close,bt_runTest;
    private TextView tv_showMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        setTitle("蓝牙检测");
        init();
        setListeners();


        registerBroader();
        registerReceiver(mRecevier,filterScan);
    }
    private void registerBroader(){
        //扫描设备时的广播
        filterScan = new IntentFilter();
        //开始查找
        filterScan.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束查找
        filterScan.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //查找设备
        filterScan.addAction(BluetoothDevice.ACTION_FOUND);
        //设备扫描模式改变
        filterScan.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //绑定状态
        filterScan.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    }
    private void init(){
        bt_power = (Button)findViewById(R.id.bt_power);
        bt_isSupport = (Button)findViewById(R.id.bt_isSupport);
        bt_open = (Button)findViewById(R.id.bt_open);
        bt_discovery = (Button)findViewById(R.id.bt_discovery);
        bt_scan = (Button)findViewById(R.id.bt_scan);
        bt_closeScan =(Button)findViewById(R.id.bt_closeScan);
        bt_close =(Button)findViewById(R.id.bt_close);
        bt_runTest=(Button)findViewById(R.id.bt_runTest);
        tv_showMessage=(TextView)findViewById(R.id.tv_showMessage);
        lv_items=(ListView)findViewById(R.id.lv_item);

    }
    private void setListeners(){
        OnClick onClick= new OnClick();
        bt_power.setOnClickListener(onClick);
        bt_open.setOnClickListener(onClick);
        bt_isSupport.setOnClickListener(onClick);
        bt_discovery.setOnClickListener(onClick);
        bt_scan.setOnClickListener(onClick);
        bt_closeScan.setOnClickListener(onClick);
        bt_close.setOnClickListener(onClick);
        bt_runTest.setOnClickListener(onClick);
    }
    private class OnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.bt_power:
                    checkPermissions();
                    break;
                case R.id.bt_isSupport:
                    if(bluetoothController.isSupportBluetooth()){
                        tv_showMessage.setText("设备支持蓝牙");
                    }
                    else{
                        tv_showMessage.setText("设备不支持蓝牙");
                    }
                    break;
                case R.id.bt_open:
                    if(bluetoothController.openBluetooth()){
                        tv_showMessage.setText("设备打开蓝牙");
                    }
                    else{
                        tv_showMessage.setText("设备不能打开蓝牙");
                    }
                    break;
                case R.id.bt_discovery:
                   bluetoothDiscovery();
                    break;
                case R.id.bt_scan:
                   scanBluetooth();
                    break;
                case R.id.bt_closeScan:
                    if(bluetoothController.closeScanBluetooth()){
                        tv_showMessage.setText("设备取消扫描");
                    }
                    else{
                        tv_showMessage.setText("设备不能正常取消扫描");
                    }
                    break;
                case R.id.bt_close:
                    if(bluetoothController.closeBluetooth()){
                        tv_showMessage.setText("设备关闭蓝牙");
                    }
                    else{
                        tv_showMessage.setText("设备不能关闭蓝牙");
                    }
                    break;
                case R.id.bt_runTest:
                    Intent intent=new Intent(BluetoothActivity.this,SetMateActivity.class);
                    startActivity(intent);
                default:
                    break;
            }
        }
   }
   private void bluetoothDiscovery(){
       if(bluetoothController.discoveryBluetooth(this)){
           tv_showMessage.setText("设备打开可发现");
       }
       else{
           tv_showMessage.setText("设备不能打开可发现");
       }
   }
   private void scanBluetooth(){
       if(bluetoothController.scanBluetooth(this)){
           tv_showMessage.setText("设备扫描蓝牙");
           bluetoothDeviceAdapterList = new bluetoothDeviceAdapterList(this);
           bluetoothDeviceAdapterList.bindData(mDeviceList);
           lv_items.setAdapter(bluetoothDeviceAdapterList);
       }
       else{
           tv_showMessage.setText("设备不能扫描");
       }
   }
    private BroadcastReceiver mRecevier= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                // setProgressBarIndeterminateVisibility(true);
                //初始化 数据列表
                mDeviceList.clear();
                bluetoothDeviceAdapterList.notifyDataSetChanged();
                tv_showMessage.setText("初始化 数据列表");
            }
            else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                // setProgressBarIndeterminateVisibility(false);;
                tv_showMessage.setText("查找完成");
                int i =  bluetoothDeviceAdapterList.getCount()-1;
                BluetoothDevice device2 = (BluetoothDevice)  bluetoothDeviceAdapterList.getItem(i);
              /*  TextView tv_1=(TextView)findViewById(R.id.tv_bluetoothAddress);
                tv_1.setText(device2.getAddress());
                TextView tv_2=(TextView)findViewById(R.id.tv_bluetoothName);
                tv_2.setText(device2.getName() + i);
*/


            }
            else if( BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //找到一个，添加一个
                tv_showMessage.setText("查找设备名 ：" + device.getName());
                mDeviceList.add(device);
                bluetoothDeviceAdapterList.notifyDataSetChanged();

            }
            else if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)){
                int scanMde = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,0);
                if( scanMde == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    tv_showMessage.setText("查找设备开始");
                }
                else{
                    tv_showMessage.setText("查找设备错误");
                }
            }
            else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if( device == null){
                    tv_showMessage.setText("no device");
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,0);
                if( status == BluetoothDevice.BOND_BONDED){
                    tv_showMessage.setText("Bonded " + device.getName());
                }
                else if (status == BluetoothDevice.BOND_BONDING){
                    tv_showMessage.setText("Bonding" + device.getName());
                }
                else if ( status == BluetoothDevice.BOND_NONE){
                    tv_showMessage.setText("Not bond " + device.getName());
                }
            }
            else tv_showMessage.setText("扫描出现意外情况");
        }
    };
    /**
     * 检查权限
     */
    private void checkPermissions() {

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            // ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
            ActivityCompat.requestPermissions(this, deniedPermissions, 1);

        }
    }

    /**
     * 权限回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 String[] permissions,
                                                 int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // case REQUEST_CODE_PERMISSION_LOCATION:
            case 1:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;

        }
    }


    /**
     * 开启GPS
     * @param permission
     */
    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("当前手机扫描蓝牙需要打开定位功能。")
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton("前往设置",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, 1);
                                            //startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    //GPS已经开启了
                }
                break;
        }
    }


    /**
     * 检查GPS是否打开
     * @return
     */
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }
}
