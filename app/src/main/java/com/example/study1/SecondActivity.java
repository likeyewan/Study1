package com.example.study1;


import android.app.NotificationManager;



import android.os.Bundle;

import android.view.View;

import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class SecondActivity extends AppCompatActivity implements View.OnClickListener{



    private NotificationManager mManager;

    private Button sendn;

    public static final String id = "channel_1";

    public static final String name = "名字";

    private NotificationUtils notificationUtils;



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sendn = findViewById(R.id.stop);

        sendn.setOnClickListener(this);

    }



    @Override

    public void onClick(View v) {

        switch (v.getId()){

            case R.id.stop:

                NotificationUtils notificationUtils = new NotificationUtils(this);

                notificationUtils.sendNotification("测试标题", "测试内容");

        }

    }

}
