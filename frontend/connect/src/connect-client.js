/**
 * Vaadin Connect client class is a low-level network calling utility. It stores
 * an endpoint and facilitates remote calls to services and methods
 * on the Vaadin Connect backend.
 *
 * ```js
 * const client = new ConnectClient();
 * const responseData = await client.call('MyVaadinService', 'myMethod');
 * ```
 *
 * Supports an `endpoint` constructor option:
 * ```js
 * const client = new ConnectClient({endpoint: '/my-connect-endpont'});
 * ```
 *
 * The default endpoint is '/connect'.
 */
export class ConnectClient {
  /**
   * @param {Object=} options={}
   * @param {string=} options.endpoint The `endpoint` initial value.
   */
  constructor(options = {}) {
    /**
     * When set to a string, adds the `Authorization: Bearer ${accessToken}`
     * HTTP reader to every request.
     * @type {string}
     */
    this.accessToken;

    /**
     * The Vaadin Connect backend endpoint.
     * @type {string}
     * @default '/connect'
     */
    this.endpoint = options.endpoint || '/connect';

    /**
     * The Vaadin Connect token endpoint.
     * @type {string}
     * @default '/oauth/token'
     */
    this.tokenEndpoint = options.tokenEndpoint || '/oauth/token';

    /**
     * The async method to call when needed a username/password pair
     *
     * @type {Function}
     * @default 'undefined, meaning to proceed with the call without adding an auth header'
     */
    this.credentials = options.credentials;

    // TODO: remove these when #58
    this._clientId = 'vaadin-connect-client';
    this._clientSecret = 'c13nts3cr3t';
  }

  async getToken(userOrRefresh, password) {
    const params = new window.URLSearchParams();
    params.append('username', userOrRefresh);
    params.append('password', password);
    params.append('grant_type', 'password');

    const response = await fetch(this.tokenEndpoint, {
      method: 'POST',
      headers: {
        'Authorization': `Basic ${window.btoa(this._clientId + ':' + this._clientSecret)}`
      },
      body: params
    });

    this.accessToken = undefined;

    if (response.status == 200) {
      this.accessToken = (await response.json()).access_token;
    }

    return response;
  }

  /**
   * Makes a JSON HTTP request to the `${endpoint}/${service}/${method}` URL,
   * optionally supplying the provided params as a JSON request body,
   * and asynchronously returns the parsed JSON response data.
   *
   * @param {string} service Service class name.
   * @param {string} method Method name to call in the service class.
   * @param {Object=} params Optional object to be send in JSON request body.
   * @return {Promise<Object>} Decoded JSON response data.
   */
  async call(service, method, params) {
    if (arguments.length < 2) {
      throw new TypeError(
        `2 arguments required, but got only ${arguments.length}`
      );
    }

    if (!this.accessToken && this.credentials) {
      let message = 'Please login';
      while (!this.accessToken) {
        const creds = await this.credentials(message ? message : 'Please login');
        if (!creds) {
          break;
        }
        if (creds.username && creds.password) {
          const response = await this.getToken(creds.username, creds.password);
          if (response.status >= 400 && response.status < 500) {
            message = (await response.json()).error_description;
          } else if (!this.accessToken) {
            window.console.error(`${response.statusText} ${response.body}`);
            break;
          }
        }
      }
    }

    const headers = {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    };

    if (this.accessToken !== undefined) {
      headers['Authorization'] = `Bearer ${this.accessToken}`;
    }

    /* global fetch */
    const response = await fetch(
      `${this.endpoint}/${service}/${method}`,
      {
        method: 'POST',
        headers,
        body: params !== undefined ? JSON.stringify(params) : undefined
      }
    );

    if (!response.ok) {
      throw new TypeError(
        `expected '200 OK' response, but got ${response.status}`
        + ` ${response.statusText}`
      );
    }

    return response.json();
  }
}
