import {Command} from '../../lib/command';

import {spawnWebpack} from '../../lib/spawn/webpack';

import kill = require('tree-kill');

export const command: Command = () => {
  const webpackProcess = spawnWebpack(['--mode', 'development', '--watch']);

  return () => {
    kill(webpackProcess.pid);
  };
};
