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
const polyserve = require('polyserve');
const httpProxy = require('http-proxy-middleware');

function makeProxyApp(target) {
  return httpProxy({target, logLevel: 'warn', changeOrigin: true, xfwd: true});
}

const backend = makeProxyApp('http://localhost:8080');

const backendEndpoints = [
  '/oauth/',
  '/connect/'
];

const CONNECT_HOSTNAME = process.env.CONNECT_HOSTNAME;
const CONNECT_PORT = process.env.CONNECT_PORT || 8081;

polyserve
  .startServers(
    {
      root: 'frontend',
      componentDir: '../../node_modules',
      componentUrl: 'node_modules',
      moduleResolution: 'node',
      npm: true,
      hostname: CONNECT_HOSTNAME,
      port: CONNECT_PORT
    },
    async(polyserveApp, options) => {
      const app = express();
      backendEndpoints.forEach(path => app.use(path, backend));
      app.use('/__intern/', makeProxyApp('http://localhost:9000'));
      app.use(express.static('static'));
      app.use(polyserveApp);
      return app;
    }
  )
  .then(({server, options}) => {
    const scheme = /^(h2$|https)/.test(options.protocol) ? 'https:' : 'http:';
    const {address, port} = server.address();
    const hostname = options.hostname || address;
    const url = new URL(`${scheme}//${hostname}:${port}`);
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
    } else {
      // Make exit on Ctrl+C / SIGINT
      process.on('SIGINT', () => process.exit(0));
    }
  })
  .catch(reason => {
    console.error(reason);
    process.exit(1);
  });
