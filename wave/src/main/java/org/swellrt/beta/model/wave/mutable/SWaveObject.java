package org.swellrt.beta.model.wave.mutable;

import org.swellrt.beta.client.ServiceConfig;
import org.swellrt.beta.common.SException;
import org.swellrt.beta.model.SList;
import org.swellrt.beta.model.SMap;
import org.swellrt.beta.model.SMutationHandler;
import org.swellrt.beta.model.SNode;
import org.swellrt.beta.model.SObject;
import org.swellrt.beta.model.SObservableNode;
import org.swellrt.beta.model.SPrimitive;
import org.swellrt.beta.model.SStatusEvent;
import org.swellrt.beta.model.SText;
import org.swellrt.beta.model.SVersionManager;
import org.swellrt.beta.model.SVisitor;
import org.swellrt.beta.model.js.Proxy;
import org.swellrt.beta.model.js.SMapProxyHandler;
import org.swellrt.beta.model.presence.SPresenceEvent;
import org.swellrt.beta.model.presence.SSession;
import org.waveprotocol.wave.model.wave.InvalidParticipantAddress;

import jsinterop.annotations.JsIgnore;


/**
 *
 * Main class providing object-based data model built on top of Wave data model.
 * <p>
 * A {@link SObject} represents a JSON-like data structure containing a nested
 * structure of {@link SMap}, {@link SList} and {@link SPrimitive} values.
 * <p>
 * The underlying implementation based on the Wave data model provides:
 * <ul>
 * <li>Real-time sync of SObject state from changes of different users, even
 * between different servers (see Wave Federation)</li>
 * <li>In-flight persistence of changes with eventual consistency (see Wave
 * Operational Transformations)</li>
 * </ul>
 * <p>
 *
 * @author pablojan@gmail.com (Pablo Ojanguren)
 *
 */
public class SWaveObject implements SObject, SObservableNode {


  private SWaveMap root;

  private SObject.StatusHandler statusHandler = null;

  private final SWavePresence presence;
  private final SWaveMetadata metadata;
  private final SWaveNodeManager nodeManager;
  private final SWaveVersionManager versionManager;


  /**
   * Private constructor.
   *
   * @param idGenerator
   * @param domain
   * @param wave
   */
  protected SWaveObject(SWaveNodeManager waveManager) {
    this.nodeManager = waveManager;
    root = waveManager.getDataRoot();
    this.presence = SWavePresence.create(waveManager.getTransient().getRootMap(),
        waveManager.getSession());
    this.metadata = waveManager.getMetadata();
    this.versionManager = SWaveVersionManager.create(waveManager.getMetadataRoot(),
        waveManager.getSession().get());
  }

  /**
   * Unit test only.
   * TODO fix visibility
   */
  public void clearCache() {
    root.clearCache();
  }

  @Override
  public void addListener(SMutationHandler h, String path) throws SException {
    root.addListener(h, path);
  }


  @Override
  public void removeListener(SMutationHandler h, String path) throws SException {
    root.removeListener(h, path);
  }

  @Override
  public void listen(SMutationHandler h) throws SException {
    this.root.listen(h);
  }

  @Override
  public void unlisten(SMutationHandler h) throws SException {
    this.root.unlisten(h);
  }

  @Override
  public String getId() {
    return nodeManager.getId();
  }

  @Override
  public void addParticipant(String participantId) throws InvalidParticipantAddress {
    nodeManager.addParticipant(participantId);
  }

  @Override
  public void removeParticipant(String participantId) throws InvalidParticipantAddress {
    nodeManager.removeParticipant(participantId);
  }

  @Override
  public String[] getParticipants() {
    return nodeManager.getParticipants().stream().map(p -> {
      return p.getAddress();
    }).toArray(String[]::new);
  }

  @Override
  public SSession[] getSessions() {
    return nodeManager.getMetadata().getSessions();
  }


  @Override
  public void setPublic(boolean isPublic) {
    nodeManager.setPublic(isPublic);
  }

  @Override
  public boolean isPublic() {
    return nodeManager.isPublic();
  }

  @Override
  public String getCreatorId() {
    return nodeManager.getCreatorId().getAddress();
  }

  public Object js() {
    return new Proxy(root, new SMapProxyHandler());
  }

  @Override
  public void setStatusHandler(StatusHandler h) {
    this.statusHandler = h;
  }

  @JsIgnore
  public void onStatusEvent(SStatusEvent e) {
    if (statusHandler != null)
      statusHandler.exec(e);
  }

  @Override
  public void setPresenceHandler(SPresenceEvent.Handler handler) {
    presence.setEventHandler(handler);
  }

  @Override
  public void trackPresence(boolean enable) {
    if (enable)
      presence.start(ServiceConfig.presencePassiveTracking() ? SWavePresence.Mode.PASSIVE
          : SWavePresence.Mode.ACTIVE);
    else
      presence.stop();
  }

  @Override
  public void setPresence(boolean online) {
    if (online)
      presence.setOnline();
    else
      presence.setOffline();
  }

  //
  // SMap interface
  //


  @Override
  public SNode pick(String key) throws SException {
    return root.pick(key);
  }

  @Override
  public SMap put(String key, SNode value) throws SException {
    return root.put(key, value);
  }

  @Override
  public SMap put(String key, Object object) throws SException {
    return root.put(key, object);
  }

  @Override
  public void remove(String key) throws SException {
    root.remove(key);
  }

  @Override
  public void removeSafe(String key) throws SException {
    root.removeSafe(key);
  }

  @Override
  public boolean has(String key) throws SException {
    return root.has(key);
  }

  @Override
  public String[] keys() throws SException {
    return root.keys();
  }

  @Override
  public void clear() throws SException {
    root.clear();
  }

  @Override
  public boolean isEmpty() {
    return root.isEmpty();
  }

  @Override
  public int size() {
    return root.size();
  }


  public boolean isNew() {
    return false;
  }

  @SuppressWarnings("rawtypes")
  @JsIgnore
  @Override
  public void accept(SVisitor visitor) {
    visitor.visit(this);
  }

  //
  // -----------------------------------------------------
  //

  @Override
  public void set(String path, Object value) {
    SNode.set(this, path, value);
  }

  @Override
  public void push(String path, Object value) {
    SNode.push(this, path, value);
  }

  @Override
  public Object pop(String path) {
    return SNode.pop(this, path);
  }

  @Override
  public int length(String path) {
    return SNode.length(this, path);
  }

  @Override
  public boolean contains(String path, String property) {
    return SNode.contains(this, path, property);
  }

  @Override
  public void delete(String path) {
    SNode.delete(this, path);
  }

  @Override
  public Object get(String path) {
    return SNode.get(this, path);
  }

  @Override
  public SNode node(String path) throws SException {
    return SNode.node(this, path);
  }

  @Override
  public SMap asMap() {
    return this.root;
  }

  @Override
  public SList<? extends SNode> asList() {
    throw new IllegalStateException("Node is not a list");
  }

  @Override
  public String asString() {
    throw new IllegalStateException("Node is not a string");
  }

  @Override
  public double asDouble() {
    throw new IllegalStateException("Node is not a number");
  }

  @Override
  public int asInt() {
    throw new IllegalStateException("Node is not a number");
  }

  @Override
  public boolean asBoolean() {
    throw new IllegalStateException("Node is not a boolean");
  }

  @Override
  public SText asText() {
    throw new IllegalStateException("Node is not a text");
  }

  @Override
  public SMap getUserStore() {
    return nodeManager.getUserRoot();
  }

  @Override
  public SMap getTransientStore() {
    return nodeManager.getTransientRoot();
  }

  @Override
  public String[] _getWavelets() {
    return nodeManager.getWavelets();
  }

  @Override
  public String[] _getDocuments(String waveletId) {
    return nodeManager.getDocuments(waveletId);
  }

  @Override
  public String _getContent(String waveletId, String documentId) {
    return nodeManager.getContent(waveletId, documentId);
  }

  @Override
  public SNode[] values() throws SException {
    return root.values();
  }

  @Override
  public SVersionManager getVersionManager() {
    return versionManager;
  }

}
