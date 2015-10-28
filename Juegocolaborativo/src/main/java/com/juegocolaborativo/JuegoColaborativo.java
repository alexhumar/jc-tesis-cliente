package com.juegocolaborativo;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.juegocolaborativo.activity.DefaultActivity;
import com.juegocolaborativo.activity.MapActivity;
import com.juegocolaborativo.activity.PiezaActivity;
import com.juegocolaborativo.activity.ResponderActivity;
import com.juegocolaborativo.activity.ResultadosActivity;
import com.juegocolaborativo.service.PoolServiceEstados;
import com.juegocolaborativo.service.PoolServiceColaborativo;
import com.juegocolaborativo.service.PoolServicePosta;
import com.juegocolaborativo.soap.SoapManager;
import com.juegocolaborativo.task.WSTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import model.Consigna;
import model.Coordenada;
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

    public void esperarTurnoJuego() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(getSubgrupo().getId())));

        //Ejecuto la tarea que cambia de estado al subgrupo
        WSTask setJugandoTask = new WSTask();
        setJugandoTask.setReferer(this);
        setJugandoTask.setMethodName(SoapManager.METHOD_ES_SUBGRUPO_ACTUAL);
        setJugandoTask.setParameters(nameValuePairs);
        setJugandoTask.executeTask("completeEsSubgrupoActual", "errorEsSubgrupoActual");
    }

    public void completeEsSubgrupoActual(SoapObject result) {
        try{
            SoapPrimitive res = (SoapPrimitive) result.getProperty("valorInteger");
            int esSubgrupoActual = Integer.parseInt(res.toString());
            if (esSubgrupoActual == 1) {
                //Deja de preguntar si es su turno
                this.getCurrentActivity().stopService(new Intent(getCurrentActivity(), PoolServicePosta.class));
                this.jugar();
            }
        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    public void errorEsSubgrupoActual(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void jugar() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(getSubgrupo().getId())));
        getSubgrupo().setEstado(getSubgrupo().ESTADO_JUGANDO);
        nameValuePairs.add(new BasicNameValuePair("idEstado", Integer.toString(getSubgrupo().getEstado())));

        //Ejecuto la tarea que cambia de estado al subgrupo
        WSTask setJugandoTask = new WSTask();
        setJugandoTask.setReferer(this);
        setJugandoTask.setMethodName(SoapManager.METHOD_CAMBIAR_ESTADO_SUBGRUPO);
        setJugandoTask.setParameters(nameValuePairs);
        setJugandoTask.executeTask("completeEnviarJugando", "errorEnviarJugando");
    }

    public void completeEnviarJugando(SoapObject result) {
        try{
            //el resultado trae el nombre y la consigna del grupo al cual pertenece el subgrupo
            Consigna consigna = new Consigna(((SoapPrimitive) result.getProperty("nombre")).toString(),((SoapPrimitive) result.getProperty("descripcion")).toString());
            this.getSubgrupo().getGrupo().setNombre(((SoapPrimitive) result.getProperty("nombreGrupo")).toString());
            this.getSubgrupo().getGrupo().setConsigna(consigna);

            this.comienzoJuego();
        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    public void errorEnviarJugando(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void enviarJugando(){
        //detengo el proximity alert del poi subgrupo y limpio el mapa (borro marker poi inicial)
        //Quizas convendria chequear si ya esta jugando (por si le llega una consulta mientras esta en su poi. Ver OnResume de MapActivity)
        this.getCurrentActivity().removerPuntoInicial();
        this.getCurrentActivity().showDialogError("Felicitaciones, has llegado al poi asignado! Hora de jugar!", "JuegoColaborativo");

        //creo la lista de consultas vacias del subgrupo
        this.getSubgrupo().setIdsConsultasQueMeHicieron(new ArrayList<Integer>());
        //llamo al PoolServiceEstados para chequear si un subgrupo realizó una pregunta que debo responder
        /* Alex - desde que arranca el juego, se inicia este servicio, que lo que hace es, cada 5 segundos, chequear si existen preguntas hechas por otro
        * subgrupo mediante la invocacion al webservice "existenPreguntasSinResponder" */
        this.getCurrentActivity().startService(new Intent(getCurrentActivity(), PoolServiceColaborativo.class));

        this.getCurrentActivity().stopService(new Intent(new Intent(getCurrentActivity(), PoolServiceEstados.class)));

        this.getCurrentActivity().startService(new Intent(getCurrentActivity(), PoolServicePosta.class));
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
            if (getSubgrupo().getEstado() == getSubgrupo().ESTADO_INICIAL){
                this.enviarJugando();
            }
            else if (getSubgrupo().getEstado() == getSubgrupo().ESTADO_FINAL){
                this.finJuego();
            }
        }
    }

    public void errorEsperarEstadoSubgrupos(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void comienzoJuego() {
        try{
            this.getCurrentActivity().showDialogError("Es tu turno! Comienza el juego!", "JuegoColaborativo");

            //traigo el poi con sus piezas
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(getSubgrupo().getId())));

            WSTask comienzoJuegoTask = new WSTask();
            comienzoJuegoTask.setReferer(this);
            comienzoJuegoTask.setMethodName(SoapManager.METHOD_GET_PIEZA_A_RECOLECTAR);
            comienzoJuegoTask.setParameters(nameValuePairs);
            comienzoJuegoTask.executeTask("getPiezaARecolectar", "errorGetPiezaARecolectar");
        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    //ALEX - Si no rompe, borrar
    /*public void errorComienzoJuego(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }*/

    public void getPiezaARecolectar(SoapObject result) {
        try{
            //SoapObject poi = (SoapObject) result.getProperty("poi");
            SoapObject poi = (SoapObject)result.getProperty("poi");
            int idPieza = Integer.parseInt(result.getProperty("id").toString());
            String nombre = result.getProperty("nombre").toString();
            String descripcion = result.getProperty("descripcion").toString();
            double latitud = Double.parseDouble(poi.getProperty("coordenadaY").toString());
            double longitud = Double.parseDouble(poi.getProperty("coordenadaX").toString());

            //creo la pieza a recolectar, la guardo en la lista de piezas del subgrupo
            PiezaARecolectar pieza = new PiezaARecolectar(idPieza,nombre,descripcion,new Poi(new Coordenada(latitud,longitud)),new ArrayList<Consigna>());
            this.getSubgrupo().getPosta().getPoi().setPieza(pieza);

            //creo el marker con su proximity
            //this.getCurrentActivity().addPiezasARecolectarToMap(pieza);
            this.mostrarInfoPieza();

        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }

    }

    public void errorGetPiezaARecolectar(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea :" + failedMethod, "Error");
    }

    public void mostrarInfoPieza(){
        this.setPiezaActual(this.getSubgrupo().getPosta().getPoi().getPieza());
        //remuevo el proximity alert y cambio el color del marker
        //this.getCurrentActivity().removeProximityAlert(MapActivity.PROX_ALERT_PIEZA_A_RECOLECTAR + this.getPiezaActual().getNombre());

        // Inicio la activity que muestra la información de la pieza
        this.getCurrentActivity().startActivity(new Intent(this, PiezaActivity.class));
    }

    public void enviarFinJuego(){
        //detengo el proximity alert del poi final y limpio el mapa (borro marker poi final)
        this.getCurrentActivity().removerPuntoFinal();
        this.getCurrentActivity().showDialogError("Has llegado a la Posta siguiente! Ahora espera los resultados!", "JuegoColaborativo");

        //aviso que ya terminé de jugar
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

    public void completeEnviarFinJuego(SoapObject result) {
        try{
            //llamo al pool service para esperar a que todos terminen de jugar
            this.getCurrentActivity().startService(new Intent(getCurrentActivity(), PoolServiceEstados.class));

            //Activo al Subgrupo siguiente
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("idSubgrupo", Integer.toString(getSubgrupo().getId())));

            WSTask setJugandoTask = new WSTask();
            setJugandoTask.setReferer(this);
            setJugandoTask.setMethodName(SoapManager.METHOD_SET_POSTA_ACTUAL);
            setJugandoTask.setParameters(nameValuePairs);
            setJugandoTask.executeTask("completeSetPostaActual", "errorSetPostaActual");
        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }

    public void errorEnviarFinJuego(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
    }

    public void completeSetPostaActual(SoapObject result){
        //NO HACE NADA. EL CICLO SE CIERRA EN esperarEstadoSubgrupos() MEDIANTE PoolServiceEstados.
    }

    public void errorSetPostaActual(String failedMethod){
        this.getCurrentActivity().showDialogError("Error en la tarea:" + failedMethod, "Error");
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
        HashMap<String, Integer> resultadosParciales = new HashMap<String, Integer>();
        try{
            for (int i = 0; i < result.getPropertyCount(); i++) {
                String nombreGrupo = ((SoapObject) result.getProperty(i)).getProperty("nombreGrupo").toString();
                int idSubgrupo = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("idSubgrupo").toString());
                //Respuesta del subgrupo
                int decisionFinalCumple = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("decisionFinalCumple").toString());
                //Si es correcta o no la decision tomada (se calcula en el servidor)
                int decisionCorrecta = Integer.parseInt(((SoapObject) result.getProperty(i)).getProperty("decisionCorrecta").toString());

                if(!resultadosParciales.containsKey(nombreGrupo)){
                    // Si no existe lo inicializo
                    resultadosParciales.put(nombreGrupo, 0);
                }

                if(decisionCorrecta == 1){
                    // Sumo 1 a las respuestas correctas
                    resultadosParciales.put(nombreGrupo, resultadosParciales.get(nombreGrupo) + 1);
                }

                // Si son los resultados de mi subgrupo los guardo para mostrar el detalle
                if(idSubgrupo == this.getSubgrupo().getId()){
                    String pieza = ((SoapObject) result.getProperty(i)).getProperty("pieza").toString();
                    this.getSubgrupo().setResultado(new Resultado(pieza, decisionFinalCumple == 1, decisionCorrecta == 1));
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
