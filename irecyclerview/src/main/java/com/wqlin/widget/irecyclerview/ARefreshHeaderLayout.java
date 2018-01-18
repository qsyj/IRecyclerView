package com.wqlin.widget.irecyclerview;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author wangql
 * @email wangql@leleyuntech.com
 * @date 2017/9/21 9:19
 */
public abstract class ARefreshHeaderLayout extends FrameLayout implements RefreshTrigger {
    public ARefreshHeaderLayout(@NonNull Context context) {
        super(context);
    }

    public ARefreshHeaderLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ARefreshHeaderLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
