/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher3;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.UserHandle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import com.android.launcher3.accessibility.LauncherAccessibilityDelegate;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsTransitionController;
import com.android.launcher3.badge.BadgeInfo;
import com.android.launcher3.dragndrop.DragController;
import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.folder.FolderIcon;
import com.android.launcher3.logging.UserEventDispatcher;
import com.android.launcher3.logging.UserEventDispatcher.UserEventDelegate;
import com.android.launcher3.model.ModelWriter;
import com.android.launcher3.popup.PopupDataProvider;
import com.android.launcher3.states.RotationHelper;
import com.android.launcher3.uioverrides.UiFactory;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.MultiHashMap;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.util.PendingRequestArgs;
import com.android.launcher3.util.SystemUiController;
import com.android.launcher3.util.TraceHelper;
import com.android.launcher3.util.ViewOnDrawExecutor;
import com.android.launcher3.widget.WidgetListRowEntry;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Default launcher application.
 */
public class Launcher extends Activity implements LauncherExterns,
        LauncherModel.Callbacks, LauncherProviderChangeListener, UserEventDelegate, WallpaperColorInfo.OnChangeListener, IBaseActivity, IBaseDraggingActivity {
    private LauncherDelegate mLauncherDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Constant.DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        TraceHelper.beginSection("Launcher-onCreate");

        super.onCreate(savedInstanceState);
        TraceHelper.partitionSection("Launcher-onCreate", "super call");
        mLauncherDelegate = new LauncherDelegate(this);
        mLauncherDelegate.init();
        onCreateAfterSuper(savedInstanceState);
        TraceHelper.endSection("Launcher-onCreate");
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        UiFactory.onEnterAnimationComplete(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mLauncherDelegate.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void rebindModel() {
        mLauncherDelegate.rebindModel();
    }

    public RotationHelper getRotationHelper() {
        return mLauncherDelegate.getRotationHelper();
    }

    public LauncherStateManager getStateManager() {
        return mLauncherDelegate.getStateManager();
    }

    @Override
    public <T extends View> T findViewById(int id) {
        return mLauncherDelegate.getLauncherView().findViewById(id);
    }

    @Override
    public void onAppWidgetHostReset() {
        mLauncherDelegate.onAppWidgetHostReset();
    }

    @Override
    public void onLauncherProviderChanged() {
        mLauncherDelegate.onLauncherProviderChanged();
    }

    @Override
    public boolean isDraggingEnabled() {
        return mLauncherDelegate.isDraggingEnabled();
    }

    public PopupDataProvider getPopupDataProvider() {
        return mLauncherDelegate.getPopupDataProvider();
    }

    public BadgeInfo getBadgeInfoForItem(ItemInfo info) {
        return mLauncherDelegate.getPopupDataProvider().getBadgeInfoForItem(info);
    }

    @Override
    public void invalidateParent(ItemInfo info) {
        mLauncherDelegate.invalidateParent(info);
    }

    @Override
    public void onActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
        mLauncherDelegate.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        mLauncherDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStop() {
        onStopBeforeSuper();
        super.onStop();
        onStopAfterSuper();
    }

    @Override
    protected void onStart() {
        onStartBeforeSuper();
        super.onStart();
        onStartAfterSuper();
    }

    @Override
    protected void onResume() {
        TraceHelper.beginSection("ON_RESUME");
        onResumeBeforeSuper();
        super.onResume();
        TraceHelper.partitionSection("ON_RESUME", "superCall");
        onResumeAfterSuper();
        TraceHelper.endSection("ON_RESUME");
    }

    @Override
    public void onPause() {
        onPauseBeforeSuper();
        super.onPause();
        onPauseAfterSuper();
    }

    @Override
    public void onUserLeaveHint() {
        onUserLeaveHintBefore();
        super.onUserLeaveHint();
        UiFactory.onLauncherStateOrResumeChanged(this);
    }

    public View.OnFocusChangeListener getFocusHandler() {
        return mLauncherDelegate.getFocusHandler();
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        mLauncherDelegate.onExtractedColorsChanged(wallpaperColorInfo);
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        mLauncherDelegate.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        mLauncherDelegate.onActionModeFinished(mode);
    }

    @Override
    public boolean finishAutoCancelActionMode() {
        return mLauncherDelegate.finishAutoCancelActionMode();
    }

    @Override
    public Rect getViewBounds(View v) {
        return mLauncherDelegate.getViewBounds(v);
    }

    @Override
    public final Bundle getActivityLaunchOptionsAsBundle(View v) {
        return mLauncherDelegate.getActivityLaunchOptionsAsBundle(v);
    }

    @Override
    public DeviceProfile getDeviceProfile() {
        return mLauncherDelegate.getDeviceProfile();
    }

    @Override
    public final UserEventDispatcher getUserEventDispatcher() {
        return mLauncherDelegate.getUserEventDispatcher();
    }

    @Override
    public boolean isInMultiWindowModeCompat() {
        return Utilities.ATLEAST_NOUGAT && isInMultiWindowMode();
    }

    @Override
    public SystemUiController getSystemUiController() {
        return mLauncherDelegate.getSystemUiController();
    }

    /*@Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        for (int i = mMultiWindowModeChangedListeners.size() - 1; i >= 0; i--) {
            mMultiWindowModeChangedListeners.get(i).onMultiWindowModeChanged(isInMultiWindowMode);
        }
    }*/

    @Override
    public boolean isStarted() {
        return mLauncherDelegate.isStarted();
    }

    /**
     * isResumed in already defined as a hidden final method in Activity.java
     */
    @Override
    public boolean hasBeenResumed() {
        return mLauncherDelegate.hasBeenResumed();
    }

    @Override
    public void addOnDeviceProfileChangeListener(DeviceProfile.OnDeviceProfileChangeListener listener) {
        mLauncherDelegate.addOnDeviceProfileChangeListener(listener);
    }

    /**
     * @return Wether this activity should be considered invisible regardless of actual visibility.
     */
    @Override
    public boolean isForceInvisible() {
        return mLauncherDelegate.isForceInvisible();
    }

    @Override
    public boolean hasSomeInvisibleFlag(int mask) {
        return mLauncherDelegate.hasSomeInvisibleFlag(mask);
    }

    public boolean isInState(LauncherState state) {
        return mLauncherDelegate.isInState(state);
    }

    @Override
    public void onSaveInstanceStateBeforeSuper(Bundle outState) {
        mLauncherDelegate.onSaveInstanceStateBeforeSuper(outState);
    }

    @Override
    public void onSaveInstanceStateAfterSuper(Bundle outState) {
        mLauncherDelegate.onSaveInstanceStateAfterSuper(outState);
    }

    /**
     * Finds all the views we need and configure them properly.
     *//*
    private void setupViews() {
    }*/

    public void updateIconBadges(final Set<PackageUserKey> updatedBadges) {
        mLauncherDelegate.updateIconBadges(updatedBadges);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLauncherDelegate.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLauncherDelegate.onDetachedFromWindow();
    }

    public AllAppsTransitionController getAllAppsController() {
        return mLauncherDelegate.getAllAppsController();
    }

    public DragLayer getDragLayer() {
        return mLauncherDelegate.getDragLayer();
    }

    public AllAppsContainerView getAppsView() {
        return mLauncherDelegate.getAppsView();
    }

    public Workspace getWorkspace() {
        return mLauncherDelegate.getWorkspace();
    }

    public Hotseat getHotseat() {
        return mLauncherDelegate.getHotseat();
    }

    public View getHotseatSearchBox() {
        return mLauncherDelegate.getHotseatSearchBox();
    }

    public <T extends View> T getOverviewPanel() {
        return (T) mLauncherDelegate.getOverviewPanel();
    }

    public DropTargetBar getDropTargetBar() {
        return mLauncherDelegate.getDropTargetBar();
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mLauncherDelegate.getAppWidgetHost();
    }

    public LauncherModel getModel() {
        return mLauncherDelegate.getModel();
    }

    public ModelWriter getModelWriter() {
        return mLauncherDelegate.getModelWriter();
    }

    public SharedPreferences getSharedPrefs() {
        return mLauncherDelegate.getSharedPrefs();
    }

    public int getOrientation() {
        return mLauncherDelegate.getOrientation();
    }

    @Override
    public void onNewIntent(Intent intent) {
        TraceHelper.beginSection("NEW_INTENT");
        super.onNewIntent(intent);
        mLauncherDelegate.onNewIntent(intent);
        TraceHelper.endSection("NEW_INTENT");
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mLauncherDelegate.onRestoreInstanceState(state);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        onSaveInstanceStateBeforeSuper(outState);
        super.onSaveInstanceState(outState);
        onSaveInstanceStateAfterSuper(outState);
    }

    @Override
    public void onUserLeaveHintBefore() {
        mLauncherDelegate.onUserLeaveHintBefore();
    }

    @Override
    public void onPauseBeforeSuper() {
        mLauncherDelegate.onPauseBeforeSuper();
    }

    @Override
    public void onPauseAfterSuper() {
        mLauncherDelegate.onPauseAfterSuper();
    }

    @Override
    public void onResumeBeforeSuper() {
        mLauncherDelegate.onResumeBeforeSuper();
    }

    @Override
    public void onResumeAfterSuper() {
        mLauncherDelegate.onResumeAfterSuper();
    }

    @Override
    public void onStartBeforeSuper() {
        mLauncherDelegate.onStartBeforeSuper();
    }

    @Override
    public void onStartAfterSuper() {
        mLauncherDelegate.onStartAfterSuper();
    }

    @Override
    public void onStopBeforeSuper() {
        mLauncherDelegate.onStopBeforeSuper();
    }

    @Override
    public void onStopAfterSuper() {
        mLauncherDelegate.onStopAfterSuper();
    }

    @Override
    public void onDestroyAfterSuper() {
        mLauncherDelegate.onDestroyAfterSuper();
    }

    @Override
    public void onCreateAfterSuper(Bundle savedInstanceState) {
        mLauncherDelegate.onCreateAfterSuper(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onDestroyAfterSuper();
    }

    @Override
    public LauncherAccessibilityDelegate getAccessibilityDelegate() {
        return mLauncherDelegate.getAccessibilityDelegate();
    }

    public DragController getDragController() {
        return mLauncherDelegate.getDragController();
    }

    /*@Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
    }*/
    @Override
    public void startIntentSenderForResult (IntentSender intent, int requestCode,
            Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) {
        try {
            super.startIntentSenderForResult(intent, requestCode,
                fillInIntent, flagsMask, flagsValues, extraFlags, options);
        } catch (IntentSender.SendIntentException e) {
            throw new ActivityNotFoundException();
        }
    }

    /**
     * Indicates that we want global search for this activity by setting the globalSearch
     * argument for {@link #startSearch} to true.
     */
    /*@Override
    public void startSearch(String initialQuery, boolean selectInitialQuery,
            Bundle appSearchData, boolean globalSearch) {
        if (appSearchData == null) {
            appSearchData = new Bundle();
            appSearchData.putString("source", "launcher-search");
        }

        if (mLauncherDelegate.getLauncherCallbacks() == null ||
                !mLauncherDelegate.getLauncherCallbacks().startSearch(initialQuery, selectInitialQuery, appSearchData)) {
            // Starting search from the callbacks failed. Start the default global search.
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, true);
        }

        // We need to show the workspace after starting the search
        mLauncherDelegate.getStateManager().goToState(NORMAL);
    }*/

    public boolean isWorkspaceLocked() {
        return mLauncherDelegate.isWorkspaceLocked();
    }

    public boolean isWorkspaceLoading() {
        return mLauncherDelegate.isWorkspaceLoading();
    }

    public void setWaitingForResult(PendingRequestArgs args) {
        mLauncherDelegate.setPendingRequestArgs(args);
    }

    @Override
    public void addPendingItem(PendingAddItemInfo info, long container, long screenId,
            int[] cell, int spanX, int spanY) {
        mLauncherDelegate.addPendingItem(info, container, screenId, cell, spanX, spanY);
    }

    @Override
    public FolderIcon findFolderIcon(long folderIconId) {
        return mLauncherDelegate.findFolderIcon(folderIconId);
    }

    @Override
    public CellLayout getCellLayout(long container, long screenId) {
        return mLauncherDelegate.getCellLayout(container, screenId);
    }

    @Override
    public FolderIcon addFolder(CellLayout layout, long container, long screenId, int cellX, int cellY) {
        return mLauncherDelegate.addFolder(layout, container, screenId, cellX, cellY);
    }

    @Override
    public View createShortcut(ViewGroup parent, ShortcutInfo info) {
        return mLauncherDelegate.createShortcut(parent, info);
    }

    /**
     * Unbinds the view for the specified item, and removes the item and all its children.
     *
     * @param v the view being removed.
     * @param itemInfo the {@link ItemInfo} for this view.
     * @param deleteFromDb whether or not to delete this item from the db.
     */
    public boolean removeItem(View v, final ItemInfo itemInfo, boolean deleteFromDb) {
        return mLauncherDelegate.removeItem(v, itemInfo, deleteFromDb);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return (event.getKeyCode() == KeyEvent.KEYCODE_HOME) || super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        mLauncherDelegate.onBackPressed();
    }

    @Override
    public void modifyUserEvent(LauncherLogProto.LauncherEvent event) {
        mLauncherDelegate.modifyUserEvent(event);
    }

    @Override
    public boolean startActivitySafely(View v, Intent intent, ItemInfo item) {
        return mLauncherDelegate.startActivitySafely(v, intent, item);
    }

    public boolean isHotseatLayout(View layout) {
        return mLauncherDelegate.isHotseatLayout(layout);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        mLauncherDelegate.onTrimMemory(level);
        UiFactory.onTrimMemory(this, level);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        final boolean result = super.dispatchPopulateAccessibilityEvent(event);
        mLauncherDelegate.dispatchPopulateAccessibilityEvent(event);
        return result;
    }

    public void setOnResumeCallback(OnResumeCallback callback) {
        mLauncherDelegate.setOnResumeCallback(callback);
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public int getCurrentWorkspaceScreen() {
        return mLauncherDelegate.getCurrentWorkspaceScreen();
    }

    /**
     * Clear any pending bind callbacks. This is called when is loader is planning to
     * perform a full rebind from scratch.
     */
    @Override
    public void clearPendingBinds() {
        mLauncherDelegate.clearPendingBinds();
    }

    /**
     * Refreshes the shortcuts shown on the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void startBinding() {
        mLauncherDelegate.startBinding();
    }

    @Override
    public void bindScreens(ArrayList<Long> orderedScreenIds) {
        mLauncherDelegate.bindScreens(orderedScreenIds);
    }

    @Override
    public void bindAppsAdded(ArrayList<Long> newScreens, ArrayList<ItemInfo> addNotAnimated,
            ArrayList<ItemInfo> addAnimated) {
        mLauncherDelegate.bindAppsAdded(newScreens, addNotAnimated, addAnimated);
    }

    /**
     * Bind the items start-end from the list.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public void bindItems(final List<ItemInfo> items, final boolean forceAnimateIcons) {
        mLauncherDelegate.bindItems(items, forceAnimateIcons);
    }

    /**
     * Add the views for a widget to the workspace.
     */
    public void bindAppWidget(LauncherAppWidgetInfo item) {
        mLauncherDelegate.bindAppWidget(item);
    }

    public void onPageBoundSynchronously(int page) {
        mLauncherDelegate.setSynchronouslyBoundPage(page);
    }

    @Override
    public void executeOnNextDraw(ViewOnDrawExecutor executor) {
        mLauncherDelegate.executeOnNextDraw(executor);
    }

    @Override
    public void clearPendingExecutor(ViewOnDrawExecutor executor) {
        mLauncherDelegate.clearPendingExecutor(executor);
    }

    @Override
    public void finishFirstPageBind(final ViewOnDrawExecutor executor) {
        mLauncherDelegate.finishFirstPageBind(executor);
    }

    /**
     * Callback saying that there aren't any more items to bind.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void finishBindingItems() {
        mLauncherDelegate.finishBindingItems();
    }

    /**
     * Add the icons for all apps.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public void bindAllApplications(ArrayList<AppInfo> apps) {
        mLauncherDelegate.bindAllApplications(apps);
    }

    /**
     * Copies LauncherModel's map of activities to shortcut ids to Launcher's. This is necessary
     * because LauncherModel's map is updated in the background, while Launcher runs on the UI.
     */
    @Override
    public void bindDeepShortcutMap(MultiHashMap<ComponentKey, String> deepShortcutMapCopy) {
        mLauncherDelegate.getPopupDataProvider().setDeepShortcutMap(deepShortcutMapCopy);
    }

    /**
     * A package was updated.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public void bindAppsAddedOrUpdated(ArrayList<AppInfo> apps) {
        mLauncherDelegate.getAppsView().getAppsStore().addOrUpdateApps(apps);
    }

    @Override
    public void bindPromiseAppProgressUpdated(PromiseAppInfo app) {
        mLauncherDelegate.getAppsView().getAppsStore().updatePromiseAppProgress(app);
    }

    @Override
    public void bindWidgetsRestored(ArrayList<LauncherAppWidgetInfo> widgets) {
        mLauncherDelegate.getWorkspace().widgetsRestored(widgets);
    }

    /**
     * Some shortcuts were updated in the background.
     * Implementation of the method from LauncherModel.Callbacks.
     *
     * @param updated list of shortcuts which have changed.
     */
    @Override
    public void bindShortcutsChanged(ArrayList<ShortcutInfo> updated, final UserHandle user) {
        mLauncherDelegate.bindShortcutsChanged(updated, user);
    }

    /**
     * Update the state of a package, typically related to install state.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public void bindRestoreItemsChange(HashSet<ItemInfo> updates) {
        mLauncherDelegate.getWorkspace().updateRestoreItems(updates);
    }

    /**
     * A package was uninstalled/updated.  We take both the super set of packageNames
     * in addition to specific applications to remove, the reason being that
     * this can be called when a package is updated as well.  In that scenario,
     * we only remove specific components from the workspace and hotseat, where as
     * package-removal should clear all items by package name.
     */
    @Override
    public void bindWorkspaceComponentsRemoved(final ItemInfoMatcher matcher) {
        mLauncherDelegate.bindWorkspaceComponentsRemoved(matcher);
    }

    @Override
    public void bindAppInfosRemoved(final ArrayList<AppInfo> appInfos) {
        mLauncherDelegate.bindAppInfosRemoved(appInfos);
    }

    @Override
    public void bindAllWidgets(final ArrayList<WidgetListRowEntry> allWidgets) {
        mLauncherDelegate.bindAllWidgets(allWidgets);
    }

    /**
     * @param packageUser if null, refreshes all widgets and shortcuts, otherwise only
     *                    refreshes the widgets and shortcuts associated with the given package/user
     */
    public void refreshAndBindWidgetsForPackageUser(@Nullable PackageUserKey packageUser) {
        mLauncherDelegate.refreshAndBindWidgetsForPackageUser(packageUser);
    }

    /**
     * $ adb shell dumpsys activity com.android.launcher3.Launcher [--all]
     */
    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        mLauncherDelegate.dump(prefix, fd, writer, args);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.N)
    public void onProvideKeyboardShortcuts(
            List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {
        mLauncherDelegate.onProvideKeyboardShortcuts(data, menu, deviceId);
        super.onProvideKeyboardShortcuts(data, menu, deviceId);
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        boolean onKeyShortcut = mLauncherDelegate.onKeyShortcut(keyCode, event);
        if(onKeyShortcut)
            return true;
        return super.onKeyShortcut(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean onKeyUp = mLauncherDelegate.onKeyUp(keyCode, event);
        if (onKeyUp)
            return true;
        return super.onKeyUp(keyCode, event);
    }

    @Retention(SOURCE)
    @IntDef(
            flag = true,
            value = {Constant.INVISIBLE_BY_STATE_HANDLER, Constant.INVISIBLE_BY_APP_TRANSITIONS,
                    Constant.INVISIBLE_BY_PENDING_FLAGS, Constant.PENDING_INVISIBLE_BY_WALLPAPER_ANIMATION})
    public @interface InvisibilityFlags{}

    @Retention(SOURCE)
    @IntDef(
            flag = true,
            value = {Constant.ACTIVITY_STATE_STARTED, Constant.ACTIVITY_STATE_RESUMED, Constant.ACTIVITY_STATE_USER_ACTIVE})
    public @interface ActivityFlags{}
}
