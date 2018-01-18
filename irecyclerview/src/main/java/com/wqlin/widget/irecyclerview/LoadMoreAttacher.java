package com.wqlin.widget.irecyclerview;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.IRecyclerView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by wqlin on 2017/9/3.
 */

public class LoadMoreAttacher implements ILoadMoreAttacher{
    private final String TAG = "LoadMoreAttacher";

    private final int WHAT_LOAD_EROR = 1;
    private final int WHAT_LOAD_MORE = 2;
    private final int WHAT_LOAD_COMPLETE = 3;

    IRecyclerView iRecyclerView;

    /**
     * 快速滑动
     */
    private boolean isFling;

    private boolean isFull = true;

    private boolean isDragLoadMore;

    private boolean loadMoreEnabled;

    private OnLoadMoreListener mOnLoadMoreListener;

    private Handler mHandler;

    public LoadMoreAttacher(IRecyclerView iRecyclerView) {
        this.iRecyclerView = iRecyclerView;
        mHandler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case WHAT_LOAD_EROR:
                        if (LoadMoreAttacher.this.iRecyclerView != null) {
                            WrapperAdapter adapter = (WrapperAdapter) LoadMoreAttacher.this.iRecyclerView.getAdapter();
                            adapter.setIsLoadMoreView(false);
                            setStatusLoadMore(Status.LOAD_RESET);
                        }
                        return true;
                    case WHAT_LOAD_COMPLETE:
                        setStatusLoadMore(Status.LOAD_COMPLETE);
                        return true;
                    case WHAT_LOAD_MORE:
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void setLoadMoreEnabled(boolean enabled) {
        this.loadMoreEnabled = enabled;
    }

    @Override
    public void setLoadMoreReset() {
        if (!loadMoreEnabled)
            return;

        setStatusLoadMore(Status.LOAD_RESET);
    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mOnLoadMoreListener = listener;
    }

    @Override
    public void destory() {
        if (mHandler != null) {
            mHandler.removeMessages(WHAT_LOAD_MORE);
            mHandler.removeMessages(WHAT_LOAD_COMPLETE);
            mHandler.removeMessages(WHAT_LOAD_EROR);
        }

        if (iRecyclerView != null) {
            RecyclerView.Adapter adapter = iRecyclerView.getAdapter();
            if (adapter != null && adapter instanceof WrapperAdapter) {
                WrapperAdapter wrapperAdapter = (WrapperAdapter) adapter;
                wrapperAdapter.destory();
            }
        }
        iRecyclerView = null;
        mOnLoadMoreListener = null;
        mHandler = null;
    }

    @Override
    public int getIAdapterCount() {
        if (iRecyclerView==null)
            return 0;
        RecyclerView.Adapter adapter = iRecyclerView.getIAdapter();
        if (adapter==null)
            return 0;
        return iRecyclerView.getIAdapter().getItemCount();
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
        if (lastView.getBottom()<iRecyclerView.getHeight()-iRecyclerView.getPaddingBottom()) {
            isFull = false;
        }
        isFull = true;
    }

    @Override
    public boolean isDragLoadMore() {
//        Log.e(TAG, "isDragLoadMore() isDragLoadMore:" + isDragLoadMore);
        return isDragLoadMore;
    }

    @Override
    public void setDragLoadMore(boolean dragLoadMore) {
        if (!loadMoreEnabled)
            return;

        isDragLoadMore = dragLoadMore;
    }

    private boolean isCanChangeStatusWhenMove() {

        if (iRecyclerView != null) {
            Status status = iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus();
//            Log.e("CanChangeStatusWhenMove", "status:" + status.name());
            if (status== Status.LOAD_ERROR||
                    status== Status.LOAD_ING)// status!= Status.LOAD_COMPLETE
                return false;
        }
        return true;
    }

    private void setStatusWhenMove(Status statusLoadMore) {
//        Log.e("setStatusWhenMove", "statusLoadMore:" + statusLoadMore.name());
        if (isCanChangeStatusWhenMove())
            setStatusLoadMore(statusLoadMore);
    }
    @Override
    public void handleMove(int dy) {
        if (!loadMoreEnabled)
            return;
        if (isAnim())
            return;
        if (iRecyclerView==null)
            return;
        Status status = iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus();
        if (isFull&&iRecyclerView.getLoadMoreFooterView() != null
                && status!= Status.LOAD_END) {
            if (dy > 0) {//下滑
                isDragLoadMore = false;
                if (getStatusLoadMore() == Status.LOAD_RELEASE_TO_REFRESH) {
                    WrapperAdapter adapter = (WrapperAdapter) iRecyclerView.getAdapter();
                    if (adapter.isLoadMoreFooterView()) {
                        View loadMoreView = adapter.getLoadMoreFooterContainer();
                        int position = iRecyclerView.getChildLayoutPosition(loadMoreView);
                        if (position >= 0) {//可见
                            int lastBottom=loadMoreView.getBottom();
                            int recyclerBottom =  iRecyclerView.getHeight()-iRecyclerView.getPaddingBottom();
                            if (lastBottom > recyclerBottom) {
                                setStatusWhenMove(Status.LOAD_RESET);
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
                                setStatusWhenMove(Status.LOAD_RESET);
                                if (iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() == Status.LOAD_ERROR) {
                                    adapter.setIsEmptyView(false);
                                } else {
                                    adapter.setIsEmptyView(true);
                                }
                            }
                            isDragLoadMore = true;
                        }
                    }
//                    Log.e(TAG, "松手加载 Status:" + getStatusLoadMore());
                    if (getStatusLoadMore() == Status.LOAD_RESET&&isDragLoadMore && adapter.isEmptyView()) {//松手加载
                        View loadMoreView = adapter.getLoadMoreFooterContainer();
                        int lastBottom=loadMoreView.getBottom();
                        int recyclerBottom =  iRecyclerView.getHeight()-iRecyclerView.getPaddingBottom();
//                        Log.e(TAG, "松手加载 lastBottom:" + lastBottom + ",recyclerBottom:" + recyclerBottom);
                        if (lastBottom <= recyclerBottom) {
                            setStatusWhenMove(Status.LOAD_RELEASE_TO_REFRESH);
                        } else {
                            setStatusWhenMove(Status.LOAD_RESET);
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

        if ( iRecyclerView!=null&&iRecyclerView.getLoadMoreFooterView()!= null) {
            iRecyclerView.getLoadMoreFooterView().setLoadMoreStatus(statusLoadMore);
        }
    }

    @Override
    public Status getStatusLoadMore() {
        if (iRecyclerView!=null&&iRecyclerView.getLoadMoreFooterView() != null) {
            return iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus();
        }
        return Status.LOAD_RESET;
    }

    @Override
    public boolean isAnim() {
        if (iRecyclerView==null)
            return false;
        RecyclerView.Adapter adapter = iRecyclerView.getAdapter();
        if (adapter==null|| !(adapter instanceof WrapperAdapter))
            return false;
        WrapperAdapter wrapperAdapter = (WrapperAdapter) adapter;

        return wrapperAdapter.isRemoveAnim();
    }

    @Override
    public void resetLoadMoreFooterViewStatus() {
        if (!loadMoreEnabled)
            return;
        if (isAnim())
            return;

//        Log.e(TAG, "resetLoadMoreFooterViewStatus() Status:" + getStatusLoadMore());
        if (iRecyclerView != null&&
                iRecyclerView.getLoadMoreFooterView()!=null&&
                iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus()!= Status.LOAD_ING&&
                iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus()!= Status.LOAD_END) {
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
        if (isAnim())
            return;
        if (iRecyclerView==null)
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
        if (isAnim())
            return;
        if (iRecyclerView==null)
            return;

        if (iRecyclerView.getAdapter()!=null&&iRecyclerView.getAdapter() instanceof WrapperAdapter&&iRecyclerView instanceof IRecyclerView) {
            WrapperAdapter adapter = (WrapperAdapter) iRecyclerView.getAdapter();
            if (true) {//adapter.isLoadMoreFooterView()
                View loadMoreView = adapter.getLoadMoreFooterContainer();
                int position = iRecyclerView.getChildLayoutPosition(loadMoreView);
                if (position >= 0) {//可见
                    int lastBottom = loadMoreView.getBottom();
                    int recyclerBottom = iRecyclerView.getHeight() - iRecyclerView.getPaddingBottom();
                    if (adapter.isEmptyView()) {
                        adapter.setIsEmptyView(false);
//                            onLoadMore(recyclerView);
                    }
                    if (iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() == Status.LOAD_ERROR) {
                        setStatusLoadMore(Status.LOAD_RESET);
                        adapter.setIsLoadMoreView(false);
                    } else {
                        if (lastBottom > recyclerBottom && iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() != Status.LOAD_END) {
                            if (iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus()!= Status.LOAD_ING)
                                setStatusLoadMore(Status.LOAD_RESET);
                            adapter.setIsLoadMoreView(false);
                        } else {
                            if (getStatusLoadMore() == Status.LOAD_RELEASE_TO_REFRESH) {
                                onLoadMore();
                            }

                        }
                    }

                } else {
                    if (iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() != Status.LOAD_END
                            && iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() != Status.LOAD_ING) {
                        adapter.setIsLoadMoreView(false);
                    }
                    if (adapter.isEmptyView()) {
                        adapter.setIsEmptyView(false);
                    }
                    if (iRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() == Status.LOAD_ERROR)
                        setStatusLoadMore(Status.LOAD_RESET);
                }
            }

        }
    }

    @Override
    public void setLoadMoreComplete(boolean isLoadMoreComplete) {
        if (!loadMoreEnabled)
            return;
        if (iRecyclerView==null)
            return;

        if (isLoadMoreComplete&&getStatusLoadMore()== Status.LOAD_ING&&iRecyclerView.getAdapter()!=null&&iRecyclerView.getAdapter() instanceof WrapperAdapter) {
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(WHAT_LOAD_COMPLETE, 250);
            }
        }
    }

    @Override
    public void setLoadMoreNull() {
        if (!loadMoreEnabled)
            return;
        setStatusLoadMore(Status.LOAD_NULL);

    }

    @Override
    public void setLoadMoreEnd() {
        if (!loadMoreEnabled)
            return;
        if (getIAdapterCount() > 0) {
            addLoadMoreFooter();
        }
        setStatusLoadMore(Status.LOAD_END);
    }

    @Override
    public void setLoadMoreError() {
        if (!loadMoreEnabled)
            return;
        if (iRecyclerView==null)
            return;

        if (getStatusLoadMore()== Status.LOAD_ING&&iRecyclerView.getAdapter()!=null&&iRecyclerView.getAdapter() instanceof WrapperAdapter) {
            setStatusLoadMore(Status.LOAD_ERROR);
            final WrapperAdapter adapter = (WrapperAdapter) iRecyclerView.getAdapter();
            adapter.setIsEmptyView(false);
            if (adapter.isLoadMoreFooterView()) {
                View loadMoreView = adapter.getLoadMoreFooterContainer();
                int position = iRecyclerView.getChildLayoutPosition(loadMoreView);
                if (position >= 0) {//可见
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(WHAT_LOAD_EROR,  WrapperAdapter.DURATION_MAX);
                    }
                }
            }

        }
    }

    @Override
    public void onScrollStateChanged(int newState) {
        if (iRecyclerView==null)
            return;

        RecyclerView.LayoutManager layoutManager = iRecyclerView.getLayoutManager();
        if (newState != RecyclerView.SCROLL_STATE_IDLE)
            return;

        WrapperAdapter adapter = (WrapperAdapter) iRecyclerView.getAdapter();
        if (adapter.isLoadMoreFooterView()) {
            View loadMoreView = adapter.getLoadMoreFooterContainer();
            int position = iRecyclerView.getChildLayoutPosition(loadMoreView);
            if (position >= 0) {//可见
                int loadMoreViewBottom = loadMoreView.getBottom();
                int recyclerBottom =  iRecyclerView.getHeight()-iRecyclerView.getPaddingBottom();
                if (loadMoreViewBottom<=recyclerBottom) {
                    if (loadMoreViewBottom < recyclerBottom) {
                        if (adapter.isEmptyView()) {
                            adapter.setIsEmptyView(false);
                        }
                    }
                    Status status=getStatusLoadMore();
                    if ( status!= Status.LOAD_ING
                            &&status!= Status.LOAD_COMPLETE
                            &&status!= Status.LOAD_END
                            &&status!= Status.LOAD_ERROR) {
                        setStatusLoadMore(Status.LOAD_ING);
                        onLoadMore();
                    }
                }
            }
        }
    }

    private void onLoadMore() {
        setStatusLoadMore(Status.LOAD_ING);
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(WHAT_LOAD_MORE, WrapperAdapter.DURATION_MAX);
        }
    }
}
