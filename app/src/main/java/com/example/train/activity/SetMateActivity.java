package com.example.train.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.train.R;
import com.example.train.adapter.bleServerListAdapter;
import com.example.train.bluetooth.bluetoothDeviceAdapterList;
import com.example.train.database.databaseDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_AUTO;
import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;


public class SetMateActivity extends AppCompatActivity {
    private Button bt_setMateScanDevice,bt_setMateConnect,bt_setMateAround,bt_setMateAroundStop,bt_setMateSendMsg,bt_setMateReadMsg
            ,bt_setMateClearAll,bt_setMateSaveMsg,bt_setMateSaveUUID;
    private EditText et_setMateSendMsg;
    private TextView tv_setMateShowGetMsg,tv_setMateServerUUID,tv_setMateCharUUId;
    private ListView lv_setMateList,lv_setMateServerList,lv_setMateCharList;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> bluetoothDevicesList=new ArrayList<>();
    private    bluetoothDeviceAdapterList bluetoothDeviceAdapter;
    private BluetoothDevice connectDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothLeScanner scanner;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private List<List<String>> bleServerList=new ArrayList<>();
    private List<List<String>> bleCharList = new ArrayList<>();
    private bleServerListAdapter bleServerListAdapter;
    private bleServerListAdapter bleCharListAdapter;
    private UUID ServerUUID,CharUUID;
    private databaseDao db;


    private  SampleAdvertiseCallback mAdertiseCallback;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothGattCharacteristic characteristicRead;
    //在该方法中停止扫描，将扫描到的设备清0
    @Override
    protected void onPause(){
        super.onPause();
        if (leCallback != null) {
          //  scanner.stopScan(leCallback);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null) {
            scanner.stopScan(leCallback);
        }
        bluetoothDevicesList.clear();
        if(bluetoothGatt!=null){
            bluetoothGatt.close();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_mate);

        if (Build.VERSION.SDK_INT >= 26)
        {
            checkPermissions();
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
        mBluetoothManager=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter=mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }


        //mBluetoothAdapter.startLeScan(leScanCallback);
        scanner=mBluetoothAdapter.getBluetoothLeScanner();
        db=new databaseDao(this);
        init();
        setOnClick();
        String sqll="select serverUUId,charUUID from bleUUID";
        Cursor cursor1=db.query(sqll);
        if(cursor1.getCount()<=0) {
            db.insert("insert into bleUUId(serverUUID,charUUID) values('0000ffe0-0000-1000-8000-00805f9b34fb','0000ffe1-0000-1000-8000-00805f9b34fb');");
            cursor1.close();
            cursor1=db.query(sqll);
        }
       while (cursor1.moveToNext()){
            if(cursor1.isLast()){
                int idex=cursor1.getColumnIndex("serverUUID");
                tv_setMateServerUUID.setHint("默认服务UUID:"+cursor1.getString(idex));
                UUID_SERVICE=UUID.fromString(cursor1.getString(idex));
                idex=cursor1.getColumnIndex("charUUID");
                tv_setMateCharUUId.setHint("默认特征UUID:"+cursor1.getString(idex));
                UUID_NOTIFY=UUID.fromString(cursor1.getString(idex));
            }
        }
        cursor1.close();
        lv_setMateList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connectDevice=bluetoothDevicesList.get(position);
                new AlertDialog.Builder(SetMateActivity.this).setTitle("提示")
                        .setMessage("确定连接"+ connectDevice.getName()+ connectDevice.getAddress()+"?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                bleServerList.clear();
                                bleServerListAdapter.notifyDataSetChanged();
                                if(bluetoothGatt!=null) {
                                    //bluetoothGatt.disconnect();
                                    bluetoothGatt.close();
                                    // bluetoothGatt=null;
                                }
                                setTitle("正在建立连接...");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    //Android6.0以上需要调用这个方法，如果6.0以上不用这个方法而是选择下面那个方法会出现133错误
                                    bluetoothGatt = connectDevice.connectGatt(SetMateActivity.this, false, bluetoothGattCallback,TRANSPORT_LE);
                                }else {
                                    bluetoothGatt = connectDevice.connectGatt(SetMateActivity.this, false, bluetoothGattCallback);
                                }

                                if(scanner!=null){
                                    scanner.stopScan(leCallback);
                                    scanner=null;
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setTitle("正在建立连接...");
                                    }
                                });
                            }

                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                /*
                if(connectDevice!=null){
                    setTitle("选中："+bluetoothDevicesList.get(position).getName()+bluetoothDevicesList.get(position).getAddress());
                    Toast.makeText(SetMateActivity.this,"选择设备"+connectDevice.getName()+connectDevice.getAddress(),Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(SetMateActivity.this,"未选择设备",Toast.LENGTH_SHORT).show();

                 */

            }
        });
        lv_setMateServerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ServerUUID=null;
                CharUUID=null;
                bleCharList.clear();
                bleCharListAdapter.notifyDataSetChanged();
                findCharServer(position);
            }
        });
        lv_setMateCharList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(ServerUUID==null){
                    showMessage2("选择服务列表,再选择特征");
                    return;
                }
                CharUUID=UUID.fromString(bleCharList.get(position).get(0).toString());
                tv_setMateCharUUId.setText(CharUUID.toString());
                bt_setMateSendMsg.setEnabled(false);
                bt_setMateReadMsg.setEnabled(false);
                String status=bleCharList.get(position).get(2);
                if(status.equals("WW")){
                    bt_setMateSendMsg.setEnabled(true);
                    bt_setMateReadMsg.setEnabled(true);
                }
                if(status.equals("RWW")){
                    bt_setMateSendMsg.setEnabled(true);
                    bt_setMateReadMsg.setEnabled(true);
                }
                if(status.equals("RW")){
                    bt_setMateSendMsg.setEnabled(true);
                    bt_setMateReadMsg.setEnabled(true);
                }
                if(status.equals("W")){
                    bt_setMateSendMsg.setEnabled(true);
                }
                if(status.equals("R")){
                    bt_setMateReadMsg.setEnabled(true);
                }

                mNotifyCharacteristic=null;
                mNotifyCharacteristic=bluetoothGatt.getService(ServerUUID).getCharacteristic(CharUUID);

            }
        });
        bleServerListAdapter=new bleServerListAdapter(SetMateActivity.this);
        bleServerListAdapter.bindData(bleServerList);
        lv_setMateServerList.setAdapter(bleServerListAdapter);

        bleCharListAdapter=new bleServerListAdapter(SetMateActivity.this);
        bleCharListAdapter.bindData(bleCharList);
        lv_setMateCharList.setAdapter(bleCharListAdapter);


    }

    private void findCharServer(int position){
        final List<String> list = bleServerList.get(position);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //BluetoothGattServer gattServer=bluetoothGatt.getService(UUID.fromString(uuid));
                ServerUUID=UUID.fromString(list.get(0));
                tv_setMateServerUUID.setText(ServerUUID.toString());
                BluetoothGattService  gattService=bluetoothGatt.getService(UUID.fromString(list.get(0)));
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                //showMessage2("gattCharacteristics Count is:" + gattCharacteristics.size());

                Log.i("TAG","Count is:" + gattCharacteristics.size());
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    List<String> charList=new ArrayList<>();
                    charList.add(gattCharacteristic.getUuid().toString());
                    String check="不可进行任何操作";
                    String status="";
                    int charaProp = gattCharacteristic.getProperties();
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                       // showMessage2("可进行操作：读");
                        Log.i("TAG","可进行操作：读");
                        check="可进行操作：read";
                        status="R";
                    }
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                       // showMessage2("可进行操作：写");
                        Log.i("TAG","可进行操作：写");
                        check+=",WRITE";
                        status+="W";
                    }
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                       // showMessage2("可进行操作：无反馈的写");
                        Log.i("TAG","可进行操作：无反馈的写");
                        check+=",WRITE_NO_RESPONSE";
                        status+="W";
                    }
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                       // showMessage2("可进行操作：通知");
                        Log.i("TAG","可进行操作：通知");
                        check+=",PROPERTY_NOTIFY";

                    }
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                       // showMessage2("可进行操作：indicate");
                        Log.i("TAG","可进行操作：indicate");
                        check+=",indicate";
                    }
                    charList.add(check);
                    charList.add(status);
                    bleCharList.add(charList);
                    bleCharListAdapter.notifyDataSetChanged();

                }
            }
        });

    }
    private BluetoothAdapter.LeScanCallback leScanCallback=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //对扫描到的设备进行处理，可以依据BluetoothDevice中的信息、信号强度rssi以及广播包和响应包组成的scanRecord字节数组进行分析
            if (!bluetoothDevicesList.contains(device)) {  //判断是否已经添加
                bluetoothDevicesList.add(device);
                setTitle(bluetoothDevicesList.toString());
                bluetoothDeviceAdapter.notifyDataSetChanged();
            }
        }
    };
    private void init(){
        bt_setMateSendMsg=(Button)findViewById(R.id.bt_setMateSendMsg);
        bt_setMateScanDevice=(Button)findViewById(R.id.bt_setScanDevice);
        bt_setMateConnect=(Button)findViewById(R.id.bt_setMateConnect);
        bt_setMateAround=(Button)findViewById(R.id.bt_setMateAround) ;
        bt_setMateAroundStop=(Button)findViewById(R.id.bt_setMateAroundStop);
        bt_setMateReadMsg=(Button)findViewById(R.id.bt_setMatereadMsg);
        bt_setMateSaveMsg=(Button)findViewById(R.id.bt_setMateSaveMsg);
        bt_setMateSaveUUID=(Button)findViewById(R.id.bt_setMateSaveUUID);
        tv_setMateCharUUId=(TextView)findViewById(R.id.tv_setMateCharUUID);
        tv_setMateServerUUID=(TextView)findViewById(R.id.tv_setMateServerUUID);
        tv_setMateShowGetMsg=(TextView)findViewById(R.id.tv_setMateShowGetMsg);
        bt_setMateClearAll=(Button)findViewById(R.id.bt_setMateClearAll);
        lv_setMateList=(ListView)findViewById(R.id.lv_setMateList);
        lv_setMateServerList=(ListView)findViewById(R.id.lv_setMateServerList);
        lv_setMateCharList=(ListView)findViewById(R.id.lv_setMateCharList);
        et_setMateSendMsg=(EditText)findViewById(R.id.et_setMateSendMsg);

        bt_setMateSendMsg.setEnabled(false);
        bt_setMateReadMsg.setEnabled(false);

    }
    private void setOnClick(){
        OnClick onClick=new OnClick();
        bt_setMateSendMsg.setOnClickListener(onClick);
        bt_setMateScanDevice.setOnClickListener(onClick);
        bt_setMateConnect.setOnClickListener(onClick);
        bt_setMateAround.setOnClickListener(onClick);
        bt_setMateAroundStop.setOnClickListener(onClick);
        bt_setMateReadMsg.setOnClickListener(onClick);
        bt_setMateClearAll.setOnClickListener(onClick);
        bt_setMateSaveUUID.setOnClickListener(onClick);
        bt_setMateSaveMsg.setOnClickListener(onClick);
    }

    private void showMessage(String msg){
        Toast.makeText(SetMateActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
    private void showMessage2(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMessage(msg);
            }
        });
    }

    public void findService(List<BluetoothGattService> gattServices)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothGatt.requestMtu(500);
        }
        //showMessage("Count is:" + gattServices.size());
        for (BluetoothGattService gattService : gattServices)
        {
            List<String> serverList= new ArrayList<>();
            serverList.add(gattService.getUuid().toString());

            Log.i("TAG","gattService.getUuid:"+gattService.getUuid().toString());
            if(gattService.getType()==0) {
               // Log.i("TAG", "gattService.g.getType:primary service");
                serverList.add("primary servie");
            }
            else{
               // Log.i("TAG","gattService.g.getType:secondary service");
                serverList.add("secondary service");
            }
            bleServerList.add(serverList);
            bleServerListAdapter.notifyDataSetChanged();
            if(gattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString()))
            {
                showMessage2("找到服务:"+UUID_SERVICE.toString());
                ServerUUID=UUID_SERVICE;
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
               // showMessage2("Count is:" + gattCharacteristics.size());

                Log.i("TAG","Count is:" + gattCharacteristics.size());
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
                {

                   // Log.i("TAG","gattCharacteristic.getUuid():"+gattService.getUuid().toString());
                   //Log.i("TAG","UUID_NOTIFY:"+UUID_NOTIFY.toString());
                    if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY.toString()))
                    {
                        showMessage2("找到服务对应的特征"+UUID_NOTIFY.toString());
                        CharUUID=UUID_NOTIFY;
                        mNotifyCharacteristic = gattCharacteristic;
                        setCharacteristicNotification(mNotifyCharacteristic, true);

                        //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                        charaGetProperties();//

                       // Log.i("TAG","发现服务，读取数据"+new String(mNotifyCharacteristic.getValue()));
                        return;
                    }
                    else{
                        showMessage2("没找到服务对应的特征，请手动选择");
                        ServerUUID=null;
                        CharUUID=null;
                        return;
                    }
                }
            }
        }
        showMessage2("没有找到服务，请手动选择");

    }
    //BluetoothGattCharacteristic读写权限检查
    private void charaGetProperties() {
        int charaProp = mNotifyCharacteristic.getProperties();
        bt_setMateSendMsg.setEnabled(false);
        bt_setMateReadMsg.setEnabled(false);
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            //showMessage2("可进行操作：读");
           // Log.i("TAG","可进行操作：读");
            bt_setMateReadMsg.setEnabled(true);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            //showMessage2("可进行操作：写");
            //Log.i("TAG","可进行操作：写");
            bt_setMateSendMsg.setEnabled(true);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
           // showMessage2("可进行操作：无反馈的写");
           //Log.i("TAG","可进行操作：无反馈的写");
            bt_setMateSendMsg.setEnabled(true);

        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            //showMessage2("可进行操作：通知");
            //Log.i("TAG","可进行操作：通知");
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            //showMessage2("可进行操作：indicate");
            //Log.i("TAG","可进行操作：indicate");

        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            showMessage("蓝牙没有初始化");
            return;
        }
       boolean b= bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if(b){
            List<BluetoothGattDescriptor> descriptors=characteristic.getDescriptors();
            for(BluetoothGattDescriptor dp:descriptors) {
                dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(dp);
                }
            Log.i("TAG","监听收数据开始");
        }
        else {
            Log.i("TAG","数据发送失败");
        }
    }
    private  BluetoothGattCallback bluetoothGattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                Log.i("TAG","连接成功");
                bluetoothGatt.discoverServices();

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                   showMessage2("连接成功");
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           setTitle("连接成功");
                       }
                   });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                  setTitle("断开连接");
                }
            }
            else   showMessage2("连接失败");

        }
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
               // broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      showMessage2("发现服务,并广播出去");
                                      findService(gatt.getServices());


                                  }
                              }
                );


            } else {
                showMessage2("onServicesDiscovered被系统回调,没有发现服务，不广播，status= " + status);
            }


        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
           Log.i("TAG","onCharacteristicRead");
           Log.i("TAG",new String(characteristic.getValue()));
           final String msg=new String(characteristic.getValue());
            if(!msg.equals("")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(msg.equals("")||msg==null)
                            tv_setMateShowGetMsg.setText("无数据传过来");
                        else
                            tv_setMateShowGetMsg.setText(msg);
                    }
                });
            }

        }


        /**
         *  发送数据后的回调
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,final int status) {//发送数据时调用
            Log.i("TAG", "数据发送了哦");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //showMessage2("onCharacteristicWrite中");
                    if(status == BluetoothGatt.GATT_SUCCESS){//写入成功
                        showMessage2("发送成功");
                        bluetoothGatt.readCharacteristic(mNotifyCharacteristic);
                        bluetoothGatt.setCharacteristicNotification(mNotifyCharacteristic,true);
                    }else if (status == BluetoothGatt.GATT_FAILURE){
                        showMessage2("发送失败");
                    }else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED){
                        showMessage2("没权限");
                    }
                }
            });


        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {//descriptor读
            //Log.e("onCDescripticRead中", "数据接收了哦"+bytesToHexString(characteristic.getValue()))
            showMessage2("onDescriptorRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {// Characteristic 改变，数据接收会调用
            final String msg=new String(characteristic.getValue());
                Log.i("TAG", "服务传回数据" + new String(characteristic.getValue()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //tv_setMateShowGetMsg.setText(msg);
                        tv_setMateShowGetMsg.append(msg);
                    }
                });

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {//descriptor写
            showMessage2("onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            showMessage2("onReliableWriteCompleted(");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) { //读Rssi
            showMessage2("onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, final int mtu, final int status) {
            if(status==0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage2("MTU改变:"+mtu);
                        Log.i("TAG","MTU改变:"+mtu+"Status:"+status);
                    }
                });

            }
            else  Log.i("TAG","MTU没有改变:"+mtu+"Status:"+status);
        }
    };
    public void WriteValue(String strValue)
    {
        byte[] send=new byte[500];
        send=strValue.getBytes();
        if(mNotifyCharacteristic==null||bluetoothGatt==null){
            showMessage2("Characteristic is null");
            return;
        }
            mNotifyCharacteristic.setValue(send);
            bluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
            Log.i("TAG",mNotifyCharacteristic.getStringValue(0));
            Log.i("TAG", mNotifyCharacteristic.getValue().toString());


    }
    ScanCallback leCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();

               if (!bluetoothDevicesList.contains(device)) {  //判断是否已经添加
                   bluetoothDevicesList.add(device);
                   setTitle(bluetoothDevicesList.toString());
                   bluetoothDeviceAdapter.notifyDataSetChanged();

                }

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
           setTitle("搜索失败代码errorCode："+errorCode);
        }
    };
    private class OnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            String sql="";
            switch (v.getId()){
                case R.id.bt_setMatereadMsg:
                    if(ServerUUID==null||CharUUID==null) {
                        showMessage("没有选择服务或特征，左右滑动选择");
                        return;
                    }
                    if(bluetoothGatt==null) {
                        showMessage("没有与设备连接");
                        return;
                    }

                     bluetoothGatt.readCharacteristic(mNotifyCharacteristic);
                     bluetoothGatt.setCharacteristicNotification(mNotifyCharacteristic,true);


                    break;
                case R.id.bt_setMateSendMsg:
                    if(bluetoothGatt==null){
                        showMessage("没有设备连接");
                        return;
                    }
                    if(ServerUUID==null||CharUUID==null) {
                        showMessage("没有选择服务或特征");
                        return;
                    }
                    showMessage2("发送消息："+et_setMateSendMsg.getText().toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WriteValue(et_setMateSendMsg.getText().toString());
                        }

                    });


                   //Log.i("TAG","显示读取内容:"+mNotifyCharacteristic.getStringValue(0));
                    break;
                case R.id.bt_setScanDevice:{
                    bluetoothDeviceAdapter=new bluetoothDeviceAdapterList(SetMateActivity.this);
                    bluetoothDeviceAdapter.bindData(bluetoothDevicesList);
                    lv_setMateList.setAdapter(bluetoothDeviceAdapter);
                    if (bluetoothDevicesList != null) {
                        bluetoothDevicesList.clear();
                        bluetoothDeviceAdapter.notifyDataSetChanged();
                    }
                    if (scanner != null) {
                        scanner.stopScan(leCallback);
                    }

                    scanner.startScan(leCallback);
                   // mBluetoothAdapter.startLeScan(leScanCallback);
                   setTitle("扫描中...");
                    break;

                }
                case R.id.bt_setMateConnect:
                    if(connectDevice==null){
                        setTitle("无连接设备");
                        return;
                    }
                    if(bluetoothGatt!=null){
                       bluetoothGatt.close();
                    }
                    connectDevice=mBluetoothAdapter.getRemoteDevice(connectDevice.getAddress());
                   // bluetoothGatt=connectDevice.connectGatt(SetMateActivity.this,false,bluetoothGattCallback);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //Android6.0以上需要调用这个方法，如果6.0以上不用这个方法而是选择下面那个方法会出现133错误
                        bluetoothGatt = connectDevice.connectGatt(SetMateActivity.this, false, bluetoothGattCallback,TRANSPORT_LE);
                    }else{
                        bluetoothGatt= connectDevice.connectGatt(SetMateActivity.this, false, bluetoothGattCallback);
                    }
                    if(bluetoothGatt==null){
                       showMessage("Bluetoothgatt==null");
                       return;
                   }
                   else {
                        showMessage("Bluetoothgatt ！=null");

                    }
                    Log.i("TAG","bluetoothGatt连接回调");
                    setTitle("重新连接...");
                    setTitle(connectDevice.toString());
                   // if(leCallback!=null)
                     //   scanner.stopScan(leCallback);
                    break;
                case R.id.bt_setMateAround://周围设备
                    gattSever();
                    break;
                case R.id.bt_setMateAroundStop://周围设备停止广播
                    stopAdvertise();
                    break;
                case R.id.bt_setMateClearAll:
                    tv_setMateShowGetMsg.setText("");
                    et_setMateSendMsg.setText("");
                    sql="delete from originalData" ;
                    if(db.insert(sql)){
                        showMessage2("清除成功");
                    }
                    else{
                        showMessage2("清除失败");
                    }
                    break;
                case R.id.bt_setMateSaveUUID:
                    String ServerUUID=tv_setMateServerUUID.getText().toString();
                    String CharUUId=tv_setMateCharUUId.getText().toString();
                    if(ServerUUID.equals("")||CharUUId.equals(""))
                    {
                        showMessage2("服务和特征UUID都不能为空");
                        return;
                    }
                    sql="delete * from bleUUID";
                    db.delete(sql);
                    sql="insert into bleUUID(serverUUID,charUUID) values('"+ServerUUID+"','"+CharUUId+"');";
                    if(db.insert(sql)){
                        showMessage2("保存成功");
                    }
                    else showMessage2("保存失败");
                    break;
                case R.id.bt_setMateSaveMsg:
                    String msg=tv_setMateShowGetMsg.getText().toString();
                    if(msg.equals(""))
                    {
                        showMessage2("数据为空，不保存");
                        return;
                    }
                    db.delete("delete  from originalData");
                    sql="insert into originalData(data) values('"+msg+"')";
                    if(db.insert(sql)){
                        showMessage2("保存成功");
                    }
                    else{
                        showMessage2("保存失败");
                    }
                    break;
            }
        }
    }
    private AdvertiseSettings buildAdvertiseSettings(){
        AdvertiseSettings.Builder settingsBuilder=new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setTimeout(0);
        return settingsBuilder.build();
    }
    private class SampleAdvertiseCallback extends AdvertiseCallback {
        @Override
        public void onStartFailure(int errorCode){
            super.onStartFailure(errorCode);
            setTitle("广播失败");
          //  sendFailureIntent(errorCode);
           // stopSelf();
        }
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect){
            super.onStartSuccess(settingsInEffect);
            setTitle("服务端的广播成功开启");
           // Log.d(TAG,"BLE服务的广播启动成功后：TxPowerLv="+settingsInEffect.getTxPowerLevel()+"；mode="+settingsInEffect.getMode()+"；timeout="+settingsInEffect.getTimeout());
            settingsInEffect.getTxPowerLevel();
            settingsInEffect.getMode();
            settingsInEffect.getTimeout();
            initServices(SetMateActivity.this);//该方法是添加一个服务
        }
    }
    private void stopAdvertising(){
        setTitle("服务停止广播");
        if(bluetoothLeAdvertiser!=null){
            bluetoothLeAdvertiser.stopAdvertising(mAdertiseCallback);
            mAdertiseCallback=null;
        }
    }

    private AdvertiseData buildAdvertiseData(){
        AdvertiseData.Builder dataBuilder=new AdvertiseData.Builder();
        dataBuilder.setIncludeDeviceName(true);
        return dataBuilder.build();
    }
    private  void gattSever(){
        bluetoothLeAdvertiser=mBluetoothAdapter.getBluetoothLeAdvertiser();

        mAdertiseCallback=new SampleAdvertiseCallback();
        bluetoothLeAdvertiser.startAdvertising(buildAdvertiseSettings(),buildAdvertiseData(),mAdertiseCallback);
        //initServices(SetMateActivity.this);
    }

//gattServer 回调
    private BluetoothGattServerCallback bluetoothGattServerCallback=new BluetoothGattServerCallback() {
        //1、首先是连接状态的回调
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.i("TAG","连接状态发生改变，安卓系统回调onConnectionStateChange:device name="+device.getName()+"address="+device.getAddress()+"status="+status+"newstate="+newState);
        }

        @Override
        public void onCharacteristicReadRequest(final BluetoothDevice device, int requestId, int offset, final BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.i("TAG","客户端有读的请求，安卓系统回调该onCharacteristicReadRequest()方法");

            mBluetoothGattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,offset,characteristic.getValue());
        }

        //接受具体字节，当有特征被写入时，回调该方法，写入的数据为参数中的value
        @Override
        public void onCharacteristicWriteRequest(final BluetoothDevice device, int requestId, final BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
           Log.i("TAG","客户端有写的请求，安卓系统回调该onCharacteristicWriteRequest()方法");
            //特征被读取，在该回调方法中回复客户端响应成功
            final String str=new String(value);
            showMessage2("接受到消息："+str);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(str.equals("" )){
                        showMessage2("客户端无数据传输");
                    }
                    else showMessage2(str);
                    Log.i("TAG",str);
                }
            });
            if(mBluetoothGattServer.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,offset,value)){
                Log.i("TAG","server sendresponse");
            }
            else
                Log.i("TAG","server sendresponse 失败");
            Log.i("TAG","server chara:"+characteristic.getUuid());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 3; i++) {
                        if (characteristic.setValue(("{" + "项目1:" + i + "}").getBytes())) {
                            Log.i("TAG", "server setvalue sucess");
                            if (mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, true)) {
                                Log.i("TAG", "notifyCharacteristicChanged true");

                            } else Log.i("TAG", "notifyCharacteristicChanged failue");
                        } else
                            Log.i("TAG", "server setvalue failure");
                        if (characteristic.setValue(("{" + "hello:" + (i+10) + ",hello2:"+(i+30)+"}").getBytes())) {
                            Log.i("TAG", "server setvalue sucess");
                            if (mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, true)) {
                                Log.i("TAG", "notifyCharacteristicChanged true");

                            } else Log.i("TAG", "notifyCharacteristicChanged failue");
                        } else
                            Log.i("TAG", "server setvalue failure");
                        if (characteristic.setValue(("{" + "项目2:" + (i+100) + "}").getBytes())) {
                            Log.i("TAG", "server setvalue sucess");
                            if (mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, true)) {
                                Log.i("TAG", "notifyCharacteristicChanged true");

                            } else Log.i("TAG", "notifyCharacteristicChanged failue");
                        } else
                            Log.i("TAG", "server setvalue failure");
                    }
                }
            });

           // Log.i("TAG","chara.tostring",characteristic.getStringValue(0));
            //response.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            //处理响应内容
            //value:客户端发送过来的数据
           //onResponseToClient(value,device,requestId,characteristic);
        }

        //特征被读取。当回复相应成功后，客户端胡读取然后触发本方法
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            Log.i("TAG","特征被读取");
            mBluetoothGattServer.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,offset,null);
        }

        //2、其次，当有描述请求被写入时，回调该方法，
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);

            mBluetoothGattServer.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,offset,value);
            // onResponseToClient(value,device,requestId,descriptor.getCharacteristic());
        }

        @Override
        public void onServiceAdded(int status,BluetoothGattService service){
            super.onServiceAdded(status,service);
           showMessage2("添加服务成功，安卓系统回调该onServiceAdded()方法");
        }
    };

    //public static final ParcelUuid Service_UUID=ParcelUuid.fromString("0000b81d-0000-1000-8000-00805f9b34fb");
    private static UUID UUID_SERVER = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static UUID UUID_CHARREAD = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private static UUID UUID_CHARWRITE = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    private static UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private UUID UUID_SERVICE =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static UUID UUID_NOTIFY =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    //  public final static UUID UUID_SERVICE =
    //        UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
  //  public final static UUID UUID_NOTIFY =
  //         UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");



   // public static final ParcelUuid Service_UUID=ParcelUuid.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
   // private static UUID UUID_SERVER = UUID.fromString(" 0000ffe0-0000-1000-8000-00805f9b34fb");
   // private static UUID UUID_CHARREAD = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
   // private static UUID UUID_CHARWRITE = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
  // private static UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private void initServices(Context context){
        mBluetoothGattServer=mBluetoothManager.openGattServer(context,bluetoothGattServerCallback);
        BluetoothGattService service=new BluetoothGattService(UUID_SERVER,BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //characteristicRead=new BluetoothGattCharacteristic(UUID_CHARREAD,BluetoothGattCharacteristic.PROPERTY_READ,BluetoothGattCharacteristic.PERMISSION_READ);
        characteristicRead=new BluetoothGattCharacteristic(UUID_CHARREAD,
                BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_READ|BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        BluetoothGattDescriptor descriptor=new BluetoothGattDescriptor(UUID_DESCRIPTOR,BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristicRead.addDescriptor(descriptor);
        service.addCharacteristic(characteristicRead);

        BluetoothGattCharacteristic characteristicWrite=new BluetoothGattCharacteristic(UUID_CHARWRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_READ|BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(characteristicWrite);

        mBluetoothGattServer.addService(service);
        setTitle("初始化服务成功：initServices ok");

    }
    private void stopAdvertise(){
        if (bluetoothLeAdvertiser != null) {
            bluetoothLeAdvertiser.stopAdvertising(mAdertiseCallback);
            bluetoothLeAdvertiser = null;
        }

        if(mBluetoothAdapter != null){
            mBluetoothAdapter = null;
        }

        if (mBluetoothGattServer != null) {
            mBluetoothGattServer .clearServices();
            mBluetoothGattServer .close();
            mBluetoothGattServer  = null;
        }
        showMessage("设备广播停止");

    }



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
