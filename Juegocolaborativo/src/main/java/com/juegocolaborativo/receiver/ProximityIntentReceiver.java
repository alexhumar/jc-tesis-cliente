package com.juegocolaborativo.receiver;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.juegocolaborativo.JuegoColaborativo;
import com.juegocolaborativo.activity.MapActivity;

public class ProximityIntentReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1000;
    private JuegoColaborativo application;

    @Override
    public void onReceive(Context context, Intent intent) {

        String key = LocationManager.KEY_PROXIMITY_ENTERING;
        int id = intent.getIntExtra("id",-1);
        Boolean entering = intent.getBooleanExtra(key, false);
        if (entering) {
            if(intent.getAction() == MapActivity.PROX_ALERT_PUNTO_INICIAL){
                //avisa que ya llego al poi incial y está en condiciones de comenzar el juego
                this.getApplication().enviarJugando();
            } else if(intent.getAction() == MapActivity.PROX_ALERT_PUNTO_FINAL){
                //avisa que ya termino de recorrer todos los pois
                this.getApplication().enviarFinJuego();
            } else {
                this.getApplication().mostrarInfoPieza(id);
            }
        }
        else {
            Log.e("location", "exiting");
        }
    }

    public JuegoColaborativo getApplication() {
        return application;
    }

    public void setApplication(JuegoColaborativo application) {
        this.application = application;
    }
}
