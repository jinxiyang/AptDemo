package com.yang.apt.demo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yang.apt.lib.annotation.BindView;
import com.yang.apt.lib.bindview.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.textView)
    public TextView textView;

    @BindView(R.id.button)
    public Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //new MainActivity_ViewBinding().bind(this);
        ButterKnife.bind(this);

        button.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "APT 成功了", Toast.LENGTH_SHORT).show();
        });
    }
}