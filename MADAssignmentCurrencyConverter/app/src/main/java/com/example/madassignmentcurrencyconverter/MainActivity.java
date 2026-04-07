package com.example.madassignmentcurrencyconverter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    // UI elements
    EditText etAmount;
    Spinner spinnerFrom, spinnerTo;
    Button btnConvert, btnSettings;
    TextView tvResult;

    // Currency options
    String[] currencies = {"INR", "USD", "JPY", "EUR"};

    // Conversion rates relative to INR (1 INR = ?)
    // INR=1, USD=0.012, JPY=1.78, EUR=0.011
    double[] ratesFromINR = {1.0, 0.012, 1.78, 0.011};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setContentView
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link UI elements
        etAmount = findViewById(R.id.etAmount);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        btnConvert = findViewById(R.id.btnConvert);
        btnSettings = findViewById(R.id.btnSettings);
        tvResult = findViewById(R.id.tvResult);

        // Set up spinners with currency list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Default: From INR, To USD
        spinnerFrom.setSelection(0);
        spinnerTo.setSelection(1);

        // Convert button click
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertCurrency();
            }
        });

        // Settings button click
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void convertCurrency() {
        String amountStr = etAmount.getText().toString().trim();

        // Validate input
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int fromIndex = spinnerFrom.getSelectedItemPosition();
        int toIndex = spinnerTo.getSelectedItemPosition();

        // Convert: amount → INR first → then to target currency
        double amountInINR = amount / ratesFromINR[fromIndex];
        double result = amountInINR * ratesFromINR[toIndex];

        // Show result
        tvResult.setText(String.format("%.2f %s = %.2f %s",
                amount, currencies[fromIndex],
                result, currencies[toIndex]));
    }
}