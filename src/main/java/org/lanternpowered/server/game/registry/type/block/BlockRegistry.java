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
package org.lanternpowered.server.game.registry.type.block;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.registry.CatalogRegistryModule;

import java.util.Optional;
import java.util.function.Function;

public interface BlockRegistry extends CatalogRegistryModule<BlockType> {

    /**
     * Gets the amount of different block states that are registered.
     *
     * @return the count
     */
    int getBlockStatesCount();

    /**
     * Registers a new catalog type in the registry with a predefined internal id.
     * 
     * @param internalId the internal id
     * @param blockType the block type
     * @param stateToDataConverter the block state to data value converter
     */
    void register(int internalId, BlockType blockType, BlockState2DataFunction stateToDataConverter);

    /**
     * Registers a new catalog type in the registry with a predefined internal id.
     * 
     * <p>This method should only be used for block types that have
     * only one block state. (No attached block traits.) If this is the case,
     * use the {@link #register(BlockType, Function)} method.</p>
     * 
     * @param internalId the internal id
     * @param blockType the block type
     */
    void register(int internalId, BlockType blockType);

    /**
     * Registers a new catalog type in the registry.
     * 
     * <p>This method should only be used for custom block types that have
     * only one block state. (No attached block traits.) If this is the case,
     * use the {@link #register(BlockType, Function)} method</p>
     *
     * @param blockType the block type
     */
    <A extends BlockType> A register(A blockType);

    /**
     * Registers a new catalog type in the registry.
     * 
     * @param blockType the block type
     * @param stateToDataConverter the block state to data value converter
     */
    void register(BlockType blockType, BlockState2DataFunction stateToDataConverter);

    /**
     * Gets the block state by using it's internal id.
     * 
     * @param internalId the internal id
     * @return the block state
     */
    Optional<BlockState> getStateByInternalId(int internalId);

    /**
     * Gets the block state by using it's internal id and data value.
     *
     * @param internalId the internal id
     * @param data the data value
     * @return the block state
     */
    Optional<BlockState> getStateByInternalIdAndData(int internalId, byte data);

    /**
     * Gets the block state by using it's internal id and data value.
     *
     * @param blockType the block type
     * @param data the data value
     * @return the block state
     */
    Optional<BlockState> getStateByTypeAndData(BlockType blockType, byte data);

    /**
     * Gets the block state by using it's internal id and data value.
     *
     * @param internalIdAndData the packed version of the internal id and data
     * @return the block state
     */
    Optional<BlockState> getStateByInternalIdAndData(int internalIdAndData);

    /**
     * Gets the data value of the specified block state.
     *
     * @param blockState the block state
     * @return the data value
     */
    byte getStateData(BlockState blockState);

    /**
     * Gets the internal id of the specified block state.
     *
     * @param blockState the block state
     * @return the internal id
     */
    short getStateInternalId(BlockState blockState);

    /**
     * Gets the packed version of the internal id and data for the
     * specified block state.
     *
     * @param blockState the block state
     * @return internalIdAndData
     */
    short getStateInternalIdAndData(BlockState blockState);

    /**
     * Gets the packed version of the specified internal id and data.
     *
     * @param internalId the internal id
     * @param data the data value
     * @return internalIdAndData
     */
    int getPackedVersion(int internalId, byte data);

}
