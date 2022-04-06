package org.tessell.tests.model.validation.rules;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import joist.util.Join;

import org.tessell.model.properties.HasRuleTriggers;
import org.tessell.model.validation.events.RuleTriggeredEvent;
import org.tessell.model.validation.events.RuleTriggeredHandler;
import org.tessell.model.validation.events.RuleUntriggeredEvent;
import org.tessell.model.validation.events.RuleUntriggeredHandler;

public abstract class AbstractRuleTest {

  protected final Map<Object, String> messages = new LinkedHashMap<Object, String>();

  public <T extends HasRuleTriggers> T listenTo(final T hasTriggers) {
    hasTriggers.addRuleTriggeredHandler(new RuleTriggeredHandler() {
      public void onTrigger(final RuleTriggeredEvent event) {
        if (event.getMessage() != null) {
          messages.put(event.getKey(), event.getMessage());
        }
      }
    });
    hasTriggers.addRuleUntriggeredHandler(new RuleUntriggeredHandler() {
      public void onUntrigger(final RuleUntriggeredEvent event) {
        messages.remove(event.getKey());
      }
    });
    return hasTriggers;
  }

  protected void assertMessages(final String... messages) {
    assertEquals(Join.lines(messages), Join.lines(this.messages.values()));
  }

  protected void assertNoMessages() {
    assertMessages();
  }

}
