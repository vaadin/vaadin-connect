// Generated from GeneratorAnonymousAllowedTestClass.java
import defaultClient from './connect-client.default';

/**
 * This nested class is also used in the OpenApi generator test
 */
export class GeneratorAnonymousAllowedTestClass {

  /**
   * Create a GeneratorAnonymousAllowedTestClass instance.
   * @param {ConnectClient=} client - an instance of ConnectClient
   */
  constructor(client = defaultClient) {
    this._client = client;
  }

  anonymousAllowed() {
    return this._client.call('GeneratorAnonymousAllowedTestClass', 'anonymousAllowed', undefined, {requireCredentials: false});
  }

  permissionAltered1() {
    return this._client.call('GeneratorAnonymousAllowedTestClass', 'permissionAltered1', undefined, {requireCredentials: true});
  }

  permissionAltered2() {
    return this._client.call('GeneratorAnonymousAllowedTestClass', 'permissionAltered2', undefined, {requireCredentials: true});
  }
}

const service = new GeneratorAnonymousAllowedTestClass(defaultClient);

export const anonymousAllowed = service.anonymousAllowed.bind(service);
export const permissionAltered1 = service.permissionAltered1.bind(service);
export const permissionAltered2 = service.permissionAltered2.bind(service);
