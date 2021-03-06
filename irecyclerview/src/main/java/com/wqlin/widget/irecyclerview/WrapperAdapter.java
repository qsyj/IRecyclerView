package com.wqlin.widget.irecyclerview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by aspsine on 16/3/12.
 */
public class WrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int REFRESH_HEADER = Integer.MIN_VALUE;
    public static final int HEADER = Integer.MIN_VALUE + 1;
    public static final int FOOTER = Integer.MAX_VALUE - 2;
    public static final int LOAD_MORE_FOOTER = Integer.MAX_VALUE-1;
    public static final int EMPTY_FOOTER = Integer.MAX_VALUE;

    private final RecyclerView.Adapter mAdapter;

    private final RefreshHeaderLayout mRefreshHeaderContainer;

    /**
     * 上拉加载的脚部View
     */
    private final FrameLayout mLoadMoreFooterContainer;

    private final LinearLayout mHeaderContainer;

    private final LinearLayout mFooterContainer;

    /**
     * 空View
     */
    private View mEmptyView;
    /**
     * 是否添加mEmptyView
     */
    private boolean isEmptyView;
    /**
     * 是否添加mLoadMoreFooterContainer
     */
    private boolean isLoadMoreFooterView;

    private RecyclerView mRecyclerView;

    /**
     * 空View的高度
     */
    private int emptyViewHeght = -1;

    /**
     * 移除mLoadMoreFooterContainer  emptyViewHeght时动画
     */
    private RemoveItemAnimator mRemoveItemAnimator;

    /**
     * 动画最大时间
     */
    public static final long DURATION_MAX = 500;
    /**
     * 一像素动画时间
     */
    private final double DURATION_UNIT = 7;

    /**
     * mHandler处理类型 移除动画
     */
    private final int WHAT_REMOVE_ANIM = 1;

    /**
     * mHandler处理类型 恢复默认动画
     */
    private final int WHAT_RESET_ITEM_ANIM = 2;

    /**
     * 正在移除动画
     */
    private boolean isRemoveAnim;
    private Handler mHandler;


    private RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            WrapperAdapter.this.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            WrapperAdapter.this.notifyItemRangeChanged(positionStart + 2, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            WrapperAdapter.this.notifyItemRangeChanged(positionStart + 2, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            WrapperAdapter.this.notifyItemRangeInserted(positionStart + 2, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            WrapperAdapter.this.notifyItemRangeRemoved(positionStart + 2, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
//            WrapperAdapter.this.notifyDataSetChanged();
            WrapperAdapter.this.notifyItemMoved(fromPosition+2,toPosition+2);
        }
    };

    public WrapperAdapter(RecyclerView.Adapter adapter, RefreshHeaderLayout refreshHeaderContainer, LinearLayout headerContainer, LinearLayout footerContainer, FrameLayout loadMoreFooterContainer) {
        this.mAdapter = adapter;
        this.mRefreshHeaderContainer = refreshHeaderContainer;
        this.mHeaderContainer = headerContainer;
        this.mFooterContainer = footerContainer;
        this.mLoadMoreFooterContainer = loadMoreFooterContainer;

        mAdapter.registerAdapterDataObserver(mObserver);
    }

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
        if (itemAnimator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) itemAnimator).setSupportsChangeAnimations(false);
        }

        mRecyclerView = recyclerView;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    WrapperAdapter wrapperAdapter = (WrapperAdapter) recyclerView.getAdapter();
                    if (isFullSpanType(wrapperAdapter.getItemViewType(position))) {
                        return gridLayoutManager.getSpanCount();
                    } else if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position - 2);
                    }
                    return 1;
                }
            });
        }
    }

   /* @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        int type = getItemViewType(position);
        if (isFullSpanType(type)) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                lp.setFullSpan(true);
            }
        }
    }*/

    private boolean isFullSpanType(int type) {
        return type == REFRESH_HEADER || type == HEADER || type == FOOTER || type == LOAD_MORE_FOOTER;
    }

    @Override
    public int getItemCount() {
        int headerFootercount = isEmptyView() ? 5 : 4;
        headerFootercount = isLoadMoreFooterView() ? headerFootercount : --headerFootercount;
        return mAdapter.getItemCount() + headerFootercount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return REFRESH_HEADER;
        } else if (position == 1) {
            return HEADER;
        } else if (1 < position && position < mAdapter.getItemCount() + 2) {
            return mAdapter.getItemViewType(position - 2);
        }else {
            if (isEmptyView() && isLoadMoreFooterView()) {
                if (position == mAdapter.getItemCount() + 5-3) {
                    return FOOTER;
                } else if (position == mAdapter.getItemCount() + 5-2) {
                    return LOAD_MORE_FOOTER;
                } else if (position == mAdapter.getItemCount() + 5  - 1) {
                    return EMPTY_FOOTER;
                }
            } else if (!isEmptyView() && isLoadMoreFooterView()) {
                if (position == mAdapter.getItemCount() + 4 - 2) {
                    return FOOTER;
                } else if (position == mAdapter.getItemCount() + 4 - 1) {
                    return LOAD_MORE_FOOTER;
                }
            } else if (isEmptyView() && !isLoadMoreFooterView()) {
                if (position == mAdapter.getItemCount() + 4 - 2) {
                    return FOOTER;
                } else if (position == mAdapter.getItemCount() + 4 - 1) {
                    return EMPTY_FOOTER;
                }
            }else if (!isEmptyView() && !isLoadMoreFooterView()){
                if (position == mAdapter.getItemCount() + 3 - 1) {
                    return FOOTER;
                }
            }
        }

        throw new IllegalArgumentException("Wrong type! Position = " + position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == REFRESH_HEADER) {
            return new RefreshHeaderContainerViewHolder(mRefreshHeaderContainer);
        } else if (viewType == HEADER) {
            return new HeaderContainerViewHolder(mHeaderContainer);
        } else if (viewType == FOOTER) {
            return new FooterContainerViewHolder(mFooterContainer);
        } else if (viewType == LOAD_MORE_FOOTER) {
            return new LoadMoreFooterContainerViewHolder(mLoadMoreFooterContainer);
        } else if (viewType == EMPTY_FOOTER) {
            View view=getEmptyView(parent.getContext());
            int height=getEmptyViewHeght();
            view.setLayoutParams(new ViewGroup.LayoutParams(0, height));
            return new EmptyFooterContainerViewHolder(view);
        } else {
            return mAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    public View getEmptyView(Context context) {
        if (mEmptyView == null) {
            mEmptyView = new View(context);
        }
        return mEmptyView;
    }

    /**
     * 获取EmptyView的高度
     * @return
     */
    private int getEmptyViewHeght() {
        if (emptyViewHeght < 0) {
            if (mRecyclerView != null) {
                emptyViewHeght=(mRecyclerView.getHeight() - mRecyclerView.getPaddingBottom() - mRecyclerView.getTop())/3;
            }
        }
        return emptyViewHeght;
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler=new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    int what = msg.what;
                    switch (what) {
                        case WHAT_REMOVE_ANIM:
                            isRemoveAnim = false;
                            return true;
                        case WHAT_RESET_ITEM_ANIM:
                            if (mRecyclerView != null) {
                                mRecyclerView.setItemAnimator((RecyclerView.ItemAnimator) msg.obj);
                            }
                            return true;
                    }
                    return false;
                }
            });
        }
        return mHandler;
    }
    private void handlRemoveAnimTime(long durationRemoveAnim) {
        isRemoveAnim = true;
        getHandler().removeMessages(WHAT_REMOVE_ANIM);
        getHandler().sendEmptyMessageDelayed(WHAT_REMOVE_ANIM, durationRemoveAnim + 50);

    }

    private void resetItemAnim(long durationRemoveAnim,final RecyclerView.ItemAnimator itemAnimator) {
        Message msg = new Message();
        msg.what = WHAT_RESET_ITEM_ANIM;
        msg.obj = itemAnimator;
        mHandler.sendMessageDelayed(msg, durationRemoveAnim + 50);
    }

    public boolean isRemoveAnim() {
        return isRemoveAnim;
    }

    public void destory() {
        if (mHandler != null) {
            mHandler.removeMessages(WHAT_REMOVE_ANIM);
            mHandler.removeMessages(WHAT_RESET_ITEM_ANIM);
            mHandler = null;
        }
    }

    public void setIsEmptyView(boolean isEmptyView) {
        if (!isEmptyView && this.isEmptyView) {//mEmptyView.getParent()!=null
            View lastView = mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1);
            View emptyView=getEmptyView(mRecyclerView.getContext());
            if (lastView==emptyView) {//可见且最后一个
                int emptyBottom=emptyView.getBottom();
                int rvBottom = mRecyclerView.getBottom() - mRecyclerView.getPaddingBottom();
                int scrollHeight = getEmptyViewHeght() + rvBottom - emptyBottom;
                if (scrollHeight > 0) {
                    final RecyclerView.ItemAnimator oldItemAnimator = mRecyclerView.getItemAnimator();
                    long removeDuration=getRemoveDuration(scrollHeight);
                    getItemAnimator().setMoveDuration(removeDuration);
                    handlRemoveAnimTime(removeDuration);
                    resetItemAnim(removeDuration,oldItemAnimator);
                }
            }
            notifyItemRemoved(getItemCount() - 1);
            this.isEmptyView = isEmptyView;
        } else if (isEmptyView && !this.isEmptyView){
            this.isEmptyView = isEmptyView;
            notifyItemInserted(getItemCount()-1);
        }

        /*if (isEmptyView&&!this.isEmptyView) {
            notifyItemInserted(getItemCount()-1);
        }
        if (!isEmptyView && this.isEmptyView) {
            notifyItemRemoved(getItemCount()-1);
        }
        this.isEmptyView = isEmptyView;*/

    }



    private RemoveItemAnimator getItemAnimator() {
        if (mRemoveItemAnimator == null) {
            mRemoveItemAnimator = new RemoveItemAnimator();
        }
        return mRemoveItemAnimator;
    }

    /**
     * 动画时间
     * @param scrollHeight 像素
     * @return
     */
    private long getRemoveDuration(int scrollHeight) {
        long duration = (long) (scrollHeight*DURATION_UNIT);
        duration = duration > DURATION_MAX ? DURATION_MAX : duration;
        return duration;
    }

    public void setIsLoadMoreView(boolean isLoadMoreFooterView) {
        if (!isLoadMoreFooterView && this.isLoadMoreFooterView) {//mLoadMoreFooterContainer.getParent()!=null
            int count = isEmptyView() ? 2 : 1;
            View lastView = mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1);
            if (lastView==mLoadMoreFooterContainer) {//可见且最后一个
                int footerBottom=mLoadMoreFooterContainer.getBottom();
                int rvBottom = mRecyclerView.getBottom() - mRecyclerView.getPaddingBottom();
                int scrollHeight = mLoadMoreFooterContainer.getMeasuredHeight() + rvBottom - footerBottom;
                if (scrollHeight > 0) {
                    final RecyclerView.ItemAnimator oldItemAnimator = mRecyclerView.getItemAnimator();
                    long removeDuration=getRemoveDuration(scrollHeight);
                    getItemAnimator().setMoveDuration(removeDuration);
                    handlRemoveAnimTime(removeDuration);
                    resetItemAnim(removeDuration,oldItemAnimator);
                }
            }
            notifyItemRemoved(getItemCount() - count);
            this.isLoadMoreFooterView = isLoadMoreFooterView;
        } else if (isLoadMoreFooterView && !this.isLoadMoreFooterView){
            this.isLoadMoreFooterView = isLoadMoreFooterView;
            notifyItemInserted(getItemCount()-1);
        }
        /*if (isLoadMoreFooterView&&!this.isLoadMoreFooterView) {
            int count = isEmptyView ? 2 : 1;
            notifyItemInserted(getItemCount()-count);
        }
        if (!isLoadMoreFooterView && this.isLoadMoreFooterView) {
            int count = isEmptyView ? 2 : 1;
            notifyItemRemoved(getItemCount()-count);
        }
        this.isLoadMoreFooterView = isLoadMoreFooterView;*/
    }
    public boolean isEmptyView() {
        return isEmptyView;
    }

    public boolean isLoadMoreFooterView() {
        return isLoadMoreFooterView;
    }

    public View getLoadMoreFooterContainer() {
        return mLoadMoreFooterContainer;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (1 < position && position < mAdapter.getItemCount() + 2) {
            mAdapter.onBindViewHolder(holder, position - 2,null);
        }
    }

    public int getHeaderCount() {
        return 2;
    }

    public int getFooterCount() {
        int count = 1;
        if (isEmptyView)
            count++;
        if (isLoadMoreFooterView)
            count++;
        return count;
    }

    static class RefreshHeaderContainerViewHolder extends RecyclerView.ViewHolder {

        public RefreshHeaderContainerViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class HeaderContainerViewHolder extends RecyclerView.ViewHolder {

        public HeaderContainerViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class FooterContainerViewHolder extends RecyclerView.ViewHolder {

        public FooterContainerViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class LoadMoreFooterContainerViewHolder extends RecyclerView.ViewHolder {

        public LoadMoreFooterContainerViewHolder(View itemView) {
            super(itemView);
        }
    }
    static class EmptyFooterContainerViewHolder extends RecyclerView.ViewHolder {

        public EmptyFooterContainerViewHolder(View itemView) {
            super(itemView);
        }
    }
}
