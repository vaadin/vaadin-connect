/**
 * The cleanup callback. This must be sync, async activities are not good
 * during shutdown of Node.js.
 */
export type CommandCleanup = () => void;

/**
 * The command callback. This could be async.
 *
 * Optionally returns a cleanup callback.
 */
export type Command = () =>
  void | CommandCleanup | Promise<void | CommandCleanup>;

/**
 * The current stack of cleanup callbacks
 */
interface GlobalExtensions {
  currentCleanupCallbacks?: CommandCleanup[];
}
const _global = global as GlobalExtensions;

/**
 * Runs the child command. Store the cleanup callback if returned.
 */
export async function useCommand(command: Command): Promise<void> {
  if (_global.currentCleanupCallbacks === undefined) {
    throw new Error('Invalid useCommand usage, use with runCommand only');
  }
  const cleanupCallback = await command();
  if (cleanupCallback) {
    // Cleanup callback is returned from command, put on the stack
    _global.currentCleanupCallbacks.unshift(cleanupCallback);
  }
}

/**
 * Executes cleanup callbacks form the stack
 */
function cleanup(callbacks: CommandCleanup[]) {
  callbacks.forEach(callback => callback());
}

/**
 * Runs the top-level command, providing the cleanup stack for the child
 * commands.
 */
export async function runCommand(command: Command): Promise<void> {
  const cleanupCallbacks: CommandCleanup[] = [];
  process.on('exit', () => cleanup(cleanupCallbacks));
  _global.currentCleanupCallbacks = cleanupCallbacks;
  console.log('run command', command); // tslint:disable-line
  await useCommand(command);
  console.log('run command end', command); // tslint:disable-line
  _global.currentCleanupCallbacks = undefined;
}
