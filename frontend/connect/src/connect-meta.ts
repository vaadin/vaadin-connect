import {usageStatistics} from '@vaadin/vaadin-usage-statistics/vaadin-usage-statistics.js';

(<any>window).Vaadin = (<any>window).Vaadin || {};
(<any>window).Vaadin.registrations = (<any>window).Vaadin.registrations || [];
(<any>window).Vaadin.registrations.push({
  is: '@vaadin/connect',
  version: '0.4.0',
});

usageStatistics();
