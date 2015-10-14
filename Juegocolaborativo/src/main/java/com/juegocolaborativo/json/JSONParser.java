package com.juegocolaborativo.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser {
	
	public static final String FIELD_JSON_STATUS = "status";
	public static final String FIELD_JSON_USERNAME = "username";

	private final static Object get(JSONObject jSONData, String field){
		try {
			return jSONData.get(field);
		} catch (JSONException e) {
			return null;
		}
	}
	
	public final static JSONObject getJSONObject(JSONArray jSONData, int index){
		try {
			return jSONData.getJSONObject(index);
		} catch (JSONException e) {
			return null;
		}
	}
	
	public final static JSONObject getJSONObject(JSONObject jSONData, String field){
		try {
			return jSONData.getJSONObject(field);
		} catch (JSONException e) {
			return null;
		}
		
	}
	
	public final static JSONArray getJSONArray(JSONObject jSONData, String field){
		return (JSONArray) get(jSONData, field);
	}
	
	public final static String getString(JSONObject jSONData, String field){
		String result = get(jSONData, field).toString();
		if (result != null){
			return result;
		} else {
			return "";
		}
	}
	
	public final static Long getLong(JSONObject jSONData, String field){
		try {
			return jSONData.getLong(field);
		} catch (JSONException e) {
			return (long)0;
		}
	}
	
	public final static Integer getInt(JSONObject jSONData, String field){
		try {
			return jSONData.getInt(field);
		} catch (JSONException e) {
			return (int)0;
		}
	}
	
	public final static Float getFloat(JSONObject jSONData, String field){
		try {
			return Float.parseFloat(jSONData.getString(field));
		} catch (JSONException e) {
			return (float)0;
		}
	}
	
	public final static Boolean getBoolean(JSONObject jSONData, String field){
		try {
			return jSONData.getBoolean(field);
		} catch (JSONException e) {
			return false;
		}
	}
	
	public final static JSONObject put(JSONObject jsonObject, String fieldName, Object element){
		try {
			jsonObject.put(fieldName, element);
		} catch (JSONException e) {
			return jsonObject;
		}
		return jsonObject;
	}
}
