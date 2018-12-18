// The code that uses the generated Vaadin Connect modules:
import client from './src/generated/connect-client.default.js';
import {addOne, hasAnonymousAccess} from './src/generated/DemoVaadinService.js';

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
    alert(await hasAnonymousAccess());
  });

const numberLabel = document.getElementById('number');
document.getElementById('addOne').addEventListener('click', async() => {
  numberLabel.textContent = await addOne(numberLabel.textContent);
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

// Same code without the generated JavaScript, using '@vaadin/connect' npm module
// API instead:
import {ConnectClient} from '@vaadin/connect';

const customClient = new ConnectClient({endpoint: '/connect', credentials});

document.getElementById('addAnotherOne').addEventListener('click', async() => {
  customClient.call('DemoVaadinService', 'addOne', {
    number: numberLabel.textContent
  }).then(incrementedValue => numberLabel.textContent = incrementedValue);
});
