/*
 * Firetweet - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.getlantern.firetweet.view.holder;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.getlantern.firetweet.R;
import org.getlantern.firetweet.adapter.AbsActivitiesAdapter;
import org.getlantern.firetweet.model.ParcelableActivity;
import org.getlantern.firetweet.model.ParcelableUser;
import org.getlantern.firetweet.util.MediaLoaderWrapper;
import org.getlantern.firetweet.util.UserColorNameUtils;
import org.getlantern.firetweet.view.ActionIconView;
import org.oshkimaadziig.george.androidutils.SpanFormatter;

/**
 * Created by mariotaku on 15/1/3.
 */
public class ActivityTitleSummaryViewHolder extends ViewHolder {

    private final AbsActivitiesAdapter adapter;
    private final ActionIconView activityTypeView;
    private final TextView titleView;
    private final TextView summaryView;
    private final ViewGroup profileImagesContainer;
    private final TextView profileImageMoreNumber;
    private final ImageView[] profileImageViews;

    public ActivityTitleSummaryViewHolder(AbsActivitiesAdapter adapter, View itemView) {
        super(itemView);
        this.adapter = adapter;
        activityTypeView = (ActionIconView) itemView.findViewById(R.id.activity_type);
        titleView = (TextView) itemView.findViewById(R.id.title);
        summaryView = (TextView) itemView.findViewById(R.id.summary);

        profileImagesContainer = (ViewGroup) itemView.findViewById(R.id.profile_images_container);
        profileImageViews = new ImageView[5];
        profileImageViews[0] = (ImageView) itemView.findViewById(R.id.activity_profile_image_0);
        profileImageViews[1] = (ImageView) itemView.findViewById(R.id.activity_profile_image_1);
        profileImageViews[2] = (ImageView) itemView.findViewById(R.id.activity_profile_image_2);
        profileImageViews[3] = (ImageView) itemView.findViewById(R.id.activity_profile_image_3);
        profileImageViews[4] = (ImageView) itemView.findViewById(R.id.activity_profile_image_4);
        profileImageMoreNumber = (TextView) itemView.findViewById(R.id.activity_profile_image_more_number);
    }

    public void displayActivity(ParcelableActivity activity) {
        final Context context = adapter.getContext();
        final Resources resources = adapter.getContext().getResources();
        switch (activity.action) {
            case ParcelableActivity.ACTION_FOLLOW: {
                activityTypeView.setImageResource(R.drawable.ic_activity_action_follow);
                activityTypeView.setColorFilter(resources.getColor(R.color.highlight_follow), Mode.SRC_ATOP);
                titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_follow,
                        R.string.activity_about_me_follow_multi, activity.sources));
                displayUserProfileImages(activity.sources);
                summaryView.setVisibility(View.GONE);
                break;
            }
            case ParcelableActivity.ACTION_FAVORITE: {
                activityTypeView.setImageResource(R.drawable.ic_activity_action_favorite);
                activityTypeView.setColorFilter(resources.getColor(R.color.highlight_favorite), Mode.SRC_ATOP);
                titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_favorite,
                        R.string.activity_about_me_favorite_multi, activity.sources));
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case ParcelableActivity.ACTION_RETWEET: {
                activityTypeView.setImageResource(R.drawable.ic_activity_action_retweet);
                activityTypeView.setColorFilter(resources.getColor(R.color.highlight_retweet), Mode.SRC_ATOP);
                titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_retweet,
                        R.string.activity_about_me_retweet_multi, activity.sources));
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case ParcelableActivity.ACTION_FAVORITED_RETWEET: {
                activityTypeView.setImageResource(R.drawable.ic_activity_action_favorite);
                activityTypeView.setColorFilter(resources.getColor(R.color.highlight_favorite), Mode.SRC_ATOP);
                titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_favorited_retweet,
                        R.string.activity_about_me_favorited_retweet_multi, activity.sources));
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case ParcelableActivity.ACTION_RETWEETED_RETWEET: {
                activityTypeView.setImageResource(R.drawable.ic_activity_action_retweet);
                activityTypeView.setColorFilter(resources.getColor(R.color.highlight_retweet), Mode.SRC_ATOP);
                titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_retweeted_retweet,
                        R.string.activity_about_me_retweeted_retweet_multi, activity.sources));
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case ParcelableActivity.ACTION_LIST_MEMBER_ADDED: {
                activityTypeView.setImageResource(R.drawable.ic_activity_action_list_added);
                activityTypeView.setColorFilter(activityTypeView.getDefaultColor(), Mode.SRC_ATOP);
                if (activity.sources.length == 1 && activity.target_object_user_lists != null
                        && activity.target_object_user_lists.length == 1) {
                    final SpannableString firstDisplayName = new SpannableString(UserColorNameUtils.getDisplayName(context,
                            activity.sources[0]));
                    final SpannableString listName = new SpannableString(activity.target_object_user_lists[0].name);
                    firstDisplayName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstDisplayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    listName.setSpan(new StyleSpan(Typeface.BOLD), 0, listName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    final String format = context.getString(R.string.activity_about_me_list_member_added_with_name);
                    final Configuration configuration = resources.getConfiguration();
                    titleView.setText(SpanFormatter.format(configuration.locale, format, firstDisplayName,
                            listName));
                } else {
                    titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_list_member_added,
                            R.string.activity_about_me_list_member_added_multi, activity.sources));
                }
                displayUserProfileImages(activity.sources);
                summaryView.setVisibility(View.GONE);
                break;
            }
        }
    }

    public void setTextSize(float textSize) {
        titleView.setTextSize(textSize);
        summaryView.setTextSize(textSize * 0.85f);
    }

    private void displayUserProfileImages(final ParcelableUser[] statuses) {
        final MediaLoaderWrapper imageLoader = adapter.getImageLoader();
        if (statuses == null) {
            for (final ImageView view : profileImageViews) {
                imageLoader.cancelDisplayTask(view);
                view.setVisibility(View.GONE);
            }
            return;
        }
        final int length = Math.min(profileImageViews.length, statuses.length);
        final boolean shouldDisplayImages = true;
        profileImagesContainer.setVisibility(shouldDisplayImages ? View.VISIBLE : View.GONE);
        if (!shouldDisplayImages) return;
        for (int i = 0, j = profileImageViews.length; i < j; i++) {
            final ImageView view = profileImageViews[i];
            view.setImageDrawable(null);
            if (i < length) {
                view.setVisibility(View.VISIBLE);
                imageLoader.displayProfileImage(view, statuses[i].profile_image_url);
            } else {
                imageLoader.cancelDisplayTask(view);
                view.setVisibility(View.GONE);
            }
        }
        if (statuses.length > profileImageViews.length) {
            final Context context = adapter.getContext();
            final int moreNumber = statuses.length - profileImageViews.length;
            profileImageMoreNumber.setVisibility(View.VISIBLE);
            profileImageMoreNumber.setText(context.getString(R.string.and_more, moreNumber));
        } else {
            profileImageMoreNumber.setVisibility(View.GONE);
        }
    }

    private Spanned getTitleStringAboutMe(int stringRes, int stringResMulti, ParcelableUser[] sources) {
        if (sources == null || sources.length == 0) return null;
        final Context context = adapter.getContext();
        final Resources resources = context.getResources();
        final Configuration configuration = resources.getConfiguration();
        final SpannableString firstDisplayName = new SpannableString(UserColorNameUtils.getDisplayName(context,
                sources[0]));
        firstDisplayName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstDisplayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sources.length == 1) {
            final String format = context.getString(stringRes);
            return SpanFormatter.format(configuration.locale, format, firstDisplayName);
        } else if (sources.length == 2) {
            final String format = context.getString(stringResMulti);
            final SpannableString secondDisplayName = new SpannableString(UserColorNameUtils.getDisplayName(context, sources[1]));
            secondDisplayName.setSpan(new StyleSpan(Typeface.BOLD), 0, secondDisplayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return SpanFormatter.format(configuration.locale, format, firstDisplayName,
                    secondDisplayName);
        } else {
            final int othersCount = sources.length - 1;
            final SpannableString nOthers = new SpannableString(resources.getQuantityString(R.plurals.N_others, othersCount, othersCount));
            nOthers.setSpan(new StyleSpan(Typeface.BOLD), 0, nOthers.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            final String format = context.getString(stringResMulti);
            return SpanFormatter.format(configuration.locale, format, firstDisplayName, nOthers);
        }
    }

    private Spanned getTitleStringByFriends(int stringRes, int stringResMulti, ParcelableUser[] sources, ParcelableUser[] targets) {
        if (sources == null || sources.length == 0) return null;
        final Context context = adapter.getContext();
        final Resources resources = context.getResources();
        final Configuration configuration = resources.getConfiguration();
        final SpannableString firstSourceName = new SpannableString(UserColorNameUtils.getDisplayName(context,
                sources[0]));
        firstSourceName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstSourceName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        final SpannableString firstTargetName = new SpannableString(UserColorNameUtils.getDisplayName(context,
                targets[0]));
        firstTargetName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstTargetName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sources.length == 1) {
            final String format = context.getString(stringRes);
            return SpanFormatter.format(configuration.locale, format, firstSourceName, firstTargetName);
        } else if (sources.length == 2) {
            final String format = context.getString(stringResMulti);
            final SpannableString secondSourceName = new SpannableString(UserColorNameUtils.getDisplayName(context, sources[1]));
            secondSourceName.setSpan(new StyleSpan(Typeface.BOLD), 0, secondSourceName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return SpanFormatter.format(configuration.locale, format, firstSourceName,
                    secondSourceName, firstTargetName);
        } else {
            final int othersCount = sources.length - 1;
            final SpannableString nOthers = new SpannableString(resources.getQuantityString(R.plurals.N_others, othersCount, othersCount));
            nOthers.setSpan(new StyleSpan(Typeface.BOLD), 0, nOthers.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            final String format = context.getString(stringResMulti);
            return SpanFormatter.format(configuration.locale, format, firstSourceName, nOthers, firstTargetName);
        }
    }

}
