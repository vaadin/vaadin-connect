/// <reference types="intern"/>

declare global {
  interface Window {
    Vaadin: any;
  }
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
      await pollUntilTruthy(function(text) {
        return (document.getElementById('loginMessage') as HTMLOutputElement)
          .textContent === text;
      }, ['Hello, test_login!']).call(page);
    });

    it('should increment number on button click', async() => {
      await page.findById('addOne').click();
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
        await pollUntilTruthy(function(text) {
          return (document.getElementById('access') as HTMLOutputElement)
            .textContent === text;
        }, ['anonymous success']).call(page);
      });

      after(async() => {
        // Get back to working credentials and login
        page = page.get('');
        await page.findById('login').click();
        await pollUntilTruthy(function(text) {
          return (document.getElementById('loginMessage') as HTMLOutputElement)
            .textContent === text;
        }, ['Hello, test_login!']).call(page);
      });
    });

    describe('exception handling', () => {
      it('should throw generic exception', async() => {
        await page.findById('exceptionButton').click();
        await pollUntilTruthy(function(text) {
          const exceptionMessage = (
            document.getElementById('exceptionMessage') as HTMLOutputElement
          ).textContent;
          return exceptionMessage && exceptionMessage.indexOf(text) > -1;
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
        await pollUntilTruthy(function(text) {
          const exceptionMessage = (
            document.getElementById('exceptionMessage') as HTMLOutputElement
          ).textContent;
          return exceptionMessage && exceptionMessage.indexOf(text) > -1;
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
        // tslint:disable-next-line:no-var-keyword prefer-const
        var registrations = await page.execute(
          function() {
            return window.Vaadin.registrations;
          }
        );
        expect(registrations[0].is).to.equal('@vaadin/connect');
      });
    });

    describe('serialization', () => {
      it('should serialize/deserialize map object', async() => {
        await page.findById('echoMapObject').click();
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('mapObject') as HTMLOutputElement
          ).textContent === text;
        }, ['{"foo":"bar"}']).call(page);
      });

      it('should serialize/deserialize Date object', async() => {
        await page.execute(function() {
          (document.getElementById('dateTimeInput') as HTMLInputElement)
            .value = '1546300800000';
        });
        await page.findById('echoDate').click();
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('dateTimeOutput') as HTMLOutputElement
          ).textContent === text;
        }, ['2019-01-01T00:00:00.000+0000']).call(page);
      });

      it('should serialize/deserialize Instant object', async() => {
        await page.execute(function() {
          (document.getElementById('dateTimeInput') as HTMLInputElement)
            .value = '1551886875';
        });
        await page.findById('echoInstant').click();
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('dateTimeOutput') as HTMLOutputElement
          ).textContent === text;
        }, ['2019-03-06T15:41:15Z']).call(page);
      });

      it('should serialize/deserialize LocalDate object', async() => {
        await page.execute(function() {
          (document.getElementById('dateTimeInput') as HTMLInputElement)
            .value = '2019-06-03';
        });
        await page.findById('echoLocalDate').click();
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('dateTimeOutput') as HTMLOutputElement
          ).textContent === text;
        }, ['2019-06-03']).call(page);
      });

      it('should serialize/deserialize LocalDateTime object', async() => {
        await page.execute(function() {
          (document.getElementById('dateTimeInput') as HTMLInputElement)
            .value = '2019-01-01T12:34:56.78';
        });
        await page.findById('echoLocalDateTime').click();
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('dateTimeOutput') as HTMLOutputElement
          ).textContent === text;
        }, ['2019-01-01T12:34:56.78']).call(page);
      });

      it('should serialize/deserialize Optional object', async() => {
        await page.execute(function() {
          (document.getElementById('optionalInput') as HTMLInputElement)
            .value = 'Hello, world!';
        });
        await page.findById('echoOptional').click();
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('optionalOutput') as HTMLOutputElement
          ).textContent === text;
        }, ['Hello, world!']).call(page);
      });

      it('should recognize a null Optional object', async() => {
        await page.execute(function() {
          (document.getElementById('optionalInput') as HTMLInputElement)
            .value = '';
        });
        await page.findById('echoOptional').click();
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('optionalOutput') as HTMLOutputElement
          ).textContent === text;
        }, ['I am an empty string']).call(page);
      });
    });

    describe('validation', () => {
      it('should show all validation errors for incorrect input', async() => {
        await page.findById('validationButton').click();
        await pollUntilTruthy(function() {
          // tslint:disable-next-line:no-var-keyword prefer-const
          var validationOutput = (
            document.getElementById('validationOutput') as HTMLOutputElement
          ).textContent || '';
          return validationOutput.indexOf('Validation error') > 0 &&
            (validationOutput.match(/parameterName/g) || []).length === 3;
        }, ['']).call(page);
      });

      it('should show no validation errors for correct input', async() => {
        await page.execute(function() {
          (document.getElementById(
            'validationNameInput') as HTMLInputElement).value = 'test_name';
          (document.getElementById(
            'validationCountInput') as HTMLInputElement).value = '1';
          (document.getElementById(
            'additionalNumberInput') as HTMLInputElement).value = '-2';
        });
        await page.findById('validationButton').click();
        await pollUntilTruthy(function(text) {
          return (
            document.getElementById('validationOutput') as HTMLOutputElement
          ).textContent === text;
        }, ['{"name":"test_name","generatedResponse":{"0":[]}}']).call(page);
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
