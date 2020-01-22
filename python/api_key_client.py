import sys
import requests
import datetime
import hmac
import hashlib


# generate the required auth headers function
def genAuthHeaders(clientId, userId, apiKeyName, apiKey, method, path, headers):
	ts = datetime.datetime.now()
	# need date in yyyyMMdd format
	dateStr = ts.strftime("%Y%m%d")
	# epoch time in millisec
	ts = round(ts.timestamp() * 1000)

	creds = [dateStr, "external", clientId, userId, apiKeyName]
	authHeader = "TCIv1-HmacSHA256 Credential=" + "/".join(creds)
	signingKey = apiKey.encode()
	for cred in creds:
		signingKey = hmac.new(signingKey, msg=cred.encode(), digestmod=hashlib.sha256).digest()

	authHeader += ", SignedEntities=METHOD;PATH;x-tci-timestamp, Signature="
	toSign = method + ";" + path + ";" + str(ts)
	signature = hmac.new(signingKey, msg=toSign.encode(), digestmod=hashlib.sha256).digest()
	authHeader += signature.hex()

	headers["Authorization"] = authHeader
	headers["x-tci-timestamp"] = str(ts)


def main(args):
	clientId = args[1]
	userId = args[2]
	apiKeyName = args[3]
	apiKey = args[4]
	appId = args[5]

	method = "GET"
	path = "/applications/" + appId

	print(clientId, userId, apiKeyName, apiKey, method, path)

	# generate auth headers
	headers = dict()
	genAuthHeaders(clientId, userId, apiKeyName, apiKey, method, path, headers)
	print(headers)

	# call rest api
	server = "https://demo.decisionlender.solutions/tci/headless-api"
	url = server + path
	resp = requests.get(url, headers=headers)
	print(resp.status_code)
	print(resp.json())


if __name__ == "__main__":
	main(sys.argv)
