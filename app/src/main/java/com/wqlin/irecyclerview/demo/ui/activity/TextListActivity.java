package com.wqlin.irecyclerview.demo.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.wqlin.irecyclerview.demo.R;
import com.wqlin.irecyclerview.demo.ui.adapter.RefreshFootAdapter;

import java.util.ArrayList;

public class TextListActivity extends AppCompatActivity {

    private RefreshFootAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_list);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter = new RefreshFootAdapter(this, recyclerView,manager , 20, new Runnable() {
            @Override
            public void run() {
                adapter.addMoreItem(getList());
            }
        });
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<String> getList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add(i + "");
        }
        return list;
    }
}
