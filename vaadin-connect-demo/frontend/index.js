// The code that uses the generated Vaadin Connect modules:
import client from './src/generated/connect-client.default';
import {addOne} from './src/generated/DemoVaadinService';

client.credentials = (options = {}) => {
  return {username: 'test_login', password: 'test_password', stayLoggedIn: true};
};

const numberLabel = document.getElementById('number');
document.getElementById('addOne').addEventListener('click', async() => {
  numberLabel.textContent = await addOne(numberLabel.textContent);
});

// Same code without the generated JavaScript, using vaadin-connect NPM module api instead:
import {ConnectClient} from '@vaadin/connect';

const customClient = new ConnectClient({
  endpoint: '/connect',
  credentials: (options = {}) => {
    return {username: 'test_login', password: 'test_password', stayLoggedIn: true};
  }
});

document.getElementById('addAnotherOne').addEventListener('click', async() => {
  customClient.call('DemoVaadinService', 'addOne', {
    number: numberLabel.textContent
  }).then(incrementedValue => numberLabel.textContent = incrementedValue);
});
