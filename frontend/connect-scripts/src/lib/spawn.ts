import crossSpawn = require('cross-spawn');

import {SpawnOptions, SpawnSyncOptions} from 'child_process';

function assertZeroExitCode(exitCode: number, cmd: string, args: string[]) {
  if (exitCode !== 0) {
    throw new Error(
      `The command "${[cmd].concat(args).join(' ')}" finished with`
      + ` a non-zero return code ${exitCode}.`
    );
  }
}

export function spawn(cmd: string, args: string[], options?: SpawnOptions) {
  const childProcess = crossSpawn(
    cmd,
    args,
    Object.assign({stdio: 'inherit'}, options)
  );
  childProcess.on(
    'exit',
    (exitCode: number) => assertZeroExitCode(exitCode, cmd, args)
  );
  return childProcess;
}

export function spawnSync(
  cmd: string,
  args: string[],
  options?: SpawnSyncOptions
) {
  const result = crossSpawn.sync(
    cmd,
    args,
    Object.assign({stdio: 'inherit'}, options)
  );
  if (result.signal === 'SIGINT') {
    process.exit(130);
  }
  assertZeroExitCode(result.status, cmd, args);
  return result;
}
