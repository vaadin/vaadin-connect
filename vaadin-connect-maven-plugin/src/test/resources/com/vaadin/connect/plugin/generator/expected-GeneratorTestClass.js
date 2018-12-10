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
    return this._client.call('GeneratorTestClass', 'countUser', undefined, {requiredCredentials: false});
  }

  /**
   * Get instant nano
   *
   * @param {number} input - input parameter
   * @returns {Promise<Instant>} Return current time as an Instant
   */
  fullFQNMethod(input) {
    return this._client.call('GeneratorTestClass', 'fullFQNMethod', {input}, {requiredCredentials: false});
  }

  /**
   * Get the map of user and roles
   *
   * @returns {Promise<object>} Return map of user and roles
   */
  getAllUserRolesMap() {
    return this._client.call('GeneratorTestClass', 'getAllUserRolesMap', undefined, {requiredCredentials: false});
  }

  /**
   * Get all users
   *
   * @returns {Promise<array>} Return list of users
   */
  getAllUsers() {
    return this._client.call('GeneratorTestClass', 'getAllUsers', undefined, {requiredCredentials: false});
  }

  /**
   * Get array int
   *
   * @param {array} input - input string array
   * @returns {Promise<array>} Return array of int
   */
  getArrayInt(input) {
    return this._client.call('GeneratorTestClass', 'getArrayInt', {input}, {requiredCredentials: false});
  }

  /**
   * Get boolean value
   *
   * @param {object} input - input map
   * @returns {Promise<boolean>} Return boolean value
   */
  getBooleanValue(input) {
    return this._client.call('GeneratorTestClass', 'getBooleanValue', {input}, {requiredCredentials: false});
  }

  /**
   * Two parameters input method
   *
   * @param {string} input - first input description
   * @param {number} secondInput - second input description
   * @returns {Promise<boolean>} Return boolean value
   */
  getTwoParameters(input, secondInput) {
    return this._client.call('GeneratorTestClass', 'getTwoParameters', {input, secondInput}, {requiredCredentials: false});
  }

  /**
   * Get user by id
   *
   * @param {number} id - id of user
   * @returns {Promise<User>} Return user with given id
   */
  getUserById(id) {
    return this._client.call('GeneratorTestClass', 'getUserById', {id}, {requiredCredentials: false});
  }

  /**
   * Update a user
   *
   * @param {User} user
   *
   */
  updateUser(user) {
    return this._client.call('GeneratorTestClass', 'updateUser', {user}, {requiredCredentials: false});
  }
}

const service = new GeneratorTestClass(defaultClient);

export const countUser = service.countUser.bind(service);
export const fullFQNMethod = service.fullFQNMethod.bind(service);
export const getAllUserRolesMap = service.getAllUserRolesMap.bind(service);
export const getAllUsers = service.getAllUsers.bind(service);
export const getArrayInt = service.getArrayInt.bind(service);
export const getBooleanValue = service.getBooleanValue.bind(service);
export const getTwoParameters = service.getTwoParameters.bind(service);
export const getUserById = service.getUserById.bind(service);
export const updateUser = service.updateUser.bind(service);
