const {describe, it, beforeEach} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');

describe('demo application', () => {
  describe('index page', () => {
    let page;
    beforeEach(context => page = context.remote.get(''));

    it('should have number', async() => {
      await page
        .findById('number').getVisibleText().then(text =>
          expect(text).to.equal('1')
        );
    });

    it('should increment number on button click', async() => {
      await page
        .findById('addOne').click().end()
        .setFindTimeout(5000)
        .findByXpath('//*[@id="number"][text()="2"]');
    });
  });
});
