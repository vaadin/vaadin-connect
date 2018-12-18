/**
 * This class is used for OpenApi generator test
 *
 * This module has been generated from GeneratorTestClass.java
 * @module GeneratorTestClass
 */

import client from './connect-client.default';

/**
 * @param {ComplexRequest} request
 * @returns {Promise<ComplexResponse>}
 */
export function complexEntitiesTest(request) {
  return client.call('GeneratorTestClass', 'complexEntitiesTest', {request}, {requireCredentials: true});
}
