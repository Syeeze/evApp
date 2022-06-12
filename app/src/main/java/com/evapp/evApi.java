package com.evapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class evApi extends AsyncTask<String, String, String> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d("Task3", "POST");
        String temp = "Not Gained";
        try {
            temp = GET(strings[0],strings[1]);
            Log.d("REST", temp);
            return temp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    private String GET(String x,String y) throws IOException {
        String corona_API = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/storesByGeo/json?lat="+x+"&lng="+y+"&m=1000";

        String data = "";
        String myUrl3 = String.format(corona_API, x);



        try {
            URL url = new URL(myUrl3);
            Log.d("CoronaApi", "The response is :" + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            String line;
            String result = "";

            BufferedReader bf;
            bf = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = bf.readLine()) != null) {
                result = result.concat(line);

            }
            Log.d("CoronaApi", "The response is :" + result);
            JSONObject root = new JSONObject(result);

            JSONArray coronaArray = root.getJSONArray("stores");
            for(int i = 0; i< coronaArray.length() ; i++){
                JSONObject item = coronaArray.getJSONObject(i);
                Log.d("corona",item.getString("name"));
                ev_item corona_item = new ev_item(
                        item.getString("lat"),
                        item.getString("longi"),
                        item.getString("addr"),
                );
                MainActivity.ev_list.add(ev_item);
            }
            startFlagForCoronaApi=false;



        } catch (NullPointerException | JsonSyntaxException | JSONException e) {
            e.printStackTrace();
        }


        return data;
    }
    public class ev_item {

        private String addr;
        private String lat;
        private String longi;


        public ev_item( String lat, String longi,String addr) {
            this.addr = addr;
            this.lat = lat;
            this.longi = longi;
        }
    }