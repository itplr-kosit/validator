# Website

This folder contains the ui, served by the daemon version of the validator. At
the moment, this is generated within this module and copied to the actual source
location of the daemon. There are plans to modularize the whole validator source
in the future so that this will be done by build process.

This ui is built using [Docusaurus 3](https://docusaurus.io/), a modern static
website generator.

### Local Development

```shell
$ npm start
```

This command starts a local development server and opens up a browser window.
Most changes are reflected live without having to restart the server.

### Build

```shell
$ npm run build
```

This command generates static content into the `build` directory and must be
copied to `src/main/resources/ui`
