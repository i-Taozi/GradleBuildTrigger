/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.sdk.controllers.conversation.messages.content;

import android.view.View;

import im.actor.core.entity.Peer;
import im.actor.sdk.R;
import im.actor.core.entity.Message;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.content.preprocessor.PreprocessedData;

public class UnsupportedHolder extends TextHolder {

    protected String text;

    public UnsupportedHolder(MessagesAdapter fragmeadaptert, View itemView, Peer peer) {
        super(fragmeadaptert, itemView, peer);

        text = fragmeadaptert.getMessagesFragment().getResources().getString(R.string.chat_unsupported);
        onConfigureViewHolder();
    }

    @Override
    protected void bindData(Message message, long readDate, long receiveDate, boolean isUpdated, PreprocessedData preprocessedData) {
        bindRawText(text, readDate, receiveDate, null, message, true);
    }
}
