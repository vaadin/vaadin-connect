/**
 * This class is used for OpenApi generator test
 *
 * This module has been generated from GeneratorTestClass.java
 * @module GeneratorTestClass
 */

// @ts-ignore
import client from './connect-client.default.js';

export function getAllUsers(): Promise<void> {
  return client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: false});
}