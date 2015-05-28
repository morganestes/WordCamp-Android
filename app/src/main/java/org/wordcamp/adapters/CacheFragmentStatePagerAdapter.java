/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wordcamp.adapters;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * FragmentStatePagerAdapter that caches each pages.
 * FragmentStatePagerAdapter is also originally caches pages,
 * but its keys are not public nor documented, so depending
 * on how it create cache key is dangerous.
 * This adapter caches pages by itself and provide getter method to the cache.
 */
public abstract class CacheFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    private static final String STATE_SUPER_STATE = "superState";
    private static final String STATE_PAGES = "pages";
    private static final String STATE_PAGE_KEY_PREFIX = "page:";

    private FragmentManager mFm;
    private SparseArray<Fragment> mPages;

    public CacheFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
        mPages = new SparseArray<Fragment>();
        mFm = fm;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        Bundle bundle = (Bundle) state;
        int pages = bundle.getInt(STATE_PAGES);
        if (0 < pages) {
            for (int i = 0; i < pages; i++) {
                Fragment f = mFm.getFragment(bundle, createCacheKey(i));
                mPages.put(i, f);
            }
        }

        Parcelable p = bundle.getParcelable(STATE_SUPER_STATE);
        super.restoreState(p, loader);
    }

    /**
     * Get a new Fragment instance.
     * Each fragments are automatically cached in this method,
     * so you don't have to do it by yourself.
     * If you want to implement instantiation of Fragments,
     * you should override {@link #createItem(int)} instead.
     *
     * {@inheritDoc}
     *
     * @param position position of the item in the adapter
     * @return fragment instance
     */
    @Override
    public Fragment getItem(int position) {
        Fragment f = createItem(position);
        // We should cache fragments manually to access to them later
        mPages.put(position, f);
        return f;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (0 <= mPages.indexOfKey(position)) {
            mPages.remove(position);
        }
        super.destroyItem(container, position, object);
    }

    /**
     * Get the item at the specified position in the adapter.
     *
     * @param position position of the item in the adapter
     * @return fragment instance
     */
    public Fragment getItemAt(int position) {
        return mPages.get(position);
    }

    /**
     * Create a new Fragment instance.
     * This is called inside {@link #getItem(int)}.
     *
     * @param position position of the item in the adapter
     * @return fragment instance
     */
    protected abstract Fragment createItem(int position);

    /**
     * Create a key string for caching Fragment pages.
     *
     * @param position position of the item in the adapter
     * @return key string for caching Fragment pages
     */
    private String createCacheKey(int position) {
        return STATE_PAGE_KEY_PREFIX + position;
    }
}
