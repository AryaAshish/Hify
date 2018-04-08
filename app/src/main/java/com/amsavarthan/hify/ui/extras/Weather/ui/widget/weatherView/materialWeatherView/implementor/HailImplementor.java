package com.amsavarthan.hify.ui.extras.Weather.ui.widget.weatherView.materialWeatherView.implementor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

import java.util.Random;

/**
 * Hail implementor.
 */

public class HailImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    public static final int TYPE_HAIL_DAY = 1;
    public static final int TYPE_HAIL_NIGHT = 2;
    private static final float INITIAL_ROTATION_3D = 1000;
    private Paint paint;
    private Path path;
    private Hail[] hails;
    private float lastDisplayRate;
    private float lastRotation3D;
    @ColorInt
    private int backgroundColor;

    public HailImplementor(MaterialWeatherView view, @TypeRule int type) {
        this.paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        this.path = new Path();

        int[] colors = new int[3];
        switch (type) {
            case TYPE_HAIL_DAY:
                backgroundColor = Color.rgb(80, 116, 193);
                colors = new int[]{
                        Color.rgb(101, 134, 203),
                        Color.rgb(152, 175, 222),
                        Color.rgb(255, 255, 255),};
                break;

            case TYPE_HAIL_NIGHT:
                backgroundColor = Color.rgb(42, 52, 69);
                colors = new int[]{
                        Color.rgb(64, 67, 85),
                        Color.rgb(127, 131, 154),
                        Color.rgb(255, 255, 255),};
                break;
        }
        float[] scales = new float[]{0.6F, 0.8F, 1};

        this.hails = new Hail[21];
        for (int i = 0; i < hails.length; i++) {
            hails[i] = new Hail(
                    view.getMeasuredWidth(), view.getMeasuredHeight(),
                    colors[i * 3 / hails.length], scales[i * 3 / hails.length]);
        }

        this.lastDisplayRate = 0;
        this.lastRotation3D = INITIAL_ROTATION_3D;
    }

    @ColorInt
    public static int getThemeColor(Context context, @TypeRule int type) {
        switch (type) {
            case TYPE_HAIL_DAY:
                return Color.rgb(80, 116, 193);

            case TYPE_HAIL_NIGHT:
                return Color.rgb(42, 52, 69);
        }
        return ContextCompat.getColor(context, R.color.colorPrimary);
    }

    @Override
    public void updateData(MaterialWeatherView view, float rotation2D, float rotation3D) {
        for (Hail h : hails) {
            h.move(
                    REFRESH_INTERVAL,
                    lastRotation3D == INITIAL_ROTATION_3D ? 0 : rotation3D - lastRotation3D);
        }
        lastRotation3D = rotation3D;
    }

    @Override
    public void draw(MaterialWeatherView view, Canvas canvas,
                     float displayRate, float scrollRate, float rotation2D, float rotation3D) {

        if (displayRate >= 1) {
            canvas.drawColor(backgroundColor);
        } else {
            canvas.drawColor(
                    ColorUtils.setAlphaComponent(
                            backgroundColor,
                            (int) (displayRate * 255)));
        }

        if (scrollRate < 1) {
            canvas.rotate(
                    rotation2D,
                    view.getMeasuredWidth() * 0.5F,
                    view.getMeasuredHeight() * 0.5F);

            for (Hail h : hails) {
                path.reset();
                path.moveTo(h.centerX - h.size, h.centerY);
                path.lineTo(h.centerX, h.centerY - h.size);
                path.lineTo(h.centerX + h.size, h.centerY);
                path.lineTo(h.centerX, h.centerY + h.size);
                path.close();
                paint.setColor(h.color);
                if (displayRate < lastDisplayRate) {
                    paint.setAlpha((int) (displayRate * (1 - scrollRate) * 255));
                } else {
                    paint.setAlpha(255);
                }
                canvas.drawPath(path, paint);
            }
        }

        lastDisplayRate = displayRate;
    }

    @IntDef({TYPE_HAIL_DAY, TYPE_HAIL_NIGHT})
    @interface TypeRule {
    }

    private class Hail {

        float centerX;
        float centerY;
        float size;
        float speed;
        @ColorInt
        int color;
        float scale;
        private float cX;
        private float cY;
        private int viewWidth;
        private int viewHeight;

        private int canvasSize;

        private Hail(int viewWidth, int viewHeight, @ColorInt int color, float scale) {
            this.viewWidth = viewWidth;
            this.viewHeight = viewHeight;

            this.canvasSize = (int) Math.pow(viewWidth * viewWidth + viewHeight * viewHeight, 0.5);

            this.size = (float) (0.0324 * viewWidth);
            this.speed = (float) (viewHeight / 200.0);
            this.color = color;
            this.scale = scale;

            this.init(true);
        }

        private void init(boolean firstTime) {
            Random r = new Random();
            cX = r.nextInt(canvasSize);
            if (firstTime) {
                cY = r.nextInt((int) (canvasSize - size)) - canvasSize;
            } else {
                cY = -size;
            }
            computeCenterPosition();
        }

        private void computeCenterPosition() {
            centerX = (float) (cX - (canvasSize - viewWidth) * 0.5);
            centerY = (float) (cY - (canvasSize - viewHeight) * 0.5);
        }

        void move(long interval, float deltaRotation3D) {
            cY += speed * interval * (Math.pow(scale, 1.5) - 5 * Math.sin(deltaRotation3D * Math.PI / 180.0));
            if (cY - size >= canvasSize) {
                init(false);
            } else {
                computeCenterPosition();
            }
        }
    }
}
