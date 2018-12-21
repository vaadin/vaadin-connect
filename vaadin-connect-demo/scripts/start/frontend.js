#!/usr/bin/env node
/**
 * Starts the frontend server and executes the optional chained command while
 * the frontend server is running.
 *
 * Usage:
 *   $ node scripts/start/frontend.js
 *   $ node scripts/start/frontend.js -- echo "The frontend is running..."
 */

const {URL} = require('url');
const {spawn} = require('child_process');

const endOfOptionsIndex = process.argv.indexOf('--');
const [chainedExecutable, ...chainedArgs] = endOfOptionsIndex > -1
  ? process.argv.slice(endOfOptionsIndex + 1)
  : [];

const express = require('express');
const webpackConfig = Object.assign(
  {},
  require('../../webpack.config.js'),
  {mode: 'development'}
);
const webpackCompiler = require('webpack')(webpackConfig);
const webpackDevMiddleware = require('webpack-dev-middleware');
const httpProxy = require('http-proxy-middleware');

function makeProxyApp(target) {
  return httpProxy({target, logLevel: 'warn', changeOrigin: true, xfwd: true});
}

const backend = makeProxyApp('http://localhost:8080');

const backendEndpoints = [
  '/oauth/',
  '/connect/'
];

const CONNECT_HOSTNAME = process.env.CONNECT_HOSTNAME || 'localhost';
const CONNECT_PORT = process.env.CONNECT_PORT || 8081;

const app = express();
backendEndpoints.forEach(path => app.use(path, backend));
app.use('/__intern/', makeProxyApp('http://localhost:9002'));
app.use(webpackDevMiddleware(webpackCompiler));

const server = require('http').createServer(app);
server.listen(CONNECT_PORT, CONNECT_HOSTNAME, () => {
  const {address, port} = server.address();
  const hostname = CONNECT_HOSTNAME || address;
  const url = new URL(`http://${hostname}:${port}`);
  console.log(`Started Vaadin Connect frontend server at: ${url}`);
  if (chainedExecutable) {
    const chainedProcess = spawn(
      chainedExecutable,
      chainedArgs,
      {stdio: 'inherit', shell: true}
    );
    chainedProcess.on('close', code => {
      server.close();
      process.exit(code);
    });
  }
});

// Make exit on Ctrl+C / SIGINT
process.on('SIGINT', () => process.exit(0));
