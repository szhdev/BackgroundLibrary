package com.noober.background.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.noober.background.BackgroundFactory;
import com.noober.background.R;

public class BLImageView extends AppCompatImageView {

    private int blShape = -1;
    private float cornerRadius = 0;
    private float[] cornerRadii;
    private boolean needsClipPath = false;
    private Path clipPath;
    private RectF clipRect;

    private float strokeWidth = 0;
    private int strokeColor = 0;
    private float strokeDashWidth = 0;
    private float strokeDashGap = 0;
    private Paint borderPaint;

    private int strokeGradientStartColor = 0;
    private int strokeGradientCenterColor = 0;
    private int strokeGradientEndColor = 0;
    private int strokeGradientAngle = 0;
    private boolean hasStrokeGradient = false;

    public BLImageView(Context context) {
        super(context);
    }

    public BLImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BLImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        BackgroundFactory.setViewBackground(context, attrs, this);
        parseClipAttrs(context, attrs);
        setupClip();
    }

    private void parseClipAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.background);
        try {
            for (int i = 0; i < ta.getIndexCount(); i++) {
                int attr = ta.getIndex(i);
                if (attr == R.styleable.background_bl_shape) {
                    blShape = ta.getInt(attr, -1);
                } else if (attr == R.styleable.background_bl_corners_radius) {
                    cornerRadius = ta.getDimension(attr, 0);
                } else if (attr == R.styleable.background_bl_corners_topLeftRadius) {
                    ensureCornerRadii();
                    float r = ta.getDimension(attr, 0);
                    cornerRadii[0] = r;
                    cornerRadii[1] = r;
                } else if (attr == R.styleable.background_bl_corners_topRightRadius) {
                    ensureCornerRadii();
                    float r = ta.getDimension(attr, 0);
                    cornerRadii[2] = r;
                    cornerRadii[3] = r;
                } else if (attr == R.styleable.background_bl_corners_bottomRightRadius) {
                    ensureCornerRadii();
                    float r = ta.getDimension(attr, 0);
                    cornerRadii[4] = r;
                    cornerRadii[5] = r;
                } else if (attr == R.styleable.background_bl_corners_bottomLeftRadius) {
                    ensureCornerRadii();
                    float r = ta.getDimension(attr, 0);
                    cornerRadii[6] = r;
                    cornerRadii[7] = r;
                } else if (attr == R.styleable.background_bl_stroke_width) {
                    strokeWidth = ta.getDimension(attr, 0);
                } else if (attr == R.styleable.background_bl_stroke_color) {
                    strokeColor = ta.getColor(attr, 0);
                } else if (attr == R.styleable.background_bl_stroke_dashWidth) {
                    strokeDashWidth = ta.getDimension(attr, 0);
                } else if (attr == R.styleable.background_bl_stroke_dashGap) {
                    strokeDashGap = ta.getDimension(attr, 0);
                } else if (attr == R.styleable.background_bl_stroke_gradient_startColor) {
                    strokeGradientStartColor = ta.getColor(attr, 0);
                } else if (attr == R.styleable.background_bl_stroke_gradient_centerColor) {
                    strokeGradientCenterColor = ta.getColor(attr, 0);
                } else if (attr == R.styleable.background_bl_stroke_gradient_endColor) {
                    strokeGradientEndColor = ta.getColor(attr, 0);
                } else if (attr == R.styleable.background_bl_stroke_gradient_angle) {
                    strokeGradientAngle = ta.getInt(attr, 0);
                }
            }
        } finally {
            ta.recycle();
        }

        if (strokeWidth > 0 && strokeColor != 0) {
            borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(strokeWidth);
            borderPaint.setColor(strokeColor);
            if (strokeDashWidth > 0 && strokeDashGap > 0) {
                borderPaint.setPathEffect(new DashPathEffect(
                        new float[]{strokeDashWidth, strokeDashGap}, 0));
                setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
        }

        hasStrokeGradient = strokeGradientStartColor != 0 && strokeGradientEndColor != 0;
        if (hasStrokeGradient && strokeWidth > 0) {
            if (borderPaint == null) {
                borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(strokeWidth);
            }
            if (strokeDashWidth > 0 && strokeDashGap > 0) {
                borderPaint.setPathEffect(new DashPathEffect(
                        new float[]{strokeDashWidth, strokeDashGap}, 0));
                setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
        }
    }

    private void ensureCornerRadii() {
        if (cornerRadii == null) {
            cornerRadii = new float[8];
        }
    }

    private boolean needsClip() {
        if (blShape == GradientDrawable.OVAL) return true;
        if (cornerRadius > 0) return true;
        if (cornerRadii != null) {
            for (float r : cornerRadii) {
                if (r > 0) return true;
            }
        }
        return false;
    }

    private void setupClip() {
        if (!needsClip()) return;

        boolean useOutlineProvider = false;

        // When strokeWidth > 0, must use clipPath to avoid clipToOutline clipping the border
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (cornerRadii == null && strokeWidth <= 0) {
                useOutlineProvider = true;
                setupOutlineClip();
            }
        }

        if (!useOutlineProvider) {
            needsClipPath = true;
            clipPath = new Path();
            clipRect = new RectF();
        }
    }

    private void setupOutlineClip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(true);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        int w = view.getWidth();
                        int h = view.getHeight();
                        if (w <= 0 || h <= 0) return;
                        int inset = (int) strokeWidth / 2;
                        if (blShape == GradientDrawable.OVAL) {
                            outline.setOval(inset, inset, w - inset, h - inset);
                        } else if (cornerRadius > 0) {
                            float adjustedRadius = Math.max(0, cornerRadius - strokeWidth);
                            outline.setRoundRect(inset, inset, w - inset, h - inset, adjustedRadius);
                        } else {
                            outline.setRect(inset, inset, w - inset, h - inset);
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (needsClipPath && w > 0 && h > 0) {
            updateClipPath(w, h);
        }
    }

    private void updateClipPath(int w, int h) {
        clipPath.reset();
        float inset = strokeWidth / 2;
        clipRect.set(inset, inset, w - inset, h - inset);
        if (blShape == GradientDrawable.OVAL) {
            clipPath.addOval(clipRect, Path.Direction.CW);
        } else if (cornerRadii != null) {
            float[] adjustedRadii = new float[8];
            for (int i = 0; i < 8; i++) {
                adjustedRadii[i] = Math.max(0, cornerRadii[i] - inset);
            }
            clipPath.addRoundRect(clipRect, adjustedRadii, Path.Direction.CW);
        } else if (cornerRadius > 0) {
            float adjustedRadius = Math.max(0, cornerRadius - inset);
            clipPath.addRoundRect(clipRect, adjustedRadius, adjustedRadius, Path.Direction.CW);
        } else {
            clipPath.addRect(clipRect, Path.Direction.CW);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needsClipPath && clipPath != null && !clipPath.isEmpty()) {
            canvas.save();
            canvas.clipPath(clipPath);
            super.onDraw(canvas);
            canvas.restore();
        } else {
            super.onDraw(canvas);
        }
        drawBorder(canvas);
    }

    private void drawBorder(Canvas canvas) {
        if (borderPaint == null && !hasStrokeGradient) return;

        if (borderPaint == null) {
            borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(strokeWidth > 0 ? strokeWidth : 2f);
        }

        if (hasStrokeGradient) {
            float halfStroke = strokeWidth / 2f;
            RectF rect = new RectF(halfStroke, halfStroke, getWidth() - halfStroke, getHeight() - halfStroke);

            double angleRad = Math.toRadians(strokeGradientAngle);
            float centerX = rect.centerX();
            float centerY = rect.centerY();
            float halfW = rect.width() / 2f;
            float halfH = rect.height() / 2f;

            float startX = centerX - (float)(Math.cos(angleRad) * halfW);
            float startY = centerY + (float)(Math.sin(angleRad) * halfH);
            float endX = centerX + (float)(Math.cos(angleRad) * halfW);
            float endY = centerY - (float)(Math.sin(angleRad) * halfH);

            int[] colors;
            float[] positions;
            if (strokeGradientCenterColor != 0) {
                colors = new int[]{strokeGradientStartColor, strokeGradientCenterColor, strokeGradientEndColor};
                positions = new float[]{0f, 0.5f, 1f};
            } else {
                colors = new int[]{strokeGradientStartColor, strokeGradientEndColor};
                positions = null;
            }

            LinearGradient shader = new LinearGradient(startX, startY, endX, endY,
                    colors, positions, Shader.TileMode.CLAMP);
            borderPaint.setShader(shader);
        }

        float halfStroke = strokeWidth / 2f;

        if (blShape == GradientDrawable.OVAL) {
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = Math.min(cx, cy) - halfStroke;
            canvas.drawCircle(cx, cy, radius, borderPaint);
        } else {
            RectF borderRect = new RectF(halfStroke, halfStroke,
                    getWidth() - halfStroke, getHeight() - halfStroke);
            if (cornerRadii != null) {
                Path borderPath = new Path();
                borderPath.addRoundRect(borderRect, cornerRadii, Path.Direction.CW);
                canvas.drawPath(borderPath, borderPaint);
            } else if (cornerRadius > 0) {
                canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint);
            } else {
                canvas.drawRect(borderRect, borderPaint);
            }
        }
    }

    /**
     * 代码动态设置圆形裁剪
     */
    public void setClipOval() {
        this.blShape = GradientDrawable.OVAL;
        this.cornerRadius = 0;
        this.cornerRadii = null;
        setupClipDynamic();
    }

    /**
     * 代码动态设置圆角裁剪
     * @param radius 圆角半径（px）
     */
    public void setClipCornerRadius(float radius) {
        this.blShape = GradientDrawable.RECTANGLE;
        this.cornerRadius = radius;
        this.cornerRadii = null;
        setupClipDynamic();
    }

    /**
     * 代码动态设置不规则四角圆角裁剪
     * @param radii 8个值的数组 [topLeftX, topLeftY, topRightX, topRightY, bottomRightX, bottomRightY, bottomLeftX, bottomLeftY]
     */
    public void setClipCornerRadii(float[] radii) {
        this.blShape = GradientDrawable.RECTANGLE;
        this.cornerRadius = 0;
        this.cornerRadii = radii;
        setupClipDynamic();
    }

    /**
     * 动态设置边框
     * @param width 边框宽度（px）
     * @param color 边框颜色
     */
    public void setStroke(float width, int color) {
        this.strokeWidth = width;
        this.strokeColor = color;
        this.strokeDashWidth = 0;
        this.strokeDashGap = 0;
        if (width > 0 && color != 0) {
            if (borderPaint == null) {
                borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                borderPaint.setStyle(Paint.Style.STROKE);
            }
            borderPaint.setStrokeWidth(width);
            borderPaint.setColor(color);
            borderPaint.setPathEffect(null);
        } else {
            borderPaint = null;
        }
        refreshClipArea();
        invalidate();
    }

    /**
     * 动态设置虚线边框
     * @param width 边框宽度（px）
     * @param color 边框颜色
     * @param dashWidth 虚线段宽度（px）
     * @param dashGap 虚线间隙（px）
     */
    public void setStroke(float width, int color, float dashWidth, float dashGap) {
        this.strokeWidth = width;
        this.strokeColor = color;
        this.strokeDashWidth = dashWidth;
        this.strokeDashGap = dashGap;
        if (width > 0 && color != 0) {
            if (borderPaint == null) {
                borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                borderPaint.setStyle(Paint.Style.STROKE);
            }
            borderPaint.setStrokeWidth(width);
            borderPaint.setColor(color);
            if (dashWidth > 0 && dashGap > 0) {
                borderPaint.setPathEffect(new DashPathEffect(
                        new float[]{dashWidth, dashGap}, 0));
                setLayerType(LAYER_TYPE_SOFTWARE, null);
            } else {
                borderPaint.setPathEffect(null);
            }
        } else {
            borderPaint = null;
        }
        refreshClipArea();
        invalidate();
    }

    /**
     * 清除裁剪效果
     */
    public void clearClip() {
        this.blShape = -1;
        this.cornerRadius = 0;
        this.cornerRadii = null;
        this.needsClipPath = false;
        this.clipPath = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(false);
            setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }
        invalidate();
    }

    private void refreshClipArea() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        if (needsClipPath && clipPath != null) {
            updateClipPath(w, h);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            invalidateOutline();
        }
    }

    private void setupClipDynamic() {
        needsClipPath = false;
        boolean useOutlineProvider = false;

        // When strokeWidth > 0, must use clipPath to avoid clipToOutline clipping the border
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (cornerRadii == null && strokeWidth <= 0) {
                useOutlineProvider = true;
                setupOutlineClip();
            } else {
                // Disable clipToOutline when switching to clipPath mode
                setClipToOutline(false);
            }
        }

        if (!useOutlineProvider) {
            needsClipPath = true;
            if (clipPath == null) {
                clipPath = new Path();
            }
            if (clipRect == null) {
                clipRect = new RectF();
            }
            int w = getWidth();
            int h = getHeight();
            if (w > 0 && h > 0) {
                updateClipPath(w, h);
            }
        }
        invalidate();
    }

    /**
     * 动态设置渐变描边
     */
    public void setStrokeGradient(float width, int startColor, int endColor, int angle) {
        setStrokeGradient(width, startColor, 0, endColor, angle);
    }

    public void setStrokeGradient(float width, int startColor, int centerColor, int endColor, int angle) {
        this.strokeWidth = width;
        this.strokeGradientStartColor = startColor;
        this.strokeGradientCenterColor = centerColor;
        this.strokeGradientEndColor = endColor;
        this.strokeGradientAngle = angle;
        this.strokeDashWidth = 0;
        this.strokeDashGap = 0;
        this.hasStrokeGradient = startColor != 0 && endColor != 0;

        if (borderPaint == null) {
            borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setStyle(Paint.Style.STROKE);
        }
        borderPaint.setStrokeWidth(width);
        borderPaint.setPathEffect(null);

        refreshClipArea();
        invalidate();
    }

    /**
     * 动态设置渐变虚线描边
     * @param width 边框宽度（px）
     * @param startColor 渐变起始颜色
     * @param endColor 渐变结束颜色
     * @param angle 渐变角度
     * @param dashWidth 虚线段宽度（px）
     * @param dashGap 虚线间隙（px）
     */
    public void setStrokeGradient(float width, int startColor, int endColor, int angle, float dashWidth, float dashGap) {
        setStrokeGradient(width, startColor, 0, endColor, angle, dashWidth, dashGap);
    }

    /**
     * 动态设置三色渐变虚线描边
     * @param width 边框宽度（px）
     * @param startColor 渐变起始颜色
     * @param centerColor 渐变中间颜色（0表示无中间色）
     * @param endColor 渐变结束颜色
     * @param angle 渐变角度
     * @param dashWidth 虚线段宽度（px）
     * @param dashGap 虚线间隙（px）
     */
    public void setStrokeGradient(float width, int startColor, int centerColor, int endColor, int angle, float dashWidth, float dashGap) {
        this.strokeWidth = width;
        this.strokeGradientStartColor = startColor;
        this.strokeGradientCenterColor = centerColor;
        this.strokeGradientEndColor = endColor;
        this.strokeGradientAngle = angle;
        this.strokeDashWidth = dashWidth;
        this.strokeDashGap = dashGap;
        this.hasStrokeGradient = startColor != 0 && endColor != 0;

        if (borderPaint == null) {
            borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setStyle(Paint.Style.STROKE);
        }
        borderPaint.setStrokeWidth(width);

        if (dashWidth > 0 && dashGap > 0) {
            borderPaint.setPathEffect(new DashPathEffect(new float[]{dashWidth, dashGap}, 0));
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        } else {
            borderPaint.setPathEffect(null);
        }

        refreshClipArea();
        invalidate();
    }
}
