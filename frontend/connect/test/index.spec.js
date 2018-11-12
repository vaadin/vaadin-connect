const {describe, it} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');

import * as index from '../index.js';

describe('index', () => {
  it('should contain Connect', () => {
    expect(index.Connect).to.be.ok;
  });
});
