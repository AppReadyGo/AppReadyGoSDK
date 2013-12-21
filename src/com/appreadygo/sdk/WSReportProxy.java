package com.appreadygo.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

class WSReportProxy {


	private static final String TAG = WSReportProxy.class.getSimpleName();

	/**
	 * Send single package to the server <Logic> 1. Create Http client 2. Read
	 * test data from the file 3. Serialize the data to an object 4 Execute
	 * </Logic>
	 * 
	 * @return
	 */
	public static boolean sendDatatoServer(WSSessionData[] sessData) {

		String resp = "Default result";
		String content = "false";
		int responseCode = 0;

		try {
			// 1.
			HttpClient httpclient = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(httpclient.getParams(),
					2000); // Timeout limit
			HttpPost request = new HttpPost(CCServices.getServiceURL());
			InternalLog.d(TAG, "Send data to URI " + request.getURI());
			// 2.
			JSONObject jsonToSend = new JSONObject();
			buildJsonObject(sessData, jsonToSend);

			String sessionJsonString = jsonToSend.toString();
//was val
		/*	JSONStringer data = new JSONStringer().object().key("data")
					.value(sessionJsonString).endObject();*/
			// 3.
			ByteArrayEntity bEntity = new ByteArrayEntity(sessionJsonString
					.getBytes());
			// text/plain;charset=UTF-8
			bEntity.setContentType("application/json;charset=UTF-8");
			bEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json;charset=UTF-8"));
			request.setEntity(bEntity);
			// 4.
			HttpResponse httpResponse = httpclient.execute(request);
			// 5.
			responseCode = httpResponse.getStatusLine().getStatusCode();
			String message = httpResponse.getStatusLine().getReasonPhrase();
			HttpEntity entity = httpResponse.getEntity();
			// 6.
			if (entity != null) {
				InputStream instream = entity.getContent();
				content = CCServices.convertStreamToString(instream);
				// Closing the input stream will trigger connection release
				instream.close();
				resp = "Response Code : " + responseCode + " Message "
						+ message + " response " + content;
			}
		} catch (UnsupportedEncodingException e) {
			// Problem with with Package/Object
			e.printStackTrace();
			resp = "Exception - setEntity() is not working";
		} catch (ClientProtocolException e) {
			// Problem with HttpClient (service is not working)
			e.printStackTrace();
			resp = "Exception - httpClient is not working";
		} catch (IOException e) {
			e.printStackTrace();
			resp = "Exception - IOException ";
		} catch (JSONException e) {
			e.printStackTrace();
			resp = "Exception - JSONException ";
		}

		InternalLog.d(TAG, resp);
		//If the response is 200 - server got the message and now it his responsibility 
		return responseCode == HttpStatus.SC_OK;

	}

	private static void buildJsonObject(WSSessionData[] sessData,
			JSONObject jsonToSend) throws JSONException {

		jsonToSend.put("cid", ApplicationConstants.cid);
		
		jsonToSend.put("task", ApplicationConstants.task);
		jsonToSend.put("usr", ApplicationConstants.username);
		jsonToSend.put("sh", ApplicationConstants.sh);
		jsonToSend.put("sw", ApplicationConstants.sw);
		jsonToSend.put("ssi", ApplicationConstants.systemInfo.ConvertToJson());
		ArrayList<WSSessionData> sessionDataList = new ArrayList<WSSessionData>(
				Arrays.asList(sessData));

		CCServices.buildJsonFromArray(jsonToSend, sessionDataList, "ssd");

	}

}
