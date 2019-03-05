import {Command} from '../../lib/command';
import {log, LogCategory} from '../../lib/log';
import {spawnMaven} from '../../lib/spawn/maven';

import kill = require('tree-kill');

/**
 * Runs the Java watcher in the background
 */
export const command: Command = () => {
  log(LogCategory.Progress, 'Watching for Java changes on ./src/main/java');
  const watcherProcess = spawnMaven(['fizzed-watcher:run']);

  return () => {
    kill(watcherProcess.pid);
  };
};
