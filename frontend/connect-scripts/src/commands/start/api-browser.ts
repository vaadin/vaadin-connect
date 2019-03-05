import {Command} from '../../lib/command';
import {log, LogCategory} from '../../lib/log';

import {URL} from 'url';

import * as express from 'express';
import {Request, Response} from 'express';

import httpProxy = require('http-proxy-middleware');

import fs = require('fs');
import util = require('util');

const BACKEND = 'http://localhost:8080';
const CONNECT_API_HOSTNAME = process.env.CONNECT_API_HOSTNAME || 'localhost';
const CONNECT_API_PORT = process.env.CONNECT_API_PORT || 8082;

const backend = makeProxyApp(BACKEND);
const backendEndpoints = [
  '/oauth/',
  '/connect/'
];

function makeProxyApp(target: string) {
  return httpProxy({target, logLevel: 'warn', changeOrigin: true, xfwd: true});
}

function renderOpenApiHtml() {
  const openApiJson = fs.readFileSync(
    'target/generated-resources/openapi.json',
    'utf8'
  );
  return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>OpenApi UI</title>
    <link rel="stylesheet" type="text/css" href="./swagger-ui.css" >
</head>

<body>
    <div id="swagger-ui"></div>

    <script src="./swagger-ui-bundle.js"> </script>
    <script>
      window.onload = function() {
        window.ui = SwaggerUIBundle({
          spec: ${openApiJson},
          "dom_id": "#swagger-ui",
          requestInterceptor: request => {
            request.url = request.url.replace(
              '${BACKEND}',
              'http://${CONNECT_API_HOSTNAME}:${CONNECT_API_PORT}'
            );
            return request;
          },
          showMutatedRequest: false,
        })
      }
    </script>
</body>
</html>`;
}

/**
 * Runs the API browser server
 */
export const command: Command = async() => {
  log(LogCategory.Progress, 'Starting the API browser...');

  const app = express();

  // Add backend proxy
  backendEndpoints.forEach(path => app.use(path, backend));

  // Add index page
  app.get(['/', '/index.html'], (_: Request, res: Response) => {
    res.send(renderOpenApiHtml());
  });

  // Add static resources
  const pathToSwaggerUi = require('swagger-ui-dist').absolutePath();
  app.use(express.static(pathToSwaggerUi));

  // Start the server
  const server = require('http').createServer(app);
  const listen = util.promisify(server.listen);
  await listen.call(server, CONNECT_API_PORT, CONNECT_API_HOSTNAME);

  // Log the started server url
  const {address, port} = server.address();
  const hostname = CONNECT_API_HOSTNAME || address;
  const url = new URL(`http://${hostname}:${port}`);
  log(LogCategory.Success, `The API browser is running at: ${url}`);

  return () => {
    log(LogCategory.Progress, 'Stopping the API browser...');
    // Cleanup
    server.close();
  };
};
