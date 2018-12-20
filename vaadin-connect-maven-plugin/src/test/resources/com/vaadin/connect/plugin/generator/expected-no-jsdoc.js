/**
 * This class is used for OpenApi generator test
 *
 * This module has been generated from GeneratorTestClass.java
 * @module GeneratorTestClass
 */

import client from './connect-client.default';

export function getAllUsers() {
  return client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: false});
}
