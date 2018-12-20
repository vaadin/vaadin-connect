const {URL} = require('url');
const {spawn} = require('child_process');
const express = require('express');
const httpProxy = require('http-proxy-middleware');
const fs = require('fs');

const BACKEND = 'http://localhost:8080';
const CONNECT_API_HOSTNAME = process.env.CONNECT_API_HOSTNAME || 'localhost';
const CONNECT_API_PORT = process.env.CONNECT_API_PORT || 8082;

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

const app = express();
backendEndpoints.forEach(path => app.use(path, backend));
app.get(['/', '/index.html'], (req, res) => {
  res.send(renderOpenApiHtml());
});
const pathToSwaggerUi = require('swagger-ui-dist').absolutePath();
app.use(express.static(pathToSwaggerUi));

const server = require('http').createServer(app);
server.listen(CONNECT_API_PORT, CONNECT_API_HOSTNAME, () => {
  const {address, port} = server.address();
  const hostname = CONNECT_API_HOSTNAME || address;
  const url = new URL(`http://${hostname}:${port}`);
  console.log(`Started Vaadin Connect OpenApi UI at: ${url}`);
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
