package com.android.launcher3;

import com.android.launcher3.util.Thunk;

public class Constant {
    public static final boolean LOGD = false;
    public static final boolean DEBUG_STRICT_MODE = false;
    public static final int REQUEST_CREATE_SHORTCUT = 1;
    public static final int REQUEST_CREATE_APPWIDGET = 5;
    public static final int REQUEST_PICK_APPWIDGET = 9;
    public static final int REQUEST_BIND_APPWIDGET = 11;
    public static final int REQUEST_BIND_PENDING_APPWIDGET = 12;
    public static final int REQUEST_RECONFIGURE_APPWIDGET = 13;
    public static final int REQUEST_PERMISSION_CALL_PHONE = 14;
    public static final float BOUNCE_ANIMATION_TENSION = 1.3f;
    /**
     * IntentStarter uses request codes starting with this. This must be greater than all activity
     * request codes used internally.
     */
    public static final int REQUEST_LAST = 100;
    // Type: int
    public static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: int
    public static final String RUNTIME_STATE = "launcher.state";
    // Type: PendingRequestArgs
    public static final String RUNTIME_STATE_PENDING_REQUEST_ARGS = "launcher.request_args";
    // Type: ActivityResultInfo
    public static final String RUNTIME_STATE_PENDING_ACTIVITY_RESULT = "launcher.activity_result";
    // Type: SparseArray<Parcelable>
    public static final String RUNTIME_STATE_WIDGET_PANEL = "launcher.widget_panel";
    public static final int ON_ACTIVITY_RESULT_ANIMATION_DELAY = 500;
    // How long to wait before the new-shortcut animation automatically pans the workspace
    public static final int NEW_APPS_PAGE_MOVE_DELAY = 500;
    public static final int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 5;
    @Thunk
    public static final int NEW_APPS_ANIMATION_DELAY = 500;
    public static final String TAG = "Launcher";
    public static final int INVISIBLE_BY_STATE_HANDLER = 1 << 0;
    public static final int INVISIBLE_BY_APP_TRANSITIONS = 1 << 1;
    public static final int INVISIBLE_BY_PENDING_FLAGS = 1 << 2;
    static final int INVISIBLE_FLAGS =
            INVISIBLE_BY_STATE_HANDLER | INVISIBLE_BY_APP_TRANSITIONS | INVISIBLE_BY_PENDING_FLAGS;
    // This is not treated as invisibility flag, but adds as a hint for an incomplete transition.
    // When the wallpaper animation runs, it replaces this flag with a proper invisibility
    // flag, INVISIBLE_BY_PENDING_FLAGS only for the duration of that animation.
    public static final int PENDING_INVISIBLE_BY_WALLPAPER_ANIMATION = 1 << 3;
    // The Intent extra that defines whether to ignore the launch animation
    public static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION =
            "com.android.launcher3.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";
    // When starting an action mode, setting this tag will cause the action mode to be cancelled
    // automatically when user interacts with the launcher.
    public static final Object AUTO_CANCEL_ACTION_MODE = new Object();
    static final int ACTIVITY_STATE_STARTED = 1 << 0;
    static final int ACTIVITY_STATE_RESUMED = 1 << 1;
    /**
     * State flag indicating if the user is active or the actitvity when to background as a result
     * of user action.
     * see #isUserActive()
     */
    static final int ACTIVITY_STATE_USER_ACTIVE = 1 << 2;
}
