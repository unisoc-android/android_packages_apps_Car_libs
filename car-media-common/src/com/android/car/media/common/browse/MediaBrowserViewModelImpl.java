/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.media.common.browse;

import static androidx.lifecycle.Transformations.map;

import static com.android.car.arch.common.LiveDataFunctions.dataOf;
import static com.android.car.arch.common.LiveDataFunctions.emitsNull;
import static com.android.car.arch.common.LiveDataFunctions.ifThenElse;
import static com.android.car.arch.common.LiveDataFunctions.pair;
import static com.android.car.arch.common.LiveDataFunctions.split;
import static com.android.car.arch.common.LoadingSwitchMap.loadingSwitchMap;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.UiThread;
import android.app.Application;
import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.RestrictTo;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.car.arch.common.LoadingSwitchMap;
import com.android.car.arch.common.switching.SwitchingLiveData;
import com.android.car.media.common.MediaItemMetadata;

import java.util.List;

/**
 * Contains observable data needed for displaying playback and browse/search UI. Instances can be
 * obtained via {@link MediaBrowserViewModel.Factory}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class MediaBrowserViewModelImpl extends AndroidViewModel implements
        MediaBrowserViewModel.WithMutableBrowseId {

    private final SwitchingLiveData<MediaBrowserCompat> mMediaBrowserSwitch =
            SwitchingLiveData.newInstance();

    private final MutableLiveData<String> mCurrentBrowseId = new MutableLiveData<>();
    private final MutableLiveData<String> mCurrentSearchQuery = dataOf(null);

    private final LiveData<Boolean> mIsLoading;
    private final LiveData<List<MediaItemMetadata>> mCurrentMediaItems;

    private final LiveData<BrowseState> mBrowseState;

    public MediaBrowserViewModelImpl(@NonNull Application application) {
        super(application);

        LiveData<MediaBrowserCompat> connectedMediaBrowser = map(mMediaBrowserSwitch.asLiveData(),
                MediaBrowserViewModelImpl::requireConnected);
        LoadingSwitchMap<List<MediaItemMetadata>> currentBrowseItems =
                loadingSwitchMap(pair(connectedMediaBrowser, mCurrentBrowseId),
                        split((mediaBrowser, browseId) ->
                                mediaBrowser == null
                                        ? null
                                        : new BrowsedMediaItems(mediaBrowser, browseId)));
        LoadingSwitchMap<List<MediaItemMetadata>> currentSearchItems =
                loadingSwitchMap(pair(connectedMediaBrowser, mCurrentSearchQuery),
                        split((mediaBrowser, query) ->
                                mediaBrowser == null
                                        ? null
                                        : new SearchedMediaItems(mediaBrowser, query)));
        mIsLoading = ifThenElse(emitsNull(mCurrentSearchQuery),
                currentBrowseItems.isLoading(), currentSearchItems.isLoading());
        mCurrentMediaItems = ifThenElse(emitsNull(mCurrentSearchQuery),
                currentBrowseItems.getOutput(), currentSearchItems.getOutput());

        mBrowseState = new MediatorLiveData<BrowseState>() {
            {
                setValue(BrowseState.EMPTY);
                addSource(mIsLoading, isLoading -> update());
                addSource(mCurrentMediaItems, items -> update());
            }

            private void update() {
                setValue(getState());
            }

            private BrowseState getState() {
                Boolean isLoading = mIsLoading.getValue();
                if (isLoading == null) {
                    // Uninitialized
                    return BrowseState.EMPTY;
                }
                if (isLoading) {
                    return BrowseState.LOADING;
                }
                List<MediaItemMetadata> items = mCurrentMediaItems.getValue();
                if (items == null) {
                    // Normally this could be null if it hasn't been initialized, but in that case
                    // isLoading would not be false, so this means it must have encountered an
                    // error.
                    return BrowseState.ERROR;
                }
                if (items.isEmpty()) {
                    return BrowseState.EMPTY;
                }
                return BrowseState.LOADED;
            }
        };
    }

    private static MediaBrowserCompat requireConnected(@Nullable MediaBrowserCompat mediaBrowser) {
        if (mediaBrowser != null && !mediaBrowser.isConnected()) {
            throw new IllegalStateException(
                    "Only connected MediaBrowsers may be provided to MediaBrowserViewModel.");
        }
        return mediaBrowser;
    }

    /**
     * Set the source {@link MediaBrowserCompat} to use for browsing. If {@code mediaBrowser} emits
     * non-null, the MediaBrowser emitted must already be in a connected state.
     */
    void setConnectedMediaBrowser(@Nullable LiveData<MediaBrowserCompat> mediaBrowser) {
        mMediaBrowserSwitch.setSource(mediaBrowser);
    }

    LiveData<? extends MediaBrowserCompat> getMediaBrowserSource() {
        return mMediaBrowserSwitch.getSource();
    }

    /**
     * Set the current item to be browsed. If available, the list of items will be emitted by {@link
     * #getBrowsedMediaItems()}.
     */
    @UiThread
    @Override
    public void setCurrentBrowseId(@Nullable String browseId) {
        mCurrentBrowseId.setValue(browseId);
    }

    /**
     * Set the current item to be searched for. If available, the list of items will be emitted
     * by {@link #getBrowsedMediaItems()}.
     */
    @UiThread
    @Override
    public void search(@Nullable String query) {
        mCurrentSearchQuery.setValue(query);
    }

    @Override
    public LiveData<BrowseState> getBrowseState() {
        return mBrowseState;
    }

    @Override
    public LiveData<Boolean> isLoading() {
        return mIsLoading;
    }

    /**
     * Fetches the MediaItemMetadatas for the current browsed id. A MediaSource must be selected and
     * its MediaBrowser connected, otherwise this will emit {@code null}. Will emit browse results
     * if provided search query is {@code null}, and search query results otherwise.
     *
     * @return a LiveData that emits the MediaItemMetadatas for the current search query or browsed
     * id or {@code null} if unavailable.
     * @see #setCurrentBrowseId(String)
     * @see #search(String)
     */
    @Override
    public LiveData<List<MediaItemMetadata>> getBrowsedMediaItems() {
        return mCurrentMediaItems;
    }
}
