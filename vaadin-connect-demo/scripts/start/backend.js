#!/usr/bin/env node
/**
 * Starts the backend server and executes the optional chained command while
 * the backend server is running.
 *
 * Usage:
 *   $ node scripts/start/backend.js
 *   $ node scripts/start/backend.js -- echo "The backend is running..."
 */

const {spawnSync} = require('child_process');

const endOfOptionsIndex = process.argv.indexOf('--');
const [chainedExecutable, ...chainedArgs] = endOfOptionsIndex > -1
  ? process.argv.slice(endOfOptionsIndex + 1)
  : [];

if (chainedExecutable) {
  process.exit(
    spawnSync(
      'mvn',
      [
        'spring-boot:start',
        'exec:exec',
        `-Dexec.executable="${chainedExecutable}"`,
        `-Dexec.args="${chainedArgs.join(' ')}"`
      ],
      {stdio: 'inherit', shell: true}
    ).status
  );
} else {
  process.exit(
    spawnSync(
      'mvn',
      ['spring-boot:run'],
      {stdio: 'inherit', shell: true}
    ).status
  );
}
