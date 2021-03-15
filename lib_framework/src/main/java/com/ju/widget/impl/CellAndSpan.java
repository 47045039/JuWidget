package com.ju.widget.impl;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/3/15
 * @Description: TODO
 */
public class CellAndSpan {

    public int cellX, cellY;
    public int spanX, spanY;

    public CellAndSpan() {
    }

    public void copyFrom(CellAndSpan copy) {
        copy.cellX = cellX;
        copy.cellY = cellY;
        copy.spanX = spanX;
        copy.spanY = spanY;
    }

    public CellAndSpan(int cellX, int cellY, int spanX, int spanY) {
        this.cellX = cellX;
        this.cellY = cellY;
        this.spanX = spanX;
        this.spanY = spanY;
    }

    public String toString() {
        return "(" + cellX + ", " + cellY + ": " + spanX + ", " + spanY + ")";
    }
}
