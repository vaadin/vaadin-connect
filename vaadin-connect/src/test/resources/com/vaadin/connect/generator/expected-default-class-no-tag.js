import defaultClient from './connect-client.default.js';

export class Default {

  /**
   * Create a Default instance.
   * @param {ConnectClient=} client - an instance of ConnectClient
   */
  constructor(client = defaultClient) {
    this._client = client;
  }

  /**
   * Get all users
   *
   * @returns {array} Return list of users
   */
  getAllUsers() {
    return await this._client.call('GeneratorTestClass', 'getAllUsers');
  }

}

const service = new Default(defaultClient);

export const getAllUsers = service.getAllUsers.bind(service);
