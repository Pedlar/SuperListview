package org.notlocalhost.superlistview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.notlocalhost.superlistview.BaseSuperAbsListview;
import org.notlocalhost.superlistview.OnMoreListener;
import org.notlocalhost.superlistview.SuperGridview;
import org.notlocalhost.superlistview.SuperListview;
import org.notlocalhost.superlistview.SwipeDismissListViewTouchListener;
import org.notlocalhost.superlistview.widget.SwipeHeaderView;
import org.notlocalhost.superlistview.widget.SwipeRefreshLayout;

import java.util.ArrayList;

/*
TODO -
 1. Write a test for this activity, so we can test the library functionality.
 */
public class ListSample extends Activity implements
        SwipeRefreshLayout.OnRefreshListener, OnMoreListener, View.OnClickListener {

    private static final int LIST = 0;
    private static final int GRID = 1;
    private BaseSuperAbsListview mList;
    private ArrayAdapter<String> mAdapter;
    private int mListType = LIST;

    private void setupListBasedOnConfig() {
        RadioGroup configHeaderType = (RadioGroup)findViewById(R.id.config_header_type);
        switch(configHeaderType.getCheckedRadioButtonId()) {
            case R.id.default_type:
                mList.setHeaderType(SwipeHeaderView.SwipeHeaderType.DEFAULT);
                break;
            case R.id.spinner_type:
                mList.setHeaderType(SwipeHeaderView.SwipeHeaderType.SPINNER);
                break;
            case R.id.spinner_text_type:
                mList.setHeaderType(SwipeHeaderView.SwipeHeaderType.SPINNER_WITH_TEXT);
                mList.setHeaderText(R.string.pull_to_refresh);
                break;
            case R.id.arrow_type:
                mList.setHeaderType(SwipeHeaderView.SwipeHeaderType.ARROW);
                break;
            case R.id.arrow_text_type:
                mList.setHeaderType(SwipeHeaderView.SwipeHeaderType.ARROW_WITH_TEXT);
                break;
        }

        // Remove all flags first
        mList.setHeaderFlags();
        RadioGroup configHeaderFlags = (RadioGroup)findViewById(R.id.config_header_flags);
        switch(configHeaderFlags.getCheckedRadioButtonId()) {
            case R.id.flag_expand:
                mList.setHeaderFlags(SwipeHeaderView.HeaderFlags.FLAG_EXPAND);
                break;
            case R.id.flag_slide_in:
                mList.setHeaderFlags(SwipeHeaderView.HeaderFlags.FLAG_SLIDE_IN);
                break;
        }
        if(((CheckBox)findViewById(R.id.flag_animate_arrow)).isChecked()) {
            mList.setHeaderFlags(SwipeHeaderView.HeaderFlags.FLAG_ANIMATE_ARROW);
        }
    }

    @SuppressWarnings("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sample);

        ArrayList<String> lst = new ArrayList<String>();
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, lst);

        findViewById(R.id.show_list).setOnClickListener(this);
        findViewById(R.id.show_grid).setOnClickListener(this);
    }

    private void setupListView() {
        mListType = LIST;
        mList = (SuperListview)findViewById(R.id.list);
        setupView();
        ((SuperListview)mList).setupSwipeToDismiss(new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
            }
        }, true);
    }

    private void setupGridView() {
        mListType = GRID;
        mList = (SuperGridview) findViewById(R.id.grid);
        setupView();
    }

    private void setupView() {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.add("More stuff");
                        mAdapter.add("More stuff");
                        mAdapter.add("More stuff");

                        mList.setAdapter(mAdapter);

                    }
                });
            }
        });
        thread.start();

        // Setting the refresh listener will enable the refresh progressbar
        mList.setRefreshListener(this);

        // Wow so beautiful
        mList.setRefreshingColor(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);

        // I want to get loadMore triggered if I see the last item (1)
        mList.setupMoreListener(this, 1);
    }

    @Override
    public void onRefresh() {
        Toast.makeText(this, "Refresh", Toast.LENGTH_LONG).show();

        // enjoy the beaty of the progressbar
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                // demo purpose, adding to the top so you can see it
                mAdapter.insert("New stuff", 0);

            }
        }, 2000);
    }

    @Override
    public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {

        Toast.makeText(this, "More", Toast.LENGTH_LONG).show();

        //demo purpose, adding to the bottom
        mAdapter.add("More asked, more served");
    }

    @Override
    public void onBackPressed() {
        if(mList != null && mList.getVisibility() == View.VISIBLE) {
            mList.setVisibility(View.GONE);
            mAdapter.clear();
            findViewById(R.id.config_layout).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        boolean showList = false;
        switch(v.getId()) {
            case R.id.show_list:
                setupListView();
                showList = true;
                break;
            case R.id.show_grid:
                setupGridView();
                showList = true;
                break;
        }

        if(showList) {
            setupListBasedOnConfig();
            findViewById(R.id.config_layout).setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
        }
    }
}
