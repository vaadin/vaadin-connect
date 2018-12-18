/**
 * This class is used
 * <h1>for OpenApi generator test</h1>
 *
 * This module has been generated from GeneratorTestClass.java
 * @module GeneratorTestClass
 */

import client from './connect-client.default';

/**
 * Get all users
 *
 * @returns {Promise<array>} Return list of users
 */
export function getAllUsers() {
  return client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: false});
}
