/**
 * This nested class is also used in the OpenApi generator test
 *
 * This module has been generated from GeneratorAnonymousAllowedTestClass.java
 * @module GeneratorAnonymousAllowedTestClass
 */

import client from './connect-client.default';

export function anonymousAllowed() {
  return client.call('GeneratorAnonymousAllowedTestClass', 'anonymousAllowed', undefined, {requireCredentials: false});
}

export function permissionAltered1() {
  return client.call('GeneratorAnonymousAllowedTestClass', 'permissionAltered1', undefined, {requireCredentials: true});
}

export function permissionAltered2() {
  return client.call('GeneratorAnonymousAllowedTestClass', 'permissionAltered2', undefined, {requireCredentials: true});
}
