package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class PayLoan extends AppCompatActivity {

    Button backBtn;

    private String QRValue;

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
        setContentView(R.layout.activity_pay_loan);
        Intent intent = getIntent();
        QRValue = intent.getExtras().getString("QRValue");

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        backBtn = findViewById(R.id.back);
        backBtn.setOnClickListener(v -> back());

        TableLayout loansTable = findViewById(R.id.loansTable);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Loan> loans = MySQLConnection.getLoansByQRValue(QRValue);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (Loan loan : loans) {
                            TableRow row = new TableRow(PayLoan.this);

                            TextView amount = new TextView(PayLoan.this);
                            String price = String.valueOf(loan.getLoanAmount());
                            try {
                                String formattedPrice = formatPrice(price);
                                amount.setText(formattedPrice);
                            } catch (ParseException e) {
                                System.out.println("Error");
                            }
                            amount.setPadding(8, 8, 8, 8);

                            TextView period = new TextView(PayLoan.this);
                            period.setText(String.valueOf(loan.getRepaymentPeriod()));
                            period.setPadding(8, 8, 8, 8);

                            TextView paid = new TextView(PayLoan.this);
                            paid.setText(String.valueOf(loan.getInstallmentsPaid()));
                            paid.setPadding(8, 8, 8, 8);

                            Button shouldPay = new Button(PayLoan.this);
                            shouldPay.setPadding(8, 8, 8, 8);

                            if (loan.getRepaymentPeriod() == loan.getInstallmentsPaid()) {
                                shouldPay.setText("");
                                shouldPay.setVisibility(View.INVISIBLE);
                                row.setBackgroundColor(Color.parseColor("#006400"));
                            } else {
                                shouldPay.setText("پرداخت");
                                row.setBackgroundColor(Color.parseColor("#8B0000"));

                                shouldPay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                int payValue = loan.getLoanAmount() / loan.getRepaymentPeriod();
                                                MySQLConnection.subtractBalance(QRValue, payValue);
                                                MySQLConnection.payLoan(loan.getId());
                                            }
                                        }).start();
                                        recreate();
                                    }
                                });
                            }

                            row.addView(amount);
                            row.addView(period);
                            row.addView(paid);
                            row.addView(shouldPay);

                            loansTable.addView(row);
                        }
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