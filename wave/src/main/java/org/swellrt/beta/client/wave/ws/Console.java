package org.swellrt.beta.client.wave.ws;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, name="console", namespace = JsPackage.GLOBAL)
public class Console {

  public static native void log(String s);
  
  private Console() {
    
  }
}
