import {Command, useCommand} from '../lib/command';

import {command as backendCommand} from './start/backend';

import {spawnInternSync} from '../lib/spawn/intern';
import {spawnWebpackSync} from '../lib/spawn/webpack';

export const command: Command = async() => {
  // Run unit tests
  spawnInternSync(['environments=node', 'functionalSuites=']);

  spawnWebpackSync(['--mode', 'development']);
  await useCommand(backendCommand);

  // Run end-to-end tests
  spawnInternSync([]);
};
