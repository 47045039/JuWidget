package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Process;
import android.os.StrictMode;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.Display;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.KeyboardShortcutInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.android.launcher3.accessibility.LauncherAccessibilityDelegate;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsTransitionController;
import com.android.launcher3.allapps.DiscoveryBounce;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.LauncherAppsCompatVO;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.dragndrop.DragController;
import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.dragndrop.DragView;
import com.android.launcher3.folder.FolderIcon;
import com.android.launcher3.folder.FolderIconPreviewVerifier;
import com.android.launcher3.keyboard.CustomActionsPopup;
import com.android.launcher3.keyboard.ViewGroupFocusHelper;
import com.android.launcher3.logging.FileLog;
import com.android.launcher3.logging.UserEventDispatcher;
import com.android.launcher3.model.ModelWriter;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.popup.PopupContainerWithArrow;
import com.android.launcher3.popup.PopupDataProvider;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.states.InternalStateHandler;
import com.android.launcher3.states.RotationHelper;
import com.android.launcher3.touch.ItemClickHandler;
import com.android.launcher3.uioverrides.DisplayRotationListener;
import com.android.launcher3.uioverrides.UiFactory;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.util.ActivityResultInfo;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.MultiHashMap;
import com.android.launcher3.util.MultiValueAlpha;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.util.PendingRequestArgs;
import com.android.launcher3.util.SystemUiController;
import com.android.launcher3.util.Themes;
import com.android.launcher3.util.Thunk;
import com.android.launcher3.util.TraceHelper;
import com.android.launcher3.util.UiThreadHelper;
import com.android.launcher3.util.ViewOnDrawExecutor;
import com.android.launcher3.views.OptionsPopupView;
import com.android.launcher3.widget.LauncherAppWidgetHostView;
import com.android.launcher3.widget.PendingAddShortcutInfo;
import com.android.launcher3.widget.PendingAddWidgetInfo;
import com.android.launcher3.widget.PendingAppWidgetHostView;
import com.android.launcher3.widget.WidgetAddFlowHandler;
import com.android.launcher3.widget.WidgetHostViewLoader;
import com.android.launcher3.widget.WidgetListRowEntry;
import com.android.launcher3.widget.WidgetsFullSheet;
import com.android.launcher3.widget.custom.CustomWidgetParser;
import com.android.launcher3.widget.mock.AppWidgetHostView;
import com.android.launcher3.widget.mock.AppWidgetManager;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.app.Activity.DEFAULT_KEYS_SEARCH_LOCAL;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN;
import static android.content.pm.ActivityInfo.CONFIG_ORIENTATION;
import static android.content.pm.ActivityInfo.CONFIG_SCREEN_SIZE;
import static com.android.launcher3.LauncherAnimUtils.SPRING_LOADED_EXIT_DELAY;
import static com.android.launcher3.LauncherState.ALL_APPS;
import static com.android.launcher3.LauncherState.NORMAL;
import static com.android.launcher3.dragndrop.DragLayer.ALPHA_INDEX_LAUNCHER_LOAD;
import static com.android.launcher3.logging.LoggerUtils.newContainerTarget;
import static com.android.launcher3.logging.LoggerUtils.newTarget;
import static com.android.launcher3.util.SystemUiController.UI_STATE_OVERVIEW;

public class LauncherDelegate implements LauncherExterns,
        LauncherModel.Callbacks, LauncherProviderChangeListener, UserEventDispatcher.UserEventDelegate, WallpaperColorInfo.OnChangeListener, IBaseActivity, IBaseDraggingActivity {
    // We only want to get the SharedPreferences once since it does an FS stat each time we get
    // it from the context.
    private SharedPreferences mSharedPrefs;
    private final Handler mHandler = new Handler();
    // private final ArrayList<MultiWindowModeChangedListener> mMultiWindowModeChangedListeners = new ArrayList<>();
    private final int[] mTmpAddItemCellCoordinates = new int[2];
    private final ArrayList<DeviceProfile.OnDeviceProfileChangeListener> mDPChangeListeners = new ArrayList<>();
    protected SystemUiController mSystemUiController;
    protected UserEventDispatcher mUserEventDispatcher;
    protected DeviceProfile mDeviceProfile;
    protected boolean mIsSafeModeEnabled;
    @Thunk
    boolean mWorkspaceLoading = true;
    private ActionMode mCurrentActionMode;
    private OnStartCallback mOnStartCallback;
    private LauncherAppTransitionManager mAppTransitionManager;
    private OnResumeCallback mOnResumeCallback;
    private LauncherAccessibilityDelegate mAccessibilityDelegate;
    private int mSynchronouslyBoundPage = PagedView.INVALID_PAGE;
    // When the recents animation is running, the visibility of the Launcher is managed by the
    // animation
    @Launcher.InvisibilityFlags
    private int mForceInvisible;
    @Launcher.ActivityFlags
    private int mActivityFlags;
    private ViewOnDrawExecutor mPendingExecutor;
    /**
     * Holds extra information required to handle a result from an external call, like
     * { #startActivityForResult(Intent, int)} or { #requestPermissions(String[], int)}
     */
    private PendingRequestArgs mPendingRequestArgs;
    // Activity result which needs to be processed after workspace has loaded.
    private ActivityResultInfo mPendingActivityResult;
    private LauncherCallbacks mLauncherCallbacks;
    private PopupDataProvider mPopupDataProvider;
    private Configuration mOldConfig;
    private RotationHelper mRotationHelper;
    @Thunk
    private DragLayer mDragLayer;
    @Thunk
    private Hotseat mHotseat;
    // Main container view for the all apps screen.
    @Thunk
    private AllAppsContainerView mAppsView;
    private AllAppsTransitionController mAllAppsController;
    private ViewGroupFocusHelper mFocusHandler;
    private Launcher mActivity;
    private View mLauncherView;
    private @Thunk
    Workspace mWorkspace;
    // UI and state for the overview panel
    private View mOverviewPanel;
    @Nullable
    private View mHotseatSearchBox;
    private DragController mDragController;
    private DropTargetBar mDropTargetBar;
    private AppWidgetManagerCompat mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;
    private LauncherModel mModel;
    private IconCache mIconCache;
    private ModelWriter mModelWriter;
    private DisplayRotationListener mRotationListener;
    private int mThemeRes = R.style.AppTheme;
    private LauncherStateManager mStateManager;
    private final Runnable mLogOnDelayedResume = this::logOnDelayedResume;
    private final BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Reset AllApps to its initial state only if we are not in the middle of
            // processing a multi-step drop
            if (getPendingRequestArgs() == null) {
                getStateManager().goToState(NORMAL);
            }
        }
    };
    public LauncherDelegate(Launcher launcher) {
        mActivity = launcher;
        mOldConfig = new Configuration(mActivity.getResources().getConfiguration());
        mAccessibilityDelegate = new LauncherAccessibilityDelegate(launcher);

        UiFactory.onCreate(launcher);
    }

    public static Launcher fromIBaseActivityContext(Context context) {
        if (context instanceof Launcher) {
            return (Launcher) context;
        }
        return ((Launcher) ((ContextWrapper) context).getBaseContext());
    }

    public static Launcher fromBaseDraggingActivityContext(Context context) {
        if (context instanceof Launcher) {
            return (Launcher) context;
        }
        return ((Launcher) ((ContextWrapper) context).getBaseContext());
    }

    public static Launcher getLauncher(Context context) {
        if (context instanceof Launcher) {
            return (Launcher) context;
        }
        return ((Launcher) ((ContextWrapper) context).getBaseContext());
    }

    public ActionMode getCurrentActionMode() {
        return mCurrentActionMode;
    }

    public void setCurrentActionMode(ActionMode currentActionMode) {
        mCurrentActionMode = currentActionMode;
    }

    public BroadcastReceiver getScreenOffReceiver() {
        return mScreenOffReceiver;
    }

    private void logOnDelayedResume() {
        if (hasBeenResumed()) {
            getUserEventDispatcher().logActionCommand(LauncherLogProto.Action.Command.RESUME,
                    getStateManager().getState().containerType, -1);
            getUserEventDispatcher().startSession();
        }
    }

    public void setSynchronouslyBoundPage(int synchronouslyBoundPage) {
        mSynchronouslyBoundPage = synchronouslyBoundPage;
    }

    public int getForceInvisible() {
        return mForceInvisible;
    }

    public int getActivityFlags() {
        return mActivityFlags;
    }

    public void setActivityFlags(int activityFlags) {
        mActivityFlags = activityFlags;
    }

    public PendingRequestArgs getPendingRequestArgs() {
        return mPendingRequestArgs;
    }

    public void setPendingRequestArgs(PendingRequestArgs pendingRequestArgs) {
        mPendingRequestArgs = pendingRequestArgs;
    }

    public ActivityResultInfo getPendingActivityResult() {
        return mPendingActivityResult;
    }

    public void setPendingActivityResult(ActivityResultInfo pendingActivityResult) {
        mPendingActivityResult = pendingActivityResult;
    }

    public LauncherCallbacks getLauncherCallbacks() {
        return mLauncherCallbacks;
    }

    public Configuration getOldConfig() {
        return mOldConfig;
    }

    public LauncherStateManager getStateManager() {
        return mStateManager;
    }

    public int getThemeRes() {
        return mThemeRes;
    }

    public boolean isSafeModeEnabled() {
        return mIsSafeModeEnabled;
    }

    public DisplayRotationListener getRotationListener() {
        return mRotationListener;
    }

    protected void dispatchDeviceProfileChanged() {
        for (int i = mDPChangeListeners.size() - 1; i >= 0; i--) {
            mDPChangeListeners.get(i).onDeviceProfileChanged(mDeviceProfile);
        }
    }

    @Override
    public boolean removeItem(View v, ItemInfo itemInfo, boolean deleteFromDb) {
        if (itemInfo instanceof ShortcutInfo) {
            // Remove the shortcut from the folder before removing it from launcher
            View folderIcon = getWorkspace().getHomescreenIconByItemId(itemInfo.container);
            if (folderIcon instanceof FolderIcon) {
                ((FolderInfo) folderIcon.getTag()).remove((ShortcutInfo) itemInfo, true);
            } else {
                getWorkspace().removeWorkspaceItem(v);
            }
            if (deleteFromDb) {
                getModelWriter().deleteItemFromDatabase(itemInfo);
            }
        } else if (itemInfo instanceof FolderInfo) {
            final FolderInfo folderInfo = (FolderInfo) itemInfo;
            if (v instanceof FolderIcon) {
                ((FolderIcon) v).removeListeners();
            }
            getWorkspace().removeWorkspaceItem(v);
            if (deleteFromDb) {
                getModelWriter().deleteFolderAndContentsFromDatabase(folderInfo);
            }
        } else if (itemInfo instanceof LauncherAppWidgetInfo) {
            final LauncherAppWidgetInfo widgetInfo = (LauncherAppWidgetInfo) itemInfo;
            getWorkspace().removeWorkspaceItem(v);
            if (deleteFromDb) {
                deleteWidgetInfo(widgetInfo);
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void addPendingItem(PendingAddItemInfo info, long container, long screenId, int[] cell, int spanX, int spanY) {
        info.container = container;
        info.screenId = screenId;
        if (cell != null) {
            info.cellX = cell[0];
            info.cellY = cell[1];
        }
        info.spanX = spanX;
        info.spanY = spanY;

        switch (info.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_CUSTOM_APPWIDGET:
            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                addAppWidgetFromDrop((PendingAddWidgetInfo) info);
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                processShortcutFromDrop((PendingAddShortcutInfo) info);
                break;
            default:
                throw new IllegalStateException("Unknown item type: " + info.itemType);
        }
    }

    /**
     * Process a shortcut drop.
     */
    private void processShortcutFromDrop(PendingAddShortcutInfo info) {
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT).setComponent(info.componentName);
        setPendingRequestArgs(PendingRequestArgs.forIntent(Constant.REQUEST_CREATE_SHORTCUT, intent, info));
        if (!info.activityInfo.startConfigActivity(mActivity, Constant.REQUEST_CREATE_SHORTCUT)) {
            handleActivityResult(Constant.REQUEST_CREATE_SHORTCUT, RESULT_CANCELED, null);
        }
    }

    void addAppWidgetFromDropImpl(int appWidgetId, ItemInfo info, AppWidgetHostView boundWidget,
                                  WidgetAddFlowHandler addFlowHandler) {
        if (Constant.LOGD) {
            Log.d(Constant.TAG, "Adding widget from drop");
        }
        addAppWidgetImpl(appWidgetId, info, boundWidget, addFlowHandler, 0);
    }

    /**
     * Process a widget drop.
     */
    private void addAppWidgetFromDrop(PendingAddWidgetInfo info) {
        AppWidgetHostView hostView = info.boundWidget;
        final int appWidgetId;
        WidgetAddFlowHandler addFlowHandler = info.getHandler();
        if (hostView != null) {
            // In the case where we've prebound the widget, we remove it from the DragLayer
            if (Constant.LOGD) {
                Log.d(Constant.TAG, "Removing widget view from drag layer and setting boundWidget to null");
            }
            getDragLayer().removeView(hostView);

            appWidgetId = hostView.getAppWidgetId();
            addAppWidgetFromDropImpl(appWidgetId, info, hostView, addFlowHandler);

            // Clear the boundWidget so that it doesn't get destroyed.
            info.boundWidget = null;
        } else {
            // In this case, we either need to start an activity to get permission to bind
            // the widget, or we need to start an activity to configure the widget, or both.
            if (FeatureFlags.ENABLE_CUSTOM_WIDGETS &&
                    info.itemType == LauncherSettings.Favorites.ITEM_TYPE_CUSTOM_APPWIDGET) {
                appWidgetId = CustomWidgetParser.getWidgetIdForCustomProvider(
                        mActivity, info.componentName);
            } else {
                appWidgetId = getAppWidgetHost().allocateAppWidgetId(info.info.widgetId);
            }

            addAppWidgetFromDropImpl(appWidgetId, info, null, addFlowHandler);
        }
    }

    @Override
    public FolderIcon addFolder(CellLayout layout, long container, final long screenId, int cellX,
                                int cellY) {
        final FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = mActivity.getText(R.string.folder_name);

        // Update the model
        getModelWriter().addItemToDatabase(folderInfo, container, screenId, cellX, cellY);

        // Create the view
        FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, mActivity, layout, folderInfo);
        getWorkspace().addInScreen(newFolder, folderInfo);
        // Force measure the new folder icon
        CellLayout parent = getWorkspace().getParentCellLayoutForView(newFolder);
        parent.getShortcutsAndWidgets().measureChild(newFolder);
        return newFolder;
    }

    @Override
    public void bindAppWidget(LauncherAppWidgetInfo item) {
        View view = inflateAppWidget(item);
        if (view != null) {
            getWorkspace().addInScreen(view, item);
            getWorkspace().requestLayout();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PendingRequestArgs pendingArgs = getPendingRequestArgs();
        if (requestCode == Constant.REQUEST_PERMISSION_CALL_PHONE && pendingArgs != null
                && pendingArgs.getRequestCode() == Constant.REQUEST_PERMISSION_CALL_PHONE) {
            setPendingRequestArgs(null);

            View v = null;
            CellLayout layout = getCellLayout(pendingArgs.container, pendingArgs.screenId);
            if (layout != null) {
                v = layout.getChildAt(pendingArgs.cellX, pendingArgs.cellY);
            }
            Intent intent = pendingArgs.getPendingIntent();

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivitySafely(v, intent, null);
            } else {
                // TODO: Show a snack bar with link to settings
                Toast.makeText(mActivity, mActivity.getString(R.string.msg_no_phone_permission,
                        mActivity.getString(R.string.derived_app_name)), Toast.LENGTH_SHORT).show();
            }
        }
        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onRequestPermissionsResult(requestCode, permissions,
                    grantResults);
        }
    }

    @Override
    public void invalidateParent(ItemInfo info) {
        FolderIconPreviewVerifier verifier = new FolderIconPreviewVerifier(getDeviceProfile().inv);
        if (verifier.isItemInPreview(info.rank) && (info.container >= 0)) {
            View folderIcon = getWorkspace().getHomescreenIconByItemId(info.container);
            if (folderIcon != null) {
                folderIcon.invalidate();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        handleActivityResult(requestCode, resultCode, data);
        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onActivityResult(requestCode, resultCode, data);
        }
    }

    void addAppWidgetImpl(int appWidgetId, ItemInfo info,
                          AppWidgetHostView boundWidget, WidgetAddFlowHandler addFlowHandler, int delay) {
        if (!addFlowHandler.startConfigActivity(mActivity, appWidgetId, info, Constant.REQUEST_CREATE_APPWIDGET)) {
            // If the configuration flow was not started, add the widget

            Runnable onComplete = new Runnable() {
                @Override
                public void run() {
                    // Exit spring loaded mode if necessary after adding the widget
                    getStateManager().goToState(NORMAL, SPRING_LOADED_EXIT_DELAY);
                }
            };
            completeAddAppWidget(appWidgetId, info, boundWidget, addFlowHandler.getProviderInfo(mActivity));
            getWorkspace().removeExtraEmptyScreenDelayed(true, onComplete, delay, false);
        }
    }

    void handleActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
        if (isWorkspaceLoading()) {
            // process the result once the workspace has loaded.
            mPendingActivityResult = new ActivityResultInfo(requestCode, resultCode, data);
            return;
        }
        mPendingActivityResult = null;

        // Reset the startActivity waiting flag
        final PendingRequestArgs requestArgs = mPendingRequestArgs;
        setPendingRequestArgs(null);
        if (requestArgs == null) {
            return;
        }

        final int pendingAddWidgetId = requestArgs.getWidgetId();

        Runnable exitSpringLoaded = new Runnable() {
            @Override
            public void run() {
                getStateManager().goToState(NORMAL, SPRING_LOADED_EXIT_DELAY);
            }
        };

        if (requestCode == Constant.REQUEST_BIND_APPWIDGET) {
            // This is called only if the user did not previously have permissions to bind widgets
            final int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_CANCELED) {
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId, requestArgs);
                getWorkspace().removeExtraEmptyScreenDelayed(true, exitSpringLoaded,
                        Constant.ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
            } else if (resultCode == RESULT_OK) {
                addAppWidgetImpl(
                        appWidgetId, requestArgs, null,
                        requestArgs.getWidgetHandler(),
                        Constant.ON_ACTIVITY_RESULT_ANIMATION_DELAY);
            }
            return;
        }

        boolean isWidgetDrop = (requestCode == Constant.REQUEST_PICK_APPWIDGET ||
                requestCode == Constant.REQUEST_CREATE_APPWIDGET);

        // We have special handling for widgets
        if (isWidgetDrop) {
            final int appWidgetId;
            int widgetId = data != null ? data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    : -1;
            if (widgetId < 0) {
                appWidgetId = pendingAddWidgetId;
            } else {
                appWidgetId = widgetId;
            }

            final int result;
            if (appWidgetId < 0 || resultCode == RESULT_CANCELED) {
                Log.e(Constant.TAG, "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not " +
                        "returned from the widget configuration activity.");
                result = RESULT_CANCELED;
                completeTwoStageWidgetDrop(result, appWidgetId, requestArgs);
                final Runnable onComplete = new Runnable() {
                    @Override
                    public void run() {
                        getStateManager().goToState(NORMAL);
                    }
                };

                getWorkspace().removeExtraEmptyScreenDelayed(true, onComplete,
                        Constant.ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
            } else {
                if (requestArgs.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    // When the screen id represents an actual screen (as opposed to a rank)
                    // we make sure that the drop page actually exists.
                    requestArgs.screenId =
                            ensurePendingDropLayoutExists(requestArgs.screenId);
                }
                final CellLayout dropLayout =
                        getWorkspace().getScreenWithId(requestArgs.screenId);

                dropLayout.setDropPending(true);
                final Runnable onComplete = new Runnable() {
                    @Override
                    public void run() {
                        completeTwoStageWidgetDrop(resultCode, appWidgetId, requestArgs);
                        dropLayout.setDropPending(false);
                    }
                };
                getWorkspace().removeExtraEmptyScreenDelayed(true, onComplete,
                        Constant.ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
            }
            return;
        }

        if (requestCode == Constant.REQUEST_RECONFIGURE_APPWIDGET
                || requestCode == Constant.REQUEST_BIND_PENDING_APPWIDGET) {
            if (resultCode == RESULT_OK) {
                // Update the widget view.
                completeAdd(requestCode, data, pendingAddWidgetId, requestArgs);
            }
            // Leave the widget in the pending state if the user canceled the configure.
            return;
        }

        if (requestCode == Constant.REQUEST_CREATE_SHORTCUT) {
            // Handle custom shortcuts created using ACTION_CREATE_SHORTCUT.
            if (resultCode == RESULT_OK && requestArgs.container != ItemInfo.NO_ID) {
                completeAdd(requestCode, data, -1, requestArgs);
                getWorkspace().removeExtraEmptyScreenDelayed(true, exitSpringLoaded,
                        Constant.ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);

            } else if (resultCode == RESULT_CANCELED) {
                getWorkspace().removeExtraEmptyScreenDelayed(true, exitSpringLoaded,
                        Constant.ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
            }
        }
        getDragLayer().clearAnimatedView();
    }

    @Thunk
    void completeTwoStageWidgetDrop(
            final int resultCode, final int appWidgetId, final PendingRequestArgs requestArgs) {
        CellLayout cellLayout = getWorkspace().getScreenWithId(requestArgs.screenId);
        Runnable onCompleteRunnable = null;
        int animationType = 0;

        AppWidgetHostView boundWidget = null;
        if (resultCode == RESULT_OK) {
            animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
            final AppWidgetHostView layout = getAppWidgetHost().createView(mActivity, appWidgetId,
                    requestArgs.getWidgetHandler().getProviderInfo(mActivity));
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    completeAddAppWidget(appWidgetId, requestArgs, layout, null);
                    getStateManager().goToState(NORMAL, SPRING_LOADED_EXIT_DELAY);
                }
            };
        } else if (resultCode == RESULT_CANCELED) {
            getAppWidgetHost().deleteAppWidgetId(appWidgetId);
            animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
        }
        if (getDragLayer().getAnimatedView() != null) {
            getWorkspace().animateWidgetDrop(requestArgs, cellLayout,
                    (DragView) getDragLayer().getAnimatedView(), onCompleteRunnable,
                    animationType, boundWidget, true);
        } else if (onCompleteRunnable != null) {
            // The animated view may be null in the case of a rotation during widget configuration
            onCompleteRunnable.run();
        }
    }

    /**
     * Add a widget to the workspace.
     *
     * @param appWidgetId The app widget id
     */
    @Thunk
    void completeAddAppWidget(int appWidgetId, ItemInfo itemInfo,
                              AppWidgetHostView hostView, LauncherAppWidgetProviderInfo appWidgetInfo) {

        if (appWidgetInfo == null) {
            appWidgetInfo = getAppWidgetManager().getLauncherAppWidgetInfo(appWidgetId);
        }

        LauncherAppWidgetInfo launcherInfo;
        launcherInfo = new LauncherAppWidgetInfo(appWidgetId, appWidgetInfo.provider);
        launcherInfo.spanX = itemInfo.spanX;
        launcherInfo.spanY = itemInfo.spanY;
        launcherInfo.minSpanX = itemInfo.minSpanX;
        launcherInfo.minSpanY = itemInfo.minSpanY;
        launcherInfo.user = appWidgetInfo.getProfile();

        getModelWriter().addItemToDatabase(launcherInfo,
                itemInfo.container, itemInfo.screenId, itemInfo.cellX, itemInfo.cellY);

        if (hostView == null) {
            // Perform actual inflation because we're live
            hostView = getAppWidgetHost().createView(mActivity, appWidgetId, appWidgetInfo);
        }
        hostView.setVisibility(View.VISIBLE);
        prepareAppWidget(hostView, launcherInfo);
        getWorkspace().addInScreen(hostView, launcherInfo);
    }

    private void prepareAppWidget(AppWidgetHostView hostView, LauncherAppWidgetInfo item) {
        hostView.setTag(item);
        item.onBindAppWidget(mActivity, hostView);
        hostView.setFocusable(true);
        hostView.setOnFocusChangeListener(getFocusHandler());
    }

    /**
     * Returns whether we should delay spring loaded mode -- for shortcuts and widgets that have
     * a configuration step, this allows the proper animations to run after other transitions.
     */
    private long completeAdd(
            int requestCode, Intent intent, int appWidgetId, PendingRequestArgs info) {
        long screenId = info.screenId;
        if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            // When the screen id represents an actual screen (as opposed to a rank) we make sure
            // that the drop page actually exists.
            screenId = ensurePendingDropLayoutExists(info.screenId);
        }

        switch (requestCode) {
            case Constant.REQUEST_CREATE_SHORTCUT:
                completeAddShortcut(intent, info.container, screenId, info.cellX, info.cellY, info);
                break;
            case Constant.REQUEST_CREATE_APPWIDGET:
                completeAddAppWidget(appWidgetId, info, null, null);
                break;
            case Constant.REQUEST_RECONFIGURE_APPWIDGET:
                completeRestoreAppWidget(appWidgetId, LauncherAppWidgetInfo.RESTORE_COMPLETED);
                break;
            case Constant.REQUEST_BIND_PENDING_APPWIDGET: {
                int widgetId = appWidgetId;
                LauncherAppWidgetInfo widgetInfo =
                        completeRestoreAppWidget(widgetId, LauncherAppWidgetInfo.FLAG_UI_NOT_READY);
                if (widgetInfo != null) {
                    // Since the view was just bound, also launch the configure activity if needed
                    LauncherAppWidgetProviderInfo provider = getAppWidgetManager()
                            .getLauncherAppWidgetInfo(widgetId);
                    if (provider != null) {
                        new WidgetAddFlowHandler(provider)
                                .startConfigActivity(mActivity, widgetInfo, Constant.REQUEST_RECONFIGURE_APPWIDGET);
                    }
                }
                break;
            }
        }

        return screenId;
    }

    /**
     * Returns the CellLayout of the specified container at the specified screen.
     */
    public CellLayout getCellLayout(long container, long screenId) {
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            if (getHotseat() != null) {
                return getHotseat().getLayout();
            } else {
                return null;
            }
        } else {
            return getWorkspace().getScreenWithId(screenId);
        }
    }

    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut((ViewGroup) getWorkspace().getChildAt(getWorkspace().getCurrentPage()), info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param parent The group the shortcut belongs to.
     * @param info   The data structure describing the shortcut.
     * @return A View inflated from layoutResId.
     */
    public View createShortcut(ViewGroup parent, ShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_icon, parent, false);
        favorite.applyFromShortcutInfo(info);
        favorite.setOnClickListener(ItemClickHandler.INSTANCE);
        favorite.setOnFocusChangeListener(getFocusHandler());
        return favorite;
    }

    /**
     * Add a shortcut to the workspace or to a Folder.
     *
     * @param data The intent describing the shortcut.
     */
    private void completeAddShortcut(Intent data, long container, long screenId, int cellX,
                                     int cellY, PendingRequestArgs args) {
        if (args.getRequestCode() != Constant.REQUEST_CREATE_SHORTCUT
                || args.getPendingIntent().getComponent() == null) {
            return;
        }

        int[] cellXY = mTmpAddItemCellCoordinates;
        CellLayout layout = getCellLayout(container, screenId);

        ShortcutInfo info = null;
        if (Utilities.ATLEAST_OREO) {
            info = LauncherAppsCompatVO.createShortcutInfoFromPinItemRequest(
                    mActivity, LauncherAppsCompatVO.getPinItemRequest(data), 0);
        }

        if (info == null) {
            // Legacy shortcuts are only supported for primary profile.
            info = Process.myUserHandle().equals(args.user)
                    ? null : null;

            if (info == null) {
                Log.e(Constant.TAG, "Unable to parse a valid custom shortcut result");
                return;
            } else if (!new PackageManagerHelper(mActivity).hasPermissionForActivity(
                    info.intent, args.getPendingIntent().getComponent().getPackageName())) {
                // The app is trying to add a shortcut without sufficient permissions
                Log.e(Constant.TAG, "Ignoring malicious intent " + info.intent.toUri(0));
                return;
            }
        }

        if (container < 0) {
            // Adding a shortcut to the Workspace.
            final View view = createShortcut(info);
            boolean foundCellSpan = false;
            // First we check if we already know the exact location where we want to add this item.
            if (cellX >= 0 && cellY >= 0) {
                cellXY[0] = cellX;
                cellXY[1] = cellY;
                foundCellSpan = true;

                // If appropriate, either create a folder or add to an existing folder
                if (getWorkspace().createUserFolderIfNecessary(view, container, layout, cellXY, 0,
                        true, null)) {
                    return;
                }
                DropTarget.DragObject dragObject = new DropTarget.DragObject();
                dragObject.dragInfo = info;
                if (getWorkspace().addToExistingFolderIfNecessary(view, layout, cellXY, 0, dragObject,
                        true)) {
                    return;
                }
            } else {
                foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
            }

            if (!foundCellSpan) {
                getWorkspace().onNoCellFound(layout);
                return;
            }

            getModelWriter().addItemToDatabase(info, container, screenId, cellXY[0], cellXY[1]);
            getWorkspace().addInScreen(view, info);
        } else {
            // Adding a shortcut to a Folder.
            FolderIcon folderIcon = findFolderIcon(container);
            if (folderIcon != null) {
                FolderInfo folderInfo = (FolderInfo) folderIcon.getTag();
                folderInfo.add(info, args.rank, false);
            } else {
                Log.e(Constant.TAG, "Could not find folder with id " + container + " to add shortcut.");
            }
        }
    }

    @Override
    public void refreshAndBindWidgetsForPackageUser(@Nullable PackageUserKey packageUser) {
        getModel().refreshAndBindWidgetsAndShortcuts(packageUser);
    }

    @Override
    public int getOrientation() {
        return getOldConfig().orientation;
    }

    @Override
    public void onAttachedToWindow() {
        FirstFrameAnimatorHelper.initializeDrawListener(mActivity.getWindow().getDecorView());
        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onAttachedToWindow();
        }
    }

    @Override
    public boolean isInState(LauncherState state) {
        return getStateManager().getState() == state;
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        setCurrentActionMode(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        setCurrentActionMode(null);
    }

    @Override
    public void onSaveInstanceStateBeforeSuper(Bundle outState) {
        if (getWorkspace().getChildCount() > 0) {
            outState.putInt(Constant.RUNTIME_STATE_CURRENT_SCREEN, getWorkspace().getNextPage());

        }
        outState.putInt(Constant.RUNTIME_STATE, getStateManager().getState().ordinal);


        AbstractFloatingView widgets = AbstractFloatingView
                .getOpenView(mActivity, AbstractFloatingView.TYPE_WIDGETS_FULL_SHEET);
        if (widgets != null) {
            SparseArray<Parcelable> widgetsState = new SparseArray<>();
            widgets.saveHierarchyState(widgetsState);
            outState.putSparseParcelableArray(Constant.RUNTIME_STATE_WIDGET_PANEL, widgetsState);
        } else {
            outState.remove(Constant.RUNTIME_STATE_WIDGET_PANEL);
        }

        // We close any open folders and shortcut containers since they will not be re-opened,
        // and we need to make sure this state is reflected.
        AbstractFloatingView.closeAllOpenViews(mActivity, false);

        if (getPendingRequestArgs() != null) {
            outState.putParcelable(Constant.RUNTIME_STATE_PENDING_REQUEST_ARGS, getPendingRequestArgs());
        }
        if (getPendingActivityResult() != null) {
            outState.putParcelable(Constant.RUNTIME_STATE_PENDING_ACTIVITY_RESULT, getPendingActivityResult());
        }
    }

    @Override
    public void onSaveInstanceStateAfterSuper(Bundle outState) {
        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onSaveInstanceState(outState);
        }
    }

    @Override
    public void updateIconBadges(Set<PackageUserKey> updatedBadges) {
        getWorkspace().updateIconBadges(updatedBadges);
        getAppsView().getAppsStore().updateIconBadges(updatedBadges);

        PopupContainerWithArrow popup = PopupContainerWithArrow.getOpen(mActivity);
        if (popup != null) {
            popup.updateNotificationHeader(updatedBadges);
        }
    }

    @Override
    public void onUserLeaveHintBefore() {
        int activityFlags = getActivityFlags();
        activityFlags &= ~Constant.ACTIVITY_STATE_USER_ACTIVE;
        setActivityFlags(activityFlags);
    }

    @Override
    public void onPauseBeforeSuper() {
        // Ensure that items added to Launcher are queued until Launcher returns
        // InstallShortcutReceiver.enableInstallQueue(InstallShortcutReceiver.FLAG_ACTIVITY_PAUSED);
        int activityFlags = getActivityFlags();
        activityFlags &= ~Constant.ACTIVITY_STATE_RESUMED;
        setActivityFlags(activityFlags);
    }

    @Override
    public void onPauseAfterSuper() {
        // Reset the overridden sysui flags used for the task-swipe launch animation, we do this
        // here instead of at the end of the animation because the start of the new activity does
        // not happen immediately, which would cause us to reset to launcher's sysui flags and then
        // back to the new app (causing a flash)
        getSystemUiController().updateUiState(UI_STATE_OVERVIEW, 0);
        getDragController().cancelDrag();
        getDragController().resetLastGestureUpTime();

        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onPause();
        }
    }

    @Override
    public void onResumeBeforeSuper() {
        int activityFlags = getActivityFlags();
        activityFlags |= Constant.ACTIVITY_STATE_RESUMED | Constant.ACTIVITY_STATE_USER_ACTIVE;
        setActivityFlags(activityFlags);
    }

    @Override
    public void onResumeAfterSuper() {
        mHandler.removeCallbacks(mLogOnDelayedResume);
        Utilities.postAsyncCallback(mHandler, mLogOnDelayedResume);

        setOnResumeCallback(null);
        // Process any items that were added while Launcher was away.
        // InstallShortcutReceiver.disableAndFlushInstallQueue(
        //         InstallShortcutReceiver.FLAG_ACTIVITY_PAUSED, this);

        // Refresh shortcuts if the permission changed.
        getModel().refreshShortcutsIfRequired();

        DiscoveryBounce.showForHomeIfNeeded(mActivity);
        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onResume();
        }
        UiFactory.onLauncherStateOrResumeChanged(mActivity);
    }

    @Override
    public void onStartBeforeSuper() {
        int activityFlags = getActivityFlags();
        activityFlags |= Constant.ACTIVITY_STATE_STARTED;
        setActivityFlags(activityFlags);
    }

    @Override
    public void onStartAfterSuper() {
        if (mOnStartCallback != null) {
            mOnStartCallback.onActivityStart(mActivity);
            mOnStartCallback = null;
        }
        FirstFrameAnimatorHelper.setIsVisible(true);

        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onStart();
        }
        getAppWidgetHost().setListenIfResumed(true);
        NotificationListener.setNotificationsChangedListener(getPopupDataProvider());
        UiFactory.onStart(mActivity);
    }

    @Override
    public void onStopBeforeSuper() {
        int activityFlags = getActivityFlags();
        activityFlags &= ~Constant.ACTIVITY_STATE_STARTED & ~Constant.ACTIVITY_STATE_USER_ACTIVE;
        setActivityFlags(activityFlags);
        setForceInvisible(0);
    }

    @Override
    public void onStopAfterSuper() {
        FirstFrameAnimatorHelper.setIsVisible(false);

        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onStop();
        }
        getUserEventDispatcher().logActionCommand(LauncherLogProto.Action.Command.STOP,
                getStateManager().getState().containerType, -1);

        getAppWidgetHost().setListenIfResumed(false);

        NotificationListener.removeNotificationsChangedListener();
        getStateManager().moveToRestState();

        UiFactory.onLauncherStateOrResumeChanged(mActivity);

        // Workaround for b/78520668, explicitly trim memory once UI is hidden
        onTrimMemory(TRIM_MEMORY_UI_HIDDEN);
    }

    @Override
    public void onDestroyAfterSuper() {
        WallpaperColorInfo.getInstance(mActivity).removeOnChangeListener(this);
        getRotationListener().disable();

        mActivity.unregisterReceiver(mScreenOffReceiver);
        getWorkspace().removeFolderListeners();

        UiFactory.setOnTouchControllersChangedListener(mActivity, null);

        // Stop callbacks from LauncherModel
        // It's possible to receive onDestroy after a new Launcher activity has
        // been created. In this case, don't interfere with the new Launcher.
        if (getModel().isCurrentCallbacks(this)) {
            getModel().stopLoader();
            LauncherAppState.getInstance(mActivity).setLauncher(null);
        }
        getRotationHelper().destroy();

        try {
            getAppWidgetHost().stopListening();
        } catch (NullPointerException ex) {
            Log.w(Constant.TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }

        TextKeyListener.getInstance().release();

        LauncherAnimUtils.onDestroyActivity();

        clearPendingBinds();

        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onDestroy();
        }
    }
    /**
     * Restores the previous state, if it exists.
     *
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }

        int stateOrdinal = savedState.getInt(Constant.RUNTIME_STATE, NORMAL.ordinal);
        LauncherState[] stateValues = LauncherState.values();
        LauncherState state = stateValues[stateOrdinal];
        if (!state.disableRestore) {
            getStateManager().goToState(state, false /* animated */);
        }

        PendingRequestArgs requestArgs = savedState.getParcelable(Constant.RUNTIME_STATE_PENDING_REQUEST_ARGS);
        if (requestArgs != null) {
            setPendingRequestArgs(requestArgs);
        }

        setPendingActivityResult(savedState.getParcelable(Constant.RUNTIME_STATE_PENDING_ACTIVITY_RESULT));

        SparseArray<Parcelable> widgetsState =
                savedState.getSparseParcelableArray(Constant.RUNTIME_STATE_WIDGET_PANEL);
        if (widgetsState != null) {
            WidgetsFullSheet.show(mActivity, false).restoreHierarchyState(widgetsState);
        }
    }
    @Override
    public void onCreateAfterSuper(Bundle savedInstanceState) {
        mSharedPrefs = Utilities.getPrefs(mActivity);

        boolean internalStateHandled = InternalStateHandler.handleCreate(mActivity, mActivity.getIntent());
        if (internalStateHandled) {
            if (savedInstanceState != null) {
                // InternalStateHandler has already set the appropriate state.
                // We dont need to do anything.
                savedInstanceState.remove(Constant.RUNTIME_STATE);
            }
        }
        restoreState(savedInstanceState);

        // We only load the page synchronously if the user rotates (or triggers a
        // configuration change) while launcher is in the foreground
        int currentScreen = PagedView.INVALID_RESTORE_PAGE;
        if (savedInstanceState != null) {
            currentScreen = savedInstanceState.getInt(Constant.RUNTIME_STATE_CURRENT_SCREEN, currentScreen);
        }

        if (!getModel().startLoader(currentScreen)) {
            if (!internalStateHandled) {
                // If we are not binding synchronously, show a fade in animation when
                // the first page bind completes.
                getDragLayer().getAlphaProperty(ALPHA_INDEX_LAUNCHER_LOAD).setValue(0);
            }
        } else {
            // Pages bound synchronously.
            getWorkspace().setCurrentPage(currentScreen);

            setWorkspaceLoading(true);
        }

        // For handling default keys
        mActivity.setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        mActivity.setContentView(getLauncherView());
        ((LauncherRootView) getLauncherView()).dispatchInsets();

        // Listen for broadcasts
        mActivity.registerReceiver(getScreenOffReceiver(), new IntentFilter(Intent.ACTION_SCREEN_OFF));

        getSystemUiController().updateUiState(SystemUiController.UI_STATE_BASE_WINDOW,
                Themes.getAttrBoolean(mActivity, R.attr.isWorkspaceDarkText));

        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onCreate(savedInstanceState);
        }
        getRotationHelper().initialize();
    }

    @Override
    public boolean isWorkspaceLocked() {
        return isWorkspaceLoading() || getPendingRequestArgs() != null;
    }

    @Override
    public boolean isHotseatLayout(View layout) {
        // TODO: Remove this method
        return getHotseat() != null && layout != null &&
                (layout instanceof CellLayout) && (layout == getHotseat().getLayout());
    }

    @Override
    public void onTrimMemory(int level) {
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            // The widget preview db can result in holding onto over
            // 3MB of memory for caching which isn't necessary.
            SQLiteDatabase.releaseMemory();

            // This clears all widget bitmaps from the widget tray
            // TODO(hyunyoungs)
        }
        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onTrimMemory(level);
        }
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        final List<CharSequence> text = event.getText();
        text.clear();
        // Populate event with a fake title based on the current state.
        // TODO: When can workspace be null?
        text.add(getWorkspace() == null
                ? mActivity.getString(R.string.all_apps_home_button_label)
                : getStateManager().getState().getDescription(mActivity));
        return false;
    }

    @Override
    public void setOnResumeCallback(OnResumeCallback callback) {
        if (mOnResumeCallback != null) {
            mOnResumeCallback.onLauncherResume();
        }
        mOnResumeCallback = callback;
    }

    @Override
    public void onBackPressed() {
        if (finishAutoCancelActionMode()) {
            return;
        }
        if (getLauncherCallbacks() != null && getLauncherCallbacks().handleBackPressed()) {
            return;
        }

        if (getDragController().isDragging()) {
            getDragController().cancelDrag();
            return;
        }

        // Note: There should be at most one log per method call. This is enforced implicitly
        // by using if-else statements.
        UserEventDispatcher ued = getUserEventDispatcher();
        AbstractFloatingView topView = AbstractFloatingView.getTopOpenView(mActivity);
        if (topView != null && topView.onBackPressed()) {
            // Handled by the floating view.
        } else if (!mActivity.isInState(NORMAL)) {
            LauncherState lastState = getStateManager().getLastState();
            ued.logActionCommand(LauncherLogProto.Action.Command.BACK, getStateManager().getState().containerType,
                    lastState.containerType);
            getStateManager().goToState(lastState);
        } else {
            // Back button is a no-op here, but give at least some feedback for the button press
            getWorkspace().showOutlinesTemporarily();
        }
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        if (event.hasModifiers(KeyEvent.META_CTRL_ON)) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_A:
                    if (mActivity.isInState(NORMAL)) {
                        getStateManager().goToState(ALL_APPS);
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_S: {
                    View focusedView = mActivity.getCurrentFocus();
                    if (focusedView instanceof BubbleTextView
                            && focusedView.getTag() instanceof ItemInfo
                            && mAccessibilityDelegate.performAction(focusedView,
                            (ItemInfo) focusedView.getTag(),
                            LauncherAccessibilityDelegate.DEEP_SHORTCUTS)) {
                        PopupContainerWithArrow.getOpen(mActivity).requestFocus();
                        return true;
                    }
                    break;
                }
                case KeyEvent.KEYCODE_O:
                    if (new CustomActionsPopup(mActivity, mActivity.getCurrentFocus()).show()) {
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_W:
                    if (mActivity.isInState(NORMAL)) {
                        OptionsPopupView.openWidgets(mActivity);
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // KEYCODE_MENU is sent by some tests, for example
            // LauncherJankTests#testWidgetsContainerFling. Don't just remove its handling.
            if (!getDragController().isDragging() && !getWorkspace().isSwitchingState() &&
                    mActivity.isInState(NORMAL)) {
                // Close any open floating views.
                AbstractFloatingView.closeAllOpenViews(mActivity);

                // Setting the touch point to (-1, -1) will show the options popup in the center of
                // the screen.
                OptionsPopupView.showDefaultOptions(mActivity, -1, -1);
            }
            return true;
        }
        return false;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.N)
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {
        ArrayList<KeyboardShortcutInfo> shortcutInfos = new ArrayList<>();
        if (mActivity.isInState(NORMAL)) {
            shortcutInfos.add(new KeyboardShortcutInfo(mActivity.getString(R.string.all_apps_button_label),
                    KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON));
            shortcutInfos.add(new KeyboardShortcutInfo(mActivity.getString(R.string.widget_button_text),
                    KeyEvent.KEYCODE_W, KeyEvent.META_CTRL_ON));
        }
        final View currentFocus = mActivity.getCurrentFocus();
        if (currentFocus != null) {
            if (new CustomActionsPopup(mActivity, currentFocus).canShow()) {
                shortcutInfos.add(new KeyboardShortcutInfo(mActivity.getString(R.string.custom_actions),
                        KeyEvent.KEYCODE_O, KeyEvent.META_CTRL_ON));
            }
            if (currentFocus.getTag() instanceof ItemInfo
                    && DeepShortcutManager.supportsShortcuts((ItemInfo) currentFocus.getTag())) {
                shortcutInfos.add(new KeyboardShortcutInfo(
                        mActivity.getString(R.string.shortcuts_menu_with_notifications_description),
                        KeyEvent.KEYCODE_S, KeyEvent.META_CTRL_ON));
            }
        }
        if (!shortcutInfos.isEmpty()) {
            data.add(new KeyboardShortcutGroup(mActivity.getString(R.string.home_screen), shortcutInfos));
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        getWorkspace().restoreInstanceStateForChild(mSynchronouslyBoundPage);
    }

    @Override
    public void onNewIntent(Intent intent) {
        boolean alreadyOnHome = mActivity.hasWindowFocus() && ((intent.getFlags() &
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        // Check this condition before handling isActionMain, as this will get reset.
        boolean shouldMoveToDefaultScreen = alreadyOnHome && mActivity.isInState(NORMAL)
                && AbstractFloatingView.getTopOpenView(mActivity) == null;
        boolean isActionMain = Intent.ACTION_MAIN.equals(intent.getAction());
        boolean internalStateHandled = InternalStateHandler
                .handleNewIntent(mActivity, intent, isStarted());

        if (isActionMain) {
            if (!internalStateHandled) {
                // Note: There should be at most one log per method call. This is enforced
                // implicitly by using if-else statements.
                UserEventDispatcher ued = getUserEventDispatcher();
                AbstractFloatingView topOpenView = AbstractFloatingView.getTopOpenView(mActivity);
                if (topOpenView != null) {
                    topOpenView.logActionCommand(LauncherLogProto.Action.Command.HOME_INTENT);
                } else if (alreadyOnHome) {
                    LauncherLogProto.Target target = newContainerTarget(getStateManager().getState().containerType);
                    target.pageIndex = getWorkspace().getCurrentPage();
                    ued.logActionCommand(LauncherLogProto.Action.Command.HOME_INTENT, target,
                            newContainerTarget(LauncherLogProto.ContainerType.WORKSPACE));
                }

                // In all these cases, only animate if we're already on home
                AbstractFloatingView.closeAllOpenViews(mActivity, isStarted());

                if (!mActivity.isInState(NORMAL)) {
                    // Only change state, if not already the same. This prevents cancelling any
                    // animations running as part of resume
                    getStateManager().goToState(NORMAL);
                }

                // Reset the apps view
                if (!alreadyOnHome) {
                    getAppsView().reset(isStarted() /* animate */);
                }

                if (shouldMoveToDefaultScreen && !getWorkspace().isTouchActive()) {
                    getWorkspace().post(getWorkspace()::moveToDefaultScreen);
                }
            }

            final View v = mActivity.getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                UiThreadHelper.hideKeyboardAsync(mActivity, v.getWindowToken());
            }

            if (getLauncherCallbacks() != null) {
                getLauncherCallbacks().onHomeIntent(internalStateHandled);
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onDetachedFromWindow();
        }
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        if (!UiFactory.dumpActivity(mActivity, writer)) {
            mActivity.dump(prefix, fd, writer, args);
        }

        if (args.length > 0 && TextUtils.equals(args[0], "--all")) {
            writer.println(prefix + "Workspace Items");
            for (int i = 0; i < getWorkspace().getPageCount(); i++) {
                writer.println(prefix + "  Homescreen " + i);

                ViewGroup layout = ((CellLayout) getWorkspace().getPageAt(i)).getShortcutsAndWidgets();
                for (int j = 0; j < layout.getChildCount(); j++) {
                    Object tag = layout.getChildAt(j).getTag();
                    if (tag != null) {
                        writer.println(prefix + "    " + tag.toString());
                    }
                }
            }

            writer.println(prefix + "  Hotseat");
            ViewGroup layout = getHotseat().getLayout().getShortcutsAndWidgets();
            for (int j = 0; j < layout.getChildCount(); j++) {
                Object tag = layout.getChildAt(j).getTag();
                if (tag != null) {
                    writer.println(prefix + "    " + tag.toString());
                }
            }
        }

        writer.println(prefix + "Misc:");
        writer.print(prefix + "\tmWorkspaceLoading=" + isWorkspaceLoading());
        writer.print(" mPendingRequestArgs=" + getPendingRequestArgs());
        writer.println(" mPendingActivityResult=" + getPendingActivityResult());
        writer.println(" mRotationHelper: " + getRotationHelper());
        dumpMisc(writer);

        try {
            FileLog.flushAll(writer);
        } catch (Exception e) {
            // Ignore
        }

        getModel().dumpState(prefix, fd, writer, args);

        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().dump(prefix, fd, writer, args);
        }
    }

    protected void dumpMisc(PrintWriter writer) {
        writer.println(" deviceProfile isTransposed=" + getDeviceProfile().isVerticalBarLayout());
        writer.println(" orientation=" + mActivity.getResources().getConfiguration().orientation);
        writer.println(" mSystemUiController: " + mSystemUiController);
        writer.println(" mActivityFlags: " + mActivityFlags);
        writer.println(" mForceInvisible: " + mForceInvisible);
    }

    @Override
    public void clearPendingExecutor(ViewOnDrawExecutor executor) {
        if (getPendingExecutor() == executor) {
            setPendingExecutor(null);
        }
    }

    public FolderIcon findFolderIcon(final long folderIconId) {
        return (FolderIcon) getWorkspace().getFirstMatch(new Workspace.ItemOperator() {
            @Override
            public boolean evaluate(ItemInfo info, View view) {
                return info != null && info.id == folderIconId;
            }
        });
    }

    /**
     * Restores a pending widget.
     *
     * @param appWidgetId The app widget id
     */
    private LauncherAppWidgetInfo completeRestoreAppWidget(int appWidgetId, int finalRestoreFlag) {
        LauncherAppWidgetHostView view = getWorkspace().getWidgetForAppWidgetId(appWidgetId);
        if (!(view instanceof PendingAppWidgetHostView)) {
            Log.e(Constant.TAG, "Widget update called, when the widget no longer exists.");
            return null;
        }

        LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) view.getTag();
        info.restoreStatus = finalRestoreFlag;
        if (info.restoreStatus == LauncherAppWidgetInfo.RESTORE_COMPLETED) {
            info.pendingItemInfo = null;
        }

        if (((PendingAppWidgetHostView) view).isReinflateIfNeeded()) {
            view.reInflate();
        }

        getModelWriter().updateItemInDatabase(info);
        return info;
    }

    /**
     * Check to see if a given screen id exists. If not, create it at the end, return the new id.
     *
     * @param screenId the screen id to check
     * @return the new screen, or screenId if it exists
     */
    private long ensurePendingDropLayoutExists(long screenId) {
        CellLayout dropLayout = getWorkspace().getScreenWithId(screenId);
        if (dropLayout == null) {
            // it's possible that the add screen was removed because it was
            // empty and a re-bind occurred
            getWorkspace().addExtraEmptyScreen();
            return getWorkspace().commitExtraEmptyScreen();
        } else {
            return screenId;
        }
    }

    // We prevent dragging when we are loading the workspace as it is possible to pick up a view
    // that is subsequently removed from the workspace in startBinding().
    @Override
    public boolean isDraggingEnabled() {
        return !isWorkspaceLoading();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int diff = newConfig.diff(mOldConfig);
        if ((diff & (CONFIG_ORIENTATION | CONFIG_SCREEN_SIZE)) != 0) {
            mUserEventDispatcher = null;
            initDeviceProfile(mDeviceProfile.inv);
            dispatchDeviceProfileChanged();
            reapplyUi();
            mDragLayer.recreateControllers();

            // TODO: We can probably avoid rebind when only screen size changed.
            rebindModel();
        }

        mOldConfig.setTo(newConfig);
        UiFactory.onLauncherStateOrResumeChanged(mActivity);
    }

    private void reapplyUi() {
        ((LauncherRootView) (mLauncherView)).dispatchInsets();
        mStateManager.reapplyState(true /* cancelCurrentAnimation */);
    }

    private void onDeviceRotationChanged() {
        if (getDeviceProfile().updateIsSeascape(mActivity.getWindowManager())) {
            reapplyUi();
        }
    }

    public ModelWriter getModelWriter() {
        return mModelWriter;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    public boolean isWorkspaceLoading() {
        return mWorkspaceLoading;
    }

    public void setWorkspaceLoading(boolean workspaceLoading) {
        mWorkspaceLoading = workspaceLoading;
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public AppWidgetManagerCompat getAppWidgetManager() {
        return mAppWidgetManager;
    }

    public void init() {
        mDragController = new DragController(mActivity);
        mAppWidgetManager = AppWidgetManagerCompat.getInstance(mActivity);
        mAppWidgetHost = new LauncherAppWidgetHost(mActivity);
        mAppWidgetHost.startListening();
        LauncherAppState app = LauncherAppState.getInstance(mActivity);
        mModel = app.setLauncher(mActivity);
        mIsSafeModeEnabled = mActivity.getPackageManager().isSafeMode();
        mRotationListener = new DisplayRotationListener(mActivity, this::onDeviceRotationChanged);
        initDeviceProfile(app.getInvariantDeviceProfile());
        mIconCache = app.getIconCache();
        mStateManager = new LauncherStateManager(mActivity);

        mAllAppsController = new AllAppsTransitionController(mActivity);
        mLauncherView = LayoutInflater.from(mActivity).inflate(R.layout.launcher, null);
        mDragLayer = mLauncherView.findViewById(R.id.drag_layer);
        mFocusHandler = mDragLayer.getFocusIndicatorHelper();
        mWorkspace = mDragLayer.findViewById(R.id.workspace);
        mWorkspace.initParentViews(mDragLayer);
        mOverviewPanel = mLauncherView.findViewById(R.id.overview_panel);
        mHotseat = mLauncherView.findViewById(R.id.hotseat);
        mHotseatSearchBox = mLauncherView.findViewById(R.id.search_container_hotseat);

        mLauncherView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // Setup the drag layer
        mDragLayer.setup(mDragController, mWorkspace);
        UiFactory.setOnTouchControllersChangedListener(mActivity, mDragLayer::recreateControllers);

        mWorkspace.setup(mDragController);
        // Until the workspace is bound, ensure that we keep the wallpaper offset locked to the
        // default state, otherwise we will update to the wrong offsets in RTL
        mWorkspace.lockWallpaperToDefaultPage();
        mWorkspace.bindAndInitFirstWorkspaceScreen(null /* recycled qsb */);
        mDragController.addDragListener(mWorkspace);

        // Get the search/delete/uninstall bar
        mDropTargetBar = mDragLayer.findViewById(R.id.drop_target_bar);

        // Setup Apps
        mAppsView = mLauncherView.findViewById(R.id.apps_view);

        // Setup the drag controller (drop targets have to be added in reverse order in priority)
        mDragController.setMoveTarget(mWorkspace);
        mDropTargetBar.setup(mDragController);

        mAllAppsController.setupViews(mAppsView);

        // Update theme
        WallpaperColorInfo wallpaperColorInfo = WallpaperColorInfo.getInstance(mActivity);
        wallpaperColorInfo.addOnChangeListener(this);
        int themeRes = getThemeRes(wallpaperColorInfo);
        if (themeRes != mThemeRes) {
            mThemeRes = themeRes;
            mActivity.setTheme(themeRes);
        }

        mPopupDataProvider = new PopupDataProvider(mActivity);
        mRotationHelper = new RotationHelper(mActivity);
        mAppTransitionManager = LauncherAppTransitionManager.newInstance(mActivity);
    }

    public View getOverviewPanel() {
        return mOverviewPanel;
    }

    public AllAppsTransitionController getAllAppsController() {
        return mAllAppsController;
    }

    @Nullable
    public View getHotseatSearchBox() {
        return mHotseatSearchBox;
    }

    public AllAppsContainerView getAppsView() {
        return mAppsView;
    }

    public DropTargetBar getDropTargetBar() {
        return mDropTargetBar;
    }

    public Hotseat getHotseat() {
        return mHotseat;
    }

    public DragController getDragController() {
        return mDragController;
    }

    public Workspace getWorkspace() {
        return mWorkspace;
    }

    public ViewGroupFocusHelper getFocusHandler() {
        return mFocusHandler;
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    public View getLauncherView() {
        return mLauncherView;
    }

    void initDeviceProfile(InvariantDeviceProfile idp) {
        mDeviceProfile = idp.getDeviceProfile(mActivity);
        if (isInMultiWindowModeCompat()) {
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            Point mwSize = new Point();
            display.getSize(mwSize);
            mDeviceProfile = mDeviceProfile.getMultiWindowProfile(mActivity, mwSize);
        }
        onDeviceProfileInitiated();
        mModelWriter = mModel.getWriter(mDeviceProfile.isVerticalBarLayout(), true);
    }

    protected void onDeviceProfileInitiated() {
        if (mDeviceProfile.isVerticalBarLayout()) {
            mRotationListener.enable();
            mDeviceProfile.updateIsSeascape(mActivity.getWindowManager());
        } else {
            mRotationListener.disable();
        }
    }

    @Override
    public DeviceProfile getDeviceProfile() {
        return mDeviceProfile;
    }

    @Override
    public LauncherAccessibilityDelegate getAccessibilityDelegate() {
        return mAccessibilityDelegate;
    }

    @Override
    public UserEventDispatcher getUserEventDispatcher() {
        if (mUserEventDispatcher == null) {
            mUserEventDispatcher = UserEventDispatcher.newInstance(mActivity, mDeviceProfile, this);
        }
        return mUserEventDispatcher;
    }

    @Override
    public boolean isInMultiWindowModeCompat() {
        return false;
    }

    @Override
    public SystemUiController getSystemUiController() {
        if (mSystemUiController == null) {
            mSystemUiController = new SystemUiController(mActivity.getWindow());
        }
        return mSystemUiController;
    }

    @Override
    public boolean isStarted() {
        return  (getActivityFlags() & Constant.ACTIVITY_STATE_STARTED) != 0;
    }

    @Override
    public boolean hasBeenResumed() {
        return (getActivityFlags() & Constant.ACTIVITY_STATE_RESUMED) != 0;
    }

    @Override
    public void addOnDeviceProfileChangeListener(DeviceProfile.OnDeviceProfileChangeListener listener) {
        mDPChangeListeners.add(listener);
    }

    @Override
    public boolean isForceInvisible() {
        return hasSomeInvisibleFlag(Constant.INVISIBLE_FLAGS);
    }

    public void setForceInvisible(int forceInvisible) {
        mForceInvisible = forceInvisible;
    }

    @Override
    public boolean hasSomeInvisibleFlag(int mask) {
        return (getForceInvisible() & mask) != 0;
    }

    @Override
    public boolean finishAutoCancelActionMode() {
        if (mCurrentActionMode != null && Constant.AUTO_CANCEL_ACTION_MODE == mCurrentActionMode.getTag()) {
            mCurrentActionMode.finish();
            return true;
        }
        return false;
    }

    @Override
    public Rect getViewBounds(View v) {
        int[] pos = new int[2];
        v.getLocationOnScreen(pos);
        return new Rect(pos[0], pos[1], pos[0] + v.getWidth(), pos[1] + v.getHeight());
    }

    @TargetApi(Build.VERSION_CODES.M)
    public ActivityOptions getActivityLaunchOptions(View v) {
        return mAppTransitionManager.getActivityLaunchOptions(mActivity, v);
    }
    
    @Override
    public Bundle getActivityLaunchOptionsAsBundle(View v) {
        ActivityOptions activityOptions = getActivityLaunchOptions(v);
        return activityOptions == null ? null : activityOptions.toBundle();
    }

    private void startShortcutIntentSafely(Intent intent, Bundle optsBundle, ItemInfo info) {
        try {
            StrictMode.VmPolicy oldPolicy = StrictMode.getVmPolicy();
            try {
                // Temporarily disable deathPenalty on all default checks. For eg, shortcuts
                // containing file Uri's would cause a crash as penaltyDeathOnFileUriExposure
                // is enabled by default on NYC.
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                        .penaltyLog().build());

                if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT) {
                    String id = ((ShortcutInfo) info).getDeepShortcutId();
                    String packageName = intent.getPackage();
                    DeepShortcutManager.getInstance(mActivity).startShortcut(
                            packageName, id, intent.getSourceBounds(), optsBundle, info.user);
                } else {
                    // Could be launching some bookkeeping activity
                    mActivity.startActivity(intent, optsBundle);
                }
            } finally {
                StrictMode.setVmPolicy(oldPolicy);
            }
        } catch (SecurityException e) {
            if (!onErrorStartingShortcut(intent, info)) {
                throw e;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected boolean onErrorStartingShortcut(Intent intent, ItemInfo info) {
        // Due to legacy reasons, direct call shortcuts require Launchers to have the
        // corresponding permission. Show the appropriate permission prompt if that
        // is the case.
        if (intent.getComponent() == null
                && Intent.ACTION_CALL.equals(intent.getAction())
                && mActivity.checkSelfPermission(android.Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {

            setPendingRequestArgs(PendingRequestArgs
                    .forIntent(Constant.REQUEST_PERMISSION_CALL_PHONE, intent, info));
            mActivity.requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE},
                    Constant.REQUEST_PERMISSION_CALL_PHONE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean startActivitySafely(View v, Intent intent, ItemInfo item) {
        boolean success = false;
        if (isSafeModeEnabled() && !Utilities.isSystemApp(mActivity, intent)) {
            Toast.makeText(mActivity, R.string.safemode_shortcut_error, Toast.LENGTH_SHORT).show();
        } else {// Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            boolean useLaunchAnimation = (v != null) &&
                    !intent.hasExtra(Constant.INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
            Bundle optsBundle = useLaunchAnimation
                    ? getActivityLaunchOptionsAsBundle(v)
                    : null;
            UserHandle user = item == null ? null : item.user;// Prepare intent
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (v != null) {
                intent.setSourceBounds(getViewBounds(v));
            }
            try {
                boolean isShortcut = Utilities.ATLEAST_MARSHMALLOW
                        && (item instanceof ShortcutInfo)
                        && (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
                        || item.itemType == LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT)
                        && !((ShortcutInfo) item).isPromise();
                if (isShortcut) {
                    // Shortcuts need some special checks due to legacy reasons.
                    startShortcutIntentSafely(intent, optsBundle, item);
                } else if (user == null || user.equals(Process.myUserHandle())) {
                    // Could be launching some bookkeeping activity
                    mActivity.startActivity(intent, optsBundle);
                } else {
                    LauncherAppsCompat.getInstance(mActivity).startActivityForProfile(
                            intent.getComponent(), user, intent.getSourceBounds(), optsBundle);
                }
                getUserEventDispatcher().logAppLaunch(v, intent);
                success = true;
            } catch (ActivityNotFoundException | SecurityException e) {
                Toast.makeText(mActivity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
                Log.e("BaseDraggingActivity", "Unable to launch. tag=" + item + " intent=" + intent, e);
            }
        }

        if (success && v instanceof BubbleTextView) {
            // This is set to the view that launched the activity that navigated the user away
            // from launcher. Since there is no callback for when the activity has finished
            // launching, enable the press state and keep this reference to reset the press
            // state when we return to launcher.
            BubbleTextView btv = (BubbleTextView) v;
            btv.setStayPressed(true);
            setOnResumeCallback(btv);
        }
        return success;
    }

    @Override
    public SharedPreferences getSharedPrefs() {
        return mSharedPrefs;
    }

    /*public boolean setLauncherCallbacks(LauncherCallbacks callbacks) {
        mLauncherCallbacks = callbacks;
        return true;
    }*/

    @Override
    public void rebindModel() {
        int currentPage = mWorkspace.getNextPage();
        if (mModel.startLoader(currentPage)) {
            mWorkspace.setCurrentPage(currentPage);
            setWorkspaceLoading(true);
        }
    }

    /*public void setLauncherOverlay(LauncherOverlay overlay) {
        if (overlay != null) {
            overlay.setOverlayCallbacks(new LauncherOverlayCallbacksImpl());
        }
        mWorkspace.setLauncherOverlay(overlay);
    }*/

    @Override
    public int getCurrentWorkspaceScreen() {
        if (getWorkspace() != null) {
            return getWorkspace().getCurrentPage();
        } else {
            return 0;
        }
    }

    @Override
    public void clearPendingBinds() {
        if (getPendingExecutor() != null) {
            getPendingExecutor().markCompleted();
            setPendingExecutor(null);
        }
    }

    @Override
    public void startBinding() {
        TraceHelper.beginSection("startBinding");
        // Floating panels (except the full widget sheet) are associated with individual icons. If
        // we are starting a fresh bind, close all such panels as all the icons are about
        // to go away.
        AbstractFloatingView.closeOpenViews(mActivity, true,
                AbstractFloatingView.TYPE_ALL & ~AbstractFloatingView.TYPE_REBIND_SAFE);

        setWorkspaceLoading(true);

        // Clear the workspace because it's going to be rebound
        getWorkspace().clearDropTargets();
        getWorkspace().removeAllWorkspaceScreens();
        getAppWidgetHost().clearViews();

        if (getHotseat() != null) {
            getHotseat().resetLayout(getDeviceProfile().isVerticalBarLayout());
        }
        TraceHelper.endSection("startBinding");
    }

    private boolean canRunNewAppsAnimation() {
        long diff = System.currentTimeMillis() - getDragController().getLastGestureUpTime();
        return diff > (Constant.NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000);
    }

    private ValueAnimator createNewAppBounceAnimation(View v, int i) {
        ValueAnimator bounceAnim = LauncherAnimUtils.ofViewAlphaAndScale(v, 1, 1, 1);
        bounceAnim.setDuration(450);
        bounceAnim.setStartDelay(i * 85);
        bounceAnim.setInterpolator(new OvershootInterpolator(Constant.BOUNCE_ANIMATION_TENSION));
        return bounceAnim;
    }

    private View inflateAppWidget(LauncherAppWidgetInfo item) {
        if (isSafeModeEnabled()) {
            PendingAppWidgetHostView view =
                    new PendingAppWidgetHostView(mActivity, item, getIconCache(), true);
            prepareAppWidget(view, item);
            return view;
        }

        TraceHelper.beginSection("BIND_WIDGET");

        final LauncherAppWidgetProviderInfo appWidgetInfo;

        if (item.hasRestoreFlag(LauncherAppWidgetInfo.FLAG_PROVIDER_NOT_READY)) {
            // If the provider is not ready, bind as a pending widget.
            appWidgetInfo = null;
        } else if (item.hasRestoreFlag(LauncherAppWidgetInfo.FLAG_ID_NOT_VALID)) {
            // The widget id is not valid. Try to find the widget based on the provider info.
            appWidgetInfo = getAppWidgetManager().findProvider(item.providerName, item.user);
        } else {
            appWidgetInfo = getAppWidgetManager().getLauncherAppWidgetInfo(item.appWidgetId);
        }

        // If the provider is ready, but the width is not yet restored, try to restore it.
        if (!item.hasRestoreFlag(LauncherAppWidgetInfo.FLAG_PROVIDER_NOT_READY) &&
                (item.restoreStatus != LauncherAppWidgetInfo.RESTORE_COMPLETED)) {
            if (appWidgetInfo == null) {
                Log.d(Constant.TAG, "Removing restored widget: id=" + item.appWidgetId
                        + " belongs to component " + item.providerName
                        + ", as the provider is null");
                getModelWriter().deleteItemFromDatabase(item);
                return null;
            }

            // If we do not have a valid id, try to bind an id.
            if (item.hasRestoreFlag(LauncherAppWidgetInfo.FLAG_ID_NOT_VALID)) {
                if (!item.hasRestoreFlag(LauncherAppWidgetInfo.FLAG_ID_ALLOCATED)) {
                    // Id has not been allocated yet. Allocate a new id.
                    item.appWidgetId = getAppWidgetHost().allocateAppWidgetId(item.appWidgetId);
                    item.restoreStatus |= LauncherAppWidgetInfo.FLAG_ID_ALLOCATED;

                    // Also try to bind the widget. If the bind fails, the user will be shown
                    // a click to setup UI, which will ask for the bind permission.
                    PendingAddWidgetInfo pendingInfo = new PendingAddWidgetInfo(appWidgetInfo);
                    pendingInfo.spanX = item.spanX;
                    pendingInfo.spanY = item.spanY;
                    pendingInfo.minSpanX = item.minSpanX;
                    pendingInfo.minSpanY = item.minSpanY;
                    Bundle options = WidgetHostViewLoader.getDefaultOptionsForWidget(mActivity, pendingInfo);

                    boolean isDirectConfig =
                            item.hasRestoreFlag(LauncherAppWidgetInfo.FLAG_DIRECT_CONFIG);
                    if (isDirectConfig && item.bindOptions != null) {
                        Bundle newOptions = item.bindOptions.getExtras();
                        newOptions.putAll(options);
                        // options = newOptions;
                    }

                    // We tried to bind once. If we were not able to bind, we would need to
                    // go through the permission dialog, which means we cannot skip the config
                    // activity.
                    item.bindOptions = null;
                    item.restoreStatus &= ~LauncherAppWidgetInfo.FLAG_DIRECT_CONFIG;

                    // Bind succeeded
                    // If the widget has a configure activity, it is still needs to set it up,
                    // otherwise the widget is ready to go.
                    item.restoreStatus = (appWidgetInfo.configure == null) || isDirectConfig
                            ? LauncherAppWidgetInfo.RESTORE_COMPLETED
                            : LauncherAppWidgetInfo.FLAG_UI_NOT_READY;

                    getModelWriter().updateItemInDatabase(item);
                }
            } else if (item.hasRestoreFlag(LauncherAppWidgetInfo.FLAG_UI_NOT_READY)
                    && (appWidgetInfo.configure == null)) {
                // The widget was marked as UI not ready, but there is no configure activity to
                // update the UI.
                item.restoreStatus = LauncherAppWidgetInfo.RESTORE_COMPLETED;
                getModelWriter().updateItemInDatabase(item);
            }
        }

        final AppWidgetHostView view;
        if (item.restoreStatus == LauncherAppWidgetInfo.RESTORE_COMPLETED) {
            // Verify that we own the widget
            if (appWidgetInfo == null) {
                FileLog.e(Constant.TAG, "Removing invalid widget: id=" + item.appWidgetId);
                deleteWidgetInfo(item);
                return null;
            }

            item.minSpanX = appWidgetInfo.minSpanX;
            item.minSpanY = appWidgetInfo.minSpanY;
            view = getAppWidgetHost().createView(mActivity, item.appWidgetId, appWidgetInfo);
        } else {
            view = new PendingAppWidgetHostView(mActivity, item, getIconCache(), false);
        }
        prepareAppWidget(view, item);

        TraceHelper.endSection("BIND_WIDGET", "id=" + item.appWidgetId);
        return view;
    }

    /**
     * Deletes the widget info and the widget id.
     */
    private void deleteWidgetInfo(final LauncherAppWidgetInfo widgetInfo) {
        final LauncherAppWidgetHost appWidgetHost = getAppWidgetHost();
        if (appWidgetHost != null && !widgetInfo.isCustomWidget() && widgetInfo.isWidgetIdAllocated()) {
            // Deleting an app widget ID is a void call but writes to disk before returning
            // to the caller...
            new AsyncTask<Void, Void, Void>() {
                public Void doInBackground(Void... args) {
                    appWidgetHost.deleteAppWidgetId(widgetInfo.appWidgetId);
                    return null;
                }
            }.executeOnExecutor(Utilities.THREAD_POOL_EXECUTOR);
        }
        getModelWriter().deleteItemFromDatabase(widgetInfo);
    }

    @Override
    public void bindItems(List<ItemInfo> items, boolean forceAnimateIcons) {
        // Get the list of added items and intersect them with the set of items here
        final AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        final Collection<Animator> bounceAnims = new ArrayList<>();
        final boolean animateIcons = forceAnimateIcons && canRunNewAppsAnimation();
        Workspace workspace = getWorkspace();
        long newItemsScreenId = -1;
        int end = items.size();
        for (int i = 0; i < end; i++) {
            final ItemInfo item = items.get(i);

            // Short circuit if we are loading dock items for a configuration which has no dock
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                    getHotseat() == null) {
                continue;
            }

            final View view;
            switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                case LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT: {
                    ShortcutInfo info = (ShortcutInfo) item;
                    view = createShortcut(info);
                    break;
                }
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER: {
                    view = FolderIcon.fromXml(R.layout.folder_icon, mActivity,
                            (ViewGroup) workspace.getChildAt(workspace.getCurrentPage()),
                            (FolderInfo) item);
                    break;
                }
                case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                case LauncherSettings.Favorites.ITEM_TYPE_CUSTOM_APPWIDGET: {
                    view = inflateAppWidget((LauncherAppWidgetInfo) item);
                    if (view == null) {
                        continue;
                    }
                    break;
                }
                default:
                    throw new RuntimeException("Invalid Item Type");
            }

            /*
             * Remove colliding items.
             */
            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                CellLayout cl = getWorkspace().getScreenWithId(item.screenId);
                if (cl != null && cl.isOccupied(item.cellX, item.cellY)) {
                    View v = cl.getChildAt(item.cellX, item.cellY);
                    Object tag = v.getTag();
                    String desc = "Collision while binding workspace item: " + item
                            + ". Collides with " + tag;
                    if (FeatureFlags.IS_DOGFOOD_BUILD) {
                        throw (new RuntimeException(desc));
                    } else {
                        Log.d(Constant.TAG, desc);
                        getModelWriter().deleteItemFromDatabase(item);
                        continue;
                    }
                }
            }
            workspace.addInScreenFromBind(view, item);
            if (animateIcons) {
                // Animate all the applications up now
                view.setAlpha(0f);
                view.setScaleX(0f);
                view.setScaleY(0f);
                bounceAnims.add(createNewAppBounceAnimation(view, i));
                newItemsScreenId = item.screenId;
            }
        }

        if (animateIcons) {
            // Animate to the correct page
            if (newItemsScreenId > -1) {
                long currentScreenId = getWorkspace().getScreenIdForPageIndex(getWorkspace().getNextPage());
                final int newScreenIndex = getWorkspace().getPageIndexForScreenId(newItemsScreenId);
                final Runnable startBounceAnimRunnable = new Runnable() {
                    public void run() {
                        anim.playTogether(bounceAnims);
                        anim.start();
                    }
                };
                if (newItemsScreenId != currentScreenId) {
                    // We post the animation slightly delayed to prevent slowdowns
                    // when we are loading right after we return to launcher.
                    getWorkspace().postDelayed(new Runnable() {
                        public void run() {
                            if (getWorkspace() != null) {
                                AbstractFloatingView.closeAllOpenViews(mActivity, false);

                                getWorkspace().snapToPage(newScreenIndex);
                                getWorkspace().postDelayed(startBounceAnimRunnable,
                                        Constant.NEW_APPS_ANIMATION_DELAY);
                            }
                        }
                    }, Constant.NEW_APPS_PAGE_MOVE_DELAY);
                } else {
                    getWorkspace().postDelayed(startBounceAnimRunnable, Constant.NEW_APPS_ANIMATION_DELAY);
                }
            }
        }
        workspace.requestLayout();
    }

    @Override
    public void bindScreens(ArrayList<Long> orderedScreenIds) {
        // Make sure the first screen is always at the start.
        if (FeatureFlags.QSB_ON_FIRST_SCREEN &&
                orderedScreenIds.indexOf(Workspace.FIRST_SCREEN_ID) != 0) {
            orderedScreenIds.remove(Workspace.FIRST_SCREEN_ID);
            orderedScreenIds.add(0, Workspace.FIRST_SCREEN_ID);
            LauncherModel.updateWorkspaceScreenOrder(mActivity, orderedScreenIds);
        } else if (!FeatureFlags.QSB_ON_FIRST_SCREEN && orderedScreenIds.isEmpty()) {
            // If there are no screens, we need to have an empty screen
            getWorkspace().addExtraEmptyScreen();
        }
        bindAddScreens(orderedScreenIds);

        // After we have added all the screens, if the wallpaper was locked to the default state,
        // then notify to indicate that it can be released and a proper wallpaper offset can be
        // computed before the next layout
        getWorkspace().unlockWallpaperFromDefaultPageOnNextLayout();
    }

    private void bindAddScreens(ArrayList<Long> orderedScreenIds) {
        int count = orderedScreenIds.size();
        for (int i = 0; i < count; i++) {
            long screenId = orderedScreenIds.get(i);
            if (!FeatureFlags.QSB_ON_FIRST_SCREEN || screenId != Workspace.FIRST_SCREEN_ID) {
                // No need to bind the first screen, as its always bound.
                getWorkspace().insertNewWorkspaceScreenBeforeEmptyScreen(screenId);
            }
        }
    }

    @Override
    public void finishFirstPageBind(ViewOnDrawExecutor executor) {
        MultiValueAlpha.AlphaProperty property = getDragLayer().getAlphaProperty(ALPHA_INDEX_LAUNCHER_LOAD);
        if (property.getValue() < 1) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(property, MultiValueAlpha.VALUE, 1);
            if (executor != null) {
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        executor.onLoadAnimationCompleted();
                    }
                });
            }
            anim.start();
        } else if (executor != null) {
            executor.onLoadAnimationCompleted();
        }
    }

    @Override
    public void finishBindingItems() {
        TraceHelper.beginSection("finishBindingItems");
        getWorkspace().restoreInstanceStateForRemainingPages();

        setWorkspaceLoading(false);

        if (getPendingActivityResult() != null) {
            handleActivityResult(getPendingActivityResult().requestCode,
                    getPendingActivityResult().resultCode, getPendingActivityResult().data);
            setPendingActivityResult(null);
        }

        // InstallShortcutReceiver.disableAndFlushInstallQueue(
        //         InstallShortcutReceiver.FLAG_LOADER_RUNNING, this);

        TraceHelper.endSection("finishBindingItems");
    }

    @Override
    public void bindAllApplications(ArrayList<AppInfo> apps) {
        getAppsView().getAppsStore().setApps(apps);

        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().bindAllApplications(apps);
        }
    }

    @Override
    public void bindAppsAddedOrUpdated(ArrayList<AppInfo> apps) {

    }

    @Override
    public void bindAppsAdded(ArrayList<Long> newScreens, ArrayList<ItemInfo> addNotAnimated, ArrayList<ItemInfo> addAnimated) {
        // Add the new screens
        if (newScreens != null) {
            bindAddScreens(newScreens);
        }

        // We add the items without animation on non-visible pages, and with
        // animations on the new page (which we will try and snap to).
        if (addNotAnimated != null && !addNotAnimated.isEmpty()) {
            bindItems(addNotAnimated, false);
        }
        if (addAnimated != null && !addAnimated.isEmpty()) {
            bindItems(addAnimated, true);
        }

        // Remove the extra empty screen
        getWorkspace().removeExtraEmptyScreen(false, false);
    }

    @Override
    public void bindPromiseAppProgressUpdated(PromiseAppInfo app) {

    }

    @Override
    public void bindShortcutsChanged(ArrayList<ShortcutInfo> updated, UserHandle user) {
        if (!updated.isEmpty()) {
            getWorkspace().updateShortcuts(updated);
        }
    }

    @Override
    public void bindWidgetsRestored(ArrayList<LauncherAppWidgetInfo> widgets) {

    }

    @Override
    public void bindRestoreItemsChange(HashSet<ItemInfo> updates) {

    }

    @Override
    public void bindWorkspaceComponentsRemoved(ItemInfoMatcher matcher) {
        getWorkspace().removeItemsByMatcher(matcher);
        getDragController().onAppsRemoved(matcher);
    }

    @Override
    public void bindAppInfosRemoved(ArrayList<AppInfo> appInfos) {
        getAppsView().getAppsStore().removeApps(appInfos);
    }

    @Override
    public void bindAllWidgets(ArrayList<WidgetListRowEntry> allWidgets) {
        getPopupDataProvider().setAllWidgets(allWidgets);
        AbstractFloatingView topView = AbstractFloatingView.getTopOpenView(mActivity);
        if (topView != null) {
            topView.onWidgetsBound();
        }
    }

    @Override
    public void onPageBoundSynchronously(int page) {

    }

    @Override
    public void executeOnNextDraw(ViewOnDrawExecutor executor) {
        if (getPendingExecutor() != null) {
            getPendingExecutor().markCompleted();
        }
        setPendingExecutor(executor);
        if (!mActivity.isInState(ALL_APPS)) {
            getAppsView().getAppsStore().setDeferUpdates(true);
            getPendingExecutor().execute(() -> getAppsView().getAppsStore().setDeferUpdates(false));
        }

        executor.attachTo(mActivity);
    }

    @Override
    public void bindDeepShortcutMap(MultiHashMap<ComponentKey, String> deepShortcutMap) {

    }

    @Override
    public void onLauncherProviderChanged() {
        if (getLauncherCallbacks() != null) {
            getLauncherCallbacks().onLauncherProviderChange();
        }
    }

    @Override
    public void onAppWidgetHostReset() {
        if (getAppWidgetHost() != null) {
            getAppWidgetHost().startListening();
        }
    }

    @Override
    public void modifyUserEvent(LauncherLogProto.LauncherEvent event) {
        if (event.srcTarget != null && event.srcTarget.length > 0 &&
                event.srcTarget[1].containerType == LauncherLogProto.ContainerType.PREDICTION) {
            LauncherLogProto.Target[] targets = new LauncherLogProto.Target[3];
            targets[0] = event.srcTarget[0];
            targets[1] = event.srcTarget[1];
            targets[2] = newTarget(LauncherLogProto.Target.Type.CONTAINER);
            event.srcTarget = targets;
            LauncherState state = getStateManager().getState();
            if (state == LauncherState.ALL_APPS) {
                event.srcTarget[2].containerType = LauncherLogProto.ContainerType.ALLAPPS;
            } else if (state == LauncherState.OVERVIEW) {
                event.srcTarget[2].containerType = LauncherLogProto.ContainerType.TASKSWITCHER;
            }
        }
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        if (getThemeRes() != getThemeRes(wallpaperColorInfo)) {
            mActivity.recreate();
        }
    }

    protected int getThemeRes(WallpaperColorInfo wallpaperColorInfo) {
        if (wallpaperColorInfo.isDark()) {
            return wallpaperColorInfo.supportsDarkText() ?
                    R.style.AppTheme_Dark_DarkText : R.style.AppTheme_Dark;
        } else {
            return wallpaperColorInfo.supportsDarkText() ?
                    R.style.AppTheme_DarkText : R.style.AppTheme;
        }
    }

    public PopupDataProvider getPopupDataProvider() {
        return mPopupDataProvider;
    }

    public RotationHelper getRotationHelper() {
        return mRotationHelper;
    }

    public ViewOnDrawExecutor getPendingExecutor() {
        return mPendingExecutor;
    }

    public void setPendingExecutor(ViewOnDrawExecutor pendingExecutor) {
        mPendingExecutor = pendingExecutor;
    }

    class LauncherOverlayCallbacksImpl implements LauncherOverlayCallbacks {

        public void onScrollChanged(float progress) {
            if (getWorkspace() != null) {
                getWorkspace().onOverlayScrollChanged(progress);
            }
        }
    }
}
