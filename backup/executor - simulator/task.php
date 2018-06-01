<?php 

$microservice = $_GET["microservice"];

$configure = array();

$configure["sensor_temperature"] = 1 ;

$configure["location_temperature"] = 1;

$configure["getGPS"] = 1;

$configure["getHumidity"] = 1;

$configure["turnHumiToGPS"] = 1;

$configure["getCellIDLocation"] = 0;

$return = array();

if($configure[$microservice]!=true){
	$return["status"] = "fail";
}else{
	$return["status"] = "success";
	switch($microservice){
		case "sensor_temperature":
			$return["temperature"] = "temp:sensory_temperature;";	
			break;
		case "location_temperature":
			$return["temperature"] = "get: location=".$_GET["location"];
			break;
		case "getGPS":
			$return["GPS"] = "gps: getGPS"; 
			break;
		case "getHumidity":
			$return["Humidity"] = "humidity: getHumidity";
			break;
		case "turnHumiToGPS":
			$return["GPS"] = "gps: turnHumiToGPS";
			break;
		case "getCellIDLocation":
			$return["Location"] = "location:getCellIDLocation";
			break;
	}		
}

echo json_encode($return);
