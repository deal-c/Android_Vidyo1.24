package com.example.administrator.vidyo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.SharedPreferences.Editor;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;
import pojo.Message;
import utils.HttpUtile;
import vidyo.vidyosample.VidyoSampleApplication;


public class LoginActivity extends Activity implements OnClickListener {
    VidyoSampleApplication app;
    private EditText login_username;
    private EditText login_password;
    private Button user_login_button;
    private CheckBox    cb_remeber;
    private Button user_register_button;
    private CheckBox psdButton;
    private EditText    edit_id;
    boolean flag=false;
    static String YES = "yes";
    static String NO = "no";
    static String name, password;

    private String isMemory = "";//isMemory变量用来判断SharedPreferences有没有数据，包括上面的YES和NO
    private String FILE = "saveUserNamePwd";//用于保存SharedPreferences的文件
    private SharedPreferences sp = null;//声明一个SharedPreferences
    public static boolean isHttps = false;
    private static final String TAG = "VidyoSampleActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_login);

        initWidget();
        sp = getSharedPreferences(FILE, MODE_PRIVATE);
        isMemory = sp.getString("isMemory", NO);
//进入界面时，这个if用来判断SharedPreferences里面name和password有没有数据，有的话则直接打在EditText上面
        if (isMemory.equals(YES)) {
            name = sp.getString("name", "");
            password = sp.getString("password", "");
            login_username.setText(name);
            login_password.setText(password);
        }
        Editor editor = sp.edit();
        editor.putString(name, login_username.toString());
        editor.putString(password, login_password.toString());
        editor.commit();

    }
private void startMain(){
    Timer timer = new Timer();
   if (flag){
       TimerTask task = new TimerTask() {
        //timertask实现runnable接口,TimerTask类就代表一个在指定时间内执行的task
        @Override
        public void run() {
            Intent  intent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();

        }
    };
    timer.schedule(task, 1000 * 3);
   }

}
    public void remenber() {
        if (cb_remeber.isChecked()) {
            if (sp == null) {
                sp = getSharedPreferences(FILE, MODE_PRIVATE);
            }
            Editor edit = sp.edit();
            edit.putString("name", login_username.getText().toString());
            edit.putString("password", login_password.getText().toString());
            edit.putString("isMemory", YES);
            edit.commit();
        } else if (!cb_remeber.isChecked()) {
            if (sp == null) {
                sp = getSharedPreferences(FILE, MODE_PRIVATE);
            }
            Editor edit = sp.edit();
            edit.putString("isMemory", NO);
            edit.commit();
        }
    }

    private void initWidget()
    {
        cb_remeber=(CheckBox)findViewById(R.id.cb_remeber);
        login_username=(EditText)findViewById(R.id.edit_username);
        login_password=(EditText)findViewById(R.id.edit_password);
        user_login_button=(Button)findViewById(R.id.button_login);
        user_register_button=(Button)findViewById(R.id.cb_remeber);
        psdButton=(CheckBox) findViewById(R.id.psdButton);
        edit_id=(EditText)findViewById(R.id.edit_id);
        user_login_button.setOnClickListener(this);

        edit_id.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "您不能不能修改Id，详情请联系客服", Toast.LENGTH_LONG).show();
            }
        });
        psdButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton arg0,boolean arg1) {
                // TODO Auto-generated method stub
                if(psdButton.isChecked()){
                    //设置EditText的密码为可见的
                    login_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    //设置密码为隐藏的
                    login_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }

        });


        login_username.setOnFocusChangeListener(new OnFocusChangeListener()
        {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if(!hasFocus){
                    String username=login_username.getText().toString().trim();
                    if(username.length()<4){
                        Toast.makeText(LoginActivity.this, "用户名不能小于4个字符", Toast.LENGTH_LONG);
                    }
                }
            }

        });
        login_password.setOnFocusChangeListener(new OnFocusChangeListener()
        {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if(!hasFocus){
                    String password=login_password.getText().toString().trim();
                    if(password.length()<4){
                        Toast.makeText(LoginActivity.this, "密码不能小于4个字符", Toast.LENGTH_SHORT);
                    }
                }
            }

        });
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        switch(v.getId())
        {
            case R.id.button_login:
                if(checkEdit())
                {
                    name = login_username.getText().toString();
                    password = login_password.getText().toString();
                    login();
                    remenber();

                }




                break;
            case R.id.cb_remeber:
               // Intent intent2=new Intent(UserLogin.this,UserRegister.class);
                //startActivity(intent2);
                break;
        }
    }

    private boolean login() {
     RequestParams params =new RequestParams("http://192.168.4.143:8090/api/v1/video/vidyo/vLogin");

        Gson    gson=new Gson();
        Message   ms=new Message();
        String   qq=gson.toJson(ms);
        params.addBodyParameter("",qq);

     x.http().post(params, new Callback.CommonCallback<String>() {
         @Override
         public void onSuccess(String result) {
         //    Log.d(TAG, "登陆成功！");
           //  Log.d(TAG, result.toString());
             Toast.makeText(LoginActivity.this,"登陆成功!",Toast.LENGTH_LONG).show();
             flag=true;
             startMain();

         }

         @Override
         public void onError(Throwable ex, boolean isOnCallback) {

             Toast.makeText(LoginActivity.this,"登陆失败"+ex.toString(),Toast.LENGTH_LONG).show();
         }

         @Override
         public void onCancelled(CancelledException cex) {

         }

         @Override
         public void onFinished() {

         }
     });
        return  flag;
    }

    private boolean checkEdit(){
        if(login_username.getText().toString().trim().equals("")){
            Toast.makeText(LoginActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
        }else if(login_password.getText().toString().trim().equals("")){
            Toast.makeText(LoginActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
        }else{
            return true;
        }
        return false;
    }




}
