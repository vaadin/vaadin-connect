const {describe, it, beforeEach, afterEach} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');
const {fetchMock} = intern.getPlugin('fetchMock');
const {sinon} = intern.getPlugin('sinon');

// Specific Browser API used in Vaadin Client and not present in node
intern.getPlugin('btoa');
intern.getPlugin('url-search-params');
intern.getPlugin('window-console');

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

    it('should include Authorization header when accessToken is defined', async() => {
      client.accessToken = 0;
      await client.call('FooService', 'fooMethod');
      expect(fetchMock.lastOptions().headers)
        .to.have.property('Authorization', `Bearer 0`);

      client.accessToken = null;
      await client.call('FooService', 'fooMethod');
      expect(fetchMock.lastOptions().headers)
        .to.have.property('Authorization', `Bearer null`);

      client.accessToken = false;
      await client.call('FooService', 'fooMethod');
      expect(fetchMock.lastOptions().headers)
        .to.have.property('Authorization', `Bearer false`);

      client.accessToken = '';
      await client.call('FooService', 'fooMethod');
      expect(fetchMock.lastOptions().headers)
        .to.have.property('Authorization', `Bearer `);

      client.accessToken = NaN;
      await client.call('FooService', 'fooMethod');
      expect(fetchMock.lastOptions().headers)
        .to.have.property('Authorization', `Bearer NaN`);

      client.accessToken = {};
      await client.call('FooService', 'fooMethod');
      expect(fetchMock.lastOptions().headers)
        .to.have.property('Authorization', `Bearer [object Object]`);

      client.accessToken = undefined;
      await client.call('FooService', 'fooMethod');
      expect(fetchMock.lastOptions().headers)
        .to.not.have.property('Authorization');
    });
  });

  describe('getToken', () => {
    let client;
    const service = 'FooService', method = 'fooMethod';
    const vaadinEndpoint = `/connect/${service}/${method}`;

    beforeEach(() => {
      client = new ConnectClient();
    });

    afterEach(() => fetchMock.restore());

    it(`when requesting a service
        should ask for user and password and use the accessToken`, async() => {
      client.credentials = sinon.fake.returns({username: 'user', password: 'abc123'});

      fetchMock
        .post(client.tokenEndpoint, {access_token: 'accessT'})
        .post(vaadinEndpoint, {fooData: 'foo'});

      const data = await client.call(service, method);
      expect(data.fooData).to.be.equal('foo');

      expect(client.credentials.callCount).to.be.equal(1);
      expect(client.accessToken).to.be.equal('accessT');

      expect(fetchMock.calls()[0][0]).to.be.equal(client.tokenEndpoint);
      expect(fetchMock.calls()[0][1].headers).to.have.property('Authorization');

      expect(fetchMock.calls()[1][0]).to.be.equal(vaadinEndpoint);
      expect(fetchMock.calls()[1][1].headers).to.have.property('Authorization');
      expect(fetchMock.calls()[1][1].headers['Authorization']).to.be.equal('Bearer accessT');
    });

    it(`when token response is invalid
        should avoid looping credentials, log an error, and proceed with the call without accessToken`, async() => {
      client.credentials = sinon.fake.returns({username: 'user', password: 'abc123'});

      const errorSpy = sinon.spy(window.console, 'error');

      fetchMock
        .post(client.tokenEndpoint, {body: 'Bad Request', status: 500})
        .post(vaadinEndpoint, {fooData: 'foo'});

      const data = await client.call(service, method);
      expect(data.fooData).to.be.equal('foo');

      expect(client.accessToken).to.be.undefined;

      expect(errorSpy.callCount).to.be.equal(1);
      expect(errorSpy.getCall(0).args[0]).to.match(/Bad Request/);
      errorSpy.restore();

      expect(client.credentials.callCount).to.be.equal(1);

      expect(fetchMock.calls()[0][0]).to.be.equal(client.tokenEndpoint);
      expect(fetchMock.calls()[0][1].headers).to.have.property('Authorization');

      expect(fetchMock.calls()[1][0]).to.be.equal(vaadinEndpoint);
      expect(fetchMock.calls()[1][1].headers).not.to.have.property('Authorization');
    });

    it(`when token response is 40X
        should continue asking for user and password until credentials returns false`, async() => {
      client.credentials = sinon.stub();
      client.credentials.onCall(0).returns({username: 'user', password: 'abc123'});
      client.credentials.onCall(1).returns({username: 'user', password: 'abc123'});
      client.credentials.onCall(2).returns(false);

      fetchMock
        .post(client.tokenEndpoint, {body: {error: 'invalid_grant', error_description: 'Bad credentials'}, status: 400})
        .post(vaadinEndpoint, {fooData: 'foo'});

      const data = await client.call(service, method);
      expect(data.fooData).to.be.equal('foo');

      expect(fetchMock.calls().length).to.be.equal(3);

      expect(client.accessToken).to.be.undefined;

      expect(client.credentials.callCount).to.be.equal(3);

      expect(fetchMock.calls()[0][0]).to.be.equal(client.tokenEndpoint);
      expect(fetchMock.calls()[0][1].headers).to.have.property('Authorization');
      expect(fetchMock.calls()[1][0]).to.be.equal(client.tokenEndpoint);
      expect(fetchMock.calls()[1][1].headers).to.have.property('Authorization');
      expect(fetchMock.calls()[2][0]).to.be.equal(vaadinEndpoint);
      expect(fetchMock.calls()[2][1].headers).not.to.have.property('Authorization');
    });
  });
});
