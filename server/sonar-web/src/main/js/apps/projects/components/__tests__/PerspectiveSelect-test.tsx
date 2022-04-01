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
import BubblesIcon from '../../../../components/icons/BubblesIcon';
import ListIcon from '../../../../components/icons/ListIcon';
import { mockReactSelectOptionProps } from '../../../../helpers/mocks/react-select';
import PerspectiveSelect from '../PerspectiveSelect';

it('should render correctly', () => {
  expect(shallow(<PerspectiveSelect onChange={jest.fn()} view="overall" />)).toMatchSnapshot();
});

it('should render with coverage selected', () => {
  expect(shallowRender({ view: 'visualizations', visualization: 'coverage' })).toMatchSnapshot();
});

it('should render option correctly', () => {
  const wrapper = shallowRender();
  const OptionRender = wrapper.instance().perspectiveOptionRender;
  let option = shallow(
    <OptionRender {...mockReactSelectOptionProps({ type: 'view', value: 'test', label: 'test' })} />
  );

  expect(option.find(ListIcon).type).toBeDefined();
  option = shallow(
    <OptionRender
      {...mockReactSelectOptionProps({ type: 'visualization', value: 'test', label: '' })}
    />
  );
  expect(option.find(BubblesIcon).type).toBeDefined();
});

it('should handle perspective change correctly', () => {
  const onChange = jest.fn();
  const instance = shallowRender({ onChange }).instance();
  instance.handleChange({ label: 'overall', value: 'overall', type: 'view' });
  instance.handleChange({ label: 'leak', value: 'leak', type: 'view' });
  instance.handleChange({ label: 'coverage', value: 'coverage', type: 'visualization' });
  expect(onChange.mock.calls).toMatchSnapshot();
});

function shallowRender(overrides: Partial<PerspectiveSelect['props']> = {}) {
  return shallow<PerspectiveSelect>(
    <PerspectiveSelect onChange={jest.fn()} view="visualizations" {...overrides} />
  );
}
