// Generated from GeneratorTestClass.java
import defaultClient from './connect-client.default';

/**
 * This class is used for OpenApi generator test
 */
export class GeneratorTestClass {

  /**
   * Create a GeneratorTestClass instance.
   * @param {ConnectClient=} client - an instance of ConnectClient
   */
  constructor(client = defaultClient) {
    this._client = client;
  }

  /**
   * Get number of users
   *
   * @returns {Promise<number>} Return number of user
   */
  countUser() {
    return this._client.call('GeneratorTestClass', 'countUser');
  }

  /**
   * Get the map of user and roles
   *
   * @returns {Promise<object>} Return map of user and roles
   */
  getAllUserRolesMap() {
    return this._client.call('GeneratorTestClass', 'getAllUserRolesMap');
  }

  /**
   * Get all users
   *
   * @returns {Promise<array>} Return list of users
   */
  getAllUsers() {
    return this._client.call('GeneratorTestClass', 'getAllUsers');
  }

  /**
   * Get array int
   *
   * @param {array} input - input string array
   * @returns {Promise<array>} Return array of int
   */
  getArrayInt(input) {
    return this._client.call('GeneratorTestClass', 'getArrayInt', {input});
  }

  /**
   * Get boolean value
   *
   * @param {object} input - input map
   * @returns {Promise<boolean>} Return boolean value
   */
  getBooleanValue(input) {
    return this._client.call('GeneratorTestClass', 'getBooleanValue', {input});
  }

  /**
   * Two parameters input method
   *
   * @param {string} input - first input description
   * @param {number} secondInput - second input description
   * @returns {Promise<boolean>} Return boolean value
   */
  getTwoParameters(input, secondInput) {
    return this._client.call('GeneratorTestClass', 'getTwoParameters', {input, secondInput});
  }

  /**
   * Get user by id
   *
   * @param {number} id - id of user
   * @returns {Promise<User>} Return user with given id
   */
  getUserById(id) {
    return this._client.call('GeneratorTestClass', 'getUserById', {id});
  }

  /**
   * Update a user
   *
   * @param {User} user
   *
   */
  updateUser(user) {
    return this._client.call('GeneratorTestClass', 'updateUser', {user});
  }
}

const service = new GeneratorTestClass(defaultClient);

export const countUser = service.countUser.bind(service);
export const getAllUserRolesMap = service.getAllUserRolesMap.bind(service);
export const getAllUsers = service.getAllUsers.bind(service);
export const getArrayInt = service.getArrayInt.bind(service);
export const getBooleanValue = service.getBooleanValue.bind(service);
export const getTwoParameters = service.getTwoParameters.bind(service);
export const getUserById = service.getUserById.bind(service);
export const updateUser = service.updateUser.bind(service);
