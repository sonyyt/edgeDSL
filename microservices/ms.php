<?php

$service = $_GET["service"];
$return  = array();
$return["status"] = "fail";

if($service == "getSensoryTemperature"){
        $execResult = shell_exec("python temp.py");
        if(is_float($execResult+0)){
                $return["temperature"] = trim($execResult);
                $return["status"] = "success";
        }
}

if($service == "getLocationTemperature"){
		@$location = $_GET["location"];
		if($location!=null){
			list($lat,$lon)=explode(",",$location);
			$url  = "http://api.openweathermap.org/data/2.5/weather?lon=".$lon."&lat=".$lat."&appid=2d5ce1f7f7e3e85f9c5e168d5494c273";
			$weather_info = file_get_contents($url);
		}
		$json = json_decode($weather_info);
		//print_r($json);
		$temp_k = $json->main->temp;
		$temp_f = $temp_k * 9/5 - 459.67;
        $return["temperature"] = trim($temp_f);
        $return["status"] = "success";
}

if($service == "getIP"){
	$localIP = $_SERVER['SERVER_ADDR'];
    $return["ip"] = "128.173.236.1";
    $return["status"] = "success";
}

if($service == "getLocationByIP"){
		@$ip = $_GET["ip"];
		if($ip!=null){
			$url  = "http://api.ip2location.com/?ip=".$ip."&key=demo&package=WS5";
			$locationInfo = file_get_contents($url);
		}
		//$location_arr = explode(";",$locationInfo);
		//$location = $location_arr[4].",".$location_arr[5];
        //$return["Location"] = $location;
		$return["Location"] = "37.1986666,-80.40870573";
        $return["status"] = "success";
}

if($service == "findOffloadTarget"){
	@$image_size = $_GET["image_size"];
	@$image_url  = stripslashes($_GET["image_url"]);
	@$privacy_concern = $_GET["privacy_concern"];
	$img = './temp.jpg';
	//missing logic: find if the remote server/local service is available;
	//missing logic: privacy concern. 
	if($privacy_concern==1){
		$remote = 0;
	}else{
		if($image_size>>100){
			$remote = 1;
			file_put_contents($img, file_get_contents($image_url));
			$image_url = "http://speed.cs.vt.edu/temp.jpg";
		}else{
			$remote = 0;
		}
	}
	$return["status"] = "success";
	$return["remote"] = $remote;
	$return["image_url"] = $image_url;
}

if($service == "sendMessage"){
	@$message = $_GET["message"];
	@$target = $_GET["phoneNumber"];
}

$tmp = rand(0,9);
if($tmp<0){
        $return = array();
        $return["status"] = "fail";

}

echo json_encode($return);
?>
