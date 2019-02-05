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
