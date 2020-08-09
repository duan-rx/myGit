package com.example.train.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.train.R;
import com.example.train.database.databaseDao;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SetItemActivity extends AppCompatActivity {
    private Spinner sp_setItemSelect,sp_setItemTypeSelect,sp_setItemLineColor;
    private Button bt_setItemCancel,bt_setItemSave;
    private EditText et_setItemName,et_setItemUnit,et_setItemString,et_setItemChanel,et_setItemMax,et_setItemMin,et_setItemReference;
    private List<String> itemSpinnerList= new ArrayList<>();
    private ArrayAdapter<String> itemSpinnerListAdapter;
    private databaseDao db;
    private String itemCode="";
    private int resultType=0;//1 代表数字类型，2 字符类型，3 布尔类型
    private int linneColor=-16777216;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_item);
        db=new databaseDao(this);
        init();
        setOnclick();
        initSpinner();
        String msg="";
        String sqll="select data from originalData";
        Cursor cursor=db.query(sqll);
        while (cursor.moveToNext()){
            int index=cursor.getColumnIndex("data");
            msg+=cursor.getString(index);
            et_setItemString.setVisibility(View.VISIBLE);
        }
        cursor.close();
        et_setItemString.setText(msg);
       sp_setItemSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

               if(position!=0) {
                   itemCode=itemSpinnerList.get(position).toString();
                   et_setItemName.setVisibility(View.VISIBLE);

                   queryItemMsg(itemCode);
               }
               else{
                   itemCode="";
                   et_setItemUnit.setVisibility(View.INVISIBLE);
                   et_setItemChanel.setVisibility(View.INVISIBLE);
                   et_setItemReference.setVisibility(View.INVISIBLE);
                   et_setItemName.setVisibility(View.INVISIBLE);
                   et_setItemMax.setVisibility(View.INVISIBLE);
                   et_setItemMin.setVisibility(View.INVISIBLE);

               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });
       sp_setItemTypeSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               if(position==0){

                   et_setItemUnit.setVisibility(View.INVISIBLE);
                   et_setItemChanel.setVisibility(View.INVISIBLE);
                   et_setItemMax.setVisibility(View.INVISIBLE);
                   et_setItemMin.setVisibility(View.INVISIBLE);
                   et_setItemReference.setVisibility(View.INVISIBLE);
                   sp_setItemLineColor.setVisibility(View.INVISIBLE);
               }
               else {
                   et_setItemUnit.setVisibility(View.VISIBLE);
                   et_setItemChanel.setVisibility(View.VISIBLE);
               }
               switch (position){
                   case 0:
                       resultType=0;
                       break;
                   case 1:
                       et_setItemMax.setEnabled(true);
                       et_setItemMin.setEnabled(true);
                       et_setItemMax.setVisibility(View.VISIBLE);
                       et_setItemMin.setVisibility(View.VISIBLE);
                       et_setItemReference.setEnabled(false);
                       et_setItemReference.setVisibility(View.INVISIBLE);
                       sp_setItemLineColor.setVisibility(View.VISIBLE);
                       resultType=1;
                       break;
                   case 2:
                       et_setItemMax.setEnabled(false);
                       et_setItemMin.setEnabled(false);
                       et_setItemMax.setVisibility(View.INVISIBLE);
                       et_setItemMin.setVisibility(View.INVISIBLE);
                       et_setItemReference.setEnabled(true);
                       et_setItemReference.setVisibility(View.VISIBLE);
                       sp_setItemLineColor.setVisibility(View.INVISIBLE);
                       resultType=2;
                       break;
                   case 3:
                       et_setItemMax.setEnabled(false);
                       et_setItemMin.setEnabled(false);
                       et_setItemReference.setEnabled(true);

                       et_setItemMax.setVisibility(View.INVISIBLE);
                       et_setItemMin.setVisibility(View.INVISIBLE);
                       et_setItemReference.setVisibility(View.VISIBLE);
                       sp_setItemLineColor.setVisibility(View.INVISIBLE);
                       resultType=3;
                       break;
               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {
           }
       });
       sp_setItemLineColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               switch (position){
                   case 0:
                       linneColor= Color.BLACK;
                       sp_setItemLineColor.setBackgroundColor(Color.BLACK);
                       break;
                   case 1:
                       linneColor= Color.GRAY;
                       sp_setItemLineColor.setBackgroundColor(Color.GRAY);
                       break;
                   case 2:
                       linneColor= Color.WHITE;
                       sp_setItemLineColor.setBackgroundColor(Color.WHITE);
                       break;
                   case 3:
                       linneColor= Color.RED;
                       sp_setItemLineColor.setBackgroundColor(Color.RED);
                       break;
                   case 4:
                       linneColor= Color.GREEN;
                       sp_setItemLineColor.setBackgroundColor(Color.GREEN);
                       break;
                   case 5:
                       linneColor= Color.BLUE;
                       sp_setItemLineColor.setBackgroundColor(Color.BLUE);
                       break;
                   case 6:
                       linneColor= Color.YELLOW;
                       sp_setItemLineColor.setBackgroundColor(Color.YELLOW);
                       break;
                   case 7:
                       linneColor= Color.CYAN;
                       sp_setItemLineColor.setBackgroundColor(Color.CYAN);
                       break;
                   default:
                       linneColor=Color.BLACK;
                       sp_setItemLineColor.setBackgroundColor(Color.BLACK);
                       break;

               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });
    }
    private void queryItemMsg(String itemCode){
        String sql="select itemName,itemUnit,channelCode,itemMax,itemMin,itemReference,itemResultType,lineColor from instrumentItem where itemCode='"
                +itemCode+"'";
        Cursor cursor=db.query(sql);
        while(cursor.moveToNext()) {
            int index = cursor.getColumnIndex("itemName");
            et_setItemName.setText(cursor.getString(index));
            index = cursor.getColumnIndex("itemUnit");
            et_setItemUnit.setText(cursor.getString(index));
            index = cursor.getColumnIndex("itemReference");
            et_setItemReference.setText(cursor.getString(index));
            Log.i("TAG","fh;;eoi");
            index = cursor.getColumnIndex("itemMax");
            et_setItemMax.setText(cursor.getString(index));
            index = cursor.getColumnIndex("itemMin");
            et_setItemMin.setText(cursor.getString(index));
            index = cursor.getColumnIndex("channelCode");
            et_setItemChanel.setText(cursor.getString(index));
            index = cursor.getColumnIndex("itemResultType");
            String itemResultType=cursor.getString(index).trim();
            if(!itemResultType.equals("")&&itemResultType!=null)
            switch (itemResultType){
                case "1":
                    sp_setItemTypeSelect.setSelection(1);
                    break;
                case "2":
                    sp_setItemTypeSelect.setSelection(2);
                    break;
                case "3":
                    sp_setItemTypeSelect.setSelection(3);
                    break;
                default:
                    sp_setItemTypeSelect.setSelection(0);
                    break;
            }
            index = cursor.getColumnIndex("lineColor");
            Log.i("TAG","fh;;eoi");
            String lineColor=cursor.getString(index).trim();
            if(!lineColor.equals(""))
            switch (cursor.getString(index)){
                case ""+Color.BLACK:
                    sp_setItemLineColor.setSelection(0);
                    break;
                case ""+Color.GRAY:
                    sp_setItemLineColor.setSelection(1);
                    break;
                case ""+Color.WHITE:
                    sp_setItemLineColor.setSelection(2);
                    break;
                case ""+Color.RED:
                    sp_setItemLineColor.setSelection(3);
                    break;
                case ""+Color.GREEN:
                    sp_setItemLineColor.setSelection(4);
                    break;
                case ""+Color.BLUE:
                    sp_setItemLineColor.setSelection(5);
                    break;
                case ""+Color.YELLOW:
                    sp_setItemLineColor.setSelection(6);
                    break;
                case ""+Color.CYAN:

                    sp_setItemLineColor.setSelection(7);
                    break;
                default:
                    sp_setItemLineColor.setSelection(0);
                    break;

            }
        }
        cursor.close();
    }
    private void init(){
        bt_setItemCancel=(Button)findViewById(R.id.bt_setItemCancel);
        bt_setItemSave=(Button)findViewById(R.id.bt_setItemSave);
        sp_setItemSelect=(Spinner)findViewById(R.id.sp_setItemSelect);
        sp_setItemTypeSelect=(Spinner)findViewById(R.id.sp_setItemTypeSelect);
        sp_setItemLineColor=(Spinner)findViewById(R.id.sp_setItemLineColor);
        et_setItemName=(EditText)findViewById(R.id.et_setItemName);
        et_setItemUnit=(EditText)findViewById(R.id.et_setItemUnit);
        et_setItemString=(EditText)findViewById(R.id.et_setItemString);
        et_setItemChanel=(EditText)findViewById(R.id.et_setItemChaannel);
        et_setItemMax=(EditText)findViewById(R.id.et_setItemMax);
        et_setItemMin=(EditText)findViewById(R.id.et_setItemMin);
        et_setItemReference=(EditText)findViewById(R.id.et_setItemReference);

        et_setItemName.setEnabled(false);
        et_setItemString.setEnabled(false);
        et_setItemMin.setEnabled(false);
        et_setItemMax.setEnabled(false);
        et_setItemReference.setEnabled(false);
        et_setItemUnit.setVisibility(View.INVISIBLE);
        et_setItemChanel.setVisibility(View.INVISIBLE);
        et_setItemString.setVisibility(View.INVISIBLE);
        et_setItemReference.setVisibility(View.INVISIBLE);
        et_setItemName.setVisibility(View.INVISIBLE);
        et_setItemMax.setVisibility(View.INVISIBLE);
        et_setItemMin.setVisibility(View.INVISIBLE);
        sp_setItemLineColor.setVisibility(View.INVISIBLE);


    }
    private void setOnclick(){
        OnClick onClick=new OnClick();
        bt_setItemSave.setOnClickListener(onClick);
        bt_setItemCancel.setOnClickListener(onClick);

    }
    private class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String sql="";
            String itemUnit="";
            String itemChannel="";
            String itemMax="";
            String itemMin="";
            switch (v.getId()){
                case R.id.bt_setItemSave:
                    if(itemCode.equals("")||resultType==0){
                        showMessage("选择项目和结果类型");
                        return;
                    }
                    if(resultType==1){//数字类型保存
                        itemMax=et_setItemMax.getText().toString().trim();
                        itemMin=et_setItemMin.getText().toString().trim();
                        if(itemMax.equals("")||itemMin.equals("")){
                            showMessage("最大值或最小值为空");
                            return;
                        }
                        Number min,max;
                        try{
                            min= NumberFormat.getNumberInstance(Locale.FRENCH).parse(itemMin);
                            max= NumberFormat.getNumberInstance(Locale.FRENCH).parse(itemMax);
                        }catch (Exception e){
                            showMessage("数据转换失败"+e.getMessage());
                            return;
                        }
                        itemUnit=et_setItemUnit.getText().toString();
                        itemChannel=et_setItemChanel.getText().toString();
                        sql="update instrumentItem set itemResultType="+resultType+", channelCode='"+
                                itemChannel+"', itemUnit='"+itemUnit+"', itemMax="+max+" ,itemMin="+
                                min+", lineColor="+linneColor+" where itemCode='"+itemCode+"'";
                        if(db.update(sql)){
                            showMessage("保存成功");
                            sp_setItemTypeSelect.setSelection(0);
                            sp_setItemSelect.setSelection(0);
                        }
                        else{
                            showMessage("保存失败");
                        }
                    }
                    else{
                        String itemReference=et_setItemReference.getText().toString();
                        itemUnit=et_setItemUnit.getText().toString();
                        itemChannel=et_setItemChanel.getText().toString();
                        sql="update instrumentItem set itemResultType="+resultType+", channelCode='"+
                                itemChannel+"', itemUnit='"+itemUnit+"' ,itemReference='"+
                                itemReference+"' where itemCode='"+itemCode+"'";

                        if(db.update(sql)){
                            showMessage("保存成功");
                            sp_setItemTypeSelect.setSelection(0);
                            sp_setItemSelect.setSelection(0);
                        }
                        else{
                            showMessage("保存失败");
                        }
                    }
                    break;
                case R.id.bt_setItemCancel:{
                    finish();
                }
            }

        }
    };
    private void initSpinner(){
        String  sql="select itemCode from instrumentItem";
        itemSpinnerList.clear();
        Cursor cursor=db.query(sql);
        itemSpinnerList.add("选择项目");
        while(cursor.moveToNext()) {
            int index = cursor.getColumnIndex("itemCode");
            itemSpinnerList.add(cursor.getString(index));
        }
        cursor.close();
        itemSpinnerListAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,itemSpinnerList);
        itemSpinnerListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_setItemSelect.setAdapter(itemSpinnerListAdapter);
        itemSpinnerListAdapter.notifyDataSetChanged();
    }
    private void showMessage(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SetItemActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });

    }
}
