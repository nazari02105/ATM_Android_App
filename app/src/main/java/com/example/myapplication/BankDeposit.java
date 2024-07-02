package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankDeposit extends AppCompatActivity {

    private String QRValue;

    EditText value;
    TextView message;
    Button back;
    Button pay;

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_deposit);

        Intent intent = getIntent();
        QRValue = intent.getExtras().getString("QRValue");

        value = findViewById(R.id.moneyValue);
        message = findViewById(R.id.message);
        back = findViewById(R.id.back);
        pay = findViewById(R.id.pay);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        back.setOnClickListener(v -> back());
        pay.setOnClickListener(v -> pay());
    }

    private void back() {
        Intent i = new Intent(getApplicationContext(), Menu.class);
        i.putExtra("QRValue", QRValue);
        startActivity(i);
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void pay() {
        String money = value.getText().toString();
        if (money.isEmpty()) {
            message.setText("لطفا مبلغ مورد نظر را وارد کنید");
            message.setVisibility(View.VISIBLE);
            message.setBackgroundColor(Color.parseColor("#991100"));
        } else if (!isInteger(money)) {
            message.setText("لطفا یک عدد را وارد کنید");
            message.setVisibility(View.VISIBLE);
            message.setBackgroundColor(Color.parseColor("#991100"));
        } else {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    String balance = MySQLConnection.getBalanceByQrValue(QRValue);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (Integer.parseInt(balance) < Integer.parseInt(money)) {
                                message.setText("مقدار وارد شده از موجودی شما بیشتر است");
                                message.setVisibility(View.VISIBLE);
                                message.setBackgroundColor(Color.parseColor("#991100"));
                            } else {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MySQLConnection.subtractBalance(QRValue, Integer.parseInt(money));
                                    }
                                }).start();
                                message.setText("عملیات با موفقیت انجام شد");
                                message.setVisibility(View.VISIBLE);
                                message.setBackgroundColor(Color.parseColor("#009911"));
                            }
                        }
                    });
                }
            });
        }
    }
}