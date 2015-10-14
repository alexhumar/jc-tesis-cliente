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

public class ResponderActivity extends DefaultActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responder);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.responder, menu);
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
            final View rootView = inflater.inflate(R.layout.fragment_responder, container, false);

            TextView tituloPieza = (TextView) rootView.findViewById(R.id.tituloPieza);
            TextView descripcionPieza = (TextView) rootView.findViewById(R.id.descripcionPieza);

            JuegoColaborativo app = (JuegoColaborativo) getActivity().getApplication();

            tituloPieza.setText(app.getSubgrupo().getConsultaActual().getNombrePieza());
            descripcionPieza.setText(app.getSubgrupo().getConsultaActual().getDescripcionPieza());

            TextView tituloConsigna = (TextView) rootView.findViewById(R.id.tituloConsigna);
            TextView descripcionConsigna = (TextView) rootView.findViewById(R.id.descripcionConsigna);

            tituloConsigna.setText(app.getSubgrupo().getGrupo().getConsigna().getNombre());
            descripcionConsigna.setText(app.getSubgrupo().getGrupo().getConsigna().getDescripcion());

            TextView cumpleConsulta = (TextView) rootView.findViewById(R.id.cumpleConsulta);
            cumpleConsulta.setText("Cumple: "+((app.getSubgrupo().getConsultaActual().getCumple() == 0)?"No":"Sí"));

            TextView descripcionConsulta = (TextView) rootView.findViewById(R.id.descripcionConsulta);
            descripcionConsulta.setText("Justificación: "+app.getSubgrupo().getConsultaActual().getJustificacion());

            final View buttonEnviar = rootView.findViewById(R.id.button_responder);
            buttonEnviar.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(((TextView) rootView.findViewById(R.id.justificacion)).getText().toString().trim().length() == 0){
                                Toast toast = Toast.makeText(rootView.getContext(), "Por favor, ingrese la justificación de su respuesta", Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                JuegoColaborativo app = (JuegoColaborativo) getActivity().getApplication();
                                ToggleButton respuesta = (ToggleButton) rootView.findViewById(R.id.respuestaConsulta);

                                ((ResponderActivity) getActivity()).showProgressDialog("Enviando respuesta");

                                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);

                                int idConsulta = app.getSubgrupo().getId();
                                int acuerdo = respuesta.isChecked() ? 1 : 0;
                                String justificacion = ((TextView) rootView.findViewById(R.id.justificacion)).getText().toString();

                                app.getSubgrupo().getConsultaActual().setRespondida(1);

                                nameValuePairs.add(new BasicNameValuePair("idSubgrupoConsultado", Integer.toString(app.getSubgrupo().getConsultaActual().getId())));
                                nameValuePairs.add(new BasicNameValuePair("acuerdo", Integer.toString(acuerdo)));
                                nameValuePairs.add(new BasicNameValuePair("justificacion", justificacion));

                                WSTask decisionTask = new WSTask();
                                decisionTask.setReferer(getActivity());
                                decisionTask.setMethodName(SoapManager.METHOD_GUARDAR_RESPUESTA);
                                decisionTask.setParameters(nameValuePairs);
                                decisionTask.executeTask("completeGuardarRespuesta", "errorGuardarRespuesta");

                                getActivity().finish();
                            }

                        }
                    }


            );

            return rootView;
        }
    }

    public void completeGuardarRespuesta(SoapObject result) {
        this.finish();
    }

    public void errorGuardarRespuesta(String failedMethod){
        showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

}