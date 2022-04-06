/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.business.application.impl;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;

/**
 * {@link ApplicationImpl} specific assertions - Generated by CustomAssertionGenerator.
 */
public class ApplicationImplAssert extends AbstractAssert<ApplicationImplAssert, ApplicationImpl> {

    /**
     * Creates a new <code>{@link ApplicationImplAssert}</code> to make assertions on actual ApplicationImpl.
     *
     * @param actual the ApplicationImpl we want to make assertions on.
     */
    public ApplicationImplAssert(ApplicationImpl actual) {
        super(actual, ApplicationImplAssert.class);
    }

    /**
     * An entry point for ApplicationImplAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myApplicationImpl)</code> and get specific
     * assertion with code completion.
     *
     * @param actual the ApplicationImpl we want to make assertions on.
     * @return a new <code>{@link ApplicationImplAssert}</code>
     */
    public static ApplicationImplAssert assertThat(ApplicationImpl actual) {
        return new ApplicationImplAssert(actual);
    }

    /**
     * Verifies that the actual ApplicationImpl's createdBy is equal to the given one.
     *
     * @param createdBy the given createdBy to compare the actual ApplicationImpl's createdBy to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's createdBy is not equal to the given one.
     */
    public ApplicationImplAssert hasCreatedBy(long createdBy) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected createdBy of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // check
        long actualCreatedBy = actual.getCreatedBy();
        if (actualCreatedBy != createdBy) {
            failWithMessage(assertjErrorMessage, actual, createdBy, actualCreatedBy);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's creationDate is equal to the given one.
     *
     * @param creationDate the given creationDate to compare the actual ApplicationImpl's creationDate to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's creationDate is not equal to the given one.
     */
    public ApplicationImplAssert hasCreationDate(java.util.Date creationDate) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected creationDate of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        java.util.Date actualCreationDate = actual.getCreationDate();
        if (!Objects.areEqual(actualCreationDate, creationDate)) {
            failWithMessage(assertjErrorMessage, actual, creationDate, actualCreationDate);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's description is equal to the given one.
     *
     * @param description the given description to compare the actual ApplicationImpl's description to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's description is not equal to the given one.
     */
    public ApplicationImplAssert hasDescription(String description) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected description of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualDescription = actual.getDescription();
        if (!Objects.areEqual(actualDescription, description)) {
            failWithMessage(assertjErrorMessage, actual, description, actualDescription);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's displayName is equal to the given one.
     *
     * @param displayName the given displayName to compare the actual ApplicationImpl's displayName to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's displayName is not equal to the given one.
     */
    public ApplicationImplAssert hasDisplayName(String displayName) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected displayName of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualDisplayName = actual.getDisplayName();
        if (!Objects.areEqual(actualDisplayName, displayName)) {
            failWithMessage(assertjErrorMessage, actual, displayName, actualDisplayName);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's homePageId is equal to the given one.
     *
     * @param homePageId the given homePageId to compare the actual ApplicationImpl's homePageId to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's homePageId is not equal to the given one.
     */
    public ApplicationImplAssert hasHomePageId(Long homePageId) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected homePageId of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        Long actualHomePageId = actual.getHomePageId();
        if (!Objects.areEqual(actualHomePageId, homePageId)) {
            failWithMessage(assertjErrorMessage, actual, homePageId, actualHomePageId);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's iconPath is equal to the given one.
     *
     * @param iconPath the given iconPath to compare the actual ApplicationImpl's iconPath to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's iconPath is not equal to the given one.
     */
    public ApplicationImplAssert hasIconPath(String iconPath) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected iconPath of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualIconPath = actual.getIconPath();
        if (!Objects.areEqual(actualIconPath, iconPath)) {
            failWithMessage(assertjErrorMessage, actual, iconPath, actualIconPath);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's id is equal to the given one.
     *
     * @param id the given id to compare the actual ApplicationImpl's id to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's id is not equal to the given one.
     */
    public ApplicationImplAssert hasId(long id) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected id of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // check
        long actualId = actual.getId();
        if (actualId != id) {
            failWithMessage(assertjErrorMessage, actual, id, actualId);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's lastUpdateDate is equal to the given one.
     *
     * @param lastUpdateDate the given lastUpdateDate to compare the actual ApplicationImpl's lastUpdateDate to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's lastUpdateDate is not equal to the given one.
     */
    public ApplicationImplAssert hasLastUpdateDate(java.util.Date lastUpdateDate) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected lastUpdateDate of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        java.util.Date actualLastUpdateDate = actual.getLastUpdateDate();
        if (!Objects.areEqual(actualLastUpdateDate, lastUpdateDate)) {
            failWithMessage(assertjErrorMessage, actual, lastUpdateDate, actualLastUpdateDate);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's layoutId is equal to the given one.
     *
     * @param layoutId the given layoutId to compare the actual ApplicationImpl's layoutId to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's layoutId is not equal to the given one.
     */
    public ApplicationImplAssert hasLayoutId(Long layoutId) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected layoutId of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        Long actualLayoutId = actual.getLayoutId();
        if (!Objects.areEqual(actualLayoutId, layoutId)) {
            failWithMessage(assertjErrorMessage, actual, layoutId, actualLayoutId);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's profileId is equal to the given one.
     *
     * @param profileId the given profileId to compare the actual ApplicationImpl's profileId to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's profileId is not equal to the given one.
     */
    public ApplicationImplAssert hasProfileId(Long profileId) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected profileId of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        Long actualProfileId = actual.getProfileId();
        if (!Objects.areEqual(actualProfileId, profileId)) {
            failWithMessage(assertjErrorMessage, actual, profileId, actualProfileId);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's state is equal to the given one.
     *
     * @param state the given state to compare the actual ApplicationImpl's state to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's state is not equal to the given one.
     */
    public ApplicationImplAssert hasState(String state) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected state of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualState = actual.getState();
        if (!Objects.areEqual(actualState, state)) {
            failWithMessage(assertjErrorMessage, actual, state, actualState);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's themeId is equal to the given one.
     *
     * @param themeId the given themeId to compare the actual ApplicationImpl's themeId to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's themeId is not equal to the given one.
     */
    public ApplicationImplAssert hasThemeId(Long themeId) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected themeId of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        Long actualThemeId = actual.getThemeId();
        if (!Objects.areEqual(actualThemeId, themeId)) {
            failWithMessage(assertjErrorMessage, actual, themeId, actualThemeId);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's token is equal to the given one.
     *
     * @param token the given token to compare the actual ApplicationImpl's token to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's token is not equal to the given one.
     */
    public ApplicationImplAssert hasToken(String token) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected token of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualToken = actual.getToken();
        if (!Objects.areEqual(actualToken, token)) {
            failWithMessage(assertjErrorMessage, actual, token, actualToken);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's updatedBy is equal to the given one.
     *
     * @param updatedBy the given updatedBy to compare the actual ApplicationImpl's updatedBy to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's updatedBy is not equal to the given one.
     */
    public ApplicationImplAssert hasUpdatedBy(long updatedBy) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected updatedBy of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // check
        long actualUpdatedBy = actual.getUpdatedBy();
        if (actualUpdatedBy != updatedBy) {
            failWithMessage(assertjErrorMessage, actual, updatedBy, actualUpdatedBy);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual ApplicationImpl's version is equal to the given one.
     *
     * @param version the given version to compare the actual ApplicationImpl's version to.
     * @return this assertion object.
     * @throws AssertionError - if the actual ApplicationImpl's version is not equal to the given one.
     */
    public ApplicationImplAssert hasVersion(String version) {
        // check that actual ApplicationImpl we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpected version of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualVersion = actual.getVersion();
        if (!Objects.areEqual(actualVersion, version)) {
            failWithMessage(assertjErrorMessage, actual, version, actualVersion);
        }

        // return the current assertion for method chaining
        return this;
    }

}
