package com.juegocolaborativo.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.juegocolaborativo.JuegoColaborativo;
import com.juegocolaborativo.R;
import com.juegocolaborativo.adapter.ResultadoItemAdapter;
import com.juegocolaborativo.adapter.ResultadoItemSubgrupoAdapter;
import com.juegocolaborativo.soap.SoapManager;
import com.juegocolaborativo.task.WSTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import model.Resultado;
import model.ResultadoFinal;

public class ResultadosActivity extends DefaultActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.resultados, menu);
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
            final View rootView = inflater.inflate(R.layout.fragment_resultados, container, false);

            JuegoColaborativo app = (JuegoColaborativo) getActivity().getApplication();
            List<ResultadoFinal> list = new ArrayList<ResultadoFinal> (app.getResultadoFinal());
            List<Resultado> listSubgrupo = new ArrayList<Resultado>();
            listSubgrupo.add(app.getSubgrupo().getResultado());

            ListView listaResultados = (ListView) rootView.findViewById(R.id.listView);
            listaResultados.setAdapter(new ResultadoItemAdapter((ResultadosActivity) this.getActivity(), list));

            ListView listaResultadosSubgrupo = (ListView) rootView.findViewById(R.id.listViewSugbrupo);
            listaResultadosSubgrupo.setAdapter(new ResultadoItemSubgrupoAdapter((ResultadosActivity) this.getActivity(), listSubgrupo));

            return rootView;
        }
    }

}
