import client from './connect-client.default.js';

export function shouldBeDisplayed1() {
  return client.call('DenyAllService', 'shouldBeDisplayed1', undefined, {requireCredentials: true});
}

export function shouldBeDisplayed2() {
  return client.call('DenyAllService', 'shouldBeDisplayed2', undefined, {requireCredentials: true});
}

export function shouldBeDisplayed3() {
  return client.call('DenyAllService', 'shouldBeDisplayed3', undefined, {requireCredentials: false});
}
