import {Command} from '../../lib/command';

import {spawnWebpackSync} from '../../lib/spawn/webpack';

export const command: Command = () => {
  spawnWebpackSync([]);
};
