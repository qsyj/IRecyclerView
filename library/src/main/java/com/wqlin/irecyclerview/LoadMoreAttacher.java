package com.wqlin.irecyclerview;

import android.support.v7.widget.IRecyclerView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by wqlin on 2017/9/3.
 */

public class LoadMoreAttacher implements ILoadMoreAttacher{
    private final String TAG = "LoadMoreAttacher";

    IRecyclerView iRecyclerView;

    /**
     * 快速滑动
     */
    private boolean isFling;

    private boolean isFull = true;

    private boolean isDragLoadMore;

    private boolean loadMoreEnabled;

    private OnLoadMoreListener mOnLoadMoreListener;

    public LoadMoreAttacher(IRecyclerView iRecyclerView) {
        this.iRecyclerView = iRecyclerView;
    }

    @Override
    public void setLoadMoreEnabled(boolean enabled) {
        this.loadMoreEnabled = enabled;
    }

    public void pullRefreshComplete() {
        if (!loadMoreEnabled)
            return;

        setStatusLoadMore(Status.LOAD_RESET);
    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mOnLoadMoreListener = listener;
    }

    @Override
    public boolean isFling() {
        return isFling;
    }

    @Override
    public void setFling(boolean fling) {
        if(!loadMoreEnabled)
            return;
        isFling = fling;
    }

    /**
     * 是否充满屏幕
     * @return
     */
    public boolean isFull() {
        return isFull;
    }

    @Override
    public void setFull() {
        if (!loadMoreEnabled)
            return;

        View lastView = iRecyclerView.getChildAt(iRecyclerView.getChildCount()-1);
        if (lastView.getBottom()<iRecyclerView.getBottom()-iRecyclerView.getPaddingBottom()) {
            isFull = false;
        }
        isFull = true;
    }

    @Override
    public boolean isDragLoadMore() {
        Log.e(TAG, "isDragLoadMore() isDragLoadMore:" + isDragLoadMore);
        return isDragLoadMore;
    }

    @Override
    public void setDragLoadMore(boolean dragLoadMore) {
        if (!loadMoreEnabled)
            return;

        isDragLoadMore = dragLoadMore;
    }

    @Override
    public void handleMove(int dy) {
        if (!loadMoreEnabled)
            return;

        if (isFull&&iRecyclerView.getLoadMoreFooterView() != null && iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() != Status.LOAD_END) {
            if (dy > 0) {//下滑
                isDragLoadMore = false;
                if (getStatusLoadMore() == Status.LOAD_RELEASE_TO_REFRESH) {
                    WrapperAdapter adapter = (WrapperAdapter) iRecyclerView.getAdapter();
                    if (adapter.isLoadMoreFooterView()) {
                        View loadMoreView = adapter.getLoadMoreFooterContainer();
                        int position = iRecyclerView.getChildLayoutPosition(loadMoreView);
                        if (position >= 0) {//可见
                            int lastBottom=loadMoreView.getBottom();
                            int recyclerBottom =  iRecyclerView.getBottom()-iRecyclerView.getPaddingBottom();
                            if (lastBottom > recyclerBottom) {
                                setStatusLoadMore(Status.LOAD_RESET);
                            }

                        }
                    }
                }
            } else if (dy < 0) {//上滑
                if (iRecyclerView.getAdapter()!=null&&iRecyclerView.getAdapter() instanceof WrapperAdapter) {
                    WrapperAdapter adapter = (WrapperAdapter) iRecyclerView.getAdapter();
                    if (adapter.isLoadMoreFooterView()) {

                        View loadMoreView = adapter.getLoadMoreFooterContainer();
                        int position = iRecyclerView.getChildLayoutPosition(loadMoreView);
                        if (position >= 0) {//可见
                            if (!adapter.isEmptyView()) {
                                setStatusLoadMore(Status.LOAD_RESET);
                                adapter.setIsEmptyView(true);
                            }
                            isDragLoadMore = true;
                        }
                    }
//                    Log.e(TAG, "松手加载 Status:" + getStatusLoadMore());
                    if (getStatusLoadMore() == Status.LOAD_RESET&&isDragLoadMore && adapter.isEmptyView()) {//松手加载
                        View loadMoreView = adapter.getLoadMoreFooterContainer();
                        int lastBottom=loadMoreView.getBottom();
                        int recyclerBottom =  iRecyclerView.getBottom()-iRecyclerView.getPaddingBottom();
//                        Log.e(TAG, "松手加载 lastBottom:" + lastBottom + ",recyclerBottom:" + recyclerBottom);
                        if (lastBottom <= recyclerBottom) {
                            setStatusLoadMore(Status.LOAD_RELEASE_TO_REFRESH);
                        } else {
                            setStatusLoadMore(Status.LOAD_RESET);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setStatusLoadMore(Status statusLoadMore) {
        if (!loadMoreEnabled)
            return;

        if ( iRecyclerView.getLoadMoreFooterView()!= null) {
            iRecyclerView.getLoadMoreFooterView().setLoadMoreStatus(statusLoadMore);
        }
    }

    @Override
    public Status getStatusLoadMore() {
        if (iRecyclerView.getLoadMoreFooterView() != null) {
            return iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus();
        }
        return Status.LOAD_RESET;
    }

    public void resetLoadMoreFooterViewStatus() {
        if (!loadMoreEnabled)
            return;

//        Log.e(TAG, "resetLoadMoreFooterViewStatus() Status:" + getStatusLoadMore());
        if (iRecyclerView != null&&
                iRecyclerView.getLoadMoreFooterView()!=null&&
                iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus()!=Status.LOAD_ING&&
                iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus()!=Status.LOAD_END) {
            LinearLayout footerContainer = iRecyclerView.getFooterContainer();
            if (iRecyclerView.getChildLayoutPosition(footerContainer)<0) {//不可见
                iRecyclerView.getLoadMoreFooterView().setLoadMoreStatus(Status.LOAD_RESET);
            }

        }
    }

    @Override
    public void addLoadMoreFooter() {
        if (!loadMoreEnabled)
            return;

        if (iRecyclerView.getAdapter()!=null&&iRecyclerView.getAdapter() instanceof WrapperAdapter) {
            WrapperAdapter adapter = (WrapperAdapter) iRecyclerView.getAdapter();
            if (!adapter.isLoadMoreFooterView()) {
                adapter.setIsLoadMoreView(true);
            }
        }
    }

    @Override
    public void removeLoadMoreFooter() {
        if (!loadMoreEnabled)
            return;

        if (iRecyclerView.getAdapter()!=null&&iRecyclerView.getAdapter() instanceof WrapperAdapter&&iRecyclerView instanceof IRecyclerView) {
            WrapperAdapter adapter = (WrapperAdapter) iRecyclerView.getAdapter();
            if (adapter.isLoadMoreFooterView()) {
                View loadMoreView = adapter.getLoadMoreFooterContainer();
                int position = iRecyclerView.getChildLayoutPosition(loadMoreView);
                if (position >= 0) {//可见
                    int lastBottom = loadMoreView.getBottom();
                    int recyclerBottom = iRecyclerView.getBottom() - iRecyclerView.getPaddingBottom();
                    if (adapter.isEmptyView()) {
                        adapter.setIsEmptyView(false);
//                            onLoadMore(recyclerView);
                    }
                    if (lastBottom > recyclerBottom && iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() != Status.LOAD_END) {
                        setStatusLoadMore(Status.LOAD_RESET);
                        adapter.setIsLoadMoreView(false);
                    } else {
                        if (getStatusLoadMore() == Status.LOAD_RELEASE_TO_REFRESH) {
                            onLoadMore();
                        }

                    }
                } else {
                    adapter.setIsLoadMoreView(false);
                    if (adapter.isEmptyView()) {
                        adapter.setIsEmptyView(false);
                    }
                }
            }

        }
    }

    @Override
    public void setLoadMoreComplete(boolean isLoadMoreComplete) {
        if (!loadMoreEnabled)
            return;

        if (isLoadMoreComplete&&getStatusLoadMore()==Status.LOAD_ING&&iRecyclerView.getAdapter()!=null&&iRecyclerView.getAdapter() instanceof WrapperAdapter) {
            iRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setStatusLoadMore(Status.LOAD_COMPLETE);
                }
            }, 250);
        }
    }

    @Override
    public void setLoadMoreEnd() {
        if (!loadMoreEnabled)
            return;

        setStatusLoadMore(Status.LOAD_END);
    }

    @Override
    public void setLoadMoreError() {
        if (!loadMoreEnabled)
            return;

        if (getStatusLoadMore()==Status.LOAD_ING&&iRecyclerView.getAdapter()!=null&&iRecyclerView.getAdapter() instanceof WrapperAdapter) {
            setStatusLoadMore(Status.LOAD_ERROR);
            final WrapperAdapter adapter = (WrapperAdapter) iRecyclerView.getAdapter();
            if (adapter.isLoadMoreFooterView()) {
                View loadMoreView = adapter.getLoadMoreFooterContainer();
                int position = iRecyclerView.getChildLayoutPosition(loadMoreView);
                if (position >= 0) {//可见
                    iRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setIsLoadMoreView(false);
                        }
                    }, 1000);
                }
            }

        }
    }

    @Override
    public void onScrollStateChanged(int newState) {
        RecyclerView.LayoutManager layoutManager = iRecyclerView.getLayoutManager();
        if (newState != RecyclerView.SCROLL_STATE_IDLE)
            return;

        WrapperAdapter adapter = (WrapperAdapter) iRecyclerView.getAdapter();
        if (adapter.isLoadMoreFooterView()) {
            View loadMoreView = adapter.getLoadMoreFooterContainer();
            int position = iRecyclerView.getChildLayoutPosition(loadMoreView);
            if (position >= 0) {//可见
                int loadMoreViewBottom = loadMoreView.getBottom();
                int recyclerBottom =  iRecyclerView.getBottom()-iRecyclerView.getPaddingBottom();
                if (loadMoreViewBottom<=recyclerBottom) {
                    if (loadMoreViewBottom < recyclerBottom) {
                        if (adapter.isEmptyView()) {
                            adapter.setIsEmptyView(false);
                        }
                    }
                    Status status=getStatusLoadMore();
                    if ( status!= Status.LOAD_ING
                            &&status!=Status.LOAD_COMPLETE
                            &&status!=Status.LOAD_END
                            &&status!=Status.LOAD_ERROR) {
                        setStatusLoadMore(Status.LOAD_ING);
                        onLoadMore();
                    }
                }
            }
        }
    }

    private void onLoadMore() {
        setStatusLoadMore(Status.LOAD_ING);
        iRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOnLoadMoreListener.onLoadMore();
            }
        }, 280);
    }
}
