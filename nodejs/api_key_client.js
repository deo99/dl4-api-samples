var crypto = require('crypto');
var najax = $ = require('najax');

// generate the required auth headers
function genAuthHeaders(clientId, apiClientKey, dealerId, userId, apiKeyName, apiKey, method, path, headers) {
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
		signingKey = crypto.createHmac('sha256', signingKey).update( creds[i] ).digest('bytes');
		if ( i > 0 )
			authHeader += "/";
		authHeader += creds[i];
	}
	////console.log(signingKey.toString('base64'));
	
	authHeader += ", SignedEntities=METHOD;PATH;x-tci-timestamp, Signature=";
	var toSign = method + ";" + path + ";" + ts;
	var signature = crypto.createHmac('sha256', signingKey).update( toSign );
	authHeader += signature.digest( 'hex' );
	
	headers["x-api-key"] = apiClientKey;
	headers["Authorization"] = authHeader;
	headers["x-tci-timestamp"] = ts;
}

var args = process.argv.slice(2);
var clientId = args[0];
var apiClientKey = args[1];
var dealerId = args[2];
var userId = args[3];
var apiKeyName = args[4];
var apiKey = args[5];
var appId = args[6];

method = "GET";
path = "/applications/" + appId;

console.log(clientId, apiClientKey, dealerId, userId, apiKeyName, apiKey, method, path);

// generate auth headers
var headers = {};
genAuthHeaders(clientId, apiClientKey, dealerId, userId, apiKeyName, apiKey, method, path, headers);
console.log(headers);

var server = "https://api-demo.decisionlender.solutions";
var url = server + path;

// make ajax request
najax( {
  	url: url,
    method: method,
    headers: headers,
    success: function(data, text, xhr) {
		console.log(xhr.status);
		data = JSON.parse(data);
		console.log(JSON.stringify(data, null, 2));
    },
    error: function(xhr, text) {
		console.log("ERROR", xhr.status);
		console.log( text );
    }
  });
 