package com.wqlin.irecyclerview;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by wqlin on 2017/9/2.
 */

public abstract class ALoadMoreFooterLayout extends FrameLayout implements IFooter{
    private Status mStatus=Status.LOAD_NULL;

    public ALoadMoreFooterLayout(@NonNull Context context) {
        super(context);
    }

    public ALoadMoreFooterLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ALoadMoreFooterLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setLoadMoreStatus(Status status) {
        if (status != this.mStatus) {
            this.mStatus = status;
            onStateChanged();
        }
    }
    public  Status getLoadMoreStatus(){
        return mStatus;
    }

    /**
     * 当状态改变时调用
     *
     */
    public void onStateChanged() {
        switch (mStatus) {
            case LOAD_RESET:
                onReset();
                break;
            case LOAD_RELEASE_TO_REFRESH:
                onReleaseToRefresh();
                break;
            case LOAD_GONE:
                onGone();
                break;

            case LOAD_ING:
                onLoding();
                break;
            case LOAD_COMPLETE:
                onComplete();
                break;
            case LOAD_ERROR:
                onError();
                break;
            case LOAD_END:
                onEnd();
                break;
            case LOAD_NULL:
                onNull();
                break;
        }
    }

    public abstract void onReset();

    public abstract void onReleaseToRefresh();

    public abstract void onGone();

    public abstract void onLoding();

    public abstract void onComplete();

    public abstract void onError();

    public abstract void onEnd();

    public abstract void onNull();

}
