package com.wqlin.widget.irecyclerview;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.WRecyclerView;
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

    WRecyclerView mWRecyclerView;

    /**
     * 快速滑动
     */
    private boolean isFling;

    private boolean isFull = true;

    private boolean isDragLoadMore;

    private boolean loadMoreEnabled;

    private OnLoadMoreListener mOnLoadMoreListener;

    private Handler mHandler;

    public LoadMoreAttacher(WRecyclerView wRecyclerView) {
        this.mWRecyclerView = wRecyclerView;
        mHandler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case WHAT_LOAD_EROR:
                        if (LoadMoreAttacher.this.mWRecyclerView != null) {
                            WrapperAdapter adapter = (WrapperAdapter) LoadMoreAttacher.this.mWRecyclerView.getAdapter();
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

        if (mWRecyclerView != null) {
            RecyclerView.Adapter adapter = mWRecyclerView.getAdapter();
            if (adapter != null && adapter instanceof WrapperAdapter) {
                WrapperAdapter wrapperAdapter = (WrapperAdapter) adapter;
                wrapperAdapter.destory();
            }
        }
        mWRecyclerView = null;
        mOnLoadMoreListener = null;
        mHandler = null;
    }

    @Override
    public int getIAdapterCount() {
        if (mWRecyclerView ==null)
            return 0;
        RecyclerView.Adapter adapter = mWRecyclerView.getIAdapter();
        if (adapter==null)
            return 0;
        return mWRecyclerView.getIAdapter().getItemCount();
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

        View lastView = mWRecyclerView.getChildAt(mWRecyclerView.getChildCount()-1);
        if (lastView.getBottom()< mWRecyclerView.getHeight()- mWRecyclerView.getPaddingBottom()) {
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

        if (mWRecyclerView != null) {
            Status status = mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus();
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
        if (mWRecyclerView ==null)
            return;
        Status status = mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus();
        if (isFull&& mWRecyclerView.getLoadMoreFooterView() != null
                && status!= Status.LOAD_END) {
            if (dy > 0) {//下滑
                isDragLoadMore = false;
                if (getStatusLoadMore() == Status.LOAD_RELEASE_TO_REFRESH) {
                    WrapperAdapter adapter = (WrapperAdapter) mWRecyclerView.getAdapter();
                    if (adapter.isLoadMoreFooterView()) {
                        View loadMoreView = adapter.getLoadMoreFooterContainer();
                        int position = mWRecyclerView.getChildLayoutPosition(loadMoreView);
                        if (position >= 0) {//可见
                            int lastBottom=loadMoreView.getBottom();
                            int recyclerBottom =  mWRecyclerView.getHeight()- mWRecyclerView.getPaddingBottom();
                            if (lastBottom > recyclerBottom) {
                                setStatusWhenMove(Status.LOAD_RESET);
                            }

                        }
                    }
                }
            } else if (dy < 0) {//上滑
                if (mWRecyclerView.getAdapter()!=null&& mWRecyclerView.getAdapter() instanceof WrapperAdapter) {
                    WrapperAdapter adapter = (WrapperAdapter) mWRecyclerView.getAdapter();
                    if (adapter.isLoadMoreFooterView()) {

                        View loadMoreView = adapter.getLoadMoreFooterContainer();
                        int position = mWRecyclerView.getChildLayoutPosition(loadMoreView);
                        if (position >= 0) {//可见
                            if (!adapter.isEmptyView()) {
                                setStatusWhenMove(Status.LOAD_RESET);
                                if (mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() == Status.LOAD_ERROR) {
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
                        int recyclerBottom =  mWRecyclerView.getHeight()- mWRecyclerView.getPaddingBottom();
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

        if ( mWRecyclerView !=null&& mWRecyclerView.getLoadMoreFooterView()!= null) {
            mWRecyclerView.getLoadMoreFooterView().setLoadMoreStatus(statusLoadMore);
        }
    }

    @Override
    public Status getStatusLoadMore() {
        if (mWRecyclerView !=null&& mWRecyclerView.getLoadMoreFooterView() != null) {
            return mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus();
        }
        return Status.LOAD_RESET;
    }

    @Override
    public boolean isAnim() {
        if (mWRecyclerView ==null)
            return false;
        RecyclerView.Adapter adapter = mWRecyclerView.getAdapter();
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
        if (mWRecyclerView != null&&
                mWRecyclerView.getLoadMoreFooterView()!=null&&
                mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus()!= Status.LOAD_ING&&
                mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus()!= Status.LOAD_END) {
            LinearLayout footerContainer = mWRecyclerView.getFooterContainer();
            if (mWRecyclerView.getChildLayoutPosition(footerContainer)<0) {//不可见
                mWRecyclerView.getLoadMoreFooterView().setLoadMoreStatus(Status.LOAD_RESET);
            }
        }
    }

    @Override
    public void addLoadMoreFooter() {
        if (!loadMoreEnabled)
            return;
        if (isAnim())
            return;
        if (mWRecyclerView ==null)
            return;

        if (mWRecyclerView.getAdapter()!=null&& mWRecyclerView.getAdapter() instanceof WrapperAdapter) {
            WrapperAdapter adapter = (WrapperAdapter) mWRecyclerView.getAdapter();
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
        if (mWRecyclerView ==null)
            return;

        if (mWRecyclerView.getAdapter()!=null&& mWRecyclerView.getAdapter() instanceof WrapperAdapter&& mWRecyclerView instanceof WRecyclerView) {
            WrapperAdapter adapter = (WrapperAdapter) mWRecyclerView.getAdapter();
            if (true) {//adapter.isLoadMoreFooterView()
                View loadMoreView = adapter.getLoadMoreFooterContainer();
                int position = mWRecyclerView.getChildLayoutPosition(loadMoreView);
                if (position >= 0) {//可见
                    int lastBottom = loadMoreView.getBottom();
                    int recyclerBottom = mWRecyclerView.getHeight() - mWRecyclerView.getPaddingBottom();
                    if (adapter.isEmptyView()) {
                        adapter.setIsEmptyView(false);
//                            onLoadMore(recyclerView);
                    }
                    if (mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() == Status.LOAD_ERROR) {
                        setStatusLoadMore(Status.LOAD_RESET);
                        adapter.setIsLoadMoreView(false);
                    } else {
                        if (lastBottom > recyclerBottom && mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() != Status.LOAD_END) {
                            if (mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus()!= Status.LOAD_ING)
                                setStatusLoadMore(Status.LOAD_RESET);
                            adapter.setIsLoadMoreView(false);
                        } else {
                            if (getStatusLoadMore() == Status.LOAD_RELEASE_TO_REFRESH) {
                                onLoadMore();
                            }

                        }
                    }

                } else {
                    if (mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() != Status.LOAD_END
                            && mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() != Status.LOAD_ING) {
                        adapter.setIsLoadMoreView(false);
                    }
                    if (adapter.isEmptyView()) {
                        adapter.setIsEmptyView(false);
                    }
                    if (mWRecyclerView.getLoadMoreFooterView().getLoadMoreStatus() == Status.LOAD_ERROR)
                        setStatusLoadMore(Status.LOAD_RESET);
                }
            }

        }
    }

    @Override
    public void setLoadMoreComplete(boolean isLoadMoreComplete) {
        if (!loadMoreEnabled)
            return;
        if (mWRecyclerView ==null)
            return;

        if (isLoadMoreComplete&&getStatusLoadMore()== Status.LOAD_ING&& mWRecyclerView.getAdapter()!=null&& mWRecyclerView.getAdapter() instanceof WrapperAdapter) {
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
        if (mWRecyclerView ==null)
            return;

        if (getStatusLoadMore()== Status.LOAD_ING&& mWRecyclerView.getAdapter()!=null&& mWRecyclerView.getAdapter() instanceof WrapperAdapter) {
            setStatusLoadMore(Status.LOAD_ERROR);
            final WrapperAdapter adapter = (WrapperAdapter) mWRecyclerView.getAdapter();
            adapter.setIsEmptyView(false);
            if (adapter.isLoadMoreFooterView()) {
                View loadMoreView = adapter.getLoadMoreFooterContainer();
                int position = mWRecyclerView.getChildLayoutPosition(loadMoreView);
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
        if (mWRecyclerView ==null)
            return;

        RecyclerView.LayoutManager layoutManager = mWRecyclerView.getLayoutManager();
        if (newState != RecyclerView.SCROLL_STATE_IDLE)
            return;

        WrapperAdapter adapter = (WrapperAdapter) mWRecyclerView.getAdapter();
        if (adapter.isLoadMoreFooterView()) {
            View loadMoreView = adapter.getLoadMoreFooterContainer();
            int position = mWRecyclerView.getChildLayoutPosition(loadMoreView);
            if (position >= 0) {//可见
                int loadMoreViewBottom = loadMoreView.getBottom();
                int recyclerBottom =  mWRecyclerView.getHeight()- mWRecyclerView.getPaddingBottom();
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
