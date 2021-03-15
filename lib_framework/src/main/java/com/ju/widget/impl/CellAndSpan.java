package com.ju.widget.impl;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/3/15
 * @Description: TODO
 */
public class CellAndSpan {

    public int x, y;
    public int spanX, spanY;

    public CellAndSpan() {
    }

    public void copy(CellAndSpan copy) {
        copy.x = x;
        copy.y = y;
        copy.spanX = spanX;
        copy.spanY = spanY;
    }

    public CellAndSpan(int x, int y, int spanX, int spanY) {
        this.x = x;
        this.y = y;
        this.spanX = spanX;
        this.spanY = spanY;
    }

    public String toString() {
        return "(" + x + ", " + y + ": " + spanX + ", " + spanY + ")";
    }
}
