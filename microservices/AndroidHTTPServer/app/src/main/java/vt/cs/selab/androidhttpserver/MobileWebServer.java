package vt.cs.selab.androidhttpserver;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import executable.MicroService;
import executable.getGPSLocation;
import executable.getNetworkLocation;
import fi.iki.elonen.NanoHTTPD;

/**
 * Created by LocalAdministrator on 1/6/2017.
 */
public class MobileWebServer  extends NanoHTTPD {
    moduleExecutor executor;
    MainActivity a;
    public MobileWebServer(Context c, MainActivity activity)
    {
        super(8080);
        a = activity;
        executor = new moduleExecutor(c);

    }
    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files) {
        String answer = "";
        try {
            // Open file from SD Card
            File root = Environment.getExternalStorageDirectory();
            FileReader index = new FileReader(root.getAbsolutePath() + "/www/index.html");
            BufferedReader reader = new BufferedReader(index);
            String line = "";
            while ((line = reader.readLine()) != null) {
                answer += line;
            }
            reader.close();

        } catch(IOException ioe) {
            Log.w("Httpd", ioe.toString());
        }


        return new NanoHTTPD.Response(answer);
    }

    @Override
    public Response serve(IHTTPSession session) {
        //a.addLog(session.getUri());
        //String msg = "<html><body><h1>Hello server</h1>\n";
        //msg += "<p>We serve " + session.getUri() + session.getParms() + " !</p>";
        //return new Response(msg + "</body></html>\n");
        long start = System.currentTimeMillis();
        String targetMS  = session.getUri().substring(1);
        Log.d("HTTP Server","executing:"+ targetMS);
        if(targetMS.contains("favicon")){
            return null;
        }
        String result = executor.execute(targetMS,session.getParms());
        long end = System.currentTimeMillis();
        Log.d("HTTP Server", "Processing Time (ms): " + (end - start));
        return new Response(result);
    }


    private class moduleExecutor{
        Context mContext;
        public moduleExecutor(Context c){
            mContext = c;
        }
        //// TODO: 1/6/2017 check module existence before invoking...If not exist, download it first from a server.
        //// TODO: 1/6/2017 what if two devices are connected in a p2p fashion?
        public String execute(String targetMS, Map<String,String> params) {
            MicroService ms = null;
            Log.d("HTTP Server", "init MS executin package for:"+targetMS);
            if(targetMS.equals("getGPSLocation")){
                //Log.d("HTTP Server", "init MS executin package for"+targetMS);
                ms = (MicroService) new getGPSLocation(mContext);
            }else if(targetMS.equals("getNetworkLocation")){
                ms = (MicroService) new getNetworkLocation(mContext);
            }
            return ms.execute(params);
        }
    }

}
