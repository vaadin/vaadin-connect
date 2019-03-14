import fs = require('fs');
import {spawn, spawnSync} from '../spawn';

function mavenify<T extends (...args: any[]) => any>(fn: T) {
  const stat = fs.statSync('./mvnw');
  // tslint:disable-next-line:no-bitwise
  if ((stat.mode & fs.constants.S_IXUSR) === 0) {
    // tslint:disable-next-line:no-bitwise
    fs.chmodSync('./mvnw', stat.mode | fs.constants.S_IXUSR);
  }
  return (args: string[]) =>
    fn('./mvnw', ['-e', ...args]) as ReturnType<T>;
}

export const spawnMaven = mavenify(spawn);
export const spawnMavenSync = mavenify(spawnSync);
