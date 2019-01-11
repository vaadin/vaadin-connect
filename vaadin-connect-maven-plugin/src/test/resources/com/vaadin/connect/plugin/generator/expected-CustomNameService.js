import client from './connect-client.default.js';

/**
 * @returns {Promise<number>}
 */
export function getNumber() {
  return client.call('customName', 'getNumber', undefined, {requireCredentials: true});
}
