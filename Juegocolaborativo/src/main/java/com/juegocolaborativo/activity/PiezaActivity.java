package com.juegocolaborativo.activity;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.juegocolaborativo.JuegoColaborativo;
import com.juegocolaborativo.R;
import com.juegocolaborativo.service.PoolServiceRespuestas;
import com.juegocolaborativo.soap.SoapManager;
import com.juegocolaborativo.task.WSTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

import model.Respuesta;
import model.Subgrupo;

public class PiezaActivity extends DefaultActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pieza);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pieza, menu);
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_pieza, container, false);

            TextView tituloPieza = (TextView) rootView.findViewById(R.id.tituloPieza);
            TextView descripcionPieza = (TextView) rootView.findViewById(R.id.descripcionPieza);

            JuegoColaborativo app = (JuegoColaborativo) getActivity().getApplication();

            //instancio las respuestas vacias de la pieza actual
            app.getSubgrupo().setCantidadRespuestas(0);
            HashMap<Integer, Respuesta> respuestas = new HashMap<Integer, Respuesta>();
            for (Subgrupo subgrupo : app.getSubgrupo().getGrupo().getSubgrupos()){
                Respuesta respuesta = new Respuesta(subgrupo);
                respuestas.put(subgrupo.getId(), respuesta);
            }

            /* Alex - para optimizar el consumo de memoria, la estructura de datos deberia ser SparseArray<Integer,SparseArray<Integer,Respuesta>>*/
            //HashMap<Integer,HashMap<Integer, Respuesta>> hashMap = new HashMap<Integer, HashMap<Integer, Respuesta>>();
            /* Alex - en hashMap se guarda, para la pieza actual, las respuestas de cada subgrupo (en este punto las respuestas estan vacías). */
            //hashMap.put(app.getPiezaActual().getId(),respuestas);
            app.getSubgrupo().setRespuestas(respuestas);
            tituloPieza.setText(app.getPiezaActual().getNombre());
            descripcionPieza.setText(app.getPiezaActual().getDescripcion());

            TextView tituloConsigna = (TextView) rootView.findViewById(R.id.tituloConsigna);
            TextView descripcionConsigna = (TextView) rootView.findViewById(R.id.descripcionConsigna);

            tituloConsigna.setText(app.getSubgrupo().getGrupo().getConsigna().getNombre());
            descripcionConsigna.setText(app.getSubgrupo().getGrupo().getConsigna().getDescripcion());


            final View buttonConsultar = rootView.findViewById(R.id.button_consultar);
            final View buttonEnviar = rootView.findViewById(R.id.button_enviar);
            final View buttonRespuestas = rootView.findViewById(R.id.button_respuestas);

            //aqui setear o no la visibilidad de los botones de consular y respuestas
            buttonConsultar.setVisibility(View.VISIBLE);
            buttonRespuestas.setVisibility(View.GONE);

            buttonEnviar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(((TextView) rootView.findViewById(R.id.justificacion)).getText().toString().trim().length() == 0){
                            Toast toast = Toast.makeText(rootView.getContext(), "Por favor, ingrese la justificación de su respuesta", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {

                            JuegoColaborativo app = (JuegoColaborativo) getActivity().getApplication();
                            //seteo el flag del subgrupo esperando rta para mostrar o no el boton
                            ToggleButton respuesta = (ToggleButton) rootView.findViewById(R.id.respuestaDecision);

                            ((PiezaActivity) getActivity()).showProgressDialog("Enviando respuesta");

                            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);

                            int idSubgrupo = app.getSubgrupo().getId();
                            int idPieza = app.getPiezaActual().getId();
                            int cumple = respuesta.isChecked() ? 1 : 0;
                            int decisionFinal = 1;
                            String justificacion = ((TextView) rootView.findViewById(R.id.justificacion)).getText().toString();

                            nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(idSubgrupo)));
                            nameValuePairs.add(new BasicNameValuePair("idPieza", Integer.toString(idPieza)));
                            nameValuePairs.add(new BasicNameValuePair("cumple", Integer.toString(cumple)));
                            nameValuePairs.add(new BasicNameValuePair("justificacion", justificacion));
                            nameValuePairs.add(new BasicNameValuePair("decisionFinal", Integer.toString(decisionFinal)));

                            WSTask decisionTask = new WSTask();
                            decisionTask.setReferer(getActivity());
                            decisionTask.setMethodName(SoapManager.METHOD_DECISION_TOMADA);
                            decisionTask.setParameters(nameValuePairs);
                            decisionTask.executeTask("completeDecisionTomada", "errorDecisionTomadaTask");
                        }

                    }
                }


            );

            buttonConsultar.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(((TextView) rootView.findViewById(R.id.justificacion)).getText().toString().trim().length() == 0){
                                Toast toast = Toast.makeText(rootView.getContext(), "Por favor, ingrese la justificación de su respuesta", Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                            JuegoColaborativo app = (JuegoColaborativo) getActivity().getApplication();
                            //seteo el flag del subgrupo esperando rta para mostrar o no el boton
                            buttonConsultar.setVisibility(View.INVISIBLE);
                            buttonRespuestas.setVisibility(View.VISIBLE);

                            ToggleButton respuesta = (ToggleButton) rootView.findViewById(R.id.respuestaDecision);

                            ((PiezaActivity) getActivity()).showProgressDialog("Enviando consulta");

                            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);

                            int idSubgrupo = app.getSubgrupo().getId();
                            int idPieza = app.getPiezaActual().getId();
                            int cumple = respuesta.isChecked() ? 1 : 0;
                            int decisionFinal = 0;
                            String justificacion = ((TextView) rootView.findViewById(R.id.justificacion)).getText().toString();

                            nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(idSubgrupo)));
                            nameValuePairs.add(new BasicNameValuePair("idPieza", Integer.toString(idPieza)));
                            nameValuePairs.add(new BasicNameValuePair("cumple", Integer.toString(cumple)));
                            nameValuePairs.add(new BasicNameValuePair("justificacion", justificacion));
                            nameValuePairs.add(new BasicNameValuePair("decisionFinal", Integer.toString(decisionFinal)));

                            WSTask decisionTask = new WSTask();
                            decisionTask.setReferer(getActivity());
                            decisionTask.setMethodName(SoapManager.METHOD_DECISION_TOMADA);
                            decisionTask.setParameters(nameValuePairs);
                            decisionTask.executeTask("completeConsultaEnviada", "errorConsultaEnviada");
                        }
                        }
                    }
            );

            buttonRespuestas.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getActivity(), RespuestasActivity.class));
                        }
                    }
            );
            return rootView;
        }
    }

    public void completeDecisionTomada(SoapObject result) {
        ((JuegoColaborativo) getApplication()).getPiezaActual().setVisitada(true);
        //stopeo el pool service de esperar respuestas si fue activado
        this.stopService(new Intent(new Intent(this, PoolServiceRespuestas.class)));
        this.startActivity(new Intent(this, MapActivity.class));
    }

    public void errorDecisionTomadaTask(String failedMethod){
        showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void completeConsultaEnviada(SoapObject result) {
        //aca deberia crear un nuevo PoolService para esperar las respuestas
        hideProgressDialog();
        this.startService(new Intent(this, PoolServiceRespuestas.class));
        this.startActivity(new Intent(this, RespuestasActivity.class));
    }

    public void errorConsultaEnviada(String failedMethod){
        showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    @Override
    protected void onResume() {
        enviarMsjConsultaRespondida();
        super.onResume();
    }
}
