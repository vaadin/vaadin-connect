import defaultClient from './connect-client.default';

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
   * @returns {Promise<array>} Return list of users
   */
  getAllUsers() {
    return this._client.call('GeneratorTestClass', 'getAllUsers', undefined, {requiredCredentials: false});
  }
}

const service = new Default(defaultClient);

export const getAllUsers = service.getAllUsers.bind(service);
