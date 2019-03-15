import {ensureExecutable} from '../ensureExecutable';
import {spawn, spawnSync} from '../spawn';

function mavenify<T extends (...args: any[]) => any>(fn: T) {
  const mvwnFilePath = './mvnw';
  ensureExecutable(mvwnFilePath);
  return (args: string[]) =>
    fn(mvwnFilePath, ['-e', ...args]) as ReturnType<T>;
}

export const spawnMaven = mavenify(spawn);
export const spawnMavenSync = mavenify(spawnSync);
