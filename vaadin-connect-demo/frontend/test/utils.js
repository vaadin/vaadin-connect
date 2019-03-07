const path = require('path');

// Enable .ts file loading with ts-node
require('ts-node').register({
  project: path.resolve(__dirname, '../../tsconfig.json'),
  compilerOptions: {
    module: "CommonJS"
  }
});

// Provide proxyquire to enable mocking dependencies in unit tests
intern.registerPlugin('proxyquire', () => {
  const proxyquire = require('proxyquire');
  return {proxyquire};
});

// Provide Sinon.JS stubbing/mocking framework for unit tests
intern.registerPlugin('sinon', () => {
  const chai = intern.getPlugin('chai');
  const sinon = require('sinon');
  chai.use(require('sinon-chai'));
  return {sinon};
});
