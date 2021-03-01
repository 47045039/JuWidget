package com.ju.widget.api;

public class Config {

    public int mCellWidth;          // Cell宽度
    public int mCellHeight;         // Cell高度
    public int mCellCountH;         // 横向Cell个数
    public int mCellCountV;         // 纵向Cell个数

    public Config() {
        this(160, 160, 3, 6);
    }

    public Config(int w, int h, int ch, int cv) {
        if (w <= 80 || h <= 80 || ch < 2 || cv < 3) {
            throw new IllegalArgumentException("Can not create a Config.");
        }

        mCellWidth = w;
        mCellHeight = h;
        mCellCountH = ch;
        mCellCountV = cv;
    }

    public boolean set(Config config) {
        if (config == null) {
            return false;
        }

        boolean changed = false;

        if (mCellWidth != config.mCellWidth) {
            mCellWidth = config.mCellWidth;
            changed = true;
        }

        if (mCellHeight != config.mCellHeight) {
            mCellHeight = config.mCellHeight;
            changed = true;
        }

        if (mCellCountH != config.mCellCountH) {
            mCellCountH = config.mCellCountH;
            changed = true;
        }

        if (mCellCountV != config.mCellCountV) {
            mCellCountV = config.mCellCountV;
            changed = true;
        }
        return changed;
    }

}
