/*
 * 				Firetweet - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.getlantern.firetweet.activity.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import org.getlantern.firetweet.R;
import org.getlantern.firetweet.adapter.SimpleParcelableUserListsAdapter;
import org.getlantern.firetweet.adapter.SimpleParcelableUsersAdapter;
import org.getlantern.firetweet.adapter.UserHashtagAutoCompleteAdapter;
import org.getlantern.firetweet.fragment.support.CreateUserListDialogFragment;
import org.getlantern.firetweet.fragment.support.SupportProgressDialogFragment;
import org.getlantern.firetweet.model.ParcelableUser;
import org.getlantern.firetweet.model.ParcelableUserList;
import org.getlantern.firetweet.model.SingleResponse;
import org.getlantern.firetweet.util.AsyncTaskUtils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.http.HttpResponseCode;

import static android.text.TextUtils.isEmpty;
import static org.getlantern.firetweet.util.ParseUtils.parseString;
import static org.getlantern.firetweet.util.Utils.getAccountScreenName;
import static org.getlantern.firetweet.util.Utils.getTwitterInstance;

public class UserListSelectorActivity extends BaseSupportDialogActivity implements OnClickListener, OnItemClickListener {

    private AutoCompleteTextView mEditScreenName;
    private ListView mUserListsListView, mUsersListView;
    private SimpleParcelableUserListsAdapter mUserListsAdapter;
    private SimpleParcelableUsersAdapter mUsersAdapter;
    private View mUsersListContainer, mUserListsContainer, mCreateUserListContainer;

    private String mScreenName;

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BROADCAST_USER_LIST_CREATED.equals(action)) {
                getUserLists(mScreenName);
            }
        }
    };

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.screen_name_confirm: {
                final String screen_name = parseString(mEditScreenName.getText());
                if (isEmpty(screen_name)) return;
                searchUser(screen_name);
                break;
            }
            case R.id.create_list: {
                final DialogFragment f = new CreateUserListDialogFragment();
                final Bundle args = new Bundle();
                args.putLong(EXTRA_ACCOUNT_ID, getAccountId());
                f.setArguments(args);
                f.show(getSupportFragmentManager(), null);
                break;
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mUsersListContainer = findViewById(R.id.users_list_container);
        mUserListsContainer = findViewById(R.id.user_lists_container);
        mEditScreenName = (AutoCompleteTextView) findViewById(R.id.edit_screen_name);
        mUserListsListView = (ListView) findViewById(R.id.user_lists_list);
        mUsersListView = (ListView) findViewById(R.id.users_list);
        mCreateUserListContainer = findViewById(R.id.create_list_container);
    }

    @Override
    public void onItemClick(final AdapterView<?> view, final View child, final int position, final long id) {
        final int view_id = view.getId();
        final ListView list = (ListView) view;
        if (view_id == R.id.users_list) {
            final ParcelableUser user = mUsersAdapter.getItem(position - list.getHeaderViewsCount());
            if (user == null) return;
            if (isSelectingUser()) {
                final Intent data = new Intent();
                data.setExtrasClassLoader(getClassLoader());
                data.putExtra(EXTRA_USER, user);
                setResult(RESULT_OK, data);
                finish();
            } else {
                getUserLists(user.screen_name);
            }
        } else if (view_id == R.id.user_lists_list) {
            final Intent data = new Intent();
            data.putExtra(EXTRA_USER_LIST, mUserListsAdapter.getItem(position - list.getHeaderViewsCount()));
            setResult(RESULT_OK, data);
            finish();
        }
    }

    public void setUsersData(final List<ParcelableUser> data) {
        mUsersAdapter.setData(data, true);
        mUsersListContainer.setVisibility(View.VISIBLE);
        mUserListsContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_ACCOUNT_ID)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_user_list_selector);
        if (savedInstanceState == null) {
            mScreenName = intent.getStringExtra(EXTRA_SCREEN_NAME);
        } else {
            mScreenName = savedInstanceState.getString(EXTRA_SCREEN_NAME);
        }

        final boolean selecting_user = isSelectingUser();
        setTitle(selecting_user ? R.string.select_user : R.string.select_user_list);
        if (!isEmpty(mScreenName)) {
            if (selecting_user) {
                searchUser(mScreenName);
            } else {
                getUserLists(mScreenName);
            }
        }
        final UserHashtagAutoCompleteAdapter adapter = new UserHashtagAutoCompleteAdapter(this);
        adapter.setAccountId(getAccountId());
        mEditScreenName.setAdapter(adapter);
        mEditScreenName.setText(mScreenName);
        mUserListsListView.setAdapter(mUserListsAdapter = new SimpleParcelableUserListsAdapter(this));
        mUsersListView.setAdapter(mUsersAdapter = new SimpleParcelableUsersAdapter(this));
        mUserListsListView.setOnItemClickListener(this);
        mUsersListView.setOnItemClickListener(this);
        if (selecting_user) {
            mUsersListContainer.setVisibility(View.VISIBLE);
            mUserListsContainer.setVisibility(View.GONE);
        } else {
            mUsersListContainer.setVisibility(isEmpty(mScreenName) ? View.VISIBLE : View.GONE);
            mUserListsContainer.setVisibility(isEmpty(mScreenName) ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_SCREEN_NAME, mScreenName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(BROADCAST_USER_LIST_CREATED);
        registerReceiver(mStatusReceiver, filter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mStatusReceiver);
        super.onStop();
    }

    private long getAccountId() {
        return getIntent().getLongExtra(EXTRA_ACCOUNT_ID, -1);
    }

    private void getUserLists(final String screenName) {
        if (screenName == null) return;
        mScreenName = screenName;
        final GetUserListsTask task = new GetUserListsTask(this, getAccountId(), screenName);
        AsyncTaskUtils.executeTask(task);
    }

    private boolean isSelectingUser() {
        return INTENT_ACTION_SELECT_USER.equals(getIntent().getAction());
    }

    private void searchUser(final String name) {
        final SearchUsersTask task = new SearchUsersTask(this, getAccountId(), name);
        AsyncTaskUtils.executeTask(task);
    }

    private void setUserListsData(final List<ParcelableUserList> data, final boolean isMyAccount) {
        mUserListsAdapter.setData(data, true);
        mUsersListContainer.setVisibility(View.GONE);
        mUserListsContainer.setVisibility(View.VISIBLE);
        mCreateUserListContainer.setVisibility(isMyAccount ? View.VISIBLE : View.GONE);
    }

    private static class GetUserListsTask extends AsyncTask<Object, Object, SingleResponse<List<ParcelableUserList>>> {

        private static final String FRAGMENT_TAG_GET_USER_LISTS = "get_user_lists";

        private final UserListSelectorActivity mActivity;
        private final Handler mHandler;
        private final long mAccountId;
        private final String mScreenName;

        GetUserListsTask(final UserListSelectorActivity activity, final long accountId, final String screenName) {
            mActivity = activity;
            mHandler = new Handler(activity.getMainLooper());
            mAccountId = accountId;
            mScreenName = screenName;
        }

        @Override
        protected SingleResponse<List<ParcelableUserList>> doInBackground(final Object... params) {
            final Twitter twitter = getTwitterInstance(mActivity, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final ResponseList<UserList> lists = twitter.getUserLists(mScreenName, true);
                final List<ParcelableUserList> data = new ArrayList<>();
                boolean is_my_account = mScreenName.equalsIgnoreCase(getAccountScreenName(mActivity, mAccountId));
                for (final UserList item : lists) {
                    final User user = item.getUser();
                    if (user != null && mScreenName.equalsIgnoreCase(user.getScreenName())) {
                        if (!is_my_account && user.getId() == mAccountId) {
                            is_my_account = true;
                        }
                        data.add(new ParcelableUserList(item, mAccountId));
                    }
                }
                final SingleResponse<List<ParcelableUserList>> result = SingleResponse.getInstance(data);
                result.getExtras().putBoolean(EXTRA_IS_MY_ACCOUNT, is_my_account);
                return result;
            } catch (final TwitterException e) {
                e.printStackTrace();
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<List<ParcelableUserList>> result) {
            final Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_GET_USER_LISTS);
            if (f instanceof DialogFragment) {
                ((DialogFragment) f).dismiss();
            }
            if (result.getData() != null) {
                mActivity.setUserListsData(result.getData(), result.getExtras().getBoolean(EXTRA_IS_MY_ACCOUNT));
            } else if (result.getException() instanceof TwitterException) {
                final TwitterException te = (TwitterException) result.getException();
                if (te.getStatusCode() == HttpResponseCode.NOT_FOUND) {
                    mActivity.searchUser(mScreenName);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final SupportProgressDialogFragment df = SupportProgressDialogFragment.show(mActivity, FRAGMENT_TAG_GET_USER_LISTS);
                    df.setCancelable(false);
                }
            });
        }

    }

    private static class SearchUsersTask extends AsyncTask<Object, Object, SingleResponse<List<ParcelableUser>>> {

        private static final String FRAGMENT_TAG_SEARCH_USERS = "search_users";
        private final UserListSelectorActivity mActivity;
        private final Handler mHandler;

        private final long mAccountId;
        private final String mName;

        SearchUsersTask(final UserListSelectorActivity activity, final long accountId, final String name) {
            mActivity = activity;
            mHandler = new Handler(activity.getMainLooper());
            mAccountId = accountId;
            mName = name;
        }

        @Override
        protected SingleResponse<List<ParcelableUser>> doInBackground(final Object... params) {
            final Twitter twitter = getTwitterInstance(mActivity, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final ResponseList<User> lists = twitter.searchUsers(mName, 1);
                final List<ParcelableUser> data = new ArrayList<>();
                for (final User item : lists) {
                    data.add(new ParcelableUser(item, mAccountId));
                }
                return SingleResponse.getInstance(data);
            } catch (final TwitterException e) {
                e.printStackTrace();
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<List<ParcelableUser>> result) {
            final Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_SEARCH_USERS);
            if (f instanceof DialogFragment) {
                ((DialogFragment) f).dismiss();
            }
            if (result.getData() != null) {
                mActivity.setUsersData(result.getData());
            }
        }

        @Override
        protected void onPreExecute() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final SupportProgressDialogFragment df = SupportProgressDialogFragment.show(mActivity, FRAGMENT_TAG_SEARCH_USERS);
                    df.setCancelable(false);
                }
            });
        }

    }

}
