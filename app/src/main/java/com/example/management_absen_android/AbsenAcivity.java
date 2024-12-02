package com.example.management_absen_android;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AbsenAcivity extends AppCompatActivity {
    private static final String TAG = "AbsenActivity";

    TextView textViewId, textViewNama, textViewTime,textViewMessage;

    private DecoratedBarcodeView barcodeScannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absen_acivity);
        // Inisialisasi komponen tampilan
        barcodeScannerView = findViewById(R.id.barcodeScannerView);
        textViewId = findViewById(R.id.textViewIdValue);
        textViewNama = findViewById(R.id.textViewNameValue);
        textViewTime = findViewById(R.id.textViewTimeValue);
        textViewMessage = findViewById(R.id.message);

        // Set konfigurasi scanner
        barcodeScannerView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory());

        // Mengatur listener untuk menangani hasil pemindaian barcode
        barcodeScannerView.decodeContinuous(callback);

        // Memulai pemindaian barcode secara otomatis
        scanCode();
    }

    // Metode untuk memulai pemindaian barcode
    private void scanCode() {
        barcodeScannerView.resume();
    }


    // Callback untuk menangani hasil pemindaian barcode
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                // Mendapatkan hasil pemindaian barcode
                String barcode = result.getText();

                // Kirim data barcode ke server menggunakan Volley
                sendBarcodeToServer(barcode);

                // Hentikan pemindaian barcode setelah mendapatkan hasil
                barcodeScannerView.pause();
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };
    // Method untuk mengirim barcode ke server menggunakan Volley
    private void sendBarcodeToServer(String barcode) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constant.ABSEN;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("barcode", barcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("success")) {
                                String id = response.getString("id");
                                String name = response.getString("name");
                                String time = response.getString("jam");
                                String message = response.getString("message");

                                textViewId.setText(id);
                                textViewNama.setText(name);
                                textViewTime.setText(time);
                                textViewMessage.setText(message);

                                // Delay 5 detik sebelum mengosongkan dan memulai pemindaian kembali
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Kosongkan elemen TextView
                                        textViewId.setText("");
                                        textViewNama.setText("");
                                        textViewTime.setText("");
                                        textViewMessage.setText("Keterangan :");

                                        // Mulai pemindaian barcode lagi
                                        scanCode();
                                    }
                                }, 5000);

                            } else {
                                // Delay 5 detik sebelum mengosongkan dan memulai pemindaian kembali
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Kosongkan elemen TextView
                                        textViewId.setText("");
                                        textViewNama.setText("");
                                        textViewTime.setText("");
                                        textViewMessage.setText("Keterangan :");

                                        // Mulai pemindaian barcode lagi
                                        scanCode();
                                    }
                                }, 5000);
                                // Jika status bukan "success", Anda dapat melakukan tindakan lain sesuai kebutuhan
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Tangani respons dari server setelah pengiriman barcode
//                        Log.d(TAG, "Response: " + response.toString());
                        // Tambahkan kode Anda di sini
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Tangani error yang terjadi saat pengiriman barcode
//                Log.e(TAG, "Error: " + error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

}