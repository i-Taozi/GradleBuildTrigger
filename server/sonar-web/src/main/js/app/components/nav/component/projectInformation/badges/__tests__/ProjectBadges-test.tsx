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
import { getProjectBadgesToken } from '../../../../../../../api/project-badges';
import CodeSnippet from '../../../../../../../components/common/CodeSnippet';
import { mockBranch } from '../../../../../../../helpers/mocks/branch-like';
import { mockComponent } from '../../../../../../../helpers/mocks/component';
import { mockMetric } from '../../../../../../../helpers/testMocks';
import { waitAndUpdate } from '../../../../../../../helpers/testUtils';
import { Location } from '../../../../../../../helpers/urls';
import { ComponentQualifier } from '../../../../../../../types/component';
import { MetricKey } from '../../../../../../../types/metrics';
import BadgeButton from '../BadgeButton';
import ProjectBadges from '../ProjectBadges';

jest.mock('../../../../../../../helpers/urls', () => ({
  getHostUrl: () => 'host',
  getPathUrlAsString: (l: Location) => l.pathname,
  getProjectUrl: () => ({ pathname: '/dashboard' } as Location)
}));

jest.mock('../../../../../../../api/project-badges', () => ({
  getProjectBadgesToken: jest.fn().mockResolvedValue('foo'),
  renewProjectBadgesToken: jest.fn().mockResolvedValue({})
}));

it('should display correctly', async () => {
  const wrapper = shallowRender();
  await waitAndUpdate(wrapper);
  expect(wrapper).toMatchSnapshot();
});

it('should renew token', async () => {
  (getProjectBadgesToken as jest.Mock).mockResolvedValueOnce('foo').mockResolvedValueOnce('bar');
  const wrapper = shallowRender({
    component: mockComponent({ configuration: { showSettings: true } })
  });
  await waitAndUpdate(wrapper);
  wrapper.find('.it__project-info-renew-badge').simulate('click');

  // it shoud be loading
  expect(wrapper.find('.it__project-info-renew-badge').props().disabled).toBe(true);

  await waitAndUpdate(wrapper);
  const buttons = wrapper.find(BadgeButton);
  expect(buttons.at(0).props().url).toMatch('token=bar');
  expect(buttons.at(1).props().url).toMatch('token=bar');
  expect(wrapper.find(CodeSnippet).props().snippet).toMatch('token=bar');

  // let's check that the loading has correclty ends.
  expect(wrapper.find('.it__project-info-renew-badge').props().disabled).toBe(false);
});

function shallowRender(overrides = {}) {
  return shallow(
    <ProjectBadges
      branchLike={mockBranch()}
      metrics={{
        [MetricKey.coverage]: mockMetric({ key: MetricKey.coverage }),
        [MetricKey.new_code_smells]: mockMetric({ key: MetricKey.new_code_smells })
      }}
      component={mockComponent({ key: 'foo', qualifier: ComponentQualifier.Project })}
      {...overrides}
    />
  );
}
