package cn.bahamut.vessage.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import cn.bahamut.observer.Observable;
import cn.bahamut.service.OnServiceInit;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.AppMain;

/**
 * Created by alexchow on 16/6/22.
 */
public class LocationService extends Observable implements OnServiceUserLogin, OnServiceUserLogout, OnServiceInit {
    private static final long MIN_TIME_MSEC = 1000 * 60 * 60;
    private static final float MIN_DISTANCE_METRE = 100;
    public static final String LOCATION_UPDATED = "LOCATION_UPDATED";
    private LocationManager locationManager;

    @Override
    public void onServiceInit(Context applicationContext) {
        locationManager = (LocationManager) AppMain.getInstance().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onUserLogin(String userId) {
        ServicesProvider.setServiceReady(LocationService.class);
        if (ActivityCompat.checkSelfPermission(AppMain.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AppMain.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_MSEC,
                MIN_DISTANCE_METRE, locationListener);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(LocationService.class);
        if (ActivityCompat.checkSelfPermission(AppMain.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AppMain.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    private volatile Location here;
    public String getHereString(){
        if(here == null){
            return null;
        }
        return String.format("{ \"type\": \"Point\", \"coordinates\": [%f, %f] }",here.getLongitude(),here.getLatitude());
    }

    public String getHereShortString(){
        if(here == null){
            return null;
        }
        return String.format("{\"long\":%f,\"lati\":%f,\"alti\":%f}",here.getLongitude(),here.getLatitude(),here.getAltitude());
    }

    public double getDistanceOfHere(Location location) {
        if (location != null && here != null) {
            return location.distanceTo(here);
        }
        return -1;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            here = location;
            postNotification(LocationService.LOCATION_UPDATED);
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
    };
}
