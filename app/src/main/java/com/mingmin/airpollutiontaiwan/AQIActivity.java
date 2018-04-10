package com.mingmin.airpollutiontaiwan;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class AQIActivity extends AppCompatActivity implements ServiceConnection, DataService.Callback {

    private SharedPreferences settings;
    private ArrayList<AQI> aqiList = new ArrayList<>();
    private ArrayList<AQI> filteredAqiList = new ArrayList<>();
    private AQIAdapter aqiAdapter;
    private DataService dataService;
    private RecyclerView recyclerView;
    private TextView tvPublishTime;
    private TextView tvStatus;
    private ImageButton ibFilter;
    private ImageButton ibSort;
    private ProgressTask progressTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aqi);
        findViews();
        switchUIStatus(false);
        settings = getSharedPreferences(Settings.PREF_FILE_NAME, MODE_PRIVATE);
        progressTask = new ProgressTask(this);
        progressTask.execute();
    }

    private void findViews() {
        tvPublishTime = findViewById(R.id.activity_aqi_publishTime);
        recyclerView = findViewById(R.id.activity_aqi_recyclerView);
        tvStatus = findViewById(R.id.activity_aqi_status);
        ibFilter = findViewById(R.id.activity_aqi_filter_data);
        ibSort = findViewById(R.id.activity_aqi_sort);
        ibSort.setTag(R.drawable.ic_sort_descending);
    }

    private void switchUIStatus(boolean isTurnedOn) {
        if (isTurnedOn) {
            ibFilter.setClickable(true);
            ibSort.setClickable(true);
            tvPublishTime.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.INVISIBLE);
        } else {
            ibFilter.setClickable(false);
            ibSort.setClickable(false);
            tvPublishTime.setVisibility(View.GONE);
            recyclerView.setVisibility(View.INVISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
        }
    }

    private void updatePublishTime(Date newDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        tvPublishTime.setText(sdf.format(newDate));
    }

    private void updateRecycleView(ArrayList<AQI> newList) {
        if (aqiAdapter == null && recyclerView.getAdapter() == null) {
            aqiAdapter = new AQIAdapter(newList);
            recyclerView.setAdapter(aqiAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            aqiAdapter.updateAQIList(newList);
        }
    }

    private ArrayList<AQI> filterCheckedCounties(ArrayList<AQI> list, boolean[] checkedCounties) {
        ArrayList<AQI> newList = new ArrayList<>();
        for (AQI aqi:list) {
            if (checkedCounties[aqi.getCountyIndex()]) {
                newList.add(aqi);
            }
        }
        return newList;
    }

    public void filter(View view) {
        final boolean[] checkedCounties = readCheckedCounties();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("選擇顯示縣市")
                .setMultiChoiceItems(R.array.counties, checkedCounties, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedCounties[which] = isChecked;
                    }
                })
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder sb = new StringBuilder();
                        for (int i=0; i<checkedCounties.length; i++) {
                            sb.append(checkedCounties[i]);
                            if (i < (checkedCounties.length - 1)) {
                                sb.append(",");
                            }
                        }
                        settings.edit()
                                .putString(Settings.CHECKED_COUNTIES, sb.toString())
                                .apply();
                        boolean isAscending = settings.getBoolean(Settings.IS_ASCENDING_AQI_SORT, false);
                        filteredAqiList = filterCheckedCounties(aqiList, readCheckedCounties());
                        updateRecycleView(sortByAQIValue(filteredAqiList, isAscending));
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    private boolean[] readCheckedCounties() {
        boolean[] checkedCounties = new boolean[AQI.COUNTIES.length];
        String strCheckedCounties = settings.getString(Settings.CHECKED_COUNTIES, "");
        if (strCheckedCounties.equals("")) {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<checkedCounties.length; i++) {
                checkedCounties[i] = true;
                sb.append(true);
                if (i < (checkedCounties.length - 1)) {
                    sb.append(",");
                }
            }
            settings.edit()
                    .putString(Settings.CHECKED_COUNTIES, sb.toString())
                    .apply();
        } else {
            String[] items = strCheckedCounties.split(",");
            if (items.length == checkedCounties.length) {
                for (int i=0; i<checkedCounties.length; i++) {
                    checkedCounties[i] = Boolean.parseBoolean(items[i]);
                }
            }
        }
        return checkedCounties;
    }

    private ArrayList<AQI> sortByAQIValue(ArrayList<AQI> list, final boolean isAscending) {
        Collections.sort(list, new Comparator<AQI>() {
            @Override
            public int compare(AQI o1, AQI o2) {
                if (isAscending) {
                    return o1.getAQI() - o2.getAQI();
                } else {
                    return o2.getAQI() - o1.getAQI();
                }
            }
        });
        return list;
    }

    public void sort(View view) {
        ImageButton ib = (ImageButton) view;
        int imageId = (int) ib.getTag();
        switch (imageId) {
            case R.drawable.ic_sort_descending:
                updateRecycleView(sortByAQIValue(filteredAqiList, true));
                ib.setTag(R.drawable.ic_sort_ascending);
                ib.setImageResource(R.drawable.ic_sort_ascending);
                settings.edit()
                        .putBoolean(Settings.IS_ASCENDING_AQI_SORT, true)
                        .apply();
                break;
            case R.drawable.ic_sort_ascending:
                updateRecycleView(sortByAQIValue(filteredAqiList, false));
                ib.setTag(R.drawable.ic_sort_descending);
                ib.setImageResource(R.drawable.ic_sort_descending);
                settings.edit()
                        .putBoolean(Settings.IS_ASCENDING_AQI_SORT, false)
                        .apply();
                break;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        DataService.DataBinder binder = (DataService.DataBinder) service;
        dataService = binder.getService();
        dataService.registerCallback(this);
        dataService.updateData();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        dataService = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, DataService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(this);
    }

    @Override
    public void onUpdateDataCompleted(ArrayList<AQI> newList) {
        aqiList = newList;
        updatePublishTime(aqiList.get(0).getPublishTime());
        boolean isAscending =settings.getBoolean(Settings.IS_ASCENDING_AQI_SORT, false);
        if (isAscending && (Integer)ibSort.getTag() == R.drawable.ic_sort_descending) {
            ibSort.setTag(R.drawable.ic_sort_ascending);
            ibSort.setImageResource(R.drawable.ic_sort_ascending);
        }
        filteredAqiList = filterCheckedCounties(aqiList, readCheckedCounties());
        updateRecycleView(sortByAQIValue(filteredAqiList, isAscending));
        switchUIStatus(true);
        cancelProgressTask();
    }

    @Override
    public void onUpdateDataError(int error) {
        switch (error) {
            case ERROR_NO_NEW_DATA:
                Snackbar.make(this.recyclerView, "沒有更新資料",  Snackbar.LENGTH_LONG).show();
                break;
            case ERROR_DOWNLOAD_DATA_FAIL:
                Snackbar.make(this.recyclerView, "下載資料失敗",  Snackbar.LENGTH_LONG).show();
                break;
        }
        dataService.loadLastAQIData();
    }

    @Override
    public void onLoadLastDataCompleted(ArrayList<AQI> lastList) {
        if (lastList.size() != 0) {
            aqiList = lastList;
            updatePublishTime(aqiList.get(0).getPublishTime());
            switchUIStatus(true);
            boolean isAscending =settings.getBoolean(Settings.IS_ASCENDING_AQI_SORT, false);
            if (isAscending && (Integer)ibSort.getTag() == R.drawable.ic_sort_descending) {
                ibSort.setTag(R.drawable.ic_sort_ascending);
                ibSort.setImageResource(R.drawable.ic_sort_ascending);
            }
            filteredAqiList = filterCheckedCounties(aqiList, readCheckedCounties());
            updateRecycleView(sortByAQIValue(filteredAqiList, isAscending));
        } else {
            tvStatus.setText("載入資料失敗\n點擊重新載入");
            tvStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressTask = new ProgressTask(v.getContext());
                    progressTask.execute();
                    dataService.updateData();
                }
            });
        }
        cancelProgressTask();
    }

    private class ProgressTask extends AsyncTask<Void, Integer, Void> {

        private Context context;
        private AlertDialog dialog;

        private ProgressTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new AlertDialog.Builder(context)
                    .setView(R.layout.progress_spinner)
                    .setCancelable(false)
                    .show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                //Auto close this task after 15 seconds.
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }

    private void cancelProgressTask() {
        if (progressTask.getStatus() != AsyncTask.Status.FINISHED) {
            progressTask.cancel(true);
        }
    }
}