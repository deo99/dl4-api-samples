package tci.dl4.restapi.client;

import org.json.simple.JSONObject;

public class ApiKeySample {
	
	public static void main(String[] args) throws Exception {
		try {
			// REST API's requests using API Key authentication must go to /tci/headles-api instead of /tci/api
			String url = "https://api-demo.decisionlender.solutions";

			String clientId = args[0];
			String apiClientKey = args[1];
			String dealerId = args[2];
			String userId = args[3];
			String apiKeyName = args[4];
			String apiKeyValue = args[5];
			String appId = args[6];
			
			String path = "/applications/" + appId;

			ApiKeyClient client = new ApiKeyClient(url, clientId, apiClientKey, dealerId, userId, apiKeyName, apiKeyValue);

			JSONObject jsonObject = client.get(path);
			System.out.println(jsonObject.toJSONString());

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
