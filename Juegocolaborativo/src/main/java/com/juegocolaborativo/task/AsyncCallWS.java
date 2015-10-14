package com.juegocolaborativo.task;

import android.os.AsyncTask;
import android.util.Log;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;

public class AsyncCallWS extends AsyncTask<String, Void, ArrayList<Object>> {
    private static final String TAG = "ActivityLogin";
    private static final String METHOD_NAME = "poseeBeca";
    private static final String NAMESPACE = "http://192.168.0.15/bkt/src/WebServices";
    private static final String ACTION = "http://192.168.0.15/bkt/src/WebServices/poseeBeca";
    private static final String URL = "http://192.168.0.15/bkt/src/WebServices/servicioQueOfrecemos.php?wsdl";

    protected  ArrayList<Object> doInBackground(String... params) {
        Log.i(TAG,"doInBackground");
        try {

            // Modelo el request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("idUsuario", "1"); // Paso parametros al WS

            // Modelo el Sobre
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            sobre.dotNet = true;
            sobre.setOutputSoapObject(request);

            // Modelo el transporte
            HttpTransportSE transporte = new HttpTransportSE(URL);

            // Llamada
            transporte.call(ACTION, sobre);

            // Resultado
            SoapPrimitive resultado = (SoapPrimitive) sobre.getResponse();

            Log.i("Resultado", resultado.toString());

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }
        return null;
    }

    protected  ArrayList<Object> onPostExecute(String result) {
        System.out.println(result);
        return new ArrayList<Object>();
        /*
        // Create here your JSONObject...
        JSONObject json = createJSONObj(result);
        customMethod(json); // And then use the json object inside this method
        */
    }

}
