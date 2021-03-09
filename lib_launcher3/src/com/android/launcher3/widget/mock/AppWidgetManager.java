/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.launcher3.widget.mock;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

/**
 * Updates AppWidget state-更新widget UI状态，对接框架IntentService;
 * gets information about installed AppWidget providers and other AppWidget related state-获取可用的widget.
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For more information about creating app widgets, read the
 * <a href="{@docRoot}guide/topics/appwidgets/index.html">App Widgets</a> developer guide.</p>
 * </div>
 */
public class AppWidgetManager {

    /**
     * Sent when it is time to configure your AppWidget while it is being added to a host.
     * This action is not sent as a broadcast to the AppWidget provider, but as a startActivity
     * to the activity specified in the {@link AppWidgetProviderInfo AppWidgetProviderInfo
     * meta-data}.
     *
     * <p>
     * The intent will contain the following extras:
     * <table>
     *   <tr>
     *     <td>{@link #EXTRA_APPWIDGET_ID}</td>
     *     <td>The appWidgetId to configure.</td>
     *  </tr>
     * </table>
     *
     * <p>If you return {@link android.app.Activity#RESULT_OK} using
     * {@link android.app.Activity#setResult Activity.setResult()}, the AppWidget will be added,
     * and you will receive an {ACTION_APPWIDGET_UPDATE} broadcast for this AppWidget.
     * If you return {@link android.app.Activity#RESULT_CANCELED}, the host will cancel the add
     * and not display this AppWidget, and you will receive a {ACTION_APPWIDGET_DELETED}
     * broadcast.
     */
    public static final String ACTION_APPWIDGET_CONFIGURE = "android.appwidget.action.APPWIDGET_CONFIGURE";

    /**
     * An intent extra that contains one appWidgetId.
     * <p>
     * The value will be an int that can be retrieved like this:
     * {sample frameworks/base/tests/appwidgets/AppWidgetHostTest/src/com/android/tests/appwidgethost/AppWidgetHostActivity.java getExtra_EXTRA_APPWIDGET_ID}
     */
    public static final String EXTRA_APPWIDGET_ID = "appWidgetId";

    /**
     * A bundle extra that contains the lower bound on the current width, in dips, of a widget instance.
     */
    public static final String OPTION_APPWIDGET_MIN_WIDTH = "appWidgetMinWidth";

    /**
     * A bundle extra that contains the lower bound on the current height, in dips, of a widget instance.
     */
    public static final String OPTION_APPWIDGET_MIN_HEIGHT = "appWidgetMinHeight";

    /**
     * A bundle extra that contains the upper bound on the current width, in dips, of a widget instance.
     */
    public static final String OPTION_APPWIDGET_MAX_WIDTH = "appWidgetMaxWidth";

    /**
     * A bundle extra that contains the upper bound on the current width, in dips, of a widget instance.
     */
    public static final String OPTION_APPWIDGET_MAX_HEIGHT = "appWidgetMaxHeight";

    /**
     * A bundle extra that hints to the AppWidgetProvider the category of host that owns this
     * this widget. Can have the value {@link
     * AppWidgetProviderInfo#WIDGET_CATEGORY_HOME_SCREEN} or {@link
     * AppWidgetProviderInfo#WIDGET_CATEGORY_KEYGUARD} or {@link
     * AppWidgetProviderInfo#WIDGET_CATEGORY_SEARCHBOX}.
     */
    public static final String OPTION_APPWIDGET_HOST_CATEGORY = "appWidgetCategory";

    /**
     * An intent extra that contains multiple appWidgetIds.
     * <p>
     * The value will be an int array that can be retrieved like this:
     * {sample frameworks/base/tests/appwidgets/AppWidgetHostTest/src/com/android/tests/appwidgethost/TestAppWidgetProvider.java getExtra_EXTRA_APPWIDGET_IDS}
     */
    public static final String EXTRA_APPWIDGET_IDS = "appWidgetIds";

    /**
     * An intent extra attached to the {@link #ACTION_APPWIDGET_HOST_RESTORED} broadcast,
     * indicating the integer ID of the host whose widgets have just been restored.
     */
    public static final String EXTRA_HOST_ID = "hostId";

    /**
     * A sentinel value that the AppWidget manager will never return as a appWidgetId.
     */
    public static final int INVALID_APPWIDGET_ID = 0;

    /**
     * Sent to widget hosts after AppWidget state related to the host has been restored from
     * backup. The intent contains information about how to translate AppWidget ids from the
     * restored data to their new equivalents.  If an application maintains multiple separate
     * widget host instances, it will receive this broadcast separately for each one.
     *
     * <p>The intent will contain the following extras:
     *
     * <table>
     *   <tr>
     *     <td>{@link #EXTRA_APPWIDGET_OLD_IDS}</td>
     *     <td>The set of appWidgetIds represented in a restored backup that have been successfully
     *     incorporated into the current environment.  This may be all of the AppWidgets known
     *     to this application, or just a subset.  Each entry in this array of appWidgetIds has
     *     a corresponding entry in the {@link #EXTRA_APPWIDGET_IDS} extra.</td>
     *  </tr>
     *   <tr>
     *     <td>{@link #EXTRA_APPWIDGET_IDS}</td>
     *     <td>The set of appWidgetIds now valid for this application.  The app should look at
     *     its restored widget configuration and translate each appWidgetId in the
     *     {@link #EXTRA_APPWIDGET_OLD_IDS} array to its new value found at the corresponding
     *     index within this array.</td>
     *  </tr>
     *  <tr>
     *     <td>{@link #EXTRA_HOST_ID}</td>
     *     <td>The integer ID of the widget host instance whose state has just been restored.</td>
     *  </tr>
     * </table>
     *
     * <p class="note">This is a protected intent that can only be sent
     * by the system.
     *
     *  #ACTION_APPWIDGET_RESTORED
     */
    public static final String ACTION_APPWIDGET_HOST_RESTORED
            = "android.appwidget.action.APPWIDGET_HOST_RESTORED";

    /**
     * An intent extra that contains multiple appWidgetIds.  These are id values as
     * they were provided to the application during a recent restore from backup.  It is
     * attached to the { #ACTION_APPWIDGET_RESTORED} broadcast intent.
     *
     * <p>
     * The value will be an int array that can be retrieved like this:
     * {@sample frameworks/base/tests/appwidgets/AppWidgetHostTest/src/com/android/tests/appwidgethost/TestAppWidgetProvider.java getExtra_EXTRA_APPWIDGET_IDS}
     */
    public static final String EXTRA_APPWIDGET_OLD_IDS = "appWidgetOldIds";

    /**
     * An extra that can be passed to
     * { #requestPinAppWidget(ComponentName, Bundle, PendingIntent)}. This would allow the
     * launcher app to present a custom preview to the user.
     *
     * <p>
     * The value should be a {@link RemoteViews} similar to what is used with
     * { #updateAppWidget} calls.
     */
    public static final String EXTRA_APPWIDGET_PREVIEW = "appWidgetPreview";
    private Context context;
    private static AppWidgetManager sAppWidgetManager;
    private final WidgetServer widgetServer;

    /**
     * Get the AppWidgetManager instance to use for the supplied {@link Context
     * Context} object.
     */
    public static AppWidgetManager getInstance(Context context) {
        if (sAppWidgetManager == null) {
            synchronized (AppWidgetManager.class) {
                if (sAppWidgetManager == null) {
                    sAppWidgetManager = new AppWidgetManager(context);
                }
            }
        }
        return sAppWidgetManager;
    }

    /**
     * Creates a new instance.
     *
     * @param context The current context in which to operate.
     * @hide
     */
    AppWidgetManager(Context context) {
        this.context = context;
        widgetServer = WidgetServer.getInstance(context);
    }

    /**
     * Update the extras for a given widget instance.
     * <p>
     * The extras can be used to embed additional information about this widget to be accessed
     * by the associated widget's AppWidgetProvider.
     *
     * @see #getAppWidgetOptions(int)
     *
     * @param appWidgetId The AppWidget instances for which to set the RemoteViews.
     * @param options The options to associate with this widget
     */
    // TODO: 2021/3/1  插件升级导致最大最小尺寸发生变化了？
    public void updateAppWidgetOptions(int appWidgetId, Bundle options) {
    }

    /**
     * We get the old options to see if the sizes have changed
     *
     * @param appWidgetId The AppWidget instances for which to set the RemoteViews.
     * @return The options associated with the given widget instance.
     */
    public Bundle getAppWidgetOptions(int appWidgetId) {
        AppWidgetProviderInfo appWidgetProviderInfo = widgetServer.appWidgetProviderInfo(appWidgetId);
        Bundle bundle = new Bundle();
        bundle.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, appWidgetProviderInfo.minWidth);
        // TODO: 2021/3/1 这里有问题
        bundle.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, appWidgetProviderInfo.minWidth*3);

        bundle.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, appWidgetProviderInfo.minHeight);
        // TODO: 2021/3/1 这里有问题
        bundle.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, appWidgetProviderInfo.minHeight*3);

        return bundle;
    }

    public @NonNull
    List<AppWidgetProviderInfo> getInstalledProvidersForProfile() {
        return getInstalledProvidersForProfile(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN);
    }

    public @NonNull List<AppWidgetProviderInfo> getInstalledProvidersForPackage() {
        return getInstalledProvidersForProfile(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN);
    }

    public List<AppWidgetProviderInfo> getInstalledProvidersForProfile(int categoryFilter) {
        /*由框架的widgetServer返回所有的info列表*/
        List<Widget> widgets = widgetServer.widgetList();
        return getInfoList(widgets);
    }

    private List<AppWidgetProviderInfo> getInfoList(List<Widget> widgets) {
        ArrayList<AppWidgetProviderInfo> list = new ArrayList<>();
        for(Widget widget: widgets){
            list.add(widget.getAppWidgetProviderInfo());
        }
        return list;
    }

    /**
     * Get the available info about the AppWidget.
     *
     * @return A appWidgetId.  If the appWidgetId has not been bound to a provider yet, or
     * you don't have access to that appWidgetId, null is returned.
     */
    public AppWidgetProviderInfo getAppWidgetInfo(int appWidgetId) {
        return widgetServer.appWidgetProviderInfo(appWidgetId);
    }
}
