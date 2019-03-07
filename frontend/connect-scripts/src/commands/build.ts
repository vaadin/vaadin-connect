import {Command} from '../lib/command';

import {spawnMaven} from '../lib/spawn/maven';

import kill = require('tree-kill');

export const command: Command = async() => {
  const packageProcess = spawnMaven(['package']);

  return () => {
    kill(packageProcess.pid);
  };
};
