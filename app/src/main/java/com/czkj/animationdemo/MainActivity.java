package com.czkj.animationdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.czkj.animationdemo.widget.RulerView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private TextView mValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mValue = (TextView) findViewById(R.id.value);

        final RulerView rulerView = (RulerView) findViewById(R.id.ruler);
        rulerView.setOnValueChangeListener(new RulerView.OnValueChangeListener() {
            @Override
            public void onValueChange(double value) {
                mValue.setText(String.format(Locale.getDefault(), "%.1f", value));
            }
        });

        rulerView.setMaxValue(500);
        rulerView.setMinValue(0);
        rulerView.ignore(true);
        rulerView.setValue(rulerView.getValue());
    }
}
