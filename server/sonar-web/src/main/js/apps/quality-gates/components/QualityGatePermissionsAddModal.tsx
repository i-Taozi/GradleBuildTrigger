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
import { debounce } from 'lodash';
import * as React from 'react';
import { searchGroups, searchUsers } from '../../../api/quality-gates';
import { Group, SearchPermissionsParameters } from '../../../types/quality-gates';
import QualityGatePermissionsAddModalRenderer from './QualityGatePermissionsAddModalRenderer';

interface Props {
  onClose: () => void;
  onSubmit: (selection: T.UserBase | Group) => void;
  qualityGate: T.QualityGate;
  submitting: boolean;
}

interface State {
  loading: boolean;
  query?: string;
  searchResults: Array<T.UserBase | Group>;
  selection?: T.UserBase | Group;
}

const DEBOUNCE_DELAY = 250;

export default class QualityGatePermissionsAddModal extends React.Component<Props, State> {
  mounted = false;
  state: State = {
    loading: false,
    searchResults: []
  };

  constructor(props: Props) {
    super(props);
    this.handleSearch = debounce(this.handleSearch, DEBOUNCE_DELAY);
  }

  componentDidMount() {
    this.mounted = true;
  }

  componentWillUnmount() {
    this.mounted = false;
  }

  handleSearch = async (query: string) => {
    const { qualityGate } = this.props;
    this.setState({ loading: true });

    const queryParams: SearchPermissionsParameters = {
      gateName: qualityGate.name,
      q: query,
      selected: 'deselected'
    };

    try {
      const [{ users }, { groups }] = await Promise.all([
        searchUsers(queryParams),
        searchGroups(queryParams)
      ]);
      if (this.mounted) {
        this.setState({ loading: false, searchResults: [...users, ...groups] });
      }
    } catch {
      if (this.mounted) {
        this.setState({ loading: false });
      }
    }
  };

  handleInputChange = (query: string) => {
    this.setState({ query });
    if (query.length > 1) {
      this.handleSearch(query);
    }
  };

  handleSelection = (selection: T.UserBase | Group) => {
    this.setState({ selection });
  };

  handleSubmit = (event: React.SyntheticEvent<HTMLFormElement>) => {
    event.preventDefault();
    const { selection } = this.state;
    if (selection) {
      this.props.onSubmit(selection);
    }
  };

  render() {
    const { submitting } = this.props;
    const { loading, query = '', searchResults, selection } = this.state;

    return (
      <QualityGatePermissionsAddModalRenderer
        loading={loading}
        onClose={this.props.onClose}
        onInputChange={this.handleInputChange}
        onSelection={this.handleSelection}
        onSubmit={this.handleSubmit}
        query={query}
        searchResults={searchResults}
        selection={selection}
        submitting={submitting}
      />
    );
  }
}
