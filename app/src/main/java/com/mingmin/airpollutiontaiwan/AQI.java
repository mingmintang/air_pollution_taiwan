package com.mingmin.airpollutiontaiwan;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AQI {
    private String SiteName;        //測站名稱
    private String County;          //縣市
    private int AQI = -1;           //空氣品質指標AQI
    private String Pollutant;       //空氣污染指標物
    private String Status;          //狀態
    private float SO2 = -1;         //二氧化硫(ppb)
    private float CO = -1;          //一氧化碳(ppm)
    private float CO_8hr = -1;      //一氧化碳8小時移動平均(ppm)
    private float O3 = -1;          //臭氧(ppb)
    private float O3_8hr = -1;      //臭氧8小時移動平均(ppb)
    private float PM10 = -1;        //懸浮微粒PM10(μg/m3)
    @SerializedName("PM2.5")
    private float PM2_5 = -1;       //細懸浮微粒PM2.5(μg/m3)
    private float NO2 = -1;         //二氧化氮(ppb)
    private float NOx= -1;          //氮氧化物(ppb)
    private float NO = -1;          //一氧化氮(ppb)
    private float WindDirec = -1;   //風向(degrees)
    private float WindSpeed = -1;   //風速(m/sec)
    private Date PublishTime;       //發布時間
    @SerializedName("PM2.5_AVG")
    private float PM2_5_AVG = -1;   //細懸浮微粒PM2.5移動平均值(μg/m3)
    private float PM10_AVG = -1;    //懸浮微粒PM10移動平均值(μg/m3)
    private float Latitude = -1;    //緯度
    private float Longitude = -1;   //經度

    public String getSiteName() {
        return SiteName;
    }

    public String getCounty() {
        return County;
    }

    public int getAQI() {
        return AQI;
    }

    public String getPollutant() {
        return Pollutant;
    }

    public String getStatus() {
        return Status;
    }

    public float getSO2() {
        return SO2;
    }

    public float getCO() {
        return CO;
    }

    public float getCO_8hr() {
        return CO_8hr;
    }

    public float getO3() {
        return O3;
    }

    public float getO3_8hr() {
        return O3_8hr;
    }

    public float getPM10() {
        return PM10;
    }

    public float getPM2_5() {
        return PM2_5;
    }

    public float getNO2() {
        return NO2;
    }

    public float getNOx() {
        return NOx;
    }

    public float getNO() {
        return NO;
    }

    public float getWindDirec() {
        return WindDirec;
    }

    public float getWindSpeed() {
        return WindSpeed;
    }

    public Date getPublishTime() {
        return PublishTime;
    }

    public float getPM2_5_AVG() {
        return PM2_5_AVG;
    }

    public float getPM10_AVG() {
        return PM10_AVG;
    }

    public float getLatitude() {
        return Latitude;
    }

    public float getLongitude() {
        return Longitude;
    }

    /*
        Level 1: AQI 0~50
        Level 2: AQI 51~100
        Level 3: AQI 101~150
        Level 4: AQI 151~200
        Level 5: AQI 201~300
        Level 6: AQI 301~500
         */
    public int getAQILevel() {
        int level = 0;
        if (0 <= AQI && AQI <= 50) {
            level = 1;
        } else if (51 <= AQI && AQI <= 100) {
            level = 2;
        } else if (101 <= AQI && AQI <= 150) {
            level = 3;
        } else if (151 <= AQI && AQI <= 200) {
            level = 4;
        } else if (201 <= AQI && AQI <= 300) {
            level = 5;
        } else if (301 <= AQI && AQI <= 500) {
            level = 6;
        }
        return level;
    }

    public static final String[] COUNTIES = {
            "基隆市", "新北市", "臺北市", "桃園市", "新竹縣",
            "新竹市", "苗栗縣", "臺中市", "彰化縣", "南投縣",
            "雲林縣", "嘉義縣", "嘉義市", "臺南市", "高雄市",
            "屏東縣", "臺東縣", "花蓮縣", "宜蘭縣", "連江縣",
            "金門縣", "澎湖縣"
    };

    public int getCountyIndex() {
        int index = -1;
        for (int i=0; i<COUNTIES.length; i++) {
            if (COUNTIES[i].equals(getCounty())) {
                index = i;
            }
        }
        return index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Show AQI data:\n");
        sb.append("AQI:").append(getAQI()).append("\n");
        sb.append("CO:").append(getCO()).append("\n");
        sb.append("CO_8hr:").append(getCO_8hr()).append("\n");
        sb.append("County:").append(getCounty()).append("\n");
        sb.append("NO:").append(getNO()).append("\n");
        sb.append("NO2:").append(getNO2()).append("\n");
        sb.append("NOx:").append(getNOx()).append("\n");
        sb.append("O3:").append(getO3()).append("\n");
        sb.append("O3_8hr:").append(getO3_8hr()).append("\n");
        sb.append("PM10:").append(getPM10()).append("\n");
        sb.append("PM10_AVG:").append(getPM10_AVG()).append("\n");
        sb.append("PM2.5:").append(getPM2_5()).append("\n");
        sb.append("PM2.5_AVG:").append(getPM2_5_AVG()).append("\n");
        sb.append("Pollutant:").append(getPollutant()).append("\n");
        sb.append("PublishTime:").append(getPublishTime()).append("\n");
        sb.append("SiteName:").append(getSiteName()).append("\n");
        sb.append("SO2:").append(getSO2()).append("\n");
        sb.append("Status:").append(getStatus()).append("\n");
        sb.append("WindDirec:").append(getWindDirec()).append("\n");
        sb.append("WindSpeed:").append(getWindSpeed()).append("\n");
        sb.append("Longitude:").append(getLongitude()).append("\n");
        sb.append("Latitude:").append(getLatitude()).append("\n");
        return sb.toString();
    }

    //Replace NumberTypeAdapter with NumberDeserializer
    public static class NumberTypeAdapter extends TypeAdapter<Number> {

        private Type type;

        public NumberTypeAdapter(Type type) {
            super();
            this.type = type;
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(value);
        }

        @Override
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String stringNumber = in.nextString();
            try{
                if ("".equals(stringNumber)) {
                    //Return -2 when value is empty string.
                    return -2;
                }
                if ("int".equals(type.toString())) {
                    return Integer.parseInt(stringNumber);
                } else if ("float".equals(type.toString())) {
                    return Float.parseFloat(stringNumber);
                } else {
                    return null;
                }
            }catch(NumberFormatException e){
                //It would cause NumberFormatException when value is not Number.
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class NumberDeserializer implements JsonDeserializer<Number> {

        @Override
        public Number deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String stringNumber = json.getAsString();
            try{
                if ("".equals(stringNumber)) {
                    //Return -2 when value is empty string.
                    return -2;
                }
                if ("int".equals(typeOfT.toString())) {
                    return Integer.parseInt(stringNumber);
                } else if ("float".equals(typeOfT.toString())) {
                    return Float.parseFloat(stringNumber);
                } else {
                    return null;
                }
            }catch(NumberFormatException e){
                //It would cause NumberFormatException when value is not Number.
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class DateDeserializer implements JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String stringDate = json.getAsString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
            try {
                if ("".equals(stringDate)) {
                    return new Date(0);
                }
                return sdf.parse(stringDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private AQI(Builder builder) {
        this.SiteName = builder.SiteName;
        this.County = builder.County;
        this.AQI = builder.AQI;
        this.Pollutant = builder.Pollutant;
        this.Status = builder.Status;
        this.SO2 = builder.SO2;
        this.CO = builder.CO;
        this.CO_8hr = builder.CO_8hr;
        this.O3 = builder.O3;
        this.O3_8hr = builder.O3_8hr;
        this.PM10 = builder.PM10;
        this.PM2_5 = builder.PM2_5;
        this.NO2 = builder.NO2;
        this.NOx = builder.NOx;
        this.NO = builder.NO;
        this.WindDirec = builder.WindDirec;
        this.WindSpeed = builder.WindSpeed;
        this.PublishTime = builder.PublishTime;
        this.PM2_5_AVG = builder.PM2_5_AVG;
        this.PM10_AVG = builder.PM10_AVG;
        this.Latitude = builder.Latitude;
        this.Longitude = builder.Longitude;
    }

    public static class Builder {

        private String SiteName;        //測站名稱
        private String County;          //縣市
        private int AQI = -1;           //空氣品質指標AQI
        private String Pollutant;       //空氣污染指標物
        private String Status;          //狀態
        private float SO2 = -1;         //二氧化硫(ppb)
        private float CO = -1;          //一氧化碳(ppm)
        private float CO_8hr = -1;      //一氧化碳8小時移動平均(ppm)
        private float O3 = -1;          //臭氧(ppb)
        private float O3_8hr = -1;      //臭氧8小時移動平均(ppb)
        private float PM10 = -1;        //懸浮微粒PM10(μg/m3)
        @SerializedName("PM2.5")
        private float PM2_5 = -1;       //細懸浮微粒PM2.5(μg/m3)
        private float NO2 = -1;         //二氧化氮(ppb)
        private float NOx= -1;          //氮氧化物(ppb)
        private float NO = -1;          //一氧化氮(ppb)
        private float WindDirec = -1;   //風向(degrees)
        private float WindSpeed = -1;   //風速(m/sec)
        private Date PublishTime;       //發布時間
        @SerializedName("PM2.5_AVG")
        private float PM2_5_AVG = -1;   //細懸浮微粒PM2.5移動平均值(μg/m3)
        private float PM10_AVG = -1;    //懸浮微粒PM10移動平均值(μg/m3)
        private float Latitude = -1;    //緯度
        private float Longitude = -1;   //經度

        public Builder setSiteName(String siteName) {
            SiteName = siteName;
            return this;
        }

        public Builder setCounty(String county) {
            County = county;
            return this;
        }

        public Builder setAQI(int AQI) {
            this.AQI = AQI;
            return this;
        }

        public Builder setPollutant(String pollutant) {
            Pollutant = pollutant;
            return this;
        }

        public Builder setStatus(String status) {
            Status = status;
            return this;
        }

        public Builder setSO2(float SO2) {
            this.SO2 = SO2;
            return this;
        }

        public Builder setCO(float CO) {
            this.CO = CO;
            return this;
        }

        public Builder setCO_8hr(float CO_8hr) {
            this.CO_8hr = CO_8hr;
            return this;
        }

        public Builder setO3(float o3) {
            O3 = o3;
            return this;
        }

        public Builder setO3_8hr(float o3_8hr) {
            O3_8hr = o3_8hr;
            return this;
        }

        public Builder setPM10(float PM10) {
            this.PM10 = PM10;
            return this;
        }

        public Builder setPM2_5(float PM2_5) {
            this.PM2_5 = PM2_5;
            return this;
        }

        public Builder setNO2(float NO2) {
            this.NO2 = NO2;
            return this;
        }

        public Builder setNOx(float NOx) {
            this.NOx = NOx;
            return this;
        }

        public Builder setNO(float NO) {
            this.NO = NO;
            return this;
        }

        public Builder setWindDirec(float windDirec) {
            WindDirec = windDirec;
            return this;
        }

        public Builder setWindSpeed(float windSpeed) {
            WindSpeed = windSpeed;
            return this;
        }

        public Builder setPublishTime(Date publishTime) {
            PublishTime = publishTime;
            return this;
        }

        public Builder setPM2_5_AVG(float PM2_5_AVG) {
            this.PM2_5_AVG = PM2_5_AVG;
            return this;
        }

        public Builder setPM10_AVG(float PM10_AVG) {
            this.PM10_AVG = PM10_AVG;
            return this;
        }

        public Builder setLatitude(float latitude) {
            Latitude = latitude;
            return this;
        }

        public Builder setLongitude(float longitude) {
            Longitude = longitude;
            return this;
        }

        public AQI build() {
            return new AQI(this);
        }
    }
}