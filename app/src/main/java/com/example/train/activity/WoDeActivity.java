package com.example.train.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.train.MainActivity;
import com.example.train.R;
import com.example.train.adapter.histroyListAdapter;
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
import java.util.List;

import static com.example.train.constants.user.userName;
import static com.example.train.constants.user.userPhone;
import static com.example.train.constants.user.userPower;

public class WoDeActivity extends AppCompatActivity {
    private Button bt_woDeHistory,bt_woDeResult,bt_woDeChangeMessage,bt_woDechangePassword,bt_woDeSave,bt_woDeCancel,bt_woDeExit;
    private EditText tv_woDe1,tv_woDe2,tv_woDe3;
    private int status=1;//1代表修改密码，2修改信息
    private  SharedPreferences sharedPreferences;
    private databaseDao db;
   private View view;
   private ListView lv_woDeLastTrain;
    private List<List<String>>  resultList=new ArrayList<>();
    private histroyListAdapter resultListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wo_de);
        sharedPreferences=super.getSharedPreferences("userInfo",MODE_PRIVATE);
        db=new databaseDao(this);

        setTitle("我的");
        init();
        setOnClick();
        resultListAdapter=new histroyListAdapter(this);
        resultListAdapter.bindData(resultList);
        lv_woDeLastTrain.setAdapter(resultListAdapter);
        queryLastTrain();
    }
    private void init(){
        bt_woDeHistory=(Button)findViewById(R.id.bt_woDeHistory);
        bt_woDeResult=(Button)findViewById(R.id.bt_woDeResult);
        bt_woDechangePassword=(Button)findViewById(R.id.bt_woDeChangePassword);
        bt_woDeChangeMessage=(Button)findViewById(R.id.bt_woDeChangeMessage);
        lv_woDeLastTrain=(ListView)findViewById(R.id.lv_woDeLastTrain);
        //bt_woDeSave=(Button)findViewById(R.id.bt_woDeSave);
       // bt_woDeCancel=(Button)findViewById(R.id.bt_woDeCancel);
        bt_woDeExit=(Button)findViewById(R.id.bt_woDeExit);
        //tv_woDe1=(EditText) findViewById(R.id.tv_woDe1);
       //tv_woDe2=(EditText) findViewById(R.id.tv_woDe2);
       // tv_woDe3=(EditText) findViewById(R.id.tv_woDe3);

    }

    private void setOnClick(){
        OnClick onClick= new OnClick();
        bt_woDeHistory.setOnClickListener(onClick);
        bt_woDeResult.setOnClickListener(onClick);
        bt_woDeChangeMessage.setOnClickListener(onClick);
        bt_woDechangePassword.setOnClickListener(onClick);
        //bt_woDeSave.setOnClickListener(onClick);
        //bt_woDeCancel.setOnClickListener(onClick);
        bt_woDeExit.setOnClickListener(onClick);
    }
    private class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()) {
                case R.id.bt_woDeHistory:
                    intent=new Intent(WoDeActivity.this,WoDeHistoryActivity.class);
                    startActivity(intent);
                    break;
                case R.id. bt_woDeResult:  //
                    intent = new Intent(WoDeActivity.this,WoDeResultActivity.class);
                    startActivity(intent);
                    break;
                case R.id.bt_woDeChangePassword://修改密码
                    status=1;
                    changeMsg();
                    /*
                    tv_woDe1.setVisibility(View.VISIBLE);
                    tv_woDe2.setVisibility(View.VISIBLE);

                    bt_woDeSave.setVisibility(View.VISIBLE);
                    bt_woDeCancel.setVisibility(View.VISIBLE);

                     */
                    break;
                case R.id.bt_woDeChangeMessage:
                    status=2;
                    changeMsg();
                    /*
                    tv_woDe1.setVisibility(View.VISIBLE);
                    tv_woDe2.setVisibility(View.VISIBLE);
                    tv_woDe3.setVisibility(View.VISIBLE);
                    if(!user.userId.equals("")){
                        tv_woDe1.setEnabled(false);
                        tv_woDe1.setText("用户ID："+user.userId);
                        tv_woDe2.setText(userName);
                        tv_woDe3.setText(user.userPhone);
                    }
                    bt_woDeSave.setVisibility(View.VISIBLE);
                    bt_woDeCancel.setVisibility(View.VISIBLE);

                     */
                    break;
                //case R.id.bt_woDeSave:
                    //woDeSave(tv_woDe1.getText().toString(),tv_woDe2.getText().toString());

                  //  break;
               // case R.id.bt_woDeCancel:
                 //   break;
                case R.id.bt_woDeExit:
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.clear();
                    editor.commit();
                    user.userId="";
                    user.userPhone="";
                    userName="";
                   // intent=new Intent(WoDeActivity.this, MainActivity.class);
                    //startActivity(intent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    }
    //用户信息修改保存
    private void woDeSave(String msg,String msg2){
        if(status==1){
            String passworld=msg;
            Log.i("TAG","password:"+passworld);
            String passworld2=msg2;
            if(!passworld.equals(passworld2)||passworld.equals("")){
                Toast.makeText(WoDeActivity.this,"密码不一致",Toast.LENGTH_LONG).show();
                changeMsg();
                return;
            }
            if(user.userLogin.equals("0")) {//本地修改
                String sql = "update user set userPassword='" + tv_woDe1.getText().toString() + "' where userId ='" + user.userId + "'";
                if (db.update(sql)) {
                   showMessage("修改成功");
                } else {
                    showMessage("提交失败");
                    return;
                }
            }
            if(user.userLogin.equals("1")){
                changeRemotePassword(passworld);
            }
        }
        if(status==2){
            String userName=msg.trim();
            String userPhone=msg2.trim();
            if(userName.equals("")||userPhone.equals("")){
                Toast.makeText(WoDeActivity.this,"输入内容",Toast.LENGTH_LONG).show();
                changeMsg();
                return;
            }
            if(user.userLogin.equals("0")) {
                String sql = "update user set userName='" + userName + "', userPhone='" + userPhone + "' where userId ='" + user.userId + "'";
                if (db.update(sql)) {
                    user.userName = userName;
                    user.userPhone = userPhone;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userName", userName);
                    editor.putString("userPhone", userPhone);
                    editor.commit();
                   showMessage("修改成功");
                } else {
                   showMessage("提交失败");
                    return;
                }
            }
            if(user.userLogin.equals("1")){
                changeUserMsg(userName,userPhone);
            }
        }

    }
    private void changeRemotePassword(final String userPassword){
        new Thread(){
            @Override
            public void run(){
                try{
                    final String path= message.path+"/train/Password";

                    URL url=new URL(path);
                    HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("POST");
                    JSONObject map=new JSONObject();
                    map.put("userId",user.userId);
                    map.put("userName", user.userName);
                    map.put("userPhone",user.userPhone);
                    map.put("userPassword",userPassword);
                    map.put("userPower",""+user.userPower);
                    map.put("userStatus",user.userStatus);

                    conn.setReadTimeout(5000);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    JSONObject json=new JSONObject();
                    json.put("Password",map.toString());
                    conn.getOutputStream().write(json.toString().getBytes());
                    Log.i("TAG","ResponseCode"+conn.getResponseCode());
                    if(conn.getResponseCode()==200){
                        InputStream inputStream=conn.getInputStream();
                        String s=getTextFromStream(inputStream);
                        Log.i("TAG","msg="+s);
                        JSONObject json2=new JSONObject(s);
                        String result=json2.getString("Result");
                        Log.i("TAG","result="+result);
                        if(result.equals("success")){
                            Message msg=handler.obtainMessage();
                            msg.obj="密码修改成功";
                            user.userPassword=userPassword;
                            handler.sendMessage(msg);
                        }
                        else{
                            showMessage("修改失败");
                        }
                    }
                    else showMessage("网络错误代码:"+conn.getResponseCode());
                }catch (Exception e){
                    showMessage(e.getMessage());
                }
                super.run();
            }
        }.start();
    }
    private void changeUserMsg(final String userName,final String userPhone){
        new Thread(){
            @Override
            public void run(){
                try{
                    final String path= message.path+"/train/changUserMsg";

                    URL url=new URL(path);
                    HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("POST");
                    JSONObject map=new JSONObject();
                    map.put("userId",user.userId);
                    map.put("userName", userName);
                    map.put("userPhone",userPhone);
                    map.put("userPassword",user.userPassword);
                    map.put("userPower",""+user.userPower);
                    map.put("userStatus",user.userStatus);

                    conn.setReadTimeout(5000);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    JSONObject json=new JSONObject();
                    json.put("UserMsg",map.toString());
                    conn.getOutputStream().write(json.toString().getBytes());
                    Log.i("TAG","ResponseCode"+conn.getResponseCode());
                    if(conn.getResponseCode()==200){
                        InputStream inputStream=conn.getInputStream();
                        String s=getTextFromStream(inputStream);
                        Log.i("TAG","msg="+s);
                        JSONObject json2=new JSONObject(s);
                        String result=json2.getString("Result");
                        Log.i("TAG","result="+result);
                        if(result.equals("success")){
                            Message msg=handler.obtainMessage();
                            msg.obj="用户修改成功";
                            user.userName=userName;
                            user.userPhone=userPhone;
                            handler.sendMessage(msg);
                        }
                        else{
                            showMessage("修改失败");
                        }
                    }
                    else showMessage("网络错误代码:"+conn.getResponseCode());
                }catch (Exception e){
                    showMessage(e.getMessage());
                }
                super.run();
            }
        }.start();
    }
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch ((String)msg.obj){
                case "密码修改成功":{
                    showMessage("密码修改成功");
                    String sql = "update user set userPassword='" + tv_woDe1.getText().toString() + "' where userId ='" + user.userId + "'";
                    if (db.update(sql)) {
                        Toast.makeText(WoDeActivity.this, "修改成功", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(WoDeActivity.this, "本地提交失败", Toast.LENGTH_LONG).show();
                        return;
                    }

                }
                    break;
                case "用户修改成功": {

                    String sql = "update user set userName='" + userName + "', userPhone='" + userPhone + "' where userId ='" + user.userId + "'";
                    if (db.update(sql)) {
                        user.userName = userName;
                        user.userPhone = userPhone;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("userName", userName);
                        editor.putString("userPhone", userPhone);
                        editor.commit();
                        Toast.makeText(WoDeActivity.this, "修改成功", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(WoDeActivity.this, "提交失败", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                    break;
            }

        };
    };

    private void changeMsg(){
        String title="";
        view = getLayoutInflater().inflate(R.layout.wo_de_chang_user, null);
        tv_woDe1=(EditText)view.findViewById(R.id.tv_woDeChangeUser1);
        tv_woDe2=(EditText)view.findViewById(R.id.tv_woDeChangeUser2);
        if(status==1){
            title= "用户:"+user.userName+"  修改密码";
            tv_woDe1.setHint("输入密码");
            tv_woDe2.setHint("再次输入密码");
        }
        if(status==2){
            title= "用户Id:"+user.userId+"  修改信息";
            tv_woDe1.setText(user.userName);
            tv_woDe2.setText(user.userPhone);
        }

         new AlertDialog.Builder(this).setTitle(title)
                // .setIcon(R.mipmap.ic_launcher)
                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface,
                                        int paramAnonymousInt) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            woDeSave(tv_woDe1.getText().toString(),tv_woDe2.getText().toString());
                        }
                    });


                    }
                }).create().show();
    }
    private void queryLastTrain(){
        //db.delete("delete from histroy");
        //db.insert("insert into histroy(userId,count) values('"+user.userId+"','3')");
        String sqlDataTime="select count from histroy where userId='"+user.userId+"' " +
                "and startTime= (select max(startTime) from histroy where userId='"+user.userId+"') ";
        Cursor maxTime= db.query(sqlDataTime);
        String count="";
        while (maxTime.moveToNext()){
            count=maxTime.getString(maxTime.getColumnIndex("count"));
            Log.i("TAG",count);
        }

        String sql="select result.count,instrumentItem.itemName,result.itemResultNum,result.itemResultString," +
                "result.itemResultBlob,instrumentItem.itemResultType,instrumentItem.itemReference,instrumentItem.itemMax," +
                "instrumentItem.itemMin from result,instrumentItem " +
                "where instrumentItem.itemcode=result.itemCode and result.userId='"+ user.userId+"' and result.count='"+count+"' order by instrumentItem.itemName,result.dataTime desc";
        queryAllResult(sql,0);
    }
    private void queryAllResult(String sql,int status){
        Cursor cursor=db.query(sql);
        List<String> l=new ArrayList<>();
        if(status==1) {
            l.add("时间");
        }else l.add("次数");
        l.add("项目：结果        参考值");
        l.add("成绩    ");
        resultList.add(l);
        while (cursor.moveToNext())
        {

            List<String> li=new ArrayList<>();
            if(status==1) {
                li.add(cursor.getString(cursor.getColumnIndex("dataTime")));
            }else li.add(cursor.getString(cursor.getColumnIndex("count")));
            String result=cursor.getString(cursor.getColumnIndex("itemName"))+":";
            String resultType= cursor.getString(cursor.getColumnIndex("itemResultType"));
            if(resultType.equals("1")) {//数字类型结果
                result+=cursor.getString(cursor.getColumnIndex("itemResultNum"))+"        ";
                result+="区间："+cursor.getString(cursor.getColumnIndex("itemMin"))
                        +"--"+ cursor.getString(cursor.getColumnIndex("itemMax"));
                Log.i("TAG",result);
                li.add(result);
                try{
                    double resultNum=Double.parseDouble(cursor.getString(cursor.getColumnIndex("itemResultNum")));
                    double itemMax=Double.parseDouble(cursor.getString(cursor.getColumnIndex("itemMax")));
                    double itemMin=Double.parseDouble(cursor.getString(cursor.getColumnIndex("itemMin")));

                    if(resultNum>itemMax){
                        li.add("偏高");
                    }
                    if(resultNum<itemMin){
                        li.add("偏低");
                    }
                    if(resultNum>=itemMin&&resultNum<=itemMax){
                        li.add("正确");
                    }
                    else li.add("");
                }catch (Exception e){
                    showMessage("数据转换失败："+e.getMessage());
                }
            }//数字类型
            if(resultType.equals("2")){//字符类型

                String resultString=cursor.getString(cursor.getColumnIndex("itemResultString"));
                String itemReference=cursor.getString(cursor.getColumnIndex("itemReference"));
                result+=resultString+"    参考："+itemReference;
                li.add(result);
                if(itemReference.equals(resultString)){
                    li.add("正确");
                }
                else li.add("异常");
            }
            if(resultType.equals("0")){//字符类型

                String resultString=cursor.getString(cursor.getColumnIndex("itemResultString"));
                result+=resultString+"    参考：无";
                li.add(result);
                li.add("");
            }
            if(resultType.equals("3")){//布尔类型

                String resultBlob=cursor.getString(cursor.getColumnIndex("itemResultBlob"));
                String itemReferenceBlob=cursor.getString(cursor.getColumnIndex("itemReference"));
                result+=resultBlob+"    参考："+itemReferenceBlob;
                li.add(result);
                if(itemReferenceBlob.equals(resultBlob)){
                    li.add("正确");
                }
                else li.add("错误");
            }
            resultList.add(li);
        }
        cursor.close();
        resultListAdapter.notifyDataSetChanged();
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
                Toast toast=Toast.makeText(WoDeActivity.this,message,Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
}
