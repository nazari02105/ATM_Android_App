package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserDeposit extends AppCompatActivity {

    private static final int REQUEST_IMAGE_OPEN = 1;

    private String QRValue;
    private String otherUserQRValue = "";

    private EditText value;
    private TextView message;
    private Button back;
    private Button pay;
    private Button buttonReadQR;
    private ImageView imageView;

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_deposit);

        Intent intent = getIntent();
        QRValue = intent.getExtras().getString("QRValue");

        buttonReadQR = findViewById(R.id.button_read_qr);
        value = findViewById(R.id.moneyValue);
        message = findViewById(R.id.message);
        back = findViewById(R.id.back);
        pay = findViewById(R.id.pay);
        imageView = findViewById(R.id.imageView_qr);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        buttonReadQR.setOnClickListener(v -> openImageSelector());
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
        } else if (otherUserQRValue.isEmpty()) {
            message.setText("لطفا کد QR کاربر مورد نظر را وارد کنید");
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
                                        MySQLConnection.addBalance(otherUserQRValue, Integer.parseInt(money));
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

    private void openImageSelector() {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Uri uri = Uri.fromFile(directory);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        startActivityForResult(intent, REQUEST_IMAGE_OPEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_OPEN && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imageView.setImageBitmap(bitmap);
                    readQRCode(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Could not read QR Code", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void readQRCode(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] intArray = new int[width * height];
        bitmap.getPixels(intArray, 0, width, 0, 0, width, height);

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, intArray);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        QRCodeMultiReader reader = new QRCodeMultiReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        try {
            Result result = reader.decode(binaryBitmap, hints);
            otherUserQRValue = result.getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not read QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}