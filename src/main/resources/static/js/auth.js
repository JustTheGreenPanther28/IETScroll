// static/js/auth.js
// Handles authentication verification, role verification & session guards

const RENDER_BASE = 'https://student-community-platform-ietscroll.onrender.com';
const BASE = (window.location.protocol === 'file:' || window.location.origin === 'null' || !window.location.origin)
    ? RENDER_BASE
    : (window.location.port === '4040' ? window.location.origin : RENDER_BASE);

const token = localStorage.getItem('iet_token');

// Redirect to login if unauthenticated
if (!token) {
    window.location.replace('login.html');
    throw new Error('Not authenticated — redirecting to login.');
}

// User state
let currentUserEmail = null;
let currentUserRole = null;
let currentUserPrivilege = null;

// Parse role from JWT token payload safely as an early check
(function checkTokenPayload() {
    try {
        const parts = token.split('.');
        if (parts.length === 3) {
            const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
            if (payload.exp && Date.now() >= payload.exp * 1000) {
                localStorage.removeItem('iet_token');
                window.location.replace('login.html');
                return;
            }
            if (payload.role) {
                currentUserRole = payload.role;
            }
            if (payload.sub) {
                currentUserEmail = payload.sub;
            }
        }
    } catch (e) {
        // Fallback to API profile verification
    }
})();

function isAdmin() {
    return currentUserRole === 'ADMIN' || currentUserPrivilege === 'ADMIN';
}

function checkAdminAccess() {
    if (!isAdmin()) {
        toast('Access denied: Admin privileges required.', 'error');
        show('dashboard');
        return false;
    }
    return true;
}

function logout() {
    api('/api/v1/user/logout', { method: 'POST' }).catch(() => {});
    localStorage.removeItem('iet_token');
    window.location.replace('login.html');
}
