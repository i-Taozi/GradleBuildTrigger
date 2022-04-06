package org.swellrt.beta.client;

import java.util.Collections;

import org.swellrt.beta.client.wave.RemoteViewServiceMultiplexer;
import org.swellrt.beta.client.wave.WaveDeps;
import org.swellrt.beta.client.wave.WaveLoader;
import org.swellrt.beta.common.ContextStatus;
import org.swellrt.beta.common.SException;
import org.swellrt.beta.model.SStatusEvent;
import org.swellrt.beta.model.presence.SSessionManager;
import org.swellrt.beta.model.wave.mutable.SWaveNodeManager;
import org.swellrt.beta.model.wave.mutable.SWaveObject;
import org.waveprotocol.wave.client.wave.DiffProvider;
import org.waveprotocol.wave.concurrencycontrol.common.ChannelException;
import org.waveprotocol.wave.concurrencycontrol.common.ResponseCode;
import org.waveprotocol.wave.concurrencycontrol.common.TurbulenceListener;
import org.waveprotocol.wave.concurrencycontrol.common.UnsavedDataListener;
import org.waveprotocol.wave.model.id.IdGenerator;
import org.waveprotocol.wave.model.id.ModernIdSerialiser;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.util.Preconditions;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.opbased.ObservableWaveView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gwt.user.client.Command;

/**
 * A class wrapping Wave components to be managed by the {@ServiceContext}.
 * Handles Wave life cycle and captures Channel Exceptions.
 *
 * @author pablojan@gmail.com (Pablo Ojanguren)
 *
 */
public class WaveContext implements UnsavedDataListener, TurbulenceListener, ContextStatus {

  private static final int INACTIVE = 0;
  private static final int ACTIVE = 1;
  private static final int ERROR = 2;

  private int state = INACTIVE;

  private final WaveId waveId;
  private final String waveDomain;
  private final SSessionManager session;
  private final ServiceStatus serviceStatus;

  private WaveLoader loader;
  private SettableFuture<ObservableWaveView> waveViewFuture;
  private SWaveObject sobject;
  private ChannelException lastException;


  private final DiffProvider diffProvider;


  public WaveContext(WaveId waveId, String waveDomain, SSessionManager session,
      ServiceStatus serviceStatus, DiffProvider diffProvider) {
    super();
    this.waveId = waveId;
    this.waveDomain = waveDomain;
    this.session = session;
    this.serviceStatus = serviceStatus;
    this.sobject = null;
    this.waveViewFuture = SettableFuture.<ObservableWaveView> create();
    this.diffProvider = diffProvider;
  }


  public void init(RemoteViewServiceMultiplexer viewServiceMultiplexer, IdGenerator idGenerator) {

    Preconditions.checkArgument(viewServiceMultiplexer != null,
        "Can't init Wave context with a null Remote Service Multiplexer");
    Preconditions.checkArgument(idGenerator != null,
        "Can't init Wave context with a null Id Generator");

    // Clean up listener on the channel multiplexer
    if (loader != null)
      loader.destroy();

    // Create a future for the object
    if (this.waveViewFuture == null || this.waveViewFuture.isDone())
      this.waveViewFuture = SettableFuture.<ObservableWaveView> create();

    // Load the wave and bind to the object
    state = ACTIVE;

    loader = WaveDeps.loaderFactory.create(waveId, viewServiceMultiplexer, idGenerator,
        waveDomain,
        Collections.<ParticipantId> emptySet(), session.get().getParticipantId(), this, this,
        this.diffProvider);

    try {

      loader.load(new Command() {

        @Override
        public void execute() {

          try {

            check(); // check for troubles in comms or async load.
            waveViewFuture.set(loader.getWave());

          } catch (SException ex) {
            waveViewFuture.setException(ex);
          }

        }
      });

    } catch (RuntimeException ex) {
      waveViewFuture.setException(ex);
    }

  }

  public void getSObject(FutureCallback<SWaveObject> callback) {

    Futures.addCallback(this.waveViewFuture, new FutureCallback<ObservableWaveView>() {

      @Override
      public void onSuccess(ObservableWaveView result) {

        if (WaveContext.this.sobject == null) {

          SWaveNodeManager nodeManager = SWaveNodeManager.create(session, loader.getIdGenerator(),
              loader.getLocalDomain(), loader.getWave(), WaveContext.this,
              loader.getDocumentRegistry());

          WaveContext.this.sobject = nodeManager.getSWaveObject();

        }

        callback.onSuccess(WaveContext.this.sobject);


      }

      @Override
      public void onFailure(Throwable t) {
        callback.onFailure(t);
      }

    });
  }

  public void getWave(FutureCallback<ObservableWaveView> callback) {
    Futures.addCallback(this.waveViewFuture, callback);
  }

  public void close() {
    if (this.sobject != null) {
      this.sobject.trackPresence(false);
    }

    onClose(false);

    if (loader != null)
      loader.destroy();

    this.state = INACTIVE;
  }

  @Override
  public void onFailure(ChannelException e) {
    this.lastException = e;
    this.state = ERROR;
    // If an exception occurs during stage loader (WaveLoader)
    // it will reach here. Check the future so.
    if (!this.waveViewFuture.isDone()) {
      this.waveViewFuture.setException(new SException(e));
    } else {
      // if the Swell object was retrieved, notify the exception
      if (this.sobject != null) {
        this.sobject.onStatusEvent(new SStatusEvent(
            ModernIdSerialiser.INSTANCE.serialiseWaveId(waveId), new SException(e)));
      }
    }
    serviceStatus.raise(ModernIdSerialiser.INSTANCE.serialiseWaveId(waveId), new SException(e));
    close();
  }

  @Override
  public void onUpdate(UnsavedDataInfo unsavedDataInfo) {
    if (this.sobject != null) {
      this.sobject.onStatusEvent(new SStatusEvent(
          ModernIdSerialiser.INSTANCE.serialiseWaveId(waveId), unsavedDataInfo.inFlightSize(),
          unsavedDataInfo.estimateUnacknowledgedSize(), unsavedDataInfo.estimateUncommittedSize(),
          unsavedDataInfo.laskAckVersion(), unsavedDataInfo.lastCommitVersion()));

    }
  }

  @Override
  public void onClose(boolean everythingCommitted) {
    if (this.sobject != null) {
      this.sobject.onStatusEvent(new SStatusEvent(
          ModernIdSerialiser.INSTANCE.serialiseWaveId(waveId), everythingCommitted));
    }
  }

  public boolean isError() {
    return this.state == ERROR;
  }

  public boolean isActive() {
    return this.state == ACTIVE;
  }

  @Override
  public void check() throws SException {

    serviceStatus.check();

    if (this.state == INACTIVE || this.state == ERROR) {
      if (lastException != null)
        throw new SException(lastException);
      else
        throw new SException(ResponseCode.UNKNOWN);
    }
  }

}
