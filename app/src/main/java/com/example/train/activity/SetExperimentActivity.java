package com.example.train.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.train.R;
import com.example.train.adapter.instrumentListAdapter;
import com.example.train.constants.message;
import com.example.train.database.databaseDao;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class SetExperimentActivity extends AppCompatActivity {
    private Spinner sp_exHeadSelect,sp_exItemSelect;
    private Button bt_exAdd,bt_exChange,bt_exDelete,bt_exSave;
    private EditText et_ex,et_ex2;
    private ListView lv_exShowItem;
    private instrumentListAdapter instrumentListAdapter;
    private int status=0;//0仪器设置，检测项目设置
    private int selectStatus=5;//新增：0,修改：1，删除：2 不做任何操作
    private List<List> instrumentList=new ArrayList<>();
    private List<String> itemSpinnerList= new ArrayList<>();
    private ArrayAdapter<String> itemSpinnerListAdapter;
    private int listPosition=0;
    databaseDao db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_experiment);
        setTitle("实验项目");
        init();

        setOnClick();
        reset();
        instrumentListAdapter=new instrumentListAdapter(this);
        db=new databaseDao(this);
        instrumentListAdapter.bindData(instrumentList);
        lv_exShowItem.setAdapter(instrumentListAdapter);


        String sql="select inCode,inName from instrument where status = 1";
        initListView(sql,"inCode","inName","仪器代码","仪器名称");

        lv_exShowItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               listViewItemClick(position);
            }
        });
        sp_exHeadSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spHeaderItemClick(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void   spHeaderItemClick(int position){
        String sql="";
        Intent intent=null;
        switch(position){
            case 0:///仪器设置
                reset();
                status=0;
                sp_exItemSelect.setVisibility(View.INVISIBLE);
                sql="select inCode,inName from instrument where status = 1";
                initListView(sql,"inCode","inName","仪器代码","仪器名称");
                break;
            case 1://检测项目设置
                reset();
                status=1;
                sp_exItemSelect.setVisibility(View.VISIBLE);
                sql="select inCode from instrument where status = 1";
                itemSpinnerList.clear();
                Cursor cursor=db.query(sql);
                itemSpinnerList.add("选择仪器");
                while(cursor.moveToNext()) {
                    int index = cursor.getColumnIndex("inCode");
                    itemSpinnerList.add(cursor.getString(index));
                }
                cursor.close();
                itemSpinnerListAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,itemSpinnerList);
                itemSpinnerListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_exItemSelect.setAdapter(itemSpinnerListAdapter);
                itemSpinnerListAdapter.notifyDataSetChanged();

                instrumentList.clear();
                instrumentListAdapter.notifyDataSetChanged();
                sql="select instrument.inCode,instrumentItem.itemName from instrument,instrumentItem where instrument.inCode=instrumentItem.inCode and instrument.status=1 ";
                //sql="select itemCode,itemName from instrumentItem";
               initListView(sql,"iinstrument.inCode","instrumentItem.itemName","仪器代码","项目名称");
                //initListView(sql,"itemName","itemCode","仪器代码","项目名称");


                break;
            case 2://项目设置
                status=1;
                intent=new Intent(SetExperimentActivity.this,SetItemActivity.class);
                startActivity(intent);
                break;
                /*
            case 4://通道号设置
                intent=new Intent(SetExperimentActivity.this,SetMateActivity.class);
                startActivity(intent);
                break;
                 */
            case 3://运行测试
                intent=new Intent(SetExperimentActivity.this,setRunActivity.class);
                startActivity(intent);
            default:
                break;
        }
    }
    private void listViewItemClick(int position){
        List<String> list;
        switch (status){
            case 0:
                reset();
                 list=instrumentList.get(position);
                et_ex.setText(list.get(0).toString());
                et_ex2.setText(list.get(1).toString());
                listPosition=position;
                break;
            case 1:
                reset();
                list=instrumentList.get(position);
                sp_exItemSelect.setVisibility(View.VISIBLE);
                et_ex2.setText(list.get(1).toString());
                String sql="select itemCode from instrumentItem " +
                        "where instrumentItem.inCode='"+list.get(0).toString()+"' and instrumentItem.itemName='"+
                        list.get(1).toString()+"'";
                Cursor cursor= db.query(sql);
                while (cursor.moveToNext())
                {
                    int index=cursor.getColumnIndex("itemCode");
                    et_ex.setText(cursor.getString(index).toString());
                }
                cursor.close();
                et_ex2.setText(list.get(1).toString());
                listPosition=position;
                int count;
                sql="select count(inCode) from instrument where status = 1";
                cursor=db.query(sql);
                {
                cursor.moveToFirst();
                 count = cursor.getInt(0);
                cursor.close();
                }
            for(int i=0;i<count;i++){
                if(itemSpinnerList.get(i).toString().equals(list.get(0)))
                sp_exItemSelect.setSelection(i);
            }

            break;

            case 2:
                break;
            default:
                break;
        }

    }

    private void init(){
        sp_exHeadSelect=(Spinner)findViewById(R.id.sp_exHeadSelect);
        sp_exItemSelect=(Spinner)findViewById(R.id.sp_exItemSelect);
        et_ex=(EditText)findViewById(R.id.et_ex1);
        et_ex2=(EditText)findViewById(R.id.et_ex2);
        bt_exAdd=(Button)findViewById(R.id.bt_exAdd);
        bt_exChange=(Button)findViewById(R.id.bt_exChange);
        bt_exDelete=(Button)findViewById(R.id.bt_exDelete);
        bt_exSave=(Button)findViewById(R.id.bt_exSave);
        lv_exShowItem=(ListView)findViewById(R.id.lv_exShowItem);

    }
    private void setOnClick(){
        OnClick onClick=new OnClick();
        bt_exSave.setOnClickListener(onClick);
        bt_exDelete.setOnClickListener(onClick);
        bt_exChange.setOnClickListener(onClick);
        bt_exAdd.setOnClickListener(onClick);
    }
    private class OnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.bt_exAdd:
                    add();
                    selectStatus=0;
                    break;
                case R.id.bt_exChange:
                    selectStatus=1;
                    et_ex2.setEnabled(true);
                     break;
                case R.id.bt_exDelete:
                    selectStatus=2;
                    delete();
                    break;
                case R.id.bt_exSave:
                    save();
                    break;
                default:
                    break;
            }
        }
    }
    private void add(){
        if(status==0){//仪器设置
            et_ex.setText("");
            et_ex.setEnabled(true);
            et_ex.setHint("仪器代码：数字和字母组合");
            et_ex2.setEnabled(true);
            et_ex2.setText("");
            et_ex2.setHint("仪器名称");
        }
        if(status==1){//检测项目设置
            et_ex.setText("");
            et_ex.setEnabled(true);
            et_ex.setHint("项目代码：数字和字母组合");
            et_ex2.setEnabled(true);
            et_ex2.setText("");
            et_ex2.setHint("项目名称");
        }


    }
    private void delete(){
        if(status==0){
            final String instrumentCode = et_ex.getText().toString();
            if(instrumentCode.equals("")){
                showMessage("无删除内容");
                return;
            }
            new AlertDialog.Builder(SetExperimentActivity.this)
                    .setIcon(R.mipmap.train)
                    .setTitle("删除"+instrumentCode)
                    .setMessage("确认删除吗？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String sql="delete from instrument where inCode='" +instrumentCode+"'";
                            if(db.update(sql)){
                                showMessage("删除成功");
                                instrumentList.remove(listPosition);
                                instrumentListAdapter.notifyDataSetChanged();
                                reset();
                            }
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    })
                    .show();

        }
        if(status==1){
            final String itemCode = et_ex.getText().toString();
            if(itemCode.equals("")){
                showMessage("无删除内容");
                return;
            }
            new AlertDialog.Builder(SetExperimentActivity.this)
                    .setTitle("删除"+itemCode)
                    .setMessage("确认删除吗？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String sql="delete  from instrumentItem where itemCode='"+itemCode+"'";
                            if(db.update(sql)){
                                showMessage("删除成功");
                                instrumentList.remove(listPosition);
                                instrumentListAdapter.notifyDataSetChanged();
                                reset();
                            }
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    })
                    .show();

        }
    }
    private void save(){
        if(selectStatus==5)
        {
            showMessage("选择操作再保存");
            return;
        }
        //仪器设置
        if(status==0){
            if(selectStatus==0){//新增
                String instrumentCode = et_ex.getText().toString();
                String instrumentName= et_ex2.getText().toString();
                et_ex.setSelected(true);
                if(instrumentCode.equals("")||instrumentName.equals(""))
                {
                    showMessage("信息不能为空");
                    return;
                }
                String sql="select inCode from instrument";
                Cursor cursor=db.query(sql);
                while(cursor.moveToNext()) {
                    int index = cursor.getColumnIndex("inCode");
                    if(cursor.getString(index).equals(instrumentCode)){
                        showMessage("仪器代码已存在");
                        cursor.close();
                    }
                }
                cursor.close();
                sql="insert into instrument(inCode,inName) values('"+instrumentCode+"','"+instrumentName+"')";
                if(db.insert(sql)){
                    List<String> list=new ArrayList<>();
                    list.add(instrumentCode);
                    list.add(instrumentName);
                    instrumentList.add(list);
                    instrumentListAdapter.notifyDataSetChanged();
                    showMessage("新增成功");
                    reset();
                }
                else {
                    showMessage("新增失败");
                    add();
                }
            }

            if(selectStatus==1){
                et_ex2.setEnabled(true);
                String instrumentName=et_ex2.getText().toString();
                String instrumentCode=et_ex.getText().toString();
                if(instrumentName.equals("")){
                   showMessage("仪器名不能为空");
                    return;
                }
                String sql = "update instrument set inName='"+instrumentName+"' where inCode ='"+instrumentCode+"'";
                if(db.update(sql)){
                    reset();
                    instrumentList.remove(listPosition);
                    List<String> list=new ArrayList<>();
                    list.add(instrumentCode);
                    list.add(instrumentName);
                    instrumentList.add(list);
                    instrumentListAdapter.notifyDataSetChanged();
                    showMessage("跟新成功");
                }
                else showMessage("更新失败");
            }
            selectStatus=5;
        }
        //检测项目设置
        if(status==1){
            if(sp_exItemSelect.getSelectedItemId()==0){
                showMessage("选择仪器");
                return;
            }
            if(selectStatus==0){//新增
                String instrumentCode = et_ex.getText().toString();
                String instrumentName= et_ex2.getText().toString();
                if(instrumentCode.equals("")||instrumentName.equals(""))
                {
                    showMessage("信息不能为空");
                    return;
                }
                String sql="select itemCode from instrumentItem";
                Cursor cursor=db.query(sql);
                while(cursor.moveToNext()) {
                    int index = cursor.getColumnIndex("itemCode");
                    if(cursor.getString(index).equals(instrumentCode)){
                        showMessage("项目代码已存在");
                        cursor.close();
                    }
                }
                cursor.close();
                String inCode=sp_exItemSelect.getSelectedItem().toString();
                setTitle(inCode);
                sql="insert into instrumentItem(inCode,itemCode,itemName) values('"+inCode+"','"+instrumentCode+"','"+instrumentName+"')";

                if(db.insert(sql)){
                    List<String> list=new ArrayList<>();
                    list.add(inCode);
                    list.add(instrumentCode);
                    instrumentList.add(list);
                    instrumentListAdapter.notifyDataSetChanged();
                    showMessage("新增成功");
                    reset();
                }
                else {
                    showMessage("新增失败");
                    add();
                    et_ex2.setText(sql);
                }
            }
            if(selectStatus==1){//修改
                et_ex2.setEnabled(true);
                String itemName=et_ex2.getText().toString();
                String itemCode=et_ex.getText().toString();
                String inCode=sp_exItemSelect.getSelectedItem().toString();
                if(itemName.equals("")){
                    showMessage("项目名不能为空");
                    return;
                }
                String sql = "update instrumentItem set itemName='"+itemName+"',inCode='"+inCode+"' where itemCode ='"+itemCode+"'";
                if(db.update(sql)){
                    reset();
                    List<String> list=new ArrayList<>();
                    list =instrumentList.get(listPosition);
                    inCode=list.get(0).toString();
                    instrumentList.remove(listPosition);
                    list.clear();
                    list.add(inCode);
                    list.add(itemName);
                    instrumentList.add(list);
                    instrumentListAdapter.notifyDataSetChanged();
                    showMessage("跟新成功");

                }
                else showMessage("更新失败");
                sp_exItemSelect.setVisibility(View.VISIBLE);
            }
        }
        selectStatus=5;
    }
    private void initListView(String sql,String column,String column2,String msg,String msg2){
        //if(status==0){
            message.message=msg;
            message.message2=msg2;
            Cursor cursor=db.query(sql);
            instrumentList.clear();
            while(cursor.moveToNext()) {
                List<String> list=new ArrayList<>();
                int index = cursor.getColumnIndex(column);
                list.add(cursor.getString(index));
                index = cursor.getColumnIndex(column2);
                list.add(cursor.getString(index));

                instrumentList.add(list);
                instrumentListAdapter.notifyDataSetChanged();
            }
            cursor.close();
       // }
    }
    private void reset(){
        et_ex.setEnabled(false);
        et_ex.setText("");
        et_ex2.setEnabled(false);
        et_ex2.setText("");
        sp_exItemSelect.setVisibility(View.INVISIBLE);
    }
    private void showMessage(String message){
        Toast.makeText(SetExperimentActivity.this,message,Toast.LENGTH_LONG).show();
    }
}
