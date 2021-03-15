package com.ju.widget.impl;

import android.content.Intent;

import java.util.Arrays;

/**
 * Represents an item in the launcher.
 */
public class ItemInfo {

    public static final int ITEM_TYPE_WIDGET = 1;
    public static final int ITEM_TYPE_APPLICATION = 2;
    public static final int ITEM_TYPE_APPWIDGET = 3;

    static final int NO_ID = -1;
    
    /**
     * The id in the settings database for this item
     */
    public long id = NO_ID;
    
    /**
     * One of {@link ItemInfo#ITEM_TYPE_WIDGET},
     * {@link ItemInfo#ITEM_TYPE_APPLICATION},
     * {@link ItemInfo#ITEM_TYPE_APPWIDGET}.
     */
    public int itemType = ITEM_TYPE_WIDGET;
    
    /**
     * The id of the container that holds this item.
     */
    public long container = NO_ID;
    
    /**
     * Indicates the X position of the associated cell.
     */
    public int cellX = -1;

    /**
     * Indicates the Y position of the associated cell.
     */
    public int cellY = -1;

    /**
     * Indicates the X cell span.
     */
    public int spanX = 1;

    /**
     * Indicates the Y cell span.
     */
    public int spanY = 1;

    /**
     * Indicates the minimum X cell span.
     */
    public int minSpanX = 1;

    /**
     * Indicates the minimum Y cell span.
     */
    public int minSpanY = 1;

    /**
     * Indicates that this item needs to be updated in the db
     */
    public boolean requiresDbUpdate = false;

    /**
     * Title of the item
     */
    CharSequence title;

    /**
     * Content description of the item.
     */
    CharSequence contentDescription;

    /**
     * The position of the item in a drag-and-drop operation.
     */
    int[] dropPos = null;

    ItemInfo() {}

    ItemInfo(ItemInfo info) {
        copyFrom(info);
    }

    public void copyFrom(ItemInfo info) {
        id = info.id;
        cellX = info.cellX;
        cellY = info.cellY;
        spanX = info.spanX;
        spanY = info.spanY;
        itemType = info.itemType;
        container = info.container;
        contentDescription = info.contentDescription;
    }

    public Intent getIntent() {
        throw new RuntimeException("Unexpected Intent");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ItemInfo{");
        sb.append("id=").append(id);
        sb.append(", itemType=").append(itemType);
        sb.append(", container=").append(container);
        sb.append(", cellX=").append(cellX);
        sb.append(", cellY=").append(cellY);
        sb.append(", spanX=").append(spanX);
        sb.append(", spanY=").append(spanY);
        sb.append(", minSpanX=").append(minSpanX);
        sb.append(", minSpanY=").append(minSpanY);
        sb.append(", requiresDbUpdate=").append(requiresDbUpdate);
        sb.append(", title=").append(title);
        sb.append(", contentDescription=").append(contentDescription);
        sb.append(", dropPos=").append(Arrays.toString(dropPos));
        sb.append('}');
        return sb.toString();
    }
}
