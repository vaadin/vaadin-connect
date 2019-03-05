#!/usr/bin/env node
// We have to keep this .js file here to enable bin symlinking during local
// installation, while the main module is not initially compiled yet.
require('./cli');
