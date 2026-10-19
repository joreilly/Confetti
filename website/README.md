# Website

The static website served at [confetti-app.dev](https://confetti-app.dev): the landing page, the privacy policy and the GraphQL tools (GraphiQL, Playground, Sandbox). It is a [Vite](https://vite.dev) project.

## Prerequisites

- Node.js, at the version in [`.node-version`](.node-version)
- [pnpm](https://pnpm.io). The exact version comes from the `packageManager` field in `package.json`, so any recent pnpm (or `corepack enable`) works.

## Running locally

```sh
pnpm install
pnpm dev
```

Then open the URL Vite prints (usually http://localhost:5173). Pages reload as you edit them.

To check the production build:

```sh
pnpm build     # type-checks, then writes the site to dist/
pnpm preview   # serves dist/
```

The GraphQL tools query the production API at `https://confetti-app.dev/graphql`, even when you run them locally.

## Layout

- `public/`: hand-written pages and assets, copied to `dist/` without changes.
- `graphiql/`: the GraphiQL page, written in React and bundled by Vite. To add another bundled page, add its HTML file to `build.rolldownOptions.input` in `vite.config.ts`.

## Deploying

CI deploys the site on pushes to `main` that touch `website/`, using `./gradlew :website:uploadWebsite`. That task builds the site and uploads `dist/` to the `confetti-landing-page` GCS bucket. You don't need to deploy by hand.
