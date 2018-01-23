package com.wqlin.irecyclerview.demo.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.WRecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wqlin.irecyclerview.demo.R;
import com.wqlin.irecyclerview.demo.model.Image;
import com.wqlin.irecyclerview.demo.network.NetworkAPI;
import com.wqlin.irecyclerview.demo.ui.adapter.ImageAdapter;
import com.wqlin.irecyclerview.demo.ui.adapter.OnItemClickListener;
import com.wqlin.irecyclerview.demo.ui.widget.BannerView;
import com.wqlin.irecyclerview.demo.ui.widget.footer.LoadMoreFooterView;
import com.wqlin.irecyclerview.demo.ui.widget.header.BatVsSupperHeaderView;
import com.wqlin.irecyclerview.demo.ui.widget.header.ClassicRefreshHeaderView;
import com.wqlin.irecyclerview.demo.utils.DensityUtils;
import com.wqlin.irecyclerview.demo.utils.ListUtils;
import com.wqlin.widget.irecyclerview.OnLoadMoreListener;
import com.wqlin.widget.irecyclerview.OnRefreshListener;

import java.util.List;


public class MainActivity extends AppCompatActivity implements OnItemClickListener<Image>, OnRefreshListener, OnLoadMoreListener {

    private WRecyclerView mWRecyclerView;
    private BannerView bannerView;

    private ImageAdapter mAdapter;

    private int mPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWRecyclerView = (WRecyclerView) findViewById(R.id.iRecyclerView);
        mWRecyclerView.setRefreshEnabled(true);
        mWRecyclerView.setLoadMoreEnabled(true);
        mWRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        bannerView = (BannerView) LayoutInflater.from(this).inflate(R.layout.layout_banner_view, mWRecyclerView.getHeaderContainer(), false);
        mWRecyclerView.addHeaderView(bannerView);
        mWRecyclerView.setRefreshHeaderView(new BatVsSupperHeaderView(this));
        mWRecyclerView.setLoadMoreFooterView(new LoadMoreFooterView(this));

        mAdapter = new ImageAdapter();
        mWRecyclerView.setAdapter(mAdapter);
//        iRecyclerView.getAdapter();
        mWRecyclerView.setOnRefreshListener(this);
        mWRecyclerView.setOnLoadMoreListener(this);

        mAdapter.setOnItemClickListener(this);

        mWRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mWRecyclerView.setRefreshing(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWRecyclerView.destory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_change_header, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_header) {
//            toggleRefreshHeader();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int position, Image image, View v) {
        mAdapter.remove(position);
        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        loadBanner();
        refresh();
    }

    @Override
    public void onLoadMore() {
        if (mAdapter.getItemCount() > 0) {
            loadMore();
        } else {
            mWRecyclerView.setLoadMoreComplete(true);
        }
    }

    private void toggleRefreshHeader() {
        if (mWRecyclerView.getRefreshHeaderView() instanceof BatVsSupperHeaderView) {
            ClassicRefreshHeaderView classicRefreshHeaderView = new ClassicRefreshHeaderView(this);
            classicRefreshHeaderView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DensityUtils.dip2px(this, 80)));
            // we can set view
            mWRecyclerView.setRefreshHeaderView(classicRefreshHeaderView);
            Toast.makeText(this, "Classic style", Toast.LENGTH_SHORT).show();
        } else if (mWRecyclerView.getRefreshHeaderView() instanceof ClassicRefreshHeaderView) {
            // we can also set layout
//            iRecyclerView.setRefreshHeaderView(R.layout.layout_irecyclerview_refresh_header);
            Toast.makeText(this, "Bat man vs Super man style", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBanner() {
        NetworkAPI.requestBanners(new NetworkAPI.Callback<List<Image>>() {
            @Override
            public void onSuccess(List<Image> images) {
                if (!ListUtils.isEmpty(images)) {
                    bannerView.setList(images);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void refresh() {
        mPage = 1;
        NetworkAPI.requestImages(mPage, new NetworkAPI.Callback<List<Image>>() {
            @Override
            public void onSuccess(List<Image> images) {
                mWRecyclerView.setRefreshComplete();
                if (ListUtils.isEmpty(images)) {
                    mAdapter.clear();
                } else {
                    mPage = 2;
                    mAdapter.setList(images);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                mWRecyclerView.setRefreshError();
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMore() {
        NetworkAPI.requestImages(mPage, new NetworkAPI.Callback<List<Image>>() {
            @Override
            public void onSuccess(final List<Image> images) {
                if (ListUtils.isEmpty(images)) {
                    mWRecyclerView.setLoadMoreEnd();
                } else {

//                    mPage++;
//                    loadMoreFooterView.setLoadMoreStatus(LoadMoreFooterView.Status.GONE);
//                    mAdapter.append(images);
                    /**
                     * FIXME here we post delay to see more animation, you don't need to do this.
                     */
                    mPage++;
                    mAdapter.append(images);
                    mWRecyclerView.setLoadMoreComplete(true);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                mWRecyclerView.setLoadMoreError();
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                mWRecyclerView.setLoadMoreComplete(true);
            }
        });
    }


}
