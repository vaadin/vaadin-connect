/**
 * This class is used for OpenApi generator test
 *
 * This module has been generated from GeneratorTestClass.java
 * @module GeneratorTestClass
 */

import client from './connect-client.default.js';

/**
 * Get number of users
 *
 * @returns {Promise<number>} Return number of user
 */
export function countUser() {
  return client.call('GeneratorTestClass', 'countUser', undefined, {requireCredentials: true});
}

/**
 * Get instant nano
 *
 * @param {number} input - input parameter
 * @returns {Promise<Instant>} Return current time as an Instant
 */
export function fullFQNMethod(input) {
  return client.call('GeneratorTestClass', 'fullFQNMethod', {input}, {requireCredentials: true});
}

/**
 * Get the map of user and roles
 *
 * @returns {Promise<object>} Return map of user and roles
 */
export function getAllUserRolesMap() {
  return client.call('GeneratorTestClass', 'getAllUserRolesMap', undefined, {requireCredentials: true});
}

/**
 * Get all users
 *
 * @returns {Promise<array>} Return list of users
 */
export function getAllUsers() {
  return client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: true});
}

/**
 * Get array int
 *
 * @param {array} input - input string array
 * @returns {Promise<array>} Return array of int
 */
export function getArrayInt(input) {
  return client.call('GeneratorTestClass', 'getArrayInt', {input}, {requireCredentials: false});
}

/**
 * Get boolean value
 *
 * @param {object} input - input map
 * @returns {Promise<boolean>} Return boolean value
 */
export function getBooleanValue(input) {
  return client.call('GeneratorTestClass', 'getBooleanValue', {input}, {requireCredentials: true});
}

/**
 * Two parameters input method
 *
 * @param {string} input - first input description
 * @param {number} secondInput - second input description
 * @returns {Promise<boolean>} Return boolean value
 */
export function getTwoParameters(input, secondInput) {
  return client.call('GeneratorTestClass', 'getTwoParameters', {input, secondInput}, {requireCredentials: false});
}

/**
 * Get user by id
 *
 * @param {number} id - id of user
 * @returns {Promise<User>} Return user with given id
 */
export function getUserById(id) {
  return client.call('GeneratorTestClass', 'getUserById', {id}, {requireCredentials: false});
}

/**
 * @param {boolean} _delete
 *
 */
export function reservedWordInParameter(_delete) {
  return client.call('GeneratorTestClass', 'reservedWordInParameter', {_delete}, {requireCredentials: true});
}

/**
 * Update a user
 *
 * @param {User} user
 *
 */
export function updateUser(user) {
  return client.call('GeneratorTestClass', 'updateUser', {user}, {requireCredentials: true});
}
