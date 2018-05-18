package com.example.times.jproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.times.jproject.Login.LoginActivity;

public class SettingActivity extends Activity{
    private SharedPreferences settings;
    private AlertDialog.Builder alertBuilder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_menu);

    }

    public void onBtnClick(View v){

        switch (v.getId()){
            case R.id.inbody :
                Intent intent = new Intent(this, InbodyActivity.class);
                startActivity(intent);
                finish();
            case R.id.logout :
                settings = getSharedPreferences("settings", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear();
                editor.commit();

                alertBuilder = new AlertDialog.Builder(SettingActivity.this);
                alertBuilder
                        .setTitle("알림")
                        .setMessage("로그아웃되었습니다.")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();


        }
    }
}
