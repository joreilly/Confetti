import { createRoot } from 'react-dom/client';
import { GraphiQL } from 'graphiql';
import { createTransport } from '@graphiql/toolkit';
import 'graphiql/setup-workers/vite';
import 'graphiql/style.css';

const transport = createTransport({
    url: 'https://confetti-app.dev/graphql',
});

createRoot(document.getElementById('graphiql')!).render(
    <GraphiQL transport={transport} defaultEditorToolsVisibility />,
);
