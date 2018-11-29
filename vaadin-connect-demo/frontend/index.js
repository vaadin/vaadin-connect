import client from './src/generated/connect-client.default';
import {addOne} from './src/generated/DemoVaadinService';

client.credentials = (options = {}) => {
  return {username: 'test_login', password: 'test_password'};
};

document.getElementById("addOne").onclick = async () => {
  number.textContent = await addOne(number.textContent);
};
