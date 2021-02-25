package com.ju.widget.interfaces;


/**
 * 通用的回调接口
 */
public interface ICommonCallback<D> {

    /**
     * 数据回调接口
     *
     * @param succ 是否成功
     * @param data 回调的数据
     */
    void onFinished(boolean succ, D data);
}
