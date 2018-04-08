package com.amsavarthan.hify.ui.extras.Weather.ui.widget.weatherView.materialWeatherView.implementor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;

import com.amsavarthan.hify.ui.extras.Weather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

import java.util.Random;

/**
 * Wind implementor.
 */

public class WindImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private static final float INITIAL_ROTATION_3D = 1000;
    private Paint paint;
    private Wind[] winds;
    private float lastDisplayRate;
    private float lastRotation3D;
    @ColorInt
    private int backgroundColor;

    public WindImplementor(MaterialWeatherView view) {
        this.paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        backgroundColor = Color.rgb(233, 158, 60);
        int[] colors = new int[]{
                Color.rgb(240, 200, 148),
                Color.rgb(237, 178, 100),
                Color.rgb(209, 142, 54),};
        float[] scales = new float[]{0.6F, 0.8F, 1};

        this.winds = new Wind[51];
        for (int i = 0; i < winds.length; i++) {
            winds[i] = new Wind(
                    view.getMeasuredWidth(), view.getMeasuredHeight(),
                    colors[i * 3 / winds.length], scales[i * 3 / winds.length]);
        }

        this.lastDisplayRate = 0;
        this.lastRotation3D = INITIAL_ROTATION_3D;
    }

    @ColorInt
    public static int getThemeColor() {
        return Color.rgb(233, 158, 60);
    }

    @Override
    public void updateData(MaterialWeatherView view, float rotation2D, float rotation3D) {
        for (Wind w : winds) {
            w.move(
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
            rotation2D -= 16;
            canvas.rotate(
                    rotation2D,
                    view.getMeasuredWidth() * 0.5F,
                    view.getMeasuredHeight() * 0.5F);

            for (Wind w : winds) {
                paint.setColor(w.color);
                if (displayRate < lastDisplayRate) {
                    paint.setAlpha((int) (displayRate * (1 - scrollRate) * 255));
                } else {
                    paint.setAlpha((int) ((1 - scrollRate) * 255));
                }
                canvas.drawRect(w.rectF, paint);
            }
        }

        lastDisplayRate = displayRate;
    }

    private class Wind {

        private final float MAX_WIDTH;
        private final float MIN_WIDTH;
        private final float MAX_HEIGHT;
        private final float MIN_HEIGHT;
        float x;
        float y;
        float width;
        float height;
        RectF rectF;
        float speed;
        @ColorInt
        int color;
        float scale;
        private int viewWidth;
        private int viewHeight;
        private int canvasSize;

        private Wind(int viewWidth, int viewHeight, @ColorInt int color, float scale) {
            this.viewWidth = viewWidth;
            this.viewHeight = viewHeight;

            this.canvasSize = (int) Math.pow(viewWidth * viewWidth + viewHeight * viewHeight, 0.5);

            this.rectF = new RectF();
            this.speed = (float) (viewWidth / 100.0);
            this.color = color;
            this.scale = scale;

            this.MAX_HEIGHT = (float) (0.0111 * viewWidth);
            this.MIN_HEIGHT = (float) (0.0093 * viewWidth);
            this.MAX_WIDTH = MAX_HEIGHT * 20;
            this.MIN_WIDTH = MIN_HEIGHT * 15;

            this.init(true);
        }

        private void init(boolean firstTime) {
            Random r = new Random();
            y = r.nextInt(canvasSize);
            if (firstTime) {
                x = r.nextInt((int) (canvasSize - MAX_HEIGHT)) - canvasSize;
            } else {
                x = -MAX_HEIGHT;
            }
            width = MIN_WIDTH + r.nextFloat() * (MAX_WIDTH - MIN_WIDTH);
            height = MIN_HEIGHT + r.nextFloat() * (MAX_HEIGHT - MIN_HEIGHT);

            buildRectF();
        }

        private void buildRectF() {
            float x = (float) (this.x - (canvasSize - viewWidth) * 0.5);
            float y = (float) (this.y - (canvasSize - viewHeight) * 0.5);
            rectF.set(x, y, x + width * scale, y + height * scale);
        }

        void move(long interval, float deltaRotation3D) {
            x += speed * interval
                    * (Math.pow(scale, 1.5)
                    + 5 * Math.sin(deltaRotation3D * Math.PI / 180.0) * Math.cos(16 * Math.PI / 180.0));
            y -= speed * interval
                    * 5 * Math.sin(deltaRotation3D * Math.PI / 180.0) * Math.sin(16 * Math.PI / 180.0);

            if (x >= canvasSize) {
                init(false);
            } else {
                buildRectF();
            }
        }
    }
}
