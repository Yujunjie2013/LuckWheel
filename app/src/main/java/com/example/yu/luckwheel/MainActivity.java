package com.example.yu.luckwheel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.yu.luckwheel.view.LuckWheelView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private LuckWheelView luckWheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        luckWheelView = findViewById(R.id.luck);
        findViewById(R.id.iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                int i = random.nextInt(8);
                i += 1;
                Log.d("yujj","当前坐标:"+i);
//                Toast.makeText(MainActivity.this, "当前position:" + i, Toast.LENGTH_SHORT).show();
                luckWheelView.startRotate(2);

            }
        });
    }
}
