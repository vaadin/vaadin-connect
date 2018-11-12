const {describe, it} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');

import {Services} from '../src/services.js';

describe('Services', () => {
  it('should be exported', () => {
    expect(Services).to.be.ok;
  });

  it('should have the stub method', () => {
    expect(() => Services.stub()).to.not.throw();
  });
});
