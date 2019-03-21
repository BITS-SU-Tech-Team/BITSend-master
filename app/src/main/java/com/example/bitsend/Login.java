package com.example.bitsend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;

public class Login extends AppCompatActivity {
    JSONObject jsonPart;
    ArrayList<String> myClubList;
    ArrayList<Integer>  myClubIDlist;
    EditText userNameEdit;
    int SelectedClubID;
    String userString;
    SharedPreferences sharedPreferences;
    String passString;
    String clubSelected;
    public AlertDialog spotdialog;
    public AlertDialog logindialog;
    EditText passEdit;
    MaterialSpinner spinner;
    JSONObject jsonParam = new JSONObject();

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){

            case R.id.devSection:
                Intent myIntent = new Intent(getApplicationContext(),Developers.class);
                startActivity(myIntent);
                return true;
            case R.id.aboutSU:
                Toast.makeText(this, "WIP", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        spotdialog = new SpotsDialog.Builder().setContext(Login.this).setCancelable(false).setMessage("Downloading Club Data").build();

        logindialog = new SpotsDialog.Builder().setContext(Login.this).setCancelable(false).setMessage("Logging In...").build();


        setContentView(R.layout.activity_login);
        sharedPreferences = this.getSharedPreferences("com.example.bitsend", Context.MODE_PRIVATE);


        String userId = sharedPreferences.getString("userId","");


        if(!userId.equals("")){
            Toast.makeText(this, "Welcome Back! ", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), ScanItemActivity.class);
            startActivity(intent);
        }



        Button login =findViewById(R.id.button2);
        myClubList = new ArrayList<String>();
        myClubIDlist = new ArrayList<Integer>();

        userNameEdit = (EditText)findViewById(R.id.usernameEdit);
        passEdit = (EditText)findViewById(R.id.passwordEditText);

         spinner = (MaterialSpinner) findViewById(R.id.spinner);


        if(Function.isNetworkAvailable(this))
        {
            DownloadClubData clubListTask = new DownloadClubData();
            clubListTask.execute();
        }else{
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }


        spinner.setItems(myClubList);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();
                clubSelected = item;
                SelectedClubID = myClubIDlist.get(position);
            }
        });



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userString = userNameEdit.getText().toString().trim();
                passString = passEdit.getText().toString().trim();

                new SendRequest().execute();


               /* startActivity(new Intent(getApplicationContext(),Scanner.class));
                finish();*/

            }
        });
    }



    class DownloadClubData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spotdialog.show();
            spinner.setEnabled(false);


        }
        protected String doInBackground(String... args) {
            try {
                URL url = new URL("http://www.su-bitspilani.org/su/api/get_clubs_depts/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // conn.setDoOutput(true);
                //conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                //conn.setRequestProperty("X-USER-ID",userId);
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

                //   Log.d("Fund901",response.toString());


                String eventsString = jsonObject.getString("clubs");
                //minfo = jsonObject.getString("message");

                JSONArray arr = new JSONArray(eventsString);

                for (int i = 0; i < arr.length(); i++) {
                    jsonPart = arr.getJSONObject(i);


                   /* HashMap<String, String> map = new HashMap<String, String>();

                    map.put("name", jsonPart.optString("name").toString());
                    map.put("id", jsonPart.optString("id").toString());*/


                    Log.d("club name901",jsonPart.getString("assoc_name"));

                    myClubList.add(jsonPart.getString("assoc_name"));
                    myClubIDlist.add(Integer.parseInt(jsonPart.getString("user_id")));







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
            spinner.setEnabled(true);




        }



    }

    public class SendRequest extends AsyncTask<String, Void, String> {


        protected void onPreExecute(){
            logindialog.show();
        }

        protected String doInBackground(String... arg0) {

            try {
                URL url = new URL("http://www.su-bitspilani.org/su/api/login/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                //conn.setRequestProperty("", "");
                conn.setDoOutput(true);
                conn.setDoInput(true);


                jsonParam.put("username",userString );
                jsonParam.put("password",passString);
                jsonParam.put("club_id",SelectedClubID);


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
                Log.d("Resp901",response.toString());

                String message = jsonObject.getString("message");

                if(jsonObject.has("id_token")) {

                    String uID = jsonObject.getString("id_token");


                   // Toast.makeText(Login.this, "id token exists bud", Toast.LENGTH_SHORT).show();

                    //Log.d("token901",uID);
                    //Log.d("Login901",uID);

                    sharedPreferences.edit().putString("userId", uID).apply();
                    sharedPreferences.edit().putString("username", userString).apply();
                    sharedPreferences.edit().putString("userclub", clubSelected).apply();
                }



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
            logindialog.hide();
            Toast.makeText(Login.this, result, Toast.LENGTH_SHORT).show();
            if(!sharedPreferences.getString("userId","").equals("")){





                Intent intent = new Intent(getApplicationContext(), ScanItemActivity.class);
                intent.putExtra("club",clubSelected);
                intent.putExtra("username",userString);
                startActivity(intent);

            }

        }
    }


}
