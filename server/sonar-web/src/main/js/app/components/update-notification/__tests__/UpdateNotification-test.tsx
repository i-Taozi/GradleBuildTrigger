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
import { shallow } from 'enzyme';
import * as React from 'react';
import { getSystemUpgrades } from '../../../../api/system';
import { Alert } from '../../../../components/ui/Alert';
import DismissableAlert from '../../../../components/ui/DismissableAlert';
import { mockUpgrades } from '../../../../helpers/mocks/system-upgrades';
import { mockAppState, mockCurrentUser, mockLoggedInUser } from '../../../../helpers/testMocks';
import { waitAndUpdate } from '../../../../helpers/testUtils';
import { Permissions } from '../../../../types/permissions';
import { UpdateNotification } from '../UpdateNotification';

jest.mock('../../../../api/system', () => {
  const { mockUpgrades } = jest.requireActual('../../../../helpers/mocks/system-upgrades');
  return {
    getSystemUpgrades: jest
      .fn()
      .mockResolvedValue({ upgrades: [mockUpgrades({ version: '9.1' })], latestLTS: '8.9' })
  };
});

function formatDate(date: Date): string {
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
}

it('should render correctly', async () => {
  const wrapper = shallowRender({
    appState: mockAppState({ version: '9.0' }),
    currentUser: mockLoggedInUser({ permissions: { global: [Permissions.Admin] } })
  });
  await waitAndUpdate(wrapper);
  expect(wrapper).toMatchSnapshot('default');
  expect(wrapper.setProps({ currentUser: mockCurrentUser() })).toMatchSnapshot('anonymous user');
});

it('should not show prompt when not admin', async () => {
  //As anonymous
  const wrapper = shallowRender();
  await waitAndUpdate(wrapper);
  expect(wrapper.type()).toBeNull();

  // As non admin user
  wrapper.setProps({ currentUser: mockLoggedInUser() });
  await waitAndUpdate(wrapper);
  expect(wrapper.type()).toBeNull();
});

it('should not show prompt when no current version', async () => {
  const wrapper = shallowRender({ appState: mockAppState({ version: 'NOVERSION' }) });
  await waitAndUpdate(wrapper);
  expect(wrapper.type()).toBeNull();
});

it('should not show prompt when no upgrade', async () => {
  (getSystemUpgrades as jest.Mock).mockResolvedValueOnce({ upgrades: [], latestLTS: '8.9' });
  const wrapper = shallowRender({
    appState: mockAppState({ version: '9.1' }),
    currentUser: mockLoggedInUser({ permissions: { global: [Permissions.Admin] } })
  });
  await waitAndUpdate(wrapper);
  expect(wrapper.type()).toBeNull();
});

it('should show prompt when no lts date', async () => {
  (getSystemUpgrades as jest.Mock).mockResolvedValueOnce({
    upgrades: [mockUpgrades({ version: '8.9', releaseDate: 'INVALID' })],
    latestLTS: '8.9'
  });
  const wrapper = shallowRender({
    appState: mockAppState({ version: '8.1' }),
    currentUser: mockLoggedInUser({ permissions: { global: [Permissions.Admin] } })
  });
  await waitAndUpdate(wrapper);
  expect(wrapper.find(DismissableAlert).props().alertKey).toBe('previous_lts8.9');
  expect(wrapper.contains('admin_notification.update.previous_lts')).toBe(true);
});

it('should show prompt when minor upgrade', async () => {
  (getSystemUpgrades as jest.Mock).mockResolvedValueOnce({
    upgrades: [mockUpgrades({ version: '9.2' }), mockUpgrades({ version: '9.1' })],
    latestLTS: '8.9'
  });
  const wrapper = shallowRender({
    appState: mockAppState({ version: '9.1' }),
    currentUser: mockLoggedInUser({ permissions: { global: [Permissions.Admin] } })
  });
  await waitAndUpdate(wrapper);
  expect(wrapper.find(DismissableAlert).props().alertKey).toBe('new_minor_version9.2');
  expect(wrapper.contains('admin_notification.update.new_minor_version')).toBe(true);
});

it('should show prompt when patch upgrade', async () => {
  (getSystemUpgrades as jest.Mock).mockResolvedValueOnce({
    upgrades: [mockUpgrades({ version: '9.2' }), mockUpgrades({ version: '9.1.1' })],
    latestLTS: '8.9'
  });
  const wrapper = shallowRender({
    appState: mockAppState({ version: '9.1' }),
    currentUser: mockLoggedInUser({ permissions: { global: [Permissions.Admin] } })
  });
  await waitAndUpdate(wrapper);
  expect(wrapper.find(DismissableAlert).props().alertKey).toBe('new_patch9.2');
  expect(wrapper.contains('admin_notification.update.new_patch')).toBe(true);
});

it('should show prompt when lts upgrade', async () => {
  (getSystemUpgrades as jest.Mock).mockResolvedValueOnce({
    upgrades: [
      mockUpgrades({ version: '8.9', releaseDate: formatDate(new Date(Date.now())) }),
      mockUpgrades({ version: '9.2' }),
      mockUpgrades({ version: '9.1.1' })
    ],
    latestLTS: '8.9'
  });
  const wrapper = shallowRender({
    appState: mockAppState({ version: '8.8' }),
    currentUser: mockLoggedInUser({ permissions: { global: [Permissions.Admin] } })
  });
  await waitAndUpdate(wrapper);
  expect(wrapper.find(DismissableAlert).props().alertKey).toBe('pre_lts8.9');
  expect(wrapper.contains('admin_notification.update.pre_lts')).toBe(true);
});

it('should show prompt when lts upgrade is more than 6 month', async () => {
  const ltsDate = new Date(Date.now());
  ltsDate.setMonth(ltsDate.getMonth() - 7);
  (getSystemUpgrades as jest.Mock).mockResolvedValueOnce({
    upgrades: [
      mockUpgrades({ version: '8.9', releaseDate: formatDate(ltsDate) }),
      mockUpgrades({ version: '9.2' }),
      mockUpgrades({ version: '9.1.1' })
    ],
    latestLTS: '8.9'
  });
  const wrapper = shallowRender({
    appState: mockAppState({ version: '8.8' }),
    currentUser: mockLoggedInUser({ permissions: { global: [Permissions.Admin] } })
  });
  await waitAndUpdate(wrapper);
  expect(wrapper.find(DismissableAlert).props().alertKey).toBe('previous_lts8.9');
  expect(wrapper.contains('admin_notification.update.previous_lts')).toBe(true);
});

it('should show correct alert when not dismissable', async () => {
  (getSystemUpgrades as jest.Mock).mockResolvedValueOnce({
    upgrades: [
      mockUpgrades({ version: '8.9', releaseDate: formatDate(new Date(Date.now())) }),
      mockUpgrades({ version: '9.2' }),
      mockUpgrades({ version: '9.1.1' })
    ],
    latestLTS: '8.9'
  });
  const wrapper = shallowRender({
    appState: mockAppState({ version: '8.8' }),
    currentUser: mockLoggedInUser({ permissions: { global: [Permissions.Admin] } })
  });
  await waitAndUpdate(wrapper);
  expect(wrapper.find(DismissableAlert).type).toBeDefined();
  wrapper.setProps({ dismissable: false });
  expect(wrapper.find(Alert).type).toBeDefined();
});

function shallowRender(props: Partial<UpdateNotification['props']> = {}) {
  return shallow(
    <UpdateNotification
      dismissable={true}
      appState={mockAppState()}
      currentUser={mockCurrentUser()}
      {...props}
    />
  );
}
