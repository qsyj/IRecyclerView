package com.wqlin.irecyclerview;

import android.support.v7.widget.DefaultItemAnimator;

/**
 * Created by wqlin on 2017/9/6.
 */

public class RemoveItemAnimator extends DefaultItemAnimator{
    private long moveDuration = 250;
    @Override
    public long getMoveDuration() {
        return moveDuration;
    }
    public void setMoveDuration(long moveDuration) {
        this.moveDuration = moveDuration;
    }
}
