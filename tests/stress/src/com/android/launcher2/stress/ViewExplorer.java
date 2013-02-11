/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.launcher2;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import android.util.Log;

public class ViewExplorer {

    public static final String TAG = "ViewExplorer";

    public static ArrayList<View> getAllViewsAsArray(Activity activity) {
        ArrayList<View> views = new ArrayList<View>();
        View topView = activity.getWindow().getDecorView();
        Log.v(TAG, "Starting exploration ... ");
        explore(topView, views, null);
        return views;
    }

    public static void explore(View view, ArrayList<View> viewAccum,
            ViewTreeVisitor visitor) {
        Log.v(TAG, "exploring child: " + view.getClass().toString());
        if (viewAccum != null) {
            viewAccum.add(view);
        }
        if (visitor != null) {
            visitor.visit(view);
        }
        try {
            ViewGroup children = (ViewGroup) view;
            for (int i = 0; i < children.getChildCount(); i++) {
                View child = children.getChildAt(i);
                explore(child, viewAccum, visitor);
            }
        } catch (ClassCastException cce) {
            Log.v(TAG, "view has no children: " + view.getClass().toString());
        }
    }

    public static void grandTour(Activity activity, ViewTreeVisitor visitor) {
        View topView = activity.getWindow().getDecorView();
        Log.v(TAG, "visitor is about to visit the viewtree ... ");
        explore(topView, null, visitor);
    }
}