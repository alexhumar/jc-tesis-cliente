package com.juegocolaborativo.task;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JSONTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... arg) {
        String linha = "";
        String retorno = "";
        String url = arg[0];

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);

        try {
            HttpResponse response = client.execute(get);

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) { // Ok
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                while ((linha = rd.readLine()) != null) {
                    retorno += linha;
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retorno;
    }

    @Override
    protected void onPostExecute(String result) {
        System.out.println(result);
        /*
        // Create here your JSONObject...
        JSONObject json = createJSONObj(result);
        customMethod(json); // And then use the json object inside this method
        */
    }

}
