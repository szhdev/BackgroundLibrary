package com.noober.background.drawable;

import android.content.res.ColorStateList;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

import androidx.annotation.NonNull;

public class ShadowGradientDrawable extends GradientDrawable {

    private float shadowRadius = 0;
    private float shadowDx = 0;
    private float shadowDy = 0;
    private int shadowColor = 0;
    private int currentSolidColor = 0;
    private float[] cornerRadii;
    private float cornerRadius;
    private int shape = GradientDrawable.RECTANGLE;
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Reusable objects to avoid allocations in draw()
    private final RectF shadowRect = new RectF();
    private final RectF contentRect = new RectF();
    private final Rect originalBounds = new Rect();
    private final Rect insetBounds = new Rect();

    // Cached BlurMaskFilter, rebuilt only when blurRadius changes
    private BlurMaskFilter cachedBlurMaskFilter;
    private float cachedBlurRadius = -1f;

    public void setShadow(float radius, float dx, float dy, int color) {
        this.shadowRadius = radius;
        this.shadowDx = dx;
        this.shadowDy = dy;
        this.shadowColor = color;
        clearPaint.setColor(Color.TRANSPARENT);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    /**
     * Returns the amount by which this drawable insets its content on each side to make
     * room for the blurred shadow. Callers (e.g. {@code BackgroundFactory}) can use this
     * value as authoritative shadow spread instead of re-parsing TypedArray attributes.
     */
    public float getShadowInset() {
        return shadowRadius;
    }

    @Override
    public void setShape(int shape) {
        super.setShape(shape);
        this.shape = shape;
    }

    @Override
    public void setCornerRadius(float radius) {
        super.setCornerRadius(radius);
        this.cornerRadius = radius;
    }

    @Override
    public void setCornerRadii(float[] radii) {
        super.setCornerRadii(radii);
        this.cornerRadii = radii;
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
    public void setColors(int[] colors) {
        super.setColors(colors);
        // When we have a gradient, interior is filled - don't clear it
        // Use any non-zero value to prevent clearing logic from triggering
        this.currentSolidColor = 0xFF000000; // Non-zero alpha prevents clearing
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (shadowRadius > 0) {
            Rect bounds = getBounds();

            // Inset content by shadow radius on all sides to make room for shadow
            // This keeps content centered regardless of offset
            float contentLeft = bounds.left + shadowRadius;
            float contentTop = bounds.top + shadowRadius;
            float contentRight = bounds.right - shadowRadius;
            float contentBottom = bounds.bottom - shadowRadius;

            // Calculate shadow rectangle based on offset
            float shadowLeft;
            float shadowTop;
            float shadowRight;
            float shadowBottom;

            if (shadowDx > 0) {
                shadowLeft = contentLeft + shadowDx;
                shadowRight = contentRight;
            } else {
                shadowLeft = contentLeft;
                shadowRight = contentRight + shadowDx;
            }

            if (shadowDy > 0) {
                shadowTop = contentTop + shadowDy;
                shadowBottom = contentBottom;
            } else {
                shadowTop = contentTop;
                shadowBottom = contentBottom + shadowDy;
            }

            shadowRect.set(shadowLeft, shadowTop, shadowRight, shadowBottom);

            // Configure shadow paint
            // Fallback to current solid color when shadowColor is not explicitly set,
            // keeping behavior consistent with library (support) module.
            shadowPaint.setColor(shadowColor == 0 ? currentSolidColor : shadowColor);
            // Use BlurMaskFilter like ShapeDrawable for consistent shadow rendering
            float blurRadius;
            // Divide radius to match visual size - same approach as ShapeDrawable
            //除以倍数，因为如果不这么做会导致阴影显示会超过 View 边界，从而导致出现阴影被截断的效果
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                blurRadius = shadowRadius / 2f;
            } else {
                blurRadius = shadowRadius / 3f;
            }
            // Cache BlurMaskFilter: rebuild only when blur radius changes to reduce allocations during frequent redraws
            if (cachedBlurMaskFilter == null || blurRadius != cachedBlurRadius) {
                cachedBlurMaskFilter = new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL);
                cachedBlurRadius = blurRadius;
                shadowPaint.setMaskFilter(cachedBlurMaskFilter);
            }
            shadowPaint.setStyle(Paint.Style.FILL);

            canvas.save();

            // Draw shadow first based on shape type
            drawShape(canvas, shadowRect, shadowPaint);

            // If there's no solid color (or solid color is transparent), clear the interior after drawing shadow
            // This removes the shadow color from the interior making it transparent
            // Only the outer blurred shadow will remain visible around the border
            if (currentSolidColor == 0 || Color.alpha(currentSolidColor) == 0) {
                contentRect.set(contentLeft, contentTop, contentRight, contentBottom);
                drawShape(canvas, contentRect, clearPaint);
            }

            // Draw content on top with insetted bounds to make room for shadow
            // We need to change the bounds temporarily because GradientDrawable uses getBounds()
            originalBounds.set(getBounds());
            insetBounds.set(
                (int) Math.ceil(contentLeft),
                (int) Math.ceil(contentTop),
                (int) Math.floor(contentRight),
                (int) Math.floor(contentBottom)
            );
            setBounds(insetBounds);

            super.draw(canvas);
            setBounds(originalBounds);

            canvas.restore();
        } else {
            super.draw(canvas);
        }
    }

    /**
     * Draw the shape (shadow or clear interior) based on current shape type and corner radii
     */
    private void drawShape(@NonNull Canvas canvas, @NonNull RectF bounds, @NonNull Paint paint) {
        switch (shape) {
            case GradientDrawable.OVAL:
            case GradientDrawable.RING:
                canvas.drawOval(bounds, paint);
                break;
            case GradientDrawable.LINE:
                canvas.drawRect(bounds, paint);
                break;
            default:
            case GradientDrawable.RECTANGLE:
                boolean hasRoundedCorners = false;
                float rx = 0;
                float ry = 0;

                // Check for individual corner radii
                if (cornerRadii != null) {
                    // Use maximum radius across all corners for shadow approximation
                    // This handles the case where some corners are rounded and others are square
                    for (float r : cornerRadii) {
                        if (r > rx) {
                            rx = r;
                        }
                    }
                    ry = rx;
                    hasRoundedCorners = rx > 0;
                }

                // If no individual radii, check for uniform radius
                if (!hasRoundedCorners && cornerRadius > 0) {
                    rx = cornerRadius;
                    ry = cornerRadius;
                    hasRoundedCorners = true;
                }

                if (hasRoundedCorners) {
                    canvas.drawRoundRect(bounds, rx, ry, paint);
                } else {
                    canvas.drawRect(bounds, paint);
                }
                break;
        }
    }
}