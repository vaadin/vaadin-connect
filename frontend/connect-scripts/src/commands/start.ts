import {Command, useCommand} from '../lib/command';

import {command as backendCommand} from './start/backend';

import {command as javaWatcherCommand} from './start/java-watcher';

import {command as apiBrowserCommand} from './start/api-browser';

import {command as webpackCommand} from './start/webpack';

export const command: Command = async() => {
  await useCommand(backendCommand);
  await useCommand(javaWatcherCommand);
  await useCommand(apiBrowserCommand);
  await useCommand(webpackCommand);

  return new Promise<void>(_ => {
    // Never resolves to keep Node.js waiting for an interrupt
  });
};
