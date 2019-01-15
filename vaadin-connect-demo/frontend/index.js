// The code that uses the generated Vaadin Connect modules:
import client from './connect-client.js';
import * as demoService from './generated/DemoVaadinService.js';
// Same code without the generated JavaScript, using '@vaadin/connect' npm module
// API instead:
import {ConnectClient} from '@vaadin/connect';

const credentials = (options = {}) => {
  return {username: 'test_login', password: 'test_password', stayLoggedIn: true};
};

switch (new URLSearchParams(location.search).get('credentials')) {
  case 'none':
    client.logout();
    break;
  case 'wrong':
    client.credentials = (options = {}) => {
      return {username: 'foo', password: 'wrong'};
    };
    break;
  default:
    client.credentials = credentials;
}

document.getElementById('checkAnonymousAccess').addEventListener('click',
  async() => {
    const accessLabel = document.getElementById('access');
    accessLabel.textContent = await demoService.hasAnonymousAccess();
  });

const numberLabel = document.getElementById('number');
document.getElementById('addOne').addEventListener('click', async() => {
  numberLabel.textContent = await demoService.addOne(numberLabel.textContent);
  updateLoginStatus(client);
});

document.getElementById('login').addEventListener('click', async() => {
  await client.login();
  updateLoginStatus(client);
});

document.getElementById('logout').addEventListener('click', async() => {
  client.logout();
  updateLoginStatus(client);
});

function updateLoginStatus(currentClient) {
  const token = currentClient.token;
  const valid = (token && token.exp > new Date() / 1000);
  document.getElementById('loginMessage').textContent = `Hello, ${valid ? token.user_name : 'no user'}!`;
}

const input = document.getElementById('inputText');
document.getElementById('submitButton').addEventListener('click', async() => {
  updateExceptionData({});
  try {
    await demoService.doNotSubmitZeroes(input.valueAsNumber ? input.valueAsNumber : 0);
  } catch (e) {
    updateExceptionData(e);
  }
});
document.getElementById('exceptionButton').addEventListener('click', async() => {
  updateExceptionData({});
  try {
    await demoService.throwsException();
  } catch (e) {
    updateExceptionData(e);
  }
});

const updateExceptionData = exception => {
  document.getElementById('exceptionMessage').textContent = exception.message;
  document.getElementById('exceptionType').textContent = exception.type;
  document.getElementById('exceptionDetail').textContent = exception.detail ? JSON.stringify(exception.detail) : null;
};

const customClient = new ConnectClient({endpoint: '/connect', credentials});

document.getElementById('addAnotherOne').addEventListener('click', async() => {
  customClient.call('DemoVaadinService', 'addOne', {
    number: numberLabel.textContent
  }).then(incrementedValue => numberLabel.textContent = incrementedValue);
});
