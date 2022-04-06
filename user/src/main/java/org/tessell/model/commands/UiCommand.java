package org.tessell.model.commands;

import static java.lang.Boolean.FALSE;
import static org.tessell.model.properties.NewProperty.booleanProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tessell.model.properties.BooleanProperty;
import org.tessell.model.properties.HasRuleTriggers;
import org.tessell.model.properties.Property;
import org.tessell.model.validation.events.RuleTriggeredEvent;
import org.tessell.model.validation.events.RuleTriggeredHandler;
import org.tessell.model.validation.events.RuleUntriggeredEvent;
import org.tessell.model.validation.events.RuleUntriggeredHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.event.shared.SimplerEventBus;

/**
 * Codifies a UI action that has an enabled state and optional validation rules
 * that must pass before it can execute.
 *
 * When {@link #execute()} is called, any dependent properties will be touched
 * to potentially trigger validation rules. If any of them fail validation or
 * return false, the execute is skipped.
 */
public abstract class UiCommand implements HasRuleTriggers, Runnable {

  private final BooleanProperty enabled = booleanProperty("enabled", true);
  private final EventBus handlers = new SimplerEventBus();
  private final Map<String, HasHandlers> errors = new HashMap<String, HasHandlers>();
  private final List<Property<Boolean>> onlyIf = new ArrayList<Property<Boolean>>();

  /**
   * Executes the UI command, first triggering any "only if" validation,
   * and then calling {@code doExecute} if they all pass.
   *
   * @throws IllegalStateException if the command is disabled
   */
  public final void execute() {
    if (!enabled.isTrue()) {
      throw new IllegalStateException("Command is disabled");
    }
    clearErrors();
    if (canExecute()) {
      doExecute();
    }
  }

  /** An alias for {@link #execute()}. */
  public void run() {
    execute();
  }

  /**
   * Adds a property that conditionalizes whether this command can be executed.
   *
   * {@code onlyIf} will be touched on {@link #execute()} and the execution
   * skipped if it is invalid or returns {@code false}.
   */
  public void addOnlyIf(Property<Boolean> onlyIf) {
    this.onlyIf.add(onlyIf);
    // TODO onlyIf.addDerived()
  }

  public void removeOnlyIf(Property<Boolean> onlyIf) {
    this.onlyIf.remove(onlyIf);
  }

  /** Fires an error message against this command's handlers. */
  public void error(String message) {
    error(handlers, message);
  }

  /** Fires an error message against the {@code errorTarget}'s handlers. */
  public void error(HasHandlers errorTarget, String message) {
    if (errors.containsKey(message)) {
      return;
    }
    errors.put(message, errorTarget);
    errorTarget.fireEvent(new RuleTriggeredEvent(message, message, new Boolean[] { false }));
  }

  /**
   * Clears any errors that were fired against this instance (by {@link #error(String)).
   *
   * Note that this is automatically called by {@link #execute()} so that any now-invalid
   * messages are removed before the rules are rerun.
   */
  public void clearErrors() {
    for (Map.Entry<String, HasHandlers> e : errors.entrySet()) {
      e.getValue().fireEvent(new RuleUntriggeredEvent(e.getKey(), e.getKey()));
    }
    errors.clear();
  }

  /**
   * @return the property of whether this command is enabled or not
   */
  public Property<Boolean> enabled() {
    return enabled;
  }

  @Override
  public HandlerRegistration addRuleTriggeredHandler(RuleTriggeredHandler handler) {
    return handlers.addHandler(RuleTriggeredEvent.getType(), handler);
  }

  @Override
  public HandlerRegistration addRuleUntriggeredHandler(RuleUntriggeredHandler handler) {
    return handlers.addHandler(RuleUntriggeredEvent.getType(), handler);
  }

  /**
   * @return the list of only if conditions; purposefully modifiable so it can be bound against
   */
  public List<Property<Boolean>> getOnlyIf() {
    return onlyIf;
  }

  /**
   * Method for subclasses to perform the command's logic once the validation rules have passed.
   */
  protected abstract void doExecute();

  /** @return {@code true} if each onlyIf property, after touching, is valid. */
  private boolean canExecute() {
    boolean allValid = true;
    for (Property<Boolean> p : onlyIf) {
      if (!p.touch() || FALSE.equals(p.get())) {
        allValid = false; // purposefully do not early return
      }
    }
    return allValid;
  }

}
