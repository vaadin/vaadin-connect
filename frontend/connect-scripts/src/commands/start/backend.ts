import {Command} from '../../lib/command';
import {log, LogCategory} from '../../lib/log';
import {spawnMaven, spawnMavenSync} from '../../lib/spawn/maven';

const url = 'http://localhost:8080/';

/**
 * Runs the Java backend server
 */
export const command: Command = async() => {
  log(LogCategory.Progress, 'Starting the backend...');

  // Note: use async spawn, otherwise the Node.js command event loop
  // is blocked, e. g., cannot handle Ctrl+C until the backend startup
  // finishes. In certain cases (empty Maven cache, for example), it might
  // take a while to start, blocking all interrupts during that is not nice.
  await new Promise(resolve => {
    const backendProcess = spawnMaven(
      // Note: we use the fork option here, otherwise the watcher cannot reload
      // the backend.
      ['compile', 'spring-boot:start', '-Dspring-boot.run.fork']
    );
    backendProcess.prependListener('exit', (code: number) => {
      if (code === 0) {
        resolve();
      } else {
        log(
          LogCategory.Error,
          '\x1b[31;1mUnable to start the backend. Check whether another',
          'instance is already running \x1b[0m'
        );
        process.exit(code);
      }
    });
  });

  log(LogCategory.Success, `The backend is running at: ${url}`);
  process.env.CONNECT_BACKEND = url;

  return () => {
    log(LogCategory.Progress, 'Stopping the backend...');

    // Cleanup: stop the forked backend
    spawnMavenSync(['spring-boot:stop', '-Dspring-boot.stop.fork', '-q']);
  };
};
