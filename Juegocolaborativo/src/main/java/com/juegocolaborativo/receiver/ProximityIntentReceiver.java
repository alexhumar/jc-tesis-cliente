package com.juegocolaborativo.receiver;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import com.juegocolaborativo.JuegoColaborativo;
import com.juegocolaborativo.activity.MapActivity;
import com.juegocolaborativo.service.PoolServiceEstados;
import com.juegocolaborativo.soap.SoapManager;
import com.juegocolaborativo.task.WSTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.util.ArrayList;

import model.Consigna;
import model.Subgrupo;

public class ProximityIntentReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1000;
    private JuegoColaborativo application;

    @Override
    public void onReceive(Context context, Intent intent) {

        String key = LocationManager.KEY_PROXIMITY_ENTERING;
        int id = intent.getIntExtra("id",-1);
        Boolean entering = intent.getBooleanExtra(key, false);
        if (entering) {
            if(intent.getAction() == MapActivity.PROX_ALERT_POI_SUBGRUPO){
                //el subgrupo avisa que ya llego a su poi y está en condiciones de comenzar el juego
                Subgrupo subgrupo = this.getApplication().getSubgrupo();

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(subgrupo.getId())));
                subgrupo.setEstado(Subgrupo.ESTADO_INICIAL);
                nameValuePairs.add(new BasicNameValuePair("idEstado", Integer.toString(subgrupo.getEstado())));

                //Ejecuto la tarea que cambia de estado al subgrupo
                WSTask setJugandoTask = new WSTask();
                setJugandoTask.setReferer(this);
                setJugandoTask.setMethodName(SoapManager.METHOD_CAMBIAR_ESTADO_SUBGRUPO);
                setJugandoTask.setParameters(nameValuePairs);
                setJugandoTask.executeTask("completeCambiarEstadoSubgrupo", "errorCambiarEstadoSubgrupo");

                //TODO USAR LOS WS setPostaActual y esSubgrupoActual para sincronizar los subgrupos.
            } else if(intent.getAction() == MapActivity.PROX_ALERT_POI_SIGUIENTE){
                //avisa que ya llego al poi siguiente
                this.getApplication().enviarFinJuego();
            } /*else {
                this.getApplication().mostrarInfoPieza(id);
            }*/
        }
        else {
            Log.e("location", "exiting");
        }
    }

    public void completeCambiarEstadoSubgrupo(SoapObject result) {
        try{
            //Genera la barrera para esperar a que todos los demas subgrupos lleguen a sus respectivas postas.
            this.getApplication().getCurrentActivity().startService(new Intent(this.getApplication().getCurrentActivity(), PoolServiceEstados.class));
        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    public void errorCambiarEstadoSubgrupo(String failedMethod){
        this.getApplication().getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public JuegoColaborativo getApplication() {
        return application;
    }

    public void setApplication(JuegoColaborativo application) {
        this.application = application;
    }
}
