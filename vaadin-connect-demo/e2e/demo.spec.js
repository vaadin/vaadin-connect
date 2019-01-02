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

    it('should not have logged in', async() => {
      await page
        .findById('loginMessage').getVisibleText().then(text =>
          expect(text).to.equal('')
        );
    });

    it('should say hello after logging in', async() => {
      await page
        .findById('login').click().end()
        .sleep(2000)
        .findById('loginMessage').getVisibleText().then(text =>
          expect(text).to.equal('Hello, test_login!')
        );
    });

    it('should increment number on button click', async() => {
      await page
        .findById('addOne').click().end()
        .sleep(2000)
        .findById('number').getVisibleText().then(text =>
          expect(text).to.equal('2')
        );
    });

    describe('anonymous access', () => {
      beforeEach(async() => {
        await page.findById('logout').click().end();
      });

      it('should allow anonymous access with missing credentials', async() => {
        // Reload the page in anonymous mode after logout
        page = page.get('?credentials=none');

        await page
          .findById('access').getVisibleText().then(text =>
            expect(text).to.equal('')
          ).end()
          .findById('checkAnonymousAccess').click().end()
          .sleep(2000)
          .findById('access').getVisibleText().then(text =>
            expect(text).to.equal('anonymous success')
          );
      });

      it('should allow anonymous access with wrong credentials', async() => {
        // Reload the page in anonymous mode after logout
        page = page.get('?credentials=wrong');

        await page
          .findById('access').getVisibleText().then(text =>
            expect(text).to.equal('')
          ).end()
          .findById('checkAnonymousAccess').click().end()
          .sleep(2000)
          .findById('access').getVisibleText().then(text =>
            expect(text).to.equal('anonymous success')
          );
      });
    });
  });
});
