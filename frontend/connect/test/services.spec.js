const {describe, it} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');

import {Connect} from '../src/connect.js';

describe('Connect', () => {
  it('should be exported', () => {
    expect(Connect).to.be.ok;
  });

  it('should have the stub method', () => {
    expect(() => Connect.stub()).to.not.throw();
  });
});
