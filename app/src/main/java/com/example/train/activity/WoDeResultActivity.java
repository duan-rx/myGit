package com.example.train.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.train.R;
import com.example.train.adapter.histroyListAdapter;
import com.example.train.constants.user;
import com.example.train.database.databaseDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WoDeResultActivity extends AppCompatActivity {
    private Spinner sp_woDeResulthead;
    private ListView lv_woDeResult;
    private List<List<String>>  resultList=new ArrayList<>();
    private histroyListAdapter resultListAdapter;
    private databaseDao db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wo_de_result);
        setTitle("所有成绩查看");
        db=new databaseDao(this);
        lv_woDeResult=(ListView)findViewById(R.id.lv_woDeResult);
        sp_woDeResulthead=(Spinner)findViewById(R.id.sp_woDeResutlHead);

        resultListAdapter=new histroyListAdapter(this);
        resultListAdapter.bindData(resultList);
        lv_woDeResult.setAdapter(resultListAdapter);


        sp_woDeResulthead.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                resultList.clear();
                resultListAdapter.notifyDataSetChanged();
                String sql="";
                switch (position){
                    case 0:
                        sql="select result.count,instrumentItem.itemName,result.itemResultNum,result.itemResultString," +
                                "result.itemResultBlob,instrumentItem.itemResultType,instrumentItem.itemReference,instrumentItem.itemMax," +
                                "instrumentItem.itemMin from result,instrumentItem " +
                                "where instrumentItem.itemcode=result.itemCode and result.userId='"+ user.userId+"' order by result.count,instrumentItem.itemName desc";
                        queryAllResult(sql,0);
                        break;
                    case 1://训练次数排序
                         sql="select result.count,instrumentItem.itemName,result.itemResultNum,result.itemResultString," +
                                "result.itemResultBlob,instrumentItem.itemResultType,instrumentItem.itemReference,instrumentItem.itemMax," +
                                "instrumentItem.itemMin from result,instrumentItem " +
                                "where  instrumentItem.itemcode=result.itemCode and result.userId='"+ user.userId+"' order by result.count,instrumentItem.itemName desc";
                      queryAllResult(sql,0);
                        break;
                    case 2://项目名排序
                        sql="select result.count,instrumentItem.itemName,result.itemResultNum,result.itemResultString," +
                                "result.itemResultBlob,instrumentItem.itemResultType,instrumentItem.itemReference,instrumentItem.itemMax," +
                                "instrumentItem.itemMin from result,instrumentItem " +
                                "where instrumentItem.itemcode=result.itemCode and result.userId='"+ user.userId+"' order by instrumentItem.itemName desc";
                        queryAllResult(sql,0);
                        break;
                    case 3://项目时间排序
                        sql="select result.dataTime,instrumentItem.itemName,result.itemResultNum,result.itemResultString," +
                                "result.itemResultBlob,instrumentItem.itemResultType,instrumentItem.itemReference,instrumentItem.itemMax," +
                                "instrumentItem.itemMin from result,instrumentItem " +
                                "where instrumentItem.itemcode=result.itemCode and result.userId='"+ user.userId+"' order by result.dataTime desc";
                        queryAllResult(sql,1);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void queryAllResult(String sql,int status){

        List<String> l=new ArrayList<>();
        resultList.clear();
        if(status==1) {
            l.add("时间");
        }else l.add("次数");
        l.add("项目：结果        参考值");
        l.add("成绩    ");
        resultList.add(l);
        if(sql.equals("")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resultListAdapter.notifyDataSetChanged();
                }
            });
            return;
        }
        Cursor cursor=db.query(sql);

        if(cursor.getCount()<=0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resultListAdapter.notifyDataSetChanged();
                }
            });
            return;
        }

        while (cursor.moveToNext())
        {
            List<String> li=new ArrayList<>();
            if(status==1) {
                try{
                    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = dateformat.parse(cursor.getString(cursor.getColumnIndex("dataTime")).toString());
                    String dateStr=new SimpleDateFormat("yyyyMMdd HH:mm").format(date);
                    li.add(dateStr.toString());
                  }catch (Exception e){
                    showMessage("日期转换失败："+e.getMessage());
                }
                //li.add(cursor.getString(cursor.getColumnIndex("dataTime")));
            }else li.add(cursor.getString(cursor.getColumnIndex("count")));
            String result=cursor.getString(cursor.getColumnIndex("itemName"))+":";
            String resultType= cursor.getString(cursor.getColumnIndex("itemResultType"));
            if(resultType.equals("1")) {//数字类型结果
                if(cursor.getString(cursor.getColumnIndex("itemResultNum"))==null){
                    result+="         区间："+cursor.getString(cursor.getColumnIndex("itemMin"))
                            +"--"+ cursor.getString(cursor.getColumnIndex("itemMax"));
                    li.add(result);
                    li.add("");
                    resultList.add(li);
                    break;
                }
                result+=cursor.getString(cursor.getColumnIndex("itemResultNum"))+"        ";
                result+="区间："+cursor.getString(cursor.getColumnIndex("itemMin"))
                        +"--"+ cursor.getString(cursor.getColumnIndex("itemMax"));
                Log.i("TAG",result);
                li.add(result);
                try{
                    String resultNumber=cursor.getString(cursor.getColumnIndex("itemResultNum")).trim();

                    String itemmax=cursor.getString(cursor.getColumnIndex("itemMax")).trim();
                    String itemin=cursor.getString(cursor.getColumnIndex("itemMin")).trim();
                    if(resultNumber.equals("")||itemin.equals("")||itemmax.equals("")||resultNumber==null){
                        li.add("不进行判断");
                        resultList.add(li);
                        break;
                    }
                    double resultNum=Double.parseDouble(resultNumber);
                    double itemMax=Double.parseDouble(itemmax);
                    double itemMin=Double.parseDouble(itemin);

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
                    Log.i("TAG","eroor:"+e.getMessage());
                    showMessage("数据转换失败："+e.getMessage());
                }
            }//数字类型
            if(resultType.equals("2")){//字符类型

                String resultString=cursor.getString(cursor.getColumnIndex("itemResultString"));
                String itemReference=cursor.getString(cursor.getColumnIndex("itemReference"));
                result+=resultString+"    参考："+itemReference;
                li.add(result);
                Log.i("TAG","String:"+result);
                if(itemReference.equals(resultString)){
                    li.add("正确");
                }
                else li.add("异常");
            }
            if(resultType.equals("0")){//没有类型

                String resultString=cursor.getString(cursor.getColumnIndex("itemResultString"));
                result+=resultString+"    参考：无";
                li.add(result);
                li.add("");
            }
            if(resultType.equals("3")){//布尔类型

                String resultBlob=cursor.getString(cursor.getColumnIndex("itemResultBlob"));
                String itemReferenceBlob=cursor.getString(cursor.getColumnIndex("itemReference"));
                result+=resultBlob+"   标准值："+itemReferenceBlob;
                li.add(result);
                if(itemReferenceBlob.equals(resultBlob)){
                    li.add("正确");
                }
                else li.add("错误");
            }
            resultList.add(li);
        }
        cursor.close();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultListAdapter.notifyDataSetChanged();
            }
        });
    }
    private void showMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast= Toast.makeText(WoDeResultActivity.this,message,Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
}
