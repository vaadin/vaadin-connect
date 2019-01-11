/**
 * This nested class is also used in the OpenApi generator test
 *
 * This module has been generated from GeneratorAnonymousAllowedTestClass.java
 * @module GeneratorAnonymousAllowedTestClass
 */

import client from './connect-client.default.js';

export function anonymousAllowed() {
  return client.call('customName', 'anonymousAllowed', undefined, {requireCredentials: false});
}

export function permissionAltered1() {
  return client.call('customName', 'permissionAltered1', undefined, {requireCredentials: true});
}

export function permissionAltered2() {
  return client.call('customName', 'permissionAltered2', undefined, {requireCredentials: true});
}
