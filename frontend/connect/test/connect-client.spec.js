const {describe, it, beforeEach, afterEach} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');
const {fetchMock} = intern.getPlugin('fetchMock');
const {sinon} = intern.getPlugin('sinon');

import {ConnectClient} from '../src/connect-client.js';

describe('ConnectClient', () => {
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

    it('should fetch service and method from default endpoint', () => {
      expect(fetchMock.calls()).to.have.lengthOf(0); // no premature requests

      client.call('FooService', 'fooMethod');

      expect(fetchMock.calls()).to.have.lengthOf(1);
      expect(fetchMock.lastUrl()).to.equal('/connect/FooService/fooMethod');
    });

    it('should return Promise', () => {
      const returnValue = client.call('FooService', 'fooMethod');
      expect(returnValue).to.be.a('promise');
    });

    it('should use POST request', () => {
      client.call('FooService', 'fooMethod');

      expect(fetchMock.lastOptions()).to.include({method: 'POST'});
    });

    it('should use JSON request headers', () => {
      client.call('FooService', 'fooMethod');

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
        expect(err).to.be.instanceOf(TypeError)
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

  describe('accessToken', () => {
    beforeEach(() => fetchMock
      .post('/connect/FooService/fooMethod', {fooData: 'foo'})
    );

    afterEach(() => fetchMock.restore());

    let client;
    beforeEach(() => client = new ConnectClient());

    const token = 'fooToken';

    it('should not have default accessToken', () => {
      expect(client).to.not.have.property('accessToken');
    });

    it('should allow setting string accessToken', () => {
      client.accessToken = token;
      expect(client).to.have.property('accessToken', token);
    });

    it('should not include Authorization header by default', async() => {
      await client.call('FooService', 'fooMethod');
      expect(fetchMock.lastOptions().headers)
        .to.not.have.property('Authorization');
    });

    it('should include Authorization header when accessToken is string', async() => {
      client.accessToken = token;

      await client.call('FooService', 'fooMethod');
      expect(fetchMock.lastOptions().headers)
        .to.have.property('Authorization', `Bearer ${token}`);
    });

    it('should not include Authorization header when accessToken is falsy', async() => {
      for (const falsy of [false, '', 0, null, undefined, NaN]) {
        client.accessToken = falsy;
        await client.call('FooService', 'fooMethod');
        expect(fetchMock.lastOptions().headers)
          .to.not.have.property('Authorization');
      }

      client.accessToken = {};
      await client.call('FooService', 'fooMethod');
      expect(fetchMock.lastOptions().headers)
        .to.have.property('Authorization', `Bearer [object Object]`);
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
      const credentials = sinon.fake
        .returns({username: 'user', password: 'abc123'});
      client = new ConnectClient({credentials});
      fetchMock.post(vaadinEndpoint, {fooData: 'foo'});
    });

    afterEach(() => {
      fetchMock.restore();
    });

    it('should ask for credentials when accessToken is missing', async() => {
      fetchMock.post(client.tokenEndpoint, {access_token: 'accessT'});
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
      fetchMock.post(client.tokenEndpoint, {access_token: 'accessT'});

      await client.call('FooService', 'fooMethod');

      const [[url, {method, headers, body}]] = fetchMock.calls();

      // TODO: remove when #58
      expect(headers).to.have.property('Authorization');

      expect(method).to.equal('POST');
      expect(url).to.equal('/oauth/token');
      expect(body.toString())
        .to.equal('grant_type=password&username=user&password=abc123');
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

      expect(client.accessToken).to.be.undefined;

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
      fetchMock.post(
        client.tokenEndpoint,
        {body: 'Server Internal Error', status: 500}
      );

      try {
        await client.call('FooService', 'fooMethod');
      } catch (err) {
        expect(err).to.be.instanceOf(TypeError)
          .and.have.property('message')
          .that.has.string('500 Internal Server Error');
        expect(client.credentials).to.be.calledOnce;
      }
    });

    it('should use accessToken when token response is ok', async() => {
      fetchMock.post(client.tokenEndpoint, {access_token: 'accessT'});

      const data = await client.call('FooService', 'fooMethod');
      expect(client).to.have.property('accessToken', 'accessT');

      const [url, {method, headers}] = fetchMock.calls()[1];
      expect(method).to.equal('POST');
      expect(url).to.equal('/connect/FooService/fooMethod');
      expect(headers).to.have.property('Authorization', 'Bearer accessT');
      expect(data).to.deep.equal({fooData: 'foo'});
    });
  });
});
