/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.yooiistudios.newsflow;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.yooiistudios.newsflow.core.news.DefaultNewsFeedProvider;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.model.news.task.NewsFeedsFetchManager;
import com.yooiistudios.newsflow.reference.R;

import java.util.ArrayList;

/*
 * MainActivity
 */
public class MainActivity extends Activity
        implements NewsFeedsFetchManager.OnFetchListener {
    private TextView mLogView;

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogView = (TextView) findViewById(R.id.textView);
        mLogView.setMovementMethod(new ScrollingMovementMethod());

        ArrayList<NewsFeed> feeds = DefaultNewsFeedProvider.getDefaultBottomNewsFeedList(
                getApplicationContext());
        NewsFeedsFetchManager.getInstance().fetch(feeds, this);
    }

    @Override
    public void onFetchAll(ArrayList<NewsFeed> newsFeeds) {
        StringBuilder builder = new StringBuilder();
        for (NewsFeed newsFeed : newsFeeds) {
            builder.append(newsFeed.toString()).append("\n\n");
        }
        mLogView.setText(builder.toString());
    }
}
