package com.wqlin.irecyclerview.demo.ui.widget.footer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wqlin.irecyclerview.ALoadMoreFooterLayout;
import com.wqlin.irecyclerview.demo.R;

/**
 * Created by aspsine on 16/3/14.
 */
public class LoadMoreFooterView extends ALoadMoreFooterLayout {

    /**
     * 进度条
     */
    private ProgressBar mProgressBar;
    /**
     * 显示的文本
     */
    private TextView mHintView;
    private LinearLayout mLinearLayout;


    public LoadMoreFooterView(Context context) {
        this(context, null);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context);
    }

    /**
     * 初始化
     *
     * @param context context
     */
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.pull_to_load_footer, this, true);
        mLinearLayout = (LinearLayout) findViewById(R.id.pull_to_load_footer_content);
        mProgressBar = (ProgressBar) findViewById(R.id.pull_to_load_footer_progressbar);
        mHintView = (TextView) findViewById(R.id.pull_to_load_footer_hint_textview);
        onNull();
    }

    @Override
    public void onReset() {
        mLinearLayout.setVisibility(VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mHintView.setVisibility(VISIBLE);
        mHintView.setText(R.string.pull_to_refresh_header_hint_normal2);
    }

    @Override
    public void onReleaseToRefresh() {
        mLinearLayout.setVisibility(VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mHintView.setVisibility(View.VISIBLE);
        mHintView.setText(R.string.pull_to_refresh_header_hint_ready);
    }

    @Override
    public void onGone() {
        mLinearLayout.setVisibility(GONE);
    }

    @Override
    public void onLoding() {
        mLinearLayout.setVisibility(VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mHintView.setVisibility(View.VISIBLE);
        mHintView.setText(R.string.pull_to_refresh_header_hint_loading);
    }

    @Override
    public void onComplete() {
        mLinearLayout.setVisibility(VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mHintView.setVisibility(View.VISIBLE);
        mHintView.setText(R.string.pull_to_refresh_complete);
    }

    @Override
    public void onError() {
        mLinearLayout.setVisibility(VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mHintView.setVisibility(View.VISIBLE);
        mHintView.setText(R.string.pull_to_refresh_network_error);
    }

    @Override
    public void onEnd() {
        mLinearLayout.setVisibility(VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mHintView.setVisibility(View.VISIBLE);
        mHintView.setText(R.string.pushmsg_center_no_more_msg);
    }

    @Override
    public void onNull() {
        mLinearLayout.setVisibility(GONE);
    }
}
