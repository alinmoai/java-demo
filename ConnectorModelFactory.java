package common;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import com.jfinal.plugin.activerecord.Page;

import common.ApgwAPIList;
import common.ConnectionSetting;
import common.ModelHelper;
import common.ReturnCode;
import exception.ErrorCodedException;
import model.BaseModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Request.Builder;

public class ConnectorModelFactory {
	public static String connectorURL = "/connector";
	
	public static <T extends BaseModel<?>>T findByIdUsingAPIKey(String id, Class<T> classType, String resourcePath, String queryString) throws ErrorCodedException {
		Builder builder = new Request.Builder().addHeader("APIKey", "key");
		return findById(builder, id, classType, resourcePath, queryString);
	}
	
	public static <T extends BaseModel<?>>T findById(String id, Class<T> classType, String resourcePath, String queryString, String token) throws ErrorCodedException {
		Builder builder = new Request.Builder();
		if(token != null) {
			builder.addHeader("Authorization", token);
		}
		return findById(builder, id, classType, resourcePath, queryString);
	}
	
	private static <T extends BaseModel<?>>T findById(Builder builder, String id, Class<T> classType, String resourcePath ,String queryString) throws ErrorCodedException {
		if(queryString == null) {
			queryString = "";
		}
		
		builder.url(ApgwAPIList.getAPGWUrl(connectorURL + resourcePath + id + queryString))
		.get();
		
		OkHttpClient client = ConnectionSetting.createHttpClient(true);
		Response response = ConnectionSetting.doRequest(builder.build(), client);
		try {
			JSONObject result = new JSONObject(response.body().string());
			if(result.getString("ret_code").equalsIgnoreCase("0000")){
				T modleInstance = classType.newInstance();
				String tagName = modleInstance.getTableName();
				if(!result.has(tagName)) {
					tagName = tagName.substring(0, tagName.length() - 1);
				}
				modleInstance.put(result.getJSONObject(tagName));
				return modleInstance;
			}else {
				throw new ErrorCodedException(result.getString("ret_code"));
			}
		} catch (JSONException | IOException e) {
			throw new ErrorCodedException(ReturnCode.SYSTEM_ERROR, "response from baseConnector can't be parse");
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ErrorCodedException(ReturnCode.SYSTEM_ERROR, "classType can't create instance");
		}
	}
	
	public static <T extends BaseModel<?>>Page<T> findAllUsingAPIKey(Class<T> classType, String resourcePath, String queryString) throws ErrorCodedException {
		Builder builder = new Request.Builder().addHeader("APIKey", "key");
		return findAll(builder, classType, resourcePath, queryString);
	}
	
	public static <T extends BaseModel<?>>Page<T> findAll(Class<T> classType, String resourcePath, String queryString, String token) throws ErrorCodedException {
		Builder builder = new Request.Builder();
		if(token != null) {
			builder.addHeader("Authorization", token);
		}
		return findAll(builder, classType, resourcePath, queryString);
	}
	
	private static <T extends BaseModel<?>>Page<T> findAll(Builder builder, Class<T> classType, String resourcePath, String queryString) throws ErrorCodedException {
		if(queryString == null) {
			queryString = "";
		}
		
		builder.url(ApgwAPIList.getAPGWUrl(connectorURL + resourcePath + queryString))
		.get();
		
		OkHttpClient client = ConnectionSetting.createHttpClient(true);
		Response response = ConnectionSetting.doRequest(builder.build(), client);
		
		try {
			JSONObject result = new JSONObject(response.body().string());
			Page<T> page = ModelHelper.transResultToPages(result, classType);
			return page;
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			throw new ErrorCodedException(ReturnCode.SYSTEM_ERROR, "response from baseConnector can't be parse");
		}				
	}
}
