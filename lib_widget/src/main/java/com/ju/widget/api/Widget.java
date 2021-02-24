package com.ju.widget.api;

import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils;

import com.ju.widget.interfaces.ICommonCallback;
import com.ju.widget.util.Tools;

import java.util.ArrayList;
import java.util.Objects;

import static com.ju.widget.api.Constants.ORIENTATION_MASK;

/**
 * Widget
 *
 * @param <D> Widget数据
 * @param <V> Widget界面
 */
public abstract class Widget<D extends WidgetData, V extends WidgetView> {

    private static final int MIN_UPDATE_INTERVAL = 30 * 60 * 1000;  // 最小刷新间隔，30min

    private final String mID;                   // Widget ID，保持全局唯一
    private final String mProductID;            // Widget Product ID
    private final Point mCellSpan;              // 跨度span
    private final int mOrientation;             // 默认支持横向 + 纵向
    private final int mUpdateInterval;          // 数据更新时间间隔，ms

    private D mData;                            // 关联数据

//    private ICommonCallback<D> mUpdateCallback; // 数据刷新的回调接口
//    private boolean mUpdating;                  // 是否正在刷新数据
//    private long mUpdateTimeStamp;              // 数据更新时戳，ms

    public Widget(String id, String pid) {
        this(id, pid, 1, 1, ORIENTATION_MASK, -1);
    }

    public Widget(String id, String pid, int orientation) {
        this(id, pid, 1, 1, orientation, -1);
    }

    public Widget(String id, String pid, int orientation, int interval) {
        this(id, pid, 1, 1, orientation, interval);
    }

    public Widget(String id, String pid, int spanX, int spanY, int interval) {
        this(id, pid, spanX, spanY, ORIENTATION_MASK, interval);
    }

    public Widget(String id, String pid, int spanX, int spanY, int orientation, int interval) {
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pid)) {
            throw new IllegalArgumentException("Widget ID & ProductID must not empty.");
        }

        mID = id;
        mProductID = pid;

        if (spanX > 0 && spanY > 0) {
            mCellSpan = new Point(spanX, spanY);
        } else {
            mCellSpan = new Point(1, 1);
        }

        int orient = orientation & ORIENTATION_MASK;
        if (orient == 0) {
            mOrientation = ORIENTATION_MASK;
        } else {
            mOrientation = orientation;
        }

        if (interval > 0 && interval < MIN_UPDATE_INTERVAL) {
            mUpdateInterval = MIN_UPDATE_INTERVAL;
        } else {
            mUpdateInterval = interval;
        }
    }

    public String getID() {
        return mID;
    }

    public String getProductID() {
        return mProductID;
    }

    public Point getCellSpan() {
        return new Point(mCellSpan);
    }

    public boolean supportHorizontalScreen() {
        return Tools.isHorizontal(mOrientation);
    }

    public boolean supportVerticalScreen() {
        return Tools.isVertical(mOrientation);
    }

    public boolean isMatch(Query query) {
        if (query == null) {
            return true;
        }

        int span = query.mMaxSpanX;
        // 查询的最大跨度 < 当前widget跨度
        if (span > 0 && span < mCellSpan.x) {
            return false;
        }

        span = query.mBestSpanX;
        // 查询的最佳跨度 != 当前widget跨度
        if (span > 0 && span != mCellSpan.x) {
            return false;
        }

        // 查询横屏widget，但当前widget不支持横屏
        if (Tools.isHorizontal(query.mOrietention) && !supportHorizontalScreen()) {
            return false;
        }

        // 查询竖屏widget，但当前widget不支持竖屏
        if (Tools.isVertical(query.mOrietention) && !supportVerticalScreen()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Widget<?, ?> widget = (Widget<?, ?>) o;
        return Objects.equals(mID, widget.mID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mID);
    }

//    private void buildUpdateCallback() {
//        if (mUpdateCallback != null) {
//            return;
//        }
//
//        mUpdateCallback = new ICommonCallback<D>() {
//            @Override
//            public void onFinished(final boolean succ, final D data) {
//                Tools.runOnMainThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        onUpdateFinished(data);
//                    }
//                });
//            }
//        };
//    }
//
//    /**
//     * 检查是否已经attach到界面
//     *
//     * @return
//     */
//    boolean isAttached() {
//        return mViews.size() > 0;
//    }
//
//    /**
//     * 绑定Widget和WidgetView
//     *
//     * @param view
//     * @return
//     */
//    boolean attach(V view) {
//        if (!mViews.contains(view)) {
//            mViews.add(view);
//        }
//        return true;
//    }
//
//    /**
//     * 解绑Widget和WidgetView
//     *
//     * @param view
//     * @return
//     */
//    boolean detach(V view) {
//        if (mViews.contains(view)) {
//            mViews.remove(view);
//        }
//        return true;
//    }
//
//    /**
//     * 通知更新数据
//     *
//     * @param context
//     * @return
//     */
//    boolean update(Context context) {
//        if (mUpdating) {
//            return false;
//        }
//
//        mUpdating = true;
//        buildUpdateCallback();
//        return doUpdate(context, mUpdateCallback);
//    }
//
//    /**
//     * 遍历更新已经attached的WidgetView
//     *
//     * @param data
//     * @return
//     */
//    boolean onUpdateFinished(D data) {
//        mData = data;
//        mUpdating = false;
//        mUpdateTimeStamp = System.currentTimeMillis();
//
//        final ArrayList<V> views = mViews;
//        if (views.size() > 0) {
//            for (V view : views) {
//                view.setData(data);
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 子类根据业务需求刷新数据，可异步也可同步刷新
//     *
//     * @param context
//     * @return
//     */
//    protected abstract boolean doUpdate(Context context, ICommonCallback callback);

}
