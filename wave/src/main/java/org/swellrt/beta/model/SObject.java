package org.swellrt.beta.model;

import org.swellrt.beta.client.rest.ServiceOperation;
import org.swellrt.beta.model.presence.SPresenceEvent;
import org.swellrt.beta.model.presence.SSession;
import org.waveprotocol.wave.model.wave.InvalidParticipantAddress;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(namespace = "swell", name = "Object")
public interface SObject extends SMap, ServiceOperation.Response {

  @JsFunction
  public interface StatusHandler {
    void exec(SStatusEvent e);
  }

  /**
   * @return the global id of this object. Null for local objects.
   */
  @JsProperty
  public String getId();

  /**
   * Adds a participant.
   * @param participantId
   * @throws InvalidParticipantAddress
   */
  public void addParticipant(String participantId) throws InvalidParticipantAddress;


  /**
   * Removes a participant.
   * @param participantId
   * @throws InvalidParticipantAddress
   */
  public void removeParticipant(String participantId) throws InvalidParticipantAddress;

  /**
   * @return array of all participants with access to the object currently. They
   *         might not get connected to the object yet.
   */
  public String[] getParticipants();

  /**
   * @return array with the last session of any participant who had access to
   *         the object. Use to provide a list of effective participants with
   *         its last visit time.
   */
  public SSession[] getSessions();


  /** Make this object to be public to any user */
  public void setPublic(boolean isPublic);

  /** Check if the object is public */
  public boolean isPublic();

  /** Get creator id */
  public String getCreatorId();

  /** @return root map of the user's private area in this object */
  public SMap getUserStore();

  /** @return root map of the transient object's store */
  public SMap getTransientStore();

  /**
   * Register a status handler for this object.
   *
   * @param h
   */
  public void setStatusHandler(StatusHandler h);

  /** @return wave wavelets supporting this object */
  public String[] _getWavelets();

  /**
   * @return wave document ids of this object
   */
  public String[] _getDocuments(String waveletId);


  /** @return document raw content */
  public String _getContent(String waveletId, String docId);

  /**
   * Register a handler for presence events. Pass null value to unset.
   */
  public void setPresenceHandler(SPresenceEvent.Handler h);

  /** start/stops the presence tracking */
  void trackPresence(boolean enable);

  /** Inform others we are online or offline */
  void setPresence(boolean online);

  /** Get the version manager to tag node versions */
  public SVersionManager getVersionManager();
}
