const {describe, it, beforeEach} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');

import {pollUntil} from '@theintern/leadfoot';

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
      await page.findById('login').click();
      await pollUntil(
        text => document.getElementById('loginMessage').textContent === text,
        'Hello, test_login!'
      );
    });

    it('should increment number on button click', async() => {
      await page.findById('addOne').click();
      await pollUntil(
        text => document.getElementById('number').textContent === text,
        '2'
      );
    });

    describe('anonymous access', () => {
      beforeEach(async() => {
        await page.findById('logout').click();
      });

      it('should allow anonymous access with missing credentials', async() => {
        // Reload the page in anonymous mode after logout
        page = page.get('?credentials=none');

        await page
          .findById('access').getVisibleText().then(text =>
            expect(text).to.equal('')
          ).end()
          .findById('checkAnonymousAccess').click();
        await pollUntil(
          text => document.getElementById('access').textContent === text,
          'anonymous success'
        );
      });

      it('should allow anonymous access with wrong credentials', async() => {
        // Reload the page in anonymous mode after logout
        page = page.get('?credentials=wrong');

        await page
          .findById('access').getVisibleText().then(text =>
            expect(text).to.equal('')
          ).end()
          .findById('checkAnonymousAccess').click();
        await pollUntil(
          text => document.getElementById('access').textContent === text,
          'anonymous success'
        );
      });
    });

    describe('exception handling', () => {
      beforeEach(async() => {
        await page.findById('login').click().end();
      });

      it('should throw when backend server throws a generic exception', async() => {
        await page
          .findById('exceptionButton').click().end()
          .sleep(2000)
          .findById('exceptionMessage').getVisibleText().then(text =>
            expect(text).to.equal('Service \'DemoVaadinService\' method \'throwsException\' execution failure')).end()
          .findById('exceptionType').getVisibleText().then(text => expect(text).to.be.empty).end()
          .findById('exceptionDetail').getVisibleText().then(text => expect(text).to.be.empty).end();
      });

      it('should throw when backend server throws VaadinConnect exception', async() => {
        await page
          .findById('submitButton').click().end()
          .sleep(2000)
          .findById('exceptionMessage').getVisibleText().then(text => expect(text).to.equal('You had one job to do!')).end()
          .findById('exceptionType').getVisibleText().then(text => expect(text).to.equal('java.lang.ArithmeticException')).end()
          .findById('exceptionDetail').getVisibleText().then(text => expect(text).to.equal('{"wrong_parameter":0}')).end();
      });
    });
  });

  describe('single page application', () => {

    it('should get index when url is root', async(context) => {
      const page = context.remote.get('');
      await page
        .findById('number').getVisibleText().then(text =>
          expect(text).to.equal('1')
        );
    });

    it('should get index when url has no extension', async(context) => {
      const page = context.remote.get('app/anyroute');
      await page
        .findById('number').getVisibleText().then(text =>
          expect(text).to.equal('1')
        );
    });

    it('should get index when url does not have an extension', async(context) => {
      const page = context.remote.get('app/anyroute');
      await page
        .findById('number').getVisibleText().then(text =>
          expect(text).to.equal('1')
        );
    });

    it('should get error when url have an extension', async(context) => {
      const page = context.remote.get('app/invalidfile.html');
      await page
        .findByCssSelector('body').getVisibleText().then(text =>
          expect(text).to.match(/error/i)
        );
    });

    it('should load static resource when it exists', async(context) => {
      const page = context.remote.get('README.txt');
      await page
        .findByCssSelector('body').getVisibleText().then(text =>
          expect(text).to.match(/README content/i)
        );
    });
  });
});
