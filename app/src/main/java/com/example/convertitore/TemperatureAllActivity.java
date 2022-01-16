package com.example.convertitore;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.convertitore.converter.Converter;

public class TemperatureAllActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temperature_all_activity);

        Toolbar toolbar = findViewById(R.id.allTemperatureToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout layout = (LinearLayout) findViewById(R.id.llAllTemperature);

        String currentFrom = getIntent().getStringExtra("from");
        double currentValue = Double.parseDouble(getIntent().getStringExtra("value"));
        Converter converter = ConverterHandler.getTemperatureConverter();

        for (String name : converter.convertibles()) {
            TextView textView = new TextView(this);
            double exchange = converter.convert(currentFrom, name, currentValue);
            textView.setText(name + ": " + exchange);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            layout.addView(textView);
        }
    }

}
