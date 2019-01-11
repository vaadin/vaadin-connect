/**
 * This class is used for OpenApi generator test
 *
 * This module has been generated from FooBarService.java
 * @module FooBarService
 */

import client from './connect-client.default.js';

/**
 * @param {boolean} value
 *
 */
export function firstMethod(value) {
  return client.call('FooBarService', 'firstMethod', {value}, {requireCredentials: false});
}
