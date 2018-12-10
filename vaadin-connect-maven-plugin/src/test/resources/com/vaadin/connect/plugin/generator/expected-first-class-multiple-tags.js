import defaultClient from './connect-client.default';

export class MyFirstJsClass {

  /**
   * Create a MyFirstJsClass instance.
   * @param {ConnectClient=} client - an instance of ConnectClient
   */
  constructor(client = defaultClient) {
    this._client = client;
  }

  /**
   * Get all users
   *
   * @returns {Promise<array>} Return list of users
   */
  getAllUsers() {
    return this._client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: false});
  }
}

const service = new MyFirstJsClass(defaultClient);

export const getAllUsers = service.getAllUsers.bind(service);
