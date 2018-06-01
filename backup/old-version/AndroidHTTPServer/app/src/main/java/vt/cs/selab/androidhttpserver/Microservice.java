package vt.cs.selab.androidhttpserver;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.PathClassLoader;

/**
 * Created by LocalAdministrator on 1/20/2017.
 */
public class Microservice{
    final String logTag = "Microservice";
    String serviceMarketAddr = "http://speed.cs.vt.edu/servicemarket/";
    httpClient http;
    MainActivity bindedActivity;

    public Microservice(MainActivity act){
        http = new httpClient();
        bindedActivity = act;
    }

    public void queryServiceByKeyword(String keyword){
        queryThread thread = new queryThread(bindedActivity);
        thread.execute(keyword);
    }



    class queryThread extends AsyncTask<String, Void, String[]> {
        private Exception exception;
        MainActivity mainActivity;

        public queryThread(MainActivity activity){
            mainActivity = activity;
        }

        @Override
        protected String[] doInBackground(String... params) {
            String keyword = params[0];
            try {
                if(keyword.equals("")){
                    keyword="%";
                }
                String url = serviceMarketAddr+"search.php?keyword="+keyword;
                String result = http.run(url);
                if(!result.contains("error")){
                    String[] items = result.split("\\|");
                    return items;
                }

            } catch (IOException e) {
                    e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String[] result) {
            mainActivity.listViewShowMicroservices(result);
        }
    }


    class httpThread extends AsyncTask<String, Void, Void> {
        private Exception exception;

        public httpThread(){
        }

        @Override
        protected Void doInBackground(String... params) {
            for(int i=0;i<params.length;i++) {
                String url = params[0];
                try {
                    String result = http.run(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute( ) {
        }
    }

    public void registerServices(){
        Context mContext = bindedActivity.getApplicationContext();
        List<String> services = new ArrayList<>();
        List<String> serviceCosts = new ArrayList<>();
        List<String> serviceQoSs = new ArrayList<>();
        String packageInfo = "packageInfo";
        String QoSFunctionsString = "getQos";
        String costFunctionsString = "getCosts";
        String printFunctionsString = "printFunctions";


        List<ApplicationInfo> apps = mContext.getPackageManager().getInstalledApplications(1);
        for (ApplicationInfo info:apps) {
            //Log.d(logTag,info.packageName);
            if (info.packageName.contains("vt.cs.selab") && !info.packageName.equals(mContext.getPackageName())  && !info.packageName.equals("vt.cs.selab.client")) {
                Log.d(logTag,info.packageName+"Package Found");
                String packagePrefix = info.packageName.replace("vt.cs.selab.", "");
                String apkName = null;
                try {
                    apkName = mContext.getPackageManager().getApplicationInfo(info.packageName, 0).sourceDir;
                    Log.d(logTag, apkName);
                    PathClassLoader pathClassLoader = new PathClassLoader(apkName, ClassLoader.getSystemClassLoader());
                    Class<?> targetC = Class.forName(info.packageName + "." + packageInfo, true, pathClassLoader);
                    Object targetObject = targetC.newInstance(); // invoke empty constructor

                    //read the requested permission
                    Method printFunctionMethod = targetObject.getClass().getMethod(printFunctionsString, Context.class);
                    String functionString = (String) printFunctionMethod.invoke(targetObject,mContext); // pass arg

                    Log.d(logTag, functionString);

                    if(!functionString.isEmpty()) {
                        String[] functions = functionString.split("\\|");
                        for (String func : functions) {
                            if (!func.trim().equals(null)) {
                                Log.d(logTag, "Service Available:" + func);
                                services.add(func);
                            }
                        }


                        Method costFunctionMethod = targetObject.getClass().getMethod(costFunctionsString);
                        String costString = (String) costFunctionMethod.invoke(targetObject); // pass arg
                        String[] costs = costString.split("\\|");
                        for (String cost : costs) {
                            if (!cost.trim().equals(null)) {
                                Log.d(logTag, "Cost:" + cost);
                                serviceCosts.add(cost);
                            }
                        }

                        Method QoSFunctionMethod = targetObject.getClass().getMethod(QoSFunctionsString);
                        String QoSString = (String) QoSFunctionMethod.invoke(targetObject); // pass arg
                        String[] QoSs = QoSString.split("\\|");
                        for (String QoS : QoSs) {
                            if (!QoS.trim().equals(null)) {
                                Log.d(logTag, "QoS:" + QoS);
                                serviceQoSs.add(QoS);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        WifiManager wifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String myIPAddress = Formatter.formatIpAddress(ip);

        String deviceReg = "http://192.168.1.1:81/reg.php?type=device&device_id="+myIPAddress;


        String serviceReg = "http://192.168.1.1:81/reg.php?type=service&device_id="+myIPAddress+"&service_name="
                +implode(services.toArray(new String[services.size()]),"|")+"&cost="+implode(serviceCosts.toArray(new String[serviceCosts.size()]),"|")
                +"&qos="+implode(serviceQoSs.toArray(new String[serviceQoSs.size()]), "|");
        Log.d(logTag,"Service Registration String:"+serviceReg);

        httpThread thread = new httpThread();
        thread.execute(deviceReg,serviceReg);
    }

    public String implode(String[] input, String connector){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length; i++) {
            sb.append(input[i]);
            if (i != input.length - 1) {
                sb.append(connector);
            }
        }
        return sb.toString();
    }

}
