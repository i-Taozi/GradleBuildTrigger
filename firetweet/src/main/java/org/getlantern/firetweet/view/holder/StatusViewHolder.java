package org.getlantern.firetweet.view.holder;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.getlantern.firetweet.Constants;
import org.getlantern.firetweet.R;
import org.getlantern.firetweet.adapter.iface.ContentCardClickListener;
import org.getlantern.firetweet.adapter.iface.IStatusesAdapter;
import org.getlantern.firetweet.app.FiretweetApplication;
import org.getlantern.firetweet.model.ParcelableLocation;
import org.getlantern.firetweet.model.ParcelableMedia;
import org.getlantern.firetweet.model.ParcelableStatus;
import org.getlantern.firetweet.model.ParcelableStatus.CursorIndices;
import org.getlantern.firetweet.util.AsyncTwitterWrapper;
import org.getlantern.firetweet.util.ImageLoadingHandler;
import org.getlantern.firetweet.util.MediaLoaderWrapper;
import org.getlantern.firetweet.util.SharedPreferencesWrapper;
import org.getlantern.firetweet.util.SimpleValueSerializer;
import org.getlantern.firetweet.util.FiretweetLinkify;
import org.getlantern.firetweet.util.TwitterCardUtils;
import org.getlantern.firetweet.util.UserColorNameUtils;
import org.getlantern.firetweet.util.Utils;
import org.getlantern.firetweet.util.Utils.OnMediaClickListener;
import org.getlantern.firetweet.view.CardMediaContainer;
import org.getlantern.firetweet.view.ForegroundColorView;
import org.getlantern.firetweet.view.ShapedImageView;
import org.getlantern.firetweet.view.ShortTimeView;
import org.getlantern.firetweet.view.iface.IColorLabelView;

import java.util.Locale;

import twitter4j.TranslationResult;

import static org.getlantern.firetweet.util.HtmlEscapeHelper.toPlainText;
import static org.getlantern.firetweet.util.UserColorNameUtils.getUserNickname;
import static org.getlantern.firetweet.util.Utils.getUserTypeIconRes;

/**
 * IDE gives me warning if I don't change default comment, so I write this XD
 * <p/>
 * Created by mariotaku on 14/11/19.
 */
public class StatusViewHolder extends RecyclerView.ViewHolder implements Constants, OnClickListener,
        OnMediaClickListener {

    @NonNull
    private final IStatusesAdapter<?> adapter;

    private final ImageView replyRetweetIcon;
    private final ShapedImageView profileImageView;
    private final ImageView profileTypeView;
    private final ImageView extraTypeView;
    private final TextView textView;
    private final TextView quoteTextView;
    private final TextView nameView, screenNameView;
    private final TextView quotedNameView, quotedScreenNameView;
    private final TextView replyRetweetView;
    private final ShortTimeView timeView;
    private final CardMediaContainer mediaPreview;
    private final TextView replyCountView, retweetCountView, favoriteCountView;
    private final View quotedNameContainer;
    private final IColorLabelView itemContent;
    private final ForegroundColorView quoteIndicator;
    private final View actionButtons;
    private final View itemMenu;

    private StatusClickListener statusClickListener;

    public StatusViewHolder(@NonNull final IStatusesAdapter<?> adapter, @NonNull final View itemView) {
        super(itemView);
        this.adapter = adapter;
        itemContent = (IColorLabelView) itemView.findViewById(R.id.item_content);
        profileImageView = (ShapedImageView) itemView.findViewById(R.id.profile_image);
        profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
        extraTypeView = (ImageView) itemView.findViewById(R.id.extra_type);
        textView = (TextView) itemView.findViewById(R.id.text);
        quoteTextView = (TextView) itemView.findViewById(R.id.quote_text);
        nameView = (TextView) itemView.findViewById(R.id.name);
        screenNameView = (TextView) itemView.findViewById(R.id.screen_name);
        quotedNameView = (TextView) itemView.findViewById(R.id.quoted_name);
        quotedScreenNameView = (TextView) itemView.findViewById(R.id.quoted_screen_name);
        replyRetweetIcon = (ImageView) itemView.findViewById(R.id.reply_retweet_icon);
        replyRetweetView = (TextView) itemView.findViewById(R.id.reply_retweet_status);
        timeView = (ShortTimeView) itemView.findViewById(R.id.time);

        mediaPreview = (CardMediaContainer) itemView.findViewById(R.id.media_preview);
        quotedNameContainer = itemView.findViewById(R.id.quoted_name_container);

        quoteIndicator = (ForegroundColorView) itemView.findViewById(R.id.quote_indicator);

        itemMenu = itemView.findViewById(R.id.item_menu);
        actionButtons = itemView.findViewById(R.id.action_buttons);

        replyCountView = (TextView) itemView.findViewById(R.id.reply_count);
        retweetCountView = (TextView) itemView.findViewById(R.id.retweet_count);
        favoriteCountView = (TextView) itemView.findViewById(R.id.favorite_count);
        //TODO
        // profileImageView.setSelectorColor(ThemeUtils.getUserHighlightColor(itemView.getContext()));
    }

    public void displaySampleStatus() {
        profileImageView.setVisibility(adapter.isProfileImageEnabled() ? View.VISIBLE : View.GONE);
        profileImageView.setImageResource(R.mipmap.ic_launcher);
        nameView.setText(TWIDERE_PREVIEW_NAME);
        screenNameView.setText("@" + TWIDERE_PREVIEW_SCREEN_NAME);
        if (adapter.getLinkHighlightingStyle() == VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            textView.setText(Html.fromHtml(TWIDERE_PREVIEW_TEXT_HTML));
            adapter.getFiretweetLinkify().applyAllLinks(textView, -1, -1, false, adapter.getLinkHighlightingStyle());
        } else {
            textView.setText(toPlainText(TWIDERE_PREVIEW_TEXT_HTML));
        }
        textView.setMovementMethod(null);
        timeView.setTime(System.currentTimeMillis());
        mediaPreview.setVisibility(adapter.isMediaPreviewEnabled() ? View.VISIBLE : View.GONE);
        mediaPreview.displayMedia(R.drawable.nyan_stars_background);
        extraTypeView.setImageResource(R.drawable.ic_action_gallery);
    }

    public void displayStatus(final ParcelableStatus status, final boolean displayInReplyTo) {
        displayStatus(status, null, displayInReplyTo, true);
    }

    public void displayStatus(@NonNull final ParcelableStatus status, @Nullable final TranslationResult translation,
                              final boolean displayInReplyTo, final boolean shouldDisplayExtraType) {
        final MediaLoaderWrapper loader = adapter.getImageLoader();
        final AsyncTwitterWrapper twitter = adapter.getTwitterWrapper();
        final FiretweetLinkify linkify = adapter.getFiretweetLinkify();
        final Context context = adapter.getContext();
        final boolean nameFirst = adapter.isNameFirst();

        final long reply_count = status.reply_count;
        final long retweet_count;
        final long favorite_count;

        if (status.retweet_id > 0) {
            final String retweetedBy = UserColorNameUtils.getDisplayName(context, status.retweeted_by_id,
                    status.retweeted_by_name, status.retweeted_by_screen_name, nameFirst);
            replyRetweetView.setText(context.getString(R.string.name_retweeted, retweetedBy));
            replyRetweetIcon.setImageResource(R.drawable.ic_activity_action_retweet);
            replyRetweetView.setVisibility(View.VISIBLE);
            replyRetweetIcon.setVisibility(View.VISIBLE);
        } else if (status.in_reply_to_status_id > 0 && status.in_reply_to_user_id > 0 && displayInReplyTo) {
            final String inReplyTo = UserColorNameUtils.getDisplayName(context, status.in_reply_to_user_id,
                    status.in_reply_to_name, status.in_reply_to_screen_name, nameFirst);
            replyRetweetView.setText(context.getString(R.string.in_reply_to_name, inReplyTo));
            replyRetweetIcon.setImageResource(R.drawable.ic_activity_action_reply);
            replyRetweetView.setVisibility(View.VISIBLE);
            replyRetweetIcon.setVisibility(View.VISIBLE);
        } else {
            replyRetweetView.setVisibility(View.GONE);
            replyRetweetIcon.setVisibility(View.GONE);
        }

        final int typeIconRes = getUserTypeIconRes(status.user_is_verified, status.user_is_protected);
        if (typeIconRes != 0) {
            profileTypeView.setImageResource(typeIconRes);
            profileTypeView.setVisibility(View.VISIBLE);
        } else {
            profileTypeView.setImageDrawable(null);
            profileTypeView.setVisibility(View.GONE);
        }

        if (status.is_quote) {
            quotedNameView.setText(getUserNickname(context, status.user_id, status.user_name, true));
            quotedScreenNameView.setText("@" + status.user_screen_name);
            timeView.setTime(status.quote_timestamp);
            nameView.setText(getUserNickname(context, status.quoted_by_user_id, status.quoted_by_user_name, true));
            screenNameView.setText("@" + status.quoted_by_user_screen_name);

            final int idx = status.quote_text_unescaped.lastIndexOf(" twitter.com");
            if (adapter.getLinkHighlightingStyle() == VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
                final String text = status.quote_text_unescaped;
                quoteTextView.setText(idx > 0 ? text.substring(0, idx - 1) : text);
            } else {
                final Spanned text = Html.fromHtml(status.quote_text_html);
                quoteTextView.setText(idx > 0 ? text.subSequence(0, idx - 1) : text);
                linkify.applyAllLinks(quoteTextView, status.account_id, getLayoutPosition(),
                        status.is_possibly_sensitive, adapter.getLinkHighlightingStyle());
                quoteTextView.setMovementMethod(null);
            }

            quotedNameContainer.setVisibility(View.VISIBLE);
            quoteTextView.setVisibility(View.VISIBLE);
            quoteIndicator.setVisibility(View.VISIBLE);

            quoteIndicator.setColor(UserColorNameUtils.getUserColor(context, status.user_id));

            if (adapter.isProfileImageEnabled()) {
                profileTypeView.setVisibility(View.VISIBLE);
                profileImageView.setVisibility(View.VISIBLE);
                loader.displayProfileImage(profileImageView, status.quoted_by_user_profile_image);
            } else {
                profileTypeView.setVisibility(View.GONE);
                profileImageView.setVisibility(View.GONE);
                loader.cancelDisplayTask(profileImageView);
            }

            final int userColor = UserColorNameUtils.getUserColor(context, status.quoted_by_user_id);
            itemContent.drawStart(userColor);
        } else {
            nameView.setText(getUserNickname(context, status.user_id, status.user_name, true));
            screenNameView.setText("@" + status.user_screen_name);
            timeView.setTime(status.timestamp);

            quotedNameContainer.setVisibility(View.GONE);
            quoteTextView.setVisibility(View.GONE);
            quoteIndicator.setVisibility(View.GONE);

            if (adapter.isProfileImageEnabled()) {
                final String user_profile_image_url = status.user_profile_image_url;
                profileTypeView.setVisibility(View.VISIBLE);
                profileImageView.setVisibility(View.VISIBLE);
                loader.displayProfileImage(profileImageView, user_profile_image_url);
            } else {
                profileTypeView.setVisibility(View.GONE);
                profileImageView.setVisibility(View.GONE);
                loader.cancelDisplayTask(profileImageView);
            }
            final int userColor = UserColorNameUtils.getUserColor(context, status.user_id);
            itemContent.drawStart(userColor);
        }


        if (adapter.shouldShowAccountsColor()) {
            itemContent.drawEnd(Utils.getAccountColor(context, status.account_id));
        } else {
            itemContent.drawEnd();
        }


        if (adapter.isMediaPreviewEnabled()) {
            mediaPreview.setStyle(adapter.getMediaPreviewStyle());
            final boolean hasMedia = status.media != null && status.media.length > 0;
            if (hasMedia && (adapter.isSensitiveContentEnabled() || !status.is_possibly_sensitive)) {
                mediaPreview.setVisibility(hasMedia ? View.VISIBLE : View.GONE);
                mediaPreview.displayMedia(status.media, loader, status.account_id, this,
                        adapter.getImageLoadingHandler());
            } else {
                mediaPreview.setVisibility(View.GONE);
            }
        } else {
            mediaPreview.setVisibility(View.GONE);
        }
        if (adapter.getLinkHighlightingStyle() == VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            textView.setText(status.text_unescaped);
        } else {
            textView.setText(Html.fromHtml(status.text_html));
            linkify.applyAllLinks(textView, status.account_id, getLayoutPosition(),
                    status.is_possibly_sensitive,
                    adapter.getLinkHighlightingStyle());
            textView.setMovementMethod(null);
        }

        if (reply_count > 0) {
            replyCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), reply_count));
        } else {
            replyCountView.setText(null);
        }

        if (twitter.isDestroyingStatus(status.account_id, status.my_retweet_id)) {
            retweetCountView.setActivated(false);
            retweet_count = Math.max(0, status.retweet_count - 1);
        } else {
            final boolean creatingRetweet = twitter.isCreatingRetweet(status.account_id, status.id);
            retweetCountView.setActivated(creatingRetweet || Utils.isMyRetweet(status.account_id,
                    status.retweeted_by_id, status.my_retweet_id));
            retweet_count = status.retweet_count + (creatingRetweet ? 1 : 0);
        }
        if (retweet_count > 0) {
            retweetCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), retweet_count));
        } else {
            retweetCountView.setText(null);
        }
        if (twitter.isDestroyingFavorite(status.account_id, status.id)) {
            favoriteCountView.setActivated(false);
            favorite_count = Math.max(0, status.favorite_count - 1);
        } else {
            final boolean creatingFavorite = twitter.isCreatingFavorite(status.account_id, status.id);
            favoriteCountView.setActivated(creatingFavorite || status.is_favorite);
            favorite_count = status.favorite_count + (creatingFavorite ? 1 : 0);
        }
        if (favorite_count > 0) {
            favoriteCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), favorite_count));
        } else {
            favoriteCountView.setText(null);
        }
        if (shouldDisplayExtraType) {
            displayExtraTypeIcon(status.card_name, status.media, status.location, status.place_full_name,
                    status.is_possibly_sensitive);
        } else {
            extraTypeView.setVisibility(View.GONE);
        }
    }

    public void displayStatus(@NonNull Cursor cursor, @NonNull CursorIndices indices,
                              final boolean displayInReplyTo) {
        final MediaLoaderWrapper loader = adapter.getImageLoader();
        final AsyncTwitterWrapper twitter = adapter.getTwitterWrapper();
        final FiretweetLinkify linkify = adapter.getFiretweetLinkify();
        final Context context = adapter.getContext();
        final boolean nameFirst = adapter.isNameFirst();

        final long reply_count = cursor.getLong(indices.reply_count);
        final long retweet_count;
        final long favorite_count;

        final long account_id = cursor.getLong(indices.account_id);
        final long user_id = cursor.getLong(indices.user_id);
        final long status_id = cursor.getLong(indices.status_id);
        final long retweet_id = cursor.getLong(indices.retweet_id);
        final long my_retweet_id = cursor.getLong(indices.my_retweet_id);
        final long in_reply_to_status_id = cursor.getLong(indices.in_reply_to_status_id);
        final long in_reply_to_user_id = cursor.getLong(indices.in_reply_to_user_id);

        final String user_name = cursor.getString(indices.user_name);
        final String user_screen_name = cursor.getString(indices.user_screen_name);
        final String card_name = cursor.getString(indices.card_name);
        final String place_full_name = cursor.getString(indices.place_full_name);

        final boolean sensitive = cursor.getShort(indices.is_possibly_sensitive) == 1;

        final ParcelableMedia[] media = SimpleValueSerializer.fromSerializedString(
                cursor.getString(indices.media), ParcelableMedia.SIMPLE_CREATOR);
        final ParcelableLocation location = ParcelableLocation.fromString(
                cursor.getString(indices.location));

        if (retweet_id > 0) {
            final long retweeted_by_id = cursor.getLong(indices.retweeted_by_user_id);
            final String retweeted_by_name = cursor.getString(indices.retweeted_by_user_name);
            final String retweeted_by_screen_name = cursor.getString(indices.retweeted_by_user_screen_name);
            final String retweetedBy = UserColorNameUtils.getDisplayName(context, retweeted_by_id,
                    retweeted_by_name, retweeted_by_screen_name, nameFirst);
            replyRetweetView.setText(context.getString(R.string.name_retweeted, retweetedBy));
            replyRetweetIcon.setImageResource(R.drawable.ic_activity_action_retweet);
            replyRetweetView.setVisibility(View.VISIBLE);
            replyRetweetIcon.setVisibility(View.VISIBLE);
        } else if (in_reply_to_status_id > 0 && in_reply_to_user_id > 0 && displayInReplyTo) {
            final String in_reply_to_name = cursor.getString(indices.in_reply_to_user_name);
            final String in_reply_to_screen_name = cursor.getString(indices.in_reply_to_user_screen_name);
            final String inReplyTo = UserColorNameUtils.getDisplayName(context, in_reply_to_user_id,
                    in_reply_to_name, in_reply_to_screen_name, nameFirst);
            replyRetweetView.setText(context.getString(R.string.in_reply_to_name, inReplyTo));
            replyRetweetIcon.setImageResource(R.drawable.ic_activity_action_reply);
            replyRetweetView.setVisibility(View.VISIBLE);
            replyRetweetIcon.setVisibility(View.VISIBLE);
        } else {
            replyRetweetView.setVisibility(View.GONE);
            replyRetweetIcon.setVisibility(View.GONE);
        }

        final int typeIconRes;

        if (cursor.getShort(indices.is_quote) == 1) {
            quotedNameView.setText(user_name);
            quotedScreenNameView.setText("@" + user_screen_name);
            timeView.setTime(cursor.getLong(indices.quote_timestamp));
            nameView.setText(cursor.getString(indices.quoted_by_user_name));
            screenNameView.setText("@" + cursor.getString(indices.quoted_by_user_screen_name));

            final String quote_text_unescaped = cursor.getString(indices.quote_text_unescaped);
            final int idx = quote_text_unescaped.lastIndexOf(" twitter.com");
            if (adapter.getLinkHighlightingStyle() == VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
                quoteTextView.setText(idx > 0 ? quote_text_unescaped.substring(0, idx - 1) : quote_text_unescaped);
            } else {
                final Spanned text = Html.fromHtml(cursor.getString(indices.quote_text_html));
                quoteTextView.setText(idx > 0 ? text.subSequence(0, idx - 1) : text);
                linkify.applyAllLinks(quoteTextView, account_id, getLayoutPosition(),
                        cursor.getShort(indices.is_possibly_sensitive) == 1,
                        adapter.getLinkHighlightingStyle());
                quoteTextView.setMovementMethod(null);
            }

            quotedNameContainer.setVisibility(View.VISIBLE);
            quoteTextView.setVisibility(View.VISIBLE);
            quoteIndicator.setVisibility(View.VISIBLE);

            quoteIndicator.setColor(UserColorNameUtils.getUserColor(context, user_id));

            if (adapter.isProfileImageEnabled()) {
                profileTypeView.setVisibility(View.VISIBLE);
                profileImageView.setVisibility(View.VISIBLE);
                loader.displayProfileImage(profileImageView, cursor.getString(indices.quoted_by_user_profile_image));
            } else {
                profileTypeView.setVisibility(View.GONE);
                profileImageView.setVisibility(View.GONE);
                loader.cancelDisplayTask(profileImageView);
            }

            final int userColor = UserColorNameUtils.getUserColor(context, cursor.getLong(indices.quoted_by_user_id));
            itemContent.drawStart(userColor);

            typeIconRes = getUserTypeIconRes(cursor.getShort(indices.quoted_by_user_is_verified) == 1,
                    cursor.getShort(indices.quoted_by_user_is_protected) == 1);
        } else {
            nameView.setText(user_name);
            screenNameView.setText("@" + user_screen_name);
            timeView.setTime(cursor.getLong(indices.status_timestamp));

            quotedNameContainer.setVisibility(View.GONE);
            quoteTextView.setVisibility(View.GONE);
            quoteIndicator.setVisibility(View.GONE);

            if (adapter.isProfileImageEnabled()) {
                final String user_profile_image_url = cursor.getString(indices.user_profile_image_url);
                profileTypeView.setVisibility(View.VISIBLE);
                profileImageView.setVisibility(View.VISIBLE);
                loader.displayProfileImage(profileImageView, user_profile_image_url);
            } else {
                profileTypeView.setVisibility(View.GONE);
                profileImageView.setVisibility(View.GONE);
                loader.cancelDisplayTask(profileImageView);
            }
            final int userColor = UserColorNameUtils.getUserColor(context, user_id);
            itemContent.drawStart(userColor);

            typeIconRes = getUserTypeIconRes(cursor.getShort(indices.is_verified) == 1,
                    cursor.getShort(indices.is_protected) == 1);
        }

        if (typeIconRes != 0) {
            profileTypeView.setImageResource(typeIconRes);
            profileTypeView.setVisibility(View.VISIBLE);
        } else {
            profileTypeView.setImageDrawable(null);
            profileTypeView.setVisibility(View.GONE);
        }

        if (adapter.shouldShowAccountsColor()) {
            itemContent.drawEnd(Utils.getAccountColor(context, account_id));
        } else {
            itemContent.drawEnd();
        }


        if (adapter.isMediaPreviewEnabled()) {
            final boolean hasMedia = media != null && media.length > 0;
            if (hasMedia && (adapter.isSensitiveContentEnabled() || !sensitive)) {
                mediaPreview.setVisibility(hasMedia ? View.VISIBLE : View.GONE);
                mediaPreview.displayMedia(media, loader, account_id, this, adapter.getImageLoadingHandler());
            } else {
                mediaPreview.setVisibility(View.GONE);
            }
        } else {
            mediaPreview.setVisibility(View.GONE);
        }
        if (adapter.getLinkHighlightingStyle() == VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            textView.setText(cursor.getString(indices.text_unescaped));
        } else {
            textView.setText(Html.fromHtml(cursor.getString(indices.text_html)));
            linkify.applyAllLinks(textView, account_id, getLayoutPosition(),
                    cursor.getShort(indices.is_possibly_sensitive) == 1,
                    adapter.getLinkHighlightingStyle());
            textView.setMovementMethod(null);
        }

        if (reply_count > 0) {
            replyCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), reply_count));
        } else {
            replyCountView.setText(null);
        }

        if (twitter.isDestroyingStatus(account_id, my_retweet_id)) {
            retweetCountView.setActivated(false);
            retweet_count = Math.max(0, cursor.getLong(indices.retweet_count) - 1);
        } else {
            final boolean creatingRetweet = twitter.isCreatingRetweet(account_id, status_id);
            final long retweeted_by_id = cursor.getLong(indices.retweeted_by_user_id);
            retweetCountView.setActivated(creatingRetweet || Utils.isMyRetweet(account_id,
                    retweeted_by_id, my_retweet_id));
            retweet_count = cursor.getLong(indices.retweet_count) + (creatingRetweet ? 1 : 0);
        }
        if (retweet_count > 0) {
            retweetCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), retweet_count));
        } else {
            retweetCountView.setText(null);
        }
        if (twitter.isDestroyingFavorite(account_id, status_id)) {
            favoriteCountView.setActivated(false);
            favorite_count = Math.max(0, cursor.getLong(indices.favorite_count) - 1);
        } else {
            final boolean creatingFavorite = twitter.isCreatingFavorite(account_id, status_id);
            favoriteCountView.setActivated(creatingFavorite || cursor.getShort(indices.is_favorite) == 1);
            favorite_count = cursor.getLong(indices.favorite_count) + (creatingFavorite ? 1 : 0);
        }
        if (favorite_count > 0) {
            favoriteCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), favorite_count));
        } else {
            favoriteCountView.setText(null);
        }
        displayExtraTypeIcon(card_name, media, location, place_full_name, sensitive);
    }

    public CardView getCardView() {
        return (CardView) itemView.findViewById(R.id.card);
    }

    public ShapedImageView getProfileImageView() {
        return profileImageView;
    }

    public ImageView getProfileTypeView() {
        return profileTypeView;
    }

    @Override
    public void onClick(View v) {
        if (statusClickListener == null) return;
        final int position = getAdapterPosition();
        switch (v.getId()) {
            case R.id.item_content: {
                statusClickListener.onStatusClick(this, position);
                break;
            }
            case R.id.item_menu: {
                statusClickListener.onItemMenuClick(this, v, position);
                break;
            }
            case R.id.profile_image: {
                statusClickListener.onUserProfileClick(this, position);
                break;
            }
            case R.id.reply_count:
            case R.id.retweet_count:
            case R.id.favorite_count: {
                statusClickListener.onItemActionClick(this, v.getId(), position);
                break;
            }
        }
    }

    @Override
    public void onMediaClick(View view, ParcelableMedia media, long accountId) {
        if (statusClickListener == null) return;
        final int position = getAdapterPosition();
        statusClickListener.onMediaClick(this, media, position);
    }

    public void setOnClickListeners() {
        setStatusClickListener(adapter);
    }

    public void setStatusClickListener(StatusClickListener listener) {
        statusClickListener = listener;
        itemView.findViewById(R.id.item_content).setOnClickListener(this);

        itemMenu.setOnClickListener(this);
        itemView.setOnClickListener(this);
        profileImageView.setOnClickListener(this);
        replyCountView.setOnClickListener(this);
        retweetCountView.setOnClickListener(this);
        favoriteCountView.setOnClickListener(this);
    }

    public void setTextSize(final float textSize) {
        nameView.setTextSize(textSize);
        quotedNameView.setTextSize(textSize);
        textView.setTextSize(textSize);
        quoteTextView.setTextSize(textSize);
        screenNameView.setTextSize(textSize * 0.85f);
        quotedScreenNameView.setTextSize(textSize * 0.85f);
        timeView.setTextSize(textSize * 0.85f);
        replyRetweetView.setTextSize(textSize * 0.75f);
        replyCountView.setTextSize(textSize);
        replyCountView.setTextSize(textSize);
        favoriteCountView.setTextSize(textSize);
    }

    public void setupViewOptions() {
        setTextSize(adapter.getTextSize());
        mediaPreview.setStyle(adapter.getMediaPreviewStyle());
        profileImageView.setStyle(adapter.getProfileImageStyle());
        actionButtons.setVisibility(adapter.isCardActionsHidden() ? View.GONE : View.VISIBLE);
        itemMenu.setVisibility(adapter.isCardActionsHidden() ? View.GONE : View.VISIBLE);
    }

    private void displayExtraTypeIcon(String cardName, ParcelableMedia[] media, ParcelableLocation location, String placeFullName, boolean sensitive) {
        if (TwitterCardUtils.CARD_NAME_AUDIO.equals(cardName)) {
            extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_music);
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (TwitterCardUtils.CARD_NAME_ANIMATED_GIF.equals(cardName)) {
            extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_movie);
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (TwitterCardUtils.CARD_NAME_PLAYER.equals(cardName)) {
            extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_play_circle);
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (media != null && media.length > 0) {
            if (hasVideo(media)) {
                extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_movie);
            } else {
                extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_gallery);
            }
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (ParcelableLocation.isValidLocation(location) || !TextUtils.isEmpty(placeFullName)) {
            extraTypeView.setImageResource(R.drawable.ic_action_location);
            extraTypeView.setVisibility(View.VISIBLE);
        } else {
            extraTypeView.setVisibility(View.GONE);
        }
    }

    private boolean hasVideo(ParcelableMedia[] media) {
        for (ParcelableMedia mediaItem : media) {
            if (mediaItem.type == ParcelableMedia.TYPE_VIDEO) return true;
        }
        return false;
    }

    public static interface StatusClickListener extends ContentCardClickListener {

        boolean isProfileImageEnabled();

        void onMediaClick(StatusViewHolder holder, ParcelableMedia media, int position);

        void onStatusClick(StatusViewHolder holder, int position);

        void onUserProfileClick(StatusViewHolder holder, int position);
    }

    public static final class DummyStatusHolderAdapter implements IStatusesAdapter<Object> {

        private final Context context;
        private final SharedPreferencesWrapper preferences;
        private final MediaLoaderWrapper loader;
        private final ImageLoadingHandler handler;
        private final AsyncTwitterWrapper twitter;
        private final FiretweetLinkify linkify;
        private int profileImageStyle;
        private int mediaPreviewStyle;
        private int textSize;
        private int linkHighlightStyle;
        private boolean nameFirst;
        private boolean displayProfileImage;
        private boolean sensitiveContentEnabled;
        private boolean hideCardActions;
        private boolean displayMediaPreview;

        public DummyStatusHolderAdapter(Context context) {
            this.context = context;
            preferences = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            final FiretweetApplication app = FiretweetApplication.getInstance(context);
            loader = app.getMediaLoaderWrapper();
            handler = new ImageLoadingHandler(R.id.media_preview_progress);
            twitter = app.getTwitterWrapper();
            linkify = new FiretweetLinkify(null);
            updateOptions();
        }

        @Override
        public MediaLoaderWrapper getImageLoader() {
            return loader;
        }

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public ImageLoadingHandler getImageLoadingHandler() {
            return handler;
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public int getProfileImageStyle() {
            return profileImageStyle;
        }

        @Override
        public int getMediaPreviewStyle() {
            return mediaPreviewStyle;
        }

        @Override
        public AsyncTwitterWrapper getTwitterWrapper() {
            return twitter;
        }

        @Override
        public float getTextSize() {
            return textSize;
        }

        @Override
        public boolean isLoadMoreIndicatorVisible() {
            return false;
        }

        @Override
        public boolean isLoadMoreSupported() {
            return false;
        }

        @Override
        public void setLoadMoreSupported(boolean supported) {

        }

        @Override
        public void setLoadMoreIndicatorVisible(boolean enabled) {

        }

        @Override
        public ParcelableStatus getStatus(int position) {
            return null;
        }

        @Override
        public int getStatusesCount() {
            return 0;
        }

        @Override
        public long getStatusId(int position) {
            return 0;
        }

        @Override
        public FiretweetLinkify getFiretweetLinkify() {
            return linkify;
        }

        @Override
        public boolean isMediaPreviewEnabled() {
            return displayMediaPreview;
        }

        @Override
        public int getLinkHighlightingStyle() {
            return linkHighlightStyle;
        }

        @Override
        public boolean isNameFirst() {
            return nameFirst;
        }

        @Override
        public boolean isSensitiveContentEnabled() {
            return sensitiveContentEnabled;
        }

        @Override
        public boolean isCardActionsHidden() {
            return hideCardActions;
        }

        @Override
        public void setData(Object o) {

        }

        @Override
        public boolean shouldShowAccountsColor() {
            return false;
        }

        public void setMediaPreviewEnabled(boolean enabled) {
            displayMediaPreview = enabled;
        }

        @Override
        public boolean isGapItem(int position) {
            return false;
        }

        @Override
        public void onGapClick(ViewHolder holder, int position) {

        }

        @Override
        public boolean isProfileImageEnabled() {
            return displayProfileImage;
        }

        @Override
        public void onStatusClick(StatusViewHolder holder, int position) {

        }

        @Override
        public void onMediaClick(StatusViewHolder holder, ParcelableMedia media, int position) {

        }

        @Override
        public void onUserProfileClick(StatusViewHolder holder, int position) {

        }

        @Override
        public void onItemActionClick(ViewHolder holder, int id, int position) {

        }

        @Override
        public void onItemMenuClick(ViewHolder holder, View menuView, int position) {

        }

        public void updateOptions() {
            profileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
            mediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
            textSize = preferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
            nameFirst = preferences.getBoolean(KEY_NAME_FIRST, true);
            displayProfileImage = preferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
            displayMediaPreview = preferences.getBoolean(KEY_MEDIA_PREVIEW, false);
            sensitiveContentEnabled = preferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false);
            hideCardActions = preferences.getBoolean(KEY_HIDE_CARD_ACTIONS, false);
            linkHighlightStyle = Utils.getLinkHighlightingStyleInt(preferences.getString(KEY_LINK_HIGHLIGHT_OPTION, null));
        }
    }
}
