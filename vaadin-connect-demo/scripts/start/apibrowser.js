const {URL} = require('url');
const {spawn} = require('child_process');
const express = require('express');
const polyserve = require('polyserve');
const httpProxy = require('http-proxy-middleware');
const fs = require('fs');

const BACKEND = 'http://localhost:8080';
const CONNECT_HOSTNAME = process.env.CONNECT_HOSTNAME;
const CONNECT_PORT = process.env.CONNECT_PORT || 8082;
const backend = makeProxyApp(BACKEND);
const backendEndpoints = [
  '/oauth/',
  '/connect/'
];

const endOfOptionsIndex = process.argv.indexOf('--');
const [chainedExecutable, ...chainedArgs] = endOfOptionsIndex > -1
  ? process.argv.slice(endOfOptionsIndex + 1)
  : [];

function makeProxyApp(target) {
  return httpProxy({target, logLevel: 'warn', changeOrigin: true, xfwd: true});
}

function renderOpenApiHtml() {
  const openApiJson = fs.readFileSync('target/generated-resources/openapi.json', 'utf8');
  return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>OpenApi UI</title>
    <link rel="stylesheet" type="text/css" href="./node_modules/swagger-ui-dist/swagger-ui.css" >
</head>

<body>
    <div id="swagger-ui"></div>

    <script src="./node_modules/swagger-ui-dist/swagger-ui-bundle.js"> </script>
    <script>
      window.onload = function() {
        window.ui = SwaggerUIBundle({
          spec: ${openApiJson},
          "dom_id": "#swagger-ui",
          requestInterceptor: request => {
            request.url = request.url.replace('${BACKEND}', 'http://${CONNECT_HOSTNAME || 'localhost'}:${CONNECT_PORT}');
            return request;
          },
          showMutatedRequest: false,
        })
      }
    </script>
</body>
</html>`;
}

polyserve
  .startServers(
    {
      componentDir: '../node_modules',
      componentUrl: 'node_modules',
      moduleResolution: 'node',
      npm: true,
      hostname: CONNECT_HOSTNAME,
      port: CONNECT_PORT
    },
    async(polyserveApp, options) => {
      const app = express();

      app.get('/', (req, res) => {
        res.send(renderOpenApiHtml());
      });
      backendEndpoints.forEach(path => app.use(path, backend));
      app.use(polyserveApp);
      return app;
    }
  )
  .then(({server, options}) => {
    const scheme = /^(h2$|https)/.test(options.protocol) ? 'https:' : 'http:';
    const {address, port} = server.address();
    const hostname = options.hostname || address;
    const url = new URL(`${scheme}//${hostname}:${port}`);
    console.log(`Started Vaadin Connect OpenApi UI at: ${url}`);
    if (chainedExecutable) {
      spawn(
        chainedExecutable,
        chainedArgs,
        {stdio: 'inherit', shell: true}
      ).on('close', code => {
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
