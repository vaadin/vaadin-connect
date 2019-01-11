const {describe, it, beforeEach, afterEach} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');
const {fetchMock} = intern.getPlugin('fetchMock');
const {sinon} = intern.getPlugin('sinon');

import {ConnectClient, VaadinConnectException} from '../src/connect-client.js';

/* global btoa localStorage setTimeout URLSearchParams */
describe('ConnectClient', () => {

  function generateOAuthJson() {
    const jwt = btoa('{"alg": "HS256", "typ": "JWT"}');
    // expiration comes in seconds from Vaadin Connect Server
    // We add 400ms to accessToken and 800ms to refreshToken
    const accessToken = btoa(`{"exp": ${Date.now() / 1000 + 0.400}, "user_name": "foo"}`);
    const refreshToken = btoa(`{"exp": ${Date.now() / 1000 + 0.800}}`);

    return {
      access_token: `${jwt}.${accessToken}.SIGNATURE`,
      refresh_token: `${jwt}.${refreshToken}.SIGNATURE`,
      exp: 10
    };
  }

  async function sleep(ms) {
    await new Promise(resolve => setTimeout(resolve, ms));
  }

  beforeEach(() => localStorage.clear());

  it('should be exported', () => {
    expect(ConnectClient).to.be.ok;
  });

  it('should instantiate without arguments', () => {
    const client = new ConnectClient();
    expect(client).to.be.instanceOf(ConnectClient);
  });

  describe('constructor options', () => {
    it('should support endpoint', () => {
      const client = new ConnectClient({endpoint: '/foo'});
      expect(client).to.have.property('endpoint', '/foo');
    });

    it('should support tokenEndpoint', () => {
      const client = new ConnectClient({tokenEndpoint: '/foo'});
      expect(client).to.have.property('tokenEndpoint', '/foo');
    });
  });

  describe('endpoint', () => {
    it('should have default endpoint', () => {
      const client = new ConnectClient();
      expect(client).to.have.property('endpoint', '/connect');
    });

    it('should allow setting new endpoint', () => {
      const client = new ConnectClient();
      client.endpoint = '/foo';
      expect(client).to.have.property('endpoint', '/foo');
    });
  });

  describe('call method', () => {
    beforeEach(() => fetchMock
      .post('/connect/FooService/fooMethod', {fooData: 'foo'})
    );

    afterEach(() => fetchMock.restore());

    let client;
    beforeEach(() => client = new ConnectClient());

    it('should require 2 arguments', async() => {
      try {
        await client.call();
      } catch (err) {
        expect(err).to.be.instanceOf(TypeError)
          .and.have.property('message').that.has.string('2 arguments required');
      }

      try {
        await client.call('FooService');
      } catch (err) {
        expect(err).to.be.instanceOf(TypeError)
          .and.have.property('message').that.has.string('2 arguments required');
      }
    });

    it('should fetch service and method from default endpoint', async() => {
      expect(fetchMock.calls()).to.have.lengthOf(0); // no premature requests

      await client.call('FooService', 'fooMethod');

      expect(fetchMock.calls()).to.have.lengthOf(1);
      expect(fetchMock.lastUrl()).to.equal('/connect/FooService/fooMethod');
    });

    it('should return Promise', () => {
      const returnValue = client.call('FooService', 'fooMethod');
      expect(returnValue).to.be.a('promise');
    });

    it('should use POST request', async() => {
      await client.call('FooService', 'fooMethod');

      expect(fetchMock.lastOptions()).to.include({method: 'POST'});
    });

    it('should use JSON request headers', async() => {
      await client.call('FooService', 'fooMethod');

      const headers = fetchMock.lastOptions().headers;
      expect(headers).to.include({
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      });
    });

    it('should resolve to response JSON data', async() => {
      const data = await client.call('FooService', 'fooMethod');
      expect(data).to.deep.equal({fooData: 'foo'});
    });

    it('should reject if response is not ok', async() => {
      fetchMock.post('/connect/FooService/notFound', 404);
      try {
        await client.call('FooService', 'notFound');
      } catch (err) {
        expect(err).to.be.instanceOf(VaadinConnectException)
          .and.have.property('message').that.has.string('404 Not Found');
      }
    });

    it('should reject if fetch is rejected', async() => {
      fetchMock.post(
        '/connect/FooService/reject',
        Promise.reject(new TypeError('Network failure'))
      );

      try {
        await client.call('FooService', 'reject');
      } catch (err) {
        expect(err).to.be.instanceOf(TypeError)
          .and.have.property('message').that.has.string('Network failure');
      }
    });

    it('should fetch from custom endpoint', async() => {
      fetchMock.post('/fooEndpoint/BarService/barMethod', {barData: 'bar'});

      client.endpoint = '/fooEndpoint';
      const data = await client.call('BarService', 'barMethod');

      expect(data).to.deep.equal({barData: 'bar'});
      expect(fetchMock.lastUrl()).to.equal('/fooEndpoint/BarService/barMethod');
    });

    it('should pass 3rd argument as JSON request body', async() => {
      await client.call('FooService', 'fooMethod', {fooParam: 'foo'});

      const requestBody = fetchMock.lastOptions().body;
      expect(requestBody).to.be.a('string');
      expect(JSON.parse(requestBody)).to.deep.equal({fooParam: 'foo'});
    });
  });

  describe('login method', () => {
    let client;

    beforeEach(() => {
      client = new ConnectClient({credentials: sinon.fake
        .returns({username: 'user', password: 'abc123'})});
      fetchMock.post(client.tokenEndpoint, generateOAuthJson);
    });

    afterEach(() => {
      fetchMock.restore();
    });

    it('should request token endpoint with credentials when calling login', async() => {

      await client.login();

      const [[url, {method, headers, body}]] = fetchMock.calls();

      // TODO: remove when #58
      expect(headers).to.have.property('Authorization');

      expect(method).to.equal('POST');
      expect(url).to.equal('/oauth/token');
      expect(body.toString())
        .to.equal('grant_type=password&username=user&password=abc123');
    });

    it('should request token endpoint only once after login', async() => {
      const vaadinEndpoint = '/connect/FooService/fooMethod';
      fetchMock.post(vaadinEndpoint, {fooData: 'foo'});
      await client.login();
      await client.call('FooService', 'fooMethod');

      expect(fetchMock.calls()).to.have.lengthOf(2);
      expect(fetchMock.calls()[0][0]).to.be.equal(client.tokenEndpoint);
      expect(fetchMock.calls()[1][0]).to.be.equal(vaadinEndpoint);
    });

    it('should use refreshToken if available', async() => {
      localStorage.setItem('vaadin.connect.refreshToken', generateOAuthJson().refresh_token);
      const newClient = new ConnectClient({credentials: client.credentials});
      await newClient.login();

      expect(fetchMock.calls()).to.have.lengthOf(1);
      expect(newClient.credentials).not.to.be.called;

      let [, {body}] = fetchMock.calls()[0];
      body = new URLSearchParams(body);
      expect(body.get('grant_type')).to.be.equal('refresh_token');
    });
  });

  describe('tokenEndpoint', () => {
    it('should have default tokenEndpoint', () => {
      expect(new ConnectClient())
        .to.have.property('tokenEndpoint', '/oauth/token');
    });

    it('should allow setting new tokenEndpoint', () => {
      const client = new ConnectClient();
      client.tokenEndpoint = '/foo';
      expect(client).to.have.property('tokenEndpoint', '/foo');
    });
  });

  describe('credentials', () => {
    let client;
    const vaadinEndpoint = '/connect/FooService/fooMethod';

    beforeEach(() => {
      client = new ConnectClient();
      fetchMock.post(vaadinEndpoint, {fooData: 'foo'});
    });

    afterEach(() => {
      fetchMock.restore();
    });

    describe('without credentials', () => {
      it('should not include Authorization header by default', async() => {
        await client.call('FooService', 'fooMethod');
        expect(fetchMock.lastOptions().headers)
          .to.not.have.property('Authorization');
      });
    });

    describe('with credentials', () => {
      beforeEach(() => {
        const credentials = sinon.fake
          .returns({username: 'user', password: 'abc123'});
        client.credentials = credentials;
      });

      it('should ask for credentials when accessToken is missing', async() => {
        fetchMock.post(client.tokenEndpoint, generateOAuthJson);
        await client.call('FooService', 'fooMethod');
        expect(client.credentials).to.be.calledOnce;
        expect(client.credentials.lastCall).to.be.calledWithExactly();
      });

      it('should not request token endpoint when credentials are falsy', async() => {
        for (const falsy of [false, '', 0, null, undefined, NaN]) {
          client.credentials = sinon.fake.returns(falsy);
          await client.call('FooService', 'fooMethod');
          expect(fetchMock.lastUrl()).to.not.equal(client.tokenEndpoint);
        }
      });

      it('should ask for credencials again when one is missing', async() => {
        client.credentials = sinon.stub();
        client.credentials.onCall(0).returns({password: 'abc123'});
        client.credentials.onCall(1).returns({username: 'user'});
        client.credentials.onCall(2).returns(false);

        await client.call('FooService', 'fooMethod');
        expect(client.credentials).to.be.calledThrice;
      });

      it('should request token endpoint with credentials', async() => {
        fetchMock.post(client.tokenEndpoint, generateOAuthJson);

        await client.call('FooService', 'fooMethod');

        const [[url, {method, headers, body}]] = fetchMock.calls();

        // TODO: remove when #58
        expect(headers).to.have.property('Authorization');

        expect(method).to.equal('POST');
        expect(url).to.equal('/oauth/token');
        expect(body.toString())
          .to.equal('grant_type=password&username=user&password=abc123');
      });

      it('should require credentials when requireCredentials is not specified but other options are', async() => {
        fetchMock.post(client.tokenEndpoint, generateOAuthJson);

        await client.call('FooService', 'fooMethod', undefined, {someOtherOption: true});

        const [[url, {method, headers, body}]] = fetchMock.calls();

        // TODO: remove when #58
        expect(headers).to.have.property('Authorization');

        expect(method).to.equal('POST');
        expect(url).to.equal('/oauth/token');
        expect(body.toString())
          .to.equal('grant_type=password&username=user&password=abc123');
      });

      it('should expose accessToken data', async() => {
        fetchMock.post(client.tokenEndpoint, generateOAuthJson);

        await client.call('FooService', 'fooMethod');
        expect(client.token).to.be.ok;
        expect(client.token.exp).to.be.above(Date.now() / 1000);
        expect(client.token.user_name).to.be.equal('foo');
      });

      it('should not be able to modify accessToken data', async() => {
        fetchMock.post(client.tokenEndpoint, generateOAuthJson);

        await client.call('FooService', 'fooMethod');
        client.token.user_name = 'bar';
        expect(client.token.user_name).to.be.equal('foo');
      });

      it('should ask for credentials again when token response is 400 or 401', async() => {
        client.credentials = sinon.stub();
        client.credentials.onCall(0).returns({username: 'user', password: 'abc123'});
        client.credentials.onCall(1).returns({username: 'user', password: 'abc123'});
        client.credentials.onCall(2).returns(false);

        fetchMock
          .post(client.tokenEndpoint,
            {body: {error: 'invalid_grant', error_description: 'Bad credentials'}, status: 400},
            {repeat: 1})
          .post(client.tokenEndpoint,
            {body: {error: 'unathorized', error_description: 'Unauthorized'}, status: 401},
            {repeat: 1, overwriteRoutes: false});

        const data = await client.call('FooService', 'fooMethod');
        expect(data).to.deep.equal({fooData: 'foo'});

        expect(fetchMock.calls().length).to.be.equal(3);

        expect(client.credentials).to.be.calledThrice;
        expect(client.credentials.getCall(0)).to.be.calledWithExactly();
        expect(client.credentials.getCall(1).args)
          .to.deep.equal([{message: 'Bad credentials'}]);
        expect(client.credentials.getCall(2).args)
          .to.deep.equal([{message: 'Unauthorized'}]);

        expect(fetchMock.calls()[0][0]).to.be.equal(client.tokenEndpoint);
        expect(fetchMock.calls()[1][0]).to.be.equal(client.tokenEndpoint);
        expect(fetchMock.calls()[2][0]).to.be.equal(vaadinEndpoint);
        expect(fetchMock.calls()[2][1].headers).not.to.have.property('Authorization');
      });

      it('should throw when token response is bad', async() => {
        const expectedBody = 'Server Internal Error';
        fetchMock.post(
          client.tokenEndpoint,
          {body: expectedBody, status: 500}
        );

        try {
          await client.call('FooService', 'fooMethod');
        } catch (err) {
          expect(err).to.be.instanceOf(VaadinConnectException)
            .and.have.property('message')
            .that.has.string(expectedBody);
          expect(client.credentials).to.be.calledOnce;
        }
      });

      it('should use accessToken when token response is ok', async() => {
        const response = generateOAuthJson();
        fetchMock.post(client.tokenEndpoint, response);

        const data = await client.call('FooService', 'fooMethod');

        let [url, {method, headers, body}] = fetchMock.calls()[0];
        body = new URLSearchParams(body);
        expect(body.get('grant_type')).to.be.equal('password');
        expect(body.get('username')).to.be.equal('user');
        expect(body.get('password')).to.be.equal('abc123');

        [url, {method, headers, body}] = fetchMock.calls()[1];
        expect(method).to.equal('POST');
        expect(url).to.equal('/connect/FooService/fooMethod');
        expect(headers).to.have.property('Authorization', `Bearer ${response.access_token}`);
        expect(data).to.deep.equal({fooData: 'foo'});
      });

      describe('refreshToken', () => {
        beforeEach(async() => {
          fetchMock.post(client.tokenEndpoint, generateOAuthJson);
        });

        it('should re-use login promise', async() => {
          await Promise.all([
            client.login(),
            client.login(),
            client.login(),
            client.login()]);
          expect(client.credentials).to.be.calledOnce;
        });

        it('should re-use login in multiple calls', async() => {
          await Promise.all([
            client.call('FooService', 'fooMethod'),
            client.call('FooService', 'fooMethod')]);
          expect(client.credentials).to.be.calledOnce;
        });

        it('should not call credentials if another auth request is pending', async() => {
          // do a First request to get an accessToken and a refreshToken
          await client.call('FooService', 'fooMethod');

          // Wait until accessToken expires but not the refreshToken
          // generated response has a expiration of 400ms for token and 800 for refresh
          await sleep(600);
          const call1 = client.call('FooService', 'fooMethod');
          const call2 = client.call('FooService', 'fooMethod');

          const [data1, data2] = await Promise.all([call1, call2]);

          expect(data1).to.deep.equal({fooData: 'foo'});
          expect(data2).to.deep.equal({fooData: 'foo'});
          expect(client.credentials).to.be.calledOnce;
          expect(fetchMock.calls().length).to.be.equal(5);
        });

        it('should use refreshToken when accessToken is expired', async() => {
          // do a First request to get an accessToken and a refreshToken
          await client.call('FooService', 'fooMethod');

          // Wait until accessToken expires but not the refreshToken
          // generated response has a expiration of 400ms for token and 800 for refresh
          await sleep(600);
          const data = await client.call('FooService', 'fooMethod');

          expect(data).to.deep.equal({fooData: 'foo'});
          expect(client.credentials).to.be.calledOnce;
          expect(fetchMock.calls().length).to.be.equal(4);

          let [, {body}] = fetchMock.calls()[2];
          body = new URLSearchParams(body);
          expect(body.get('grant_type')).to.be.equal('refresh_token');
          expect(body.get('client_id')).to.be.equal('vaadin-connect-client');
          expect(body.get('refresh_token')).to.be.ok;
          expect(body.get('username')).to.be.null;
        });

        it('should call credentials if refreshToken is expired', async() => {
          // do a First request to get an accessToken and a refreshToken
          await client.call('FooService', 'fooMethod');

          // Wait until both accessToken and refresToken expire
          await sleep(1000);
          const data = await client.call('FooService', 'fooMethod');

          expect(data).to.deep.equal({fooData: 'foo'});
          expect(client.credentials).to.be.calledTwice;
          expect(fetchMock.calls().length).to.be.equal(4);

          let [, {body}] = fetchMock.calls()[2];
          body = new URLSearchParams(body);
          expect(body.get('grant_type')).to.be.equal('password');
          expect(body.get('username')).to.be.equal('user');
          expect(body.get('password')).to.be.equal('abc123');
        });

        it('should not save refreshToken when stayLoggedIn is false', async() => {
          // A first request to get authentication but not storing refreshToken
          await client.call('FooService', 'fooMethod');
          expect(await localStorage.getItem('vaadin.connect.refreshToken')).not.to.be.ok;

          // emulate refresh page
          const newClient = new ConnectClient();
          newClient.credentials = client.credentials;
          await newClient.call('FooService', 'fooMethod');
          expect(await localStorage.getItem('vaadin.connect.refreshToken')).not.to.be.ok;

          expect(client.credentials).to.be.calledTwice;
          expect(fetchMock.calls().length).to.be.equal(4);

          let [, {body}] = fetchMock.calls()[2];
          body = new URLSearchParams(body);
          expect(body.get('grant_type')).to.be.equal('password');
        });

        it('should not fail if stored accessToken is corrupted', async() => {
          localStorage.setItem('vaadin.connect.refreshToken', 'CORRUPTED-TOKEN');

          const newClient = new ConnectClient();
          newClient.credentials = client.credentials;
          await newClient.call('FooService', 'fooMethod');

          expect(client.credentials).to.be.calledOnce;
          expect(fetchMock.calls().length).to.be.equal(2);

          let [, {body}] = fetchMock.calls()[0];
          body = new URLSearchParams(body);
          expect(body.get('grant_type')).to.be.equal('password');
        });

        it('should not use refreshToken if getting invalid_token response', async() => {
          localStorage.setItem('vaadin.connect.refreshToken', generateOAuthJson().refresh_token);
          fetchMock.restore();

          fetchMock
            .post(client.tokenEndpoint,
              {body: {error: 'invalid_token', error_description: 'Cannot convert access token to JSON'}, status: 401},
              {repeat: 1})
            .post(client.tokenEndpoint, generateOAuthJson,
              {repeat: 1, overwriteRoutes: false})
            .post(vaadinEndpoint, {fooData: 'foo'});

          const newClient = new ConnectClient({credentials: client.credentials});
          await newClient.call('FooService', 'fooMethod');

          expect(newClient.credentials).to.be.calledOnce;
          expect(fetchMock.calls().length).to.be.equal(3);

          let [, {body}] = fetchMock.calls()[0];
          body = new URLSearchParams(body);
          expect(body.get('grant_type')).to.be.equal('refresh_token');

          [, {body}] = fetchMock.calls()[1];
          body = new URLSearchParams(body);
          expect(body.get('grant_type')).to.be.equal('password');
        });
      });

      describe('with {requireCredentials: false} option', () => {
        it('should include Authorization header if authorized before', async() => {
          // Simulate login
          const response = generateOAuthJson();
          fetchMock.post(client.tokenEndpoint, response);
          await client.call('FooService', 'fooMethod');

          await client.call('FooService', 'fooMethod', undefined,
            {requireCredentials: false});

          expect(fetchMock.lastOptions().headers)
            .to.have.property('Authorization', `Bearer ${response.access_token}`);
        });

        it('should not include Authorization header by default', async() => {
          await client.call('FooService', 'fooMethod', undefined,
            {requireCredentials: false});

          expect(fetchMock.calls().length).to.equal(1);
          expect(fetchMock.lastOptions().headers)
            .to.not.have.property('Authorization');
        });

        it('should not ask for credentials', async() => {
          await client.call('FooService', 'fooMethod', undefined,
            {requireCredentials: false});

          expect(client.credentials).to.not.be.called;
        });
      });

      describe('with stayLoggedIn', () => {
        beforeEach(() => {
          const credentials = sinon.fake
            .returns({username: 'user', password: 'abc123', stayLoggedIn: true});
          client.credentials = credentials;
        });

        it('should use refreshToken from localStorage when client refreshes', async() => {
          fetchMock.post(client.tokenEndpoint, generateOAuthJson);

          await client.call('FooService', 'fooMethod');
          expect(await localStorage.getItem('vaadin.connect.refreshToken')).to.be.ok;

          // refresh should re-use refreshToken
          const newClient = new ConnectClient();
          newClient.credentials = sinon.fake();
          await newClient.call('FooService', 'fooMethod');

          expect(newClient.credentials).not.be.called;
          expect(fetchMock.calls().length).to.be.equal(4);

          let [, {body}] = fetchMock.calls()[2];
          body = new URLSearchParams(body);
          expect(body.get('grant_type')).to.be.equal('refresh_token');
        });

        describe('logout', () => {
          it('should remove tokens on logout', async() => {
            fetchMock.post(client.tokenEndpoint, generateOAuthJson);

            await client.call('FooService', 'fooMethod');
            expect(await localStorage.getItem('vaadin.connect.refreshToken')).to.be.ok;

            await client.logout();
            expect(await localStorage.getItem('vaadin.connect.refreshToken')).not.to.be.ok;

            expect(fetchMock.calls().length).to.be.equal(2);

            await client.call('FooService', 'fooMethod');
            expect(fetchMock.calls().length).to.be.equal(4);
          });

          it('should abort pending token request on logout', async() => {
            // Delay token response
            fetchMock.post(client.tokenEndpoint, () =>
              new Promise(resolve => setTimeout(() => resolve(generateOAuthJson()), 500)));
            // Logout before token request finishes
            setTimeout(() => client.logout(), 300);
            try {
              await client.call('FooService', 'fooMethod');
              expect.fail('token request not aborted');
            } catch (error) {
              expect(error.message).to.equal('URL \'/oauth/token\' aborted.');
            }
          });

          it('should not abort new request after logout', async() => {
            fetchMock.post(client.tokenEndpoint, generateOAuthJson);
            client.logout();
            await client.call('FooService', 'fooMethod');
            const data = await client.call('FooService', 'fooMethod');
            expect(data).to.deep.equal({fooData: 'foo'});
          });
        });
      });

    });
  });
});
