package com.amsavarthan.hify.ui.activities.Extras.Weather.utils.helpter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.History;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.DaoMaster;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.HistoryEntity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.LocationEntity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.weather.AlarmEntity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.weather.AlarmEntityDao;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.weather.DailyEntity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.weather.DailyEntityDao;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.weather.HourlyEntity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.weather.HourlyEntityDao;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.weather.WeatherEntity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.weather.WeatherEntityDao;

import org.greenrobot.greendao.database.Database;

import java.util.List;


/**
 * Database helper
 */

public class DatabaseHelper {

    private final static String DATABASE_NAME = "Hify_Weather_db";
    private static DatabaseHelper instance;
    private GeoWeatherOpenHelper helper;

    private DatabaseHelper(Context c) {
        helper = new GeoWeatherOpenHelper(c, DATABASE_NAME, null);
    }

    public static DatabaseHelper getInstance(Context c) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                instance = new DatabaseHelper(c);
            }
        }
        return instance;
    }

    private SQLiteDatabase getDatabase() {
        return helper.getWritableDatabase();
    }

    public void writeLocation(Location location) {
        LocationEntity.insertLocation(getDatabase(), location);
    }

    // location.

    public void writeLocationList(List<Location> list) {
        LocationEntity.writeLocationList(getDatabase(), list);
    }

    public void deleteLocation(Location location) {
        LocationEntity.deleteLocation(getDatabase(), location);
    }

    public List<Location> readLocationList() {
        return LocationEntity.readLocationList(getDatabase());
    }

    public void writeHistory(Weather weather) {
        HistoryEntity.insertHistory(getDatabase(), weather);
    }

    // history.

    public History readHistory(Weather weather) {
        return HistoryEntity.searchYesterdayHistory(getDatabase(), weather);
    }

    public void writeWeather(Location location, Weather weather) {
        WeatherEntity.insertWeather(getDatabase(), location, weather);
        DailyEntity.insertDailyList(getDatabase(), location, weather);
        HourlyEntity.insertDailyList(getDatabase(), location, weather);
        AlarmEntity.insertAlarmList(getDatabase(), location, weather);
    }

    // weather.

    public Weather readWeather(Location location) {
        Weather weather = WeatherEntity.searchWeather(getDatabase(), location);
        if (weather != null) {
            weather
                    .buildWeatherDailyList(DailyEntity.searchLocationDailyEntity(getDatabase(), location))
                    .buildWeatherHourlyList(HourlyEntity.searchLocationHourlyEntity(getDatabase(), location))
                    .buildWeatherAlarmList(AlarmEntity.searchLocationAlarmEntity(getDatabase(), location));
        }
        return weather;
    }

    private class GeoWeatherOpenHelper extends DaoMaster.DevOpenHelper {

        GeoWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            if (newVersion >= 22) {
                if (oldVersion < 23) {
                    WeatherEntityDao.dropTable(db, true);
                    DailyEntityDao.dropTable(db, true);
                    HourlyEntityDao.dropTable(db, true);
                    AlarmEntityDao.dropTable(db, true);

                    WeatherEntityDao.createTable(db, true);
                    DailyEntityDao.createTable(db, true);
                    HourlyEntityDao.createTable(db, true);
                    AlarmEntityDao.createTable(db, true);
                }
            } else {
                super.onUpgrade(db, oldVersion, newVersion);
            }
        }
    }
/*
    // city.

    public void writeCityList(CityListResult result) {
        CityEntity.insertCityList(getDatabase(), result);
    }

    public boolean isNeedWriteCityList() {
        return CityEntity.isNeedWriteData(getDatabase());
    }

    public List<Location> readCityList() {
        return CityEntity.readCityLocation(getDatabase());
    }

    String[] searchCityId(String district, String city, String province) {
        List<Location> locationList = CityEntity.accurateSearchCity(getDatabase(), district);
        if (locationList.size() == 1) {
            return new String[] {
                    locationList.get(0).cityId,
                    district};
        } else if (locationList.size() == 0) {
            locationList = CityEntity.accurateSearchCity(getDatabase(), city);
            if (locationList.size() == 0) {
                return new String[] {Location.NULL_ID, ""};
            } else {
                return new String[] {
                        locationList.get(0).cityId,
                        city};
            }
        } else {
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).prov.equals(province.replace("省", ""))) {
                    return new String[] {
                            locationList.get(i).cityId,
                            city
                    };
                }
            }
            return new String[] {Location.NULL_ID, ""};
        }
    }

    public List<Location> fuzzySearchCityList(String txt) {
        return CityEntity.fuzzySearchCity(getDatabase(), txt);
    }

    // oversea city.

    public void writeOverseaCityList(OverseaCityListResult result) {
        OverseaCityEntity.insertOverseaCityList(getDatabase(), result);
    }

    public boolean isNeedWriteOverseaCityList() {
        return OverseaCityEntity.isNeedWriteData(getDatabase());
    }

    public List<Location> readOverseaCityList() {
        return OverseaCityEntity.readOverseaCityLocation(getDatabase());
    }

    String[] searchOverseaCityId(String city) {
        List<Location> locationList = OverseaCityEntity.accurateSearchOverseaCity(getDatabase(), city);
        if (locationList.size() > 0) {
            return new String[] {
                    locationList.get(0).cityId,
                    city};
        } else {
            return new String[] {Location.NULL_ID, ""};
        }
    }

    public List<Location> fuzzySearchOverseaCityList(String txt) {
        return OverseaCityEntity.fuzzySearchOverseaCity(getDatabase(), txt);
    }
*/
}
