package com.noober.background.drawable;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;


import com.noober.background.R;

import org.xmlpull.v1.XmlPullParserException;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT;

public class GradientDrawableCreator implements ICreateDrawable {

    private TypedArray typedArray;

    GradientDrawableCreator(TypedArray typedArray) {
        this.typedArray = typedArray;
    }

    private int gradientState = -1;

    GradientDrawableCreator(TypedArray typedArray, @AttrRes int gradientState) {
        this.typedArray = typedArray;
        this.gradientState = gradientState;
    }

    @Override
    public GradientDrawable create() throws XmlPullParserException {
        // Parse shadow attributes first to know which type we need
        float shadowSize = 0;
        int shadowColor = 0;
        float shadowOffsetX = 0;
        float shadowOffsetY = 0;

        float[] cornerRadius = new float[8];
        float sizeWidth = 0;
        float sizeHeight = 0;
        float strokeWidth = -1;
        float strokeDashWidth = 0;
        int strokeColor = 0;
        int solidColor = 0;
        float strokeGap = 0;
        float centerX = 0;
        float centerY = 0;
        int centerColor = 0;
        int startColor = 0;
        int endColor = 0;
        int gradientType = LINEAR_GRADIENT;
        int gradientAngle = 0;
        float gradientRadius = 0;
        boolean useLevel = false;
        int shape = 0;
        Rect padding = new Rect();
        ColorStateList colorStateList = null;
        ColorStateList strokeColorStateList = null;
        int strokeGradientStartColor = 0;
        int strokeGradientCenterColor = 0;
        int strokeGradientEndColor = 0;
        int strokeGradientAngle = 0;

        // First pass: collect all attribute values
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.background_bl_shape) {
                shape = typedArray.getInt(attr, 0);
            } else if (attr == R.styleable.background_bl_solid_color) {
                solidColor = typedArray.getColor(attr, 0);
            } else if (attr == R.styleable.background_bl_corners_radius) {
                // radius will be set later
                float radius = typedArray.getDimension(attr, 0);
                for (int j = 0; j < 8; j += 2) {
                    cornerRadius[j] = radius;
                    cornerRadius[j + 1] = radius;
                }
            } else if (attr == R.styleable.background_bl_corners_bottomLeftRadius) {
                cornerRadius[6] = typedArray.getDimension(attr, 0);
                cornerRadius[7] = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_corners_bottomRightRadius) {
                cornerRadius[4] = typedArray.getDimension(attr, 0);
                cornerRadius[5] = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_corners_topLeftRadius) {
                cornerRadius[0] = typedArray.getDimension(attr, 0);
                cornerRadius[1] = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_corners_topRightRadius) {
                cornerRadius[2] = typedArray.getDimension(attr, 0);
                cornerRadius[3] = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_corners_leftRadius) {
                cornerRadius[0] = typedArray.getDimension(attr, 0);
                cornerRadius[1] = typedArray.getDimension(attr, 0);
                cornerRadius[6] = typedArray.getDimension(attr, 0);
                cornerRadius[7] = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_corners_topRadius) {
                cornerRadius[0] = typedArray.getDimension(attr, 0);
                cornerRadius[1] = typedArray.getDimension(attr, 0);
                cornerRadius[2] = typedArray.getDimension(attr, 0);
                cornerRadius[3] = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_corners_rightRadius) {
                cornerRadius[2] = typedArray.getDimension(attr, 0);
                cornerRadius[3] = typedArray.getDimension(attr, 0);
                cornerRadius[4] = typedArray.getDimension(attr, 0);
                cornerRadius[5] = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_corners_bottomRadius) {
                cornerRadius[4] = typedArray.getDimension(attr, 0);
                cornerRadius[5] = typedArray.getDimension(attr, 0);
                cornerRadius[6] = typedArray.getDimension(attr, 0);
                cornerRadius[7] = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_gradient_angle ||
                    attr == R.styleable.background_bl_checkable_gradient_angle ||
                    attr == R.styleable.background_bl_unCheckable_gradient_angle ||
                    attr == R.styleable.background_bl_checked_gradient_angle ||
                    attr == R.styleable.background_bl_unChecked_gradient_angle ||
                    attr == R.styleable.background_bl_enabled_gradient_angle ||
                    attr == R.styleable.background_bl_unEnabled_gradient_angle ||
                    attr == R.styleable.background_bl_selected_gradient_angle ||
                    attr == R.styleable.background_bl_unSelected_gradient_angle ||
                    attr == R.styleable.background_bl_pressed_gradient_angle ||
                    attr == R.styleable.background_bl_unPressed_gradient_angle ||
                    attr == R.styleable.background_bl_focused_gradient_angle ||
                    attr == R.styleable.background_bl_unFocused_gradient_angle) {

                if (gradientState == -1) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_checkable_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == -android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_unCheckable_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_checked_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == -android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_unChecked_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_enabled_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == -android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_unEnabled_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_selected_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == -android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_unSelected_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_pressed_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == -android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_unPressed_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_focused_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                } else if (gradientState == -android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_unFocused_gradient_angle) {
                    gradientAngle = typedArray.getInteger(attr, 0);
                }

            } else if (attr == R.styleable.background_bl_gradient_centerX ||
                    attr == R.styleable.background_bl_checkable_gradient_centerX ||
                    attr == R.styleable.background_bl_unCheckable_gradient_centerX ||
                    attr == R.styleable.background_bl_checked_gradient_centerX ||
                    attr == R.styleable.background_bl_unChecked_gradient_centerX ||
                    attr == R.styleable.background_bl_enabled_gradient_centerX ||
                    attr == R.styleable.background_bl_unEnabled_gradient_centerX ||
                    attr == R.styleable.background_bl_selected_gradient_centerX ||
                    attr == R.styleable.background_bl_unSelected_gradient_centerX ||
                    attr == R.styleable.background_bl_pressed_gradient_centerX ||
                    attr == R.styleable.background_bl_unPressed_gradient_centerX ||
                    attr == R.styleable.background_bl_focused_gradient_centerX ||
                    attr == R.styleable.background_bl_unFocused_gradient_centerX) {

                if (gradientState == -1) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_checkable_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_unCheckable_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_checked_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_unChecked_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_enabled_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_unEnabled_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_selected_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_unSelected_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_pressed_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_unPressed_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_focused_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_unFocused_gradient_centerX) {
                    centerX = typedArray.getFloat(attr, -1);
                }
            } else if (attr == R.styleable.background_bl_gradient_centerY ||
                    attr == R.styleable.background_bl_checkable_gradient_centerY ||
                    attr == R.styleable.background_bl_unCheckable_gradient_centerY ||
                    attr == R.styleable.background_bl_checked_gradient_centerY ||
                    attr == R.styleable.background_bl_unChecked_gradient_centerY ||
                    attr == R.styleable.background_bl_enabled_gradient_centerY ||
                    attr == R.styleable.background_bl_unEnabled_gradient_centerY ||
                    attr == R.styleable.background_bl_selected_gradient_centerY ||
                    attr == R.styleable.background_bl_unSelected_gradient_centerY ||
                    attr == R.styleable.background_bl_pressed_gradient_centerY ||
                    attr == R.styleable.background_bl_unPressed_gradient_centerY ||
                    attr == R.styleable.background_bl_focused_gradient_centerY ||
                    attr == R.styleable.background_bl_unFocused_gradient_centerY) {
                if (gradientState == -1) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_checkable_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_unCheckable_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_checked_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_unChecked_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_enabled_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_unEnabled_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_selected_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_unSelected_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_pressed_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_unPressed_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_focused_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                } else if (gradientState == -android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_unFocused_gradient_centerY) {
                    centerY = typedArray.getFloat(attr, -1);
                }
            } else if (attr == R.styleable.background_bl_gradient_centerColor ||
                    attr == R.styleable.background_bl_checkable_gradient_centerColor ||
                    attr == R.styleable.background_bl_unCheckable_gradient_centerColor ||
                    attr == R.styleable.background_bl_checked_gradient_centerColor ||
                    attr == R.styleable.background_bl_unChecked_gradient_centerColor ||
                    attr == R.styleable.background_bl_enabled_gradient_centerColor ||
                    attr == R.styleable.background_bl_unEnabled_gradient_centerColor ||
                    attr == R.styleable.background_bl_selected_gradient_centerColor ||
                    attr == R.styleable.background_bl_unSelected_gradient_centerColor ||
                    attr == R.styleable.background_bl_pressed_gradient_centerColor ||
                    attr == R.styleable.background_bl_unPressed_gradient_centerColor ||
                    attr == R.styleable.background_bl_focused_gradient_centerColor ||
                    attr == R.styleable.background_bl_unFocused_gradient_centerColor) {
                if (gradientState == -1) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_checkable_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_unCheckable_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_checked_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_unChecked_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_enabled_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_unEnabled_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_selected_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_unSelected_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_pressed_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_unPressed_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_focused_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_unFocused_gradient_centerColor) {
                    centerColor = typedArray.getColor(attr, 0);
                }

            } else if (attr == R.styleable.background_bl_gradient_endColor ||
                    attr == R.styleable.background_bl_checkable_gradient_endColor ||
                    attr == R.styleable.background_bl_unCheckable_gradient_endColor ||
                    attr == R.styleable.background_bl_checked_gradient_endColor ||
                    attr == R.styleable.background_bl_unChecked_gradient_endColor ||
                    attr == R.styleable.background_bl_enabled_gradient_endColor ||
                    attr == R.styleable.background_bl_unEnabled_gradient_endColor ||
                    attr == R.styleable.background_bl_selected_gradient_endColor ||
                    attr == R.styleable.background_bl_unSelected_gradient_endColor ||
                    attr == R.styleable.background_bl_pressed_gradient_endColor ||
                    attr == R.styleable.background_bl_unPressed_gradient_endColor ||
                    attr == R.styleable.background_bl_focused_gradient_endColor ||
                    attr == R.styleable.background_bl_unFocused_gradient_endColor) {
                if (gradientState == -1) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_checkable_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_unCheckable_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_checked_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_unChecked_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_enabled_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_unEnabled_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_selected_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_unSelected_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_pressed_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_unPressed_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_focused_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_unFocused_gradient_endColor) {
                    endColor = typedArray.getColor(attr, 0);
                }
            } else if (attr == R.styleable.background_bl_gradient_startColor ||
                    attr == R.styleable.background_bl_checkable_gradient_startColor ||
                    attr == R.styleable.background_bl_unCheckable_gradient_startColor ||
                    attr == R.styleable.background_bl_checked_gradient_startColor ||
                    attr == R.styleable.background_bl_unChecked_gradient_startColor ||
                    attr == R.styleable.background_bl_enabled_gradient_startColor ||
                    attr == R.styleable.background_bl_unEnabled_gradient_startColor ||
                    attr == R.styleable.background_bl_selected_gradient_startColor ||
                    attr == R.styleable.background_bl_unSelected_gradient_startColor ||
                    attr == R.styleable.background_bl_pressed_gradient_startColor ||
                    attr == R.styleable.background_bl_unPressed_gradient_startColor ||
                    attr == R.styleable.background_bl_focused_gradient_startColor ||
                    attr == R.styleable.background_bl_unFocused_gradient_startColor) {
                if (gradientState == -1) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_checkable_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_unCheckable_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_checked_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_unChecked_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_enabled_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_unEnabled_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_selected_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_unSelected_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_pressed_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_unPressed_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_focused_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                } else if (gradientState == -android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_unFocused_gradient_startColor) {
                    startColor = typedArray.getColor(attr, 0);
                }
            } else if (attr == R.styleable.background_bl_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_checkable_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_unCheckable_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_checked_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_unChecked_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_enabled_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_unEnabled_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_selected_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_unSelected_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_pressed_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_unPressed_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_focused_gradient_gradientRadius ||
                    attr == R.styleable.background_bl_unFocused_gradient_gradientRadius) {
                if (gradientState == -1) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_checkable_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == -android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_unCheckable_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_checked_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == -android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_unChecked_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_enabled_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == -android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_unEnabled_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_selected_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == -android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_unSelected_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_pressed_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == -android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_unPressed_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_focused_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                } else if (gradientState == -android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_unFocused_gradient_gradientRadius) {
                    gradientRadius = typedArray.getDimension(attr, 0);
                }

            } else if (attr == R.styleable.background_bl_gradient_type ||
                    attr == R.styleable.background_bl_checkable_gradient_type ||
                    attr == R.styleable.background_bl_unCheckable_gradient_type ||
                    attr == R.styleable.background_bl_checked_gradient_type ||
                    attr == R.styleable.background_bl_unChecked_gradient_type ||
                    attr == R.styleable.background_bl_enabled_gradient_type ||
                    attr == R.styleable.background_bl_unEnabled_gradient_type ||
                    attr == R.styleable.background_bl_selected_gradient_type ||
                    attr == R.styleable.background_bl_unSelected_gradient_type ||
                    attr == R.styleable.background_bl_pressed_gradient_type ||
                    attr == R.styleable.background_bl_unPressed_gradient_type ||
                    attr == R.styleable.background_bl_focused_gradient_type ||
                    attr == R.styleable.background_bl_unFocused_gradient_type) {
                if (gradientState == -1) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_checkable_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == -android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_unCheckable_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_checked_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == -android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_unChecked_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_enabled_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == -android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_unEnabled_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_selected_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == -android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_unSelected_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_pressed_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == -android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_unPressed_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_focused_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                } else if (gradientState == -android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_unFocused_gradient_type) {
                    gradientType = typedArray.getInt(attr, 0);
                }
            } else if (attr == R.styleable.background_bl_gradient_useLevel ||
                    attr == R.styleable.background_bl_checkable_gradient_useLevel ||
                    attr == R.styleable.background_bl_unCheckable_gradient_useLevel ||
                    attr == R.styleable.background_bl_checked_gradient_useLevel ||
                    attr == R.styleable.background_bl_unChecked_gradient_useLevel ||
                    attr == R.styleable.background_bl_enabled_gradient_useLevel ||
                    attr == R.styleable.background_bl_unEnabled_gradient_useLevel ||
                    attr == R.styleable.background_bl_selected_gradient_useLevel ||
                    attr == R.styleable.background_bl_unSelected_gradient_useLevel ||
                    attr == R.styleable.background_bl_pressed_gradient_useLevel ||
                    attr == R.styleable.background_bl_unPressed_gradient_useLevel ||
                    attr == R.styleable.background_bl_focused_gradient_useLevel ||
                    attr == R.styleable.background_bl_unFocused_gradient_useLevel) {
                if (gradientState == -1) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_checkable_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == -android.R.attr.state_checkable &&
                        attr == R.styleable.background_bl_unCheckable_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_checked_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == -android.R.attr.state_checked &&
                        attr == R.styleable.background_bl_unChecked_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_enabled_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == -android.R.attr.state_enabled &&
                        attr == R.styleable.background_bl_unEnabled_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_selected_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == -android.R.attr.state_selected &&
                        attr == R.styleable.background_bl_unSelected_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_pressed_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == -android.R.attr.state_pressed &&
                        attr == R.styleable.background_bl_unPressed_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_focused_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                } else if (gradientState == -android.R.attr.state_focused &&
                        attr == R.styleable.background_bl_unFocused_gradient_useLevel) {
                    useLevel = typedArray.getBoolean(attr, false);
                }

            } else if (attr == R.styleable.background_bl_padding_left) {
                padding.left = (int) typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_padding_top) {
                padding.top = (int) typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_padding_right) {
                padding.right = (int) typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_padding_bottom) {
                padding.bottom = (int) typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_size_width) {
                sizeWidth = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_size_height) {
                sizeHeight = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_stroke_width) {
                strokeWidth = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_stroke_color) {
                strokeColor = typedArray.getColor(attr, 0);
            } else if (attr == R.styleable.background_bl_stroke_dashWidth) {
                strokeDashWidth = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_stroke_dashGap) {
                strokeGap = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_shadow_size) {
                shadowSize = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_shadow_color) {
                shadowColor = typedArray.getColor(attr, 0);
            } else if (attr == R.styleable.background_bl_shadow_offsetX) {
                shadowOffsetX = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_shadow_offsetY) {
                shadowOffsetY = typedArray.getDimension(attr, 0);
            } else if (attr == R.styleable.background_bl_stroke_gradient_startColor) {
                strokeGradientStartColor = typedArray.getColor(attr, 0);
            } else if (attr == R.styleable.background_bl_stroke_gradient_centerColor) {
                strokeGradientCenterColor = typedArray.getColor(attr, 0);
            } else if (attr == R.styleable.background_bl_stroke_gradient_endColor) {
                strokeGradientEndColor = typedArray.getColor(attr, 0);
            } else if (attr == R.styleable.background_bl_stroke_gradient_angle) {
                strokeGradientAngle = typedArray.getInt(attr, 0);
            }
        }

        // Create the appropriate drawable upfront
        GradientDrawable drawable;
        boolean hasShadow = shadowSize > 0;
        if (hasShadow && shadowColor == 0) {
            // 用户设置了 size 但没有设 color，自动继承 solidColor
            if (solidColor != 0) {
                shadowColor = solidColor;
            } else {
                hasShadow = false;  // 两者都没有，则不启用阴影
            }
        }
        boolean hasStrokeGradient = strokeGradientStartColor != 0 && strokeGradientEndColor != 0 && strokeWidth > 0;

        if (hasShadow || hasStrokeGradient) {
            ShadowGradientDrawable shadowDrawable = new ShadowGradientDrawable();
            if (hasShadow) {
                shadowDrawable.setShadow(shadowSize, shadowOffsetX, shadowOffsetY, shadowColor);
            }
            drawable = shadowDrawable;
        } else {
            drawable = new GradientDrawable();
        }

        // Now configure all properties once on the created instance
        if (shape != 0) {
            drawable.setShape(shape);
        }

        if (hasSetRadius(cornerRadius)) {
            drawable.setCornerRadii(cornerRadius);
        }

        if (gradientRadius > 0) {
            // gradientRadius is handled during iteration above
            // for state-specific attributes, we already set it on the drawable
            // so no need to do anything here for non-shadow case
            // but the gradientRadius value is already captured above
        }

        if (gradientType != LINEAR_GRADIENT) {
            drawable.setGradientType(gradientType);
        }

        if (useLevel) {
            drawable.setUseLevel(useLevel);
        }

        if (typedArray.hasValue(R.styleable.background_bl_size_width) &&
                typedArray.hasValue(R.styleable.background_bl_size_height)) {
            drawable.setSize((int) sizeWidth, (int) sizeHeight);
        }

        // Handle gradient radius - for non-state attributes, we need to set it
        // Note: For state-specific, it was already set during parsing above
        // but since gradientRadius value is captured regardless, we can set it again safely
        if (gradientRadius > 0) {
            drawable.setGradientRadius(gradientRadius);
        }

        // set gradient orientation based on angle if needed
        if (((typedArray.hasValue(R.styleable.background_bl_gradient_startColor) &&
                typedArray.hasValue(R.styleable.background_bl_gradient_endColor)) ||
                (typedArray.hasValue(R.styleable.background_bl_checkable_gradient_startColor) &&
                        typedArray.hasValue(R.styleable.background_bl_unCheckable_gradient_endColor)) ||
                (typedArray.hasValue(R.styleable.background_bl_checked_gradient_startColor) &&
                        typedArray.hasValue(R.styleable.background_bl_unChecked_gradient_endColor)) ||
                (typedArray.hasValue(R.styleable.background_bl_enabled_gradient_startColor) &&
                        typedArray.hasValue(R.styleable.background_bl_unEnabled_gradient_endColor)) ||
                (typedArray.hasValue(R.styleable.background_bl_selected_gradient_startColor) &&
                        typedArray.hasValue(R.styleable.background_bl_unSelected_gradient_endColor)) ||
                (typedArray.hasValue(R.styleable.background_bl_pressed_gradient_startColor) &&
                        typedArray.hasValue(R.styleable.background_bl_unPressed_gradient_endColor)) ||
                (typedArray.hasValue(R.styleable.background_bl_focused_gradient_startColor) &&
                        typedArray.hasValue(R.styleable.background_bl_unFocused_gradient_endColor))) &&
                android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int[] colors;
            if (typedArray.hasValue(R.styleable.background_bl_gradient_centerColor)) {
                colors = new int[3];
                colors[0] = startColor;
                colors[1] = centerColor;
                colors[2] = endColor;
            } else {
                colors = new int[2];
                colors[0] = startColor;
                colors[1] = endColor;
            }
            drawable.setColors(colors);
        }

        // handle gradient orientation based on angle
        if (gradientType == LINEAR_GRADIENT &&
                android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                (typedArray.hasValue(R.styleable.background_bl_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_checkable_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_checked_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_enabled_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_selected_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_pressed_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_focused_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_unCheckable_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_unChecked_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_unEnabled_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_unSelected_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_unPressed_gradient_angle) ||
                        typedArray.hasValue(R.styleable.background_bl_unFocused_gradient_angle))) {
            gradientAngle %= 360;
            if (gradientAngle % 45 != 0) {
                throw new XmlPullParserException(typedArray.getPositionDescription()
                        + "<gradient> tag requires 'angle' attribute to "
                        + "be a multiple of 45");
            }
            GradientDrawable.Orientation mOrientation = GradientDrawable.Orientation.LEFT_RIGHT;
            switch (gradientAngle) {
                case 0:
                    mOrientation = GradientDrawable.Orientation.LEFT_RIGHT;
                    break;
                case 45:
                    mOrientation = GradientDrawable.Orientation.BL_TR;
                    break;
                case 90:
                    mOrientation = GradientDrawable.Orientation.BOTTOM_TOP;
                    break;
                case 135:
                    mOrientation = GradientDrawable.Orientation.BR_TL;
                    break;
                case 180:
                    mOrientation = GradientDrawable.Orientation.RIGHT_LEFT;
                    break;
                case 225:
                    mOrientation = GradientDrawable.Orientation.TR_BL;
                    break;
                case 270:
                    mOrientation = GradientDrawable.Orientation.TOP_BOTTOM;
                    break;
                case 315:
                    mOrientation = GradientDrawable.Orientation.TL_BR;
                    break;
            }
            drawable.setOrientation(mOrientation);
        }

        // set gradient center
        if ((typedArray.hasValue(R.styleable.background_bl_gradient_centerX) &&
                typedArray.hasValue(R.styleable.background_bl_gradient_centerY)) ||
                (typedArray.hasValue(R.styleable.background_bl_checkable_gradient_centerX) &&
                        typedArray.hasValue(R.styleable.background_bl_unCheckable_gradient_centerY)) ||
                (typedArray.hasValue(R.styleable.background_bl_checked_gradient_centerX) &&
                        typedArray.hasValue(R.styleable.background_bl_unChecked_gradient_centerY)) ||
                (typedArray.hasValue(R.styleable.background_bl_enabled_gradient_centerX) &&
                        typedArray.hasValue(R.styleable.background_bl_unEnabled_gradient_centerY)) ||
                (typedArray.hasValue(R.styleable.background_bl_selected_gradient_centerX) &&
                        typedArray.hasValue(R.styleable.background_bl_unSelected_gradient_centerY)) ||
                (typedArray.hasValue(R.styleable.background_bl_pressed_gradient_centerX) &&
                        typedArray.hasValue(R.styleable.background_bl_unPressed_gradient_centerY)) ||
                (typedArray.hasValue(R.styleable.background_bl_focused_gradient_centerX) &&
                        typedArray.hasValue(R.styleable.background_bl_unFocused_gradient_centerY))) {
            drawable.setGradientCenter(centerX, centerY);
        }

        //设置填充颜色
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int start = 0;
            ArrayList<Integer> stateList = new ArrayList<>();
            ArrayList<Integer> colorList = new ArrayList<>();
            if (typedArray.hasValue(R.styleable.background_bl_pressed_solid_color)) {
                stateList.add(android.R.attr.state_pressed);
                colorList.add(typedArray.getColor(R.styleable.background_bl_pressed_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_unPressed_solid_color)) {
                stateList.add(-android.R.attr.state_pressed);
                colorList.add(typedArray.getColor(R.styleable.background_bl_unPressed_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_checkable_solid_color)) {
                stateList.add(android.R.attr.state_checkable);
                colorList.add(typedArray.getColor(R.styleable.background_bl_checkable_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_unCheckable_solid_color)) {
                stateList.add(-android.R.attr.state_checkable);
                colorList.add(typedArray.getColor(R.styleable.background_bl_unCheckable_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_checked_solid_color)) {
                stateList.add(android.R.attr.state_checked);
                colorList.add(typedArray.getColor(R.styleable.background_bl_checked_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_unChecked_solid_color)) {
                stateList.add(-android.R.attr.state_checked);
                colorList.add(typedArray.getColor(R.styleable.background_bl_unChecked_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_enabled_solid_color)) {
                stateList.add(android.R.attr.state_enabled);
                colorList.add(typedArray.getColor(R.styleable.background_bl_enabled_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_unEnabled_solid_color)) {
                stateList.add(-android.R.attr.state_enabled);
                colorList.add(typedArray.getColor(R.styleable.background_bl_unEnabled_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_selected_solid_color)) {
                stateList.add(android.R.attr.state_selected);
                colorList.add(typedArray.getColor(R.styleable.background_bl_selected_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_unSelected_solid_color)) {
                stateList.add(-android.R.attr.state_selected);
                colorList.add(typedArray.getColor(R.styleable.background_bl_unSelected_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_focused_solid_color)) {
                stateList.add(android.R.attr.state_focused);
                colorList.add(typedArray.getColor(R.styleable.background_bl_focused_solid_color, 0));
            }
            if (typedArray.hasValue(R.styleable.background_bl_unFocused_solid_color)) {
                stateList.add(-android.R.attr.state_focused);
                colorList.add(typedArray.getColor(R.styleable.background_bl_unFocused_solid_color, 0));
            }

            if (stateList.size() > 0) {
                int size = stateList.size();
                if (typedArray.hasValue(R.styleable.background_bl_solid_color)) {
                    size++;
                }
                int[][] state = new int[size][];
                int[] color = new int[size];
                for (int iState : stateList) {
                    state[start] = new int[]{iState};
                    color[start] = colorList.get(start);
                    start++;
                }

                if (typedArray.hasValue(R.styleable.background_bl_solid_color)) {
                    state[start] = new int[]{};
                    color[start] = solidColor;
                }

                colorStateList = new ColorStateList(state, color);
                drawable.setColor(colorStateList);
            } else if (typedArray.hasValue(R.styleable.background_bl_solid_color)) {
                drawable.setColor(solidColor);
            }
        } else if (typedArray.hasValue(R.styleable.background_bl_solid_color)) {
            drawable.setColor(solidColor);
        }

        //设置边框颜色
        // configure stroke gradient if applicable
        if (hasStrokeGradient && drawable instanceof ShadowGradientDrawable) {
            ShadowGradientDrawable shadowDrawable = (ShadowGradientDrawable) drawable;
            shadowDrawable.setStrokeGradient((int) strokeWidth, strokeGradientStartColor,
                    strokeGradientCenterColor, strokeGradientEndColor, strokeGradientAngle);
            shadowDrawable.setBlShape(shape);
            if (hasSetRadius(cornerRadius)) {
                shadowDrawable.setBlCornerRadii(cornerRadius);
            }
            if (strokeDashWidth > 0 && strokeGap > 0) {
                shadowDrawable.setStrokeDash(strokeDashWidth, strokeGap);
            }
        }

        if (!hasStrokeGradient && typedArray.hasValue(R.styleable.background_bl_stroke_width)) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int start = 0;
                ArrayList<Integer> stateList = new ArrayList<>();
                ArrayList<Integer> colorList = new ArrayList<>();
                if (typedArray.hasValue(R.styleable.background_bl_pressed_stroke_color) &&
                        typedArray.hasValue(R.styleable.background_bl_unPressed_stroke_color)) {
                    stateList.add(android.R.attr.state_pressed);
                    stateList.add(-android.R.attr.state_pressed);
                    colorList.add(typedArray.getColor(R.styleable.background_bl_pressed_stroke_color, 0));
                    colorList.add(typedArray.getColor(R.styleable.background_bl_unPressed_stroke_color, 0));
                }
                if (typedArray.hasValue(R.styleable.background_bl_checkable_stroke_color) &&
                        typedArray.hasValue(R.styleable.background_bl_unCheckable_stroke_color)) {
                    stateList.add(android.R.attr.state_checkable);
                    stateList.add(-android.R.attr.state_checkable);
                    colorList.add(typedArray.getColor(R.styleable.background_bl_checkable_stroke_color, 0));
                    colorList.add(typedArray.getColor(R.styleable.background_bl_unCheckable_stroke_color, 0));
                }
                if (typedArray.hasValue(R.styleable.background_bl_checked_stroke_color) &&
                        typedArray.hasValue(R.styleable.background_bl_unChecked_stroke_color)) {
                    stateList.add(android.R.attr.state_checked);
                    stateList.add(-android.R.attr.state_checked);
                    colorList.add(typedArray.getColor(R.styleable.background_bl_checked_stroke_color, 0));
                    colorList.add(typedArray.getColor(R.styleable.background_bl_unChecked_stroke_color, 0));
                }
                if (typedArray.hasValue(R.styleable.background_bl_enabled_stroke_color) &&
                        typedArray.hasValue(R.styleable.background_bl_unEnabled_stroke_color)) {
                    stateList.add(android.R.attr.state_enabled);
                    stateList.add(-android.R.attr.state_enabled);
                    colorList.add(typedArray.getColor(R.styleable.background_bl_enabled_stroke_color, 0));
                    colorList.add(typedArray.getColor(R.styleable.background_bl_unEnabled_stroke_color, 0));
                }
                if (typedArray.hasValue(R.styleable.background_bl_selected_stroke_color) &&
                        typedArray.hasValue(R.styleable.background_bl_unSelected_stroke_color)) {
                    stateList.add(android.R.attr.state_selected);
                    stateList.add(-android.R.attr.state_selected);
                    colorList.add(typedArray.getColor(R.styleable.background_bl_selected_stroke_color, 0));
                    colorList.add(typedArray.getColor(R.styleable.background_bl_unSelected_stroke_color, 0));
                }
                if (typedArray.hasValue(R.styleable.background_bl_focused_stroke_color) &&
                        typedArray.hasValue(R.styleable.background_bl_unFocused_stroke_color)) {
                    stateList.add(android.R.attr.state_focused);
                    stateList.add(-android.R.attr.state_focused);
                    colorList.add(typedArray.getColor(R.styleable.background_bl_focused_stroke_color, 0));
                    colorList.add(typedArray.getColor(R.styleable.background_bl_unFocused_stroke_color, 0));
                }

                if (stateList.size() > 0) {
                    int[][] state = new int[stateList.size()][];
                    int[] color = new int[stateList.size()];
                    for (int iState : stateList) {
                        state[start] = new int[]{iState};
                        color[start] = colorList.get(start);
                        start++;
                    }
                    strokeColorStateList = new ColorStateList(state, color);
                    drawable.setStroke((int) strokeWidth, strokeColorStateList, strokeDashWidth, strokeGap);
                } else if (typedArray.hasValue(R.styleable.background_bl_stroke_color)) {
                    drawable.setStroke((int) strokeWidth, strokeColor, strokeDashWidth, strokeGap);
                }

                stateList = null;
                colorList = null;
            } else if (typedArray.hasValue(R.styleable.background_bl_stroke_color)) {
                drawable.setStroke((int) strokeWidth, strokeColor, strokeDashWidth, strokeGap);
            }
        }

        // set padding
        if (typedArray.hasValue(R.styleable.background_bl_padding_left) &&
                typedArray.hasValue(R.styleable.background_bl_padding_top) &&
                typedArray.hasValue(R.styleable.background_bl_padding_right) &&
                typedArray.hasValue(R.styleable.background_bl_padding_bottom)) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                drawable.setPadding(padding.left, padding.top, padding.right, padding.bottom);
            } else {
                try {
                    Field paddingField = drawable.getClass().getDeclaredField("mPadding");
                    paddingField.setAccessible(true);
                    paddingField.set(drawable, padding);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!hasStrokeGradient && strokeWidth > 0 && !typedArray.hasValue(R.styleable.background_bl_stroke_color) && !typedArray.hasValue(R.styleable.background_bl_pressed_stroke_color)) {
            // width set but no color - set stroke with transparent color
            drawable.setStroke((int) strokeWidth, 0);
        }

        return drawable;
    }

    private boolean hasSetRadius(float[] radius) {
        boolean hasSet = false;
        for (float f : radius) {
            if (f != 0.0f) {
                hasSet = true;
                break;
            }
        }
        return hasSet;
    }
}
