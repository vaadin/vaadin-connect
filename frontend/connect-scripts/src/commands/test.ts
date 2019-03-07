import {Command, useCommand} from '../lib/command';

import {command as backendCommand} from './start/backend';
import {command as testE2eCommand} from './test/e2e';
import {command as testUnitCommand} from './test/unit';

import {spawnWebpackSync} from '../lib/spawn/webpack';

/**
 * Runs frontend unit tests, then end-to-end tests with
 * the frontend and backend.
 */
export const command: Command = async() => {
  await useCommand(testUnitCommand);

  spawnWebpackSync(['--mode', 'development']);
  await useCommand(backendCommand);

  await useCommand(testE2eCommand);
};
