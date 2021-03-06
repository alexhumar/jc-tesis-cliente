package com.juegocolaborativo.service;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.juegocolaborativo.JuegoColaborativo;

import java.util.Timer;
import java.util.TimerTask;

public class PoolServiceRespuestas extends Service {

    public static final long UPDATE_INTERVAL = 5000;
    public static final long DELAY_INTERVAL  = 0;

    private Timer timer = new Timer();

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _startService();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        _shutdownService();
    }

    private void _startService() {
        /* Alex - por lo que entendi, se crea un thread aparte donde se ejecuta la invocacion al metodo esperarRespuestasSubgrupos a intervalos regulares.
         * Dicho metodo invoca al WebService "existenRespuestas" que retorna las respuestas para el subgrupo que consulta, en caso que existan. */
        TimerTask asynchronousTask;
        final Handler handler = new Handler();
        asynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            doServiceWork();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        timer.scheduleAtFixedRate(asynchronousTask, DELAY_INTERVAL, UPDATE_INTERVAL);
    }

    private void doServiceWork() {
        // Llamar al WS
        ((JuegoColaborativo) getApplication()).esperarRespuestasSubgrupos();
    }

    private void _shutdownService() {
        if (timer != null){
            timer.cancel();
        }
    }
}