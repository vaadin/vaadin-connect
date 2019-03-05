import {spawn, spawnSync} from '../spawn';

function internify<T extends (...args: any[]) => any>(fn: T) {
  const nodeArgs: string[] = [];
  if (process.env.NODE_DEBUG_OPTION) {
    nodeArgs.push(process.env.NODE_DEBUG_OPTION);
  }

  return (args: string[]) => fn('node', [
    ...nodeArgs,
    './node_modules/.bin/intern',
    ...args
  ]) as ReturnType<T>;
}

export const spawnIntern = internify(spawn);
export const spawnInternSync = internify(spawnSync);
