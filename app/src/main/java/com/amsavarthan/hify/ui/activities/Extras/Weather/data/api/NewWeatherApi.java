package com.amsavarthan.hify.ui.activities.Extras.Weather.data.api;

import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.result.NewAlertResult;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.result.NewAqiResult;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.result.NewDailyResult;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.result.NewHourlyResult;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.result.NewLocationResult;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.result.NewRealtimeResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Accu api.
 */

public interface NewWeatherApi {

    @GET("locations/v1/cities/search.json")
    Call<List<NewLocationResult>> getWeatherLocation(@Query("alias") String alias,
                                                     @Query("apikey") String apikey,
                                                     @Query("q") String q,
                                                     @Query("language") String language);

    @GET("locations/v1/cities/geoposition/search.json")
    Call<NewLocationResult> getWeatherLocationByGeoPosition(@Query("alias") String alias,
                                                            @Query("apikey") String apikey,
                                                            @Query("q") String q,
                                                            @Query("language") String language);

    @GET("currentconditions/v1/{city_key}.json")
    Call<List<NewRealtimeResult>> getNewRealtime(@Path("city_key") String city_key,
                                                 @Query("apikey") String apikey,
                                                 @Query("language") String language,
                                                 @Query("details") boolean details);

    @GET("forecasts/v1/daily/15day/{city_key}.json")
    Call<NewDailyResult> getNewDaily(@Path("city_key") String city_key,
                                     @Query("apikey") String apikey,
                                     @Query("language") String language,
                                     @Query("metric") boolean metric,
                                     @Query("details") boolean details);

    @GET("forecasts/v1/hourly/24hour/{city_key}.json")
    Call<List<NewHourlyResult>> getNewHourly(@Path("city_key") String city_key,
                                             @Query("apikey") String apikey,
                                             @Query("language") String language,
                                             @Query("metric") boolean metric);

    @GET("alerts/v1/{city_key}.json")
    Call<List<NewAlertResult>> getNewAlert(@Path("city_key") String city_key,
                                           @Query("apikey") String apikey,
                                           @Query("language") String language,
                                           @Query("details") boolean details);

    @GET("airquality/v1/observations/{city_key}.json")
    Call<NewAqiResult> getNewAqi(@Path("city_key") String city_key,
                                 @Query("apikey") String apikey);
}
