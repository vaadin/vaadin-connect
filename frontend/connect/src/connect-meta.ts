/// <reference path="vaadin-usage-statistics.d.ts">
// tslint:disable-next-line:max-line-length
import {usageStatistics} from '@vaadin/vaadin-usage-statistics/vaadin-usage-statistics.js';

declare global {
  // tslint:disable-next-line:interface-name
  interface Window { Vaadin: any; }
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.registrations = window.Vaadin.registrations || [];
window.Vaadin.registrations.push({
  is: '@vaadin/connect',
  version: '0.4.0',
});

usageStatistics();
