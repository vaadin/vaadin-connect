const path = require('path');

// Enable .ts file loading with ts-node
require('ts-node').register({
  project: path.resolve(__dirname, '../tsconfig.json'),
  compilerOptions: {
    module: 'CommonJS'
  }
});
