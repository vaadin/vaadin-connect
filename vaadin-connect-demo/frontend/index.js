import {ConnectClient} from '@vaadin/connect';

const client = new ConnectClient({
  endpoint: '/connect',
  credentials: (options = {}) => {
    return {username: 'test_login', password: 'test_password'};
  }
});

/* global addOne, number */
addOne.onclick = async() => {
  number.textContent = await client.call('DemoVaadinService', 'addOne', {
    number: Number(number.textContent)
  });
};
