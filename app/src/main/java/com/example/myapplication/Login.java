package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
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

public class Login extends AppCompatActivity {

    private static final int REQUEST_IMAGE_OPEN = 1;

    private Button buttonReadQR;
    private Button buttonLogin;
    private ImageView imageView;
    private EditText editTextPassword;
    private TextView errorView;

    private String QRValue = "";

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonReadQR = findViewById(R.id.button_read_qr);
        buttonLogin = findViewById(R.id.login);
        imageView = findViewById(R.id.imageView_qr);
        editTextPassword = findViewById(R.id.editText_password);
        errorView = findViewById(R.id.error_message);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        buttonReadQR.setOnClickListener(v -> openImageSelector());
        buttonLogin.setOnClickListener(v -> login());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void login() {
        String password = editTextPassword.getText().toString();
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        } else if (QRValue.isEmpty()) {
            Toast.makeText(this, "Please select your card", Toast.LENGTH_SHORT).show();
        } else {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    boolean result = MySQLConnection.validateCard(QRValue, password);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (result) {
                                Intent i = new Intent(getApplicationContext(), Menu.class);
                                i.putExtra("QRValue", QRValue);
                                startActivity(i);
                            } else {
                                errorView.setVisibility(View.VISIBLE);
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
            QRValue = result.getText();
            Toast.makeText(this, "Scanned: " + result.getText(), Toast.LENGTH_LONG).show();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not read QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}