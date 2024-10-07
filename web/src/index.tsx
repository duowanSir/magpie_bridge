// src/index.tsx
import React from 'react';
import ReactDOM from 'react-dom';
import MagpieBridgeLogger from './MagpieBridgeLogger';

ReactDOM.render(
    <React.StrictMode>
        <MagpieBridgeLogger />
    </React.StrictMode>,
    document.getElementById('root')
);