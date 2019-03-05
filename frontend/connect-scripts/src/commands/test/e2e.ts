import {Command} from '../../lib/command';

import {spawnInternSync} from '../../lib/spawn/intern';

/**
 * Runs end-to-end tests.
 */
export const command: Command = async() => {
  spawnInternSync([]);
};
