<?php

include('httpful.phar');

function encode($data) {
    return str_replace(['+', '/'], ['-', '_'], base64_encode($data));
}

function decode($data) {
    return base64_decode(str_replace(['-', '_'], ['+', '/'], $data));
}

function genAuthHeaders($clientId, $apiClientKey, $dealerId, $userId, $apiKeyName, $apiKey, $method, $path) {
	// get the current date in yyyyMMdd format
	$date = date('Ymd');
	// current UTC timestamp in millisec
	$ts = round(microtime(true) * 1000);
	$ts = '1536353464539';

	$authHeader = "TCIv1-HmacSHA256 Credential=";
	$creds = array( $date, "external", $clientId, $dealerId, $userId, $apiKeyName );
	
	if( !isset( $dealerId ) || empty($dealerId)) { 
		$creds = array( $date, "external", $clientId, $userId, $apiKeyName );
	}
	
	$signingKey = $apiKey;
	
	// derive request signing key from the api key + creds
	for( $i = 0; $i < count($creds); $i++ ) {
		$signingKey = hash_hmac("sha256", $creds[$i], $signingKey, true);
		if ( $i > 0 ) {
			$authHeader = $authHeader . "/";
		}
		$authHeader = $authHeader . $creds[$i];
	}
	$authHeader = $authHeader . ", SignedEntities=METHOD;PATH;x-tci-timestamp, Signature=";

	// generate signature
	$toSign = $method . ";" . $path . ";" . $ts;
	$signature = hash_hmac('sha256', $toSign, $signingKey, true);
	$authHeader = $authHeader . bin2hex($signature);

	$headers = array (
		"x-api-key" => $apiClientKey,
		"Authorization" => $authHeader,
		"x-tci-timestamp" => $ts
	);

	return $headers;
} 

$clientId = $argv[1];
$apiClientKey = $argv[2];
$dealerId = $argv[3];
$userId = $argv[4];
$apiKeyName = $argv[5];
$apiKey = $argv[6];
$appId = $argv[7];

$method = "GET";
$path = "/applications/" . $appId;

echo($clientId . " " . $apiClientKey . " " . $dealerId . " " . $userId . " " . $apiKeyName . " " . $apiKey . " " . $method . " " . $path . "\n");

$headers = genAuthHeaders($clientId, $apiClientKey, $dealerId, $userId, $apiKeyName, $apiKey, $method, $path);

var_dump($headers);

$url = "https://api-demo.decisionlender.solutions" . $path;
$response = \Httpful\Request::get($url)
	->addHeaders($headers)
	->send();

echo($response);

?>
