package com.juegocolaborativo.task;

import android.os.AsyncTask;
import android.util.Log;

import com.juegocolaborativo.soap.SoapManager;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.apache.http.NameValuePair;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class WSTask extends AsyncTask<Void, Void, SoapObject> {

    private Object referer = null;
    private Method method = null;
    private String errorCallback = null;
    private Boolean primitive = true;
    private String methodName = null;
    private ArrayList<NameValuePair> parameters = null;

    public Object getReferer() {
        return referer;
    }

    public void setReferer(Object referer) {
        this.referer = referer;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getErrorCallback() {
        return errorCallback;
    }

    public void setErrorCallback(String errorCallback) {
        this.errorCallback = errorCallback;
    }

    public ArrayList<NameValuePair> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<NameValuePair> parameters) {
        this.parameters = parameters;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String method_name) {
        this.methodName = method_name;
    }

    public Boolean getPrimitive() {        return primitive;    }

    public void setPrimitive(Boolean primitive) {        this.primitive = primitive;    }

    protected SoapObject doInBackground(Void... params) {

        try {

            SoapManager soapManager = new SoapManager();
            // Modelo el request
            SoapObject request = new SoapObject(soapManager.getNamespace(), this.getMethodName());

            // Paso parametros al WS
            for(NameValuePair parameter : this.getParameters()) {
                request.addProperty(parameter.getName(), parameter.getValue());
            }

            // Modelo el Sobre
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            sobre.setOutputSoapObject(request);
            //sobre.env = "http://schemas.xmlsoap.org/soap/envelope"; //Alex - Prueba

            // Modelo el transporte
            HttpTransportSE transporte = new HttpTransportSE(soapManager.getUrl());

            transporte.debug = true; //Alex - prueba

            // Llamada
            String theCall = soapManager.getNamespace() + "#" + this.getMethodName(); //Alex - prueba
            transporte.call(theCall, sobre);

            Log.d("WS Prueba",transporte.responseDump); //Alex - prueba

            // Resultado
            SoapObject resultado;
            resultado = (SoapObject) sobre.getResponse();

            return resultado;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ERROR_IO", ' '+e.toString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR", ' '+e.getMessage());
            return null;
        }
    }

    protected void onPostExecute(SoapObject result) {
        try {
            // Aca debería chequear si todo llegó bien
            if (result != null){
                (this.getMethod()).invoke(this.getReferer(), result);
            } else if (this.getErrorCallback() != null){
                Method errorMethod = this.getReferer().getClass().getMethod(this.getErrorCallback(), String.class);
                errorMethod.invoke(this.getReferer(), this.getMethodName());
            }
        } catch (Exception e) {
            Log.e("ERROR", "Error en invocación de método de callback: "+ this.getMethod() + " - ERROR: " + e.getStackTrace());
            //AndroidLogger.logger.error("Error en invocación de método de callback: "+ this.getMethod());
        }
    }

    public void executeTask(String methodCallback, String errorMethodCallback){
        try {
            this.setMethod(this.getReferer().getClass().getMethod(methodCallback, SoapObject.class));
            this.setErrorCallback(errorMethodCallback);
            this.execute();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}
