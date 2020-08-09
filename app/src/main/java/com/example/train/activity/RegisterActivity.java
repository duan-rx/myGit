package com.example.train.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.train.R;
import com.example.train.constants.message;
import com.example.train.constants.user;
import com.example.train.database.databaseDao;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private databaseDao databaseDao;
    private EditText et_registerUserId,et_registerUserName,et_registerUserPhone,et_registerUserPassword;
    private EditText et_registerUserPassword2;
    private Button bt_register,bt_remoteRegister;
    private String userId,userName,userPhone,userPassword,userPassword2;
    private RadioGroup rg_ckecked;
    private int userPower=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("用户注册");
        init();
        databaseDao= new databaseDao(this);
        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(register()){
                    localRegister();
                }

            }
        });
        bt_remoteRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(register()){
                    remoteRegister();
                };

            }
        });
        rg_ckecked.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if(rb.getText().toString().equals("管理员")){
                    userPower=1;
                }
               else  userPower=0;
               showMessage(""+userPower);
            }
        });
    }
    private void init(){
        et_registerUserId  =(EditText)findViewById(R.id.et_registerId);
        et_registerUserName =(EditText)findViewById(R.id.et_registerUsername);
        et_registerUserPhone =(EditText)findViewById(R.id.et_registerPhone);
        et_registerUserPassword=(EditText)findViewById(R.id.et_registerPassword);
        et_registerUserPassword2=(EditText)findViewById(R.id.et_registerPassword2);
        bt_register =(Button)findViewById(R.id.bt_register);
        bt_remoteRegister=(Button)findViewById(R.id.bt_remoteRegister);
        rg_ckecked=(RadioGroup)findViewById(R.id.rg_ckecked);
    }
    private boolean register(){

        userId = et_registerUserId.getText().toString().trim();
        userName =et_registerUserName.getText().toString().trim();
        userPhone=et_registerUserPhone.getText().toString().trim();
        userPassword = et_registerUserPassword.getText().toString().trim();
        userPassword2 = et_registerUserPassword2.getText().toString().trim();
       if(userId.equals("")||userName.equals("")||userPassword.equals("")||userPhone.equals("")){
           showMessage("文本框不能为空");
           return false;
       }
       if(!userPassword.equals(userPassword2)){
           showMessage("密码不一致");
           return false;
       }
       return true;
    }
    private void remoteRegister(){
            if(userName.equals("")||userPassword.equals(""))
            {
                showMessage("用户名或密码为空");
                return;
            }
            setTitle(userPassword);
            new Thread(){
                @Override
                public void run(){
                    try{
                        final String path= message.path+"/train/Register";

                        URL url=new URL(path);
                        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                        conn.setRequestMethod("POST");
                        //HashMap<String,String> map=new HashMap<>();
                        JSONObject map=new JSONObject();
                        map.put("userId",userId);
                        map.put("userName",userName);
                        map.put("userPhone",userPhone);
                        map.put("userPassword",userPassword);
                        map.put("userPower","0");
                        if(userPower==1){
                            map.put("userStatus","2");
                        }
                        else map.put("userStatus","1");

                        conn.setReadTimeout(5000);
                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        JSONObject json=new JSONObject();
                        json.put("Register",map.toString());
                        //设置请求体的类型是文本类型
                        //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        //设置请求体的长度
                        // conn.setRequestProperty("Content-Length", String.valueOf(text.length()));
                        conn.getOutputStream().write(json.toString().getBytes());

                        //Log.i("TAG",text);
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
                                msg.obj="注册成功";

                                handler.sendMessage(msg);
                            }
                            else{
                                showMessage("注册失败，id、手机号、用户名已存在");
                            }
                        }
                        else showMessage("网络错误代码:"+conn.getResponseCode());
                    }catch (Exception e){
                        showMessage("连接网络失败");
                    }
                    super.run();
                }
            }.start();
        }
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch ((String)msg.obj){
                case "注册成功":{
                    showMessage("远程注册成功，进行本地注册");
                    localRegister();
                }
            }

        };
    };
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
    private void localRegister(){
        String sql="select * from user where userId='"+userId+"'or userName='"+userName+"' or userPhone='"+userPhone+"';";
        Cursor cursor=databaseDao.query(sql);
        if(cursor.getCount()>0){
            String msg="本地已有 ";
            while (cursor.moveToNext()){

                int index=cursor.getColumnIndex("userId");
                msg+="ID："+cursor.getString(index);
                index=cursor.getColumnIndex("userName");
                msg+="名称："+cursor.getString(index);
                index=cursor.getColumnIndex("userPhone");
                msg+="手机号："+cursor.getString(index);
                cursor.close();
                showMessage(msg);
                return;
            }

        }
        try {
            sql ="insert into user(userId,userName,userPhone,userPassword,userPower) values('"+userId
                    +"','"+userName+"','"+userPhone+"','"+userPassword+"','"+userPower+"')";
            Log.i("TAG",sql);
            if(databaseDao.insert(sql)){
                showMessage("本地注册成功");
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else showMessage("本地注册失败");


        }
        catch (Exception e){
            showMessage(e.getMessage());
            showMessage("注册失败");
        }
        sql ="select * from user";
        cursor=databaseDao.query(sql);
        String user="";
        while(cursor.moveToNext()) {
            int index = cursor.getColumnIndex("userName");
            String UserName = cursor.getString(index);
            index = cursor.getColumnIndex("userPassword");
            String UserPassword = cursor.getString(index);
            user+=UserName  +"  "+UserPassword+"  " ;
        }
        cursor.close();
        setTitle(user);
    }
    private void showMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast=Toast.makeText(RegisterActivity.this,message,Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
}
