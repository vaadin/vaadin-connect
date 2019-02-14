const fs = require('fs');
const path = require('path');
const version = require('../package.json').version;

const opts = {encoding: 'utf8'};

const metaModulePathname = path.resolve(
  process.cwd(),
  'src',
  'connect-meta.ts'
);

fs.writeFileSync(
  metaModulePathname,
  fs.readFileSync(metaModulePathname, opts)
    .replace(/version: '.*'/, `version: '${version}'`),
  opts
);

// Stage this change for the new version tag commit. The same as
// `$ git add src/connect-meta.ts, but using git plumbing command.
require('child_process').execSync(`git update-index -- ${metaModulePathname}`);
