function httpGetAsync(theUrl, callback)
{
	var xmlhttp = new XMLHttpRequest();
	var url = theUrl;

	xmlhttp.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			//var myArr = JSON.parse(this.responseText);
			var myArr = this.responseText;
			callback(myArr);
		}
	};
	xmlhttp.open("GET", url, true);
	xmlhttp.send();
}

function myFunction(arr) {
    var out = "";
    var i;
    for(i = 0; i < arr.length; i++) {
        out += '<a href="' + arr[i].url + '">' + 
        arr[i].display + '</a><br>';
    }
    document.getElementById("list").innerHTML = out;
}

function display(arr) {
    document.getElementById("list").innerHTML = arr;
}

function crossDomain(){
	if (window.XDomainRequest) xmlhttp = new XDomainRequest();
	else if (window.XMLHttpRequest) xmlhttp = new XMLHttpRequest();
	else xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");

	xmlhttp.open("GET", "http://api.hostip.info/get_html.php", false);
	xmlhttp.send();

	hostipInfo = xmlhttp.responseText.split("\n");
	var IP = false;
	for (i = 0; hostipInfo.length >= i; i++) {
		if (hostipInfo[i]) {

			ipAddress = hostipInfo[i].split(":");
			if (ipAddress[0] == "IP") {
				IP = ipAddress[1];
			}	
		}
	}
	display(IP);
}

function httpGet(theUrl)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", theUrl, false ); // false for synchronous request
    xmlHttp.send( null );
    return xmlHttp.responseText;
}

function executeServiceSuite(serviceSuiteName){
	//alert('123')
	url = "http://speed.cs.vt.edu/dmg/executeServiceSuite.php"
	response = httpGetAsync(url,display)
}


function display(response){
	alert('executionResult:'+response)
	document.getElementById('list').textContent = response
}