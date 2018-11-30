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
  isExpired() {
    return Date.now() / 1000 > this.json.exp;
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
      this.refreshToken = new Token(token);
      this.stayLoggedIn = true;
    }
    return this;
  }
  isAccessTokenExpired() {
    return !this.accessToken || this.accessToken.isExpired();
  }
  isRefreshTokenExpired() {
    return !this.refreshToken || this.refreshToken.isExpired();
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
 */
export class ConnectClient {
  /**
   * @param {Object=} options={}
   * @param {string=} options.endpoint The `endpoint` initial value.
   * @param {string=} options.tokenEndpoint The `tokenEndpoint` initial value.
   * @param {CredentialsCallback=} options.credentials The `credentials` initial value.
   */
  constructor(options = {}) {
    // TODO: remove this since tokens is a private object. Do we need
    // to maintain this API?
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
  }

  /**
   * Makes a JSON HTTP request to the `${endpoint}/${service}/${method}` URL,
   * optionally supplying the provided params as a JSON request body,
   * and asynchronously returns the parsed JSON response data.
   *
   * @param {string} service Service class name.
   * @param {string} method Method name to call in the service class.
   * @param {Object=} params Optional object to be send in JSON request body.
   * @returns {} Decoded JSON response data.
   */
  async call(service, method, params) {
    if (arguments.length < 2) {
      throw new TypeError(
        `2 arguments required, but got only ${arguments.length}`
      );
    }

    let message;
    while (tokens.get(this).isAccessTokenExpired()) {
      const prevTokens = tokens.get(this);
      let stayLoggedIn = prevTokens.stayLoggedIn;
      tokens.set(this, new AuthTokens().save());

      /* global URLSearchParams btoa */
      const body = new URLSearchParams();
      if (!prevTokens.isRefreshTokenExpired()) {
        body.append('grant_type', 'refresh_token');
        body.append('client_id', clientId);
        body.append('refresh_token', prevTokens.refreshToken.token);
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
          headers: {
            'Authorization': `Basic ${btoa(clientId + ':' + clientSecret)}`
          },
          body
        });

        if (tokenResponse.status === 400 || tokenResponse.status === 401) {
          // Wrong credentials response, loop to ask again with the message
          message = (await tokenResponse.json()).error_description;
        } else {
          assertResponseIsOk(tokenResponse);
          // Successful token response
          const json = await tokenResponse.json();
          this.accessToken = json.access_token;
          tokens.set(this, new AuthTokens(json));
          if (stayLoggedIn) {
            tokens.get(this).save();
          }
          break;
        }
      }
    }

    const headers = {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    };
    if (this.accessToken) {
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

    assertResponseIsOk(response);

    return response.json();
  }
}
