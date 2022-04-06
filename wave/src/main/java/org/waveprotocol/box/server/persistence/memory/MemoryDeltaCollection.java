/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.waveprotocol.box.server.persistence.memory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.waveprotocol.box.common.Receiver;
import org.waveprotocol.box.server.persistence.PersistenceException;
import org.waveprotocol.box.server.swell.WaveletContributions;
import org.waveprotocol.box.server.waveserver.ByteStringMessage;
import org.waveprotocol.box.server.waveserver.DeltaStore.DeltasAccess;
import org.waveprotocol.box.server.waveserver.DeltaStore.Snapshot;
import org.waveprotocol.box.server.waveserver.WaveletDeltaRecord;
import org.waveprotocol.wave.federation.Proto.ProtocolAppliedWaveletDelta;
import org.waveprotocol.wave.model.id.WaveletName;
import org.waveprotocol.wave.model.operation.wave.TransformedWaveletDelta;
import org.waveprotocol.wave.model.version.HashedVersion;
import org.waveprotocol.wave.model.wave.data.WaveletData;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * An in-memory implementation of DeltasAccess
 *
 * @author josephg@google.com (Joseph Gentle)
 */
public class MemoryDeltaCollection implements DeltasAccess {
  private final Map<Long, WaveletDeltaRecord> deltas = Maps.newHashMap();
  private final Map<Long, WaveletDeltaRecord> endDeltas = Maps.newHashMap();
  private final WaveletName waveletName;

  private HashedVersion endVersion = null;

  public MemoryDeltaCollection(WaveletName waveletName) {
    Preconditions.checkNotNull(waveletName);
    this.waveletName = waveletName;
  }

  @Override
  public boolean isEmpty() {
    return deltas.isEmpty();
  }

  @Override
  public WaveletName getWaveletName() {
    return waveletName;
  }

  @Override
  public HashedVersion getEndVersion() {
    return endVersion;
  }

  @Override
  public WaveletDeltaRecord getDelta(long version) {
    return deltas.get(version);
  }

  @Override
  public WaveletDeltaRecord getDeltaByEndVersion(long version) {
    return endDeltas.get(version);
  }

  @Override
  public HashedVersion getAppliedAtVersion(long version) throws InvalidProtocolBufferException {
    WaveletDeltaRecord delta = getDelta(version);
    return (delta != null) ? delta.getAppliedAtVersion() : null;
  }

  @Override
  public HashedVersion getResultingVersion(long version) {
    WaveletDeltaRecord delta = getDelta(version);
    return (delta != null) ? delta.getTransformedDelta().getResultingVersion() : null;
  }

  @Override
  public ByteStringMessage<ProtocolAppliedWaveletDelta> getAppliedDelta(long version) {
    WaveletDeltaRecord delta = getDelta(version);
    return (delta != null) ? delta.getAppliedDelta() : null;
  }

  @Override
  public TransformedWaveletDelta getTransformedDelta(long version) {
    WaveletDeltaRecord delta = getDelta(version);
    return (delta != null) ? delta.getTransformedDelta() : null;
  }

  @Override
  public void close() {
    // Does nothing.
  }

  @Override
  public void append(Collection<WaveletDeltaRecord> newDeltas) {
    for (WaveletDeltaRecord delta : newDeltas) {
      // Before:   ... |   D   |
      //            start     end
      // After:    ... |   D   |  D + 1 |
      //                     start     end
      long startVersion = delta.getTransformedDelta().getAppliedAtVersion();
      Preconditions.checkState(
          (startVersion == 0 && endVersion == null) ||
          (startVersion == endVersion.getVersion()));
      deltas.put(startVersion, delta);
      endVersion = delta.getTransformedDelta().getResultingVersion();
      endDeltas.put(endVersion.getVersion(), delta);
    }
  }

  @Override
  public long getAllDeltas(Receiver<WaveletDeltaRecord> receiver) throws IOException {
    for (WaveletDeltaRecord delta : deltas.values())
      receiver.put(delta);

    return deltas.size();
  }

  @Override
  public long getDeltasInRange(long startVersion, long endVersion,
      Receiver<WaveletDeltaRecord> receiver) throws IOException {

    Preconditions.checkState((startVersion >= 0 && startVersion < endVersion));

    long count = 0;
    long v = startVersion;
    while (v < endVersion) {
      WaveletDeltaRecord delta = deltas.get(v);
      if (delta != null) {
        if (!receiver.put(delta)) {
          break;
        }
        v = delta.getResultingVersion().getVersion();
        count++;
      } else {
        break;
      }
    }

    return count;
  }

  @Override
  public Snapshot loadSnapshot() throws PersistenceException {
    return null;
  }

  @Override
  public void storeSnapshot(WaveletData waveletData)
      throws PersistenceException {
    // No op
  }

  @Override
  public WaveletDeltaRecord getLastDelta() throws IOException {
    return endDeltas.get(endVersion.getVersion());
  }

  @Override
  public WaveletContributions loadContributions() throws PersistenceException {
    // Not supported!
    return null;
  }

  @Override
  public WaveletContributions loadContributionsForVersion(long version)
      throws PersistenceException {
    // Not supported!
    return null;
  }

  @Override
  public void storeContributions(WaveletContributions contributions) throws PersistenceException {
    // Not supported!
  }
}
