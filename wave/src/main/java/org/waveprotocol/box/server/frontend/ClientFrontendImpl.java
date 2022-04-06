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

package org.waveprotocol.box.server.frontend;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.waveprotocol.box.common.DeltaSequence;
import org.waveprotocol.box.common.comms.WaveClientRpc;
import org.waveprotocol.box.server.waveserver.WaveBus;
import org.waveprotocol.box.server.waveserver.WaveServerException;
import org.waveprotocol.box.server.waveserver.WaveletProvider;
import org.waveprotocol.box.server.waveserver.WaveletProvider.SubmitRequestListener;
import org.waveprotocol.wave.concurrencycontrol.common.ChannelException;
import org.waveprotocol.wave.concurrencycontrol.common.Recoverable;
import org.waveprotocol.wave.concurrencycontrol.common.ResponseCode;
import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
import org.waveprotocol.wave.model.id.IdFilter;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;
import org.waveprotocol.wave.model.id.WaveletName;
import org.waveprotocol.wave.model.operation.wave.AddParticipant;
import org.waveprotocol.wave.model.operation.wave.RemoveParticipant;
import org.waveprotocol.wave.model.operation.wave.TransformedWaveletDelta;
import org.waveprotocol.wave.model.operation.wave.WaveletOperation;
import org.waveprotocol.wave.model.version.HashedVersion;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.ParticipantIdUtil;
import org.waveprotocol.wave.model.wave.data.ReadableWaveletData;
import org.waveprotocol.wave.util.logging.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;


/**
 * Implements {@link ClientFrontend}.
 *
 * When a wavelet is added and it's not at version 0, buffer updates until a
 * request for the wavelet's history has completed.
 */
public class ClientFrontendImpl implements ClientFrontend, WaveBus.Subscriber {
  private static final Log LOG = Log.get(ClientFrontendImpl.class);

  private final static AtomicInteger channel_counter = new AtomicInteger(0);

  private final WaveletProvider waveletProvider;
  private final WaveletInfo waveletInfo;
  private final String waveDomain;

  /**
   * The legacy "@domain.com" meta participant. It grants access for anyone
   * registered but non anonymous accounts.
   */
  private final ParticipantId anyoneRegistered;

  /**
   * The swell ";@domain.com" meta participant. It grants access for anyone
   * including anonymous accounts.
   */
  private final ParticipantId anyoneUniversal;

  /**
   * Creates a client frontend and subscribes it to the wave bus.
   *
   * @throws WaveServerException if the server fails during initialization.
   */
  public static ClientFrontendImpl create(WaveletProvider waveletProvider, WaveBus wavebus,
      WaveletInfo waveletInfo, String waveDomain) throws WaveServerException {

    ClientFrontendImpl impl =
        new ClientFrontendImpl(waveletProvider, waveletInfo, waveDomain);

    wavebus.subscribe(impl);
    return impl;
  }

  /**
   * Constructor.
   *
   * @param waveletProvider
   * @param waveDomain the server wave domain. It is assumed that the wave domain is valid.
   */
  @VisibleForTesting
  ClientFrontendImpl(WaveletProvider waveletProvider, WaveletInfo waveletInfo, String waveDomain) {
    this.waveletProvider = waveletProvider;
    this.waveletInfo = waveletInfo;
    this.waveDomain = waveDomain;
    this.anyoneRegistered = ParticipantIdUtil.makeAnyoneRegistered(waveDomain);
    this.anyoneUniversal = ParticipantIdUtil.makeAnyoneUniversal(waveDomain);
  }

  @Override
  public void openRequest(ParticipantId loggedInUser, WaveId waveId, IdFilter waveletIdFilter,
      Collection<WaveClientRpc.WaveletVersion> knownWavelets, OpenListener openListener) {
    LOG.info("received openRequest from " + loggedInUser + " for " + waveId + ", filter "
        + waveletIdFilter + ", known wavelets: " + knownWavelets);

    // Users must be logged in always, but they might be anonymous.
    if (loggedInUser == null) {
      openListener.onFailure(new ChannelException(ResponseCode.NOT_LOGGED_IN, "Not Logged in", null, Recoverable.NOT_RECOVERABLE, waveId, null));
      return;
    }

    if (!knownWavelets.isEmpty()) {
      openListener.onFailure(new ChannelException(ResponseCode.INTERNAL_ERROR, "Known wavelets not supported", null, Recoverable.NOT_RECOVERABLE, waveId, null));
      return;
    }

    boolean isNewWave = false;
    try {
      isNewWave = waveletInfo.initialiseWave(waveId);
    } catch (WaveServerException e) {
      LOG.severe("Wave server failed lookup for " + waveId, e);
      openListener.onFailure(new ChannelException(ResponseCode.WAVE_RETRIEVAL_ERROR, "Wave server failed to look up wave", null, Recoverable.NOT_RECOVERABLE, waveId, null));
      return;
    }

    String channelId = generateChannelID();
    UserManager userManager = waveletInfo.getUserManager(loggedInUser);
    WaveViewSubscription subscription =
        userManager.subscribe(waveId, waveletIdFilter, channelId, openListener);
    LOG.info("Subscribed " + loggedInUser + " to " + waveId + " channel " + channelId);

    Set<WaveletId> waveletIds;
    try {
      waveletIds = waveletInfo.visibleWaveletsFor(subscription, loggedInUser);
    } catch (WaveServerException e1) {
      waveletIds = Sets.newHashSet();
      LOG.warning("Failed to retrieve visible wavelets for " + loggedInUser, e1);
    }

    for (WaveletId waveletId : waveletIds) {
      WaveletName waveletName = WaveletName.of(waveId, waveletId);
      // Ensure that implicit participants will also receive updates.
      // TODO (Yuri Z.) If authorizing participant was removed from the wave
      // (the shared domain participant), then all implicit participant that
      // were authorized should be unsubsrcibed.
      waveletInfo.notifyAddedImplcitParticipant(waveletName, loggedInUser);
      // The WaveletName by which the waveletProvider knows the relevant deltas

      // TODO(anorth): if the client provides known wavelets, calculate
      // where to start sending deltas from.


      CommittedWaveletSnapshot snapshotToSend;

      // Send a snapshot of the current state.
      // TODO(anorth): calculate resync point if the client already knows
      // a snapshot.
      try {
        snapshotToSend = waveletProvider.getSnapshot(waveletName);
      } catch (WaveServerException e) {
        LOG.warning("Failed to retrieve snapshot for wavelet " + waveletName, e);
        openListener.onFailure(new ChannelException(ResponseCode.WAVELET_RETRIEVAL_ERROR, "Wave server failure retrieving wavelet", null, Recoverable.NOT_RECOVERABLE, waveId, waveletId));
        return;
      }

      LOG.info("snapshot in response is: " + (snapshotToSend != null));
      if (snapshotToSend == null) {
        // Send deltas.
        openListener.onUpdate(waveletName, snapshotToSend, DeltaSequence.empty(), null, null,
            channelId);
      } else {
        // Send the snapshot.
        openListener.onUpdate(waveletName, snapshotToSend, DeltaSequence.empty(),
            snapshotToSend.committedVersion, null, channelId);
      }
    }

    WaveletName dummyWaveletName = createDummyWaveletName(waveId);

    if (waveletIds.size() == 0) {
      // Send message with just the channel id.
      LOG.info("sending just a channel id for " + dummyWaveletName);
      openListener.onUpdate(dummyWaveletName, null, DeltaSequence.empty(), null, null, channelId);
    }

    LOG.info("sending marker for " + dummyWaveletName);
    openListener.onUpdate(dummyWaveletName, null, DeltaSequence.empty(), null, true, null);

    // After all, if user can't see any wavelet, response an error
    // A user should be able to access at least the master wavelet
    // The condition is place here, after updates for dummyWavelet and marker
    // to keep protocol as compatible as possible

    if (!isNewWave && waveletIds.isEmpty()) {
      LOG.warning("No visible wavelets for " + loggedInUser + ", response failure");
      openListener.onFailure(new ChannelException(ResponseCode.NOT_AUTHORIZED, "No visible wavelets", null, Recoverable.NOT_RECOVERABLE, waveId, null));
        return;
    }
  }

  private String generateChannelID() {
    return "ch" + channel_counter.addAndGet(1);
  }

  @Override
  public void submitRequest(ParticipantId loggedInUser, final WaveletName waveletName,
      final ProtocolWaveletDelta delta, final String channelId,
      final SubmitRequestListener listener) {
    final ParticipantId author = new ParticipantId(delta.getAuthor());

    if (!author.equals(loggedInUser)) {
      listener.onFailure("Author field on delta must match logged in user");
      return;
    }

    waveletInfo.getUserManager(author).submitRequest(channelId, waveletName);
    waveletProvider.submitRequest(waveletName, delta, new SubmitRequestListener() {
      @Override
      public void onSuccess(int operationsApplied,
          HashedVersion hashedVersionAfterApplication, long applicationTimestamp) {
        listener.onSuccess(operationsApplied, hashedVersionAfterApplication,
            applicationTimestamp);
        waveletInfo.getUserManager(author).submitResponse(channelId, waveletName,
            hashedVersionAfterApplication);
      }

      @Override
      public void onFailure(String error) {
        listener.onFailure(error);
        // (pablojan) this always throws a npe in the submitResponse precondition
        //waveletInfo.getUserManager(author).submitResponse(channelId, waveletName, null);
      }
    });
  }

  @Override
  public void waveletCommitted(WaveletName waveletName, HashedVersion version) {
    for (ParticipantId participant : waveletInfo.getWaveletParticipants(waveletName)) {
      waveletInfo.getUserManager(participant).onCommit(waveletName, version);
    }
  }

  /**
   * Sends new deltas to a particular user on a particular wavelet.
   * Updates the participants of the specified wavelet if the participant was added or removed.
   *
   * @param waveletName the waveletName which the deltas belong to.
   * @param participant on the wavelet.
   * @param newDeltas newly arrived deltas of relevance for participant. Must
   *        not be empty.
   * @param add whether the participant is added by the first delta.
   * @param remove whether the participant is removed by the last delta.
   */
  private void participantUpdate(WaveletName waveletName, ParticipantId participant,
      DeltaSequence newDeltas, boolean add, boolean remove) {
    if(LOG.isFineLoggable()) {
      LOG.fine("Sending deltas to " + participant + " for " + waveletName + ":"
          + newDeltas.getEndVersion().getVersion());
    }
    if (add) {
      waveletInfo.notifyAddedExplicitWaveletParticipant(waveletName, participant);
    }
    waveletInfo.getUserManager(participant).onUpdate(waveletName, newDeltas);
    if (remove) {
      waveletInfo.notifyRemovedExplicitWaveletParticipant(waveletName, participant);
    }
  }

  /**
   * Tracks wavelet versions and ensures that the deltas are contiguous. Updates
   * wavelet subscribers with new new deltas.
   */
  @Override
  public void waveletUpdate(ReadableWaveletData wavelet, DeltaSequence newDeltas) {
    if (newDeltas.isEmpty()) {
      return;
    }

    WaveletName waveletName = WaveletName.of(wavelet.getWaveId(), wavelet.getWaveletId());

    if(waveletInfo.getCurrentWaveletVersion(waveletName).getVersion() == 0 && LOG.isWarningLoggable()) {
      LOG.warning("Wavelet " + waveletName.toString()
          + " does not appear to have been initialized by client. Continuing anyway.");
    }

    waveletInfo.syncWaveletVersion(waveletName, newDeltas);

    Set<ParticipantId> remainingparticipants =
        Sets.newHashSet(waveletInfo.getWaveletParticipants(waveletName));
    Set<ParticipantId> deletedparticipants = Sets.newHashSet();
    // Participants added during the course of newDeltas.
    Set<ParticipantId> newParticipants = Sets.newHashSet();
    for (int i = 0; i < newDeltas.size(); i++) {
      TransformedWaveletDelta delta = newDeltas.get(i);
      // Participants added or removed in this delta get the whole delta.
      for (WaveletOperation op : delta) {
        if (op instanceof AddParticipant) {
          ParticipantId p = ((AddParticipant) op).getParticipantId();
          remainingparticipants.add(p);
          newParticipants.add(p);
        }
        if (op instanceof RemoveParticipant) {
          ParticipantId p = ((RemoveParticipant) op).getParticipantId();
          remainingparticipants.remove(p);
          deletedparticipants.add(p);
          participantUpdate(waveletName, p, newDeltas.subList(0, i + 1), newParticipants.remove(p),
              true);
        }
      }
    }

    // Send out deltas to those who end up being participants at the end
    // (either because they already were, or because they were added).
    for (ParticipantId p : remainingparticipants) {
      boolean isNew = newParticipants.contains(p);
      participantUpdate(waveletName, p, newDeltas, isNew, false);
    }

    // If the wavelet is still public, send delta updates to implicit participants
    boolean publicToRegistered = remainingparticipants.contains(anyoneRegistered);
    boolean publicToAnyone = remainingparticipants.contains(anyoneUniversal);

    if (publicToAnyone || publicToRegistered) {

      Set<ParticipantId> explicitparticipants = waveletInfo.getWaveletParticipants(waveletName);
      Set<ParticipantId> implicitparticipants = waveletInfo
          .getImplicitWaveletParticipants(waveletName);

      for (ParticipantId p : implicitparticipants) {
        // If an implicit participant become explicit,
        // no need to update it again
        // We keep it as implicit in case it is
        // removed and the wavelet is still public
        if ((!explicitparticipants.contains(p) && !deletedparticipants.contains(p))
            && (publicToAnyone || (publicToRegistered && p.isRegistered())))
          participantUpdate(waveletName, p, newDeltas, false, false);
      }

    }

  }

  @VisibleForTesting
  static WaveletName createDummyWaveletName(WaveId waveId) {
    final WaveletName dummyWaveletName =
      WaveletName.of(waveId, WaveletId.of(waveId.getDomain(), "dummy+root"));
    return dummyWaveletName;
  }
}
