package com.noober.background.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

public class ResourceUtils {

    @Nullable
    public static Drawable getDrawable(Context context, String resName) {
        Resources resources = context.getResources();
        if(resName.startsWith("#")){
            return new ColorDrawable(Color.parseColor(resName));
        }
        int id = resources.getIdentifier(resName, "drawable", context.getPackageName());
        if(id == 0){
            id = resources.getIdentifier(resName, "mipmap", context.getPackageName());
        }
        if(id == 0){
            id = resources.getIdentifier(resName, "color", context.getPackageName());
        }
        if(id == 0){
            return null;
        }
        return ContextCompat.getDrawable(context, id);
    }


    public static int getColor(Context context, String resName) {
        Resources resources = context.getResources();
        if (resName.startsWith("#")) {
            return Color.parseColor(resName);
        }
        // 支持 ?attr/xxx 和 ?xxx 主题属性引用
        if (resName.startsWith("?attr/") || resName.startsWith("?")) {
            String attrName = resName.startsWith("?attr/")
                ? resName.substring(6) : resName.substring(1);
            int attrId = resources.getIdentifier(attrName, "attr", context.getPackageName());
            if (attrId == 0) {
                attrId = resources.getIdentifier(attrName, "attr", "android");
            }
            if (attrId != 0) {
                TypedValue typedValue = new TypedValue();
                if (context.getTheme().resolveAttribute(attrId, typedValue, true)) {
                    if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
                        && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                        return typedValue.data;
                    }
                    if (typedValue.resourceId != 0) {
                        return ContextCompat.getColor(context, typedValue.resourceId);
                    }
                }
            }
            return -1;
        }
        // 支持 @color/xxx 格式
        String colorName = resName;
        if (resName.startsWith("@color/")) {
            colorName = resName.substring(7);
        }
        int id = resources.getIdentifier(colorName, "color", context.getPackageName());
        if (id == 0) {
            return -1;
        }
        return ContextCompat.getColor(context, id);
    }
}
