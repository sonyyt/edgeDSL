package vt.cs.selab.androidhttpserver;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import dalvik.system.PathClassLoader;
import fi.iki.elonen.NanoHTTPD;

/**
 * Created by LocalAdministrator on 1/6/2017.
 */
public class MobileWebServer extends NanoHTTPD {
    moduleExecutor executor;

    public MobileWebServer(Context c) {
        super(8080);
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

        } catch (IOException ioe) {
            Log.w("Httpd", ioe.toString());
        }


        return new NanoHTTPD.Response(answer);
    }

    @Override
    public Response serve(IHTTPSession session) {
//        String msg = "<html><body><h1>Hello server</h1>\n";
//        msg += "<p>We serve " + session.getUri() + " !</p>";
//        return new Response(msg + "</body></html>\n");

        String targetPackage = session.getUri().substring(1);
        String targetClass = session.getParms().get("method");
        String result = executor.execute(targetPackage, targetClass, null);
        return new Response(result);
    }


    private class moduleExecutor {
        Context mContext;

        public moduleExecutor(Context c) {
            mContext = c;
        }

        //// TODO: 1/6/2017 check module existence before invoking...If not exist, download it first from a server.
        //// TODO: 1/6/2017 what if two devices are connected in a p2p fashion?
        public String execute(String targetPackage, String targetClass, Map<String, String> param) {
            String exeMethodName = "execute";
            String packagePrefix = "vt.cs.selab.";
            String paramMethodString = "isAvailable";
            //Log.d(logTag,exeMethodName);

            String packagePath = packagePrefix + targetPackage;
            String targetClassName = packagePath + "." + targetClass;
            try {
                String apkName = mContext.getPackageManager().getApplicationInfo(packagePath, 0).sourceDir;
                PathClassLoader pathClassLoader = new PathClassLoader(apkName, ClassLoader.getSystemClassLoader());
                Class<?> targetC = Class.forName(targetClassName, true, pathClassLoader);
                Object targetObject = targetC.newInstance(); // invoke empty constructor

                // read the input parameters

                java.lang.reflect.Method paramMethod = targetObject.getClass().getMethod(paramMethodString, Context.class);
                Boolean isAvailable = (Boolean) paramMethod.invoke(targetObject, mContext); // pass arg

                java.lang.reflect.Method executionMethod = targetObject.getClass().getMethod(exeMethodName, String[].class);
                String result = (String) executionMethod.invoke(targetObject, param); // pass arg
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        }
    }

}
