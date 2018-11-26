// Generated from GeneratorTestClass.java
import defaultClient from './connect-client.default.js';

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
   * @returns {number} Return number of user
   */
  countUser() {
    return await this._client.call('GeneratorTestClass', 'countUser');
  }

  /**
   * Get the map of user and roles
   *
   * @returns {object} Return map of user and roles
   */
  getAllUserRolesMap() {
    return await this._client.call('GeneratorTestClass', 'getAllUserRolesMap');
  }

  /**
   * Get all users
   *
   * @returns {array} Return list of users
   */
  getAllUsers() {
    return await this._client.call('GeneratorTestClass', 'getAllUsers');
  }

  /**
   * Get array int
   *
   * @param {array} input - input string array
   * @returns {array} Return array of int
   */
  getArrayInt(input) {
    return await this._client.call('GeneratorTestClass', 'getArrayInt', { input });
  }

  /**
   * Get boolean value
   *
   * @param {object} input - input map
   * @returns {boolean} Return boolean value
   */
  getBooleanValue(input) {
    return await this._client.call('GeneratorTestClass', 'getBooleanValue', { input });
  }

  /**
   * Two parameters input method
   *
   * @param {string} input - first input description
   * @param {number} secondInput - second input description
   * @returns {boolean} Return boolean value
   */
  getTwoParameters(input, secondInput) {
    return await this._client.call('GeneratorTestClass', 'getTwoParameters', { input, secondInput });
  }

  /**
   * Get user by id
   *
   * @param {number} id - id of user
   * @returns {User} Return user with given id
   */
  getUserById(id) {
    return await this._client.call('GeneratorTestClass', 'getUserById', { id });
  }

  /**
   * Update a user
   *
   * @param {User} user
   * @returns Request has been processed without any return result
   */
  updateUser(user) {
    return await this._client.call('GeneratorTestClass', 'updateUser', { user });
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
