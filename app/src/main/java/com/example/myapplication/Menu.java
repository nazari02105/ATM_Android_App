package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Menu extends AppCompatActivity {

    private String QRValue;

    TextView depositBankText, stockText, installmentPaymentText, depositPlayerText, receiveLoanText, exitText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent intent = getIntent();
        QRValue = intent.getExtras().getString("QRValue");

        depositBankText = findViewById(R.id.depositBankText);
        stockText = findViewById(R.id.stockText);
        installmentPaymentText = findViewById(R.id.installmentPaymentText);
        depositPlayerText = findViewById(R.id.depositPlayerText);
        receiveLoanText = findViewById(R.id.receiveLoanText);
        exitText = findViewById(R.id.exitText);

        exitText.setOnClickListener(v -> exit());
        stockText.setOnClickListener(v -> balanceCheck());
        depositBankText.setOnClickListener(v -> bankDeposit());
        depositPlayerText.setOnClickListener(v -> userDeposit());
        receiveLoanText.setOnClickListener(v -> receiveLoan());
        installmentPaymentText.setOnClickListener(v -> payLoan());
    }

    private void exit() {
        Intent i = new Intent(getApplicationContext(), Login.class);
        startActivity(i);
    }

    private void balanceCheck() {
        Intent i = new Intent(getApplicationContext(), Balance.class);
        i.putExtra("QRValue", QRValue);
        startActivity(i);
    }

    private void bankDeposit() {
        Intent i = new Intent(getApplicationContext(), BankDeposit.class);
        i.putExtra("QRValue", QRValue);
        startActivity(i);
    }

    private void userDeposit() {
        Intent i = new Intent(getApplicationContext(), UserDeposit.class);
        i.putExtra("QRValue", QRValue);
        startActivity(i);
    }

    private void receiveLoan() {
        Intent i = new Intent(getApplicationContext(), ReceiveLoan.class);
        i.putExtra("QRValue", QRValue);
        startActivity(i);
    }

    private void payLoan() {
        Intent i = new Intent(getApplicationContext(), PayLoan.class);
        i.putExtra("QRValue", QRValue);
        startActivity(i);
    }
}