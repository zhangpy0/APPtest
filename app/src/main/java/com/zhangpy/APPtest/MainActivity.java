package com.zhangpy.APPtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.zhangpy.APPtest.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private EmulatorDetector emulatorDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        emulatorDetector = new EmulatorDetector();
        boolean isEmulator = emulatorDetector.readSysProperty(this, new EmulatorCheckCallback() {
            @Override
            public void onEmulatorDetected(String result) {
                // 在这里处理检测结果，例如使用弹窗显示
                Toast.makeText(MainActivity.this, "Emulator!!! |\n " + result, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRealDeviceDetected(String result) {
                Toast.makeText(MainActivity.this, "RealDevice!!! |\n" + result, Toast.LENGTH_LONG).show();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS}, 100);
        } else {
            // 已经有权限，可以获取手机号和联系人
            getPhoneAndContacts();
        }


        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            // 判断是否所有的权限都被授予了
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                // 所有的权限都授予了，可以获取手机号和联系人
                getPhoneAndContacts();
            } else {
                // 有权限被拒绝了，提示用户
                Toast.makeText(this, "需要授予相关权限才能使用该功能", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getPhoneAndContacts() {
        // 获取手机号
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            // 有权限，可以获取手机号
            String phoneNumber = telephonyManager.getLine1Number();
        } else {
            // 没有权限，提示用户或者处理异常
            Toast.makeText(this, "需要授予读取电话状态的权限才能获取手机号", Toast.LENGTH_SHORT).show();
        }

        String phoneNumber = telephonyManager.getLine1Number();
        // 获取联系人
        List<String> contacts = new ArrayList<>();
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 获取联系人姓名
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                // 获取联系人手机号
                @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contacts.add(name + ": " + number);
            }
            cursor.close();
        }
        // 用一个弹窗将这些信息展示
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("手机号和联系人");
        builder.setMessage("你的手机号是：" + phoneNumber + "\n你的联系人有：" + TextUtils.join("\n", contacts));
        builder.setPositiveButton("发送短信", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 向指定手机号发送短信
                sendSMS("18090988060", "测试短信");
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(this, "短信已发送", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


}