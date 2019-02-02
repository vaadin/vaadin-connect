import client from './connect-client.default.js';

/**
 * Get a collection by author name. The generator should not mix this type with the Java&#x27;s Collection type.
 *
 * @param {string} name - author name
 * @returns {Promise<Collection>} Return a collection
 */
export function getCollectionByAuthor(name) {
  return client.call('CollectionService', 'getCollectionByAuthor', {name}, {requireCredentials: true});
}

/**
 * Get a list of user name.
 *
 * @returns {Promise<array>} Return list of user name
 */
export function getListOfUserName() {
  return client.call('CollectionService', 'getListOfUserName', undefined, {requireCredentials: true});
}
