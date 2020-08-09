package com.example.train.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.train.R;
import com.example.train.adapter.histroyListAdapter;
import com.example.train.adapter.resultManagerListAdapter;
import com.example.train.constants.convertStream;
import com.example.train.constants.message;
import com.example.train.constants.user;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ResultManagerActivity extends AppCompatActivity {
    private Button bt_resultMangerScan;
    private Spinner sp_resultManager;
    private ListView lv_resultManager;
    private EditText et_resultManager;
    private List<List<String>> list=new ArrayList<>();
    private resultManagerListAdapter listAdapter;
    private List<List<String>> commitResult=new ArrayList<>();//获取提交数据
    private histroyListAdapter commitResultAdapter;
    private String studentRemark="";//备注
    private int selectPosition=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_manager);
        init();
        setTitle("成绩管理");
        commitResultAdapter=new histroyListAdapter(this);
        listAdapter=new resultManagerListAdapter(this);
        listAdapter.bindData(list);
        lv_resultManager.setAdapter(listAdapter);
        sp_resultManager.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                list.clear();
                listAdapter.notifyDataSetChanged();
                showMessage("正在获取数据");
                switch(position){
                    case 0:
                        break;
                    case 1://用户名排序
                        selectSort("userName");
                        break;
                    case 2://提交次数
                        selectSort("number");
                        break;
                    case 3://提交时间
                        selectSort("commitTime");
                        break;
                    case 4://审查状态
                        selectSort("isCheck");
                        break;
                    default:
                        break;

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        bt_resultMangerScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!et_resultManager.getText().toString().trim().equals("")) {
                    list.clear();
                    listAdapter.notifyDataSetChanged();
                    selectSort(et_resultManager.getText().toString().trim());
                }
                else{
                    showMessage("搜索不能为空");
                }
            }
        });
        lv_resultManager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0) return;
                commitResult.clear();
                commitResultAdapter.notifyDataSetChanged();
                selectPosition=position;
                remoteClick(position);
            }
        });
        lv_resultManager.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0)return false;
                else{
                    selectPosition=position;
                    return true;
                }

            }
        });
        selectSort("isCheck");
    }
    private void init(){
        bt_resultMangerScan=(Button)findViewById(R.id.bt_resultManager);
        sp_resultManager=(Spinner)findViewById(R.id.sp_resultManager);
        lv_resultManager=(ListView)findViewById(R.id.lv_resultManager);
        et_resultManager=(EditText)findViewById(R.id.et_resultManagerUsername);
    }
    private void selectSort(String sort){
        JSONObject json=new JSONObject();
        try{
            json.put("ResultManager",sort);
        }catch (Exception e){
            showMessage("数据转换json失败:"+e.getMessage());
            return;
        }
        uploadeMsg(json);

    }
    private void remoteClick(final int position){
        String count=list.get(position).get(1).toString();
        String userId=list.get(position).get(5).toString();
        JSONObject json=new JSONObject();
        try{
            json.put("userId",userId);
            json.put("number",count);
            json.put("getResult","getResult");
            if(list.get(position).get(3).equals("0")){
                json.put("isCheck","1");
            }
            uploadeMsg(json);
        }catch (Exception e){
            showMessage("数据转换json失败"+e.getMessage());
            return;
        }

    }
    //网络数据提交
    private void uploadeMsg(final JSONObject jsonUser){
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
                        String result=json2.getString("Result");
                        if(result.equals("ResultManagerSuccess")){
                            int i=0;
                            List<String> l=new ArrayList<>();
                            Log.i("TAG",json2.toString());
                            l.add("用户名");
                            l.add("次数");
                            l.add("标题");
                            l.add("状态");
                            l.add("上传时间           ");
                            list.add(l);

                            if(json2.getString("Count").equals("0")){
                                Message msg=handler.obtainMessage();
                                msg.obj="没有历史数据";
                                handler.sendMessage(msg);
                                return;
                            }
                            JSONObject head=new JSONObject(json2.getString(""+i));
                            while(!json2.getString("Count").equals(""+i)){
                                JSONObject history=new JSONObject(json2.getString(""+i));
                                Log.i("TAG",history.toString());
                                List<String> li=new ArrayList<>();
                                li.add(history.getString("userName"));
                                li.add(history.getString("number"));
                                li.add(history.getString("title"));
                                li.add(history.getString("isCheck"));
                                li.add(history.getString("commitTime"));
                                li.add(history.getString("userId"));

                                list.add(li);
                                i++;
                            }
                            Message msg=handler.obtainMessage();
                            msg.obj="历史查询成功";
                            handler.sendMessage(msg);
                        }
                        if(result.equals("ResultManagerFaild")){
                            showMessage("查询失败");
                        }
                        if(result.equals("getResultSuccess")) {
                            if(json2.has("studentRemark")){
                                studentRemark=json2.getString("studentRemark");
                            }
                            int i=0;
                            List<String> l=new ArrayList<>();
                            l.add("项目");
                            l.add("结果");
                            l.add("时间              ");
                            commitResult.add(l);

                            if(json2.getString("Count").equals("0")){
                               showMessage("没有数据");
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
                            msg.obj = "获取详细结果成功";
                            handler.sendMessage(msg);
                        }
                        if(result.equals("insertTeacherRemarkSuccess")) {
                            showMessage("提交备注成功");
                        }
                        if(result.equals("insertTeacherRemarkFaild")) {
                            showMessage("提交备注失败");
                        }
                        if(result.equals("faild")){
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
                case "历史查询成功":
                    showMessage("查询成功");
                    listAdapter.notifyDataSetChanged();
                    break;
                case "没有历史数据":
                    showMessage("无数据");
                    listAdapter.notifyDataSetChanged();
                    break;
                case "获取详细结果成功":
                    remoteResultShow();
                    list.get(selectPosition).set(3,"1");
                    listAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;

            }
        };
    };
    //弹窗显示提交结果
    private void remoteResultShow(){
        if(studentRemark.equals("")){
            studentRemark="未写";
        }
        Log.i("TAG",commitResult.toString());
        View view = getLayoutInflater().inflate(R.layout.history_list, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(ResultManagerActivity.this).setTitle(list.get(selectPosition).get(0).toString()+":"+list.get(selectPosition).get(1).toString()+"详细数据")
                // .setIcon(R.mipmap.ic_launcher)
                .setMessage("用户提交备注：\n"+studentRemark)
                .setView(view)
                .create();
        ListView listView=view.findViewById(R.id.lv_historyList);
        commitResultAdapter.bindData(commitResult);
        listView.setAdapter(commitResultAdapter);
        commitResultAdapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        Button bt_historyListUpload=(Button)view.findViewById(R.id.bt_historyListUpload);
        Button bt_historyListCancel=(Button)view.findViewById(R.id.bt_historylistCancel);
        bt_historyListCancel.setVisibility(View.VISIBLE);
        bt_historyListUpload.setVisibility(View.VISIBLE);
        bt_historyListCancel.setText("退出");
        bt_historyListUpload.setText("评语");
        bt_historyListUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setUploadMsg(selectPosition);
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
    private void  setUploadMsg(final int position){
        View view = getLayoutInflater().inflate(R.layout.result_commit_msg,null);
        final EditText et_commitNumber=(EditText)view.findViewById(R.id.et_commitNumber);
        final EditText et_commitTitle=(EditText)view.findViewById(R.id.et_commitTitle);
        et_commitNumber.setEnabled(false);
        et_commitTitle.setEnabled(false);
        final EditText et_commitRemark=(EditText)view.findViewById(R.id.et_commitRemark);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("提交信息")
                // .setIcon(R.mipmap.ic_launcher)
                .setMessage("只需写备注即可")
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
                            jsonCommitMsg.put("teacherId", user.userId);
                            jsonCommitMsg.put("number",list.get(selectPosition).get(1).toString());
                            jsonCommitMsg.put("userId",list.get(selectPosition).get(5).toString());
                            jsonCommitMsg.put("teacherRemark",et_commitRemark.getText().toString().trim());
                            jsonCommitMsg.put("teacheWriteRemark","teacheWriteRemark");
                            uploadeMsg(jsonCommitMsg);
                        }catch (Exception e){
                            showMessage("数据转换json失败"+e.getMessage());
                            Log.i("TAG","数据转换json失败"+e.getMessage());
                            return;
                        }
                    }
                }).create();

        alertDialog.show();


    }
    private void showMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast= Toast.makeText(ResultManagerActivity.this,message,Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
}
