package android.support.v7.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.wqlin.widget.irecyclerview.ALoadMoreFooterLayout;
import com.wqlin.widget.irecyclerview.ARefreshHeaderLayout;
import com.wqlin.widget.irecyclerview.ILoadMoreAttacher;
import com.wqlin.widget.irecyclerview.LoadMoreAttacher;
import com.wqlin.widget.irecyclerview.OnLoadMoreListener;
import com.wqlin.widget.irecyclerview.OnLoadMoreScrollListener;
import com.wqlin.widget.irecyclerview.OnRefreshListener;
import com.wqlin.widget.irecyclerview.RefreshHeaderLayout;
import com.wqlin.widget.irecyclerview.RefreshTrigger;
import com.wqlin.widget.irecyclerview.SimpleAnimatorListener;
import com.wqlin.widget.irecyclerview.WrapperAdapter;


/**
 * 请不要使用getAdapter(), 使用getIAdapter()
 * <p>
 * 注意销毁调用{@link #destory()}
 * Created by aspsine on 16/3/3.
 */
public class WRecyclerView extends RecyclerView {
    private static final String TAG = WRecyclerView.class.getSimpleName();

    private static final int STATUS_DEFAULT = 0;

    private static final int STATUS_SWIPING_TO_REFRESH = 1;

    private static final int STATUS_RELEASE_TO_REFRESH = 2;

    private static final int STATUS_REFRESHING = 3;

    private static final boolean DEBUG = false;

    private int mStatus;

    private boolean mIsAutoRefreshing;

    private boolean mRefreshEnabled;

    private boolean mLoadMoreEnabled;
    /**
     * 当父类是NestedScrollingParent时,当滑动到边缘时是否显示Glow(当执行scrollByInternal() <p>
     * -->dispatchNestedScroll()-->parent onNestedScroll()后int[] offsetInWindow的值未改变并且View大小未改变,则重写dispatchNestedScroll()返回false)
     */
    private boolean showOverScrollWhenScrollingParent = false;
    /**
     * 执行{@link #dispatchNestedScroll(int, int, int, int, int[])}时回调 mDispatchNestedScrollListener
     */
    private DispatchNestedScrollListener mDispatchNestedScrollListener;
    private int mRefreshFinalMoveOffset;

    private OnRefreshListener mOnRefreshListener;

    private OnLoadMoreListener mOnLoadMoreListener;

    private RefreshHeaderLayout mRefreshHeaderContainer;

    private FrameLayout mLoadMoreFooterContainer;

    private LinearLayout mHeaderViewContainer;

    private LinearLayout mFooterViewContainer;

    private View mRefreshHeaderView;

    private ALoadMoreFooterLayout mLoadMoreFooterView;

    ILoadMoreAttacher mLoadMoreAttacher;

    /**
     * 阻尼系数
     */
    private final float scale = 2.5f;

    public WRecyclerView(Context context) {
        this(context, null);
    }

    public WRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setStatus(STATUS_DEFAULT);
        setLoadMoreAttacher();
        mOnLoadMoreScrollListener= new OnLoadMoreScrollListener(mLoadMoreAttacher) {
            @Override
            public void onLoadMore(RecyclerView recyclerView) {
                if (mOnLoadMoreListener != null && mStatus == STATUS_DEFAULT) {
                    mOnLoadMoreListener.onLoadMore();
                }
            }
        };
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (mRefreshHeaderView != null) {
            if (mRefreshHeaderView.getMeasuredHeight() > mRefreshFinalMoveOffset) {
                mRefreshFinalMoveOffset = 0;
            }
        }
    }

    @Override
    boolean scrollByInternal(int x, int y, MotionEvent ev) {
        if (mLoadMoreAttacher != null) {
            if (mLoadMoreAttacher.isAnim())
                return false;
        }

/*============================================================上拉加载s 阻尼效果======================================================================*/
        if (mLoadMoreAttacher.isDragLoadMore()) {
            x = (int) ((float) x / scale);
            y = (int) ((float) y / scale);
        }
/*============================================================上拉加载s======================================================================*/

        return super.scrollByInternal(x, y, ev);
    }

    public void destory() {
        if (mLoadMoreAttacher != null) {
            mLoadMoreAttacher.destory();
            mLoadMoreAttacher = null;
        }
    }

    private void setLoadMoreAttacher() {
        mLoadMoreAttacher = new LoadMoreAttacher(this);
    }

    public void setRefreshEnabled(boolean enabled) {
        this.mRefreshEnabled = enabled;
    }

    public void setLoadMoreEnabled(boolean enabled) {
        this.mLoadMoreEnabled = enabled;
        if (mLoadMoreEnabled) {
            removeOnScrollListener(mOnLoadMoreScrollListener);
            addOnScrollListener(mOnLoadMoreScrollListener);

        } else {
            removeOnScrollListener(mOnLoadMoreScrollListener);
        }
        mLoadMoreAttacher.setLoadMoreEnabled(mLoadMoreEnabled);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
/*============================================================上拉加载s======================================================================*/
        boolean fling=super.fling(velocityX, velocityY);
        mLoadMoreAttacher.setFling(fling);
//        Log.e(IRecyclerView.TAG, "fling:" + fling);
/*============================================================上拉加载e======================================================================*/
        return fling;
    }

    public boolean isFling() {
        return mLoadMoreAttacher.isFling();
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mOnRefreshListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mOnLoadMoreListener = listener;
        mLoadMoreAttacher.setOnLoadMoreListener(mOnLoadMoreListener);
    }


    public void setRefreshing(boolean refreshing) {
        if (mStatus == STATUS_DEFAULT && refreshing) {
            this.mIsAutoRefreshing = true;
            setStatus(STATUS_SWIPING_TO_REFRESH);
            startScrollDefaultStatusToRefreshingStatus();
        } else if (mStatus == STATUS_REFRESHING && !refreshing) {
            this.mIsAutoRefreshing = false;
            startScrollRefreshingStatusToDefaultStatus();
        } else {
            this.mIsAutoRefreshing = false;
            Log.w(TAG, "isRefresh = " + refreshing + " current status = " + mStatus);
        }
    }

    public void setRefreshComplete() {
        setRefreshing(false);
        mLoadMoreAttacher.setLoadMoreReset();
    }

    public void setRefreshError() {
        setRefreshing(false);
    }

    public void setRefreshFinalMoveOffset(int refreshFinalMoveOffset) {
        this.mRefreshFinalMoveOffset = refreshFinalMoveOffset;
    }

    public void setRefreshHeaderView(ARefreshHeaderLayout refreshHeaderView) {
        if (mRefreshHeaderView != null) {
            removeRefreshHeaderView();
        }
        if (mRefreshHeaderView != refreshHeaderView) {
            this.mRefreshHeaderView = refreshHeaderView;
            ensureRefreshHeaderContainer();
            mRefreshHeaderContainer.addView(refreshHeaderView);
        }
    }

    public void setLoadMoreFooterView(ALoadMoreFooterLayout loadMoreFooterView) {
        if (mLoadMoreFooterView != null) {
            removeLoadMoreFooterView();
        }
        if (mLoadMoreFooterView != loadMoreFooterView) {
            this.mLoadMoreFooterView = loadMoreFooterView;
            ensureLoadMoreFooterContainer();
            mLoadMoreFooterContainer.addView(loadMoreFooterView);
        }
    }

    public void setLoadMoreFooterView(@LayoutRes int loadMoreFooterLayoutRes) {
        ensureLoadMoreFooterContainer();
        final ALoadMoreFooterLayout loadMoreFooter = (ALoadMoreFooterLayout) LayoutInflater.from(getContext()).inflate(loadMoreFooterLayoutRes, mLoadMoreFooterContainer, false);
        if (loadMoreFooter != null) {
            setLoadMoreFooterView(loadMoreFooter);
        }
    }

    public View getRefreshHeaderView() {
        return mRefreshHeaderView;
    }

    public ALoadMoreFooterLayout getLoadMoreFooterView() {
        return mLoadMoreFooterView;
    }

    public LinearLayout getHeaderContainer() {
        ensureHeaderViewContainer();
        return mHeaderViewContainer;
    }

    public LinearLayout getFooterContainer() {
        ensureFooterViewContainer();
        return mFooterViewContainer;
    }

    public void addHeaderView(View headerView) {
        ensureHeaderViewContainer();
        mHeaderViewContainer.addView(headerView);
        Adapter adapter = getAdapter();
        if (adapter != null) {
            adapter.notifyItemChanged(1);
        }
    }

    public void addFooterView(View footerView) {
        ensureFooterViewContainer();
        mFooterViewContainer.addView(footerView);
        Adapter adapter = getAdapter();
        if (adapter != null) {
            adapter.notifyItemChanged(adapter.getItemCount() - 2);
        }
    }

    /*public RecyclerView.Adapter getIAdapter() {
        final WrapperAdapter wrapperAdapter = (WrapperAdapter) getAdapter();
        return wrapperAdapter.getAdapter();
    }

    public void setIAdapter(Adapter adapter) {
        ensureRefreshHeaderContainer();
        ensureHeaderViewContainer();
        ensureFooterViewContainer();
        ensureLoadMoreFooterContainer();
        setAdapter(new WrapperAdapter(adapter, mRefreshHeaderContainer, mHeaderViewContainer, mFooterViewContainer, mLoadMoreFooterContainer));
    }*/
    @Override
    public void setAdapter(Adapter adapter) {
        ensureRefreshHeaderContainer();
        ensureHeaderViewContainer();
        ensureFooterViewContainer();
        ensureLoadMoreFooterContainer();
        super.setAdapter(new WrapperAdapter(adapter, mRefreshHeaderContainer, mHeaderViewContainer, mFooterViewContainer, mLoadMoreFooterContainer));
    }

    /**
     * 请不使用该方法  因为得到的Adapter并不是setAdapter(Adapter adapter)中的adapter <p>
     * 请使用getIAdapter()
     * @return
     */
    @Deprecated
    @Override
    public Adapter getAdapter() {
        return super.getAdapter();
    }

    public RecyclerView.Adapter getIAdapter() {
        final WrapperAdapter wrapperAdapter = (WrapperAdapter) getAdapter();
        return wrapperAdapter.getAdapter();
    }

    private void ensureRefreshHeaderContainer() {
        if (mRefreshHeaderContainer == null) {
            mRefreshHeaderContainer = new RefreshHeaderLayout(getContext());
            mRefreshHeaderContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        }
    }

    private void ensureLoadMoreFooterContainer() {
        if (mLoadMoreFooterContainer == null) {
            mLoadMoreFooterContainer = new FrameLayout(getContext());
            mLoadMoreFooterContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void ensureHeaderViewContainer() {
        if (mHeaderViewContainer == null) {
            mHeaderViewContainer = new LinearLayout(getContext());
            mHeaderViewContainer.setOrientation(LinearLayout.VERTICAL);
            mHeaderViewContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void ensureFooterViewContainer() {
        if (mFooterViewContainer == null) {
            mFooterViewContainer = new LinearLayout(getContext());
            mFooterViewContainer.setOrientation(LinearLayout.VERTICAL);
            mFooterViewContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void removeRefreshHeaderView() {
        if (mRefreshHeaderContainer != null) {
            mRefreshHeaderContainer.removeView(mRefreshHeaderView);
        }
    }

    private void removeLoadMoreFooterView() {
        if (mLoadMoreFooterContainer != null) {
            mLoadMoreFooterContainer.removeView(mLoadMoreFooterView);
        }
    }

    private int mActivePointerId = -1;
    private int mLastTouchX = 0;
    private int mLastTouchY = 0;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        final int action = MotionEventCompat.getActionMasked(e);
        final int actionIndex = MotionEventCompat.getActionIndex(e);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
/*============================================================上拉加载s======================================================================*/
                mLoadMoreAttacher.setFling(false);
                mLoadMoreAttacher.resetLoadMoreFooterViewStatus();
                if (mStatus!=STATUS_REFRESHING&&mLoadMoreAttacher.getIAdapterCount() > 0) {//头部正在刷新不添加LoadMoreFooterView 并且item count(不包括头部脚部)必须大于0
                    mLoadMoreAttacher.setFull();
                    if (mLoadMoreAttacher.isFull()) {
                        mLoadMoreAttacher.addLoadMoreFooter();
                    }
                }
/*============================================================上拉加载e======================================================================*/

                mActivePointerId = MotionEventCompat.getPointerId(e, 0);
                mLastTouchX = (int) (MotionEventCompat.getX(e, actionIndex) + 0.5f);
                mLastTouchY = (int) (MotionEventCompat.getY(e, actionIndex) + 0.5f);
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                mActivePointerId = MotionEventCompat.getPointerId(e, actionIndex);
                mLastTouchX = (int) (MotionEventCompat.getX(e, actionIndex) + 0.5f);
                mLastTouchY = (int) (MotionEventCompat.getY(e, actionIndex) + 0.5f);
            }
            break;

            case MotionEventCompat.ACTION_POINTER_UP: {

                onPointerUp(e);
            }
            break;
            case  MotionEvent.ACTION_UP:
/*============================================================上拉加载s======================================================================*/
                mLoadMoreAttacher.removeLoadMoreFooter();
/*============================================================上拉加载e======================================================================*/
                break;

        }

        return super.onInterceptTouchEvent(e);
    }

    /**
     * 当父类是NestedScrollingParent时,当滑动到边缘时是否显示Glow<p>
     * (当执行scrollByInternal() -->dispatchNestedScroll()-->parent onNestedScroll()后int[] offsetInWindow的值未改变并且View大小未改变,则重写dispatchNestedScroll()返回false) <p>
     *  若需要自定义条件判断则使用   @link {@link #setDispatchNestedScrollListener(DispatchNestedScrollListener)}}
     *
     * @param showOverScrollWhenScrollingParent
     */
    public void showGlowWhenScrollingParent(boolean showOverScrollWhenScrollingParent) {
        this.showOverScrollWhenScrollingParent = showOverScrollWhenScrollingParent;
    }

    /**
     * 执行{@link #dispatchNestedScroll(int, int, int, int, int[])}时回调 进行拦截
     */
    public void setDispatchNestedScrollListener(DispatchNestedScrollListener dispatchNestedScrollListener) {
        mDispatchNestedScrollListener = dispatchNestedScrollListener;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        if (mDispatchNestedScrollListener != null) {
            mDispatchNestedScrollListener.dispatchNestedScrollPre(this,dxConsumed,dyConsumed,dxUnconsumed,dyUnconsumed,offsetInWindow);
        }
        int[] oldOffsetInWindow = new int[2];
        oldOffsetInWindow[0] = offsetInWindow[0];
        oldOffsetInWindow[1] = offsetInWindow[1];
        int oldWidth = getWidth();
        int oldHeight = getHeight();
        boolean dispatchNestedScroll = super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
        if (mDispatchNestedScrollListener != null) {
            return mDispatchNestedScrollListener.dispatchNestedScroll(this, dispatchNestedScroll, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
        } else {
            if (dispatchNestedScroll) {
                if (oldOffsetInWindow[0] == offsetInWindow[0] &&
                        oldOffsetInWindow[1] == offsetInWindow[1] &&
                        oldWidth==getWidth()&&
                        oldHeight==getHeight()) {
                    return false;
                }
            }
            return dispatchNestedScroll;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        final int action = MotionEventCompat.getActionMasked(e);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {

                final int index = MotionEventCompat.getActionIndex(e);
                mActivePointerId = MotionEventCompat.getPointerId(e, 0);
                mLastTouchX = getMotionEventX(e, index);
                mLastTouchY = getMotionEventY(e, index);
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                final int index = MotionEventCompat.findPointerIndex(e, mActivePointerId);
                if (index < 0) {
//                    Log.e(TAG, "Error processing scroll; pointer index for id " + index + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                final int x = getMotionEventX(e, index);
                final int y = getMotionEventY(e, index);

                final int dx = x - mLastTouchX;
                final int dy = y - mLastTouchY;
/*============================================================上拉加载s======================================================================*/
                mLoadMoreAttacher.handleMove(dy);
/*=============================================================上拉加载e==========================================================================*/

                mLastTouchX = x;
                mLastTouchY = y;

                final boolean triggerCondition = isEnabled() && mRefreshEnabled && mRefreshHeaderView != null && isFingerDragging() && canTriggerRefresh();
                if (DEBUG) {
//                    Log.i(TAG, "triggerCondition = " + triggerCondition + "; mStatus = " + mStatus + "; dy = " + dy);
                }
                if (triggerCondition) {

                    final int refreshHeaderContainerHeight = mRefreshHeaderContainer.getMeasuredHeight();
                    final int refreshHeaderViewHeight = mRefreshHeaderView.getMeasuredHeight();

                    if (dy > 0 && mStatus == STATUS_DEFAULT) {
                        setStatus(STATUS_SWIPING_TO_REFRESH);
                        mRefreshTrigger.onStart(false, refreshHeaderViewHeight, mRefreshFinalMoveOffset);
                    } else if (dy < 0) {
                        if (mStatus == STATUS_SWIPING_TO_REFRESH && refreshHeaderContainerHeight <= 0) {
                            setStatus(STATUS_DEFAULT);
                        }
                        if (mStatus == STATUS_DEFAULT) {
                            break;
                        }
                    }

                    if (mStatus == STATUS_SWIPING_TO_REFRESH || mStatus == STATUS_RELEASE_TO_REFRESH) {
                        if (refreshHeaderContainerHeight >= refreshHeaderViewHeight) {
                            setStatus(STATUS_RELEASE_TO_REFRESH);
                        } else {
                            setStatus(STATUS_SWIPING_TO_REFRESH);
                        }
                        fingerMove(dy);
                        return true;
                    }
                }
            }
            break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(e);
                mActivePointerId = MotionEventCompat.getPointerId(e, index);
                mLastTouchX = getMotionEventX(e, index);
                mLastTouchY = getMotionEventY(e, index);
            }
            break;

            case MotionEventCompat.ACTION_POINTER_UP: {
                onPointerUp(e);
            }
            break;

            case MotionEvent.ACTION_UP: {
/*============================================================上拉加载s======================================================================*/
//                Log.e(IRecyclerView.TAG, "ACTION_POINTER_UP Status:" + mLoadMoreAttacher.getStatusLoadMore());
                mLoadMoreAttacher.setDragLoadMore(false);
                mLoadMoreAttacher.removeLoadMoreFooter();
/*============================================================上拉加载e======================================================================*/
                onFingerUpStartAnimating();
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                onFingerUpStartAnimating();
            }
            break;
        }
        return super.onTouchEvent(e);
    }

    private boolean isFingerDragging() {
        return getScrollState() == SCROLL_STATE_DRAGGING;
    }

    public boolean canTriggerRefresh() {
        final Adapter adapter = getAdapter();
        if (adapter == null || adapter.getItemCount() <= 0) {
            return true;
        }
        View firstChild = getChildAt(0);
        int position = getChildLayoutPosition(firstChild);
        if (position == 0) {
            if (firstChild.getTop() == mRefreshHeaderContainer.getTop()) {
                return true;
            }
        }
        return false;
    }

    private int getMotionEventX(MotionEvent e, int pointerIndex) {
        return (int) (MotionEventCompat.getX(e, pointerIndex) + 0.5f);
    }

    private int getMotionEventY(MotionEvent e, int pointerIndex) {
        return (int) (MotionEventCompat.getY(e, pointerIndex) + 0.5f);
    }

    private void onFingerUpStartAnimating() {
        if (mStatus == STATUS_RELEASE_TO_REFRESH) {
            startScrollReleaseStatusToRefreshingStatus();
        } else if (mStatus == STATUS_SWIPING_TO_REFRESH) {
            startScrollSwipingToRefreshStatusToDefaultStatus();
        }
    }

    private void onPointerUp(MotionEvent e) {
        final int actionIndex = MotionEventCompat.getActionIndex(e);
        if (MotionEventCompat.getPointerId(e, actionIndex) == mActivePointerId) {
            // Pick a new pointer to pick up the slack.
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(e, newIndex);
            mLastTouchX = getMotionEventX(e, newIndex);
            mLastTouchY = getMotionEventY(e, newIndex);
        }
    }

    private void fingerMove(int dy) {
        int ratioDy = (int) (dy * 0.5f + 0.5f);
        int offset = mRefreshHeaderContainer.getMeasuredHeight();
        int finalDragOffset = mRefreshFinalMoveOffset;

        int nextOffset = offset + ratioDy;
        if (finalDragOffset > 0) {
            if (nextOffset > finalDragOffset) {
                ratioDy = finalDragOffset - offset;
            }
        }

        if (nextOffset < 0) {
            ratioDy = -offset;
        }
        move(ratioDy);
    }

    private void move(int dy) {
        if (dy != 0) {
            int height = mRefreshHeaderContainer.getMeasuredHeight() + dy;
            setRefreshHeaderContainerHeight(height);
            mRefreshTrigger.onMove(false, false, height);
        }
    }

    private void setRefreshHeaderContainerHeight(int height) {
        mRefreshHeaderContainer.getLayoutParams().height = height;
        mRefreshHeaderContainer.requestLayout();
    }

    private void startScrollDefaultStatusToRefreshingStatus() {
        mRefreshTrigger.onStart(true, mRefreshHeaderView.getMeasuredHeight(), mRefreshFinalMoveOffset);

        int targetHeight = mRefreshHeaderView.getMeasuredHeight();
        int currentHeight = mRefreshHeaderContainer.getMeasuredHeight();
        startScrollAnimation(400, new AccelerateInterpolator(), currentHeight, targetHeight);
    }

    private void startScrollSwipingToRefreshStatusToDefaultStatus() {
        final int targetHeight = 0;
        final int currentHeight = mRefreshHeaderContainer.getMeasuredHeight();
        startScrollAnimation(300, new DecelerateInterpolator(), currentHeight, targetHeight);
    }

    private void startScrollReleaseStatusToRefreshingStatus() {
        mRefreshTrigger.onRelease();

        final int targetHeight = mRefreshHeaderView.getMeasuredHeight();
        final int currentHeight = mRefreshHeaderContainer.getMeasuredHeight();
        startScrollAnimation(300, new DecelerateInterpolator(), currentHeight, targetHeight);
    }

    private void startScrollRefreshingStatusToDefaultStatus() {
        mRefreshTrigger.onComplete();

        final int targetHeight = 0;
        final int currentHeight = mRefreshHeaderContainer.getMeasuredHeight();
        startScrollAnimation(400, new DecelerateInterpolator(), currentHeight, targetHeight);
    }

    /*============================================================上拉加载s======================================================================*/
    public void setLoadMoreComplete(boolean isLoadMoreComplete) {
        mLoadMoreAttacher.setLoadMoreComplete(isLoadMoreComplete);
    }

    public void setLoadMoreReset() {
        mLoadMoreAttacher.setLoadMoreReset();
    }
    public void setLoadMoreStatusNull() {
        mLoadMoreAttacher.setLoadMoreNull();
    }
    public void setLoadMoreEnd() {
        mLoadMoreAttacher.setLoadMoreEnd();
    }

    public void setLoadMoreError() {
        mLoadMoreAttacher.setLoadMoreError();
    }
/*============================================================上拉加载e======================================================================*/

    private ValueAnimator mScrollAnimator;

    private void startScrollAnimation(final int time, final Interpolator interpolator, int value, int toValue) {
        if (mScrollAnimator == null) {
            mScrollAnimator = new ValueAnimator();
        }
        //cancel
        mScrollAnimator.removeAllUpdateListeners();
        mScrollAnimator.removeAllListeners();
        mScrollAnimator.cancel();

        //reset new value
        mScrollAnimator.setIntValues(value, toValue);
        mScrollAnimator.setDuration(time);
        mScrollAnimator.setInterpolator(interpolator);
        mScrollAnimator.addUpdateListener(mAnimatorUpdateListener);
        mScrollAnimator.addListener(mAnimationListener);
        mScrollAnimator.start();
    }

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final int height = (Integer) animation.getAnimatedValue();
            setRefreshHeaderContainerHeight(height);
            switch (mStatus) {
                case STATUS_SWIPING_TO_REFRESH: {
                    mRefreshTrigger.onMove(false, true, height);
                }
                break;

                case STATUS_RELEASE_TO_REFRESH: {
                    mRefreshTrigger.onMove(false, true, height);
                }
                break;

                case STATUS_REFRESHING: {
                    mRefreshTrigger.onMove(true, true, height);
                }
                break;
            }

        }
    };

    private Animator.AnimatorListener mAnimationListener = new SimpleAnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            int lastStatus = mStatus;

            switch (mStatus) {
                case STATUS_SWIPING_TO_REFRESH: {
                    if (mIsAutoRefreshing) {
                        mRefreshHeaderContainer.getLayoutParams().height = mRefreshHeaderView.getMeasuredHeight();
                        mRefreshHeaderContainer.requestLayout();
                        setStatus(STATUS_REFRESHING);
                        if (mOnRefreshListener != null) {
                            mOnRefreshListener.onRefresh();
                            mRefreshTrigger.onRefresh();
                        }
                    } else {
                        mRefreshHeaderContainer.getLayoutParams().height = 0;
                        mRefreshHeaderContainer.requestLayout();
                        setStatus(STATUS_DEFAULT);
                    }
                }
                break;

                case STATUS_RELEASE_TO_REFRESH: {
                    mRefreshHeaderContainer.getLayoutParams().height = mRefreshHeaderView.getMeasuredHeight();
                    mRefreshHeaderContainer.requestLayout();
                    setStatus(STATUS_REFRESHING);
                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onRefresh();
                        mRefreshTrigger.onRefresh();
                    }
                }
                break;

                case STATUS_REFRESHING: {
                    mIsAutoRefreshing = false;
                    mRefreshHeaderContainer.getLayoutParams().height = 0;
                    mRefreshHeaderContainer.requestLayout();
                    setStatus(STATUS_DEFAULT);
                    mRefreshTrigger.onReset();
                }
                break;
            }
            if (DEBUG) {
                Log.i(TAG, "onAnimationEnd " + getStatusLog(lastStatus) + " -> " + getStatusLog(mStatus) + " ;refresh view height:" + mRefreshHeaderContainer.getMeasuredHeight());
            }
        }
    };

    private RefreshTrigger mRefreshTrigger = new RefreshTrigger() {
        @Override
        public void onStart(boolean automatic, int headerHeight, int finalHeight) {
            if (mRefreshHeaderView != null && mRefreshHeaderView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) mRefreshHeaderView;
                trigger.onStart(automatic, headerHeight, finalHeight);
            }
        }

        @Override
        public void onMove(boolean finished, boolean automatic, int moved) {
            if (mRefreshHeaderView != null && mRefreshHeaderView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) mRefreshHeaderView;
                trigger.onMove(finished, automatic, moved);
            }
        }

        @Override
        public void onRefresh() {
            if (mRefreshHeaderView != null && mRefreshHeaderView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) mRefreshHeaderView;
                trigger.onRefresh();
            }
        }

        @Override
        public void onRelease() {
            if (mRefreshHeaderView != null && mRefreshHeaderView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) mRefreshHeaderView;
                trigger.onRelease();
            }
        }

        @Override
        public void onComplete() {
            if (mRefreshHeaderView != null && mRefreshHeaderView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) mRefreshHeaderView;
                trigger.onComplete();
            }
        }

        @Override
        public void onReset() {
            if (mRefreshHeaderView != null && mRefreshHeaderView instanceof RefreshTrigger) {
                RefreshTrigger trigger = (RefreshTrigger) mRefreshHeaderView;
                trigger.onReset();
            }
        }
    };

    private OnLoadMoreScrollListener mOnLoadMoreScrollListener ;

    private void setStatus(int status) {
        this.mStatus = status;
        if (DEBUG) {
            printStatusLog();
        }
    }

    private void printStatusLog() {
        Log.i(TAG, getStatusLog(mStatus));
    }

    private String getStatusLog(int status) {
        final String statusLog;
        switch (status) {
            case STATUS_DEFAULT:
                statusLog = "status_default";
                break;

            case STATUS_SWIPING_TO_REFRESH:
                statusLog = "status_swiping_to_refresh";
                break;

            case STATUS_RELEASE_TO_REFRESH:
                statusLog = "status_release_to_refresh";
                break;

            case STATUS_REFRESHING:
                statusLog = "status_refreshing";
                break;
            default:
                statusLog = "status_illegal!";
                break;
        }
        return statusLog;
    }
    public interface DispatchNestedScrollListener {

        /**
         * RecyclerView dispatchNestedScroll()之前
         */
        void dispatchNestedScrollPre(RecyclerView recyclerView,int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow);

        /**
         * RecyclerView dispatchNestedScroll()之后没有return; 该方法将改变boolean dispatchNestedScroll的值
         *
         */
        boolean dispatchNestedScroll(RecyclerView recyclerView,boolean dispatchNestedScroll,int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow);
    }
}
