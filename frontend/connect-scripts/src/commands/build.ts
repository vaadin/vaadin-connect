import {Command} from '../lib/command';

import {spawnMavenSync} from '../lib/spawn/maven';

export const command: Command = () => {
  spawnMavenSync(['package']);
};
