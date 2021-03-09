package com.android.launcher3;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

public interface IBaseDraggingActivity {
    boolean finishAutoCancelActionMode();

    Rect getViewBounds(View v);

    Bundle getActivityLaunchOptionsAsBundle(View v);

    boolean startActivitySafely(View v, Intent intent, ItemInfo item);
}
