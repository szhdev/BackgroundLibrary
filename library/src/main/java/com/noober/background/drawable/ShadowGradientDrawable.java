package com.noober.background.drawable;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;

public class ShadowGradientDrawable extends GradientDrawable {

    private float shadowRadius = 0;
    private float shadowDx = 0;
    private float shadowDy = 0;
    private int shadowColor = 0;
    private int currentSolidColor = 0;
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void setShadow(float radius, float dx, float dy, int color) {
        this.shadowRadius = radius;
        this.shadowDx = dx;
        this.shadowDy = dy;
        this.shadowColor = color;
    }

    @Override
    public void setColor(int color) {
        super.setColor(color);
        this.currentSolidColor = color;
    }

    @Override
    public void setColor(ColorStateList color) {
        super.setColor(color);
        // For state list, use default color for shadow
        if (color != null && color.getDefaultColor() != 0) {
            this.currentSolidColor = color.getDefaultColor();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (shadowRadius > 0) {
            shadowPaint.setColor(currentSolidColor);
            shadowPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
            Rect bounds = getBounds();
            if (getCornerRadius() > 0) {
                float[] radii = getCornerRadii();
                if (radii != null) {
                    canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom,
                            radii[0], radii[1], shadowPaint);
                } else {
                    canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom,
                            getCornerRadius(), getCornerRadius(), shadowPaint);
                }
            } else {
                canvas.drawRect(bounds, shadowPaint);
            }
        }
        super.draw(canvas);
    }
}
