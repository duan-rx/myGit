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
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.train.R;
import com.example.train.adapter.bleDeviceListAdapter;
import com.example.train.constants.user;
import com.example.train.database.databaseDao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

public class setRunActivity extends AppCompatActivity {
    private EditText et_setRunSendMsg;
    private TextView tv_setRunStatus;
    private Button bt_setRunReScan,bt_setRunReConnect,bt_setRunStopScan,bt_setStopConnect
            ,bt_setRunClearMsg,bt_setRunSendMsg;
    private ListView lv_setRunShowList;

    private BluetoothGattServer gattServer=null;
    private BluetoothGattCharacteristic characteristic=null;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothLeScanner scanner;
    private List<List<String>> listMsg=new ArrayList<>();
    private bleDeviceListAdapter listAdapter;
    private int status=0;//0 ble扫描状态 1 连接状态
    private databaseDao db=null;
    private UUID ServerUUID,CharUUID;
    private BluetoothDevice connectDevice;
    private String instrumentCode;
    private int fouService=1;//1为第一次连接
    private int isConnect=0;//连接是否成功，1成功
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_run);
        status=0;
        initDatabase();
        initBluetoothBle();
        init();
        setOnclick();
        listAdapter=new bleDeviceListAdapter(this);
        listAdapter.bindData(listMsg);
        lv_setRunShowList.setAdapter(listAdapter);
        initBleScan();
        lv_setRunShowList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(status==0){
                    connectBle(listMsg.get(position).get(1).toString());
                }
                else {
                   // Log.i("TAG",listMsg.get(position).get(2).toString()+listMsg.get(position).get(3).toString());
                    changeChannel(position,0);//对数据进行匹配
                    return;
                }
            }
        });
    }
    private void initDatabase(){
        db=new databaseDao(setRunActivity.this);
        String sql="select * from bleUUID";
        Cursor cursor=db.query(sql);
        if(cursor.getCount()<=0) {
            sql = "insert into bleUUId(serverUUID,charUUID) values('0000ffe0-0000-1000-8000-00805f9b34fb','0000ffe1-0000-1000-8000-00805f9b34fb');";
            db.insert(sql);
        }
        sql="select serverUUID,charUUID from bleUUID";
        Log.i("TAG",sql);
        cursor=db.query(sql);
        if(cursor.getCount()<=0){
            new AlertDialog.Builder(setRunActivity.this).setTitle("提示").setMessage("没有选定ble服务uuid和特征uuid,请前往蓝牙设置进行设置")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }
        while (cursor.moveToNext()){
            int index = cursor.getColumnIndex("serverUUID");
            ServerUUID=UUID.fromString(cursor.getString(index));
            index = cursor.getColumnIndex("charUUID");
            CharUUID=UUID.fromString(cursor.getString(index));

        }
        cursor.close();

        sql="select inCode,inName from instrument where status=1";
        cursor=db.query(sql);
        Log.i("TAG",sql);
        if(cursor.getCount()<=0){
            new AlertDialog.Builder(setRunActivity.this).setTitle("提示").setMessage("没有仪器可以选择，请添加仪器")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }
        final String[] inCode=new String[cursor.getCount()];
        String[] inName=new String[cursor.getCount()];
        int position=0;
        while (cursor.moveToNext()){

            int index = cursor.getColumnIndex("inCode");
            inCode[position]=cursor.getString(index);
            index = cursor.getColumnIndex("inName");
            inName[position]=cursor.getString(index);
            position++;

        }
        cursor.close();
        final int[] selectPosition= new int[1];
        new AlertDialog.Builder(setRunActivity.this).setTitle("选择仪器")
                .setIcon(R.mipmap.ic_launcher)
                .setSingleChoiceItems(inName, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        selectPosition[0]=which;
                        instrumentCode=inCode[which];
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        instrumentCode=inCode[selectPosition[0]];
                        Log.i("TAG","inCode:"+instrumentCode);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();

    }
    private void init(){
        bt_setRunReConnect=(Button)findViewById(R.id.bt_setRunReConnect);
        bt_setRunReScan=(Button)findViewById(R.id.bt_setRunReScan);
        bt_setStopConnect=(Button)findViewById(R.id.bt_setRunStopConnect);
        bt_setRunStopScan=(Button)findViewById(R.id.bt_setRunStopScan);
        bt_setRunClearMsg=(Button)findViewById(R.id.bt_setRunClearMsg);
        bt_setRunSendMsg=(Button)findViewById(R.id.bt_setRunSendMsg);
        et_setRunSendMsg=(EditText)findViewById(R.id.et_setRunMsg);
        tv_setRunStatus=(TextView)findViewById(R.id.tv_setRunStatus);
        lv_setRunShowList=(ListView)findViewById(R.id.lv_setRunShowList);
    }
    private void setOnclick(){
        OnClick onClick=new OnClick();
       bt_setRunStopScan.setOnClickListener(onClick);
        bt_setRunReScan.setOnClickListener(onClick);
        bt_setStopConnect.setOnClickListener(onClick);
        bt_setRunReConnect.setOnClickListener(onClick);
        bt_setRunClearMsg.setOnClickListener(onClick);
        bt_setRunSendMsg.setOnClickListener(onClick);

    }
    private class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.bt_setRunReScan://重新扫描
                    initBleScan();
                    status=0;
                    break;
                case R.id.bt_setRunStopScan://停止扫描
                    if(scanner!=null){
                        scanner.stopScan(scanCallback);
                        scanner=null;
                        tv_setRunStatus.setText("停止扫描");
                    }
                    else tv_setRunStatus.setText("已经停止扫描");
                    status=0;
                    break;
                case R.id.bt_setRunReConnect://重新连接

                    if(bluetoothGatt!=null) {
                        //bluetoothGatt.disconnect();
                        bluetoothGatt.close();
                        //bluetoothGatt=null;
                    }
                    if(connectDevice==null){
                        showMessage2("设备没有连接过，不能重新连接");
                        return;
                    }
                    connectDevice = mBluetoothAdapter.getRemoteDevice(connectDevice.getAddress());
                    //.connect();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //Android6.0以上需要调用这个方法，如果6.0以上不用这个方法而是选择下面那个方法会出现133错误
                        bluetoothGatt = connectDevice.connectGatt(setRunActivity.this, false, bluetoothGattCallback,TRANSPORT_LE);
                    }else{
                        bluetoothGatt= connectDevice.connectGatt(setRunActivity.this, false, bluetoothGattCallback);
                    }
                    tv_setRunStatus.setText("重新连接...");
                    status=1;
                    isConnect=0;
                    break;
                case R.id.bt_setRunStopConnect://断开连接
                    status=1;
                    if(connectDevice==null){
                        showMessage2("还没有连接过,无需断开");
                        return;
                    }
                    if(bluetoothGatt==null){
                        showMessage2("已经断开连接");
                        return;
                    }
                   // bluetoothGatt.disconnect();
                    bluetoothGatt.close();
                    tv_setRunStatus.setText("断开连接");
                    isConnect=0;
                    break;
                case R.id.bt_setRunClearMsg:
                    listMsg.clear();
                    listAdapter.notifyDataSetChanged();
                    break;
                case R.id.bt_setRunSendMsg://发送
                    if(bluetoothGatt==null){
                        showMessage2("没有连接");
                        return;
                    }
                    if(isConnect==1) {
                        characteristic = bluetoothGatt.getService(ServerUUID).getCharacteristic(CharUUID);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("TAG", "发送" + et_setRunSendMsg.getText().toString());
                                WriteValue(et_setRunSendMsg.getText().toString());

                            }
                        });
                    }else showMessage2("未建立连接，稍等...");

            }
        }
    };
    private void changeChannel(final int position,int status){//0为对已匹配，1未匹配
        Log.i("TAG","listMsg:"+listMsg.get(position).get(2).toString());
        if(listMsg.get(position).get(2).toString().equals("")){
            selectItem(position);
            return;
        }
            View view = getLayoutInflater().inflate(R.layout.train_count, null);
            final AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("提示")
                    .setView(view)
                    .setMessage("改变：" + listMsg.get(position).get(2).toString() + ":" + listMsg.get(position).get(3).toString() + "的匹配字符")
                    .create();
            final EditText et_trainCount = (EditText) view.findViewById(R.id.et_trainCount);
            et_trainCount.setInputType(InputType.TYPE_CLASS_TEXT);
            TextView tv_tranCountCancel = (TextView) view.findViewById(R.id.tv_trainCountCancel);
            TextView tv_tranCountOk = (TextView) view.findViewById(R.id.tv_trainCountOk);
            if(status==0) {
                String sql = "select channelCode from instrumentItem " +
                        "where inCode='" + instrumentCode + "' and  itemCode='" + listMsg.get(position).get(2).toString().trim() + "';";
                Cursor cursor = db.query(sql);
                if (cursor.getCount() <= 0) {
                    showMessage2("无此项目");
                    return;
                }
                while (cursor.moveToNext()) {
                    String channelCode = cursor.getString(cursor.getColumnIndex("channelCode"));
                    Log.i("TAG", "channel:" + channelCode);
                    if (channelCode.equals("") || channelCode == null) {
                        et_trainCount.setHint("输入字符匹配");
                    } else {
                        et_trainCount.setHint("原始匹配字符：" + channelCode);
                    }

                    break;
                }
                cursor.close();
            }else {
                et_trainCount.setHint("输入字符匹配");
            }
            tv_tranCountCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                }
            });
            tv_tranCountOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String number = et_trainCount.getText().toString().trim();
                    if (et_trainCount.equals("")) {
                        showMessage2("输入字符为空");
                    }
                    String sql = "update instrumentItem set channelCode='" + number + "'" +
                            " where inCode='" + instrumentCode + "' and  itemCode='" + listMsg.get(position).get(2).toString().trim() + "';";
                    if (db.update(sql)) {
                        showMessage2("更新成功");
                    } else showMessage2("更新失败");
                    alertDialog.cancel();
                }
            });
            alertDialog.show();

    }
    private void selectItem(final int position){
        String sql="select itemCode,itemName from instrumentItem where inCode='" + instrumentCode+"'";
        Cursor cursor=db.query(sql);
        if(cursor.getCount()<=0){
            new AlertDialog.Builder(setRunActivity.this).setTitle("提示").setMessage("没有项目可以选择，请添加项目")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           return;
                        }
                    }).show();
        }
         final String[] itemCode=new String[cursor.getCount()];
         final String[] itemName=new String[cursor.getCount()];
        Log.i("TAG","count:"+cursor.getCount());
        int p=0;
        Log.i("TAG","fhoeiaf");
        while (cursor.moveToNext()){
            int index = cursor.getColumnIndex("itemName");
            itemName[p]=cursor.getString(index);
            index = cursor.getColumnIndex("itemCode");
            itemCode[p]=cursor.getString(index);
            p++;
        }
        cursor.close();
        Log.i("TAG","fhoeiaf3");
        final int[] selectPosition= new int[1];
        new AlertDialog.Builder(setRunActivity.this).setTitle("选择仪器")
                .setIcon(R.mipmap.ic_launcher)
                .setSingleChoiceItems(itemName, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        listMsg.get(position).set(2,itemCode[which]);
                        listMsg.get(position).set(3,itemName[which]);
                        Log.i("TAG","select:"+which+":"+listMsg.get(position).get(3));
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       //instrumentCode=inCode[selectPosition[0]];
                        changeChannel(position,1);
                        Log.i("TAG","inCode:"+instrumentCode);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }
    private void initBluetoothBle(){
        if (Build.VERSION.SDK_INT >= 26)
        {
            checkPermissions();
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            tv_setRunStatus.setText( "设备不支持低功耗蓝牙");
            finish();
        }
        mBluetoothManager=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter=mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            tv_setRunStatus.setText( "不支持蓝牙");
            finish();
            return;
        }
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }
    private void dealData (String msg, String data){
        String[] itemCode = new String[1];
        String[] itemType = new String[1];
        String[] itemName = new String[1];
        Log.i("TAG", msg);
        List<String> list = new ArrayList<>();
        String[] msg3 = msg.split(":");
        if(data.equals(""))
            list.add("原始数据：" + msg);
        else {
            list.add("原始数据：" + data);
        }
        //list.add(msg3[0]);
        String sql = "select itemCode,itemName,itemResultType from instrumentItem where inCode='" + instrumentCode + "' and  channelCode='" + msg3[0].trim() + "';";
       // String  sql="select itemCode,itemName,itemResultType,itemUnit,itemReference,itemMax,itemMin,lineColor from instrumentItem where inCode='"+instrumentCode+"' and  channelCode='"+msg3[0].trim()+"';";
        Cursor cursor = db.query(sql);
        if (cursor.getCount() <= 0) {
            list.add("项目设置里-项目匹配，没有匹配或错误,数据进行舍弃");
            showMessage2("无此匹配:" + msg3[0]);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listAdapter.notifyDataSetChanged();
                }
            });
            list.add("");
            list.add("");
            listMsg.add(list);
            return;
        }
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex("itemCode");
            itemCode[0] = cursor.getString(index);
            index = cursor.getColumnIndex("itemName");
            itemName[0] = cursor.getString(index);
            index = cursor.getColumnIndex("itemResultType");
            itemType[0] = cursor.getString(index);
            break;
        }
        cursor.close();
        switch (itemType[0]) {
            case "1":
                sql = "insert into result(count,userId,inCode,itemCode,itemResultNum,status,dataTime) values('-1','"
                        + user.userId + "','" + instrumentCode + "','" + itemCode[0] + "','" + msg3[1] + "','" + 1 + "','" + System.currentTimeMillis() + "');";
                if (db.insert(sql)) {
                    Log.i("TAG","数据插入成功");
                    list.add("处理数字型结果：" + itemName[0] + ":" + msg3[1]);
                } else {
                    showMessage2("数据保存失败");
                }
                break;
            case "3":
                sql = "insert into result(count, userId,inCode,itemCode,itemResultBlob,status,dataTime) values('-1','"
                        + user.userId + "','" + instrumentCode + "','" + itemCode[0] + "','" + msg3[1] + "','" + 1 + "','" + System.currentTimeMillis() + "');";
                if (db.insert(sql)) {
                    // Log.i("TAG","数据插入成功");
                    list.add("处理布尔型结果：" + itemName[0] + ":" + msg3[1]);
                } else {
                    showMessage2("数据保存失败");
                }
                break;

            default:
                sql = "insert into result(count,userId,inCode,itemCode,itemResultString,status,dataTime) values('-1','"
                        + user.userId + "','" + instrumentCode + "','" + itemCode[0] + "','" + msg3[1] + "','" + 1 + "','" + System.currentTimeMillis() + "');";
                if (db.insert(sql)) {
                    //Log.i("TAG","数据插入成功");
                    list.add("处理字符结果：" + itemName[0] + ":" + msg3[1]);
                } else {
                    showMessage2("数据保存失败");
                }
                break;
        }
        list.add(itemCode[0]);
        list.add(itemName[0]);
        listMsg.add(list);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });
        db.delete("delete from result where count='-1'");
        //db.delete("delete from result ");
    }
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            List<String> list=new ArrayList<>();
            if(device.getName()==null){
                list.add("null");
            }
            else list.add(device.getName());
            list.add(device.getAddress());
            if (!listMsg.contains(list)) {  //判断是否已经添加
                listMsg.add(list);
                listAdapter.notifyDataSetChanged();
           }
        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            tv_setRunStatus.setText("搜索失败代码errorCode："+errorCode);
            Log.i("TAG","搜索失败代码errorCode："+errorCode);
        }
    };
    private void initBleScan(){
        listMsg.clear();
        listAdapter.notifyDataSetChanged();
        if (scanner!=null) {
            scanner.stopScan(scanCallback);
        }
        scanner=mBluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(scanCallback);
        tv_setRunStatus.setText("正在扫描设备");
    }
    private BluetoothGattCallback bluetoothGattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bluetoothGatt.discoverServices();
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    tv_setRunStatus.setText("建立连接，开始查找服务");
                    isConnect=0;
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                    tv_setRunStatus.setText("断开连接");
                }
            } else tv_setRunStatus.setText("连接失败");

        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
               // findService(gatt.getServices());
                characteristic=gatt.getService(ServerUUID).getCharacteristic(CharUUID);
                if(characteristic!=null){
                    bluetoothGatt.readCharacteristic(characteristic);
                    bluetoothGatt.setCharacteristicNotification(characteristic,true);
                    isConnect=1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_setRunStatus.setText("连接成功，可进行数据传输");
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_setRunStatus.setText("连接失败，设置-》蓝牙检测 进行测试");
                        }
                    });
                }

            } else {
                tv_setRunStatus.setText("没有发现服务，不广播，status= " + status);
            }


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.i("TAG", "onCharacteristicRead");
            Log.i("TAG", new String(characteristic.getValue()));
            final String msg = new String(characteristic.getValue());
            if (!msg.equals("")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (msg.equals("") || msg == null)
                            tv_setRunStatus.setText("无数据传过来");
                        else
                            tv_setRunStatus.setText(msg);
                    }
                });
            }

        }


        /**
         * 发送数据后的回调
         *
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          final BluetoothGattCharacteristic characteristic, final int status) {//发送数据时调用
            Log.i("TAG", "数据发送了哦");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //showMessage2("onCharacteristicWrite中");
                    if (status == BluetoothGatt.GATT_SUCCESS) {//写入成功
                        showMessage2("发送成功");
                    } else if (status == BluetoothGatt.GATT_FAILURE) {
                        showMessage2("发送失败");
                    } else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
                        showMessage2("没权限");
                    }
                }
            });


        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {//descriptor读
            //Log.e("onCDescripticRead中", "数据接收了哦"+bytesToHexString(characteristic.getValue()))
            tv_setRunStatus.setText("onDescriptorRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {// Characteristic 改变，数据接收会调用
            final String msg = new String(characteristic.getValue());
            String msg2 = msg.substring(1, msg.length() - 1);
            if (msg2.contains(",")) {
                String[] msg3 = msg2.split(",");
                {
                    for (int i = 0; i < msg3.length; i++) {
                        dealData(msg3[i].toString().trim(),msg);
                    }
                }
            } else {
                dealData(msg2.trim(),msg);
            }



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
    public void findService(List<BluetoothGattService> gattServices) {
        bluetoothGatt.requestMtu(500);
        //showMessage("Count is:" + gattServices.size());
        for (BluetoothGattService gattService : gattServices)
        {
            List<String> serverList= new ArrayList<>();
            if(gattService.getUuid().toString().equalsIgnoreCase(ServerUUID.toString()))
            {
                tv_setRunStatus.setText("找到服务:"+ServerUUID.toString());
                ServerUUID=gattService.getUuid();
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                // showMessage2("Count is:" + gattCharacteristics.size());

                //Log.i("TAG","Count is:" + gattCharacteristics.size());
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
                {

                    // Log.i("TAG","gattCharacteristic.getUuid():"+gattService.getUuid().toString());
                    //Log.i("TAG","UUID_NOTIFY:"+UUID_NOTIFY.toString());
                    if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(CharUUID.toString()))
                    {
                        tv_setRunStatus.setText("找到服务对应的特征，可以进行数据传输"+gattCharacteristic.getUuid().toString());
                        CharUUID=gattCharacteristic.getUuid();
                        characteristic=null;
                        characteristic= gattCharacteristic;
                        //setCharacteristicNotification(characteristic, true);

                        //setCharacteristicNotification(characteristic, true);
                        bluetoothGatt.readCharacteristic(characteristic);
                        bluetoothGatt.setCharacteristicNotification(characteristic,true);

                        // Log.i("TAG","发现服务，读取数据"+new String(mNotifyCharacteristic.getValue()));
                        return;
                    }
                    else{
                        tv_setRunStatus.setText("没找到服务对应的特征，请去蓝牙设置");
                        ServerUUID=null;
                        CharUUID=null;
                        return;
                    }
                }
            }
        }
        tv_setRunStatus.setText("没有找到服务，请去蓝牙设置");

    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            tv_setRunStatus.setText("蓝牙没有初始化");
            return;
        }
        boolean b= bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        bluetoothGatt.readCharacteristic(characteristic);
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
    private void connectBle(String bleAddress){
        connectDevice = mBluetoothAdapter.getRemoteDevice(bleAddress);
        new AlertDialog.Builder(setRunActivity.this).setTitle("提示")
                .setMessage("确定连接"+ connectDevice.getName()+ connectDevice.getAddress()+"?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(bluetoothGatt!=null) {
                           //bluetoothGatt.disconnect();
                            bluetoothGatt.close();
                           // bluetoothGatt=null;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //Android6.0以上需要调用这个方法，如果6.0以上不用这个方法而是选择下面那个方法会出现133错误
                            bluetoothGatt = connectDevice.connectGatt(setRunActivity.this, false, bluetoothGattCallback,TRANSPORT_LE);
                        }else {
                            bluetoothGatt = connectDevice.connectGatt(setRunActivity.this, false, bluetoothGattCallback);
                        }
                        status=1;

                        listMsg.clear();
                        listAdapter.notifyDataSetChanged();
                        if(scanner!=null){
                            scanner.stopScan(scanCallback);
                            scanner=null;
                        }
                    }

                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

    }
    public void WriteValue(String strValue) {
        byte[] send=new byte[500];
        send=strValue.getBytes();

        if(characteristic==null||bluetoothGatt==null){
            showMessage2("Characteristic is null");
            return;
        }
        characteristic.setValue(send);
        Log.i("TAg","CharUUId"+characteristic.getUuid());
        bluetoothGatt.writeCharacteristic(characteristic);
    }
    private void showMessage2(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(setRunActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });
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
