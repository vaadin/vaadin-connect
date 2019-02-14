/**
 * This class is used for OpenApi generator test
 *
 * This module has been generated from JsonTestService.java
 * @module JsonTestService
 */

// @ts-ignore
import client from './connect-client.default.js';

/**
 * Object with unknown structure
 */
type Instant = object;

export interface User {
  name: string; 
  password: string; 
  roles: object; 
}

/**
 * Object with unknown structure
 */
type Version = object;

/**
 * Status bean. Used only in request parameters to verify that request
parameter type descriptions are generated.
 */
export interface Status {
  createdAt: Instant; 
  text: string; 
}

/**
 * Role bean
 */
export interface Role {
  roleName: string; 
}

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
  input: number
): Promise<Instant> {
  return client.call('JsonTestService', 'fullFQNMethod', {input});
}

/**
 * Get the map of user and roles
 *
 * Return map of user and roles
 */
export function getAllUserRolesMap(): Promise<object> {
  return client.call('JsonTestService', 'getAllUserRolesMap');
}

/**
 * Get all users
 *
 * Return list of users
 */
export function getAllUsers(): Promise<any[]> {
  return client.call('JsonTestService', 'getAllUsers');
}

/**
 * Get array int
 *
 * @param input input string array
 * Return array of int
 */
export function getArrayInt(
  input: array
): Promise<any[]> {
  return client.call('JsonTestService', 'getArrayInt', {input}, {requireCredentials: false});
}

/**
 * Get boolean value
 *
 * @param input input map
 * Return boolean value
 */
export function getBooleanValue(
  input: object
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
  input: string,

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
): Promise<User> {
  return client.call('JsonTestService', 'getUserById', {id}, {requireCredentials: false});
}

/**
 *
 */
export function inputBeanTypeDependency(
  input: Version
): Promise<void> {
  return client.call('JsonTestService', 'inputBeanTypeDependency', {input});
}

/**
 *
 */
export function inputBeanTypeLocal(
  input: Status
): Promise<void> {
  return client.call('JsonTestService', 'inputBeanTypeLocal', {input});
}

/**
 *
 */
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
  user: User
): Promise<void> {
  return client.call('JsonTestService', 'updateUser', {user});
}
