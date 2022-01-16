package com.example.convertitore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.convertitore.datepicker.DatePickerFragment;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;

public class CurrencyActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener/*, DatePickerDialog.OnDateSetListener*/ {

    private TextView tResult;
    private EditText eValue;
    private Spinner sCurrency1, sCurrency2;
    private Button bConverter, bConverterAll;
    private ImageView iCurrency1, iCurrency2;

    static {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currency_activity);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar toolbar = findViewById(R.id.currencyToolbar);
        setSupportActionBar(toolbar);

        tResult = findViewById(R.id.tCurrencyResult);
        eValue = findViewById(R.id.eCurrencyValue);
        sCurrency1 = findViewById(R.id.sCurrency1);
        sCurrency2 = findViewById(R.id.sCurrency2);
        iCurrency1 = findViewById(R.id.iCurrency1);
        iCurrency2 = findViewById(R.id.iCurrency2);
        bConverter = findViewById(R.id.bCurrencyConverter);
        bConverterAll = findViewById(R.id.bCurrencyConverterAll);

        bConverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convertCurrency();
            }
        });

        bConverterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CurrencyActivity.this, CurrencyAllActivity.class);
                intent.putExtra("from", sCurrency1.getSelectedItem().toString());
                intent.putExtra("to", sCurrency1.getSelectedItem().toString());
                intent.putExtra("value", eValue.getText().toString());
                startActivity(intent);
            }
        });

        eValue.setImeOptions(EditorInfo.IME_ACTION_DONE);

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this, R.array.country,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sCurrency1.setAdapter(adapter);
        sCurrency2.setAdapter(adapter);
        sCurrency1.setOnItemSelectedListener(this);
        sCurrency2.setOnItemSelectedListener(this);

        // Date Picker
        DatePickerFragment dateFrag = new DatePickerFragment();
        dateFrag.show(getSupportFragmentManager(), "DatePicker");
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
            case R.id.sCurrency1:
                iCurrency1.setImageResource(resourceId);
                break;
            case R.id.sCurrency2:
                iCurrency2.setImageResource(resourceId);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void convertCurrency() {
        String from = sCurrency1.getSelectedItem().toString();
        String to = sCurrency2.getSelectedItem().toString();
        double value = Double.parseDouble(eValue.getText().toString());

        if (value < 0) {
            return;
        }

        double result = ConverterHandler.getCurrencyConverter().convert(from, to, value);

        DecimalFormat df = new DecimalFormat("#.##");
        tResult.setText(df.format(result));
    }

    // @Override
    // public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
    //     //Calendar mCalendar = Calendar.getInstance();
    //     //mCalendar.set(Calendar.YEAR, i);
    //     //mCalendar.set(Calendar.MONTH, i1);
    //     //mCalendar.set(Calendar.DAY_OF_MONTH, i2);
    //     //String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.getTime());
    // }
}