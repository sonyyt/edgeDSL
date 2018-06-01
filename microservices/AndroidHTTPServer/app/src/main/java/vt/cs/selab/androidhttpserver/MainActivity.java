package vt.cs.selab.androidhttpserver;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private MobileWebServer server;
    final String logTag = "server";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server = new MobileWebServer(this.getApplicationContext(),this);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("HTTPServer","IP address:8080/functionNames" +  getIpAddr());
    }


    public String getIpAddr() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        String ipString = String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        return ipString;
    }

    // DON'T FORGET to stop the server
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (server != null)
            server.stop();
    }

}
