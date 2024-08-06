package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Balance extends AppCompatActivity {

    private String QRValue;

    TextView textView;
    Button button;

    private ExecutorService executorService;
    private Handler mainHandler;

    private String formatPrice(String price) throws ParseException {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        Number number = numberFormat.parse(price);
        return numberFormat.format(number);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        Intent intent = getIntent();
        QRValue = intent.getExtras().getString("QRValue");

        textView = findViewById(R.id.balanceValue);
        button = findViewById(R.id.back);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        button.setOnClickListener(v -> back());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String balance = MySQLConnection.getBalanceByQrValue(QRValue);
                try {
                    String formattedPrice = formatPrice(balance);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText("موجودی کارت شما: " + formattedPrice + " ریال");
                        }
                    });
                } catch (ParseException e) {
                    System.out.println("Error");
                }
            }
        });
    }

    private void back() {
        Intent i = new Intent(getApplicationContext(), Menu.class);
        i.putExtra("QRValue", QRValue);
        startActivity(i);
    }
}