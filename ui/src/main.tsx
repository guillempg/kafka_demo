import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

async function prepare() {
  if (import.meta.env.MODE === 'true') {
    const { worker } = await import('./mocks/browser');
    return worker.start();
  }
}
prepare().then(() => {
  ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
    <React.StrictMode>
      <App />
    </React.StrictMode>
  );
});
