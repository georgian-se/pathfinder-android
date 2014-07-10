package gse.pathfinder.api;

import gse.pathfinder.Preferences;
import gse.pathfinder.models.HttpRequest;
import gse.pathfinder.sql.HttpRequestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
	// static final String DEFAULT_HOST = "10.0.2.2:8000";
	// static final String	DEFAULT_HOST	= "172.16.50.128:3000";
	static final String DEFAULT_HOST = "213.157.197.227";

	public static final String getDefaultHost(Context context) {
		return Preferences.getPreference(context).getString("host", DEFAULT_HOST);
	}

	public static final void setDefaultHost(Context context, String host) {
		SharedPreferences settings = Preferences.getPreference(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("host", host);
		editor.commit();
	}

	static final String getApiUrl(Context context) {
		return "http://" + getDefaultHost(context) + "/api";
	}

	public static boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnected();
	}

	private static InputStream postInputStream(String url, List<NameValuePair> params) throws IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new UrlEncodedFormEntity(params));

		HttpResponse httpResponse = httpClient.execute(httpPost);
		HttpEntity httpEntity = httpResponse.getEntity();
		return httpEntity.getContent();
	}

	private static InputStream getInputStream(String url, HttpParams params) throws IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		httpGet.setParams(params);
		HttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity httpEntity = httpResponse.getEntity();
		return httpEntity.getContent();
	}

	private static JSONObject getJSonFromInputStream(InputStream is) throws IOException, JSONException {
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			return new JSONObject(sb.toString());
		} finally {
			is.close();
		}
	}

	static JSONObject post(Context context, String url, List<NameValuePair> params) throws IOException, JSONException, UnsupportedEncodingException {
		InputStream is = postInputStream(url, params);
		return getJSonFromInputStream(is);
	}

	static JSONObject get(Context context, String url, HttpParams params) throws IOException, JSONException, UnsupportedEncodingException {
		InputStream is = getInputStream(url, params);
		return getJSonFromInputStream(is);
	}

	private static void saveToLocalDatabase(Context context, String url, List<NameValuePair> params) {
		HttpRequestUtils.saveRequestToDatabase(context, HttpRequest.newRequest(url, params));
	}

	private static void sendQueue(Context context) {
		if (isConnected(context)) {
			HttpRequest request;
			InputStream is = null;
			while ((request = HttpRequestUtils.getFirstRequestFromDatabase(context)) != null) {
				try {
					is = postInputStream(request.getUrl(), request.getParams());
					HttpRequestUtils.deleteRequestFromDatabase(context, request.getId());
				} catch (IOException ex) {
					ex.printStackTrace();
					return;
				} finally {
					try {
						is.close();
					} catch (Exception ex) {}
				}
			}
		}
	}

	static void sendData(Context context, String url, List<NameValuePair> params) {
		saveToLocalDatabase(context, url, params);
		sendQueue(context);
	}
}
