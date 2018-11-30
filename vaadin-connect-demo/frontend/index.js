// The code that uses the generated Vaadin Connect modules:

import client from './src/generated/connect-client.default';
import {addOne} from './src/generated/DemoVaadinService';
import {ConnectClient} from '@vaadin/connect';

client.credentials = (options = {}) => {
  return {username: 'test_login', password: 'test_password', stayLoggedIn: true};
};

const numberLabel = document.getElementById('number');
document.getElementById('addOne').onclick = async() => {
  numberLabel.textContent = await addOne(numberLabel.textContent);
};

// If you don't want to generate the modules and the client, the client npm module itself is enough to work with the Vaadin Connect backend:

const customClient = new ConnectClient({
  endpoint: '/connect',
  credentials: (options = {}) => {
    return {username: 'test_login', password: 'test_password'};
  }
});

customClient.call('DemoVaadinService', 'addOne', {
  number: 5
}).then(incrementedValue => {
  if (incrementedValue !== 6) {
    console.error(`Received unexpected incremented value: ${incrementedValue}`);
  }
});

// Or you can handle everything yourself, sending POST requests to Vaadin Connect backend to get tokens and interact with the services.
