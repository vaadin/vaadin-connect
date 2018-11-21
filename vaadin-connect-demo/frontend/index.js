import {ConnectClient} from '@vaadin/connect';

const client = new ConnectClient({endpoint: '/connect'});

async function fetchAccessToken(client) {
  /* global URLSearchParams, fetch, btoa */
  const params = new URLSearchParams();
  params.append('username', 'test_login');
  params.append('password', 'test_password');
  params.append('grant_type', 'password');

  const response = await fetch('/oauth/token', {
    method: 'POST',
    headers: {
      'Authorization': `Basic ${btoa('vaadin-connect-client:c13nts3cr3t')}`
    },
    body: params
  });
  client.accessToken = (await response.json()).access_token;
}

/* global addOne, number */
addOne.onclick = async() => {
  if (!client.accessToken) {
    await fetchAccessToken(client);
  }

  number.textContent = await client.call('DemoVaadinService', 'addOne', {
    number: Number(number.textContent)
  });
};
