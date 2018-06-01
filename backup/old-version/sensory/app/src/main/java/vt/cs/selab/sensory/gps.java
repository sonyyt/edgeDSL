package vt.cs.selab.sensory;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by LocalAdministrator on 6/24/2016.
 */
public class gps {
    private Context mContext;
    private String logTag = "sensory";


    public boolean isAvailable(Context c){
        mContext = c;
        Log.d("gps","111");
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        if(bestProvider==null) {
            return false;
        }else{
            return true;
        }
    }

    public String execute(String[] params) {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Log.d(logTag,"bestProvider:"+bestProvider);

        if(bestProvider == null){
            Location loc = new Location("");
            loc.setLongitude(80.426745);
            loc.setLatitude(37.231264);
            String msg = "location:"+loc.getLatitude()+"|"+loc.getLongitude();
            Log.d(logTag,msg);
            return msg;
        }else{
            locationManager.requestLocationUpdates(bestProvider, 400, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }


        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
        }
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if(location == null){
            Log.d(logTag,"No Last Know Location:");
            location = new Location("");
            location.setLongitude(-80.426745);
            location.setLatitude(37.231264);
        }
        Double lat,lon;
        lat = location.getLatitude ();
        lon = location.getLongitude();
        Log.d("gps","location:"+lat+"|"+lon);
        return "location:"+lat+"|"+lon;


    }

    public String printParameters(Context context){
        mContext = context;
        return "test1|test2|test3";
    }

    public String estimateCost(){
        float battery = PeerSpecs.getBatteryUsage(mContext);
        return String.valueOf(100-(int)battery);
    }

    public String estimateQoS(){
        float currentCPUUsage = PeerSpecs.readUsage();
        return String.valueOf(1-currentCPUUsage);
    }
}
