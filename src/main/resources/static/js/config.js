// static/js/config.js
// Application-wide configuration constants — load this FIRST before all other scripts

// ── API Base URL ──────────────────────────────────────────────────────────────
// Points to localhost:4040 in local dev, production Render URL otherwise
const RENDER_BASE = 'https://student-community-platform-ietscroll.onrender.com';

const BASE = (function resolveBase() {
    const proto = window.location.protocol;
    const port  = window.location.port;
    const origin = window.location.origin;
    if (proto === 'file:' || !origin || origin === 'null') return RENDER_BASE;
    if (port === '4040') return origin;   // local dev
    return RENDER_BASE;                   // production
})();

// ── Pagination ────────────────────────────────────────────────────────────────
const PAGE_SIZE = 10;

// ── App metadata ──────────────────────────────────────────────────────────────
const APP_NAME    = 'IET Scroll';
const APP_VERSION = '2.0.0';
