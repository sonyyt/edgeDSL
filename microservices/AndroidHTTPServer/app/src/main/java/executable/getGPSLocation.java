package executable;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.Map;

/**
 * Created by LocalAdministrator on 6/24/2016.
 */
public class getGPSLocation implements MicroService {
    private Context mContext;
    String Provider = "gps";
    String logTag = "getGPSLocation";
    LocationManager locationManager;

    public getGPSLocation(Context c) {
        mContext = c;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (bestProvider.equals(Provider)) {
            Log.d(logTag, "gps is available;");
        }
    }

    @Override
    public String execute(Map<String, String> params) {
        Looper.prepare();
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        locationManager.requestLocationUpdates(Provider, 400, 1, new LocationListener() {
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

        Location location = locationManager.getLastKnownLocation("gps");
        if(location == null){
            Log.d(logTag,"No Last Know Location:");
            return null;
        }
        Double lat,lon;
        lat = location.getLatitude ();
        lon = location.getLongitude();
        Log.d("gps","location: lat: "+lat+"| lon:"+lon);
        return lat+"|"+lon;


    }

}
