package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

public class ReceiveLoan extends AppCompatActivity {

    private RadioGroup radioGroup1, radioGroup2;
    private Button submitButton, backButton;
    TextView message;

    private String QRValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_loan);

        Intent intent = getIntent();
        QRValue = intent.getExtras().getString("QRValue");

        radioGroup1 = findViewById(R.id.radioGroup1);
        radioGroup2 = findViewById(R.id.radioGroup2);
        submitButton = findViewById(R.id.submitButton);
        backButton = findViewById(R.id.backButton);
        message = findViewById(R.id.message);

        backButton.setOnClickListener(v -> back());

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId1 = radioGroup1.getCheckedRadioButtonId();
                int selectedId2 = radioGroup2.getCheckedRadioButtonId();

                if (selectedId1 != -1 && selectedId2 != -1) {
                    RadioButton selectedRadioButton1 = findViewById(selectedId1);
                    RadioButton selectedRadioButton2 = findViewById(selectedId2);

                    String selectedValue1 = selectedRadioButton1.getText().toString();
                    String selectedValue2 = selectedRadioButton2.getText().toString();

                    int amount;
                    int months;

                    if (selectedValue1.equals("۵۰۰,۰۰۰"))
                        amount = 500000;
                    else if (selectedValue1.equals("۱,۰۰۰,۰۰۰"))
                        amount = 1000000;
                    else
                        amount = 5000000;

                    if (selectedValue2.equals("۱۲ ماه"))
                        months = 12;
                    else if (selectedValue2.equals("۲۴ ماه"))
                        months = 24;
                    else
                        months = 36;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MySQLConnection.addLoan(amount, months, QRValue);
                            MySQLConnection.addBalance(QRValue, amount);
                        }
                    }).start();

                    message.setText("وام با موفقیت واریز شد");
                    message.setVisibility(View.VISIBLE);
                    message.setBackgroundColor(Color.parseColor("#009911"));
                } else {
                    message.setText("لطفا هر دو مورد را انتخاب بفرمایید");
                    message.setVisibility(View.VISIBLE);
                    message.setBackgroundColor(Color.parseColor("#991100"));
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