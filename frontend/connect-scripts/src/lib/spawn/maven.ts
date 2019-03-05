import {spawn, spawnSync} from '../spawn';

function mavenify<T extends (...args: any[]) => any>(fn: T) {
  return (args: string[]) =>
    fn('./mvnw', ['-e', ...args]) as ReturnType<T>;
}

export const spawnMaven = mavenify(spawn);
export const spawnMavenSync = mavenify(spawnSync);
