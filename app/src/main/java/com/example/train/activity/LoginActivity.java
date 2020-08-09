package com.example.train.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.train.R;
import com.example.train.constants.message;
import com.example.train.constants.user;
import com.example.train.database.databaseDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText et_loginName,et_loginPassword;
    private Button bt_login,bt_toRegister,bt_remoteLogin;
    private TextView tv_loginFindPassworld;
    private databaseDao databaseDao;
    private SharedPreferences sharedPreferences;
    private int find=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("用户登录");
        init();
        sharedPreferences=super.getSharedPreferences("userInfo",MODE_PRIVATE);
        databaseDao = new databaseDao(this);
        setClickListner();

    }
    private void init(){
        et_loginName =(EditText)findViewById(R.id.et_loginUsername);
        et_loginPassword=(EditText)findViewById(R.id.et_loginPassword);
        bt_login=(Button)findViewById(R.id.bt_login);
        bt_toRegister=(Button)findViewById(R.id.bt_toRegister);
        bt_remoteLogin=(Button)findViewById(R.id.bt_remoteLogin);
        tv_loginFindPassworld=(TextView)findViewById(R.id.tv_loginFindPassworld);
    }
    private void setClickListner(){
        OnClick onClick =new OnClick();
        bt_login.setOnClickListener(onClick);
        bt_toRegister.setOnClickListener(onClick);
        bt_remoteLogin.setOnClickListener(onClick);
        tv_loginFindPassworld.setOnClickListener(onClick);
    }
    private class OnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent=null;
            switch (v.getId()){
                case R.id.bt_login:  //本地登陆
                    login();

                   setTitle("登录");
                    break;
                case R.id.bt_toRegister:  //注册
                    intent = new Intent(LoginActivity.this,RegisterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.bt_remoteLogin://远程登陆
                    remoteLogin();
                    break;
                case R.id.tv_loginFindPassworld:
                        new AlertDialog.Builder(LoginActivity.this).setTitle("消息").setMessage("确定后，然后只需输入账号登录修改")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        find=1;
                                        bt_remoteLogin.setText("远程找回");
                                        bt_login.setText("本地找回");
                                        et_loginPassword.setVisibility(View.INVISIBLE);
                                        setTitle("找回密码");
                                    }
                                }).show();
                    break;
                default:
                     break;
            }

        }
    }
    private void remoteLogin(){
        final String userName=et_loginName.getText().toString().trim();
        final String userPassword=et_loginPassword.getText().toString().trim();
        final String[] path=new String[1];
        if(find==0) {
            if (userName.equals("") || userPassword.equals("")) {
                showMsg("用户名或密码为空");
                return;
            }
            path[0]= message.path+"/train/Login?userName="+userName+"&userPassword="+userPassword;
        }
        if(find==1) {
            if (userName.equals("")) {
                showMsg("用户名为空");
                return;
            }
            path[0]= message.path+"/train/Login?userName="+userName+"&userPassword=1";
        }
        setTitle(userPassword);
        new Thread(){
            @Override
            public void run(){
                try{
                    URL url=new URL(path[0]);
                    HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setReadTimeout(1000);
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
                            showMsg("登陆失败，密码或账号错误");
                        }
                        else{

                            JSONObject json2=new JSONObject(result);
                            user.userId=json2.getString("userId");
                            user.userName=json2.getString("userName");
                            user.userPhone=json2.getString("userPhone");
                            user.userPower=json2.getString("userPower");
                            user.userStatus=json2.getString("userStatus");
                            user.userLogin="1";
                            if(find==0){
                                Message msg=handler.obtainMessage();
                                msg.obj="登陆成功";
                                handler.sendMessage(msg);
                            }
                            if(find==1){
                                Message msg=handler.obtainMessage();
                                msg.obj="找回密码";
                                handler.sendMessage(msg);
                            }

                        }

                    }
                    else showMsg("网络错误代码:"+conn.getResponseCode());
                }catch (Exception e){
                   showMsg("网络连接失败");
                }
                super.run();
            }
        }.start();
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Intent intent;
            switch ((String)msg.obj){
                case "登陆成功":

                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("userId",user.userId);
                    editor.putString("userName",user.userName);
                    editor.putString("userPhone",user.userPhone);
                    editor.putString("userStatus",user.userStatus);
                    editor.putString("userPower",user.userPower);
                    editor.putString("userLogin","1");
                    editor.commit();
                     intent=new Intent(LoginActivity.this,WoDeActivity.class);
                    startActivity(intent);
                    finish();
                break;
                case "找回密码":
                    if(user.userStatus.equals("3")){
                        intent=new Intent(LoginActivity.this,WoDeActivity.class);
                        startActivity(intent);
                    }else {
                        showMsg("需要管理员同意才能进行修改");
                        setTitle("联系管理员允许");
                        user.userId="";
                        user.userLogin="";
                        user.userPower="";
                        user.userName="";
                    }

                    break;
            }
            //Toast.makeText(LoginActivity.this, (String) msg.obj,Toast.LENGTH_SHORT).show();
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
    private void login(){
        String userName,userPassword;
        userName = et_loginName.getText().toString().trim();
        userPassword = et_loginPassword.getText().toString().trim();
        if(find==0){
            if(userName.equals("")||userPassword.equals(""))
            {
                showMsg("用户名或密码为空");
                return;
            }
        }
        if(find==1)
            if(userName.equals(""))
            {
                showMsg("用户名为空");
                return;
            }
        String sql ="select * from user where userName = '"+userName+"' or userPhone = '"+ userName+"'";
        String UserPassword="",UserName="",UserPhone="",UserId="",UserStatus="",UserPower="";
       Cursor cursor=databaseDao.query(sql);
        while(cursor.moveToNext()) {
            int index = cursor.getColumnIndex("userName");
            UserName = cursor.getString(index);
            index = cursor.getColumnIndex("userPassword");
            UserPassword = cursor.getString(index);
            index = cursor.getColumnIndex("userPhone");
            UserPhone = cursor.getString(index);
            index = cursor.getColumnIndex("userId");
            UserId = cursor.getString(index);
            index = cursor.getColumnIndex("userStatus");
            UserStatus = cursor.getString(index);
            index = cursor.getColumnIndex("userPower");
            UserPower = cursor.getString(index);
        }
        cursor.close();
        if(find==1){
            user.userId=UserId;
            user.userName=UserName;
            user.userPhone=UserPhone;
            user.userStatus=UserStatus;
            user.userPower=UserPower;
            user.userLogin="0";
        }
        if(find==0)
        if(userPassword.equals(UserPassword)&& (userName.equals(UserName)||userName.equals(UserPhone))){
            user.userId=UserId;
            user.userName=UserName;
            user.userPhone=UserPhone;
            user.userStatus=UserStatus;
            user.userPower=UserPower;
            user.userLogin="0";

            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("userId",UserId);
            editor.putString("userName",UserName);
            editor.putString("userPhone",UserPhone);
            editor.putString("userStatus",user.userStatus);
            editor.putString("userPower",user.userPower);
            editor.putString("userLogin",user.userLogin);
            editor.commit();
            Toast.makeText(this,"登陆成功",Toast.LENGTH_SHORT).show();

        }
        else {
            showMsg("用户名或密码错误");
        }
        Intent intent=new Intent(LoginActivity.this,WoDeActivity.class);
        startActivity(intent);
        finish();
    }
    private void showMsg(final String msg){
      runOnUiThread(new Runnable() {
          @Override
          public void run() {
              Toast.makeText(LoginActivity.this,msg,Toast.LENGTH_SHORT).show();
          }
      });

    }
}
