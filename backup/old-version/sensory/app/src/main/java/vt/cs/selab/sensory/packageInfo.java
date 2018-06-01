package vt.cs.selab.sensory;

import android.content.Context;

/**
 * Created by LocalAdministrator on 7/1/2016.
 */
public class packageInfo {
    String costs;
    String QoS;
    String functions;
    Context mContext;
    public packageInfo(){
        costs = "";
        QoS = "";
        functions = "";
    }

    public String printFunctions(Context c){
        mContext = c;
        gps mgps = new gps();
        if(mgps.isAvailable(mContext)){
            functions += "sensory_gps";
            costs    += mgps.estimateCost();
            QoS      += mgps.estimateQoS();

        }
        return functions;
    }

    public String getCosts(){
        return costs;
    }
    public String getQoS(){return QoS;}

}
