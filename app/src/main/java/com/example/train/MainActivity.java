package com.example.train;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.train.activity.BluetoothActivity;
import com.example.train.activity.LoginActivity;
import com.example.train.activity.RegisterActivity;
import com.example.train.activity.ResultManagerActivity;
import com.example.train.activity.SetExperimentActivity;
import com.example.train.activity.SetMateActivity;
import com.example.train.activity.TrainActivity;
import com.example.train.activity.WoDeActivity;
import com.example.train.activity.WoDeHistoryActivity;
import com.example.train.activity.setRunActivity;
import com.example.train.adapter.shouYeListAdapter;
import com.example.train.adapter.userListeAdapter;
import com.example.train.constants.convertStream;
import com.example.train.constants.message;
import com.example.train.constants.user;
import com.example.train.database.databaseDao;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Spinner mSpineerSheZhi;
    private Button bt_xunLian,bt_woDe,bt_shouYe;
    private  SharedPreferences sharedPreferences;
    private ListView lv_userList;
    private List<List<String>> list=new ArrayList<>();//用户信息
    private List<String> shouYeList=new ArrayList<>();
    private userListeAdapter userListAdapter;
    private shouYeListAdapter shouYeListAdapter;
    private int clickPosition =0,clickCount;//连续点击三下可以改变用户与管理员
    private String instrumentCode="";
    private databaseDao db=null;
    private int status=0;//0首页状态，1 用户管理状态
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("CPR训练管理系统");
        db=new databaseDao(this);
        init();
        setClickListener();
        set();//点击设置进行页面跳转
        userListAdapter=new userListeAdapter(MainActivity.this);
        shouYeListAdapter=new shouYeListAdapter(this);

        sharedPreferences=super.getSharedPreferences("userInfo",MODE_PRIVATE);
       lv_userList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
           @Override
           public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
               if(position==0) return false;
               if(status==1) {
                   showMsg("" + position);
                   changUser(position);
               }
               return true;
           }
       });
       lv_userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              if(position==0) return;
               if(status==1) {//用户管理事件
                   if (clickPosition != position) {
                       clickPosition = position;
                       clickCount = 0;
                   }
                   if (clickPosition == position) {
                       clickCount++;
                   }
                   if (clickCount == 3) {
                       clickCount = 0;
                       changUserPower(position);
                   }
               }
               if(status==0){//首页
                   if(user.userPower.equals("1")){
                        Intent intent=new Intent(MainActivity.this,ResultManagerActivity.class);
                        startActivity(intent);
                   }
                   if(user.userPower.equals("0")){
                       Intent intent=new Intent(MainActivity.this, WoDeHistoryActivity.class);
                       startActivity(intent);
                   }
               }
           }
       });
        if(user.userId.equals(""))
        if(!sharedPreferences.getString("userId","").equals("")&&!sharedPreferences.getString("userId","").isEmpty()){
            user.userId=sharedPreferences.getString("userId","");
            user.userName=sharedPreferences.getString("userName","");
            user.userPhone=sharedPreferences.getString("userPhone","");
            user.userStatus=sharedPreferences.getString("userStatus","");
            user.userPower=sharedPreferences.getString("userPower","");
            user.userLogin=sharedPreferences.getString("userLogin","");

        }
        else {
            Intent intent= new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        if(user.userId!=""&&user.userLogin.equals("1")){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    shouYe();
                }
            },1000);
       }

    }
    private void init(){
        mSpineerSheZhi = (Spinner) findViewById(R.id.sp_sheZhi);
        bt_xunLian =(Button)findViewById(R.id.bt_xunLian);
        bt_shouYe =(Button)findViewById(R.id.bt_shouYe);
        bt_woDe=(Button)findViewById(R.id.bt_woDe);
        lv_userList=(ListView)findViewById(R.id.lv_userList);
    }
    private void setClickListener(){
        OnClick onClick = new OnClick();
        bt_woDe.setOnClickListener(onClick);
        bt_xunLian.setOnClickListener(onClick);
        bt_shouYe.setOnClickListener(onClick);
    }
    private class OnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()){
                case R.id.bt_xunLian:
                    xunLian();
                    break;
                case R.id.bt_shouYe:
                    status=0;
                    shouYe();
                    return;
                   //break;
                case R.id.bt_woDe:

                    if(user.userId.equals("")) {
                        if (!sharedPreferences.getString("userId", "") .equals("")&& !sharedPreferences.getString("userId", "").isEmpty()) {
                            user.userId = sharedPreferences.getString("userId", "");
                            user.userName = sharedPreferences.getString("userName", "");
                            user.userPhone = sharedPreferences.getString("userPhone", "");
                            user.userStatus = sharedPreferences.getString("userStatus", "");
                            user.userPower = sharedPreferences.getString("userPower", "");
                            user.userLogin = sharedPreferences.getString("userLogin", "");
                            intent = new Intent(MainActivity.this, WoDeActivity.class);
                        }
                        else intent= new Intent(MainActivity.this, LoginActivity.class);
                    }
                    else {
                        intent = new Intent(MainActivity.this, WoDeActivity.class);
                    }
                    startActivity(intent);
                    break;
                default:
                    break;
            }

        }
    }
    private void shouYe(){
        if(user.userId.equals("")){
            showMessage("请先登陆");
            return;
        }
        shouYeList.clear();
        shouYeListAdapter.bindData(shouYeList);
        lv_userList.setAdapter(shouYeListAdapter);
        shouYeListAdapter.notifyDataSetChanged();

        if(user.userLogin.equals("1")){
            JSONObject jsonObject=new JSONObject();
            if(user.userPower.equals("0")){
                try{
                    jsonObject.put("shouYeQuey","0");
                    jsonObject.put("userId",user.userId);
                }catch (Exception e){

                }

                shouYeQuery(jsonObject);
            }
            if(user.userPower.equals("1")){
                try{
                    jsonObject.put("shouYeQuey","1");
                    jsonObject.put("userId",user.userId);
                }catch (Exception e){

                }
                shouYeQuery(jsonObject);
            }
        }
    }
    private void xunLian(){
        if(user.userId.equals("")){
            showMessage("请先登陆");
            return;
        }
        String sql="select inCode,inName from instrument where status=1";
        Cursor cursor=db.query(sql);
        Log.i("TAG",sql);
        if(cursor.getCount()<=0){
            new AlertDialog.Builder(MainActivity.this).setTitle("提示").setMessage("没有仪器可以选择，请添加仪器")
                    .setIcon(R.mipmap.train)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent=new Intent(MainActivity.this,SetExperimentActivity.class);
                            startActivity(intent);
                        }
                    }).show();
            return;
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
        new AlertDialog.Builder(MainActivity.this).setTitle("选择仪器")
                .setIcon(R.mipmap.train)
                .setSingleChoiceItems(inName, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        selectPosition[0]=which;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        instrumentCode=inCode[selectPosition[0]];
                        Log.i("TAG","inCode:"+instrumentCode);
                        Intent  intent=new Intent(MainActivity.this, TrainActivity.class);
                        intent.putExtra("inCode",instrumentCode);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                }).show();

    }
    private void set(){
        mSpineerSheZhi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                userListAdapter.bindData(list);
                lv_userList.setAdapter(userListAdapter);
                userListAdapter.notifyDataSetChanged();
                list.clear();
                if(user.userId.equals("")){
                    showMessage("请先登陆");
                    return;
                }
                switch(position){
                    case 0:
                        break;
                    case 1://蓝牙检测
                        intent = new Intent(MainActivity.this, SetMateActivity.class);
                        startActivity(intent);
                        break;
                    case 2://实验项目
                        intent = new Intent(MainActivity.this, SetExperimentActivity.class);
                        startActivity(intent);
                        break;
                    case 3://用户管理
                        status=1;
                        if(user.userLogin.equals("0"))
                        {
                            showMsg("你没有进行远程登陆，无法进行操作");
                            return;
                        }
                        if(user.userPower.equals("1")) {
                            showMsg("长按列表或点击三下可进行操作");
                            UserAll();
                        }
                        else showMsg("你不是管理员，无法进行操作");
                        break;
                    case 4://成绩管理
                        if(user.userLogin.equals("0"))
                        {
                            showMsg("你没有进行远程登陆，无法进行操作");
                            return;
                        }
                        if(user.userPower.equals("1")) {
                            intent = new Intent(MainActivity.this, ResultManagerActivity.class);
                            startActivity(intent);
                        }
                        else showMsg("你不是管理员，无法进行操作");
                        break;
                    default:
                        break;

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void shouYeQuery(final JSONObject jsonUser){
        new Thread(){
            @Override
            public void run(){
                try{
                    final String path= message.path+"/train/commitResult";
                    URL url=new URL(path);
                    HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setReadTimeout(5000);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.getOutputStream().write(jsonUser.toString().getBytes());
                    if(conn.getResponseCode()==200){
                        InputStream inputStream=conn.getInputStream();
                        String s= convertStream.getTextFromStream(inputStream);
                        Log.i("TAG",s);
                        JSONObject json2=new JSONObject(s);
                        String result="";
                        if(json2.has("Result")) {
                            result = json2.getString("Result");
                        }

                        if(result.equals("shouYeQuerySuccess")) {

                            int i=0;
                            List<String> l=new ArrayList<>();
                            if(user.userPower.equals("1")) {
                                shouYeList.add("用户最新信息");
                            }else shouYeList.add("检查回复消息");

                            if(json2.getString("Count").equals("0")){
                                Message msg=handler.obtainMessage();
                                msg.obj="首页无数据";
                                handler.sendMessage(msg);
                                return;
                            }
                            JSONObject head=new JSONObject(json2.getString(""+i));
                            while(!json2.getString("Count").equals(""+i)){
                                JSONObject history=new JSONObject(json2.getString(""+i));
                                List<String> li=new ArrayList<>();
                                if(user.userPower.equals("1")) {
                                    String msg=history.getString("userName")+"在 "+history.getString("commitTime")+
                                            "提交了：”"+history.getString("title")+"”";
                                    shouYeList.add(msg);
                                }
                                if(user.userPower.equals("0")) {
                                    String msg=history.getString("userName")+"在 "+history.getString("commentTime")+
                                            "检查了：“"+history.getString("title")+" ”并回复了消息";
                                    shouYeList.add(msg);
                                }
                                i++;
                            }
                            Message msg = handler.obtainMessage();
                            msg.obj = "获取首页数据成功";
                            handler.sendMessage(msg);
                        }

                        if(result.equals("queryHistoryFaild")) {
                            Message msg = handler.obtainMessage();
                            msg.obj = "获取首页数据失败";
                            handler.sendMessage(msg);
                            showMessage("获取首页数据失败");
                        }

                        if(result.equals(" faild")){
                            showMessage("操作失败");
                        }
                    }
                    else showMessage("网络错误代码:"+conn.getResponseCode());
                }catch (Exception e){
                    showMessage("网络连接出现错误"+e.getMessage());
                    Log.i("TAG",e.getMessage());
                }
                super.run();
            }
        }.start();
    }
    private void UserAll(){
        new Thread(){
            @Override
            public void run(){
                try{
                    String path= message.path+"/train/UserAll";
                    URL url=new URL(path);
                    HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setReadTimeout(5000);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Charsert", "UTF-8");
                    Log.i("TAG","ResponseCode"+conn.getResponseCode());
                    if(conn.getResponseCode()==200){
                        InputStream inputStream=conn.getInputStream();
                        String s=getTextFromStream(inputStream);
                        Log.i("TAG","msg="+s);
                        JSONObject json=new JSONObject(s);
                        String result=json.getString("Result");
                        Log.i("TAG","result="+result);
                        if(result.equals("faild")){
                            showMsg("用户检索失败");
                        }
                        else{
                            int rowAll=0;
                            try {
                                rowAll=Integer.parseInt(result);
                            }catch (Exception e){
                                showMsg(e.getMessage());
                            }
                            List<String> li2= new ArrayList<>();
                            li2.add("用户ID");
                            li2.add("用户名");
                            li2.add("用户电话");
                            li2.add("状态");
                            li2.add("角色");
                            list.add(li2);
                            Log.i("TAG","jrowAll"+rowAll);
                            for(int i=0;i<rowAll;i++){
                                String json2=json.getString(""+i);
                                Log.i("TAG","json2"+json.toString());
                                JSONObject jsonMap=new JSONObject(json2);
                                Log.i("TAG",jsonMap.toString());
                                List<String> li= new ArrayList<>();
                                li.add(jsonMap.getString("userId"));
                                li.add(jsonMap.getString("userName"));
                                li.add(jsonMap.getString("userPhone"));
                                li.add(jsonMap.getString("userStatus"));
                                li.add(jsonMap.getString("userPower"));
                                list.add(li);
                                Message msg=handler.obtainMessage();
                                msg.obj="收到用户信息";
                                handler.sendMessage(msg);
                            }
                        }

                    }
                    else showMsg("网络错误代码:"+conn.getResponseCode());
                }catch (Exception e){
                    Log.i("TAG","errot"+e.getMessage());
                    showMsg(e.getMessage());
                }
                super.run();
            }
        }.start();
    }
    private void changUser(final int position){
        final List<String> li=list.get(position);
        //处理同意管理员申请
        if(li.get(3).toString().equals("2")){
            final JSONObject jsonUser=new JSONObject();

            new AlertDialog.Builder(MainActivity.this).setTitle("消息")
                    .setMessage("同意ID："+li.get(0)+" 名称："+li.get(1)+"成为管里员吗？")
                    .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                jsonUser.put("userId",li.get(0).toString());
                                jsonUser.put("userName", li.get(1).toString());
                                jsonUser.put("userPhone",li.get(2).toString());
                                jsonUser.put("userPassword","null");
                                jsonUser.put("userPower","1");
                                jsonUser.put("userStatus","1");

                            }catch (Exception e){
                                showMessage("数据解析出错");
                                return;
                            }
                            updateUserMsg(jsonUser,"同意管理员申请",position,"");

                        }
                    })
                    .setNegativeButton("不同意", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                jsonUser.put("userId",li.get(0).toString());
                                jsonUser.put("userName", li.get(1).toString());
                                jsonUser.put("userPhone",li.get(2).toString());
                                jsonUser.put("userPassword","null");
                                jsonUser.put("userPower","0");
                                jsonUser.put("userStatus","1");

                            }catch (Exception e){
                                return;
                            }
                            updateUserMsg(jsonUser,"不同意管理员申请",position,"");
                        }
                    }).show();
        }
       //处理改变用户状态
        else{
            final int[] selectPosition= new int[1];
            final String[] selectName={"停用","启用","允许改密码"};
            new AlertDialog.Builder(MainActivity.this).setTitle("选择操作")
                    .setIcon(R.mipmap.ic_launcher)
                    .setSingleChoiceItems(selectName, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            selectPosition[0]=which;
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final JSONObject jsonUser=new JSONObject();
                            if(selectPosition[0]==2) selectPosition[0]=3;
                            try {
                                jsonUser.put("userId", li.get(0).toString());
                                jsonUser.put("userName", li.get(1).toString());
                                jsonUser.put("userPhone", li.get(2).toString());
                                jsonUser.put("userPassword", "null");
                                jsonUser.put("userPower", li.get(4).toString());
                                jsonUser.put("userStatus", ""+selectPosition[0]);
                            }catch (Exception e){
                                showMessage("数据解析异常");
                                return;
                            }
                            updateUserMsg(jsonUser,"更改用户状态",position, ""+selectPosition[0]);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
}
    private void changUserPower(int position){
        JSONObject jsonUser=new JSONObject();
        List<String> li=list.get(position);
        String userPower="";
        try {
            jsonUser.put("userId", li.get(0).toString());
            jsonUser.put("userName", li.get(1).toString());
            jsonUser.put("userPhone", li.get(2).toString());
            jsonUser.put("userPassword", "null");
            jsonUser.put("userStatus", li.get(3).toString());

            if(li.get(4).toString().equals("0")) userPower="1";
            else userPower="0";

            jsonUser.put("userPower", userPower);
        }catch (Exception e){
            showMessage("数据解析异常");
            return;
        }
        updateUserMsg(jsonUser,"更改管理员",position,userPower);
    }
    private void updateUserMsg(final JSONObject jsonUser, final String selectMsg, final int position,final String values){
        new Thread(){
            @Override
            public void run(){
                try{
                    final String path= message.path+"/train/changUserMsg";
                    URL url=new URL(path);
                    HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setReadTimeout(5000);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    JSONObject json=new JSONObject();
                    Log.i("TAG","jsonUser="+jsonUser.toString());
                    json.put("UserMsg",jsonUser.toString());
                    conn.getOutputStream().write(json.toString().getBytes());
                    if(conn.getResponseCode()==200){
                        InputStream inputStream=conn.getInputStream();
                        String s=getTextFromStream(inputStream);
                        JSONObject json2=new JSONObject(s);
                        String result=json2.getString("Result");
                        Log.i("TAG","result="+result);
                        if(result.equals("success")){
                            Message msg=handler.obtainMessage();
                            List<String> li= list.get(position);
                            if(selectMsg.equals("同意管理员申请")){
                                msg.obj="同意管理员申请";
                                Log.i("TAG","同意=");
                               li.set(3,"1");
                               li.set(4,"1");
                                Log.i("TAG","同意="+list.get(position).get(4).toString());
                            }
                            if(selectMsg.equals("不同意管理员申请")){
                                msg.obj="不同意管理员申请";
                                list.get(position).set(3,"1");
                                list.get(position).set(4,"0");
                            }
                            if(selectMsg.equals("更改用户状态")){
                                msg.obj="更改用户状态";
                                list.get(position).set(3,values);
                            }
                            if(selectMsg.equals("更改管理员")){
                                msg.obj="更改管理员";
                                list.get(position).set(4,values);
                            }

                            handler.sendMessage(msg);

                        }
                        else{
                            showMessage("修改失败");
                        }
                    }
                    else showMessage("网络错误代码:"+conn.getResponseCode());
                }catch (Exception e){
                    showMessage("网络连接出现错误"+e.getMessage());
                }
                super.run();
            }
        }.start();
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Intent intent;
            switch ((String)msg.obj){
                case "收到用户信息":
                    userListAdapter.notifyDataSetChanged();
                    break;
                case "获取首页数据成功":
                    shouYeListAdapter.notifyDataSetChanged();
                    break;
                case "首页无数据":
                    shouYeListAdapter.notifyDataSetChanged();
                    break;
                default:

                    break;

            }
        };
    };

    private void showMsg(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });

    }
    private String getTextFromStream(InputStream is){

        try {
            int len = 0;
            byte[] b = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while((len = is.read(b)) != -1){
                bos.write(b, 0, len);
            }

            String text = new String(bos.toByteArray());
            bos.close();
            return text;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    private void showMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast=Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
}