import defaultClient from './connect-client.default';

export class MySecondJsClass {

  /**
   * Create a MySecondJsClass instance.
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
    return this._client.call('GeneratorTestClass', 'getAllUsers');
  }

}

const service = new MySecondJsClass(defaultClient);

export const getAllUsers = service.getAllUsers.bind(service);
