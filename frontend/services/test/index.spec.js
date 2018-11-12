const {describe, it} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');

import * as index from '../index.js';

describe('index', () => {
  it('should contain Services', () => {
    expect(index.Services).to.be.ok;
  });
});
