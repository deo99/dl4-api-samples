package tci.dl4.restapi.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ApiKeyClient {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
	private String basePath = null;
	private String apiClientId = null;
	private String apiUserName = null;
	private String apiKeyName = null;
	private SecretKeySpec initialKey;
	
	public ApiKeyClient(String basePath, String apiClientId, String apiUserName, String apiKeyName, String apiKeyValue)
			throws UnsupportedEncodingException {
		this.basePath = basePath;
		this.apiClientId = apiClientId;
		this.apiUserName = apiUserName;
		this.apiKeyName = apiKeyName;
		this.initialKey = new SecretKeySpec(apiKeyValue.getBytes("UTF-8"), "HmacSHA256");
	}

	private byte[] sign(String toSign, SigningKey signingKey) throws Exception {
		Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
		hmacSHA256.init(signingKey.key);
		return hmacSHA256.doFinal(toSign.getBytes("UTF-8"));
	}
	
	// generate a signing key.  Current time must be passed as keys must rotate every 24 hours
	// The signing key is derived from the API Key generated in the UI along with the date,
	// client ID, user name and API key name
	private SigningKey keyGen(Date timestamp) throws Exception {
		String date = DATE_FORMATTER.format(timestamp);
		List<String> credentials = Arrays.asList( date, "external", apiClientId, apiUserName, apiKeyName );
		SigningKey signingKey = new SigningKey();
		signingKey.key = initialKey;

		String seperator = "";
		for ( String cred : credentials ) {
			byte[] signed = sign( cred, signingKey );
			signingKey.key = new SecretKeySpec(signed, "HmacSHA256");
			signingKey.credentials += seperator + URLEncoder.encode( cred, "UTF-8" );
			seperator = "/";
		}

		return signingKey;
	}
	
	// adds the HTTP Authorization header to the request.  the format is as follows:
	// TCIv1-HmacSHA256 Credential=yyyyMMdd/external/clientId/userId/keyName SignedEntities=METHOD,PATH,x-tci-timestamp Signature=<signatureInHex>
	// also adds a custom x-tci-timestamp header that is set to current time in millesconds since Unix epoch time
	protected void addAuthHeaderToRequest(HttpUriRequest req, String path) throws Exception {
		Date timestamp = new Date();
		SigningKey signingKey = keyGen(timestamp);
		
		String timestampStr = Long.toString(new Date().getTime());
		if ( ! path.startsWith("/") )
			path = "/" + path;
		
		SortedMap<String,String> toSignMap = new TreeMap<String,String>();
		toSignMap.put( "METHOD", req.getMethod() );
		toSignMap.put( "PATH", path );
		toSignMap.put( "x-tci-timestamp", timestampStr );
		
		String toSign = "";
		String toSignKeys = "";
		String seperator = "";
		for ( Entry<String,String> entry : toSignMap.entrySet() ) {
			toSignKeys += seperator + entry.getKey();
			toSign += seperator + entry.getValue();
			seperator = ";";
		}
		
		byte[] signatureBytes = sign( toSign, signingKey );
		String signature = Hex.encodeHexString(signatureBytes);
		
		String authHeader = "TCIv1-HmacSHA256";
		authHeader += " Credential=" + signingKey.credentials + ",";
		authHeader += " SignedEntities=" + toSignKeys + ","; 
		authHeader += " Signature=" + signature;

		req.setHeader("x-tci-timestamp", timestampStr);
		req.setHeader("Authorization", authHeader);
		
		System.out.println("Auth headers:");
		System.out.println("\tAuthorization -- " + authHeader);
		System.out.println("\tx-tci-timestamp -- " + timestampStr);
	}
	
	public JSONObject get(String path) throws Exception {
		HttpGet httpget = new HttpGet(basePath + path);
		addAuthHeaderToRequest(httpget, path);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = httpclient.execute(httpget);
		String appContent = IOUtils.toString(response.getEntity().getContent());

		JSONObject jsonObject = null;
		if (appContent != null) {
			jsonObject = toJSONObject(appContent);
		}
		return jsonObject;
	}

	public JSONObject toJSONObject(String jsonStr) throws Exception {
		JSONObject jsonObj = null;
		if (jsonStr != null) {
			jsonObj = (JSONObject) JSONValue.parseWithException(jsonStr);
		}
		return jsonObj;
	}

	private static class SigningKey {
		String credentials = "";
		SecretKeySpec key = null;
	}

}

