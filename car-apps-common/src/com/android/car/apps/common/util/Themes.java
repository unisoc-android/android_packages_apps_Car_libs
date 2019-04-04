/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.car.apps.common.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

/**
 * Various utility methods associated with theming.
 */
public class Themes {

    /**
     * Returns the color assigned to the given attribute.
     */
    public static int getAttrColor(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    /**
     * Returns the boolean assigned to the given attribute.
     */
    public static boolean getAttrBoolean(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        boolean value = ta.getBoolean(0, false);
        ta.recycle();
        return value;
    }

    /**
     * Returns the Drawable assigned to the given attribute.
     */
    public static Drawable getAttrDrawable(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        Drawable value = ta.getDrawable(0);
        ta.recycle();
        return value;
    }

    /**
     * Returns the Integer assigned to the given attribute.
     */
    public static int getAttrInteger(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int value = ta.getInteger(0, 0);
        ta.recycle();
        return value;
    }

    /**
     * Returns the identifier of the resolved resource assigned to the given attribute.
     */
    public static int getAttrResourceId(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int resId = ta.getResourceId(0, 0);
        ta.recycle();
        return resId;
    }
}