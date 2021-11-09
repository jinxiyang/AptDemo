package com.yang.apt.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.yang.apt.lib.annotation.BindView;
import com.yang.apt.lib.bindview.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.textView)
    public TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Log.i("======", "onCreate: " + (textView == null));
    }
}