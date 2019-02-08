// @ts-ignore
import client from './connect-client.default.js';

/**
 * Get all users
 *
 * Return list of users
 */
export function getAllUsers(): Promise<array> {
  return client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: false});
}