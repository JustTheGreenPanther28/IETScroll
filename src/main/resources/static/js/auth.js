// static/js/auth.js
// Authentication guard, session validation, role helpers
// Depends on: config.js (BASE defined there)

const token = localStorage.getItem('iet_token');

// Redirect immediately if no token present
if (!token) {
    window.location.replace('login.html');
    throw new Error('Not authenticated — redirecting to login.');
}

// ── Shared user-state (mutated by profile.js after /api/v1/user resolves) ────
let currentUserEmail     = null;
let currentUserRole      = null;
let currentUserPrivilege = null;

// ── Early JWT decode (best-effort, before any API call) ──────────────────────
(function validateTokenEarly() {
    try {
        const parts = token.split('.');
        if (parts.length !== 3) return;

        const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));

        // Token expired?
        if (payload.exp && Date.now() >= payload.exp * 1000) {
            localStorage.removeItem('iet_token');
            window.location.replace('login.html');
            return;
        }

        if (payload.role) currentUserRole      = payload.role;
        if (payload.sub)  currentUserEmail     = payload.sub;
    } catch {
        // Silent — profile.js will verify via API call
    }
})();

// ── Role helpers ──────────────────────────────────────────────────────────────
function isAdmin() {
    return currentUserRole === 'ADMIN' || currentUserPrivilege === 'ADMIN';
}

// ── Logout ───────────────────────────────────────────────────────────────────
function logout() {
    // api() is loaded after auth.js, safe to call only on user action
    if (typeof api === 'function') {
        api('/api/v1/user/logout', { method: 'POST' }).catch(() => {});
    }
    localStorage.removeItem('iet_token');
    window.location.replace('login.html');
}
