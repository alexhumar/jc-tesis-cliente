package com.juegocolaborativo.soap;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.juegocolaborativo.JuegoColaborativo;

/**
 * Created by Dario on 12/12/13.
 */
public class SoapManager {

    private String namespace;
    private String url;

    public static final String METHOD_LOGIN = "login";
    public static final String METHOD_PUNTO_INICIAL = "getPuntoInicial";
    public static final String METHOD_CAMBIAR_ESTADO_SUBGRUPO = "cambiarEstadoSubgrupo";
    public static final String METHOD_GET_PIEZAS_A_RECOLECTAR = "getPiezas";
    public static final String METHOD_DECISION_TOMADA = "decisionTomada";
    public static final String METHOD_ESPERAR_ESTADO_SUBGRUPOS = "esperarEstadoSubgrupos";
    public static final String METHOD_EXISTE_PREGUNTA_SIN_RESPONDER = "existePreguntaSinResponder";
    public static final String METHOD_EXISTEN_RESPUESTAS = "existenRespuestas";
    public static final String METHOD_GUARDAR_RESPUESTA = "guardarRespuesta";
    public static final String METHOD_GET_SUBGRUPOS = "getSubgrupos";
    public static final String METHOD_GET_RESULTADOS = "getResultadoFinal";

    public SoapManager(){

        //Alex - Hago algunos cambios para probar lo de los web services.

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JuegoColaborativo.getInstance());
        //String server = preferences.getString("serverURL", "http://192.168.0.1");
        String server = preferences.getString("serverURL", "http://192.168.0.21");
        String port = preferences.getString("serverPort", "80");

        this.setNamespace(server + ":" + port + "/sfjuco/web/app_dev.php/soap/services");

        //this.setUrl(this.getNamespace() + "WSJuegoColaborativo.php?wsdl");
        this.setUrl(this.getNamespace() + "?wsdl");
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
