package com.wqlin.irecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by aspsine on 16/3/13.
 */
public abstract class OnLoadMoreScrollListener extends RecyclerView.OnScrollListener {
    private  Status statusLoadMore = Status.LOAD_RESET;

    private ILoadMoreAttacher mLoadMoreAttacher;

    public OnLoadMoreScrollListener(ILoadMoreAttacher loadMoreAttacher) {
        this.mLoadMoreAttacher = loadMoreAttacher;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        int i = 0;
        /*if (recyclerView.getAdapter()!=null&&recyclerView.getAdapter() instanceof WrapperAdapter) {
            WrapperAdapter adapter = (WrapperAdapter) recyclerView.getAdapter();
            if (!adapter.isEmptyView()&&adapter.isLoadMoreFooterView()) {
                View loadMoreView = adapter.getLoadMoreFooterContainer();
                int position = recyclerView.getChildLayoutPosition(loadMoreView);
                if (position >= 0) {//可见
                    adapter.setIsEmptyView(true);
                    isDragLoadMore = true;
                }
            }
        }
        isDragLoadMore = false;*/
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        mLoadMoreAttacher.onScrollStateChanged(newState);
        /*RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();


        boolean triggerCondition = visibleItemCount > 0
                && newState == RecyclerView.SCROLL_STATE_IDLE
                && canTriggerLoadMore(recyclerView);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            Log.e(OnLoadMoreScrollListener.class.getSimpleName(), "SCROLL_STATE_IDLE");
        }
        if (triggerCondition) {
            onLoadMore(recyclerView);
        }*/
    }

    public boolean canTriggerLoadMore(RecyclerView recyclerView) {
        View lastChild = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
        int position = recyclerView.getChildLayoutPosition(lastChild);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int totalItemCount = layoutManager.getItemCount();
        return totalItemCount - 1 == position;
    }

    /*public void addLoadMoreFooter(RecyclerView recyclerView) {
        if (recyclerView.getAdapter()!=null&&recyclerView.getAdapter() instanceof WrapperAdapter) {
            WrapperAdapter adapter = (WrapperAdapter) recyclerView.getAdapter();
            if (!adapter.isLoadMoreFooterView()) {
                adapter.setIsLoadMoreView(true);
            }

        }
    }
    public void removeLoadMoreFooter(RecyclerView recyclerView) {
        if (recyclerView.getAdapter()!=null&&recyclerView.getAdapter() instanceof WrapperAdapter&&recyclerView instanceof IRecyclerView) {
            WrapperAdapter adapter = (WrapperAdapter) recyclerView.getAdapter();
            if (adapter.isLoadMoreFooterView()) {
                View loadMoreView = adapter.getLoadMoreFooterContainer();
                int position = recyclerView.getChildLayoutPosition(loadMoreView);
                if (position >= 0) {//可见
                    int lastBottom=loadMoreView.getBottom();
                    int recyclerBottom =  recyclerView.getBottom()-recyclerView.getPaddingBottom();
                    if (adapter.isEmptyView()) {
                        adapter.setIsEmptyView(false);
//                            onLoadMore(recyclerView);
                    }
                    if (lastBottom > recyclerBottom) {
                        adapter.setIsLoadMoreView(false);
                    } else {
                        onLoadMore(recyclerView);
                    }
                }
            }

        }
    }*/

    public abstract void onLoadMore(RecyclerView recyclerView);
}
