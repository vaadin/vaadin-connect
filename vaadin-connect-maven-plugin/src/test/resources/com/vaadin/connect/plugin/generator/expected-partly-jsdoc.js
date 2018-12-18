// Generated from GeneratorTestClass.java
import defaultClient from './connect-client.default';

/**
 * This class is used for OpenApi generator test
 */
export class GeneratorTestClass {

  /**
   * Create a GeneratorTestClass instance.
   * @param {ConnectClient=} client - an instance of ConnectClient
   */
  constructor(client = defaultClient) {
    this._client = client;
  }

  /**
   * @param {ComplexRequest} request
   * @returns {Promise<ComplexResponse>}
   */
  complexEntitiesTest(request) {
    return this._client.call('GeneratorTestClass', 'complexEntitiesTest', {request}, {requireCredentials: true});
  }
}

const service = new GeneratorTestClass(defaultClient);


/**
 * @param {ComplexRequest} request
 * @returns {Promise<ComplexResponse>}
 */
export function complexEntitiesTest(request) {
  return service.complexEntitiesTest.apply(service, arguments);
}