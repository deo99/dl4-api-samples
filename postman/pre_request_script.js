function genAuthHeaders(clientId, dealerId, userId, apiKeyName, apiKey, method, path, headers) {
	var date = new Date();
	var ts = date.getTime().toString();
	
	// need yyyyMMdd format
	var y = date.getFullYear();
	var m = date.getMonth() + 1;
	var d = date.getDate();
	var dateStr = y.toString();
	if ( m < 10 ) {
		dateStr += "0";
	}
	dateStr += m.toString();
	if ( d < 10 ) {
		dateStr += "0";
	}
	dateStr += d.toString();

	var authHeader = "TCIv1-HmacSHA256 Credential=";
	var creds = [ dateStr, "external", clientId, userId, apiKeyName ];
	if (dealerId != undefined && dealerId != '') {
		creds = [ dateStr, "external", clientId, dealerId, userId, apiKeyName ];
	}
	
	var signingKey = apiKey;
	for ( var i = 0; i < creds.length; i++ ) {
		signingKey = new CryptoJS.HmacSHA256( creds[i], signingKey );
		if ( i > 0 )
			authHeader += "/";
		authHeader += creds[i];
	}
	
	authHeader += ", SignedEntities=METHOD;PATH;x-tci-timestamp, Signature=";
	var toSign = method + ";" + path + ";" + ts;
	var signature = new CryptoJS.HmacSHA256( toSign, signingKey );
	authHeader += CryptoJS.enc.Hex.stringify( signature );

	headers["Authorization"] = authHeader;
	headers["x-tci-timestamp"] = ts;
}

headers = {};
// dynamically get path and method from pm
path = '';
for ( i = 2; i != pm.request.url.path.length; i++ ) {
    path = path + '/' + pm.request.url.path[i];
}
method = pm.request.method;
console.log(method + " " + path);
genAuthHeaders("<clientId>", "<dealerId>", "<userId>", "<keyName>", "<keyValue>", method, path, headers);

postman.setEnvironmentVariable("authHeader", headers['Authorization']);
postman.setEnvironmentVariable("tsHeader", headers['x-tci-timestamp']);
