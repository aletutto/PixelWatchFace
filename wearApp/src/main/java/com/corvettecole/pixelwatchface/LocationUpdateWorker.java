package com.corvettecole.pixelwatchface;

import static com.corvettecole.pixelwatchface.Constants.KEY_LATITUDE;
import static com.corvettecole.pixelwatchface.Constants.KEY_LONGITUDE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.ActivityCompat;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;

public class LocationUpdateWorker extends ListenableWorker {

  /**
   * @param appContext   The application {@link Context}
   * @param workerParams Parameters to setup the internal state of this worker
   */
  public LocationUpdateWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
    super(appContext, workerParams);
  }

  @NonNull
  @Override
  public ListenableFuture<Result> startWork() {
    String TAG = "location_update_worker";
    Log.d(TAG, "starting location work...");
    FusedLocationProviderClient fusedLocationClient = LocationServices
        .getFusedLocationProviderClient(getApplicationContext());

    return CallbackToFutureAdapter.getFuture(completer -> {
      fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
        if (location != null) {
          Log.d(TAG, "location: (" + location.getLatitude() + "," + location
              .getLongitude() + ")");
          //mCurrentWeather.setLocation(location);
          Data output = new Data.Builder()
              .putDouble(KEY_LATITUDE, location.getLatitude())
              .putDouble(KEY_LONGITUDE, location.getLongitude())
              .build();

          completer.set(Result.success(output));
        } else {
          if (ActivityCompat.checkSelfPermission(getApplicationContext(),
              Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            completer.set(Result.failure());
          } else {
            // if no location, but permission exists, try again
            Log.d(TAG, "error retrieving location");
            completer.set(Result.retry());
          }
        }
      });
      return completer;
    });
  }


}
