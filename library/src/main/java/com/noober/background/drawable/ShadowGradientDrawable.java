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
import android.support.annotation.NonNull;

public class ShadowGradientDrawable extends GradientDrawable {

    private static final PorterDuffXfermode CLEAR_XFERMODE =
            new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

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

    // Cached BlurMaskFilter, rebuilt only when shadowRadius changes
    private BlurMaskFilter cachedBlurMaskFilter;
    private float cachedShadowRadius = -1f;

    public void setShadow(float radius, float dx, float dy, int color) {
        if (this.shadowRadius != radius) {
            cachedBlurMaskFilter = null;  // 参数变化时清理旧缓存
            cachedShadowRadius = -1f;
        }
        this.shadowRadius = radius;
        this.shadowDx = dx;
        this.shadowDy = dy;
        this.shadowColor = color;
        clearPaint.setColor(Color.TRANSPARENT);
        clearPaint.setXfermode(CLEAR_XFERMODE);
        shadowPaint.setStyle(Paint.Style.FILL);
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
            int boundsWidth = bounds.right - bounds.left;
            int boundsHeight = bounds.bottom - bounds.top;

            if (boundsWidth <= 0 || boundsHeight <= 0) {
                super.draw(canvas);
                return;
            }

            // 限制 shadowRadius 不超过 bounds 一半
            float effectiveShadowRadius = Math.min(shadowRadius, Math.min(boundsWidth, boundsHeight) / 2f);

            // Inset content by shadow radius on all sides to make room for shadow
            // This keeps content centered regardless of offset
            float contentLeft = bounds.left + effectiveShadowRadius;
            float contentTop = bounds.top + effectiveShadowRadius;
            float contentRight = bounds.right - effectiveShadowRadius;
            float contentBottom = bounds.bottom - effectiveShadowRadius;

            // Calculate shadow rectangle based on offset
            float shadowLeft;
            float shadowTop;
            float shadowRight;
            float shadowBottom;

            // Constrain offsets by effectiveShadowRadius
            float effectiveDx = Math.max(-effectiveShadowRadius, Math.min(shadowDx, effectiveShadowRadius));
            float effectiveDy = Math.max(-effectiveShadowRadius, Math.min(shadowDy, effectiveShadowRadius));

            if (effectiveDx > 0) {
                shadowLeft = contentLeft + effectiveDx;
                shadowRight = contentRight;
            } else {
                shadowLeft = contentLeft;
                shadowRight = contentRight + effectiveDx;
            }

            if (effectiveDy > 0) {
                shadowTop = contentTop + effectiveDy;
                shadowBottom = contentBottom;
            } else {
                shadowTop = contentTop;
                shadowBottom = contentBottom + effectiveDy;
            }

            shadowRect.set(shadowLeft, shadowTop, shadowRight, shadowBottom);

            // Configure shadow paint
            shadowPaint.setColor(shadowColor == 0 ? currentSolidColor : shadowColor);
            // Use BlurMaskFilter like ShapeDrawable for consistent shadow rendering
            // Cache BlurMaskFilter: rebuild only when shadowRadius changes to reduce allocations during frequent redraws
            if (cachedBlurMaskFilter == null || shadowRadius != cachedShadowRadius) {
                // Divide radius to match visual size - same approach as ShapeDrawable
                //除以倍数，因为如果不这么做会导致阴影显示会超过 View 边界，从而导致出现阴影被截断的效果
                float blurRadius;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    blurRadius = shadowRadius / 2f;
                } else {
                    blurRadius = shadowRadius / 3f;
                }
                cachedBlurMaskFilter = new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL);
                cachedShadowRadius = shadowRadius;
                shadowPaint.setMaskFilter(cachedBlurMaskFilter);
            }

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

                rx = Math.max(0, rx);
                ry = Math.max(0, ry);

                if (hasRoundedCorners) {
                    canvas.drawRoundRect(bounds, rx, ry, paint);
                } else {
                    canvas.drawRect(bounds, paint);
                }
                break;
        }
    }
}
