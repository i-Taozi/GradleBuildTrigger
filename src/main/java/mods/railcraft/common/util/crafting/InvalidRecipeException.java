/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2020
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.util.crafting;

import mods.railcraft.common.util.misc.RailcraftException;

/**
 * Created by CovertJaguar on 4/7/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class InvalidRecipeException extends RailcraftException {
    public InvalidRecipeException(String messagePattern, Object... arguments) {
        super(messagePattern, arguments);
    }
}
