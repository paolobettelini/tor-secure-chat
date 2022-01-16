package com.example.convertitore;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DecimalFormat;

public class TemperatureActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView tResult;
    private EditText eValue;
    private Spinner sTemperature1, sTemperature2;
    private Button bConverter, bConverterAll;
    private ImageView iTemperature1, iTemperature2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temperature_activity);

        Toolbar toolbar = findViewById(R.id.temperatureToolbar);
        setSupportActionBar(toolbar);

        tResult = findViewById(R.id.tTemperatureResult);
        eValue = findViewById(R.id.eTemperatureValue);
        sTemperature1 = findViewById(R.id.sTemperature1);
        sTemperature2 = findViewById(R.id.sTemperature2);
        iTemperature1 = findViewById(R.id.iTemperature1);
        iTemperature2 = findViewById(R.id.iTemperature2);
        bConverter = findViewById(R.id.bTemperatureConverter);
        bConverterAll = findViewById(R.id.bTemperatureConverterAll);

        bConverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convertTemperature();
            }
        });

        bConverterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TemperatureActivity.this, TemperatureAllActivity.class);
                intent.putExtra("from", sTemperature1.getSelectedItem().toString());
                intent.putExtra("to", sTemperature2.getSelectedItem().toString());
                intent.putExtra("value", eValue.getText().toString());
                startActivity(intent);
            }
        });

        eValue.setImeOptions(EditorInfo.IME_ACTION_DONE);

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this, R.array.temperature,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sTemperature1.setAdapter(adapter);
        sTemperature2.setAdapter(adapter);
        sTemperature1.setOnItemSelectedListener(this);
        sTemperature2.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.actionCurrency:
                startActivity(new Intent(this, CurrencyActivity.class));
                return true;
            case R.id.actionTemperature:
                startActivity(new Intent(this, TemperatureActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String text = adapterView.getSelectedItem().toString();
        int resourceId = getResources().getIdentifier(text.toLowerCase(),"drawable", getPackageName());

        switch (adapterView.getId()) {
            case R.id.sTemperature1:
                iTemperature1.setImageResource(resourceId);
                break;
            case R.id.sTemperature2:
                iTemperature2.setImageResource(resourceId);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void convertTemperature() {
        String from = sTemperature1.getSelectedItem().toString();
        String to = sTemperature2.getSelectedItem().toString();
        double value = Double.parseDouble(eValue.getText().toString());

        if (value < 0) {
            return;
        }

        double result = ConverterHandler.getTemperatureConverter().convert(from, to, value);

        DecimalFormat df = new DecimalFormat("#.##");
        tResult.setText(df.format(result));
    }

}
