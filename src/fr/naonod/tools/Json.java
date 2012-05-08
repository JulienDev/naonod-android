package fr.naonod.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class Json {

//	public static JSONArray getJSon(URL url) {
//		try {
//			// Read all the text returned by the server		
//            URLConnection urlc = url.openConnection();
//            BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
//			
//			String str;
//			StringBuffer jString = new StringBuffer();
//			while ((str = in.readLine()) != null) {
//				// str is one line of text; readLine() strips the newline character(s)
//				jString.append(str);
//			}
//			try {
////				return new JSONObject(jString.toString());
//				return new JSONArray(jString.toString());
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			in.close();
//		} catch (MalformedURLException e) {
//			Log.i("MalformedURLException", e.getMessage());
//		} catch (IOException e) {
//			Log.i("IOException", e.getMessage());
//		}
//		return null;
//	}
	
	public static JSONArray getJSon(String url, List<NameValuePair> nameValuePairs) {
		try {
			
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response=httpclient.execute(httppost);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			
			String str;
			StringBuffer jString = new StringBuffer();
			while ((str = reader.readLine()) != null) {
				// str is one line of text; readLine() strips the newline character(s)
				jString.append(str);
			}
			try {
				return new JSONArray(jString.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			reader.close();
		} catch (MalformedURLException e) {
			Log.i("MalformedURLException", e.getMessage());
		} catch (IOException e) {
			Log.i("IOException", e.getMessage());
		}
		return null;
	}
}