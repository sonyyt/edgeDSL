<?php


$ms = $_GET["microservice"];


if($ms==null){
	echo "null";
}else{
	switch($ms){
		case "sensor_temperature":
			$url = "http://192.168.1.188/ms.php?service=getSensoryTemperature";	
			break;
		case "location_temperature":
			$url = "http://speed.cs.vt.edu/ms.php?service=getLocationTemperature";	
			break;
		case "getGPS":
			$url = "http://192.168.1.226:8080/getGPSLocation?1=1";
			break;
		case "getCellIDLocation":
			$url = "http://192.168.1.226:8080/getNetworkLocation?1=1";
			break;
		case "getIP":
			$url = "http://speed.cs.vt.edu/ms.php?service=getIP";	
			break;
		case "IP_Location":
			$url = "http://speed.cs.vt.edu/ms.php?service=getLocationByIP";	
			break;
		case "take_picture":
			$url = "http://192.168.1.188/ms.php?service=takephoto";
			break;
		case "findOffloadTarget":
			$url = "http://speed.cs.vt.edu/ms.php?service=findOffloadTarget";
			break;
		case "cloud_face_detection":
			$url = "http://ec2-13-59-86-32.us-east-2.compute.amazonaws.com/ms.php?service=faceDetection";
			break;
		case "local_face_detection":
			$url = "http://192.168.1.188/ms.php?service=faceDetection";
			break;
	}		
	echo $url;
}