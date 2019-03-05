/// <reference types="intern"/>

declare global {
  interface Window {Vaadin: any;}
}

const {
  after,
  before,
  beforeEach,
  describe,
  it
} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');

import {Command, pollUntilTruthy} from '@theintern/leadfoot';

describe('demo application', () => {
  describe('index page', () => {
    let page: Command<void>;

    before(async context => {
      await context.remote.session.setExecuteAsyncTimeout(30000);
      await context.remote.session.setFindTimeout(30000);
      page = context.remote.get('');
    });

    it('should have number', async() => {
      const num = await page.findById('number').getVisibleText();
      expect(num).to.equal('1');
    });

    it('should not have logged in', async() => {
      const loginMessage = await page.findById('loginMessage').getVisibleText();
      expect(loginMessage).to.equal('');
    });

    it('should say hello after logging in', async() => {
      await page.findById('login').click();
      // tslint:disable-next-line:only-arrow-functions
      await pollUntilTruthy(function(text) {
        return (document.getElementById('loginMessage') as HTMLOutputElement)
          .textContent === text;
      }, ['Hello, test_login!']).call(page);
    });

    it('should increment number on button click', async() => {
      await page.findById('addOne').click();
      // tslint:disable-next-line:only-arrow-functions
      await pollUntilTruthy(function(text) {
        return (document.getElementById('number') as HTMLOutputElement)
          .textContent === text;
      }, ['2']).call(page);
    });

    describe('anonymous access', () => {
      beforeEach(async() => {
        await page.findById('logout').click();
      });

      it('should allow anonymous access with missing credentials', async() => {
        // Reload the page in anonymous mode after logout
        page = page.get('?credentials=none');
        await page;

        const access = await page.findById('access').getVisibleText();
        expect(access).to.equal('');
        await page.findById('checkAnonymousAccess').click();
        // tslint:disable-next-line:only-arrow-functions
        await pollUntilTruthy(function(text) {
          return (document.getElementById('access') as HTMLOutputElement)
            .textContent === text;
        }, ['anonymous success']).call(page);
      });

      it('should allow anonymous access with wrong credentials', async() => {
        // Reload the page in anonymous mode after logout
        page = page.get('?credentials=wrong');
        await page;

        const access = await page.findById('access').getVisibleText();
        await page.findById('checkAnonymousAccess').click();
        expect(access).to.equal('');
        // tslint:disable-next-line:only-arrow-functions
        await pollUntilTruthy(function(text) {
          return (document.getElementById('access') as HTMLOutputElement)
            .textContent === text;
        }, ['anonymous success']).call(page);
      });

      after(async() => {
        // Get back to working credentials and login
        page = page.get('');
        await page.findById('login').click();
        // tslint:disable-next-line:only-arrow-functions
        await pollUntilTruthy(function(text) {
          return (document.getElementById('loginMessage') as HTMLOutputElement)
            .textContent === text;
        }, ['Hello, test_login!']).call(page);
      });
    });

    describe('exception handling', () => {
      it('should throw generic exception', async() => {
        await page.findById('exceptionButton').click();
        // tslint:disable-next-line:only-arrow-functions
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('exceptionMessage') as HTMLOutputElement
          ).textContent === text;
        }, [
          'Service \'DemoVaadinService\' method \'throwsException\''
            + ' execution failure'
        ]).call(page);
        const exceptionType = await page.findById('exceptionType')
          .getVisibleText();
        expect(exceptionType).to.be.empty;
        const exceptionDetail = await page.findById('exceptionDetail')
          .getVisibleText();
        expect(exceptionDetail).to.be.empty;
      });

      it('should throw VaadinConnect exception', async() => {
        await page.findById('submitButton').click();
        // tslint:disable-next-line:only-arrow-functions
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('exceptionMessage') as HTMLOutputElement
          ).textContent === text;
        }, [
          'You had one job to do!'
        ]).call(page);
        const exceptionType = await page.findById('exceptionType')
          .getVisibleText();
        expect(exceptionType).to.equal('java.lang.ArithmeticException');
        const exceptionDetail = await page.findById('exceptionDetail')
          .getVisibleText();
        expect(exceptionDetail).to.equal('{"wrong_parameter":0}');
      });
    });

    describe('statistics', () => {
      it('should be registered in Vaadin namespace', async() => {
        // tslint:disable-next-line:no-var-keyword only-arrow-functions
        var registrations = await page.execute(function() {
          return window.Vaadin.registrations;
        });
        expect(registrations[0].is).to.equal('@vaadin/connect');
      });
    });

    describe('serialization', () => {
      it('should serialize/deserialize map object', async() => {
        await page.findById('echoMapObject').click();
        // tslint:disable-next-line:only-arrow-functions
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('mapObject') as HTMLOutputElement
          ).textContent === text;
        }, ['{"foo":"bar"}']).call(page);
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
