import client from './connect-client';

import * as demoService from './generated/DemoVaadinService';

import {
  AccessToken,
  ConnectClient,
  CredentialsCallback,
  VaadinConnectError
} from '@vaadin/connect';

const credentials: CredentialsCallback = () => {
  return {
    username: 'test_login',
    password: 'test_password',
    stayLoggedIn: true
  };
};

const wrongCredentials: CredentialsCallback = () => {
  return {
    username: 'foo',
    password: 'wrong'
  };
};

switch (new URLSearchParams(location.search).get('credentials')) {
  case 'none':
    client.logout();
    break;
  case 'wrong':
    client.credentials = wrongCredentials;
    break;
  default:
    client.credentials = credentials;
}

(document.getElementById('checkAnonymousAccess') as HTMLButtonElement)
  .addEventListener('click', async() => {
    const accessLabel = document.getElementById('access') as HTMLLabelElement;
    accessLabel.textContent = await demoService.hasAnonymousAccess();
  });

const numberLabel = document.getElementById('number') as HTMLLabelElement;
(document.getElementById('addOne') as HTMLButtonElement)
  .addEventListener('click', async() => {
    numberLabel.textContent = (await demoService.addOne(
      parseInt(numberLabel.textContent || '0', 10)
    )).toString();
    updateLoginStatus(client);
  });

(document.getElementById('login') as HTMLButtonElement)
  .addEventListener('click', async() => {
    await client.login();
    updateLoginStatus(client);
  });

(document.getElementById('logout') as HTMLButtonElement)
  .addEventListener('click', async() => {
    client.logout();
    updateLoginStatus(client);
  });

function updateLoginStatus(currentClient: ConnectClient) {
  const token: AccessToken = currentClient.token;
  const valid: boolean = token && token.exp > new Date().getTime() / 1000;
  (document.getElementById('loginMessage') as HTMLLabelElement)
    .textContent = `Hello, ${valid ? token.user_name : 'no user'}!`;
}

const input = document.getElementById('inputText') as HTMLInputElement;
(document.getElementById('submitButton') as HTMLButtonElement)
  .addEventListener('click', async() => {
    updateExceptionData({} as VaadinConnectError);
    try {
      await demoService.doNotSubmitZeroes(
        input.valueAsNumber ? input.valueAsNumber : 0
      );
    } catch (e) {
      updateExceptionData(e);
    }
  });

(document.getElementById('exceptionButton') as HTMLButtonElement)
  .addEventListener('click', async() => {
    updateExceptionData({} as VaadinConnectError);
    try {
      await demoService.throwsException();
    } catch (e) {
      updateExceptionData(e);
    }
  });

const updateExceptionData = (exception: VaadinConnectError) => {
  (document.getElementById('exceptionMessage') as HTMLLabelElement)
    .textContent = exception.message;
  (document.getElementById('exceptionType') as HTMLLabelElement)
    .textContent = exception.type || '';
  (document.getElementById('exceptionDetail') as HTMLLabelElement)
    .textContent = exception.detail ? JSON.stringify(exception.detail) : null;
};

(document.getElementById('echoMapObject') as HTMLButtonElement)
  .addEventListener('click', async() => {
    const mapObject = await demoService.echoMapObject({foo: 'bar'});
    (document.getElementById('mapObject') as HTMLLabelElement)
        .textContent = JSON.stringify(mapObject);
  });

const dateTimeInput =
  document.getElementById('dateTimeInput') as HTMLInputElement;
const dateTimeOutput =
  document.getElementById('dateTimeOutput') as HTMLLabelElement;

(document.getElementById('echoInstant') as HTMLButtonElement)
  .addEventListener('click', async() => {
    dateTimeOutput.textContent =
      await demoService.echoInstant(dateTimeInput.value);
  });

(document.getElementById('echoDate') as HTMLButtonElement)
  .addEventListener('click', async() => {
    dateTimeOutput.textContent =
      await demoService.echoDate(dateTimeInput.value);
  });

(document.getElementById('echoLocalDate') as HTMLButtonElement)
  .addEventListener('click', async() => {
    dateTimeOutput.textContent =
      await demoService.echoLocalDate(dateTimeInput.value);
  });

(document.getElementById('echoLocalDateTime') as HTMLButtonElement)
  .addEventListener('click', async() => {
    dateTimeOutput.textContent =
      await demoService.echoLocalDateTime(dateTimeInput.value);
  });

const customClient = new ConnectClient({endpoint: '/connect', credentials});

(document.getElementById('addAnotherOne') as HTMLButtonElement)
  .addEventListener('click', async() => {
    customClient.call('DemoVaadinService', 'addOne', {
      number: numberLabel.textContent
    }).then(incrementedValue => numberLabel.textContent = incrementedValue);
  });

(document.getElementById('validationButton') as HTMLButtonElement)
  .addEventListener('click', async() => {
    const name = (document.getElementById(
      'validationNameInput') as HTMLInputElement).value;
    const count = (document.getElementById(
      'validationCountInput') as HTMLInputElement).valueAsNumber;
    const additionalNumber = (document.getElementById(
      'additionalNumberInput') as HTMLInputElement).valueAsNumber;

    let result = null;
    try {
      result = JSON.stringify(await demoService.complexEntitiesTest({
        count,
        name,
        nestedClass: {nestedValue: additionalNumber}
      }));
    } catch (e) {
      result = e.message;
    }
    document.getElementById('validationOutput')!.textContent = result;
  });

(document.getElementById('echoOptional') as HTMLButtonElement)
  .addEventListener('click', async() => {
    const optionalString = (document.getElementById(
      'optionalInput') as HTMLInputElement).value;
    const optionalOutput =
      document.getElementById('optionalOutput') as HTMLLabelElement;
    optionalOutput.textContent =
      await demoService.echoOptionalString(optionalString || null);
  });
