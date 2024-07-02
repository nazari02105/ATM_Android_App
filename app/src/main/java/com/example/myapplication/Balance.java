package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Balance extends AppCompatActivity {

    private String QRValue;

    TextView textView;
    Button button;

    private ExecutorService executorService;
    private Handler mainHandler;

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
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("موجودی کارت شما: " + balance + " ریال");
                    }
                });
            }
        });
    }

    private void back() {
        Intent i = new Intent(getApplicationContext(), Menu.class);
        i.putExtra("QRValue", QRValue);
        startActivity(i);
    }
}