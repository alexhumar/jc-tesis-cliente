package com.juegocolaborativo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;

import com.juegocolaborativo.JuegoColaborativo;
import com.juegocolaborativo.R;
import com.juegocolaborativo.adapter.ResultadosAdapter;

import model.PiezaARecolectar;

/**
 * Created by drapetti on 18/12/13.
 */
public class DefaultActivity extends Activity {

    private ProgressDialog progressDialog = null;
    private ResultadosAdapter resultadosAdapter;

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    public void showProgressDialog(String msg){
        if (!(this.getProgressDialog() != null && this.getProgressDialog().isShowing())){
            ProgressDialog pd = new ProgressDialog(this);
            pd.setCancelable(false);
            this.setProgressDialog(pd);
        }

        this.getProgressDialog().setMessage(msg);
        this.getProgressDialog().show();
    }

    public void hideProgressDialog(){
        if (this.getProgressDialog() != null && this.getProgressDialog().isShowing()){
            this.getProgressDialog().cancel();
        }
    }

    /**
     * Muestra un error generico
     */
    public void showDialogError(String mensaje, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(title)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        this.hideProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Seteo la actividad que se está ejecutando en la aplicación
        ((JuegoColaborativo) getApplication()).setCurrentActivity(this);
    }

    @Override
    public void onBackPressed() {
        return;
    }

    public void removerPuntoInicial(){}

    public void addPiezasARecolectarToMap(PiezaARecolectar pieza){}

    public void removeProximityAlert(String intentAction){}

    public void markerVisitado(Integer id){}

    public void removerPuntoFinal(){}

    public void enviarMsjConsultaRespondida(){
        //si vuelvo de responder una consulta, mostrar mensaje
        //TODO volver a meter la consulta actual en el Subgrupo. (model.jar)
        if ((((JuegoColaborativo) getApplication()).getSubgrupo().getConsultaActual() != null) && (((JuegoColaborativo) getApplication()).getSubgrupo().getConsultaActual().getRespondida() == 1)){
            //borro la consulta actual porque ya fue respondida
            ((JuegoColaborativo) getApplication()).getSubgrupo().setConsultaActual(null);
            showDialogError("Se envió la respuesta al subgrupo, gracias!", "Respuesta");
        }
    }

    public ResultadosAdapter getResultadosAdapter() {
        return resultadosAdapter;
    }

    public void setResultadosAdapter(ResultadosAdapter resultadosAdapter) {
        this.resultadosAdapter = resultadosAdapter;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, SetPreferenceActivity.class);
                startActivityForResult(i, 1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
