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

package com.yooiistudios.newskit.ui.presenter;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.yooiistudios.newskit.core.news.News;

public class NewsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        News news = (News) item;
        if (news != null) {
            viewHolder.getTitle().setText(news.getTitle());
//            viewHolder.getSubtitle().setText(news.getDescription());
//            viewHolder.getBody().setText(news.getDescription());
            viewHolder.getBody().setText(news.getDisplayableRssDescription());
        }
    }
}
