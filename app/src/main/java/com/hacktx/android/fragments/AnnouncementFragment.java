/*
 * Copyright 2017 HackTX.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hacktx.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hacktx.android.R;
import com.hacktx.android.models.Announcement;
import com.hacktx.android.network.HackTxClient;
import com.hacktx.android.network.services.HackTxService;
import com.hacktx.android.views.SpacesItemDecoration;
import com.hacktx.android.views.adapters.AnnouncementsRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AnnouncementFragment extends BaseFragment {

    private static final String TAG = "AnnouncementsActivity";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout mEmptyLayout;
    private List<Announcement> announcements;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_announcement, container, false);
        announcements = new ArrayList<>();

        mEmptyLayout = (ConstraintLayout) root.findViewById(R.id.empty_view);

        setupToolbar((Toolbar) root.findViewById(R.id.toolbar), R.string.fragment_announcement_title);

        setupSwipeRefreshLayout(root);
        setupCollapsibleToolbar((AppBarLayout) root.findViewById(R.id.appBar), swipeRefreshLayout);
        setupRecyclerView(root);
        setupEmptyLayout((TextView) root.findViewById(R.id.fragment_empty_title), (Button) root.findViewById(R.id.fragment_empty_btn));
        getAnnouncements();

        return root;
    }

    private void setupSwipeRefreshLayout(ViewGroup root) {
        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.announcementsSwipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.hacktx_blue);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAnnouncements();
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void setupCollapsibleToolbar(AppBarLayout appBarLayout, final SwipeRefreshLayout swipeRefreshLayout) {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                if (i == 0) {
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });
    }

    private void setupRecyclerView(ViewGroup root) {
        mRecyclerView = (RecyclerView) root.findViewById(R.id.announcement_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SpacesItemDecoration(20));

        // specify an adapter (see also next example)
        mAdapter = new AnnouncementsRecyclerView(announcements);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupEmptyLayout(TextView text, Button retryBtn) {
        text.setText(R.string.announce_empty_title);

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAnnouncements();
            }
        });
    }

    private void getAnnouncements() {
        HackTxService hackTxService = HackTxClient.getInstance().getApiService();
        hackTxService.getAnnouncements(new Callback<ArrayList<Announcement>>() {
            @Override
            public void success(ArrayList<Announcement> messages, Response response) {
                hideEmptyView();
                announcements.clear();
                announcements.addAll(messages);
                Collections.sort(announcements, Announcement.AnnouncementComparator);
                mAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, error.toString());
                swipeRefreshLayout.setRefreshing(false);
                showEmptyView();
            }
        });
    }

    private void showEmptyView() {
        swipeRefreshLayout.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView() {
        mEmptyLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }
}
