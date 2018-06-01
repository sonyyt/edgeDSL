<?php

$hum = $_GET["humidity"];
if($hum<50){
	$rain_possibility = $hum-20;
}else{
	$rain_possibility = $hum+12;
}

echo $rain_possibility;