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

package org.telegram.tl.L57.messages;

import org.telegram.mtproto.ProtocolBuffer;
import org.telegram.tl.TLObject;
import org.telegram.tl.TLVector;
import org.telegram.tl.APIContext;
import org.telegram.tl.L57.*;

public class Search extends TLObject {

    public static final int ID = 0xd4569248;

    public int flags;
    public org.telegram.tl.TLInputPeer peer;
    public String q;
    public org.telegram.tl.TLMessagesFilter filter;
    public int min_date;
    public int max_date;
    public int offset;
    public int max_id;
    public int limit;

    public Search() {
    }

    public Search(int flags, org.telegram.tl.TLInputPeer peer, String q, org.telegram.tl.TLMessagesFilter filter, int min_date, int max_date, int offset, int max_id, int limit) {
        this.flags = flags;
        this.peer = peer;
        this.q = q;
        this.filter = filter;
        this.min_date = min_date;
        this.max_date = max_date;
        this.offset = offset;
        this.max_id = max_id;
        this.limit = limit;
    }

    @Override
    public void deserialize(ProtocolBuffer buffer) {
        flags = buffer.readInt();
        peer = (org.telegram.tl.TLInputPeer) buffer.readTLObject(APIContext.getInstance());
        q = buffer.readString();
        filter = (org.telegram.tl.TLMessagesFilter) buffer.readTLObject(APIContext.getInstance());
        min_date = buffer.readInt();
        max_date = buffer.readInt();
        offset = buffer.readInt();
        max_id = buffer.readInt();
        limit = buffer.readInt();
    }

    @Override
    public ProtocolBuffer serialize() {
        ProtocolBuffer buffer = new ProtocolBuffer(52);
        serializeTo(buffer);
        return buffer;
    }


    @Override
    public void serializeTo(ProtocolBuffer buff) {
        buff.writeInt(getConstructor());
        buff.writeInt(flags);
        buff.writeTLObject(peer);
        buff.writeString(q);
        buff.writeTLObject(filter);
        buff.writeInt(min_date);
        buff.writeInt(max_date);
        buff.writeInt(offset);
        buff.writeInt(max_id);
        buff.writeInt(limit);
    }


    public int getConstructor() {
        return ID;
    }
}