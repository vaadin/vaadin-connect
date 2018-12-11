/**
 * Throws a TypeError if the response is not 200 OK.
 * @param {Response} response The response to assert.
 * @private
 */
const assertResponseIsOk = response => {
  if (!response.ok) {
    throw new TypeError(
      `expected '200 OK' response, but got ${response.status}`
      + ` ${response.statusText}`
    );
  }
};

/** @private */
const tokens = new WeakMap();
/** @private */
const controllers = new WeakMap();

/** @private */
const refreshTokenKey = 'vaadin.connect.refreshToken';
/** @private */
const clientId = 'vaadin-connect-client';
/** @private */
const clientSecret = 'c13nts3cr3t';

/** @private */
class Token {
  /* global atob */
  constructor(token) {
    this.token = token;
    this.json = JSON.parse(atob(token.split('.')[1]));
  }
  isValid() {
    return this.json.exp > Date.now() / 1000;
  }
}

/** @private */
class AuthTokens {
  /* global localStorage */
  constructor(authJson) {
    if (authJson) {
      this.accessToken = new Token(authJson.access_token);
      this.refreshToken = new Token(authJson.refresh_token);
    }
  }
  save() {
    if (this.refreshToken) {
      localStorage.setItem(refreshTokenKey, this.refreshToken.token);
      this.stayLoggedIn = true;
    } else {
      localStorage.removeItem(refreshTokenKey);
    }
    return this;
  }
  restore() {
    const token = localStorage.getItem(refreshTokenKey);
    if (token) {
      try {
        this.refreshToken = new Token(token);
        this.stayLoggedIn = true;
      } catch (e) {
        // stored token is corrupted, remove it
        this.save();
      }
    }
    return this;
  }
}

/**
 * An object to provide user credentials for authorization grants.
 *
 * @typedef {Object} Credentials
 * @property {string} username
 * @property {string} password
 * @property {boolean} stayLoggedIn
 */

/**
 * A callback providing credentials for authorization.
 * @callback CredentialsCallback
 * @async
 * @param {Object} options
 * @param {string=} options.message When credentials are asked again, contains
 * the error description from last token response.
 * @returns {Credentials}
 */

/**
 * The Access Token structure returned by the authentication server.
 *
 * @typedef {Object} AccessToken
 * @property {String} user_name the user used in credentials
 * @property {Number} exp expiration time in seconds from since January 1, 1970
 * @property {Array<string>} authorities list of the roles that meets the token
 */

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
 * const client = new ConnectClient({endpoint: '/my-connect-endpoint'});
 * ```
 *
 * The default endpoint is '/connect'.
 *
 * ### Authorization
 *
 * The Connect client does OAuth 2 access token requests using
 * the `tokenEndpoint` constructor option.
 *
 * Supports the password credentials grant, which uses a username/password
 * pair provided by the `credentials` async callback constructor option:
 *
 * ```js
 * new ConnectClient({
 *   credentials: async() => {
 *     return {username: 'user', password: 'abc123'};
 *   }
 * });
 * ```
 *
 * The default token endpoint is '/oauth/token'.
 *
 * By default, the client requires authorization for calls, therefore
 * the `credentials` callback is called before a non-authorized client
 * is about to make a call. You can omit the authorization requirement using
 * the `requireCredentials: false` call option:
 *
 * ```js
 * const params = {};
 * await client.call('MyVaadinService', 'myMethod', params, {
 *   requireCredentials: false
 * });
 * ```
 */
export class ConnectClient {
  /**
   * @param {Object=} options={}
   * @param {string=} options.endpoint The `endpoint` initial value.
   * @param {string=} options.tokenEndpoint The `tokenEndpoint` initial value.
   * @param {CredentialsCallback=} options.credentials The `credentials` initial value.
   */
  constructor(options = {}) {
    /**
     * The Vaadin Connect backend endpoint.
     * @type {string}
     * @default '/connect'
     */
    this.endpoint = options.endpoint || '/connect';

    /**
     * The Vaadin Connect OAuth 2 token endpoint.
     * @type {string}
     * @default '/oauth/token'
     */
    this.tokenEndpoint = options.tokenEndpoint || '/oauth/token';

    /**
     * Called when the client needs a username/password pair to authorize through
     * the token endpoint. When undefined or returns a falsy value,
     * the authorization is skipped, the requests made by the `call` method
     * would not include the authorization header.
     * @type {CredentialsCallback}
     */
    this.credentials = options.credentials;

    tokens.set(this, new AuthTokens().restore());

    /* global AbortController */
    controllers.set(this, new AbortController());
  }

  /**
   * Remove current accessToken and refreshToken, and cancel any authentication request
   * that might be in progress.
   * After calling `logout()`, any new service call will ask for user credentials.
   */
  async logout() {
    controllers.get(this).abort();
    tokens.set(this, new AuthTokens().save());
  }

  /**
   * The access token returned by the authorization server.
   *
   * @type {AccessToken}
   */
  get token() {
    const token = tokens.get(this).accessToken;
    return token && Object.assign({}, token.json);
  }

  /**
   * Makes a JSON HTTP request to the `${endpoint}/${service}/${method}` URL,
   * optionally supplying the provided params as a JSON request body,
   * and asynchronously returns the parsed JSON response data.
   *
   * @param {string} service Service class name.
   * @param {string} method Method name to call in the service class.
   * @param {Object=} params Optional object to be send in JSON request body.
   * @param {Object=} options Optional client options for this call.
   * @param {boolean=true} options.requireCredentials Require authorization.
   * @returns {} Decoded JSON response data.
   */
  async call(service, method, params, options = {}) {
    if (arguments.length < 2) {
      throw new TypeError(
        `2 arguments required, but got only ${arguments.length}`
      );
    }

    options = Object.assign({requireCredentials: true}, options);
    if (options.requireCredentials) {
      await this.login();
    }

    const current = tokens.get(this);
    const headers = {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    };
    if (current.accessToken) {
      headers['Authorization'] = `Bearer ${current.accessToken.token}`;
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

    assertResponseIsOk(response);

    return response.json();
  }

  /**
   * Makes a HTTP request to the {@link ConnectClient#tokenEndpoint} URL
   * to login and get the accessToken if the current {@link ConnectClient#token}
   * is not available or invalid. The {@link ConnectClient#credentials}
   * will be called if the `refreshToken` is invalid.
   */
  async login() {
    let message;
    let current = tokens.get(this);
    while (!(current.accessToken && current.accessToken.isValid())) {

      let stayLoggedIn = current.stayLoggedIn;

      // delete current credentials because we are going to take new ones
      tokens.set(this, new AuthTokens().save());

      /* global URLSearchParams btoa */
      const body = new URLSearchParams();
      if (current.refreshToken && current.refreshToken.isValid()) {
        body.append('grant_type', 'refresh_token');
        body.append('client_id', clientId);
        body.append('refresh_token', current.refreshToken.token);
      } else if (this.credentials) {
        const creds = message !== undefined
          ? await this.credentials({message})
          : await this.credentials();
        if (!creds) {
          // No credentials returned, skip the token request
          break;
        }
        if (creds.username && creds.password) {
          body.append('grant_type', 'password');
          body.append('username', creds.username);
          body.append('password', creds.password);
          stayLoggedIn = creds.stayLoggedIn;
        }
      } else {
        break;
      }

      if (body.has('grant_type')) {
        const tokenResponse = await fetch(this.tokenEndpoint, {
          method: 'POST',
          signal: controllers.get(this).signal,
          headers: {
            'Authorization': `Basic ${btoa(clientId + ':' + clientSecret)}`,
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: body.toString()
        });

        if (tokenResponse.status === 400 || tokenResponse.status === 401) {
          const invalidResponse = await tokenResponse.json();
          if (invalidResponse.error === 'invalid_token') {
            current = new AuthTokens();
          }
          // Wrong credentials response, loop to ask again with the message
          message = invalidResponse.error_description;
        } else {
          assertResponseIsOk(tokenResponse);
          // Successful token response
          current = new AuthTokens(await tokenResponse.json());
          tokens.set(this, current);
          if (stayLoggedIn) {
            current.save();
          }
          break;
        }
      }
    }
  }
}
