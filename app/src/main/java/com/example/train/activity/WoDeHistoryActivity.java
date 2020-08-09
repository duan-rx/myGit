package com.example.train.activity;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.ActivityChooserModel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.train.MainActivity;
import com.example.train.R;
import com.example.train.adapter.histroyListAdapter;
import com.example.train.constants.convertStream;
import com.example.train.constants.message;
import com.example.train.constants.user;
import com.example.train.database.databaseDao;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class WoDeHistoryActivity extends AppCompatActivity {
    private Button bt_woDeLocalhistory,bt_woDeCommitHistory;
    private ListView lv_woDeHistory;
    private List<List<String>> list=new ArrayList<>();
    private List<List<String>> commitResult=new ArrayList<>();//获取提交数据
    private histroyListAdapter commitResultAdapter;
    private histroyListAdapter listAdapter;
    private databaseDao db;
    private String teacherRemark="";//提交评语

    private int status=0;//0 代表本地记录，1 提交记录
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wo_de_history);

        db=new databaseDao(this);

        /*
        db.delete("delete  from histroy where userId='"+user.userId+"'");
        int i=20;
        while (i<30){
            String sql="insert into histroy(count,userId) values('"+i+"','"+user.userId+"')";
            db.insert(sql);
            i++;
        }
        db.delete("delete from result where userId='"+user.userId+"'");
        i=20;
        while (i<30){
            int j=0;
            while (j<10){
                String sql="insert into result(userId,inCode,itemCode,itemResultNum,itemResultString,itemResultBlob,count)" +
                        " values('"+user.userId+"','1','2','"+j+"','"+j+"','"+j+"','"+i+"')";
                db.insert(sql);
                j++;
                Log.i("TAG",""+i);
            }
            i++;
        }

         */
        init();
        setOnClick();
        commitResultAdapter=new histroyListAdapter(this);
        listAdapter= new histroyListAdapter(this);
        listAdapter.bindData(list);
        lv_woDeHistory.setAdapter(listAdapter);
        setTitle("本地记录");

        lv_woDeHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0) return false;

                if(status==0){ //本地记录状态
                    localLongClick(position);
                }
                if (status == 1) {//提交记录状态
                    remoteLongClick(position);
                }
                return true;
            }
        });
        lv_woDeHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0) return;

                if(status==0){
                    localClick(position);
                }
                if (status == 1) {
                    showMessage("正在获取数据");
                    teacherRemark="";
                    commitResult.clear();
                    remoteClick(position);
                }
            }
        });

        localHistory();
    }
    private void init(){
        bt_woDeLocalhistory=(Button)findViewById(R.id.bt_woDeLocalHistory);
        bt_woDeCommitHistory=(Button)findViewById(R.id.bt_woDeCommitHistory);
        lv_woDeHistory=(ListView)findViewById(R.id.lv_woDeHistory);

    }
    private void setOnClick(){
        OnClick onClick=new OnClick();
        bt_woDeCommitHistory.setOnClickListener(onClick);
        bt_woDeLocalhistory.setOnClickListener(onClick);

    }
    private class OnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.bt_woDeLocalHistory://本地记录
                    setTitle("本地记录");
                    if(status!=0){
                        list.clear();
                        listAdapter.notifyDataSetChanged();
                        localHistory();
                    }
                    status=0;
                    break;
                case R.id.bt_woDeCommitHistory://提交记录
                    if(user.userLogin.equals("0")){
                        showMessage("账号需要远程登陆,请退出重新登陆");
                        return;
                    }
                    setTitle("提交记录");
                    if(status!=1) {
                        list.clear();
                        listAdapter.notifyDataSetChanged();
                        showMessage("正在获取数据");
                        remoteHistory();
                    }
                    status=1;
                    break;

                default:
                    break;
            }
        }
    }
    //本地历史数据检索
    private void  localHistory(){
        List<String> head=new ArrayList<>();
        head.add("训练次数");
        head.add("开始时间");
        head.add("结束时间");
        list.add(head);
        listAdapter.notifyDataSetChanged();
        String sql="select * from histroy where userId='"+user.userId+"'";
        Cursor cursor=db.query(sql);
        while(cursor.moveToNext()) {
            List<String> li= new ArrayList<>();
            int index = cursor.getColumnIndex("count");
            li.add(cursor.getString(index));
            index = cursor.getColumnIndex("startTime");
            li.add(cursor.getString(index));


            index = cursor.getColumnIndex("stopTime");
            li.add(cursor.getString(index));
            list.add(li);
            listAdapter.notifyDataSetChanged();
        }
        cursor.close();
    }
    //服务器历史数据检索
    private void remoteHistory(){
        JSONObject json=new JSONObject();
        try {
            json.put("requireHistory",user.userId);
        }catch (Exception e){
            showMessage("数据转换json失败："+e.getMessage());
        }
       uploadeMsg(json,0);
    }
    //获取本地结果
    private void localClick(final int position){

        String count=list.get(position).get(0).toString();
        List<List<String>> itemList= new ArrayList<>();
        String Sql="select instrumentItem.itemName,instrumentItem.itemResultType,result.itemResultNum, result.itemResultString,result.itemResultBlob,result.dataTime " +
                "from result,instrumentItem " +
                "where count='"+count+"' and userId='"+user.userId +"' and result.itemCode=instrumentItem.itemCode";
        Cursor cursor=db.query(Sql);
        List<String> head=new ArrayList<>();
        head.add("项目名");
        head.add("结果");
        head.add("时间");
        itemList.add(head);
        while(cursor.moveToNext()) {

            List<String> li=new ArrayList<>();
            int index = cursor.getColumnIndex("itemName");
            li.add(cursor.getString(index));

            String result="";
            switch (cursor.getString(cursor.getColumnIndex("itemResultType"))){
                case "1":
                    index = cursor.getColumnIndex("itemResultNum");
                    li.add(cursor.getString(index));
                    break;
                case "2":
                    index = cursor.getColumnIndex("itemResultString");
                    li.add(cursor.getString(index));
                    break;
                case "3":
                    index = cursor.getColumnIndex("itemResultBlob");
                    li.add(cursor.getString(index));
                    break;
                default :
                    index = cursor.getColumnIndex("itemResultString");
                    if(cursor.getString(index)==null){
                        li.add("无");
                    }
                    else
                         li.add(cursor.getString(index));
                    break;
            }
            index = cursor.getColumnIndex("dataTime");
            li.add(cursor.getString(index));
            itemList.add(li);
        }
        cursor.close();

        View view = getLayoutInflater().inflate(R.layout.history_list, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("训练次数为："+count)
                // .setIcon(R.mipmap.ic_launcher)
                .setView(view)
                .create();
                /*
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("上传", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface,
                                        int paramAnonymousInt) {
                        if(user.userLogin.equals("0")){
                            showMessage("账号需要远程登陆才可以上传,请退出重新登陆");
                            return;
                        }
                        setUploadMsg(position);

                    }
                }).create();

                 */
        ListView listView=view.findViewById(R.id.lv_historyList);
        histroyListAdapter adapter=new histroyListAdapter(WoDeHistoryActivity.this);
        adapter.bindData(itemList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        Button bt_historyListUpload=(Button)view.findViewById(R.id.bt_historyListUpload);
        Button bt_historyListCancel=(Button)view.findViewById(R.id.bt_historylistCancel);
        bt_historyListCancel.setVisibility(View.VISIBLE);
        bt_historyListUpload.setVisibility(View.VISIBLE);
        bt_historyListCancel.setText("取消");
        bt_historyListUpload.setText("上传");
        bt_historyListUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.userLogin.equals("0")){
                    showMessage("账号需要远程登陆才可以上传,请退出重新登陆");
                    return;
                }
                setUploadMsg(position);
                alertDialog.cancel();
            }
        });
        bt_historyListCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        alertDialog.show();
    }
    //获取提交结果
    private void remoteClick(final int position){
        String count=list.get(position).get(0).toString();
        JSONObject json=new JSONObject();
        try{
            json.put("userId",user.userId);
            json.put("number",count);
            json.put("getResult","getResult");
            uploadeMsg(json,0);
        }catch (Exception e){
            showMessage("数据转换json失败"+e.getMessage());
            return;
        }

    }
    //删除本地结果
    private void localLongClick(final int position){
        final String count=list.get(position).get(0).toString();
        new AlertDialog.Builder(WoDeHistoryActivity.this)
                .setTitle("提示")
                .setMessage("确定删除本地记录："+count+"以及对应的结果吗")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.delete("delete from result where count='"+count+"' and userId='"+user.userId +"' ");
                        db.delete(" delete from  histroy where count='"+count+"' and userId='"+user.userId +"' ");
                        list.remove(position);
                        listAdapter.notifyDataSetChanged();
                    }
                }).create().show();

    }
    //删除提交结果
    private void remoteLongClick(final int position){
        final String count=list.get(position).get(0).toString();
        new AlertDialog.Builder(WoDeHistoryActivity.this)
                .setTitle("提示")
                .setMessage("确定删除提交记录："+count+"以及对应的结果吗")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       JSONObject json=new JSONObject();
                       try{
                           json.put("userId",user.userId);
                           json.put("number",count);
                           json.put("deleteResult","historyAndresult");
                           uploadeMsg(json,position);
                       }catch (Exception e){
                           showMessage("数据转换json失败："+e.getMessage());
                           return;
                       }


                    }
                }).create().show();
    }
    //提交数据说明
    private void  setUploadMsg(final int position){
        View view = getLayoutInflater().inflate(R.layout.result_commit_msg,null);
        final EditText et_commitNumber=(EditText)view.findViewById(R.id.et_commitNumber);
        final EditText et_commitTitle=(EditText)view.findViewById(R.id.et_commitTitle);
        final EditText et_commitRemark=(EditText)view.findViewById(R.id.et_commitRemark);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("提交信息")
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
                        JSONObject jsonCommitMsg=new JSONObject();
                        try{
                            jsonCommitMsg.put("userId",user.userId);
                            jsonCommitMsg.put("number",et_commitNumber.getText().toString().trim());
                            jsonCommitMsg.put("title",et_commitTitle.getText().toString().trim());
                            jsonCommitMsg.put("studentRemark",et_commitRemark.getText().toString().trim());
                            uploadResult(position,jsonCommitMsg,et_commitNumber.getText().toString().trim());
                        }catch (Exception e){
                            showMessage("数据转换json失败"+e.getMessage());
                            return;
                        }
                    }
                }).create();

        alertDialog.show();


    }
    //提交数据准备
    private void uploadResult(int position,JSONObject commitMsg,final  String number){
        String count=list.get(position).get(0).toString();
        List<List<String>> itemList= new ArrayList<>();
        String Sql="select instrumentItem.itemName,result.itemResultNum, result.itemResultString,result.itemResultBlob,result.dataTime,instrumentItem.itemResultType " +
                "from result,instrumentItem " +
                "where count='"+count+"' and userId='"+user.userId +"' and result.itemCode=instrumentItem.itemCode";
        Cursor cursor=db.query(Sql);
        JSONObject jsonAll=new JSONObject();
        int resultCount=0;
        while(cursor.moveToNext()) {
            try{
                JSONObject json=new JSONObject();
                json.put("userId",user.userId);
                json.put("number",number);

                int index = cursor.getColumnIndex("itemName");
                Log.i("TAG",cursor.getString(index));
                json.put("itemName",cursor.getString(index));

                String result="";
                switch (cursor.getString(cursor.getColumnIndex("itemResultType"))){
                    case "1":{
                        index = cursor.getColumnIndex("itemResultNum");
                        result+=cursor.getString(index);
                        break;
                    }
                    case "2":
                        index = cursor.getColumnIndex("itemResultString");
                        result+=cursor.getString(index);
                        break;
                    case "3":
                        index = cursor.getColumnIndex("itemResultBlob");
                        result+=cursor.getString(index);
                        break;
                    case "0":
                        index = cursor.getColumnIndex("itemResultString");
                        result+=cursor.getString(index);
                        break;
                    default:
                        index = cursor.getColumnIndex("itemResultString");
                        result+=cursor.getString(index);
                        break;
                }



                json.put("itemResult",result);

                index = cursor.getColumnIndex("dataTime");
                json.put("itemTime",cursor.getString(index));
                jsonAll.put(""+resultCount,json.toString());
                resultCount++;
                if(cursor.isLast())jsonAll.put("resultCount",resultCount);
            }
            catch (Exception e){
                showMessage("数据转化json格式失败");
            }

        }
        cursor.close();
        JSONObject uploadeResult=new JSONObject();
        try{
            uploadeResult.put("uploadeResult",jsonAll.toString());
            uploadeResult.put("uploadeCommitMsg",commitMsg.toString());
            uploadeMsg(uploadeResult,position);
        }
        catch (Exception e){
            showMessage("数据转化json格式失败");
            return;
        }

    }
    //网络数据提交
    private void uploadeMsg(final JSONObject jsonUser,final int position){
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
                        Log.i("TAG","result="+result);
                        if(result.equals("queryHistorySuccess")){
                            int i=0;
                            List<String> l=new ArrayList<>();
                            Log.i("TAG",json2.toString());
                            l.add("次数");
                            l.add("标题");
                            l.add("上传时间           ");
                            list.add(l);

                            if(json2.getString("Count").equals("0")){
                                Message msg=handler.obtainMessage();
                                msg.obj="没有提交的历史数据";
                                handler.sendMessage(msg);
                                return;
                            }
                            JSONObject head=new JSONObject(json2.getString(""+i));
                            while(!json2.getString("Count").equals(""+i)){
                                JSONObject history=new JSONObject(json2.getString(""+i));
                                List<String> li=new ArrayList<>();
                                li.add(history.getString("number"));
                                li.add(history.getString("title"));
                                li.add(history.getString("commitTime"));
                                list.add(li);
                                i++;
                            }
                            Message msg=handler.obtainMessage();
                            msg.obj="提交历史查询成功";
                            handler.sendMessage(msg);
                        }
                        if(result.equals("getResultSuccess")) {
                            if(json2.has("teacherRemark")){
                                teacherRemark=json2.getString("teacherRemark");
                            }
                            int i=0;
                            List<String> l=new ArrayList<>();
                            Log.i("TAG",json2.toString());
                            l.add("项目");
                            l.add("结果");
                            l.add("时间              ");
                            commitResult.add(l);

                            if(json2.getString("Count").equals("0")){
                                Message msg=handler.obtainMessage();
                                msg.obj="没有提交的历史数据";
                                handler.sendMessage(msg);
                                return;
                            }
                            JSONObject head=new JSONObject(json2.getString(""+i));
                            while(!json2.getString("Count").equals(""+i)){
                                JSONObject history=new JSONObject(json2.getString(""+i));
                                List<String> li=new ArrayList<>();
                                li.add(history.getString("itemName"));
                                li.add(history.getString("itemResult"));
                                li.add(history.getString("itemTime"));
                                commitResult.add(li);
                                i++;
                            }
                            Message msg = handler.obtainMessage();
                            msg.obj = "获取提交结果成功";
                            handler.sendMessage(msg);
                        }
                        if(result.equals("getResultfaild")){
                            showMessage("获取提交结果失败");
                        }
                        if(result.equals("deleteSuccess")) {
                            list.remove(position);
                            Message msg = handler.obtainMessage();
                            msg.obj = "删除提交数据成功";
                            showMessage("删除提交数据成功");
                            handler.sendMessage(msg);
                        }
                        if(result.equals("deleteFaild")) {
                            showMessage("删除提交数据失败");
                        }
                        if(result.equals("queryHistoryFaild")) {
                            Message msg = handler.obtainMessage();
                            msg.obj = "提交历史查询失败";
                            handler.sendMessage(msg);
                        }
                        if(result.equals("success")){
                            Message msg=handler.obtainMessage();
                            msg.obj="结果插入成功";
                            handler.sendMessage(msg);

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

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Intent intent;
            switch ((String)msg.obj){
                case "结果插入成功":
                    showMessage("提交成功");
                    break;
                case "提交历史查询成功":
                    listAdapter.notifyDataSetChanged();
                    break;
                case "删除提交数据成功":
                    listAdapter.notifyDataSetChanged();
                    break;
                case "获取提交结果成功":
                    remoteResultShow();
                    break;
                default:
                    listAdapter.notifyDataSetChanged();
                    break;

            }
        };
    };
    //弹窗显示提交结果
    private void remoteResultShow(){
        if(teacherRemark.equals("")){
            teacherRemark="结果未被查看或没有写评语";
        }
        View view = getLayoutInflater().inflate(R.layout.history_list, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("提交的数据")
                // .setIcon(R.mipmap.ic_launcher)
                .setMessage("评语："+teacherRemark)
                .setView(view)
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface,
                                        int paramAnonymousInt) {
                    }
                }).create();
        ListView listView=view.findViewById(R.id.lv_historyList);
        commitResultAdapter.bindData(commitResult);
        listView.setAdapter(commitResultAdapter);
        commitResultAdapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        alertDialog.show();
    }
    private void showMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast= Toast.makeText(WoDeHistoryActivity.this,message,Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
}
