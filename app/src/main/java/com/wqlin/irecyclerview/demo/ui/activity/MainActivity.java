package com.wqlin.irecyclerview.demo.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.support.v7.widget.IRecyclerView;
import com.wqlin.irecyclerview.OnLoadMoreListener;
import com.wqlin.irecyclerview.OnRefreshListener;
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

import java.util.List;


public class MainActivity extends AppCompatActivity implements OnItemClickListener<Image>, OnRefreshListener, OnLoadMoreListener {

    private IRecyclerView iRecyclerView;
    private BannerView bannerView;

    private ImageAdapter mAdapter;

    private int mPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iRecyclerView = (IRecyclerView) findViewById(R.id.iRecyclerView);
        iRecyclerView.setRefreshEnabled(true);
        iRecyclerView.setLoadMoreEnabled(true);
        iRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        bannerView = (BannerView) LayoutInflater.from(this).inflate(R.layout.layout_banner_view, iRecyclerView.getHeaderContainer(), false);
        iRecyclerView.addHeaderView(bannerView);
        iRecyclerView.setRefreshHeaderView(new BatVsSupperHeaderView(this));
        iRecyclerView.setLoadMoreFooterView(new LoadMoreFooterView(this));

        mAdapter = new ImageAdapter();
        iRecyclerView.setAdapter(mAdapter);
//        iRecyclerView.getAdapter();
        iRecyclerView.setOnRefreshListener(this);
        iRecyclerView.setOnLoadMoreListener(this);

        mAdapter.setOnItemClickListener(this);

        iRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                iRecyclerView.setRefreshing(true);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_change_header, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_header) {
            toggleRefreshHeader();
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
            iRecyclerView.setLoadMoreComplete(true);
        }
    }

    private void toggleRefreshHeader() {
        if (iRecyclerView.getRefreshHeaderView() instanceof BatVsSupperHeaderView) {
            ClassicRefreshHeaderView classicRefreshHeaderView = new ClassicRefreshHeaderView(this);
            classicRefreshHeaderView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DensityUtils.dip2px(this, 80)));
            // we can set view
            iRecyclerView.setRefreshHeaderView(classicRefreshHeaderView);
            Toast.makeText(this, "Classic style", Toast.LENGTH_SHORT).show();
        } else if (iRecyclerView.getRefreshHeaderView() instanceof ClassicRefreshHeaderView) {
            // we can also set layout
            iRecyclerView.setRefreshHeaderView(R.layout.layout_irecyclerview_refresh_header);
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
                iRecyclerView.setRefreshComplete();
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
                iRecyclerView.setRefreshError();
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMore() {
        NetworkAPI.requestImages(mPage, new NetworkAPI.Callback<List<Image>>() {
            @Override
            public void onSuccess(final List<Image> images) {
                if (ListUtils.isEmpty(images)) {
                    iRecyclerView.setLoadMoreEnd();
                } else {

//                    mPage++;
//                    loadMoreFooterView.setLoadMoreStatus(LoadMoreFooterView.Status.GONE);
//                    mAdapter.append(images);
                    /**
                     * FIXME here we post delay to see more animation, you don't need to do this.
                     */
                    mPage++;
                    mAdapter.append(images);
                    iRecyclerView.setLoadMoreComplete(true);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                iRecyclerView.setLoadMoreError();
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                iRecyclerView.setLoadMoreComplete(true);
            }
        });
    }


}
