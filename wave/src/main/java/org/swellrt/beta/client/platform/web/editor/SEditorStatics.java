package org.swellrt.beta.client.platform.web.editor;

import java.util.Map;

import org.swellrt.beta.client.platform.web.ServiceEntryPoint;
import org.swellrt.beta.client.platform.web.editor.caret.CaretAnnotationHandler;
import org.swellrt.beta.model.presence.SSessionManager;
import org.waveprotocol.wave.client.common.util.UserAgent;
import org.waveprotocol.wave.client.debug.logger.DomLogger;
import org.waveprotocol.wave.client.doodad.diff.DiffAnnotationHandler;
import org.waveprotocol.wave.client.doodad.diff.DiffDeleteRenderer;
import org.waveprotocol.wave.client.doodad.link.LinkAnnotationHandler;
import org.waveprotocol.wave.client.doodad.link.LinkAnnotationHandler.LinkAttributeAugmenter;
import org.waveprotocol.wave.client.editor.Editor;
import org.waveprotocol.wave.client.editor.EditorSettings;
import org.waveprotocol.wave.client.editor.EditorStaticDeps;
import org.waveprotocol.wave.client.editor.Editors;
import org.waveprotocol.wave.client.editor.content.ContentDocument;
import org.waveprotocol.wave.client.editor.content.misc.StyleAnnotationHandler;
import org.waveprotocol.wave.client.editor.content.paragraph.LineRendering;
import org.waveprotocol.wave.client.editor.keys.KeyBindingRegistry;
import org.waveprotocol.wave.client.widget.popup.PopupChrome;
import org.waveprotocol.wave.client.widget.popup.PopupChromeProvider;
import org.waveprotocol.wave.client.widget.popup.simple.Popup;
import org.waveprotocol.wave.common.logging.LoggerBundle;
import org.waveprotocol.wave.model.conversation.Blips;
import org.waveprotocol.wave.model.document.util.LineContainers;

/**
 * A static central place of configuration of SwellRT's Editor component. <br>
 * This class will invoke configuration logic of Wave's Editor parts.
 * <p>
 * <br>
 *
 * See following classes for more info on editor initialization
 * <ul>
 * <li>{@link Editors#initRootRegistries()}
 * <li>{@link Editor#ROOT_HANDLER_REGISTRY}
 * <li>{@link Editor#ROOT_ANNOTATION_REGISTRY}
 * <li>{@link Editor#init(org.waveprotocol.wave.client.editor.content.Registries, KeyBindingRegistry, EditorSettings)}
 * </ul>
 *
 *
 */
public class SEditorStatics {

  /** The annotation handler for carets */
  private static CaretAnnotationHandler caretAnnotationHandler;

  /** The key binding registry */
  private static KeyBindingRegistry keyBindingRegistry;

  /** @return the key binding registry */
  protected static KeyBindingRegistry getKeyBindingRegistry() {
    return keyBindingRegistry;
  }


  /** Editor's specific settings. */
  private static EditorSettings editorSettings;

  /** The latest configuration object */
  protected static SEditorConfig config;

  /** @return editor settings */
  protected static EditorSettings getSettings() {
    return editorSettings;
  }

  /** @return the session provider of the currently connected participants */
  public static SSessionManager getSSession() {
    return ServiceEntryPoint.getServiceContext().getSession();
  }

  /** @return true if configuration was done */
  public static boolean isConfigured() {
    return config != null;
  }

  public static SEditorConfig getConfig() {
    return config;
  }

  private static native SEditorConfig getDefaultConfig() /*-{
    return {};
  }-*/;

  public static void setConfigDefault() {
    setConfig(getDefaultConfig());
  }

  /**
   *
   */
  public static void setConfig(SEditorConfig config) {

    //
    // Logging
    //

    if (config.logPanel() != null) {

      DomLogger.enableAllModules();
      DomLogger.enable(config.logPanel());

    } else if (config.consoleLog()) {
      EditorStaticDeps.logger = LoggerBundle.CONSOLE_IMPL;
    }

    if (config.traceUserAgent())
      logUserAgent();

    //
    //
    //

    initConfigurableRegistries(config);

    configureEditorSettings(config);

    SEditorStatics.config = config;
  }

  private static void configureEditorSettings(SEditorConfig config) {

    editorSettings = new EditorSettings().setHasDebugDialog(config.debugDialog())
        .setUndoEnabled(config.undo()).setUseFancyCursorBias(config.fancyCursorBias())
        .setUseSemanticCopyPaste(config.semanticCopyPaste())
        .setUseWhitelistInEditor(config.whitelistEditor())
        .setUseWebkitCompositionEvents(config.webkitComposition());

  }

  private static void logUserAgent() {

    String s = "";

    s += "Android: " + UserAgent.isAndroid() + ", ";
    s += "IPhone: " + UserAgent.isIPhone() + ", ";

    s += "Linux: " + UserAgent.isLinux() + ", ";
    s += "Mac: " + UserAgent.isMac() + ", ";
    s += "Win: " + UserAgent.isWin() + ", ";

    s += "Mobile Webkit: " + UserAgent.isMobileWebkit() + ", ";
    s += "Webkit: " + UserAgent.isWebkit() + ", ";

    s += "Safari: " + UserAgent.isSafari() + ", ";
    s += "Chrome: " + UserAgent.isChrome() + ", ";
    s += "Firefox: " + UserAgent.isFirefox() + ", ";

    s += "IE: " + UserAgent.isIE() + ", ";
    s += "IE7: " + UserAgent.isIE7() + ", ";
    s += "IE8: " + UserAgent.isIE8() + ", ";

    EditorStaticDeps.logger.trace().log("User Agent: " + UserAgent.debugUserAgentString());
    EditorStaticDeps.logger.trace().log("User Agent details: " + s);

  }

  /**
   * Put here editor registry stuff depending on external configuration
   */
  private static void initConfigurableRegistries(SEditorConfig config) {

    caretAnnotationHandler = CaretAnnotationHandler.register(Editor.ROOT_REGISTRIES,
        config.caretFactory());

  }

  /**
   * Wave's Editor and ContentDocument logic relies on pluggable components as
   * handlers, renderers that must be declared in Editor's registry.
   * <p>
   * Here we do initialization and registry of main handlers and renderer
   * objects.
   * <P>
   * Call this method before creating any {@link ContentDocument} instance.
   */
  public static void initRegistries() {

    Editors.initRootRegistries();

    EditorStaticDeps.setPopupProvider(Popup.LIGHTWEIGHT_POPUP_PROVIDER);
    EditorStaticDeps.setPopupChromeProvider(new PopupChromeProvider() {
      @Override
      public PopupChrome createPopupChrome() {
        return null;
      }
    });

    //
    // Register Doodads: all are statically handled
    //

    // Code taken from RegistriesHolder
    LineRendering.registerContainer(Blips.BODY_TAGNAME,
        Editor.ROOT_REGISTRIES.getElementHandlerRegistry());
    // Blips.init();
    LineContainers.setTopLevelContainerTagname(Blips.BODY_TAGNAME);

    StyleAnnotationHandler.register(Editor.ROOT_REGISTRIES);

    // Listen for Diff annotations to paint new content or to insert a
    // delete-content tag to be rendered by the DiffDeleteRendere

    DiffAnnotationHandler.register(Editor.ROOT_REGISTRIES.getAnnotationHandlerRegistry(),
        Editor.ROOT_REGISTRIES.getPaintRegistry());

    DiffDeleteRenderer.register(Editor.ROOT_REGISTRIES.getElementHandlerRegistry());

    //
    // Reuse existing link annotation handler, but also support external
    // controller to get notified on mutation or input events
    //
    LinkAnnotationHandler.register(Editor.ROOT_REGISTRIES, new LinkAttributeAugmenter() {
      @Override
      public Map<String, String> augment(Map<String, Object> annotations, boolean isEditing,
          Map<String, String> current) {
        return current;
      }
    });

    keyBindingRegistry = new KeyBindingRegistry();

    // TODO register widgets. Widgets definitions are (so far) statically
    // registered
    // so they shouldn't be associated with any particular instance of SEditor

    /*
     * widgetRegistry.each(new ProcV<JsoWidgetController>() {
     *
     * @Override public void apply(String key, JsoWidgetController value) {
     * value.setEditorJsFacade(editorJsFacade); }
     *
     * });
     *
     * WidgetDoodad.register(Editor.ROOT_REGISTRIES.getElementHandlerRegistry(),
     * widgetRegistry);
     */
  }



}
