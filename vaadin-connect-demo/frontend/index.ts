import client from './connect-client';

import * as demoService from './generated/DemoVaadinService';

import {
  AccessToken,
  ConnectClient,
  CredentialsCallback,
  VaadinConnectException
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
      Number.parseInt(numberLabel.textContent || '0', 10)
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
    updateExceptionData({} as VaadinConnectException);
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
    updateExceptionData({} as VaadinConnectException);
    try {
      await demoService.throwsException();
    } catch (e) {
      updateExceptionData(e);
    }
  });

const updateExceptionData = (exception: VaadinConnectException) => {
  (document.getElementById('exceptionMessage') as HTMLLabelElement)
    .textContent = exception.message;
  (document.getElementById('exceptionType') as HTMLLabelElement)
    .textContent = exception.type || '';
  (document.getElementById('exceptionDetail') as HTMLLabelElement)
    .textContent = exception.detail ? JSON.stringify(exception.detail) : null;
};

const customClient = new ConnectClient({endpoint: '/connect', credentials});

(document.getElementById('addAnotherOne') as HTMLButtonElement)
  .addEventListener('click', async() => {
    customClient.call('DemoVaadinService', 'addOne', {
      number: numberLabel.textContent
    }).then(incrementedValue => numberLabel.textContent = incrementedValue);
  });
