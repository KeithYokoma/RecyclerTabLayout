package com.nshmura.recyclertablayout.demo.imitationloop;

import com.nshmura.recyclertablayout.RecyclerTabLayout;
import com.nshmura.recyclertablayout.demo.Demo;
import com.nshmura.recyclertablayout.demo.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.view.MenuItem;

import java.util.ArrayList;

public class DemoImitationLoopActivity extends AppCompatActivity {

    protected static final String KEY_DEMO = "demo";

    private int mScrollState;
    private DemoImitationLoopPagerAdapter mAdapter;
    private ViewPager2 mViewPager;
    private ArrayList<String> mItems;
    private ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            //got to center
            boolean nearLeftEdge = (position <= mItems.size());
            boolean nearRightEdge = (position >= mAdapter.getItemCount() - mItems.size());
            if (nearLeftEdge || nearRightEdge) {
                mViewPager.setCurrentItem(mAdapter.getCenterPosition(0), false);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;
        }
    };

    public static void startActivity(Context context, Demo demo) {
        Intent intent = new Intent(context, DemoImitationLoopActivity.class);
        intent.putExtra(KEY_DEMO, demo.name());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_basic);

        Demo demo = Demo.valueOf(getIntent().getStringExtra(KEY_DEMO));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(demo.titleResId);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItems = new ArrayList<>();
        mItems.add(":)");
        for (int i = 1; i <= 9; i++) {
            mItems.add(String.valueOf(i));
        }

        mAdapter = new DemoImitationLoopPagerAdapter(this);
        mAdapter.addAll(mItems);

        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mAdapter.getCenterPosition(0));
        mViewPager.registerOnPageChangeCallback(onPageChangeCallback);

        RecyclerTabLayout recyclerTabLayout = findViewById(R.id.recycler_tab_layout);
        recyclerTabLayout.setUpWithViewPager(mViewPager);
    }

    @Override
    protected void onDestroy() {
        mViewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}