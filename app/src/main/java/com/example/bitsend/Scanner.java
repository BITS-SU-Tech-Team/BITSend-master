package com.example.bitsend;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.zxing.Result;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.NonNull;
import dmax.dialog.SpotsDialog;

public class Scanner extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    SharedPreferences sharedPreferences;
    String userId;
    Intent intent;
    public AlertDialog spotdialog;
    private static final int RC_PERMISSION = 10;
    private Boolean mPermission;
    JSONObject jsonParam = new JSONObject();
    MaterialStyledDialog dialog;
    String eventName;
    String eventID;
    String qrCode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);


        spotdialog = new SpotsDialog.Builder().setContext(Scanner.this).setCancelable(false).setMessage("Sending QR to server..").build();

        getSupportActionBar().hide();

        intent = getIntent();
        eventName = intent.getStringExtra("ename");
        eventID = intent.getStringExtra("item");








        CodeScannerView codeScannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, codeScannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {






                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {




                                dialog = new MaterialStyledDialog.Builder(Scanner.this)
                                        .setTitle(eventName)
                                        .setIcon(R.drawable.su_logo)
                                        .setCancelable(false)
                                        .setStyle(Style.HEADER_WITH_TITLE)
                                        .setDescription(result.toString())
                                        .setPositiveText("Confirm")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                                                //  Toast.makeText(Scanner.this, "positive", Toast.LENGTH_SHORT).show();
                                                mCodeScanner.startPreview();
                                                new SendRequest().execute();
                                            }
                                        })
                                        .setNegativeText("Cancel")
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                                                //Toast.makeText(Scanner.this, "Cancelled!", Toast.LENGTH_SHORT).show();
                                                mCodeScanner.startPreview();
                                            }
                                        })
                                        .build();



                                qrCode = result.toString();







                               dialog.show();

                                //Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_SHORT).show();

                            }
                        });

                    }





        });

        mCodeScanner.setErrorCallback(new ErrorCallback() {
            @Override
            public void onError(@NonNull Exception error) {new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show();
                        mCodeScanner.startPreview();
                    }
                };
                Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_LONG).show();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                mPermission = false;
                requestPermissions(new String[]{Manifest.permission.CAMERA}, RC_PERMISSION);
            } else {
                mPermission = true;
            }

        }


        sharedPreferences = this.getSharedPreferences("com.example.bitsend", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId","");


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull String[] permissions, @android.support.annotation.NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermission = true;
                mCodeScanner.startPreview();
            } else {
                mPermission = false;
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mPermission) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        finish();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }



    public class SendRequest extends AsyncTask<String, Void, String> {


        protected void onPreExecute(){
            spotdialog.show();
        }

        protected String doInBackground(String... arg0) {

            try {
                URL url = new URL("http://www.su-bitspilani.org/su/api/update_delivery_status/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-COORD-ID",userId);
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                //conn.setRequestProperty("", "");
                conn.setDoOutput(true);
                conn.setDoInput(true);


                jsonParam.put("qr_code",qrCode);
                jsonParam.put("item_id",eventID);


                //Log.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                // os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                // Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONObject jsonObject = new JSONObject(response.toString());
                String message = jsonObject.getString("message");






                //Toast.makeText(SignUpActivity.this, message.toString(), Toast.LENGTH_SHORT).show();

                conn.disconnect();
                return message;
            } catch (Exception e) {
                e.printStackTrace();
                return "Error!";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            spotdialog.hide();
           Toast.makeText(Scanner.this, result, Toast.LENGTH_SHORT).show();


        }
    }


}

