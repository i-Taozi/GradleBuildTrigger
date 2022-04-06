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

package org.telegram.tl.L57;

import org.telegram.mtproto.ProtocolBuffer;
import org.telegram.tl.TLObject;
import org.telegram.tl.TLVector;
import org.telegram.tl.APIContext;
import org.telegram.tl.L57.*;

public class UpdateUserPhoto extends org.telegram.tl.TLUpdate {

    public static final int ID = 0x95313b0c;

    public int user_id;
    public int date;
    public org.telegram.tl.TLUserProfilePhoto photo;
    public boolean previous;

    public UpdateUserPhoto() {
    }

    public UpdateUserPhoto(int user_id, int date, org.telegram.tl.TLUserProfilePhoto photo, boolean previous) {
        this.user_id = user_id;
        this.date = date;
        this.photo = photo;
        this.previous = previous;
    }

    @Override
    public void deserialize(ProtocolBuffer buffer) {
        user_id = buffer.readInt();
        date = buffer.readInt();
        photo = (org.telegram.tl.TLUserProfilePhoto) buffer.readTLObject(APIContext.getInstance());
        previous = buffer.readBool();
    }

    @Override
    public ProtocolBuffer serialize() {
        ProtocolBuffer buffer = new ProtocolBuffer(21);
        serializeTo(buffer);
        return buffer;
    }


    @Override
    public void serializeTo(ProtocolBuffer buff) {
        buff.writeInt(getConstructor());
        buff.writeInt(user_id);
        buff.writeInt(date);
        buff.writeTLObject(photo);
        buff.writeBool(previous);
    }


    public int getConstructor() {
        return ID;
    }
}