import sys
import requests
import datetime
import hmac
import hashlib


# generate the required auth headers function
def genAuthHeaders(clientId, apiClientKey, dealerId, userId, apiKeyName, apiKey, method, path, headers):
	ts = datetime.datetime.now()
	# need date in yyyyMMdd format
	dateStr = ts.strftime("%Y%m%d")
	# epoch time in millisec
	ts = round(ts.timestamp() * 1000)
	
	creds = [dateStr, "external", clientId, dealerId, userId, apiKeyName]
	if not dealerId:
		creds = [dateStr, "external", clientId, userId, apiKeyName]
		
	authHeader = "TCIv1-HmacSHA256 Credential=" + "/".join(creds)
	signingKey = apiKey.encode()
	for cred in creds:
		signingKey = hmac.new(signingKey, msg=cred.encode(), digestmod=hashlib.sha256).digest()

	authHeader += ", SignedEntities=METHOD;PATH;x-tci-timestamp, Signature="
	toSign = method + ";" + path + ";" + str(ts)
	signature = hmac.new(signingKey, msg=toSign.encode(), digestmod=hashlib.sha256).digest()
	authHeader += signature.hex()

	headers["x-api-key"] = apiClientKey
	headers["Authorization"] = authHeader
	headers["x-tci-timestamp"] = str(ts)


def main(args):
	clientId = args[1]
	apiClientKey = args[2]
	dealerId = args[3]
	userId = args[4]
	apiKeyName = args[5]
	apiKey = args[6]
	appId = args[7]

	method = "GET"
	path = "/applications/" + appId

	print(clientId, apiClientKey, dealerId, userId, apiKeyName, apiKey, method, path)

	# generate auth headers
	headers = dict()
	genAuthHeaders(clientId, apiClientKey, dealerId, userId, apiKeyName, apiKey, method, path, headers)
	print(headers)

	# call rest api
	server = "https://api-demo.decisionlender.solutions"
	url = server + path
	resp = requests.get(url, headers=headers)
	print(resp.status_code)
	print(resp.json())


if __name__ == "__main__":
	main(sys.argv)
