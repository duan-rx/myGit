package com.example.train.activity;

import androidx.annotation.Nullable;
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
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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
import com.example.train.adapter.histroyListAdapter;
import com.example.train.adapter.shouYeListAdapter;
import com.example.train.adapter.trainResultListAdapter;
import com.example.train.constants.user;
import com.example.train.database.databaseDao;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;



import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

public class TrainActivity extends AppCompatActivity {
    private databaseDao db=null;
    private UUID ServerUUID,CharUUID;
    private BluetoothDevice connectDevice;
    private String instrumentCode="";  //仪器代码
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scanner;
    private List<List<String>> listDevice=new ArrayList<>();
    private bleDeviceListAdapter listDeviceAdapter;
    private trainResultListAdapter listTrainResultAdapter;
    private Button bt_trainReScan,bt_trainReConnect,bt_trainStopScan,bt_trainStopConnect,bt_trainStopTrain,bt_trainStartTrain;
    private Button bt_trainSendMsg;
    private EditText et_trainSendMsg;
    private ListView lv_trainShowList;
    private int status=0;// 0扫描状态 1 连接状态
    private int count=-1;// 训练的次数 -1为停止
    private Boolean train=false;//训练标记
    private boolean bluetoothIsOpen=false;//蓝牙是否打开
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic=null;
    private Map<String,String> map= new HashMap<>();//标记项目
    private Map<String,String> lineMap=new HashMap<>();//标记曲线
    private int itemCount=0;//训练map标记
    private int lineCount=0;//曲线lineMap 标记
    private long startTime=0;//时间标记
    private LineChart lineChart;
    private XAxis xAxis; //x轴
    private YAxis leftYAxis;//y轴左侧
    private YAxis rightYAxis;//Y轴右侧
    private Legend legend;//图列
    private LimitLine limitLine;//限制线
    private IntentFilter intentFilter;
    private boolean isConnected=false;//是否成功建立连接

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReceive!=null) {
            unregisterReceiver(mReceive);
        }
        if (scanner != null) {
            scanner.stopScan(scanCallback);
        }

        if (bluetoothGatt != null) {
            bluetoothGatt.connect();
        }
        if(mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        Intent intent=getIntent();
        instrumentCode=intent.getStringExtra("inCode");
        Log.i("TAG","inst:"+instrumentCode);
        init();
        setOnclick();
        initDatabase();
        initBluetoothBle();
        listTrainResultAdapter=new trainResultListAdapter(this);
        listTrainResultAdapter.bindData(listDevice);
        listDeviceAdapter=new bleDeviceListAdapter(this);
        listDeviceAdapter.bindData(listDevice);
        lv_trainShowList.setAdapter(listDeviceAdapter);
        intentFilter=new IntentFilter();
        intentFilter.addAction("selectedInstrument");
        registerReceiver(mReceive,intentFilter);
        if(bluetoothIsOpen) {

            String sql="select bleAddress from instrument where inCode='"+instrumentCode+"'";
            String bleAddress="";
            Cursor cursor=db.query(sql);
            while (cursor.moveToNext()){
                if(cursor.isLast()){
                    bleAddress=cursor.getString(cursor.getColumnIndex("bleAddress"));
                }
            }
            cursor.close();
            Log.i("TAG","bleAddress:"+bleAddress);
            Log.i("TAG","bleAddress:"+"hhhhh"+bleAddress);
            if("".equals(bleAddress)||bleAddress==null) {
                scanBluetoothDevice();
            }
            else {
                connectDevice = mBluetoothAdapter.getRemoteDevice(bleAddress);
                if(connectDevice!=null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //Android6.0以上需要调用这个方法，如果6.0以上不用这个方法而是选择下面那个方法会出现133错误
                        bluetoothGatt = connectDevice.connectGatt(TrainActivity.this, false, bluetoothGattCallback, TRANSPORT_LE);
                    } else {
                        bluetoothGatt = connectDevice.connectGatt(TrainActivity.this, false, bluetoothGattCallback);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTitle("正在与"+connectDevice.getName()+"建立连接...");
                        }
                    });
                }
                else {
                    scanBluetoothDevice();
                    Log.i("TAG","connectDevice==null");
                }
            }

            //scanBluetoothDevice();
        }
        lv_trainShowList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0) return;
                if(status==0){
                    connectBle(listDevice.get(position).get(1).toString());
                }
                else {
                    return;
                }
            }
        });
        //图表操作
        initChart(lineChart);
        LineData lineData = new LineData();
        lineChart.setData(lineData);
    }
    private BroadcastReceiver mReceive=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if("selectedInstrument".equals(action)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //initBluetoothBle();
                    }
                });

                Log.i("TAG","收到广播");
            }
        }
    };
    //初始化图表
    private void initChart(LineChart lineChart) {
        /*****图表设置*****/
        //是否展示网格线
        lineChart.setDrawGridBackground(false);
        lineChart.setBackgroundColor(Color.WHITE);
        //是否显示边界
        lineChart.setDrawBorders(false);
        //是否可以拖动
        lineChart.setDragEnabled(true);
        //是否有触法事件
        lineChart.setTouchEnabled(true);
        //设置xy轴动画效果
        lineChart.animateY(2500);
        lineChart.animateX(1500);

        /***xy 轴***/
        xAxis = lineChart.getXAxis();
        leftYAxis = lineChart.getAxisLeft();
        rightYAxis = lineChart.getAxisRight();
        //禁止xy网格线
        xAxis.setDrawGridLines(false);
        leftYAxis.setDrawGridLines(true);
        rightYAxis.setDrawGridLines(false);
        //x y轴网格线为虚线（实体线长度，间隔长度，偏移量
        leftYAxis.enableAxisLineDashedLine(10f,10f,0f);
        //右侧Y轴去掉
        rightYAxis.setEnabled(false);
        //x轴设置显示在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        //保证y轴从0开始，不然会上移
        leftYAxis.setAxisMinimum(0f);
        rightYAxis.setAxisMinimum(0f);

        /**折线图列 标签 设置*****/
        legend = lineChart.getLegend();
        //设置显示类型 EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);

    }

    /**
     * 曲线初始化设置 一个LineDataSet 代表一条曲线
     *
     * @param lineDataSet 线条
     * @param color       线条颜色
     * @param mode
     */
    private void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        //设置曲线值的圆点是实心还是空心
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(5f);
        //设置折线图填充
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        if (mode == null) {
            //设置曲线展示为圆滑曲线（如果不设置则默认折线）
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }
    }
    /**
     * 展示曲线
     *
     * @param dataList 数据集合
     * @param name     曲线名称
     * @param color    曲线颜色
     */
    public void showLineChart(List<String> dataList, String name, int color) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 20; i++) {

            /**
             * 在此可查看 Entry构造方法，可发现 可传入数值 Entry(float x, float y)
             * 也可传入Drawable， Entry(float x, float y, Drawable icon) 可在XY轴交点 设置Drawable图像展示
             */
            Entry entry = new Entry(i, (float) i);//(x,y)
            entries.add(entry);
        }
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

    }
    /**
     * 添加曲线
     */
    private void addLine(String name, int color) {
        List<Entry> entries = new ArrayList<>();
        //for (int i = 0; i < 1; i++) {
        //    Entry entry = new Entry(i, (float)i);
       //     entries.add(entry);
       // }
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
        lineChart.getLineData().addDataSet(lineDataSet);
        lineChart.invalidate();

    }
    //数据库准备
    private void initDatabase(){
        db=new databaseDao(TrainActivity.this);
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
            new AlertDialog.Builder(TrainActivity.this).setTitle("提示").setMessage("没有选定ble服务uuid和特征uuid,请前往蓝牙设置进行设置")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }
        while (cursor.moveToNext()){
            int index = cursor.getColumnIndex("serverUUID");
            ServerUUID= UUID.fromString(cursor.getString(index));
            index = cursor.getColumnIndex("charUUID");
            CharUUID=UUID.fromString(cursor.getString(index));

        }
        cursor.close();



        Cursor curso=db.query("select count from histroy where userId='"+user.userId+"' order by count");
        String maxCount="";
        Log.i("TAG","select max(count) from histroy where userId='"+user.userId+"'");
        while (curso.moveToNext()){
            maxCount=curso.getString(curso.getColumnIndex("count"));
        }
         curso.close();
        Cursor curso2=db.query("select count from histroy where userId='"+user.userId+"' " +
                "and startTime= (select max(startTime) from histroy where userId='"+user.userId+"') ");
        String lastCount="";
        while (curso2.moveToNext()){
            lastCount=curso2.getString(curso2.getColumnIndex("count"));
        }
        curso2.close();
        if(maxCount.equals(""))
        {
            lastCount="无";
            maxCount="无";
        }
        View view = getLayoutInflater().inflate(R.layout.train_count, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("输入训练次数：")
                .setView(view)
                .setMessage("上次训练次数为："+lastCount+",记录中最大的训练次数为："+maxCount+",默认值为月、日、时、分")
                .create();
        final EditText et_trainCount=(EditText)view.findViewById(R.id.et_trainCount);
        SimpleDateFormat dateformat = new SimpleDateFormat("MMddHHmm");
        String dateStr = dateformat.format(System.currentTimeMillis());
        try{
            count=Integer.parseInt(dateStr);
        }catch (Exception e){
            showMessage("转换失败");
            return;
        }
        et_trainCount.setHint("默认值："+dateStr);
        TextView tv_tranCountCancel=(TextView)view.findViewById(R.id.tv_trainCountCancel);
        TextView tv_tranCountOk =(TextView)view.findViewById(R.id.tv_trainCountOk);
        tv_tranCountCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_tranCountOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number=et_trainCount.getText().toString().trim();
                try{
                    if(!number.equals("")){
                        count=Integer.parseInt(number);
                    }

                }catch (Exception e){
                    showMessage("请输入数字");
                    return;
                }
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void initBluetoothBle(){
        Log.i("TAG","initBlue");
        checkPermissions();
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                showMessage("设备不支持蓝牙");
                finish();
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            showMessage("设备不支持蓝牙");
            finish();
        }
        if(!mBluetoothAdapter.isEnabled()){
          //  bluetoothIsOpen=false;
            //mBluetoothAdapter.enable();
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            TrainActivity.this.startActivityForResult(intent,1);
        }
       else {
            bluetoothIsOpen=true;
        }


    }
    //
    private void init() {
        bt_trainReConnect = (Button) findViewById(R.id.bt_trainReConnect);
        bt_trainReScan = (Button) findViewById(R.id.bt_trainReScan);
        bt_trainStopConnect = (Button) findViewById(R.id.bt_trainStopConnect);
        bt_trainStopScan = (Button) findViewById(R.id.bt_trainStopScan);
        bt_trainStopTrain=(Button) findViewById(R.id.bt_trainStopTrain);
        bt_trainStartTrain=(Button)findViewById(R.id.bt_trainStartTrain);
        bt_trainSendMsg=(Button)findViewById(R.id.bt_trainSendMsg) ;
        et_trainSendMsg=(EditText)findViewById(R.id.et_trainSendMsg);
        lv_trainShowList=(ListView)findViewById(R.id.lv_trainShowList);
        lineChart=(LineChart)findViewById(R.id.lineChart);
    }
    private void setOnclick(){
        OnClick onClick=new OnClick();
        bt_trainStopScan.setOnClickListener(onClick);
        bt_trainReScan.setOnClickListener(onClick);
        bt_trainStopConnect.setOnClickListener(onClick);
        bt_trainReConnect.setOnClickListener(onClick);
        bt_trainStartTrain.setOnClickListener(onClick);
        bt_trainStopTrain.setOnClickListener(onClick);
        bt_trainSendMsg.setOnClickListener(onClick);

    }
    private class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.bt_trainReScan://重新扫描
                    if(bluetoothGatt!=null){
                        bluetoothGatt.close();
                    }
                    isConnected=false;
                    lv_trainShowList.setAdapter(listDeviceAdapter);
                    scanBluetoothDevice();
                    status=0;
                    break;
                case R.id.bt_trainStopScan://停止扫描
                    if(scanner!=null){
                        scanner.stopScan(scanCallback);
                        scanner=null;
                        setTitle("停止扫描");
                    }
                    else setTitle("已经停止扫描");
                    isConnected=false;
                    status=0;
                    break;
                case R.id.bt_trainReConnect://重新连接
                    lv_trainShowList.setAdapter(listTrainResultAdapter);
                    if(bluetoothGatt!=null) {
                       // bluetoothGatt.disconnect();
                        bluetoothGatt.close();
                        //bluetoothGatt=null;
                    }
                    if(connectDevice==null){
                        showMessage("设备没有连接过，不能重新连接");
                        return;
                    }
                    setTitle("重新连接...");
                    bluetoothGatt=null;
                    connectDevice = mBluetoothAdapter.getRemoteDevice(connectDevice.getAddress());
                    //.connect();
                    isConnected=false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //Android6.0以上需要调用这个方法，如果6.0以上不用这个方法而是选择下面那个方法会出现133错误
                        bluetoothGatt = connectDevice.connectGatt(TrainActivity.this, false, bluetoothGattCallback,TRANSPORT_LE);
                    }else{
                        bluetoothGatt= connectDevice.connectGatt(TrainActivity.this, false, bluetoothGattCallback);
                    }
                    status=1;
                    break;
                case R.id.bt_trainStopConnect://断开连接
                   // listDevice.clear();
                    ///itemCount=0;

                    status=1;
                    if(connectDevice==null){
                        showMessage("还没有连接过,无需断开");
                        return;
                    }
                    if(bluetoothGatt==null){
                        showMessage("已经断开连接");
                        return;
                    }
                    // bluetoothGatt.disconnect();
                    bluetoothGatt.close();
                    setTitle("断开连接");
                    isConnected=false;
                    break;
                case R.id.bt_trainStopTrain://结束训练
                    if(train==true) {
                        train = false;
                        setTitle("结束训练");
                        showMessage("结束训练");
                        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateStr = dateformat.format(System.currentTimeMillis());
                        db.update("update histroy set stopTime='" + dateStr + "' where userId='" + user.userId + "' " +
                                "and count='" + count + "'");
                    }
                    else showMessage("还未开始训练，无需停止");
                    break;
                case R.id.bt_trainStartTrain://开始训练
                    if(instrumentCode.equals("")){
                        showMessage("未选择仪器");
                        finish();
                    }
                    if(count==-1){
                        showMessage("没有输入训练次数");
                        finish();
                    }
                    if(train==false) {
                        train = true;
                        if (connectDevice == null) {
                            showMessage("蓝牙未连接");
                            return;
                        }

                        if (bluetoothGatt == null) {
                            showMessage("蓝牙未连接,准备好");
                            return;
                        }
                        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateStr = dateformat.format(System.currentTimeMillis());
                        db.update("update histroy set startTime='" + dateStr + "' where userId='" + user.userId + "' " +
                                "and count='" + count + "'");
                        //bluetoothGatt.readCharacteristic(characteristic);
                        // bluetoothGatt.setCharacteristicNotification(characteristic,true);
                        lineChart.clearValues();
                        map.clear();
                        lineMap.clear();
                        listDevice.clear();

                        itemCount=0;
                        lineCount=0;
                        startTime=System.currentTimeMillis();
                        listTrainResultAdapter.bindData(listDevice);
                        lv_trainShowList.setAdapter(listTrainResultAdapter);
                        List<String> li=new ArrayList<>();
                        li.add("项目");
                        li.add("结果");
                        li.add("单位");
                        li.add("参照");
                        li.add("判断");
                        listDevice.add(li);
                        listTrainResultAdapter.notifyDataSetChanged();
                        setTitle("开始训练");
                        showMessage("开始训练");
                    }
                    else showMessage("已在训练中");
                    break;
                case R.id.bt_trainSendMsg:{//发送消息
                    final String msg= et_trainSendMsg.getText().toString().trim();
                    if(msg.equals("")){
                        showMessage("消息为空");
                        return;
                    }
                    if(connectDevice==null){
                        showMessage("蓝牙未连接");
                        return;
                    }

                    if(bluetoothGatt==null){
                        showMessage("蓝牙未连接,准备好");
                        return;
                    }
                    if(isConnected==false){
                        showMessage("蓝牙未连接成功");
                        return;
                    }
                    characteristic=bluetoothGatt.getService(ServerUUID).getCharacteristic(CharUUID);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WriteValue(msg);
                        }
                    });
                    break;
                }
            }
        }
    };
    //扫描蓝牙
    private void scanBluetoothDevice(){
        listDevice.clear();
        List<String> li=new ArrayList<>();
        li.add("蓝牙列表:");
        li.add("");
        listDevice.add(li);
        listDeviceAdapter.notifyDataSetChanged();
        if (scanner!=null) {
            scanner.stopScan(scanCallback);
        }

        scanner=mBluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(scanCallback);//蓝牙开始进行扫描
        setTitle("正在扫描...");
    }

    //扫描回调
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i("TAG","fhoeh");
            BluetoothDevice device = result.getDevice();
            List<String> list=new ArrayList<>();
            if(device.getName()==null){
                list.add("null");
            }
            else list.add(device.getName());
            list.add(device.getAddress());
            Log.i("TAG",list.toString());
            if (!listDevice.contains(list)) {  //判断是否已经添加
                showMessage("发现设备："+list.get(0).toString());
                listDevice.add(list);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listDeviceAdapter.notifyDataSetChanged();
                    }
                });

            }
        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            showMessage("搜索失败代码errorCode："+errorCode);
            Log.i("TAG","搜索失败代码errorCode："+errorCode);
        }
    };
    //连接回调

    private BluetoothGattCallback bluetoothGattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("TAG","onConnectionStateChange");
            String intentAction;
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                bluetoothGatt.discoverServices();
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    String sql="select * from histroy where userId='"+user.userId+"' and count= '"+count+"'";
                    Cursor cursor=db.query(sql);
                    if(cursor.getCount()<=0){
                        if(!db.insert("insert into histroy(userId,count) values('"+user.userId+"','"+count+"')")){
                            showMessage("插入第"+count+"次训练记录失败");
                        }
                        else Log.i("TAG","insert count="+count);
                    }
                    cursor.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTitle("连接成功，正在寻找服务");

                        }
                    });

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTitle("断开连接");
                        }
                    });
                }
            }
            else   runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTitle("连接失败");
                }
            });

        }
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i("TAG","onServicesDiscovered");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
              //  setCharacteristicNotification(characteristic,true);
               // findService(gatt.getServices());
              characteristic=gatt.getService(ServerUUID).getCharacteristic(CharUUID);
              if(characteristic!=null){
                  bluetoothGatt.readCharacteristic(characteristic);
                  bluetoothGatt.setCharacteristicNotification(characteristic,true);
                  isConnected=true;
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          setTitle("连接成功，可进行数据传输");
                      }
                  });
              }else {
                  isConnected=false;
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          setTitle("连接失败，设置-》蓝牙检测 进行测试");
                      }
                  });
              }

            } else {
                showMessage("没有发现服务，不广播，status= " + status);
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
                            showMessage("无数据传过来");
                        else
                            showMessage(msg);
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
                                          final BluetoothGattCharacteristic characteristic, final int status) {//发送数据时调用
            Log.i("TAG", "数据发送了哦");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //showMessage2("onCharacteristicWrite中");
                    if(status == BluetoothGatt.GATT_SUCCESS){//写入成功
                        showMessage("发送成功");
                         //setCharacteristicNotification(characteristic,true);

                    }else if (status == BluetoothGatt.GATT_FAILURE){
                        showMessage("发送失败");
                    }else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED){
                        showMessage("没权限");
                    }
                }
            });


        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {//descriptor读
            Log.e("onCDescripticRead中", "数据接收了哦");
            showMessage("onDescriptorRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {// Characteristic 改变，数据接收会调用
            if(train) {
                final String msg = new String(characteristic.getValue());
                String msg2 = msg.substring(1, msg.length() - 1);
                if (msg2.contains(",")) {
                    String[] msg3 = msg2.split(",");
                    {
                        for (int i = 0; i < msg3.length; i++) {
                            dealData(msg3[i].toString().trim());
                        }
                    }
                } else {
                    dealData(msg2);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {//descriptor写
            showMessage("onDescriptorWrite");
            Log.i("TAG","onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            showMessage("onReliableWriteCompleted(");
            Log.i("TAG","onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) { //读Rssi
            showMessage("onReadRemoteRssi");
            Log.i("TAG","onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, final int mtu, final int status) {
            Log.i("TAG","onMtuChanged");
            if(status==0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage("MTU改变:"+mtu);
                        Log.i("TAG","MTU改变:"+mtu+"Status:"+status);
                    }
                });

            }
            else  Log.i("TAG","MTU没有改变:"+mtu+"Status:"+status);
        }
    };
    private void dealData(String msg2){
        String itemCode="";
        String itemType="";
        String itemName="";
        String itemUnit="";
        String itemReference="";
        String itemMax="";
        String itemMin="";
        String lineColor="";
        int LineColor=0;
        List<String> list=new ArrayList<>();
        String[] msg3=msg2.split(":");
        //list.add("原始数据："+msg2);
        //list.add(msg3[0]);
        String sql="select itemCode,itemName,itemResultType,itemUnit,itemReference,itemMax,itemMin,lineColor from instrumentItem where inCode='"+instrumentCode+"' and  channelCode='"+msg3[0].trim()+"';";
        Cursor cursor=db.query(sql);
        if(cursor.getCount()<=0){
            //list.add("项目设置里-》项目匹配，没有匹配或错误,数据进行舍弃");
           // showMessage("无此匹配:"+msg3[0]);
            //listDevice.add(list);
            //listDeviceAdapter.notifyDataSetChanged();
            return;
        }
        while(cursor.moveToNext()) {
            int index = cursor.getColumnIndex("itemCode");
            itemCode = cursor.getString(index);
            index = cursor.getColumnIndex("itemName");
            itemName = cursor.getString(index);
            index = cursor.getColumnIndex("itemResultType");
            itemType = cursor.getString(index);
            index = cursor.getColumnIndex("itemUnit");
            itemUnit = cursor.getString(index);
            index = cursor.getColumnIndex("itemReference");
            itemReference = cursor.getString(index);
            index = cursor.getColumnIndex("itemMax");
            itemMax = cursor.getString(index);
            index = cursor.getColumnIndex("itemMin");
            itemMin = cursor.getString(index);
            index = cursor.getColumnIndex("lineColor");
            lineColor = cursor.getString(index);
            break;
        }
        cursor.close();
        if(itemUnit==null){
            itemUnit="";
        }
        if(itemReference==null){
            itemReference="";
        }
        Log.i("TAG","数字数据插矮三分阿尔菲啊啊啊我发发WAFA F AEF 入成功"+itemType);
        switch (itemType){
            case "1":
                sql="insert into result(userId,inCode,itemCode,itemResultNum,count,status,dataTime) values('"
                        + user.userId+"','"+instrumentCode+"','"+itemCode+"','"+msg3[1]+"','"+count+"','"+1+"','"+""+new Timestamp(System.currentTimeMillis())+"');";
                if(db.insert(sql)){
                    //Log.i("TAG","数字数据插入成功");
                    //list.add("处理数字型结果："+itemName+":" + msg3[1]);
                }
                else{
                    showMessage("数据保存失败");
                }
                break;
            case "3":
                sql="insert into result(userId,inCode,itemCode,itemResultBlob,count,status,dataTime) values('"
                        + user.userId+"','"+instrumentCode+"','"+itemCode+"','"+msg3[1]+"','"+count+"','"+1+"','"+""+new Timestamp(System.currentTimeMillis())+"');";
                if(db.insert(sql)){
                     //Log.i("TAG","布尔数据插入成功");
                   // list.add("处理布尔型结果："+itemName+":" + msg3[1]);
                }
                else{
                    showMessage("数据保存失败");
                }
                break;

            default:
                sql="insert into result(userId,inCode,itemCode,itemResultString,count,status,dataTime) values('"
                        + user.userId+"','"+instrumentCode+"','"+itemCode+"','"+msg3[1]+"','"+count+"','"+1+"','"+""+new Timestamp(System.currentTimeMillis())+"');";
                if(db.insert(sql)){
                   // Log.i("TAG","字符数据插入成功");
                   // list.add("处理字符结果："+itemName+":" + msg3[1]);
                }
                else{
                    showMessage("数据保存失败");
                }
                break;
        }
         LineData data=lineChart.getData();
        //处理结果新增
        Log.i("TAG","次数："+itemCount+":"+lineCount);
        if(!map.containsKey("#title#")){
            map.put("#title#","#title#");

        }
        if(!map.containsValue(itemName)){
            Log.i("TAG","添加："+msg2);

            map.put(""+itemCount,itemName);

            list.add(itemName);
            list.add(msg3[1]);//结果
            if(itemUnit=="null")
            {
                list.add("");
            }
            else list.add(itemUnit);

            Log.i("TAG","添加itemType："+itemType);
            switch (itemType) {
                case "1"://数字类型

                    list.add("区间："+itemMin+"-"+itemMax);
                    Double max,min,result;
                    try{
                        max=Double.parseDouble(itemMax);
                        min=Double.parseDouble(itemMin);
                        result=Double.parseDouble(msg3[1]);
                        Float.parseFloat(msg3[1]);
                    } catch (Exception e){
                        showMessage("结果不为数字："+msg3[0]+"="+msg3[1]);
                        list.add("转换错误");
                        break;
                    }
                    try{
                        LineColor=Integer.parseInt(lineColor);
                    }catch (Exception e){
                        LineColor=0;
                    }
                    addLine(itemName,LineColor);
                    Entry entryh = new Entry((System.currentTimeMillis()-startTime)/500,Float.parseFloat(msg3[1]));
                    //data.addEntry(entryh,itemCount);
                    data.addEntry(entryh,lineCount);
                    lineMap.put(""+itemCount,""+lineCount);
                    lineCount++;
                    if(result>max) {
                        list.add("偏高");

                    }
                    if(result<min){
                        list.add("偏低");
                    }
                    if(result>=min&&result<=max){
                        list.add("正确");
                    }
                    break;
                case "3":
                    list.add("标准值："+itemReference);
                    if(msg3[1].equals(itemReference)){
                        list.add("正确");
                    }
                    else list.add("错误");
                    break;
                case "2":
                    list.add("参考："+itemReference);
                    if(msg3[1].equals(itemReference)){
                        list.add("正确");
                    }
                    else list.add("异常");
                    break;
                 case "0":
                     list.add("参考：无");
                      list.add("无");
                     break;
            }
            itemCount++;
            listDevice.add(list);
            Log.i("TAG","list="+list.toString());
        }

        //处理结果替换
        else {
            for(int i=0;i<map.size();i++){
                Log.i("TAG","替换itemType："+itemType);
                if(map.get(""+i).equals(itemName)){
                    //data=lineChart.getData();
                    //i+1 listDevice 0 为标题
                    listDevice.get(i+1).set(1,msg3[1]);
                    Log.i("TAG","替换itemType："+itemType);
                    switch (itemType) {
                        //数字类型
                        case "1":
                            Double max, min, result;
                            try {
                                max = Double.parseDouble(itemMax);
                                min = Double.parseDouble(itemMin);
                                result = Double.parseDouble(msg3[1]);
                                Entry entryh = new Entry((System.currentTimeMillis()-startTime)/500,Float.parseFloat(msg3[1]));
                                data.addEntry(entryh, Integer.parseInt(lineMap.get(""+i)));
                            } catch (Exception e) {
                                showMessage("结果不为数字：" + msg3[0] + "=" + msg3[1]);
                                listDevice.get(i+1).set(4, "转换错误");
                                break;
                            }

                            if (result > max) {
                                listDevice.get(i+1).set(4, "偏高");

                            }
                            if (result < min) {
                                listDevice.get(i+1).set(4, "偏低");
                            }
                            if (result >= min && result <= max) {
                                listDevice.get(i+1).set(4, "正确");
                            }
                            break;
                        //布尔类型
                        case "3":
                            //list.add("标准值："+itemReference);
                            if(msg3[1].equals(itemReference)){
                                listDevice.get(i+1).set(4,"正确");
                            }
                            else listDevice.get(i+1).set(4,"错误");
                            break;

                        //字符类型
                        case "2":
                            //list.add("参考："+itemReference);
                            if(msg3[1].equals(itemReference)){
                                listDevice.get(i+1).set(4,"正确");
                            }
                            else listDevice.get(i+1).set(4,"异常");
                            break;
                        case "0":
                            break;
                    }
                    Log.i("TAG","list="+listDevice.get(i).toString());
                    break;
                }
            }

        }

       // Log.i("TAG","listDevice="+listDevice.toString());
      /*  runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listTrainResultAdapter.notifyDataSetChanged();
                lineChart.notifyDataSetChanged();
                lineChart.moveViewToX(20 );
            }
        });

       */
        Message msg=handler.obtainMessage();
        msg.obj="数据接受成功";
        handler.sendMessage(msg);

    }
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Intent intent;
            switch ((String)msg.obj){
                case "数据接受成功":
                    listTrainResultAdapter.notifyDataSetChanged();
                    lineChart.notifyDataSetChanged();
                    lineChart.moveViewToX(20 );
                    Log.i("TAG","刷新");
                    Log.i("TAG",listDevice.toString());
                    break;
                default:

                    break;

            }
        };
    };
    public void findService(List<BluetoothGattService> gattServices) {
        bluetoothGatt.requestMtu(500);
        //showMessage("Count is:" + gattServices.size());
        for (BluetoothGattService gattService : gattServices)
        {
            List<String> serverList= new ArrayList<>();
            if(gattService.getUuid().toString().equalsIgnoreCase(ServerUUID.toString()))
            {
                showMessage("找到服务");
                setTitle("找到服务");
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
                        showMessage("找到特征");
                        Log.i("TAG","找到服务对应的特征，可以进行数据传输");
                        setTitle("找到服务对应的特征，可以进行数据传输");
                        isConnected=true;

                        CharUUID=gattCharacteristic.getUuid();
                        characteristic=null;
                        characteristic= gattCharacteristic;

                        setCharacteristicNotification(characteristic, true);
                        //bluetoothGatt.setCharacteristicNotification(characteristic,true);
                         Log.i("TAG","发现服务，读取数据"+new String(characteristic.getValue()));
                        return;
                    }
                    else{
                        setTitle("无特征UUID，请去 设置-》蓝牙检测设置");
                        ServerUUID=null;
                        CharUUID=null;
                        isConnected=false;
                        return;
                    }
                }
            }
        }
        setTitle("无服务UUID，请去 设置-》蓝牙检测设置");

    }
    //多线程容易出错
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
           setTitle("蓝牙没有初始化");
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
    private void connectBle(String bleAddress){
        connectDevice = mBluetoothAdapter.getRemoteDevice(bleAddress);
        new AlertDialog.Builder(TrainActivity.this).setTitle("提示")
                .setMessage("确定连接"+ connectDevice.getName()+ connectDevice.getAddress()+"?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(instrumentCode.equals("")){
                            showMessage("未选择仪器");
                            finish();
                        }
                        if(count==-1){
                            showMessage("没有输入训练次数");
                            finish();
                        }
                        setTitle("正在建立连接...");
                        listDevice.clear();
                        lv_trainShowList.setAdapter(listTrainResultAdapter);


                        if(bluetoothGatt!=null) {
                            //bluetoothGatt.disconnect();
                            bluetoothGatt.close();
                            // bluetoothGatt=null;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //Android6.0以上需要调用这个方法，如果6.0以上不用这个方法而是选择下面那个方法会出现133错误
                            bluetoothGatt = connectDevice.connectGatt(TrainActivity.this, false, bluetoothGattCallback,TRANSPORT_LE);
                        }else {
                            bluetoothGatt = connectDevice.connectGatt(TrainActivity.this, false, bluetoothGattCallback);
                        }
                        db.update("update instrument set bleAddress='"+connectDevice.getAddress()+"' where inCode='"+instrumentCode+"'");

                        listDevice.clear();
                        listDeviceAdapter.notifyDataSetChanged();
                        if(scanner!=null){
                            scanner.stopScan(scanCallback);
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

    }
    public void WriteValue(String strValue) {
        byte[] send=new byte[500];
        send=strValue.getBytes();

        if(characteristic==null||bluetoothGatt==null){
            showMessage("Characteristic is null");
            return;
        }
        characteristic.setValue(send);
        Log.i("TAg","CharUUId"+characteristic.getUuid());
        bluetoothGatt.writeCharacteristic(characteristic);
    }
    //消息提示
    private void showMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast= Toast.makeText(TrainActivity.this,message,Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            //mAdapter.findDeviceBlueTooth();

            String sql="select bleAddress from instrument where inCode='"+instrumentCode+"'";
            String bleAddress="";
            Cursor cursor=db.query(sql);
            while (cursor.moveToNext()){
                if(cursor.isLast()){
                    bleAddress=cursor.getString(cursor.getColumnIndex("bleAddress"));
                }
            }
            cursor.close();
            if(bleAddress.equals("")) {
                scanBluetoothDevice();
            }
            else {
                Log.i("TAG","bleAddress"+bleAddress);
                connectDevice=null;
                connectDevice = mBluetoothAdapter.getRemoteDevice(bleAddress);
                if(connectDevice!=null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //Android6.0以上需要调用这个方法，如果6.0以上不用这个方法而是选择下面那个方法会出现133错误
                        bluetoothGatt = connectDevice.connectGatt(TrainActivity.this, false, bluetoothGattCallback, TRANSPORT_LE);
                    } else {
                        bluetoothGatt = connectDevice.connectGatt(TrainActivity.this, false, bluetoothGattCallback);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTitle("正在与"+connectDevice.getName()+"建立连接...");
                        }
                    });
                }
                else {
                    scanBluetoothDevice();
                }
            }

        }

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
