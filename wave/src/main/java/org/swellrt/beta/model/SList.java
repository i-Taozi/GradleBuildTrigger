package org.swellrt.beta.model;


import org.swellrt.beta.common.SException;
import org.swellrt.beta.model.local.SListLocal;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOptional;
import jsinterop.annotations.JsType;

@JsType(namespace = "swell", name = "List")
public interface SList<T extends SNode> extends SNode, SObservableNode {

  @SuppressWarnings("rawtypes")
  public static SList create(@JsOptional Object data) throws IllegalCastException {
    /*
	  if (data != null && data instanceof JavaScriptObject)
		  return SListJs.create((JavaScriptObject) data);
    */
    return new SListLocal();
  }

  @SuppressWarnings("rawtypes")
  @JsIgnore
  public static SList create() {
    return new SListLocal();
  }

  /**
   * Returns a container or a primitive value container.
   * @param key
   * @return
   */
  public SNode pick(int index) throws SException;

  public SList<T> addAt(Object object, int index) throws SException;

  public SList<T> add(Object object) throws SException;

  public SList<T> remove(int index) throws SException;

  public int indexOf(T node) throws SException;

  public void clear() throws SException;

  public boolean isEmpty();

  public int size();

  @JsIgnore
  public Iterable<T> values();

}
