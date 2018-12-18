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


export function anonymousAllowed() {
  return service.anonymousAllowed.apply(service, arguments);
}

export function permissionAltered1() {
  return service.permissionAltered1.apply(service, arguments);
}

export function permissionAltered2() {
  return service.permissionAltered2.apply(service, arguments);
}
