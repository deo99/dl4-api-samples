package tci.dl4.restapi.client;

import org.json.simple.JSONObject;

public class ApiKeySample {
	
	public static void main(String[] args) throws Exception {
		try {
			// REST API's requests using API Key authentication must go to /tci/headles-api instead of /tci/api
			String url = "https://demo.decisionlender.solutions/tci/headless-api";

			String clientId = args[0];
			String userId = args[1];
			String apiKeyName = args[2];
			String apiKeyValue = args[3];
			String appId = args[4];
			
			String path = "/applications/" + appId;

			ApiKeyClient client = new ApiKeyClient(url, clientId, userId, apiKeyName, apiKeyValue);

			JSONObject jsonObject = client.get(path);
			System.out.println(jsonObject.toJSONString());

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
