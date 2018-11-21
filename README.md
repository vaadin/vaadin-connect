# vaadin-connect
A Vaadin Labs experiment with a secure stateless communication framework

## Dependencies

Java dependencies are installed automatically with Maven.

Use Yarn to install the JS dependencies:

    $ yarn install

## Running the Demo Application

Use the following command to run the Java backend and the frontend development
server simultaniously for the demo project:

    $ yarn start

After the server is up and running, open
<a href="http://localhost:8081" target="_blank">http://localhost:8081</a> in
your browser.

## Tests

To run Java tests:

    $ mvn verify

To run JS tests:

    $ yarn test

