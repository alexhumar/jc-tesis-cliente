package com.juegocolaborativo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.juegocolaborativo.R;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import model.ResultadoFinal;

/**
 * Created by Dario on 25/03/14.
 */
public class ResultadoItemAdapter extends BaseAdapter {

    private Context context;
    private List<ResultadoFinal> items;

    public ResultadoItemAdapter(Context context, List<ResultadoFinal> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (convertView == null) {
            // Create a new view into the list.
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item, parent, false);
        }

        // Set data into the view.
        TextView grupo = (TextView) rowView.findViewById(R.id.nombreGrupo);
        TextView puntaje = (TextView) rowView.findViewById(R.id.puntaje);

        ResultadoFinal item = this.items.get(position);
        grupo.setText(item.getNombreGrupo());
        puntaje.setText(Integer.toString(item.getPuntaje()));

        return rowView;
    }

}
