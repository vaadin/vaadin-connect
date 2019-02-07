/**
 * This class is used for OpenApi generator test
 *
 * This module has been generated from JsonTestService.java
 * @module JsonTestService
 */

import client from './connect-client.default.js';

/**
 * Status bean. Used only in request parameters to verify that request
parameter type descriptions are generated.
 *
 * @typedef {object} Status
 * @param {Instant} createdAt
 * @param {string} text
 */

/**
 * @typedef {object} User
 * @param {string} name
 * @param {string} password
 * @param {object} roles
 */

/**
 * Role bean
 *
 * @typedef {object} Role
 * @param {string} roleName
 */

/**
 * Get number of users
 *
 * @returns {Promise<number>} Return number of user
 */
export function countUser() {
  return client.call('JsonTestService', 'countUser', undefined, {requireCredentials: true});
}

/**
 * Get instant nano
 *
 * @param {number} input - input parameter
 * @returns {Promise<Instant>} Return current time as an Instant
 */
export function fullFQNMethod(input) {
  return client.call('JsonTestService', 'fullFQNMethod', {input}, {requireCredentials: true});
}

/**
 * Get the map of user and roles
 *
 * @returns {Promise<object>} Return map of user and roles
 */
export function getAllUserRolesMap() {
  return client.call('JsonTestService', 'getAllUserRolesMap', undefined, {requireCredentials: true});
}

/**
 * Get all users
 *
 * @returns {Promise<array>} Return list of users
 */
export function getAllUsers() {
  return client.call('JsonTestService', 'getAllUsers', undefined, {requireCredentials: true});
}

/**
 * Get array int
 *
 * @param {array} input - input string array
 * @returns {Promise<array>} Return array of int
 */
export function getArrayInt(input) {
  return client.call('JsonTestService', 'getArrayInt', {input}, {requireCredentials: false});
}

/**
 * Get boolean value
 *
 * @param {object} input - input map
 * @returns {Promise<boolean>} Return boolean value
 */
export function getBooleanValue(input) {
  return client.call('JsonTestService', 'getBooleanValue', {input}, {requireCredentials: true});
}

/**
 * Two parameters input method
 *
 * @param {string} input - first input description
 * @param {number} secondInput - second input description
 * @returns {Promise<boolean>} Return boolean value
 */
export function getTwoParameters(input, secondInput) {
  return client.call('JsonTestService', 'getTwoParameters', {input, secondInput}, {requireCredentials: false});
}

/**
 * Get user by id
 *
 * @param {number} id - id of user
 * @returns {Promise<User>} Return user with given id
 */
export function getUserById(id) {
  return client.call('JsonTestService', 'getUserById', {id}, {requireCredentials: false});
}

/**
 * @param {Version} input
 *
 */
export function inputBeanTypeDependency(input) {
  return client.call('JsonTestService', 'inputBeanTypeDependency', {input}, {requireCredentials: true});
}

/**
 * @param {Status} input
 *
 */
export function inputBeanTypeLocal(input) {
  return client.call('JsonTestService', 'inputBeanTypeLocal', {input}, {requireCredentials: true});
}

/**
 * @param {boolean} _delete
 *
 */
export function reservedWordInParameter(_delete) {
  return client.call('JsonTestService', 'reservedWordInParameter', {_delete}, {requireCredentials: true});
}

/**
 * Update a user
 *
 * @param {User} user - User to be updated
 *
 */
export function updateUser(user) {
  return client.call('JsonTestService', 'updateUser', {user}, {requireCredentials: true});
}
