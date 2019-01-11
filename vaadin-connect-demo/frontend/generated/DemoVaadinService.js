

import client from './connect-client.default';

/**
 * @param {number} number
 * @returns {Promise<number>}
 */
export function addOne(number) {
  return client.call('DemoVaadinService', 'addOne', {number}, {requireCredentials: true});
}

/**
 * @param {ComplexRequest} request
 * @returns {Promise<ComplexResponse>}
 */
export function complexEntitiesTest(request) {
  return client.call('DemoVaadinService', 'complexEntitiesTest', {request}, {requireCredentials: true});
}

export function deniedByClass() {
  return client.call('DemoVaadinService', 'deniedByClass', undefined, {requireCredentials: true});
}

/**
 * @returns {Promise<string>}
 */
export function hasAnonymousAccess() {
  return client.call('DemoVaadinService', 'hasAnonymousAccess', undefined, {requireCredentials: false});
}

export function noReturnNoArguments() {
  return client.call('DemoVaadinService', 'noReturnNoArguments', undefined, {requireCredentials: true});
}

export function permitRoleAdmin() {
  return client.call('DemoVaadinService', 'permitRoleAdmin', undefined, {requireCredentials: true});
}

/**
 * @returns {Promise<string>}
 */
export function throwsException() {
  return client.call('DemoVaadinService', 'throwsException', undefined, {requireCredentials: true});
}
