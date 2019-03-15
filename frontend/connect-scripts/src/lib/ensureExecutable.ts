// tslint:disable:no-bitwise
import fs = require('fs');

const executableMode = fs.constants.S_IXUSR
  | fs.constants.S_IXGRP
  | fs.constants.S_IXOTH;

/**
 * Makes the file at `filepath` have executable Unix permissions.
 */
export function ensureExecutable(filepath: string): void {
  if (process.platform === 'win32') {
    // Does not support Unix permissions, nothing to do.
    return;
  }

  const fileMode = fs.statSync(filepath).mode;
  const wantedFileMode = fileMode | executableMode;
  if (fileMode !== wantedFileMode) {
    fs.chmodSync(filepath, wantedFileMode);
  }
}
