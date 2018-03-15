package com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather;

import android.content.Context;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.result.NewHourlyResult;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.table.weather.HourlyEntity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.helpter.WeatherHelper;


/**
 * Hourly.
 */

public class Hourly {

    public String time;
    public boolean dayTime;
    public String weather;
    public String weatherKind;
    public int temp;
    public int precipitation;

    public Hourly() {
    }

/*
    Hourly buildHourly(Calendar c, Daily daily, FWResult.WeatherDetailsInfo.Weather24HoursDetailsInfos info) {
        time = info
                .startTime.split(" ")[1].split(":")[0] + "时";
        int hour = c.get(Calendar.HOUR_OF_DAY);
        dayTime = (Integer.parseInt(daily.astros[0].split(":")[0]) <= hour)
                || (hour <= Integer.parseInt(daily.astros[1].split(":")[0]));
        weather = info.weather;
        weatherKind = WeatherHelper.getFWeatherKind(weather);
        temp = Integer.parseInt(info.highestTemperature);
        precipitation = WeatherHelper.getPrecipitation(Integer.parseInt(info.precipitation));
        return this;
    }
    
    Hourly buildHourly(Calendar c, Daily daily, HefengResult.HeWeather.HourlyForecast hourly) {
        if (Integer.parseInt(hourly.pop) > 5) {
            if (Integer.parseInt(hourly.tmp) > 1) {
                weather = "Rain";
                weatherKind = WeatherHelper.getHefengWeatherKind("300");
            } else if (Integer.parseInt(hourly.tmp) > -2) {
                weather = "Sleet";
                weatherKind = WeatherHelper.getHefengWeatherKind("313");
            } else {
                weather = "Snow";
                weatherKind = WeatherHelper.getHefengWeatherKind("400");
            }
        } else {
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if ((Integer.parseInt(daily.astros[0].split(":")[0]) <= hour)
                    || (hour <= Integer.parseInt(daily.astros[1].split(":")[0]))) {
                dayTime = true;
                weather = daily.weathers[0];
                weatherKind = WeatherHelper.getHefengWeatherKind(daily.weatherKinds[0]);
            } else {
                dayTime = false;
                weather = daily.weathers[1];
                weatherKind = WeatherHelper.getHefengWeatherKind(daily.weatherKinds[1]);
            }
        }
        time = hourly.date.split(" ")[1].split(":")[0];
        temp = Integer.parseInt(hourly.tmp);
        precipitation = Integer.parseInt(hourly.pop);
        return this;
    }
*/

    public Hourly buildHourly(Context c, NewHourlyResult result) {
        time = result.DateTime.split("T")[1].split(":")[0] + c.getString(R.string.of_clock);
        dayTime = result.IsDaylight;
        weather = result.IconPhrase;
        weatherKind = WeatherHelper.getNewWeatherKind(result.WeatherIcon);
        temp = (int) result.Temperature.Value;
        precipitation = result.PrecipitationProbability;
        return this;
    }

    Hourly buildHourly(HourlyEntity entity) {
        time = entity.time;
        dayTime = entity.dayTime;
        weather = entity.weather;
        weatherKind = entity.weatherKind;
        temp = entity.temp;
        precipitation = entity.precipitation;
        return this;
    }
}
