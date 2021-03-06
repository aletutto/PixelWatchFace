package com.corvettecole.pixelwatchface.weather;

import static com.corvettecole.pixelwatchface.util.WatchFaceUtil.convertToCelsius;
import static com.corvettecole.pixelwatchface.util.WatchFaceUtil.drawableToBitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.corvettecole.pixelwatchface.R;
import com.corvettecole.pixelwatchface.util.Settings;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

public class CurrentWeather {

  private static volatile CurrentWeather instance;

  private String mIconName = "clear-day";
  private long mTime;
  private double mTemperature = Double.MIN_VALUE;
  private double mHumidity;
  private double mPrecipitationChance;
  private String mSummary;
  private String mTimeZone;
  private Bitmap mIconBitmap;
  private String mWeatherProvider;
  private Settings mSettings;

  private CurrentWeather(Context context) {
    if (instance != null) {
      throw new RuntimeException(
          "Use getInstance() method to get the single instance of this class");
    } else {
      mSettings = Settings.getInstance(context.getApplicationContext());
    }
  }

  public static CurrentWeather getInstance(Context context) {
    if (instance == null) {
      synchronized (CurrentWeather.class) {
        if (instance == null) {
          instance = new CurrentWeather(context.getApplicationContext());
        }
      }
    }
    return instance;
  }

  public void parseWeatherJSON(JSONObject forecast) throws JSONException {
    final String TAG = "parseWeatherJSON";
    final String lastIconName = mIconName;

    if (mSettings.isUseDarkSky()) {
      setWeatherProvider("DarkSky");
      mTimeZone = forecast.getString("timezone");
      JSONObject currently = forecast.getJSONObject("currently");
      mHumidity = currently.getDouble("humidity");
      mTime = currently.getLong("time");
      mIconName = currently.getString("icon");
      mPrecipitationChance = currently.getDouble("precipProbability");
      mSummary = currently.getString("summary");
      mTemperature = currently.getDouble("temperature");
      Log.d(TAG, getFormattedTime());
    } else {
      mWeatherProvider = "OpenStreetMap";
      String tempIcon = forecast.getJSONArray("weather").toString();
      JSONObject main = forecast.getJSONObject("main");
      mHumidity = main.getDouble("humidity")
          / 100; //adjust OpenStreetMap format to dark sky format with /100
      mTemperature = main.getDouble("temp");
      String iconName = tempIcon
          .substring(tempIcon.indexOf("\"icon\":\"") + 8, tempIcon.indexOf("\"}]"));
      Log.d(TAG, iconName);
      mIconName = iconName;
    }
    // set icon bitmap to null so that the icon is refreshed when next retrieved
    if (!lastIconName.equals(mIconName)) {
      mIconBitmap = null;
    }
  }

  public String getFormattedTemperature() {
    String unit = mSettings.isUseCelsius() ? "°C" : "°F";
    if (mTemperature == Double.MIN_VALUE) {
      if (mSettings.isShowTemperatureFractional()) {
        if (mSettings.isUseEuropeanDateFormat()) {
          return "--,-" + unit;
        }
        return "--.- " + unit;
      } else {
        return "-- " + unit;
      }
    } else {
      double temperature = mSettings.isUseCelsius() ? convertToCelsius(mTemperature) : mTemperature;
      if (mSettings.isShowTemperatureFractional()) {
        return String.format("%.1f %s", temperature, unit);
      } else {
        return String.format("%d %s", Math.round(temperature), unit);
      }
    }
  }

  public boolean isWeatherDataPresent() {
    return mTemperature != Double.MIN_VALUE;
  }

  public String getTimeZone() {
    return mTimeZone;
  }

  public void setTimeZone(String timeZone) {
    mTimeZone = timeZone;
  }

  public Bitmap getIconBitmap(Context context) {
    if (mIconBitmap == null) {
      mIconBitmap = Bitmap
          .createScaledBitmap(drawableToBitmap(context.getDrawable(getIconId())), 34, 34, false);
    }
    return mIconBitmap;
  }

  public int getIconId() {
    //#TODO use custom icons so as to have fitting icons for every weather condition from any provider (see these: http://adamwhitcroft.com/climacons/)
    int iconId = R.drawable.clear_day;
    if (mWeatherProvider != null) {
      if (mWeatherProvider.equalsIgnoreCase("DarkSky")) {
        // clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night.
        switch (mIconName) {
          case "clear-day":
            iconId = R.drawable.clear_day;
            break;
          case "clear-night":
            iconId = R.drawable.clear_night;
            break;
          case "rain":
            iconId = R.drawable.rain;
            break;
          case "snow":
            iconId = R.drawable.snow;
            break;
          case "sleet":
            iconId = R.drawable.sleet;
            break;
          case "wind":
            iconId = R.drawable.wind;
            break;
          case "fog":
            iconId = R.drawable.fog;
            break;
          case "cloudy":
            iconId = R.drawable.cloudy;
            break;
          case "partly-cloudy-day":
            iconId = R.drawable.partly_cloudy;
            break;
          case "partly-cloudy-night":
            iconId = R.drawable.cloudy_night;
            break;
        }
      } else if (mWeatherProvider.equalsIgnoreCase("OpenStreetMap")) {
        switch (mIconName) {
          case "01d":
            iconId = R.drawable.clear_day;
            break;
          case "01n":
            iconId = R.drawable.clear_night;
            break;
          case "02d":
          case "03d":
            iconId = R.drawable.partly_cloudy;
            break;
          case "02n":
          case "03n":
            iconId = R.drawable.cloudy_night;
            break;
          case "04d":
          case "04n":
            iconId = R.drawable.cloudy;
            break;
          case "09d":
          case "09n":
          case "10d":
          case "11d":
          case "10n":
          case "11n":
            iconId = R.drawable.rain;
            break;
          case "13d":
          case "13n":
            iconId = R.drawable.snow;
            break;
          case "50d":
          case "50n":
            iconId = R.drawable.fog;
            break;
        }
      }
    }
    return iconId;
  }

  public long getTime() {
    return mTime;
  }

  public String getFormattedTime() {
    SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
    formatter.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
    Date dateTime = new Date(getTime() * 1000);
    String timeString = formatter.format(dateTime);

    return timeString;
  }

  public double getHumidity() {
    return mHumidity;
  }

  public void setHumidity(double humidity) {
    mHumidity = humidity;
  }

  public int getPrecipChance() {
    double precipPercentage = mPrecipitationChance * 100;
    return (int) Math.round(precipPercentage);
  }

  public void setPrecipChance(double precipChance) {
    mPrecipitationChance = precipChance;
  }

  public String getSummary() {
    return mSummary;
  }

  public void setSummary(String summary) {
    mSummary = summary;
  }

  public String getWeatherProvider() {
    return mWeatherProvider;
  }

  public void setWeatherProvider(String mWeatherProvider) {
    this.mWeatherProvider = mWeatherProvider;
  }
}
