#!/usr/bin/env node
import {runCommand} from './lib/command';
import {log, LogCategory} from './lib/log';

// Graceful shutdown
process.on('SIGINT', () => process.exit(0));
process.on('SIGBREAK', () => process.exit(0));
process.on('SIGHUP', () => process.exit(129));
process.on('SIGTERM', () => process.exit(137));

// Quit for unhandled rejections explicitly to avoid a deprecation warning
process.on('unhandledRejection', () => process.exit(1));

async function main() {
  const commandName: string = process.argv[2];
  if (!commandName) {
    throw new Error('Missing command name argument');
  }

  switch (commandName) {
  case 'start':
    await runCommand(require('./commands/start').command);
    break;
  case 'build':
    await runCommand(require('./commands/build').command);
    break;
  case 'build:frontend':
    await runCommand(require('./commands/build/frontend').command);
    break;
  case 'test':
    await runCommand(require('./commands/test').command);
    break;
  case 'test:unit':
    await runCommand(require('./commands/test/unit').command);
    break;
  case 'test:e2e':
    await runCommand(require('./commands/test/e2e').command);
    break;
  default:
    throw new Error(`Unknown command '${commandName}'`);
  }
}

main().catch(error => {
  log(
    LogCategory.Error,
    '\x1b[31;1mUnable to continue because of the error:\x1b[0m',
    error.message
  );
  throw error;
});
