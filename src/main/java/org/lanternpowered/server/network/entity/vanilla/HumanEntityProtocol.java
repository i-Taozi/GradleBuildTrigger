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
package org.lanternpowered.server.network.entity.vanilla;

import org.lanternpowered.server.entity.LanternEntity;
import org.lanternpowered.server.network.entity.EntityProtocolUpdateContext;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutTabListEntries;
import org.lanternpowered.server.profile.LanternGameProfile;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;

import java.util.Collections;
import java.util.Objects;

public class HumanEntityProtocol extends HumanoidEntityProtocol<LanternEntity> {

    private String lastName;

    public HumanEntityProtocol(LanternEntity entity) {
        super(entity);
    }

    @Override
    protected void spawn(EntityProtocolUpdateContext context) {
        spawn(context, this.entity.getTranslation().get());
    }

    private void spawn(EntityProtocolUpdateContext context, String name) {
        final LanternGameProfile gameProfile = new LanternGameProfile(this.entity.getUniqueId(), name);
        final MessagePlayOutTabListEntries.Entry.Add addEntry = new MessagePlayOutTabListEntries.Entry.Add(
                gameProfile, GameModes.SURVIVAL, null, 0);
        context.sendToAllExceptSelf(() -> new MessagePlayOutTabListEntries(Collections.singleton(addEntry)));
        super.spawn(context);
        final MessagePlayOutTabListEntries.Entry.Remove removeEntry = new MessagePlayOutTabListEntries.Entry.Remove(gameProfile);
        context.sendToAllExceptSelf(() -> new MessagePlayOutTabListEntries(Collections.singleton(removeEntry)));
    }

    @Override
    protected void update(EntityProtocolUpdateContext context) {
        final String name = this.entity.getTranslation().get();
        if (!Objects.equals(this.lastName, name)) {
            spawn(context, name);
            update0(EntityProtocolUpdateContext.empty());
            this.lastName = name;
        } else {
            update0(context);
        }
    }

    protected void update0(EntityProtocolUpdateContext context) {
        super.update(context);
    }
}
