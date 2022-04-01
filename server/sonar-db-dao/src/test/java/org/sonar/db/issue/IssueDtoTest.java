/*
 * SonarQube
 * Copyright (C) 2009-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.db.issue;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.sonar.api.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.Duration;
import org.sonar.core.issue.DefaultIssue;
import org.sonar.db.rule.RuleDefinitionDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IssueDtoTest {


  @Test
  public void set_data_check_maximal_length() {
    assertThatThrownBy(() -> {
      StringBuilder s = new StringBuilder(4500);
      for (int i = 0; i < 4500; i++) {
        s.append('a');
      }
      new IssueDto().setIssueAttributes(s.toString());
    })
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Value is too long for issue attributes:");
  }

  @Test
  public void set_issue_fields() {
    Date createdAt = DateUtils.addDays(new Date(), -5);
    Date updatedAt = DateUtils.addDays(new Date(), -3);
    Date closedAt = DateUtils.addDays(new Date(), -1);

    IssueDto dto = new IssueDto()
      .setKee("100")
      .setType(RuleType.VULNERABILITY)
      .setRuleUuid("rule-uuid-1")
      .setRuleKey("squid", "AvoidCycle")
      .setLanguage("xoo")
      .setComponentKey("org.sonar.sample:Sample")
      .setComponentUuid("CDEF")
      .setProjectUuid("GHIJ")
      .setModuleUuid("BCDE")
      .setModuleUuidPath("ABCD.BCDE.")
      .setProjectKey("org.sonar.sample")
      .setStatus(Issue.STATUS_CLOSED)
      .setResolution(Issue.RESOLUTION_FALSE_POSITIVE)
      .setGap(15.0)
      .setEffort(10L)
      .setLine(6)
      .setSeverity("BLOCKER")
      .setMessage("message")
      .setManualSeverity(true)
      .setAssigneeUuid("perceval")
      .setIssueAttributes("key=value")
      .setAuthorLogin("pierre")
      .setIssueCreationDate(createdAt)
      .setIssueUpdateDate(updatedAt)
      .setIssueCloseDate(closedAt);

    DefaultIssue issue = dto.toDefaultIssue();
    assertThat(issue.key()).isEqualTo("100");
    assertThat(issue.type()).isEqualTo(RuleType.VULNERABILITY);
    assertThat(issue.ruleKey()).hasToString("squid:AvoidCycle");
    assertThat(issue.language()).isEqualTo("xoo");
    assertThat(issue.componentUuid()).isEqualTo("CDEF");
    assertThat(issue.projectUuid()).isEqualTo("GHIJ");
    assertThat(issue.componentKey()).isEqualTo("org.sonar.sample:Sample");
    assertThat(issue.moduleUuid()).isEqualTo("BCDE");
    assertThat(issue.moduleUuidPath()).isEqualTo("ABCD.BCDE.");
    assertThat(issue.projectKey()).isEqualTo("org.sonar.sample");
    assertThat(issue.status()).isEqualTo(Issue.STATUS_CLOSED);
    assertThat(issue.resolution()).isEqualTo(Issue.RESOLUTION_FALSE_POSITIVE);
    assertThat(issue.gap()).isEqualTo(15.0);
    assertThat(issue.effort()).isEqualTo(Duration.create(10L));
    assertThat(issue.line()).isEqualTo(6);
    assertThat(issue.severity()).isEqualTo("BLOCKER");
    assertThat(issue.message()).isEqualTo("message");
    assertThat(issue.manualSeverity()).isTrue();
    assertThat(issue.assignee()).isEqualTo("perceval");
    assertThat(issue.attribute("key")).isEqualTo("value");
    assertThat(issue.authorLogin()).isEqualTo("pierre");
    assertThat(issue.creationDate()).isEqualTo(DateUtils.truncate(createdAt, Calendar.SECOND));
    assertThat(issue.updateDate()).isEqualTo(DateUtils.truncate(updatedAt, Calendar.SECOND));
    assertThat(issue.closeDate()).isEqualTo(DateUtils.truncate(closedAt, Calendar.SECOND));
    assertThat(issue.isNew()).isFalse();
  }

  @Test
  public void set_rule() {
    IssueDto dto = new IssueDto()
      .setKee("100")
      .setRule(new RuleDefinitionDto().setUuid("uuid-1").setRuleKey("AvoidCycle").setRepositoryKey("squid").setIsExternal(true))
      .setLanguage("xoo");

    assertThat(dto.getRuleUuid()).isEqualTo("uuid-1");
    assertThat(dto.getRuleRepo()).isEqualTo("squid");
    assertThat(dto.getRule()).isEqualTo("AvoidCycle");
    assertThat(dto.getRuleKey()).hasToString("squid:AvoidCycle");
    assertThat(dto.getLanguage()).isEqualTo("xoo");
    assertThat(dto.isExternal()).isTrue();
  }

  @Test
  public void set_tags() {
    IssueDto dto = new IssueDto();
    assertThat(dto.getTags()).isEmpty();
    assertThat(dto.getTagsString()).isNull();

    dto.setTags(Arrays.asList("tag1", "tag2", "tag3"));
    assertThat(dto.getTags()).containsOnly("tag1", "tag2", "tag3");
    assertThat(dto.getTagsString()).isEqualTo("tag1,tag2,tag3");

    dto.setTags(Arrays.asList());
    assertThat(dto.getTags()).isEmpty();

    dto.setTagsString("tag1, tag2 ,,tag3");
    assertThat(dto.getTags()).containsOnly("tag1", "tag2", "tag3");

    dto.setTagsString(null);
    assertThat(dto.getTags()).isEmpty();

    dto.setTagsString("");
    assertThat(dto.getTags()).isEmpty();
  }

  @Test
  public void toDtoForComputationInsert_givenDefaultIssueWithAllFields_returnFullIssueDto() {
    long now = System.currentTimeMillis();
    Date dateNow = Date.from(new Date(now).toInstant().truncatedTo(ChronoUnit.SECONDS));
    DefaultIssue defaultIssue = createExampleDefaultIssue(dateNow);

    IssueDto issueDto = IssueDto.toDtoForComputationInsert(defaultIssue, "ruleUuid", now);

    assertThat(issueDto).extracting(IssueDto::getKey, IssueDto::getType, IssueDto::getRuleKey).
      containsExactly("key", RuleType.BUG.getDbConstant(), RuleKey.of("repo", "rule"));

    assertThat(issueDto).extracting(IssueDto::getIssueCreationDate, IssueDto::getIssueCloseDate,
      IssueDto::getIssueUpdateDate, IssueDto::getSelectedAt, IssueDto::getUpdatedAt, IssueDto::getCreatedAt)
      .containsExactly(dateNow, dateNow, dateNow, dateNow.getTime(), now, now);

    assertThat(issueDto).extracting(IssueDto::getLine, IssueDto::getMessage,
      IssueDto::getGap, IssueDto::getEffort, IssueDto::getResolution, IssueDto::getStatus, IssueDto::getSeverity)
      .containsExactly(1, "message", 1.0, 1L, Issue.RESOLUTION_FALSE_POSITIVE, Issue.STATUS_CLOSED, "BLOCKER");

    assertThat(issueDto).extracting(IssueDto::getTags, IssueDto::getAuthorLogin)
      .containsExactly(Set.of("todo"), "admin");

    assertThat(issueDto).extracting(IssueDto::isManualSeverity, IssueDto::getChecksum, IssueDto::getAssigneeUuid,
      IssueDto::isExternal, IssueDto::getComponentUuid, IssueDto::getComponentKey, IssueDto::getModuleUuid,
      IssueDto::getModuleUuidPath, IssueDto::getProjectUuid, IssueDto::getProjectKey, IssueDto::getIssueAttributes,
      IssueDto::getRuleUuid)
      .containsExactly(true, "123", "123", true, "123", "componentKey", "moduleUuid",
        "path/to/module/uuid", "123", "projectKey", "key=value", "ruleUuid");

    assertThat(issueDto.isQuickFixAvailable()).isTrue();

  }

  @Test
  public void toDtoForUpdate_givenDefaultIssueWithAllFields_returnFullIssueDto() {
    long now = System.currentTimeMillis();
    Date dateNow = Date.from(new Date(now).toInstant().truncatedTo(ChronoUnit.SECONDS));
    DefaultIssue defaultIssue = createExampleDefaultIssue(dateNow);

    IssueDto issueDto = IssueDto.toDtoForUpdate(defaultIssue, now);

    assertThat(issueDto).extracting(IssueDto::getKey, IssueDto::getType, IssueDto::getRuleKey).
      containsExactly("key", RuleType.BUG.getDbConstant(), RuleKey.of("repo", "rule"));

    assertThat(issueDto).extracting(IssueDto::getIssueCreationDate, IssueDto::getIssueCloseDate,
      IssueDto::getIssueUpdateDate, IssueDto::getSelectedAt, IssueDto::getUpdatedAt)
      .containsExactly(dateNow, dateNow, dateNow, dateNow.getTime(), now);

    assertThat(issueDto).extracting(IssueDto::getLine, IssueDto::getMessage,
      IssueDto::getGap, IssueDto::getEffort, IssueDto::getResolution, IssueDto::getStatus, IssueDto::getSeverity)
      .containsExactly(1, "message", 1.0, 1L, Issue.RESOLUTION_FALSE_POSITIVE, Issue.STATUS_CLOSED, "BLOCKER");

    assertThat(issueDto).extracting(IssueDto::getTags, IssueDto::getAuthorLogin)
      .containsExactly(Set.of("todo"), "admin");

    assertThat(issueDto).extracting(IssueDto::isManualSeverity, IssueDto::getChecksum, IssueDto::getAssigneeUuid,
      IssueDto::isExternal, IssueDto::getComponentUuid, IssueDto::getComponentKey, IssueDto::getModuleUuid,
      IssueDto::getModuleUuidPath, IssueDto::getProjectUuid, IssueDto::getProjectKey, IssueDto::getIssueAttributes)
      .containsExactly(true, "123", "123", true, "123", "componentKey", "moduleUuid",
        "path/to/module/uuid", "123", "projectKey", "key=value");

    assertThat(issueDto.isQuickFixAvailable()).isTrue();

  }

  private DefaultIssue createExampleDefaultIssue(Date dateNow) {
    DefaultIssue defaultIssue = new DefaultIssue();
    defaultIssue.setKey("key")
      .setType(RuleType.BUG)
      .setLine(1)
      .setMessage("message")
      .setGap(1.0)
      .setEffort(Duration.create(1))
      .setResolution(Issue.RESOLUTION_FALSE_POSITIVE)
      .setStatus(Issue.STATUS_CLOSED)
      .setSeverity("BLOCKER")
      .setManualSeverity(true)
      .setChecksum("123")
      .setAssigneeUuid("123")
      .setRuleKey(RuleKey.of("repo", "rule"))
      .setIsFromExternalRuleEngine(true)
      .setTags(List.of("todo"))
      .setComponentUuid("123")
      .setComponentKey("componentKey")
      .setModuleUuid("moduleUuid")
      .setModuleUuidPath("path/to/module/uuid")
      .setProjectUuid("123")
      .setProjectKey("projectKey")
      .setAttribute("key", "value")
      .setAuthorLogin("admin")
      .setCreationDate(dateNow)
      .setCloseDate(dateNow)
      .setUpdateDate(dateNow)
      .setSelectedAt(dateNow.getTime())
      .setQuickFixAvailable(true);
    return defaultIssue;
  }
}
