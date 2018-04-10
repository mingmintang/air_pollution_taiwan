package com.mingmin.airpollutiontaiwan;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DataService extends Service {

    private static final String AQI_REQUEST_URL = "http://opendata.epa.gov.tw/ws/Data/REWIQA/?sort=SiteName&format=json";

    private SharedPreferences settings;
    private DataDBHelper dbHelper;

    public interface Callback {
        int ERROR_NO_NEW_DATA = 1;
        int ERROR_DOWNLOAD_DATA_FAIL = 2;
        void onUpdateDataCompleted(ArrayList<AQI> newList);
        void onUpdateDataError(int error);
        void onLoadLastDataCompleted(ArrayList<AQI> lastList);
    }

    public void registerCallback(AQIActivity callback) {
        handler = new DataHandler(callback);
    }

    private static class DataHandler extends Handler {
        private static final int MSG_UPDATE_DATA_COMPLETED = 1;
        private static final int MSG_UPDATE_DATA_ERROR = 2;
        private static final int MSG_LOAD_LAST_DATA_COMPLETED = 3;
        private final WeakReference<AQIActivity> ref;
        private DataHandler(AQIActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DataService.Callback callback = ref.get();
            switch (msg.what) {
                case MSG_UPDATE_DATA_COMPLETED:
                    ArrayList<AQI> list = (ArrayList<AQI>) msg.obj;
                    callback.onUpdateDataCompleted(list);
                    break;
                case MSG_UPDATE_DATA_ERROR:
                    int error = (int) msg.obj;
                    callback.onUpdateDataError(error);
                    break;
                case MSG_LOAD_LAST_DATA_COMPLETED:
                    ArrayList<AQI> lastList = (ArrayList<AQI>) msg.obj;
                    callback.onLoadLastDataCompleted(lastList);
                    break;
            }
        }
    }
    private DataHandler handler;

    public class DataBinder extends Binder {
        DataService getService() {
            return DataService.this;
        }
    }

    private DataBinder dataBinder = new DataBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences(Settings.PREF_FILE_NAME, MODE_PRIVATE);
        dbHelper = DataDBHelper.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return dataBinder;
    }

    public void updateData() {
        Date currentDate = new Date();
        long currentTime = currentDate.getTime();
        long lastUpdateTime = settings.getLong(Settings.LAST_UPDATE_TIME, 0);
        if ((currentTime - lastUpdateTime) >= 60 * 60 * 1000) {
            Request request = new Request.Builder()
                    .url(AQI_REQUEST_URL)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Call httpCall = client.newCall(request);
            httpCall.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Message msg = new Message();
                    msg.what = DataHandler.MSG_UPDATE_DATA_ERROR;
                    msg.obj = Callback.ERROR_DOWNLOAD_DATA_FAIL;
                    handler.sendMessage(msg);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String json = response.body().string();
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(int.class, new AQI.NumberDeserializer())
                            .registerTypeAdapter(float.class, new AQI.NumberDeserializer())
                            .registerTypeAdapter(Date.class, new AQI.DateDeserializer())
                            .create();
                    ArrayList<AQI> aqiList = gson.fromJson(json, new TypeToken<ArrayList<AQI>>() {
                    }.getType());
                    long newTime = aqiList.get(0).getPublishTime().getTime();
                    long lastUpdateTime = settings.getLong(Settings.LAST_UPDATE_TIME, 0);
                    if (newTime > lastUpdateTime) {
                        Message msg = new Message();
                        msg.what = DataHandler.MSG_UPDATE_DATA_COMPLETED;
                        msg.obj = aqiList;
                        handler.sendMessage(msg);
                        settings.edit()
                                .putLong(Settings.LAST_UPDATE_TIME, newTime)
                                .apply();
                        updateAQITableData(aqiList);
                    } else if (newTime == lastUpdateTime) {
                        Message msg = new Message();
                        msg.what = DataHandler.MSG_UPDATE_DATA_ERROR;
                        msg.obj = Callback.ERROR_NO_NEW_DATA;
                        handler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.what = DataHandler.MSG_UPDATE_DATA_ERROR;
                        msg.obj = Callback.ERROR_DOWNLOAD_DATA_FAIL;
                        handler.sendMessage(msg);
                    }
                }
            });
        } else {
            Message msg = new Message();
            msg.what = DataHandler.MSG_UPDATE_DATA_ERROR;
            msg.obj = Callback.ERROR_NO_NEW_DATA;
            handler.sendMessage(msg);
        }
    }

    private void updateAQITableData(ArrayList<AQI> list) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DataDBHelper.AQITable.TABLE_NAME, null, null);
        for (AQI aqi : list) {
            ContentValues cv = new ContentValues();
            cv.put(DataDBHelper.AQITable.COLUMN_SITE_NAME, aqi.getSiteName());
            cv.put(DataDBHelper.AQITable.COLUMN_COUNTY, aqi.getCounty());
            cv.put(DataDBHelper.AQITable.COLUMN_AQI, aqi.getAQI());
            cv.put(DataDBHelper.AQITable.COLUMN_POLLUTANT, aqi.getPollutant());
            cv.put(DataDBHelper.AQITable.COLUMN_STATUS, aqi.getStatus());
            cv.put(DataDBHelper.AQITable.COLUMN_SO2, aqi.getSO2());
            cv.put(DataDBHelper.AQITable.COLUMN_CO, aqi.getCO());
            cv.put(DataDBHelper.AQITable.COLUMN_CO_8HR, aqi.getCO_8hr());
            cv.put(DataDBHelper.AQITable.COLUMN_O3, aqi.getO3());
            cv.put(DataDBHelper.AQITable.COLUMN_O3_8HR, aqi.getO3_8hr());
            cv.put(DataDBHelper.AQITable.COLUMN_PM10, aqi.getPM10());
            cv.put(DataDBHelper.AQITable.COLUMN_PM2_5, aqi.getPM2_5());
            cv.put(DataDBHelper.AQITable.COLUMN_NO2, aqi.getNO2());
            cv.put(DataDBHelper.AQITable.COLUMN_NOx, aqi.getNOx());
            cv.put(DataDBHelper.AQITable.COLUMN_NO, aqi.getNO());
            cv.put(DataDBHelper.AQITable.COLUMN_WIND_DIREC, aqi.getWindDirec());
            cv.put(DataDBHelper.AQITable.COLUMN_WIND_SPEED, aqi.getWindSpeed());
            cv.put(DataDBHelper.AQITable.COLUMN_PUBLISH_TIME, aqi.getPublishTime().getTime());
            cv.put(DataDBHelper.AQITable.COLUMN_PM2_5_AVG, aqi.getPM2_5_AVG());
            cv.put(DataDBHelper.AQITable.COLUMN_PM10_AVG, aqi.getPM10_AVG());
            cv.put(DataDBHelper.AQITable.COLUMN_LATITUDE, aqi.getLatitude());
            cv.put(DataDBHelper.AQITable.COLUMN_LONGITUDE, aqi.getLongitude());
            db.insert(DataDBHelper.AQITable.TABLE_NAME, null, cv);
        }
        db.close();
    }

    private ArrayList<AQI> loadAQITableData(long lastUpdateTime) {
        ArrayList<AQI> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cur = db.query(DataDBHelper.AQITable.TABLE_NAME,
                null,
                DataDBHelper.AQITable.COLUMN_PUBLISH_TIME + "=?",
                new String[]{String.valueOf(lastUpdateTime)},
                null,
                null,
                null);
        if (cur != null) {
            int count = cur.getCount();
            if (count > 0) {
                cur.moveToFirst();
                do {
                    AQI aqi = new AQI.Builder()
                            .setSiteName(cur.getString(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_SITE_NAME)))
                            .setCounty(cur.getString(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_COUNTY)))
                            .setAQI(cur.getInt(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_AQI)))
                            .setPollutant(cur.getString(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_POLLUTANT)))
                            .setStatus(cur.getString(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_STATUS)))
                            .setSO2(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_SO2)))
                            .setCO(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_CO)))
                            .setCO_8hr(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_CO_8HR)))
                            .setO3(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_O3)))
                            .setO3_8hr(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_O3_8HR)))
                            .setPM10(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_PM10)))
                            .setPM2_5(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_PM2_5)))
                            .setNO2(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_NO2)))
                            .setNOx(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_NOx)))
                            .setNO(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_NO)))
                            .setWindDirec(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_WIND_DIREC)))
                            .setWindSpeed(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_WIND_SPEED)))
                            .setPublishTime(new Date(cur.getLong(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_PUBLISH_TIME))))
                            .setPM2_5_AVG(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_PM2_5_AVG)))
                            .setPM10_AVG(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_PM10_AVG)))
                            .setLatitude(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_LATITUDE)))
                            .setLongitude(cur.getFloat(cur.getColumnIndex(DataDBHelper.AQITable.COLUMN_LONGITUDE)))
                            .build();
                    list.add(aqi);
                } while (cur.moveToNext());
            }
            cur.close();
        }
        db.close();
        return list;
    }

    public void loadLastAQIData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<AQI> list = loadAQITableData(settings.getLong(Settings.LAST_UPDATE_TIME, 0));
                Message msg = new Message();
                msg.what = DataHandler.MSG_LOAD_LAST_DATA_COMPLETED;
                msg.obj = list;
                handler.sendMessage(msg);
            }
        }).start();
    }
}