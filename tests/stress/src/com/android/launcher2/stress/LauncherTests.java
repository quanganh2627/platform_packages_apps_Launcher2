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

import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import java.util.ArrayList;
import android.view.KeyEvent;
import com.android.launcher.R;
import android.view.View;
import android.widget.TextView;
import android.test.TouchUtils;
import android.database.Cursor;
import android.net.Uri;
import java.net.URISyntaxException;
import android.view.MotionEvent;
import android.view.Display;
import android.graphics.Point;
import android.content.ContentResolver;
import android.view.ViewGroup;

/**
 * Instrumentation class for Launcher app. adb shell am mInstrument -e class \
 * com.android.launcher2.tests.LauncherTests -w \
 * com.android.launcher2.tests/android.test.InstrumentationTestRunner
 */

public class LauncherTests extends ActivityInstrumentationTestCase2<Launcher> {
    private static final int WAIT_FOR_SYNC = 1000;
    private static final int WAIT_FOR_SCREEN_CHANGE = 2000;
    private static final int WAIT_FOR_SHORTCUT = 4000;
    private static final int MOTION_EVENT_SPAN = 300;
    private static final String TAG = "LauncherTests";
    private static final String SHORTCUT_APP_NAME = "Calculator";
    private Instrumentation mInst = null;
    private Context mContext;
    private Launcher mActivity;
    private int mWidth;
    private int mHeight;

    public LauncherTests() {
        super(Launcher.class);
        Log.v(TAG, "LauncherTests constructor");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Log.v(TAG, "Test setUp");
        mInst = getInstrumentation();
        mContext = mInst.getTargetContext();
        mActivity = getActivity();
        getScreenDimensions();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Log.v(TAG, "Test tearDown");
    }

    private void getScreenDimensions() {
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point launcherSize = new Point();
        display.getSize(launcherSize);
        mWidth = launcherSize.x;
        mHeight = launcherSize.y;
    }

    public void testLauncherIsLoaded() throws Exception {
        Log.v(TAG, "Instrumentation test start");
        assertNotNull(mActivity);
        Log.v(TAG, "launcher activity is available");
        Log.v(TAG, "Instrumentation test stop");
    }

    public void testLauncherRotation() {
        Log.v(TAG, "Instrumentation test start");
        mInst.waitForIdleSync();
        SystemClock.sleep(WAIT_FOR_SYNC);
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getInstrumentation().waitForIdleSync();
        SystemClock.sleep(WAIT_FOR_SYNC);
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Log.v(TAG, "Instrumentation test stop");
    }

    private void swipeLeft() {
        long downTime = SystemClock.uptimeMillis();
        SystemClock.sleep(MOTION_EVENT_SPAN);
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, mWidth / 2, mHeight / 2, 0);
        mInst.sendPointerSync(event);

        downTime = SystemClock.uptimeMillis();
        SystemClock.sleep(MOTION_EVENT_SPAN);
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE,
                mWidth / 10, mHeight / 2, 0);
        mInst.sendPointerSync(event);

        downTime = SystemClock.uptimeMillis();
        SystemClock.sleep(MOTION_EVENT_SPAN);
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,
                mWidth / 10, mHeight / 2, 0);
        mInst.sendPointerSync(event);
    }

    public void testSwitchBetweenScreens() throws InterruptedException {
        Log.v(TAG, "Instrumentation test start");
        mInst.waitForIdleSync();
        SystemClock.sleep(WAIT_FOR_SYNC);
        Workspace workspace = mActivity.getWorkspace();
        int startScreen = workspace.getCurrentPage();
        Log.v(TAG, "start workspace screen: " + startScreen);
        swipeLeft();
        Thread.sleep(WAIT_FOR_SCREEN_CHANGE);
        int currentScreen = workspace.getCurrentPage();
        Log.v(TAG, "current workspace screen: " + currentScreen);
        assertTrue("Screen must change", startScreen != currentScreen);
        Log.v(TAG, "Instrumentation test stop");
    }

    private int countShortcuts(Context context, String name) {
        Log.v(TAG, "Shortcut name: " + name);
        int nrOfShorcuts = 0;

        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, new String[] {
                LauncherSettings.Favorites._ID, LauncherSettings.Favorites.INTENT },
                LauncherSettings.Favorites.TITLE + "=?", new String[] { name }, null);
        nrOfShorcuts = c.getCount();
        Log.v(TAG, "number of shortcuts found: " + nrOfShorcuts);
        c.close();

        return nrOfShorcuts;

    }

    private void openAllAppsView() throws InterruptedException {
        View allAppsBar = mActivity.findViewById(R.id.hotseat);
        View callLayout = ((ViewGroup) allAppsBar).getChildAt(0);
        View allAppsContainer = ((ViewGroup) callLayout).getChildAt(0);
        View allAppsButton = ((ViewGroup) allAppsContainer).getChildAt(0);
        TouchUtils.clickView(this, allAppsButton);
        Thread.sleep(WAIT_FOR_SYNC);
    }

    private void shortcutLongClickView(View v) throws InterruptedException {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        int height = v.getMeasuredHeight();
        int width = v.getMeasuredWidth();
        int x = location[0] + width / 2;
        int y = location[1] + height / 2;
        Log.v(TAG, "long click at " + x + "-" + y);
        long downTime = SystemClock.uptimeMillis();
        SystemClock.sleep(MOTION_EVENT_SPAN);
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        mInst.sendPointerSync(event);
        SystemClock.sleep(WAIT_FOR_SHORTCUT);
        downTime = SystemClock.uptimeMillis();
        SystemClock.sleep(MOTION_EVENT_SPAN);
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
        mInst.sendPointerSync(event);
    }

    private void makeShortcut() throws InterruptedException {
        ShortcutVisitor visitor = new ShortcutVisitor();
        visitor.shortcutText = SHORTCUT_APP_NAME;
        ViewExplorer.grandTour(mActivity, visitor);
        Log.v(TAG, "shortcut source app must not be null");
        assertNotNull(visitor.shortcutView);
        shortcutLongClickView(visitor.shortcutView);
        Thread.sleep(WAIT_FOR_SYNC);
    }

    public void testAddShortcutToHomeScreen() throws InterruptedException {
        Log.v(TAG, "Instrumentation test start");
        mInst.waitForIdleSync();
        SystemClock.sleep(WAIT_FOR_SYNC);
        int nrOfShortcutsBefore = countShortcuts(mContext, SHORTCUT_APP_NAME);
        Log.v(TAG, "Shortcuts found before: " + nrOfShortcutsBefore);
        openAllAppsView();
        makeShortcut();
        int nrOfShortcutsAfter = countShortcuts(mContext, SHORTCUT_APP_NAME);
        Log.v(TAG, "Shortcuts found after: " + nrOfShortcutsAfter);
        assertTrue("shortcuts after must be greater than before",
                nrOfShortcutsAfter == nrOfShortcutsBefore + 1);
        Log.v(TAG, "Instrumentation test stop");
    }

    private void deleteShortcuts() {
        final ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, new String[] {
                LauncherSettings.Favorites._ID, LauncherSettings.Favorites.INTENT },
                LauncherSettings.Favorites.TITLE + "=?",
                new String[] { SHORTCUT_APP_NAME }, null);

        final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
        final int intentIndex = c
                .getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
        while (c.moveToNext()) {
            final long id = c.getLong(idIndex);
            final Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
            cr.delete(uri, null, null);
        }
        cr.notifyChange(LauncherSettings.Favorites.CONTENT_URI, null);

    }

    public void testRemoveShortcutsFromHomeScreen() throws InterruptedException {
        Log.v(TAG, "Instrumentation test start");
        testAddShortcutToHomeScreen();
        deleteShortcuts();
        assertTrue("There must not be any shortcut on the Home Screen",
                countShortcuts(mContext, SHORTCUT_APP_NAME) == 0);
        Log.v(TAG, "Instrumentation test stop");
    }

}

class ShortcutVisitor implements ViewTreeVisitor {
    private static final String TAG = "ShortcutVisitor";
    public String shortcutText;
    public View shortcutView;

    @Override
    public void visit(View view) {
        if (view instanceof TextView) {
            String text = ((TextView) view).getText().toString();
            Log.v(TAG, "found TextView with text: " + text);
            if (text.contains(shortcutText)) {
                shortcutView = view;
            }
        }
    }
}