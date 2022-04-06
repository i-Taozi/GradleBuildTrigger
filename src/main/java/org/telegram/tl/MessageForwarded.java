/*
 *     This file is part of Telegram Server
 *     Copyright (C) 2015  Aykut Alparslan KOÇ
 *
 *     Telegram Server is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Telegram Server is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.telegram.tl;

import org.telegram.mtproto.ProtocolBuffer;
import org.telegram.tl.*;

public class MessageForwarded extends TLMessage {

    public static final int ID = -1553471722;

    public int flags;
    public int id;
    public int fwd_from_id;
    public int fwd_date;
    public int from_id;
    public TLPeer to_id;
    public int date;
    public String message;
    public TLMessageMedia media;

    public MessageForwarded() {
    }

    public MessageForwarded(int flags, int id, int fwd_from_id, int fwd_date, int from_id, TLPeer to_id, int date, String message, TLMessageMedia media){
        this.flags = flags;
        this.id = id;
        this.fwd_from_id = fwd_from_id;
        this.fwd_date = fwd_date;
        this.from_id = from_id;
        this.to_id = to_id;
        this.date = date;
        this.message = message;
        this.media = media;
    }

    @Override
    public void deserialize(ProtocolBuffer buffer) {
        flags = buffer.readInt();
        id = buffer.readInt();
        fwd_from_id = buffer.readInt();
        fwd_date = buffer.readInt();
        from_id = buffer.readInt();
        to_id = (TLPeer) buffer.readTLObject(APIContext.getInstance());
        date = buffer.readInt();
        message = buffer.readString();
        media = (TLMessageMedia) buffer.readTLObject(APIContext.getInstance());
    }

    @Override
    public ProtocolBuffer serialize() {
        ProtocolBuffer buffer = new ProtocolBuffer(32);
        serializeTo(buffer);
        return buffer;
    }

    @Override
    public void serializeTo(ProtocolBuffer buff) {
        buff.writeInt(getConstructor());
        buff.writeInt(flags);
        buff.writeInt(id);
        buff.writeInt(fwd_from_id);
        buff.writeInt(fwd_date);
        buff.writeInt(from_id);
        buff.writeTLObject(to_id);
        buff.writeInt(date);
        buff.writeString(message);
        buff.writeTLObject(media);
    }

    public int getConstructor() {
        return ID;
    }
}