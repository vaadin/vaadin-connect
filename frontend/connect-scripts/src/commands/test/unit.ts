import {Command} from '../../lib/command';

import {spawnInternSync} from '../../lib/spawn/intern';

/**
 * Runs frontend unit tests on Node.js.
 */
export const command: Command = async() => {
  spawnInternSync(['environments=node', 'functionalSuites=']);
};
