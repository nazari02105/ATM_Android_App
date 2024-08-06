package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_OPEN = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    private Button buttonGenerateQR;
    private Button buttonSaveQR;
    private Button buttonReadQR;
    private Button buttonLogin;
    private ImageView imageViewQR;
    private EditText editTextFilename;
    private EditText editTextPassword;
    private Bitmap generatedBitmap;
    private String QRCodeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonGenerateQR = findViewById(R.id.button_generate_qr);
        buttonSaveQR = findViewById(R.id.button_save_qr);
        buttonReadQR = findViewById(R.id.button_read_qr);
        buttonLogin = findViewById(R.id.login);
        imageViewQR = findViewById(R.id.imageView_qr);
        editTextFilename = findViewById(R.id.editText_filename);
        editTextPassword = findViewById(R.id.editText_password);

        buttonGenerateQR.setOnClickListener(v -> {
            String filename = editTextFilename.getText().toString();
            if (!filename.isEmpty()) {
                generateQRCode(filename);
            } else {
                Toast.makeText(this, "Please enter a filename", Toast.LENGTH_SHORT).show();
            }
        });

        buttonSaveQR.setOnClickListener(v -> {
            if (generatedBitmap != null) {
                String filename = editTextFilename.getText().toString();
                String password = editTextPassword.getText().toString();
                if (!filename.isEmpty()) {
                    saveQRCode(generatedBitmap, filename, password);
                } else {
                    Toast.makeText(this, "Please enter a filename", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Generate a QR code first", Toast.LENGTH_SHORT).show();
            }
        });

        buttonLogin.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), Login.class);
            startActivity(i);
        });

        buttonReadQR.setOnClickListener(v -> openImageSelector());

        requestPermissions();
    }

    private void generateQRCode(String text) {
        QRCodeValue = text;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            generatedBitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageViewQR.setImageBitmap(generatedBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not generate QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCode(Bitmap bitmap, String filename, String password) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(directory, filename + ".png");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Toast.makeText(this, "QR Code saved", Toast.LENGTH_SHORT).show();
            System.out.println(filename + "-" + password + "-" + QRCodeValue);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MySQLConnection.addCard(filename, QRCodeValue, password);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not save QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageSelector() {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Uri uri = Uri.fromFile(directory);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);  // This line suggests the initial directory
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
            Toast.makeText(this, "Scanned: " + result.getText(), Toast.LENGTH_LONG).show();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not read QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
        }
    }
}