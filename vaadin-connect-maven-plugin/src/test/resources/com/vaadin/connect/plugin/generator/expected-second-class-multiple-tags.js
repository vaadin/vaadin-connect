import client from './connect-client.default.js';

/**
 * Get all users
 *
 * @returns {Promise<array>} Return list of users
 */
export function getAllUsers() {
  return client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: false});
}
