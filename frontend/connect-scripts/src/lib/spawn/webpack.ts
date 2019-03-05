import {spawn, spawnSync} from '../spawn';

function webpackify<T extends (...args: any[]) => any>(fn: T) {
  return (args: string[]) => fn('node', [
    './node_modules/.bin/webpack',
    '--progress',
    '--display', 'minimal',
    ...args
  ]) as ReturnType<T>;
}

export const spawnWebpack = webpackify(spawn);
export const spawnWebpackSync = webpackify(spawnSync);
