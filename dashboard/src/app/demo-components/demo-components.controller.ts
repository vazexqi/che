/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

enum Tab {
  FONT,
  PANEL,
  SELECTER,
  ICONS,
  BUTTONS,
  INPUT,
  LIST
}

/**
 * This class is handling the controller for the demo of components
 * @author Florent Benoit
 */
export class DemoComponentsController {
  $location: ng.ILocationService;
  $routeParams: ng.route.IRouteParamsService;

  selectedTabIndex: number = 0;
  tab: Object = Tab;

  booksByAuthor: {
    [author: string]: any[];
  };

  button2Disabled: boolean;

  listItemsDocs: string[];

  listItemsTasks: any[];

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, $routeParams: ng.route.IRouteParamsService) {
    this.$location = $location;

    let requestedDemoTab = ($routeParams as any).demo || Tab[Tab.FONT];
    this.selectedTabIndex = Tab[requestedDemoTab.toUpperCase()] || 0;

    this.init();
  }

  init() {
    // selecter
    this.booksByAuthor = {};
    this.booksByAuthor['St Exupery'] = [{title: 'The little prince'}];
    this.booksByAuthor['V. Hugo'] = [{title: 'Les miserables'}, {title: 'The Hunchback of Notre-Dame'}];
    this.booksByAuthor['A. Dumas'] = [{title: 'The count of Monte Cristo'}, {title: 'The Three Musketeers'}];


    this.button2Disabled = true;

    this.listItemsDocs = ['Document1', 'Document2', 'Document3', 'Document4', 'Document5'];

    this.listItemsTasks = [{name : 'Task 1', done: false}, {name : 'Task 2', done: true}, {name : 'Task 3', done: false},
      {name : 'Task 4', done: true}, {name : 'Task 5', done: false}];
  }

  /**
   * Use current tab as query parameter for this tab to be selected after demoComponents is reloaded.
   *
   * @param tabIndex {number} current tab index
   */
  setDemoTab(tabIndex: number): void {
    // remove any query from url
    this.$location.url(this.$location.path());

    this.$location.search('demo', Tab[tabIndex]);
  }

  toggleDisabled2() {
    this.button2Disabled = !this.button2Disabled;
  }

  isToggleDisabled2() {
    return this.button2Disabled;
  }


}
