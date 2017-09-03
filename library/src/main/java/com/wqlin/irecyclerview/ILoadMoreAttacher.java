package com.wqlin.irecyclerview;

/**
 * Created by wqlin on 2017/9/3.
 */

public interface ILoadMoreAttacher {

    /**
     * 是否快速滑动
     * @return
     */
    boolean isFling();

    /**
     * 是否快速滑动
     * @return
     */
    void setFling(boolean fling);

    /**
     * 是否下拉刷新
     * @param enabled
     */
    void setLoadMoreEnabled(boolean enabled);

    /**
     * 是否充满屏幕
     * @return
     */
    boolean isFull();

    /**
     * 设置是否充满屏幕(onInterceptTouchEvent() ACTION_DOWN)
     */
    void setFull();

    /**
     * 下拉刷新完成
     */
    void pullRefreshComplete();

    /**
     * 加载更多 回调监听
     * @param listener
     */
    void setOnLoadMoreListener(OnLoadMoreListener listener);

    /**
     * 重置FootView状态(onInterceptTouchEvent() ACTION_DOWN)
     */
    void resetLoadMoreFooterViewStatus();

    /**
     * 添加LoadMoreFooterView到WrapperAdapter中(onInterceptTouchEvent() ACTION_DOWN)
     */
    void addLoadMoreFooter();

    /**
     * 移除LoadMoreFooterView到WrapperAdapter中(onInterceptTouchEvent() ACTION_UP   onTouchEvent() ACTION_UP )
     */
    void removeLoadMoreFooter();

    /**
     * 处理 onTouchEvent()  MotionEvent.ACTION_MOVE
     * @param dy
     */
    void handleMove(int dy);

    /**
     * 设置FooterView状态
     * @param statusLoadMore
     */
    void setStatusLoadMore(Status statusLoadMore);

    /**
     *
     * @return  FooterView状态
     */
    Status getStatusLoadMore();

    /**
     * 是否向上拖动加载
     * @return
     */
    boolean isDragLoadMore();

    /**
     * 是否向上拖动加载
     * @param dragLoadMore
     */
    void setDragLoadMore(boolean dragLoadMore);

    /**
     * 成功加载完成
     * @param isLoadMoreComplete
     */
    void setLoadMoreComplete(boolean isLoadMoreComplete);

    /**
     * 加载结束  没有更多数据
     */
    void setLoadMoreEnd();

    /**
     * 加载更多 出现错误
     */
    void setLoadMoreError();

    /**
     * 滑动状态改变  OnLoadMoreScrollListener onScrolled()
     * @param newState
     */
    void onScrollStateChanged(int newState);
}
