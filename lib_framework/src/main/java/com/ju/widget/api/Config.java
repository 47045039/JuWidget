package com.ju.widget.api;

public class Config {

    /**
     * 固定配置，指定默认的Cell的宽高和个数
     */
    public int mCellWidth;          // Cell宽度，单位：px
    public int mCellHeight;         // Cell高度，单位：px
    public int mCellCountX;         // 横向Cell个数
    public int mCellCountY;         // 纵向Cell个数

    /**
     * 间隔信息需要根据view宽高和固定配置计算出来
     */
    public int mCellGapX;           // 横向Cell间隔
    public int mCellGapY;           // 纵向Cell间隔

    public Config() {
        this(160, 160, 3, 6);
    }

    public Config(int w, int h, int cx, int cy) {
        if (w <= 80 || h <= 80 || cx <= 2 || cy <= 2) {
            throw new IllegalArgumentException("Can not create a Config.");
        }

        mCellWidth = w;
        mCellHeight = h;
        mCellCountX = cx;
        mCellCountY = cy;
    }

    public boolean setGap(int gapX, int gapY) {
        if (gapX >= 0 && gapY >= 0) {
            mCellGapX = gapX;
            mCellGapY = gapY;
        }
        return true;
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

        if (mCellCountX != config.mCellCountX) {
            mCellCountX = config.mCellCountX;
            changed = true;
        }

        if (mCellCountY != config.mCellCountY) {
            mCellCountY = config.mCellCountY;
            changed = true;
        }

        if (mCellGapX != config.mCellGapX) {
            mCellGapX = config.mCellGapX;
            changed = true;
        }

        if (mCellGapY != config.mCellGapY) {
            mCellGapY = config.mCellGapY;
            changed = true;
        }

        return changed;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Config{");
        sb.append("mCellWidth=").append(mCellWidth);
        sb.append(", mCellHeight=").append(mCellHeight);
        sb.append(", mCellCountX=").append(mCellCountX);
        sb.append(", mCellCountY=").append(mCellCountY);
        sb.append(", mCellGapX=").append(mCellGapX);
        sb.append(", mCellGapY=").append(mCellGapY);
        sb.append('}');
        return sb.toString();
    }
}
