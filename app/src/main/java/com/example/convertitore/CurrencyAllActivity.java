package com.example.convertitore;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.convertitore.converter.Converter;

public class CurrencyAllActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // https://medium.com/@haxzie/how-to-create-custom-recyclerview-adapter-with-multiple-view-items-b65bfdafc112
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currency_all_activity);

        Toolbar toolbar = findViewById(R.id.currencyAllToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout layout = (LinearLayout) findViewById(R.id.llAllCurrency);

        String currentFrom = getIntent().getStringExtra("from");
        double currentValue = 0;
        try {
            currentValue = Double.parseDouble(getIntent().getStringExtra("value"));
        } catch (NumberFormatException e) {
            return;
        }
        Converter converter = ConverterHandler.getCurrencyConverter();

        for (String name : converter.convertibles()) {
            TextView textView = new TextView(this);
            double exchange = converter.convert(currentFrom, name, currentValue);
            textView.setText(name + ": " + exchange);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            layout.addView(textView);
        }
    }

}