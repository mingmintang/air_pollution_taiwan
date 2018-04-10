package com.mingmin.airpollutiontaiwan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "data.db";
    private static final int DB_VERSION = 1;

    private static DataDBHelper instance;
    public static DataDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DataDBHelper(context, DB_NAME, null, DB_VERSION);
        }
        return instance;
    }

    private DataDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAQITable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class AQITable {
        public static final String TABLE_NAME = "aqi";
        public static final String COLUMN_SITE_NAME = "SiteName";
        public static final String COLUMN_COUNTY = "County";
        public static final String COLUMN_AQI = "AQI";
        public static final String COLUMN_POLLUTANT = "Pollutant";
        public static final String COLUMN_STATUS = "Status";
        public static final String COLUMN_SO2 = "SO2";
        public static final String COLUMN_CO = "CO";
        public static final String COLUMN_CO_8HR = "CO_8hr";
        public static final String COLUMN_O3 = "O3";
        public static final String COLUMN_O3_8HR = "O3_8hr";
        public static final String COLUMN_PM10 = "PM10";
        public static final String COLUMN_PM2_5 = "PM2_5";
        public static final String COLUMN_NO2 = "NO2";
        public static final String COLUMN_NOx = "NOx";
        public static final String COLUMN_NO = "NO";
        public static final String COLUMN_WIND_DIREC = "WindDirec";
        public static final String COLUMN_WIND_SPEED = "WindSpeed";
        public static final String COLUMN_PUBLISH_TIME = "PublishTime";
        public static final String COLUMN_PM2_5_AVG = "PM2_5_AVG";
        public static final String COLUMN_PM10_AVG = "PM10_AVG";
        public static final String COLUMN_LONGITUDE = "Longitude";
        public static final String COLUMN_LATITUDE = "Latitude";
    }

    private void createAQITable(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE aqi")
          .append(" (_id INTEGER PRIMARY KEY, ")
          .append("SiteName TEXT, ")
          .append("County TEXT, ")
          .append("AQI INTEGER, ")
          .append("Pollutant TEXT, ")
          .append("Status TEXT, ")
          .append("SO2 REAL, ")
          .append("CO REAL, ")
          .append("CO_8hr REAL, ")
          .append("O3 REAL, ")
          .append("O3_8hr REAL, ")
          .append("PM10 REAL, ")
          .append("PM2_5 REAL, ")
          .append("NO2 REAL, ")
          .append("NOx REAL, ")
          .append("NO REAL, ")
          .append("WindDirec REAL, ")
          .append("WindSpeed REAL, ")
          .append("PublishTime INTEGER, ")
          .append("PM2_5_AVG REAL, ")
          .append("PM10_AVG REAL, ")
          .append("Longitude REAL, ")
          .append("Latitude REAL)");
        db.execSQL(sb.toString());
    }
}
