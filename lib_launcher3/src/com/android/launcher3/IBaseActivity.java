package com.android.launcher3;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import com.android.launcher3.folder.FolderIcon;
import com.android.launcher3.logging.UserEventDispatcher;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.util.SystemUiController;
import com.android.launcher3.util.ViewOnDrawExecutor;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

public interface IBaseActivity {
    void refreshAndBindWidgetsForPackageUser(@Nullable PackageUserKey packageUser);
    int getOrientation();
    void onAttachedToWindow();
    boolean isInState(LauncherState state);
    void onActionModeStarted(ActionMode mode);
    void onActionModeFinished(ActionMode mode);
    void onSaveInstanceStateBeforeSuper(Bundle outState);
    void onSaveInstanceStateAfterSuper(Bundle outState);
    void updateIconBadges(final Set<PackageUserKey> updatedBadges);
    void onUserLeaveHintBefore();
    void onPauseBeforeSuper();
    void onPauseAfterSuper();
    void onResumeBeforeSuper();
    void onResumeAfterSuper();
    void onStartBeforeSuper();
    void onStartAfterSuper();
    void onStopBeforeSuper();
    void onStopAfterSuper();
    void onDestroyAfterSuper();
    void onCreateAfterSuper(Bundle savedInstanceState);
    boolean isWorkspaceLocked();
    boolean isHotseatLayout(View layout);
    void onTrimMemory(int level);
    boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event);
    void setOnResumeCallback(OnResumeCallback callback);
    void onBackPressed();
    boolean onKeyShortcut(int keyCode, KeyEvent event);
    boolean onKeyUp(int keyCode, KeyEvent event);
    void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId);
    void onRestoreInstanceState(Bundle state);
    void onNewIntent(Intent intent);
    void onDetachedFromWindow();
    void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args);
    void clearPendingExecutor(ViewOnDrawExecutor executor);
    FolderIcon findFolderIcon(final long folderIconId);
    CellLayout getCellLayout(long container, long screenId);
    FolderIcon addFolder(CellLayout layout, long container, final long screenId, int cellX, int cellY);
    View createShortcut(ViewGroup parent, ShortcutInfo info);
    boolean removeItem(View v, final ItemInfo itemInfo, boolean deleteFromDb);

    void addPendingItem(PendingAddItemInfo info, long container, long screenId, int[] cell, int spanX, int spanY);

    void bindAppWidget(LauncherAppWidgetInfo item);

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

    void invalidateParent(ItemInfo info);

    void onActivityResult(final int requestCode, final int resultCode, final Intent data);

    boolean isDraggingEnabled();

    void onConfigurationChanged(Configuration newConfig);

    DeviceProfile getDeviceProfile();

    View.AccessibilityDelegate getAccessibilityDelegate();

    UserEventDispatcher getUserEventDispatcher();

    boolean isInMultiWindowModeCompat();

    SystemUiController getSystemUiController();

    boolean isStarted();

    boolean hasBeenResumed();

    void addOnDeviceProfileChangeListener(DeviceProfile.OnDeviceProfileChangeListener listener);

    boolean isForceInvisible();

    boolean hasSomeInvisibleFlag(int mask);
}
