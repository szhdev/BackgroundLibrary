package com.noober.background.drawable;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;

import android.support.annotation.NonNull;

/**
 * 支持渐变描边的 GradientDrawable 扩展。
 * 在原有 GradientDrawable 基础上，通过 LinearGradient Shader 绘制渐变色边框。
 */
public class BLShapeDrawable extends GradientDrawable {

    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF strokeRect = new RectF();

    private int strokeGradientStartColor;
    private int strokeGradientCenterColor;  // 0 表示不使用中间色
    private int strokeGradientEndColor;
    private int strokeGradientAngle;  // 渐变角度，0=左到右，90=下到上，与 GradientDrawable 规范一致
    private float blStrokeWidth;

    private float strokeDashWidth;
    private float strokeDashGap;

    private float blCornerRadius;
    private float[] blCornerRadii;  // 八个值 [topLeftX, topLeftY, topRightX, topRightY, ...]
    private int blShape = RECTANGLE;

    // Shader 缓存
    private LinearGradient cachedShader;
    private int cachedWidth = -1;
    private int cachedHeight = -1;

    /**
     * 设置渐变描边参数
     */
    public void setStrokeGradient(float width, int startColor, int centerColor, int endColor, int angle) {
        this.blStrokeWidth = width;
        this.strokeGradientStartColor = startColor;
        this.strokeGradientCenterColor = centerColor;
        this.strokeGradientEndColor = endColor;
        this.strokeGradientAngle = angle;
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(width);
        invalidateSelf();
    }

    /**
     * 设置虚线效果
     */
    public void setStrokeDash(float dashWidth, float dashGap) {
        this.strokeDashWidth = dashWidth;
        this.strokeDashGap = dashGap;
        if (dashWidth > 0 && dashGap > 0) {
            strokePaint.setPathEffect(new DashPathEffect(
                    new float[]{dashWidth, dashGap}, 0));
        } else {
            strokePaint.setPathEffect(null);
        }
        invalidateSelf();
    }

    public void setBlCornerRadius(float radius) {
        this.blCornerRadius = radius;
    }

    public void setBlCornerRadii(float[] radii) {
        this.blCornerRadii = radii;
    }

    public void setBlShape(int shape) {
        this.blShape = shape;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        if (blStrokeWidth > 0 && strokeGradientStartColor != 0 && strokeGradientEndColor != 0) {
            drawGradientStroke(canvas);
        }
    }

    private void drawGradientStroke(Canvas canvas) {
        Rect bounds = getBounds();
        int width = bounds.width();
        int height = bounds.height();

        if (width <= 0 || height <= 0) return;

        // 更新 Shader 缓存
        if (cachedShader == null || width != cachedWidth || height != cachedHeight) {
            cachedShader = createGradientShader(bounds);
            cachedWidth = width;
            cachedHeight = height;
            strokePaint.setShader(cachedShader);
        }

        // 描边路径内缩 strokeWidth/2
        float inset = blStrokeWidth / 2f;
        strokeRect.set(bounds.left + inset, bounds.top + inset,
                bounds.right - inset, bounds.bottom - inset);

        switch (blShape) {
            case OVAL:
                canvas.drawOval(strokeRect, strokePaint);
                break;
            case RECTANGLE:
            default:
                if (blCornerRadii != null) {
                    // 使用 Path 绘制不规则圆角
                    Path path = new Path();
                    path.addRoundRect(strokeRect, blCornerRadii, Path.Direction.CW);
                    canvas.drawPath(path, strokePaint);
                } else if (blCornerRadius > 0) {
                    float rx = Math.max(0, blCornerRadius);
                    canvas.drawRoundRect(strokeRect, rx, rx, strokePaint);
                } else {
                    canvas.drawRect(strokeRect, strokePaint);
                }
                break;
        }
    }

    private LinearGradient createGradientShader(Rect bounds) {
        // 根据 angle 计算渐变方向坐标
        // angle 规范：0=左到右, 45=左下到右上, 90=下到上, 135=右下到左上,
        //            180=右到左, 225=右上到左下, 270=上到下, 315=左上到右下
        float centerX = bounds.exactCenterX();
        float centerY = bounds.exactCenterY();
        float halfWidth = bounds.width() / 2f;
        float halfHeight = bounds.height() / 2f;

        double angleRad = Math.toRadians(strokeGradientAngle);
        float x0 = (float) (centerX - halfWidth * Math.cos(angleRad));
        float y0 = (float) (centerY + halfHeight * Math.sin(angleRad));
        float x1 = (float) (centerX + halfWidth * Math.cos(angleRad));
        float y1 = (float) (centerY - halfHeight * Math.sin(angleRad));

        if (strokeGradientCenterColor != 0) {
            return new LinearGradient(x0, y0, x1, y1,
                    new int[]{strokeGradientStartColor, strokeGradientCenterColor, strokeGradientEndColor},
                    new float[]{0f, 0.5f, 1f},
                    Shader.TileMode.CLAMP);
        } else {
            return new LinearGradient(x0, y0, x1, y1,
                    strokeGradientStartColor, strokeGradientEndColor,
                    Shader.TileMode.CLAMP);
        }
    }
}
