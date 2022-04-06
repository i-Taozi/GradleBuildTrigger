/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.server.config.user.ban;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public final class BanEntrySerializer implements TypeSerializer<BanEntry> {

    @Override
    public BanEntry deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        if (!value.getNode("ip").isVirtual()) {
            return value.getValue(TypeToken.of(BanEntry.Ip.class));
        } else {
            return value.getValue(TypeToken.of(BanEntry.Profile.class));
        }
    }

    @Override
    public void serialize(TypeToken<?> type, BanEntry obj, ConfigurationNode value) throws ObjectMappingException {
        if (obj instanceof BanEntry.Ip) {
            value.setValue(TypeToken.of(BanEntry.Ip.class), (BanEntry.Ip) obj);
        } else {
            value.setValue(TypeToken.of(BanEntry.Profile.class), (BanEntry.Profile) obj);
        }
    }

}
