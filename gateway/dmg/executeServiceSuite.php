<?php 
$time_pre = microtime(true);
$ms = $_GET["ms"];
//passthru("java -jar executor.jar ms=".$ms, $output); 
exec("java -jar executor.jar ms=".$ms, $output); 
$time_post = microtime(true);
$exec_time = $time_post - $time_pre;
echo $exec_time."\n";

?>