/**
 * This class is used for OpenApi generator test
 *
 * This module has been generated from GeneratorTestClass.java
 * @module GeneratorTestClass
 */

// @ts-ignore
import client from './connect-client.default';

/**
 *
 */
export function complexEntitiesTest(
  request: ComplexRequest
): Promise<ComplexResponse> {
  return client.call('GeneratorTestClass', 'complexEntitiesTest', {request});
}
