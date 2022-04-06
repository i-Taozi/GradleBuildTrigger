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

package org.getlantern.firetweet.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.getlantern.jsonserializer.JSONParcel;
import org.getlantern.jsonserializer.JSONParcelable;
import org.getlantern.firetweet.provider.FiretweetDataStore.CachedUsers;
import org.getlantern.firetweet.provider.FiretweetDataStore.DirectMessages.ConversationEntries;
import org.getlantern.firetweet.util.HtmlEscapeHelper;
import org.getlantern.firetweet.util.ParseUtils;
import org.getlantern.firetweet.util.TwitterContentUtils;

import twitter4j.URLEntity;
import twitter4j.User;

public class ParcelableUser implements FiretweetParcelable, Comparable<ParcelableUser> {

    public static final Parcelable.Creator<ParcelableUser> CREATOR = new Parcelable.Creator<ParcelableUser>() {
        @Override
        public ParcelableUser createFromParcel(final Parcel in) {
            return new ParcelableUser(in);
        }

        @Override
        public ParcelableUser[] newArray(final int size) {
            return new ParcelableUser[size];
        }
    };

    public static final JSONParcelable.Creator<ParcelableUser> JSON_CREATOR = new JSONParcelable.Creator<ParcelableUser>() {
        @Override
        public ParcelableUser createFromParcel(final JSONParcel in) {
            return new ParcelableUser(in);
        }

        @Override
        public ParcelableUser[] newArray(final int size) {
            return new ParcelableUser[size];
        }
    };

    public final long account_id, id, created_at, position;

    public final boolean is_protected, is_verified, is_follow_request_sent, is_following;

    public final String description_plain, name, screen_name, location, profile_image_url, profile_banner_url, url,
            url_expanded, description_html, description_unescaped, description_expanded;

    public final int followers_count, friends_count, statuses_count, favorites_count, listed_count;

    public final int background_color, link_color, text_color;

    public final boolean is_cache, is_basic;

    public ParcelableUser(final long account_id, final long id, final String name,
                          final String screen_name, final String profile_image_url) {
        this.account_id = account_id;
        this.id = id;
        this.name = name;
        this.screen_name = screen_name;
        this.profile_image_url = profile_image_url;
        this.created_at = 0;
        this.position = 0;
        is_protected = false;
        is_verified = false;
        is_follow_request_sent = false;
        is_following = false;
        description_plain = null;
        location = null;
        profile_banner_url = null;
        url = null;
        url_expanded = null;
        description_html = null;
        description_unescaped = null;
        description_expanded = null;
        followers_count = 0;
        friends_count = 0;
        statuses_count = 0;
        favorites_count = 0;
        listed_count = 0;
        background_color = 0;
        link_color = 0;
        text_color = 0;
        is_cache = true;
        is_basic = true;
    }

    public ParcelableUser(final Cursor cursor, CachedIndices indices, final long account_id) {
        this.account_id = account_id;
        position = -1;
        is_follow_request_sent = false;
        id = indices.id != -1 ? cursor.getLong(indices.id) : -1;
        name = indices.name != -1 ? cursor.getString(indices.name) : null;
        screen_name = indices.screen_name != -1 ? cursor.getString(indices.screen_name) : null;
        profile_image_url = indices.profile_image_url != -1 ? cursor.getString(indices.profile_image_url) : null;
        created_at = indices.created_at != -1 ? cursor.getLong(indices.created_at) : -1;
        is_protected = indices.is_protected != -1 && cursor.getInt(indices.is_protected) == 1;
        is_verified = indices.is_verified != -1 && cursor.getInt(indices.is_verified) == 1;
        favorites_count = indices.favorites_count != -1 ? cursor.getInt(indices.favorites_count) : 0;
        listed_count = indices.listed_count != -1 ? cursor.getInt(indices.listed_count) : 0;
        followers_count = indices.followers_count != -1 ? cursor.getInt(indices.followers_count) : 0;
        friends_count = indices.friends_count != -1 ? cursor.getInt(indices.friends_count) : 0;
        statuses_count = indices.statuses_count != -1 ? cursor.getInt(indices.statuses_count) : 0;
        location = indices.location != -1 ? cursor.getString(indices.location) : null;
        description_plain = indices.description_plain != -1 ? cursor.getString(indices.description_plain) : null;
        description_html = indices.description_html != -1 ? cursor.getString(indices.description_html) : null;
        description_expanded = indices.description_expanded != -1 ? cursor.getString(indices.description_expanded) : null;
        url = indices.url != -1 ? cursor.getString(indices.url) : null;
        url_expanded = indices.url_expanded != -1 ? cursor.getString(indices.url_expanded) : null;
        profile_banner_url = indices.profile_banner_url != -1 ? cursor.getString(indices.profile_banner_url) : null;
        description_unescaped = HtmlEscapeHelper.toPlainText(description_html);
        is_following = indices.is_following != -1 && cursor.getInt(indices.is_following) == 1;
        background_color = indices.background_color != -1 ? cursor.getInt(indices.background_color) : 0;
        link_color = indices.link_color != -1 ? cursor.getInt(indices.link_color) : 0;
        text_color = indices.text_color != -1 ? cursor.getInt(indices.text_color) : 0;
        is_cache = true;
        is_basic = indices.description_plain == -1 || indices.url == -1 || indices.location == -1;
    }

    public ParcelableUser(final JSONParcel in) {
        position = in.readLong("position");
        account_id = in.readLong("account_id");
        id = in.readLong("user_id");
        created_at = in.readLong("created_at");
        is_protected = in.readBoolean("is_protected");
        is_verified = in.readBoolean("is_verified");
        name = in.readString("name");
        screen_name = in.readString("screen_name");
        description_plain = in.readString("description_plain");
        description_html = in.readString("description_html");
        description_expanded = in.readString("description_expanded");
        description_unescaped = in.readString("description_unescaped");
        location = in.readString("location");
        profile_image_url = in.readString("profile_image_url");
        profile_banner_url = in.readString("profile_banner_url");
        url = in.readString("url");
        is_follow_request_sent = in.readBoolean("is_follow_request_sent");
        followers_count = in.readInt("followers_count");
        friends_count = in.readInt("friends_count");
        statuses_count = in.readInt("statuses_count");
        favorites_count = in.readInt("favorites_count");
        listed_count = in.readInt("listed_count");
        url_expanded = in.readString("url_expanded");
        is_following = in.readBoolean("is_following");
        background_color = in.readInt("background_color");
        link_color = in.readInt("link_color");
        text_color = in.readInt("text_color");
        is_cache = in.readBoolean("is_cache");
        is_basic = in.readBoolean("is_basic");
    }

    public ParcelableUser(final Parcel in) {
        position = in.readLong();
        account_id = in.readLong();
        id = in.readLong();
        created_at = in.readLong();
        is_protected = in.readInt() == 1;
        is_verified = in.readInt() == 1;
        name = in.readString();
        screen_name = in.readString();
        description_plain = in.readString();
        description_html = in.readString();
        description_expanded = in.readString();
        description_unescaped = in.readString();
        location = in.readString();
        profile_image_url = in.readString();
        profile_banner_url = in.readString();
        url = in.readString();
        is_follow_request_sent = in.readInt() == 1;
        followers_count = in.readInt();
        friends_count = in.readInt();
        statuses_count = in.readInt();
        favorites_count = in.readInt();
        listed_count = in.readInt();
        url_expanded = in.readString();
        is_following = in.readInt() == 1;
        background_color = in.readInt();
        link_color = in.readInt();
        text_color = in.readInt();
        is_cache = in.readInt() == 1;
        is_basic = in.readInt() == 1;
    }

    public ParcelableUser(final User user, final long account_id) {
        this(user, account_id, 0);
    }

    public ParcelableUser(final User user, final long account_id, final long position) {
        this.position = position;
        this.account_id = account_id;
        final URLEntity[] urls_url_entities = user.getURLEntities();
        id = user.getId();
        created_at = user.getCreatedAt().getTime();
        is_protected = user.isProtected();
        is_verified = user.isVerified();
        name = user.getName();
        screen_name = user.getScreenName();
        description_plain = user.getDescription();
        description_html = TwitterContentUtils.formatUserDescription(user);
        description_expanded = TwitterContentUtils.formatExpandedUserDescription(user);
        description_unescaped = HtmlEscapeHelper.toPlainText(description_html);
        location = user.getLocation();
        profile_image_url = user.getProfileImageUrlHttps();
        profile_banner_url = user.getProfileBannerImageUrl();
        url = user.getURL();
        url_expanded = url != null && urls_url_entities != null && urls_url_entities.length > 0 ? urls_url_entities[0].getExpandedURL() : null;
        is_follow_request_sent = user.isFollowRequestSent();
        followers_count = user.getFollowersCount();
        friends_count = user.getFriendsCount();
        statuses_count = user.getStatusesCount();
        favorites_count = user.getFavouritesCount();
        listed_count = user.getListedCount();
        is_following = user.isFollowing();
        background_color = ParseUtils.parseColor("#" + user.getProfileBackgroundColor(), 0);
        link_color = ParseUtils.parseColor("#" + user.getProfileLinkColor(), 0);
        text_color = ParseUtils.parseColor("#" + user.getProfileTextColor(), 0);
        is_cache = false;
        is_basic = false;
    }

    @Override
    public int compareTo(@NonNull final ParcelableUser that) {
        final long diff = position - that.position;
        if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) diff;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static ParcelableUser[] fromUsersArray(@Nullable final User[] users, long account_id) {
        if (users == null) return null;
        final ParcelableUser[] result = new ParcelableUser[users.length];
        for (int i = 0, j = users.length; i < j; i++) {
            result[i] = new ParcelableUser(users[i], account_id);
        }
        return result;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeLong(position);
        out.writeLong(account_id);
        out.writeLong(id);
        out.writeLong(created_at);
        out.writeInt(is_protected ? 1 : 0);
        out.writeInt(is_verified ? 1 : 0);
        out.writeString(name);
        out.writeString(screen_name);
        out.writeString(description_plain);
        out.writeString(description_html);
        out.writeString(description_expanded);
        out.writeString(description_unescaped);
        out.writeString(location);
        out.writeString(profile_image_url);
        out.writeString(profile_banner_url);
        out.writeString(url);
        out.writeInt(is_follow_request_sent ? 1 : 0);
        out.writeInt(followers_count);
        out.writeInt(friends_count);
        out.writeInt(statuses_count);
        out.writeInt(favorites_count);
        out.writeInt(listed_count);
        out.writeString(url_expanded);
        out.writeInt(is_following ? 1 : 0);
        out.writeInt(background_color);
        out.writeInt(link_color);
        out.writeInt(text_color);
        out.writeInt(is_cache ? 1 : 0);
        out.writeInt(is_basic ? 1 : 0);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof ParcelableUser)) return false;
        final ParcelableUser other = (ParcelableUser) obj;
        if (account_id != other.account_id) return false;
        if (id != other.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (account_id ^ account_id >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableUser{account_id=" + account_id + ", id=" + id + ", created_at=" + created_at + ", position="
                + position + ", is_protected=" + is_protected + ", is_verified=" + is_verified
                + ", is_follow_request_sent=" + is_follow_request_sent + ", is_following=" + is_following
                + ", description_plain=" + description_plain + ", name=" + name + ", screen_name=" + screen_name
                + ", location=" + location + ", profile_image_url=" + profile_image_url + ", profile_banner_url="
                + profile_banner_url + ", url=" + url + ", url_expanded=" + url_expanded + ", description_html="
                + description_html + ", description_unescaped=" + description_unescaped + ", description_expanded="
                + description_expanded + ", followers_count=" + followers_count + ", friends_count=" + friends_count
                + ", statuses_count=" + statuses_count + ", favorites_count=" + favorites_count + ", is_cache="
                + is_cache + "}";
    }

    public static ParcelableUser fromDirectMessageConversationEntry(final Cursor cursor) {
        final long account_id = cursor.getLong(ConversationEntries.IDX_ACCOUNT_ID);
        final long id = cursor.getLong(ConversationEntries.IDX_CONVERSATION_ID);
        final String name = cursor.getString(ConversationEntries.IDX_NAME);
        final String screen_name = cursor.getString(ConversationEntries.IDX_SCREEN_NAME);
        final String profile_image_url = cursor.getString(ConversationEntries.IDX_PROFILE_IMAGE_URL);
        return new ParcelableUser(account_id, id, name, screen_name, profile_image_url);
    }

    public static ContentValues makeCachedUserContentValues(final ParcelableUser user) {
        if (user == null) return null;
        final ContentValues values = new ContentValues();
        values.put(CachedUsers.USER_ID, user.id);
        values.put(CachedUsers.NAME, user.name);
        values.put(CachedUsers.SCREEN_NAME, user.screen_name);
        values.put(CachedUsers.PROFILE_IMAGE_URL, user.profile_image_url);
        values.put(CachedUsers.CREATED_AT, user.created_at);
        values.put(CachedUsers.IS_PROTECTED, user.is_protected);
        values.put(CachedUsers.IS_VERIFIED, user.is_verified);
        values.put(CachedUsers.LISTED_COUNT, user.listed_count);
        values.put(CachedUsers.FAVORITES_COUNT, user.favorites_count);
        values.put(CachedUsers.FOLLOWERS_COUNT, user.followers_count);
        values.put(CachedUsers.FRIENDS_COUNT, user.friends_count);
        values.put(CachedUsers.STATUSES_COUNT, user.statuses_count);
        values.put(CachedUsers.LOCATION, user.location);
        values.put(CachedUsers.DESCRIPTION_PLAIN, user.description_plain);
        values.put(CachedUsers.DESCRIPTION_HTML, user.description_html);
        values.put(CachedUsers.DESCRIPTION_EXPANDED, user.description_expanded);
        values.put(CachedUsers.URL, user.url);
        values.put(CachedUsers.URL_EXPANDED, user.url_expanded);
        values.put(CachedUsers.PROFILE_BANNER_URL, user.profile_banner_url);
        values.put(CachedUsers.IS_FOLLOWING, user.is_following);
        values.put(CachedUsers.BACKGROUND_COLOR, user.background_color);
        values.put(CachedUsers.LINK_COLOR, user.link_color);
        values.put(CachedUsers.TEXT_COLOR, user.text_color);
        return values;
    }

    @Override
    public void writeToParcel(final JSONParcel out) {
        out.writeLong("position", position);
        out.writeLong("account_id", account_id);
        out.writeLong("user_id", id);
        out.writeLong("created_at", created_at);
        out.writeBoolean("is_protected", is_protected);
        out.writeBoolean("is_verified", is_verified);
        out.writeString("name", name);
        out.writeString("screen_name", screen_name);
        out.writeString("description_plain", description_plain);
        out.writeString("description_html", description_html);
        out.writeString("description_expanded", description_expanded);
        out.writeString("description_unescaped", description_unescaped);
        out.writeString("location", location);
        out.writeString("profile_image_url", profile_image_url);
        out.writeString("profile_banner_url", profile_banner_url);
        out.writeString("url", url);
        out.writeBoolean("is_follow_request_sent", is_follow_request_sent);
        out.writeInt("followers_count", followers_count);
        out.writeInt("friends_count", friends_count);
        out.writeInt("statuses_count", statuses_count);
        out.writeInt("favorites_count", favorites_count);
        out.writeInt("listed_count", listed_count);
        out.writeString("url_expanded", url_expanded);
        out.writeBoolean("is_following", is_following);
        out.writeInt("background_color", background_color);
        out.writeInt("link_color", link_color);
        out.writeInt("text_color", text_color);
        out.writeBoolean("is_cache", is_cache);
        out.writeBoolean("is_basic", is_basic);
    }

    public static final class CachedIndices {

        public final int id, name, screen_name, profile_image_url, created_at, is_protected,
                is_verified, favorites_count, listed_count, followers_count, friends_count,
                statuses_count, location, description_plain, description_html, description_expanded,
                url, url_expanded, profile_banner_url, is_following, background_color, link_color, text_color;

        public CachedIndices(Cursor cursor) {
            id = cursor.getColumnIndex(CachedUsers.USER_ID);
            name = cursor.getColumnIndex(CachedUsers.NAME);
            screen_name = cursor.getColumnIndex(CachedUsers.SCREEN_NAME);
            profile_image_url = cursor.getColumnIndex(CachedUsers.PROFILE_IMAGE_URL);
            created_at = cursor.getColumnIndex(CachedUsers.CREATED_AT);
            is_protected = cursor.getColumnIndex(CachedUsers.IS_PROTECTED);
            is_verified = cursor.getColumnIndex(CachedUsers.IS_VERIFIED);
            favorites_count = cursor.getColumnIndex(CachedUsers.FAVORITES_COUNT);
            listed_count = cursor.getColumnIndex(CachedUsers.LISTED_COUNT);
            followers_count = cursor.getColumnIndex(CachedUsers.FOLLOWERS_COUNT);
            friends_count = cursor.getColumnIndex(CachedUsers.FRIENDS_COUNT);
            statuses_count = cursor.getColumnIndex(CachedUsers.STATUSES_COUNT);
            location = cursor.getColumnIndex(CachedUsers.LOCATION);
            description_plain = cursor.getColumnIndex(CachedUsers.DESCRIPTION_PLAIN);
            description_html = cursor.getColumnIndex(CachedUsers.DESCRIPTION_HTML);
            description_expanded = cursor.getColumnIndex(CachedUsers.DESCRIPTION_EXPANDED);
            url = cursor.getColumnIndex(CachedUsers.URL);
            url_expanded = cursor.getColumnIndex(CachedUsers.URL_EXPANDED);
            profile_banner_url = cursor.getColumnIndex(CachedUsers.PROFILE_BANNER_URL);
            is_following = cursor.getColumnIndex(CachedUsers.IS_FOLLOWING);
            background_color = cursor.getColumnIndex(CachedUsers.BACKGROUND_COLOR);
            link_color = cursor.getColumnIndex(CachedUsers.LINK_COLOR);
            text_color = cursor.getColumnIndex(CachedUsers.TEXT_COLOR);
        }

    }

}
