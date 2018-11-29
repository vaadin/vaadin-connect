import client from './src/generated/connect-client.default';
import {addOne} from './src/generated/DemoVaadinService';

client.credentials = (options = {}) => {
  return {username: 'test_login', password: 'test_password'};
};

const numberLabel = document.getElementById('number');
document.getElementById('addOne').onclick = async() => {
  numberLabel.textContent = await addOne(numberLabel.textContent);
};
