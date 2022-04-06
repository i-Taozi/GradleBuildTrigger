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
package org.lanternpowered.server.data.io.store.data;

import static org.lanternpowered.server.data.DataHelper.getOrCreateView;

import com.google.common.reflect.TypeToken;
import org.lanternpowered.api.catalog.CatalogKeys;
import org.lanternpowered.server.data.DataHelper;
import org.lanternpowered.server.data.DataQueries;
import org.lanternpowered.server.data.IValueContainer;
import org.lanternpowered.server.data.KeyRegistration;
import org.lanternpowered.server.data.element.Element;
import org.lanternpowered.server.data.io.store.ObjectStore;
import org.lanternpowered.server.data.io.store.SimpleValueContainer;
import org.lanternpowered.server.data.persistence.DataTypeSerializer;
import org.lanternpowered.server.data.persistence.DataTypeSerializerContext;
import org.lanternpowered.server.game.Lantern;
import org.lanternpowered.server.game.registry.type.data.DataSerializerRegistry;
import org.lanternpowered.server.game.registry.type.data.KeyRegistryModule;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.CompositeValueStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CompositeValueContainerStore<T extends S, S extends CompositeValueStore<S, H>, H extends ValueContainer<?>> implements ObjectStore<T> {

    private static final DataQuery VALUES = DataQuery.of("Values");

    @SuppressWarnings("unchecked")
    @Override
    public void deserialize(T object, DataView dataView) {
        if (object instanceof IValueContainer) {
            final IValueContainer<S> valueContainer = (IValueContainer<S>) object;
            final SimpleValueContainer simpleValueContainer = new SimpleValueContainer(new HashMap<>());

            deserializeValues(object, simpleValueContainer, dataView);
            final Optional<DataView> optDataView = dataView.getView(VALUES);
            if (optDataView.isPresent()) {
                final DataTypeSerializerContext context = DataSerializerRegistry.INSTANCE.getTypeSerializerContext();
                final DataView valuesView = optDataView.get();
                for (Map.Entry<DataQuery, Object> entry : valuesView.getValues(false).entrySet()) {
                    final Key key = KeyRegistryModule.get().get(CatalogKeys.resolve(entry.getKey().toString())).orElse(null);
                    if (key == null) {
                        Lantern.getLogger().warn("Unable to deserialize the data value with key: {} because it doesn't exist.",
                                entry.getKey().toString());
                    } else {
                        final TypeToken<?> typeToken = key.getElementToken();
                        final DataTypeSerializer dataTypeSerializer = DataSerializerRegistry.INSTANCE.getTypeSerializer(typeToken).orElse(null);
                        if (dataTypeSerializer == null) {
                            Lantern.getLogger().warn("Unable to deserialize the data key value: {}, "
                                    + "no supported deserializer exists.", key.getKey().toString());
                        } else {
                            if (simpleValueContainer.get(key).isPresent()) {
                                Lantern.getLogger().warn("Duplicate usage of the key {} for value container {}",
                                        key.getKey().toString(), object.getClass().getName());
                            } else {
                                simpleValueContainer.set(key, dataTypeSerializer.deserialize(typeToken, context, entry.getValue()));
                            }
                        }
                    }
                }
            }

            for (Map.Entry<Key<?>, Object> entry : simpleValueContainer.getValues().entrySet()) {
                final Element element = (Element) valueContainer.getValueCollection().getElement((Key) entry.getKey()).orElse(null);
                if (element != null) {
                    element.set(entry.getValue());
                } else {
                    Lantern.getLogger().debug("Attempted to offer a unsupported value with key \"{}\" to the object {}",
                            entry.getKey().toString(), object.toString());
                }
            }

            dataView.getView(DataQueries.SPONGE_DATA).ifPresent(view ->
                    DataHelper.deserializeRawContainerData(dataView, valueContainer, DataQueries.CUSTOM_MANIPULATORS));
        } else {
            // Not sure what to do
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(T object, DataView dataView) {
        if (object instanceof IValueContainer) {
            final IValueContainer<S> valueContainer = (IValueContainer<S>) object;
            final SimpleValueContainer simpleValueContainer = new SimpleValueContainer(new HashMap<>());

            for (KeyRegistration<?,?> registration : valueContainer.getValueCollection().getAll()) {
                if (!(registration instanceof Element)) {
                    continue;
                }
                final Key key = registration.getKey();
                final Object element = ((Element) registration).get();
                if (element != null) {
                    simpleValueContainer.set(key, element);
                }
            }

            // Serialize the values, all written values will be removed from
            // the simple value container
            serializeValues(object, simpleValueContainer, dataView);

            // Write the rest to the Values tag
            final Map<Key<?>, Object> values = simpleValueContainer.getValues();
            if (!values.isEmpty()) {
                final DataView valuesView = dataView.createView(VALUES);
                final DataTypeSerializerContext context = DataSerializerRegistry.INSTANCE.getTypeSerializerContext();
                for (Map.Entry<Key<?>, Object> entry : values.entrySet()) {
                    final TypeToken<?> typeToken = entry.getKey().getElementToken();
                    final DataTypeSerializer dataTypeSerializer = DataSerializerRegistry.INSTANCE.getTypeSerializer(typeToken).orElse(null);
                    if (dataTypeSerializer == null) {
                        Lantern.getLogger().warn("Unable to serialize the data key value: " + entry.getKey());
                    } else {
                        valuesView.set(DataQuery.of(entry.getKey().getKey().toString()),
                                dataTypeSerializer.serialize(typeToken, context, entry.getValue()));
                    }
                }
                if (valuesView.isEmpty()) {
                    dataView.remove(VALUES);
                }
            }

            DataHelper.serializeRawContainerData(getOrCreateView(dataView, DataQueries.SPONGE_DATA),
                    valueContainer, DataQueries.CUSTOM_MANIPULATORS);
        } else {
            // Not sure what to do
        }
    }

    /**
     * Serializes all the values of the {@link SimpleValueContainer} and puts
     * them into the {@link DataView}.
     *
     * @param valueContainer The value container
     * @param dataView The data view
     */
    public void serializeValues(T object, SimpleValueContainer valueContainer, DataView dataView) {
    }

    /**
     * Deserializers all the values from the {@link DataView}
     * into the {@link SimpleValueContainer}.
     *
     * @param valueContainer The value container
     * @param dataView The data view
     */
    public void deserializeValues(T object, SimpleValueContainer valueContainer, DataView dataView) {
    }
}
