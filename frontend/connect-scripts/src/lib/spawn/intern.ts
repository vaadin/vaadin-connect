import {spawn, spawnSync} from '../spawn';

function internify<T extends (...args: any[]) => any>(fn: T) {
  return (args: string[]) => fn(
    './node_modules/.bin/intern',
    args,
    {
      env: Object.assign({}, process.env, {
        NODE_OPTIONS: [
          process.env.NODE_DEBUG_OPTION,
          process.env.NODE_OPTIONS
        ].filter(Boolean).join(' ') || ''
      })
    }
  ) as ReturnType<T>;
}

export const spawnIntern = internify(spawn);
export const spawnInternSync = internify(spawnSync);
