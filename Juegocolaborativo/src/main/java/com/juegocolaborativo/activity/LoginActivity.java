package com.juegocolaborativo.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import com.juegocolaborativo.JuegoColaborativo;
import com.juegocolaborativo.R;
import com.juegocolaborativo.soap.SoapManager;
import com.juegocolaborativo.task.WSTask;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.ksoap2.serialization.SoapPrimitive;

import model.Coordenada;
import model.Grupo;
import model.Poi;
import model.Subgrupo;

public class LoginActivity extends DefaultActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Comentario de prueba :: Chache
        /*Alex - Le agrega al FrameLayout de activity_login.xml el fragment declarado en esta clase*/
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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
            final View rootView = inflater.inflate(R.layout.fragment_login, container, false);

            final View button = rootView.findViewById(R.id.button_login);
            button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(((TextView) rootView.findViewById(R.id.subgrupo)).getText().toString().trim().length() == 0){
                            Toast toast = Toast.makeText(rootView.getContext(), "Por favor, ingrese un subgrupo", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {

                            ((LoginActivity) getActivity()).showProgressDialog("Verificando");

                            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                            //obtengo el subgrupo de la vista
                            String subgrupo = ((TextView) rootView.findViewById(R.id.subgrupo)).getText().toString();
                            //password por ahora no utilizamos
                            //String password = ((TextView) rootView.findViewById(R.id.password)).getText().toString();

                            //los agrego para pasarlos como parametro
                            nameValuePairs.add(new BasicNameValuePair("subgrupo", subgrupo));

                            //instancio la clase que ejecuta el web service
                            WSTask loginTask = new WSTask();
                            loginTask.setReferer(getActivity());
                            loginTask.setMethodName(SoapManager.METHOD_LOGIN);
                            loginTask.setParameters(nameValuePairs);
                            loginTask.executeTask("completeLoginTask", "errorLoginTask");
                        }

                    }
                }
            );

            return rootView;
        }

    }

    public void completeLoginTask(SoapObject result) {
        SoapPrimitive res = (SoapPrimitive) result.getProperty("valorInteger");
        int idSubgrupo = Integer.parseInt(res.toString());
        if(idSubgrupo == -1){
            showDialogError("Nombre de subgrupo incorrecto", "Error login");
        } else {

            ((JuegoColaborativo) getApplication()).setSubgrupo(new Subgrupo(idSubgrupo));

            this.hideProgressDialog();

            this.showProgressDialog("Obteniendo el punto de encuentro inicial");

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

            nameValuePairs.add(new BasicNameValuePair("idSubgrupo", res.toString()));

            WSTask loginTask = new WSTask();
            loginTask.setReferer(this);
            loginTask.setMethodName(SoapManager.METHOD_PUNTO_INICIAL);
            loginTask.setParameters(nameValuePairs);
            loginTask.executeTask("completePuntoInicial", "errorLoginTask");
        }
    }

    public void completePuntoInicial(SoapObject result) {
        for (int i = 0; i < result.getPropertyCount(); i++) {
            double latitud = Double.parseDouble(((SoapObject) result.getProperty(i)).getProperty("latitud").toString());
            double longitud = Double.parseDouble(((SoapObject) result.getProperty(i)).getProperty("longitud").toString());
            int poiInicial = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("inicial").toString());
            int poiFinal = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("final").toString());

            if(poiInicial == 1){
                ((JuegoColaborativo) getApplication()).getSubgrupo().setPoiInicial(new Poi((new Coordenada(latitud, longitud))));
            } else {
                if(poiFinal == 1){
                    ((JuegoColaborativo) getApplication()).getSubgrupo().setPoiFinal(new Poi((new Coordenada(latitud, longitud))));
                }
            }
        }

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(((JuegoColaborativo) getApplication()).getSubgrupo().getId())));

        WSTask subgruposTask = new WSTask();
        subgruposTask.setReferer(this);
        subgruposTask.setMethodName(SoapManager.METHOD_GET_SUBGRUPOS);
        subgruposTask.setParameters(nameValuePairs);
        subgruposTask.executeTask("completeGetSubgrupos", "errorGetSubgrupos");
    }

    public void errorLoginTask(String failedMethod){
        showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    /*Alex - por lo que entendi, un subgrupo queda apuntando a un grupo que contiene a todos los demas subgrupos.
    * Luego se lanza MapActivity. */
    public void completeGetSubgrupos(SoapObject result) {
        try{
            ((JuegoColaborativo) getApplication()).getSubgrupo().setGrupo(new Grupo());
            ((JuegoColaborativo) getApplication()).getSubgrupo().getGrupo().setSubgrupos(new ArrayList<Subgrupo>());
            for (int i = 0; i < result.getPropertyCount(); i++) {
                int idSubgrupo = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("id").toString());
                if (idSubgrupo != ((JuegoColaborativo) getApplication()).getSubgrupo().getId()) {
                    String nombreSubgrupo = ((SoapObject) result.getProperty(i)).getProperty("nombre").toString();
                    Subgrupo subgrupo = new Subgrupo(idSubgrupo);
                    subgrupo.setNombre(nombreSubgrupo);
                    ((JuegoColaborativo) getApplication()).getSubgrupo().getGrupo().getSubgrupos().add(subgrupo);
                }
            }
            this.startActivity(new Intent(this, MapActivity.class));
        } catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    public void errorGetSubgrupos(String failedMethod){
        showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

}
