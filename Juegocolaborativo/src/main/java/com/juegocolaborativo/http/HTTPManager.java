package com.juegocolaborativo.http;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;



public class HTTPManager {
	private String url; 
	private DefaultHttpClient httpClient;
	private Object referer;
	private JSONObject result;
	
	public static final String METHOD_LOGIN = "login.php";
	public static final String METHOD_LOGOUT = "logout.php";
	
	public static final int CONNECTION_TIMEOUT = 50000;
	public static final int SOCKET_TIMEOUT = 8000;
	
	public static final int STATUS_OK 		= 1;
	public static final int STATUS_TIMEOUT 	= -1;
	public static final int STATUS_ERROR 	= -2;
	
	public HTTPManager(Object referer){
		super();
				
		//String hostName = Config.getInstance().getStringProperty("HOST_NAME");
        String hostName = "http://192.168.0.7/prueba";
		hostName += (hostName.lastIndexOf('/') == hostName.length() - 1) ? "" : '/';
		
		this.url = hostName;
		this.setReferer(referer);
		
		HttpParams httpParameters = new BasicHttpParams();
		
		HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
		
		HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);

		this.httpClient = new DefaultHttpClient(httpParameters);
	}
	
	public HttpClient getHttpClient() {
		return httpClient;
	}
	public void setHttpClient(DefaultHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public JSONObject getResult() {
		return result;
	}

	public void setResult(JSONObject result) {
		this.result = result;
	}
	
	/**
	 * 
	 * @param method URL DEL METODO A EJECUTAR
	 * @param list 
	 * @return CODIGO DE ESTADO [STATUS_OK|STATUS_TIMEOUT] 
	 */
	public int loadData(String method, List<NameValuePair> list){
		try {
			HttpPost httppost = new HttpPost(this.getUrl() + method);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			
			httppost.setEntity(new UrlEncodedFormEntity(list));
		    
		    System.out.println("Ejecutando request : "+ this.getUrl() + method + list.toString());
		    
		    String result = this.getHttpClient().execute(httppost,responseHandler);
		    
		    System.out.println("RESULTADO:  " + result);
			
		    this.setResult(new JSONObject(result));
			
		    return STATUS_OK;
		} catch (ConnectTimeoutException cte){
			//AndroidLogger.logger.error("Error: HTTPManager>>getData - TIMEOUT - URL: " + this.getUrl() + method);
//			System.out.println("Error: HTTPManager>>getData - TIMEOUT - URL: " + this.getUrl() + method);
			return STATUS_TIMEOUT;
		} catch (Exception e) {
			System.out.println("Error: HTTPManager>>getData - URL: " + this.getUrl() + method);
			e.printStackTrace();
			//AndroidLogger.logger.error("Error: HTTPManager>>getData - URL: " + this.getUrl() + method);
			return STATUS_ERROR;
		}
	}

	/**
	 * @return the referer
	 */
	public Object getReferer() {
		return referer;
	}

	/**
	 * @param referer the referer to set
	 */
	public void setReferer(Object referer) {
		this.referer = referer;
	}


}
