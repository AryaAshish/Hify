package com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather;

import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.result.NewRealtimeResult;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.weather.WeatherEntity;

/**
 * Base.
 */

public class Base {

    public String cityId;
    public String city;
    public String date;
    public String time;
    public long timeStamp;

    Base() {
    }

/*
    void buildBase(FWResult result) {
        cityId = "CN" + result.cityid;
        city = result.city;
        date = result.realtime.time.split(" ")[0];
        time = result.realtime.time.split(" ")[1].split(":")[0]
                + ":" + result.realtime.time.split(" ")[1].split(":")[1];
    }

    void buildBase(HefengResult result, int p) {
        cityId = result.heWeather.get(p).basic.id;
        city = result.heWeather.get(p).basic.city;
        date = result.heWeather.get(p).basic.updateRotation.loc.split(" ")[0];
        time = result.heWeather.get(p).basic.updateRotation.loc.split(" ")[1];
    }
*/

    public void buildBase(Location location, NewRealtimeResult result) {
        cityId = location.cityId;
        city = location.city;
        date = result.LocalObservationDateTime.split("T")[0];
        time = result.LocalObservationDateTime.split("T")[1].split(":")[0]
                + ":" + result.LocalObservationDateTime.split("T")[1].split(":")[1];
        timeStamp = System.currentTimeMillis();
    }

    void buildBase(WeatherEntity entity) {
        cityId = entity.cityId;
        city = entity.city;
        date = entity.date;
        time = entity.time;
        timeStamp = entity.timeStamp;
    }
}
