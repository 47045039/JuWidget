package com.ju.widget.api;

import static com.ju.widget.api.Constants.ORIENTATION_H;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: 查询参数
 */
public class Query {

    public int mMaxSpanX = -1;                  // 最大跨度，-1 = 不受最大跨度限制
    public int mBestSpanX = -1;                 // 最佳跨度，-1 = 不受最佳跨度限制

    public int mOrietention = ORIENTATION_H;    // 默认查询支持横屏的widget
    public int mMaxCount = -1;                  // 返回结果的最大数量，-1 = 不受数量限制

}
