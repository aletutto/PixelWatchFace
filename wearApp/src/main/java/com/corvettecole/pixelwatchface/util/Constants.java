package com.corvettecole.pixelwatchface.util;

public class Constants {

  public static final String LOCATION_UPDATE_WORKER = "location_update_worker";
  public static final String WEATHER_UPDATE_WORKER = "weather_update_worker";
  public static final int WEATHER_UPDATE_INTERVAL = 30;
  public static final int WEATHER_BACKOFF_DELAY_ONETIME = 30; // seconds
  public static final int WEATHER_BACKOFF_DELAY_PERIODIC = 5; // minutes
  public static final int WEATHER_FLEX_PERIOD = 15; // minutes, run within 15 minutes of the 30 minute period

  public static final String KEY_LATITUDE = "latitude";
  public static final String KEY_LONGITUDE = "longitude";

  // ratio values for watch face placement
  public static final float WEATHER_ICON_MARGIN_RATIO = 1.7f;
  public static final float WEATHER_ICON_Y_OFFSET_RATIO = 1.4f;
  public static final float INFO_BAR_Y_SPACING_RATIO = 1.8f;

  public enum UPDATE_REQUIRED {
    WEATHER,
    FONT
  }

}
