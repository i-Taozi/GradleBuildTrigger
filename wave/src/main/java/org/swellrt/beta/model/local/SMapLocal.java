package org.swellrt.beta.model.local;

import java.util.HashMap;
import java.util.Map;

import org.swellrt.beta.common.SException;
import org.swellrt.beta.model.IllegalCastException;
import org.swellrt.beta.model.SList;
import org.swellrt.beta.model.SMap;
import org.swellrt.beta.model.SMutationHandler;
import org.swellrt.beta.model.SNode;
import org.swellrt.beta.model.SText;
import org.swellrt.beta.model.SUtils;
import org.swellrt.beta.model.SVisitor;
import org.swellrt.beta.model.js.HasJsProxy;
import org.swellrt.beta.model.js.Proxy;
import org.swellrt.beta.model.js.SMapProxyHandler;

import jsinterop.annotations.JsIgnore;


public class SMapLocal implements SMap, HasJsProxy {

  public SMapLocal() {

  }

  private Map<String, SNode> map = new HashMap<String, SNode>();
  private Proxy proxy = null;


  @Override
  public SMap put(String key, SNode value) {
    map.put(key, value);
    return this;
  }

  @Override
  public SMap put(String key, Object object) throws IllegalCastException {
    SNode node = SUtils.castToSNode(object);
    return put(key, node);
  }

  @Override
  public void remove(String key) {
    map.remove(key);
  }

  @Override
  public void removeSafe(String key) {
    map.remove(key);
  }

  @Override
  public boolean has(String key) {
    return map.containsKey(key);
  }

  @Override
  public String[] keys() {
    return map.keySet().toArray(new String[map.size()]);
  }

  @Override
  public Proxy getJsProxy() {
    return proxy;
  }

  @Override
  public void setJsProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  @Override
  public SNode pick(String key) {
    SNode n = map.get(key);
    return n;
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public int size() {
    return map.size();
  }

  public Object js() {
    if (proxy == null)
      proxy = new Proxy(this, new SMapProxyHandler());
    return proxy;
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
    return this;
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
  public void addListener(SMutationHandler h, String path) throws SException {
  }

  @Override
  public void listen(SMutationHandler h) throws SException {
  }

  @Override
  public void removeListener(SMutationHandler h, String path) throws SException {
  }

  @Override
  public void unlisten(SMutationHandler h) throws SException {
  }

  @Override
  public SNode[] values() throws SException {
    return map.values().toArray(new SNode[map.size()]);
  }
}
