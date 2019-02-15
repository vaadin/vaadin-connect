// @ts-ignore
import client from './connect-client.default';

/**
 * Get all users
 *
 * Return list of users
 */
export function getAllUsers(): Promise<any[]> {
  return client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: false});
}
