// static/js/api.js
// Central HTTP client, UI utility helpers, shared renderers
// Depends on: config.js (BASE, PAGE_SIZE), auth.js (token, logout)

// ── Security: HTML escape to prevent XSS ─────────────────────────────────────
function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g,  '&amp;')
        .replace(/</g,  '&lt;')
        .replace(/>/g,  '&gt;')
        .replace(/"/g,  '&quot;')
        .replace(/'/g,  '&#039;');
}

// ── Toast notifications ───────────────────────────────────────────────────────
function toast(msg, type = 'success') {
    const el = document.getElementById('toast');
    if (!el) return;
    el.textContent = msg;
    el.className = 'toast-msg show ' + type;
    clearTimeout(el._toastTimer);
    el._toastTimer = setTimeout(() => el.classList.remove('show'), 3500);
}

// ── Core API fetch wrapper ────────────────────────────────────────────────────
async function api(path, { method = 'GET', jsonBody, formData, params } = {}) {
    let url = BASE + path;

    if (params) {
        const qs = new URLSearchParams(params).toString();
        url += (url.includes('?') ? '&' : '?') + qs;
    }

    const headers = { Authorization: 'Bearer ' + token };
    let body;
    if (jsonBody !== undefined) {
        headers['Content-Type'] = 'application/json';
        body = JSON.stringify(jsonBody);
    } else if (formData) {
        body = formData;
    }

    let res;
    try {
        res = await fetch(url, { method, headers, body });
    } catch {
        throw new Error('Could not reach the server. Check your connection and try again.');
    }

    if (res.status === 401) {
        logout();
        throw new Error('Session expired — please log in again.');
    }

    if (!res.ok) {
        let msg = 'Request failed (' + res.status + ')';
        try {
            const errBody = await res.json();
            msg = errBody.message || errBody.error || msg;
        } catch { /* ignore */ }
        throw new Error(msg);
    }

    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

// ── Pagination helper ─────────────────────────────────────────────────────────
function updatePaginationButtons(prevId, nextId, page, itemCount, res) {
    const prevBtn = document.getElementById(prevId);
    const nextBtn = document.getElementById(nextId);
    if (prevBtn) prevBtn.disabled = page <= 0;

    let isLastPage;
    if (res && typeof res.last === 'boolean') {
        isLastPage = res.last;
    } else if (res && typeof res.totalPages === 'number') {
        isLastPage = page >= res.totalPages - 1;
    } else {
        isLastPage = itemCount < PAGE_SIZE;
    }
    if (nextBtn) nextBtn.disabled = isLastPage;
}

// ── Date/time formatter ───────────────────────────────────────────────────────
function formatNotifTime(isoString) {
    try {
        const d = new Date(isoString);
        return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })
             + ' at '
             + d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
    } catch {
        return isoString || 'Recently';
    }
}

// ── Privilege badge renderer ──────────────────────────────────────────────────
function renderPrivilegeBadge(privilege) {
    const p = (privilege || 'NORMAL').toUpperCase();
    const map = {
        TRUSTED:     { cls: 'badge-trusted', icon: 'bi-patch-check-fill' },
        ADMIN:       { cls: 'badge-admin',   icon: 'bi-shield-fill-check' },
        MAY_BE_SPAM: { cls: 'badge-spam',    icon: 'bi-exclamation-triangle-fill' },
        NEW:         { cls: 'badge-new',     icon: 'bi-sparkles' },
    };
    const { cls, icon } = map[p] || { cls: 'badge-normal', icon: 'bi-person' };
    return `<span class="privilege-badge ${cls}"><i class="bi ${icon}"></i> ${escapeHtml(p.replace(/_/g, ' '))}</span>`;
}

// ── Image file validation ─────────────────────────────────────────────────────
function validateImageFile(file) {
    if (!file) return 'Please choose an image file.';
    const allowed = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    if (!allowed.includes(file.type)) return 'Invalid file type. Allowed: JPG, PNG, WEBP, GIF.';
    if (file.size > 5 * 1024 * 1024) return 'File is too large. Maximum allowed size is 5 MB.';
    return null;
}

// ── Live image preview wiring ─────────────────────────────────────────────────
function wireImagePreview(inputId, previewId) {
    const input   = document.getElementById(inputId);
    const preview = document.getElementById(previewId);
    if (!input || !preview) return;

    input.addEventListener('change', () => {
        preview.innerHTML = '';
        const file = input.files[0];
        if (!file) return;
        const err = validateImageFile(file);
        if (err) { toast(err, 'error'); input.value = ''; return; }
        const url = URL.createObjectURL(file);
        preview.innerHTML = `<img src="${url}" class="img-preview mt-2" alt="Preview">`;
    });
}
