/**
 * This class is used for OpenApi generator test
 *
 * This module has been generated from JsonTestService.java
 * @module JsonTestService
 */

// @ts-ignore
import client from './connect-client.default';
import Version from './com/fasterxml/jackson/core/Version';
import Status from './com/vaadin/connect/plugin/generator/services/json/JsonTestService/Status';
import User from './com/vaadin/connect/plugin/generator/services/json/JsonTestService/User';

/**
 * Get number of users
 *
 * Return number of user
 */
export function countUser(): Promise<number> {
  return client.call('JsonTestService', 'countUser');
}

/**
 * Get instant nano
 *
 * @param input input parameter
 * Return current time as an Instant
 */
export function fullFQNMethod(
  input: number | null
): Promise<string | null> {
  return client.call('JsonTestService', 'fullFQNMethod', {input});
}

/**
 * Get the map of user and roles
 *
 * Return map of user and roles
 */
export function getAllUserRolesMap(): Promise<{ [key: string]: User | null; } | null> {
  return client.call('JsonTestService', 'getAllUserRolesMap');
}

/**
 * Get all users
 *
 * Return list of users
 */
export function getAllUsers(): Promise<Array<User | null> | null> {
  return client.call('JsonTestService', 'getAllUsers');
}

/**
 * Get array int
 *
 * @param input input string array
 * Return array of int
 */
export function getArrayInt(
  input: Array<string | null> | null
): Promise<Array<number> | null> {
  return client.call('JsonTestService', 'getArrayInt', {input}, {requireCredentials: false});
}

/**
 * Get boolean value
 *
 * @param input input map
 * Return boolean value
 */
export function getBooleanValue(
  input: { [key: string]: User | null; } | null
): Promise<boolean> {
  return client.call('JsonTestService', 'getBooleanValue', {input});
}

/**
 * Two parameters input method
 *
 * @param input first input description
 * @param secondInput second input description
 * Return boolean value
 */
export function getTwoParameters(
  input: string | null,
  secondInput: number
): Promise<boolean> {
  return client.call('JsonTestService', 'getTwoParameters', {input, secondInput}, {requireCredentials: false});
}

/**
 * Get user by id
 *
 * @param id id of user
 * Return user with given id
 */
export function getUserById(
  id: number
): Promise<User | null> {
  return client.call('JsonTestService', 'getUserById', {id}, {requireCredentials: false});
}

export function inputBeanTypeDependency(
  input: Version | null
): Promise<void> {
  return client.call('JsonTestService', 'inputBeanTypeDependency', {input});
}

export function inputBeanTypeLocal(
  input: Status | null
): Promise<void> {
  return client.call('JsonTestService', 'inputBeanTypeLocal', {input});
}

export function optionalParameter(
  parameter: Array<string | null> | null,
  requiredParameter: string | null
): Promise<void> {
  return client.call('JsonTestService', 'optionalParameter', {parameter, requiredParameter});
}

export function optionalReturn(): Promise<User | null> {
  return client.call('JsonTestService', 'optionalReturn');
}

export function reservedWordInParameter(
  _delete: boolean
): Promise<void> {
  return client.call('JsonTestService', 'reservedWordInParameter', {_delete});
}

/**
 * Update a user
 *
 * @param user User to be updated
 *
 */
export function updateUser(
  user: User | null
): Promise<void> {
  return client.call('JsonTestService', 'updateUser', {user});
}
