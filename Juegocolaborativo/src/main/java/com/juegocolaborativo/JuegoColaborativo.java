package com.juegocolaborativo;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.juegocolaborativo.activity.DefaultActivity;
import com.juegocolaborativo.activity.MapActivity;
import com.juegocolaborativo.activity.PiezaActivity;
import com.juegocolaborativo.activity.ResponderActivity;
import com.juegocolaborativo.activity.RespuestasActivity;
import com.juegocolaborativo.activity.ResultadosActivity;
import com.juegocolaborativo.service.PoolServiceEstados;
import com.juegocolaborativo.service.PoolServiceColaborativo;
import com.juegocolaborativo.soap.SoapManager;
import com.juegocolaborativo.task.WSTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import model.Consigna;
import model.Coordenada;
import model.Grupo;
import model.PiezaARecolectar;
import model.Poi;
import model.Respuesta;
import model.Resultado;
import model.ResultadoFinal;
import model.Subgrupo;
import model.Consulta;

/**
 * Created by Dario on 04/01/14.
 */
public class JuegoColaborativo extends Application {

    private DefaultActivity currentActivity;
    private Subgrupo subgrupo;
    private PiezaARecolectar piezaActual;
    private Consulta consultaQueMeHicieronActual;
    private TreeSet<ResultadoFinal> resultadoFinal = new TreeSet<ResultadoFinal>();
    private static JuegoColaborativo instance;

    public TreeSet<ResultadoFinal> getResultadoFinal() {
        return resultadoFinal;
    }

    public void setResultadoFinal(TreeSet<ResultadoFinal> resultadoFinal) {
        this.resultadoFinal = resultadoFinal;
    }

    public DefaultActivity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(DefaultActivity currentActivity) {
        this.currentActivity = currentActivity;
    }

    public PiezaARecolectar getPiezaActual() {
        return piezaActual;
    }

    public void setPiezaActual(PiezaARecolectar piezaActual) {
        this.piezaActual = piezaActual;
    }

    public Subgrupo getSubgrupo() {
        return subgrupo;
    }

    public void setSubgrupo(Subgrupo subgrupo) {
        this.subgrupo = subgrupo;
    }

    public void enviarJugando(){
        //detengo el proximity alert del poi subgrupo y limpio el mapa (borro marker poi inicial)
        this.getCurrentActivity().removerPuntoInicial();
        this.getCurrentActivity().showDialogError("Felicitaciones, has llegado al poi asignado! Hora de jugar!", "JuegoColaborativo");

        //creo la lista de consultas vacias del subgrupo
        this.getSubgrupo().setIdsConsultasQueMeHicieron(new ArrayList<Integer>());

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(getSubgrupo().getId())));
        getSubgrupo().setEstado(getSubgrupo().ESTADO_JUGANDO);
        nameValuePairs.add(new BasicNameValuePair("idEstado", Integer.toString(getSubgrupo().getEstado())));

        //llamo al PoolServiceEstados para chequear si un subgrupo realizó una pregunta que debo responder
        /* Alex - desde que arranca el juego, se inicia este servicio, que lo que hace es, cada 5 segundos, chequear si existen preguntas hechas por otro
        * subgrupo mediante la invocacion al webservice "existenPreguntasSinResponder" */
        this.getCurrentActivity().startService(new Intent(getCurrentActivity(), PoolServiceColaborativo.class));

        //Ejecuto la tarea que cambia de estado al subgrupo
        WSTask setJugandoTask = new WSTask();
        setJugandoTask.setReferer(this);
        setJugandoTask.setMethodName(SoapManager.METHOD_CAMBIAR_ESTADO_SUBGRUPO);
        setJugandoTask.setParameters(nameValuePairs);
        setJugandoTask.executeTask("completeEnviarJugando", "errorEnviarJugando");
    }

    /*
        Método que es llamado por el PoolServiceEstados que devuelve 1 si todos los subgrupos llegaron a un determinado estado, 0 de lo contrario
     */
    public void esperarEstadoSubgrupos(){
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("idEstado", Integer.toString(getSubgrupo().getEstado())));

        WSTask esperarEstadoTask = new WSTask();
        esperarEstadoTask.setReferer(this);
        esperarEstadoTask.setMethodName(SoapManager.METHOD_ESPERAR_ESTADO_SUBGRUPOS);
        esperarEstadoTask.setParameters(nameValuePairs);
        esperarEstadoTask.executeTask("completeEsperarEstadoSubgrupos", "errorEsperarEstadoSubgrupos");
    }

/*Se invoca desde PoolServiceEstados, que se ejecuta cada 5 segundos creo.*/
public void completeEsperarEstadoSubgrupos(SoapObject result) {
    //chequeo si el valor del resultado es positivo para levantar la barrera
    SoapPrimitive res = (SoapPrimitive) result.getProperty("valorInteger");
    int llegaronSubgrupos = Integer.parseInt(res.toString());

    if (llegaronSubgrupos == 1){
        //ahora dependendiendo del estado esperado, es el metodo que debo llamar despues de ejecutar la tarea
        if (getSubgrupo().getEstado() == getSubgrupo().ESTADO_JUGANDO){
            this.comienzoJuego();
        }else{
            this.finJuego();
        }
    }
}

    public void errorEsperarEstadoSubgrupos(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void completeEnviarJugando(SoapObject result) {
        try{
            //el resultado trae el nombre y la consigna del grupo al cual pertenece el subgrupo
            Consigna consigna = new Consigna(((SoapPrimitive) result.getProperty("nombre")).toString(),((SoapPrimitive) result.getProperty("descripcion")).toString());
            this.getSubgrupo().getGrupo().setNombre(((SoapPrimitive) result.getProperty("nombreGrupo")).toString());
            this.getSubgrupo().getGrupo().setConsigna(consigna);

            this.getCurrentActivity().startService(new Intent(getCurrentActivity(), PoolServiceEstados.class));
        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    public void errorEnviarJugando(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void comienzoJuego() {
        try{
            this.getCurrentActivity().showDialogError("Llegaron todos! Comienza el juego!", "JuegoColaborativo");
            this.getCurrentActivity().stopService(new Intent(new Intent(getCurrentActivity(), PoolServiceEstados.class)));
            //traigo todos los pois con sus piezas
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(getSubgrupo().getId())));

            WSTask comienzoJuegoTask = new WSTask();
            comienzoJuegoTask.setReferer(this);
            comienzoJuegoTask.setMethodName(SoapManager.METHOD_GET_PIEZAS_A_RECOLECTAR);
            comienzoJuegoTask.setParameters(nameValuePairs);
            comienzoJuegoTask.executeTask("getPiezasARecolectar", "errorGetPiezasARecolectar");
        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    public void errorComienzoJuego(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void getPiezasARecolectar(SoapObject result) {
        try{
            for (int i = 0; i < result.getPropertyCount(); i++) {
                SoapObject poi = (SoapObject)((SoapObject) result.getProperty(i)).getProperty("poi");
                int idPieza = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("id").toString());
                double latitud = Double.parseDouble(poi.getProperty("latitud").toString());
                double longitud = Double.parseDouble(poi.getProperty("longitud").toString());
                String nombre = ((SoapObject) result.getProperty(i)).getProperty("nombre").toString();
                String descripcion = ((SoapObject) result.getProperty(i)).getProperty("descripcion").toString();

                //creo la pieza a recolectar, la guardo en la lista de piezas del subgrupo
                PiezaARecolectar pieza = new PiezaARecolectar(idPieza,nombre,descripcion,new Poi(new Coordenada(latitud,longitud)),new ArrayList<Consigna>());
                this.getSubgrupo().getPiezasARecolectar().put(idPieza, pieza);

                //creo el marker con su proximity
                this.getCurrentActivity().addPiezasARecolectarToMap(pieza);
            }

        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }

    }

    public void errorGetPiezasARecolectar(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea :" + failedMethod, "Error");
    }

    public void mostrarInfoPieza(int id_pieza){
        this.setPiezaActual(this.getSubgrupo().getPiezaWithId(id_pieza));
        //remuevo el proximity alert y cambio el color del marker
        this.getCurrentActivity().removeProximityAlert(MapActivity.PROX_ALERT_PIEZA_A_RECOLECTAR+this.getPiezaActual().getNombre());

        // Inicio la activity que muestra la información de la pieza
        this.getCurrentActivity().startActivity(new Intent(this, PiezaActivity.class));
    }

    public void enviarFinJuego(){
        //detengo el proximity alert del poi final y limpio el mapa (borro marker poi final)
        this.getCurrentActivity().removerPuntoFinal();
        this.getCurrentActivity().showDialogError("Has llegado al punto de encuentro final! Ahora espera la llegada de todos los subgrupos", "JuegoColaborativo");

        //aviso que ya terminé de visitar los pois
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(getSubgrupo().getId())));
        getSubgrupo().setEstado(getSubgrupo().ESTADO_FINAL);
        nameValuePairs.add(new BasicNameValuePair("idEstado", Integer.toString(getSubgrupo().getEstado())));

        WSTask setJugandoTask = new WSTask();
        setJugandoTask.setReferer(this);
        setJugandoTask.setMethodName(SoapManager.METHOD_CAMBIAR_ESTADO_SUBGRUPO);
        setJugandoTask.setParameters(nameValuePairs);
        setJugandoTask.executeTask("completeEnviarFinJuego", "errorEnviarFinJuego");
    }

    public void errorEnviarFinJuego(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void completeEnviarFinJuego(SoapObject result) {
        try{
            //llamo al pool service para esperar a que todos terminen de recorrer sus pois
            this.getCurrentActivity().startService(new Intent(getCurrentActivity(), PoolServiceEstados.class));
        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    public void finJuego(){
        this.getCurrentActivity().stopService(new Intent(new Intent(getCurrentActivity(), PoolServiceEstados.class)));
        // this.getCurrentActivity().showDialogError("Llegaron todos, el ganador es: ?", "Error");
        this.getCurrentActivity().stopService(new Intent(new Intent(getCurrentActivity(), PoolServiceColaborativo.class)));

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(getSubgrupo().getId())));

        WSTask esperarEstadoTask = new WSTask();
        esperarEstadoTask.setReferer(this);
        esperarEstadoTask.setMethodName(SoapManager.METHOD_GET_RESULTADOS);
        esperarEstadoTask.setParameters(nameValuePairs);
        esperarEstadoTask.executeTask("completeGetResultados", "errorGetResultados");
    }

/*
Método que es llamado por el PoolServiceEstados para chequear si hay preguntas por responder
*/
public void esperarPreguntasSubgrupos(){
    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
    nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(getSubgrupo().getId())));

    WSTask esperarEstadoTask = new WSTask();
    esperarEstadoTask.setReferer(this);
    esperarEstadoTask.setMethodName(SoapManager.METHOD_EXISTE_PREGUNTA_SIN_RESPONDER);
    esperarEstadoTask.setParameters(nameValuePairs);
    esperarEstadoTask.executeTask("completeEsperarPreguntasSubgrupos", "errorEsperarPreguntasSubgrupos");
}

    public void errorEsperarPreguntasSubgrupos(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void completeEsperarPreguntasSubgrupos(SoapObject result) {
        try{
            //chequeo si el valor del resultado es positivo para levantar la pregunta y poder responderla
            SoapPrimitive res = (SoapPrimitive) result.getProperty("idSubgrupoConsultado");
            int id = Integer.parseInt(res.toString());

            if (id != -1){
                //chequeo si ya respondí esa consulta
                if ((this.getSubgrupo().getIdsConsultasQueMeHicieron().isEmpty()) || (!(this.getSubgrupo().getIdsConsultasQueMeHicieron()).contains(id))){
                    //agrego el id a las consultas que me hicieron
                    this.getSubgrupo().getIdsConsultasQueMeHicieron().add(id);
                    //creo el objeto Consulta y lo seteo a la consultaActual
                    SoapPrimitive res2 = (SoapPrimitive) result.getProperty("nombrePieza");
                    String nombrePieza = res2.toString();
                    SoapPrimitive res3 = (SoapPrimitive) result.getProperty("descripcionPieza");
                    String descripcionPieza = res3.toString();
                    SoapPrimitive res4 = (SoapPrimitive) result.getProperty("cumple");
                    int cumple = Integer.parseInt(res4.toString());
                    SoapPrimitive res5 = (SoapPrimitive) result.getProperty("justificacion");
                    String justificacion = res5.toString();
                    this.getSubgrupo().setConsultaActual(new Consulta(id,nombrePieza,descripcionPieza,cumple,justificacion));
                    // Inicio la activity para responder la consulta
                    this.getCurrentActivity().startActivity(new Intent(this, ResponderActivity.class));
                }
            }

        } catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    /*
    Método que es llamado por el PoolServiceEstados para chequear si hay respuestas a una consulta realizada
    */
    public void esperarRespuestasSubgrupos(){
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(getSubgrupo().getId())));

        WSTask esperarEstadoTask = new WSTask();
        esperarEstadoTask.setReferer(this);
        esperarEstadoTask.setMethodName(SoapManager.METHOD_EXISTEN_RESPUESTAS);
        esperarEstadoTask.setParameters(nameValuePairs);
        esperarEstadoTask.executeTask("completeEsperarRespuestasSubgrupos", "errorEsperarRespuestasSubgrupos");
    }

    public void errorEsperarRespuestasSubgrupos(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void completeEsperarRespuestasSubgrupos(SoapObject result) {
        try{
            if (result.getPropertyCount() > this.getSubgrupo().getCantidadRespuestas()){
                HashMap<Integer, Respuesta> respuestas = this.getSubgrupo().getRespuestas().get(this.getPiezaActual().getId());
                    for (int i = 0; i < result.getPropertyCount(); i++) {
                        int idSubgrupo = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("idSubgrupo").toString());
                        int cumple = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("cumple").toString());
                        String justificacion = ((SoapObject) result.getProperty(i)).getProperty("justificacion").toString();

                        //busco en la lista de respuestas de la pieza actual del subgrupo y guardo la respuesta
                        respuestas.get(idSubgrupo).setCumple(cumple);
                        respuestas.get(idSubgrupo).setJustificacion(justificacion);

                        this.getSubgrupo().setCantidadRespuestas(this.getSubgrupo().getCantidadRespuestas() + 1);
                    }

                    getCurrentActivity().getResultadosAdapter().notifyDataSetChanged();

                }

        } catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    public void errorGetResultados(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void completeGetResultados(SoapObject result) {
        // Inicializo el arreglo de resultados
        this.setResultadoFinal(new TreeSet<ResultadoFinal>());
        this.getSubgrupo().setResultados(new ArrayList<Resultado>());
        HashMap<String, Integer> resultadosParciales = new HashMap<String, Integer>();
        try{
            for (int i = 0; i < result.getPropertyCount(); i++) {
                String nombreGrupo = ((SoapObject) result.getProperty(i)).getProperty("nombreGrupo").toString();
                int idSubgrupo = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("idSubgrupo").toString());
                int respuestaCumple = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("respuestaCumple").toString());
                int cumple = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("cumple").toString());

                if(!resultadosParciales.containsKey(nombreGrupo)){
                    // Si no existe lo inicializo
                    resultadosParciales.put(nombreGrupo, 0);
                }

                if(respuestaCumple == cumple){
                    // Sumo 1 a las respuestas correctas
                    resultadosParciales.put(nombreGrupo, resultadosParciales.get(nombreGrupo) + 1);
                }

                // Si son los resultados de mi subgrupo los guardo para mostrar el detalle
                if(idSubgrupo == this.getSubgrupo().getId()){
                    String pieza = ((SoapObject) result.getProperty(i)).getProperty("pieza").toString();
                    this.getSubgrupo().getResultados().add(new Resultado(pieza, respuestaCumple == 1, cumple == respuestaCumple));
                }
            }

            // Recorro el hashmap auxiliar para genera la lista ordenada de resultados
            for (HashMap.Entry<String, Integer> entry : resultadosParciales.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                this.getResultadoFinal().add(new ResultadoFinal(key, value));
            }

            // Inicio la activity que muestra los resultados finales
            this.getCurrentActivity().startActivity(new Intent(this, ResultadosActivity.class));
            // getCurrentActivity().getResultadosAdapter().notifyDataSetChanged();
        } catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static JuegoColaborativo getInstance() {
        return instance;
    }
}
