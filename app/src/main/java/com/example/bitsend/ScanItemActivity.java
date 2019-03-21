package com.example.bitsend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import dmax.dialog.SpotsDialog;

public class ScanItemActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    ArrayAdapter arrayAdapter;
    String userId;
    JSONObject jsonPart;
    Button logoutBtn;
    ArrayList<String> combinedList;
    ArrayList<String> myClubItemList;
    ArrayList<String> myClubItemIDList;
    ArrayList<String> myClubMerchList;
    ArrayList<String> myClubMerchIDList;
    Button scanQRBtn;
    ListView itemListView;
    TextView assocNameTv;
    TextView usernameDisplayTv;
    MaterialSpinner eventSpin;
    MaterialSpinner merchSpin;

    Boolean eventEnabled = true;

    String eventID;
    String merchID;

    Intent intent;


    String userDispString;
    public AlertDialog spotdialog;
    String userClubString;

    String eventName;
    String merchName;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
       // Toast.makeText(this, "back", Toast.LENGTH_SHORT).show();
        finishAffinity();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_item);

        spotdialog = new SpotsDialog.Builder().setContext(ScanItemActivity.this).setCancelable(false).setMessage("Downloading Items Data").build();

        logoutBtn = (Button)findViewById(R.id.logoutBTN);



        sharedPreferences = this.getSharedPreferences("com.example.bitsend", Context.MODE_PRIVATE);

        userDispString = sharedPreferences.getString("username","");
        userClubString = sharedPreferences.getString("userclub","");



        getSupportActionBar().hide();

        scanQRBtn = (Button)findViewById(R.id.scanButton);

        usernameDisplayTv = (TextView)findViewById(R.id.userDisplayTv);



        assocNameTv = (TextView)findViewById(R.id.assocnameTv);

        combinedList = new ArrayList<String>();
        intent = getIntent();
        combinedList.add("Events");

        usernameDisplayTv.setText(userDispString);

        assocNameTv.setText(userClubString);


       eventSpin = (MaterialSpinner)findViewById(R.id.eventSpin);
       merchSpin = (MaterialSpinner)findViewById(R.id.merchSpin);




       logoutBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               sharedPreferences.edit().putString("userId", "").apply();
               sharedPreferences.edit().putString("username", "").apply();
               sharedPreferences.edit().putString("userclub", "").apply();
               Intent intent = new Intent(getApplicationContext(), Login.class);
               startActivity(intent);
           }
       });

        myClubItemList = new ArrayList<String>();
        myClubItemIDList = new ArrayList<String>();
        myClubItemList.add("None");

        myClubMerchList = new ArrayList<String>();
        myClubMerchIDList = new ArrayList<String>();

        myClubMerchList.add("None");
        myClubItemIDList.add("None");
        myClubMerchIDList.add("None");



        userId = sharedPreferences.getString("userId","");


        if(Function.isNetworkAvailable(this))
        {
            DownloadClubItems clubItemListTask = new DownloadClubItems();
            clubItemListTask.execute();
        }else{
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }

        eventSpin.setItems(myClubItemList);
        eventSpin.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();



                eventName = item;
                eventID = myClubItemIDList.get(position);


                if(position==0){
                    merchSpin.setEnabled(true);
                }else{
                    merchSpin.setEnabled(false);

                }

            }
        });

        merchSpin.setItems(myClubMerchList);
        merchSpin.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();

                merchName = item;
                merchID = myClubMerchIDList.get(position);

                if(position==0){
                    eventSpin.setEnabled(true);
                    eventEnabled = true;
                }else{
                    eventSpin.setEnabled(false);
                    eventEnabled = false;
                }
            }
        });




        scanQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if(eventEnabled && eventName==null){
                    Snackbar.make(view, "Please Select Either One Of Events/Merch", Snackbar.LENGTH_LONG).show();
                }

                if(eventEnabled && (eventName!=null) && (eventName!="None")){
                    Intent intent = new Intent(getApplicationContext(), Scanner.class);
                    intent.putExtra("item",eventID);
                    intent.putExtra("ename", eventName);
                    startActivity(intent);
                    finish();
                }
                else if((!eventEnabled) && (merchName!="None")){

                    Intent intent = new Intent(getApplicationContext(), Scanner.class);
                    intent.putExtra("item",merchID);
                    intent.putExtra("ename", merchName);
                    startActivity(intent);
                    finish();

                }


            }
        });







    }



    class DownloadClubItems extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            spotdialog.show();
            eventSpin.setEnabled(false);
            merchSpin.setEnabled(false);


        }
        protected String doInBackground(String... args) {
            try {
                URL url = new URL("http://www.su-bitspilani.org/su/api/get_club_items/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // conn.setDoOutput(true);
                //conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("X-COORD-ID",userId);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.connect();

                //conn.setDoInput(true);





                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONObject jsonObject = new JSONObject(response.toString());

                   Log.d("Items901",response.toString());


                String eventsString = jsonObject.getString("events");


                String merchString = jsonObject.getString("wears");


                //minfo = jsonObject.getString("message");

                JSONArray arr = new JSONArray(eventsString);

                JSONArray arr2 = new JSONArray(merchString);

                for (int i = 0; i < arr.length(); i++) {
                    jsonPart = arr.getJSONObject(i);

                    myClubItemList.add(jsonPart.getString("name"));
                    myClubItemIDList.add(jsonPart.getString("gm_id"));
                    //combinedList.add(jsonPart.getString("namme"));


                }

                for (int i = 0; i < arr2.length(); i++) {
                    jsonPart = arr2.getJSONObject(i);

                    myClubMerchList.add(jsonPart.getString("name"));
                    myClubMerchIDList.add(jsonPart.getString("gm_id"));


                }








                conn.disconnect();
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error!";
            }
        }
        @Override
        protected void onPostExecute(String xml) {


            spotdialog.hide();
            eventSpin.setEnabled(true);
            merchSpin.setEnabled(true);

            Log.d("ItemArray901",myClubItemList.toString());






        }



    }


}
