/**
 * This class is used for OpenApi generator test
 *
 * This module has been generated from GeneratorTestClass.java
 * @module GeneratorTestClass
 */

import client from './connect-client.default';

/**
 * @param {boolean} _delete
 *
 */
export function reservedWordInParameter(_delete) {
  return client.call('GeneratorTestClass', 'reservedWordInParameter', {_delete}, {requireCredentials: false});
}
