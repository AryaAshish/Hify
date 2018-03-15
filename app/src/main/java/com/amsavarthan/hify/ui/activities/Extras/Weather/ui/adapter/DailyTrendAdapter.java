package com.amsavarthan.hify.ui.activities.Extras.Weather.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.WeatherExtra;
import com.amsavarthan.hify.ui.activities.Extras.Weather.basic.GeoActivity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.History;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Daily;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.activities.Extras.Weather.ui.dialog.WeatherDialog;
import com.amsavarthan.hify.ui.activities.Extras.Weather.ui.widget.trendView.TrendItemView;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.helpter.WeatherHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;


/**
 * Daily trend adapter.
 */

public class DailyTrendAdapter extends RecyclerView.Adapter<DailyTrendAdapter.ViewHolder> {

    private Context context;

    private Weather weather;

    private float[] maxiTemps;
    private float[] miniTemps;
    private int highestTemp;
    private int lowestTemp;

    private int[] themeColors;

    public DailyTrendAdapter(Context context, @NonNull Weather weather, @Nullable History history,
                             int[] themeColors) {
        this.context = context;
        this.weather = weather;

        this.maxiTemps = new float[weather.dailyList.size() * 2 - 1];
        for (int i = 0; i < maxiTemps.length; i += 2) {
            maxiTemps[i] = weather.dailyList.get(i / 2).temps[0];
        }
        for (int i = 1; i < maxiTemps.length; i += 2) {
            maxiTemps[i] = (maxiTemps[i - 1] + maxiTemps[i + 1]) * 0.5F;
        }

        this.miniTemps = new float[weather.dailyList.size() * 2 - 1];
        for (int i = 0; i < miniTemps.length; i += 2) {
            miniTemps[i] = weather.dailyList.get(i / 2).temps[1];
        }
        for (int i = 1; i < miniTemps.length; i += 2) {
            miniTemps[i] = (miniTemps[i - 1] + miniTemps[i + 1]) * 0.5F;
        }

        highestTemp = history == null ? Integer.MIN_VALUE : history.maxiTemp;
        lowestTemp = history == null ? Integer.MAX_VALUE : history.miniTemp;
        for (int i = 0; i < weather.dailyList.size(); i++) {
            if (weather.dailyList.get(i).temps[0] > highestTemp) {
                highestTemp = weather.dailyList.get(i).temps[0];
            }
            if (weather.dailyList.get(i).temps[1] < lowestTemp) {
                lowestTemp = weather.dailyList.get(i).temps[1];
            }
        }

        this.themeColors = themeColors;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend_daily, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.onBindView(context, position);
    }

    @Override
    public int getItemCount() {
        return weather.dailyList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView weekText;
        private TextView dateText;
        private ImageView dayIcon;
        private ImageView nightIcon;
        private TrendItemView trendItemView;

        ViewHolder(View itemView) {
            super(itemView);

            this.weekText = itemView.findViewById(R.id.item_trend_daily_weekTxt);
            this.dateText = itemView.findViewById(R.id.item_trend_daily_dateTxt);
            this.dayIcon = itemView.findViewById(R.id.item_trend_daily_icon_day);
            this.nightIcon = itemView.findViewById(R.id.item_trend_daily_icon_night);
            this.trendItemView = itemView.findViewById(R.id.item_trend_daily_trendItem);

            itemView.findViewById(R.id.item_trend_daily).setOnClickListener(this);
        }

        @SuppressLint("SetTextI18n")
        void onBindView(Context context, int position) {
            Daily daily = weather.dailyList.get(position);

            weekText.setText(daily.week);
            dateText.setText(daily.getDateInFormat(context.getString(R.string.date_format_short)));
            Glide.with(context)
                    .load(WeatherHelper.getWeatherIcon(daily.weatherKinds[0], true)[3])
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .into(dayIcon);
            Glide.with(context)
                    .load(WeatherHelper.getWeatherIcon(daily.weatherKinds[1], false)[3])
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .into(nightIcon);
            trendItemView.setData(
                    buildTempArrayForItem(maxiTemps, position),
                    buildTempArrayForItem(miniTemps, position),
                    Math.max(daily.precipitations[0], daily.precipitations[1]),
                    highestTemp,
                    lowestTemp);
            trendItemView.setLineColors(themeColors[1], themeColors[2]);
        }

        private float[] buildTempArrayForItem(float[] temps, int adapterPosition) {
            float[] a = new float[3];
            a[1] = temps[2 * adapterPosition];
            if (2 * adapterPosition - 1 < 0) {
                a[0] = TrendItemView.NONEXISTENT_VALUE;
            } else {
                a[0] = temps[2 * adapterPosition - 1];
            }
            if (2 * adapterPosition + 1 >= temps.length) {
                a[2] = TrendItemView.NONEXISTENT_VALUE;
            } else {
                a[2] = temps[2 * adapterPosition + 1];
            }
            return a;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.item_trend_daily:
                    GeoActivity activity = WeatherExtra.getInstance().getTopActivity();
                    if (activity != null) {
                        WeatherDialog weatherDialog = new WeatherDialog();
                        weatherDialog.setData(weather, getAdapterPosition(), true);
                        weatherDialog.show(activity.getFragmentManager(), null);
                    }
                    break;
            }
        }
    }
}