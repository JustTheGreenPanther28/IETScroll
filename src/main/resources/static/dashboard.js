const RENDER_BASE = 'https://student-community-platform-ietscroll.onrender.com';
const BASE = (window.location.protocol === 'file:' || window.location.origin === 'null' || !window.location.origin)
    ? RENDER_BASE
    : (window.location.port === '4040' ? window.location.origin : RENDER_BASE);
const token = localStorage.getItem('iet_token');
if (!token) {
    window.location.replace('login.html');
    throw new Error('Not authenticated — redirecting to login.');
}

let currentUserEmail = null;
let currentUserRole = null;
let currentUserPrivilege = null;

// Escaping helper for XSS defense
function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function toast(msg, type = 'success') {
    const el = document.getElementById('toast');
    if (!el) return;
    el.textContent = msg;
    el.className = 'toast-msg show ' + type;
    setTimeout(() => el.classList.remove('show'), 3500);
}

// Central fetch wrapper
async function api(path, { method = 'GET', jsonBody, formData, params } = {}) {
    let url = BASE + path;
    if (params) {
        const qs = new URLSearchParams(params).toString();
        url += (url.includes('?') ? '&' : '?') + qs;
    }
    const headers = { 'Authorization': 'Bearer ' + token };
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
    } catch (networkErr) {
        throw new Error('Could not reach the server. Check your connection and try again.');
    }
	
    if (res.status === 401) {
        logout();
        throw new Error('Session expired');
    }
    if (!res.ok) {
        let msg = 'Request failed (' + res.status + ')';
        try {
            const errBody = await res.json();
            msg = errBody.message || errBody.error || msg;
        } catch {}
        throw new Error(msg);
    }
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

const PAGE_SIZE = 10;

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

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    if (!sidebar) return;
    const isOpen = sidebar.classList.toggle('open');
    if (overlay) overlay.classList.toggle('active', isOpen);
}

function show(id, el) {
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    const targetSection = document.getElementById(id);
    if (targetSection) {
        targetSection.classList.add('active');
    }

    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    let targetLink = el;
    if (!targetLink) {
        targetLink = document.querySelector(`.nav-link[onclick*="'${id}'"]`);
    }
    if (targetLink) targetLink.classList.add('active');

    const titles = {
        dashboard: 'Overview',
        users: 'Users Audit & Governance',
        profile: 'Profile',
        notifications: 'Activity Log',
        lost: 'Lost Items',
        found: 'Found Items',
        team: 'Team Finder',
        resume: 'Resume Checker'
    };
    const titleEl = document.getElementById('header-title');
    if (titleEl) titleEl.innerText = titles[id] || (id.charAt(0).toUpperCase() + id.slice(1));

    // Close mobile sidebar and overlay
    const sidebar = document.getElementById('sidebar');
    if (sidebar) sidebar.classList.remove('open');
    const overlay = document.getElementById('sidebarOverlay');
    if (overlay) overlay.classList.remove('active');

    window.scrollTo({ top: 0, behavior: 'smooth' });

    if (id === 'dashboard') { loadDashboardStats(); if (currentUserRole === 'ADMIN') loadAdminAnalytics(); }
    if (id === 'users') loadUsersTable();
    if (id === 'notifications') { markAllNotificationsRead(); renderNotifications(); }
    if (id === 'lost') loadLostBrowse();
    if (id === 'found') loadFoundBrowse();
    if (id === 'team') loadTeamBrowse();
}

function switchTab(prefix, tabId) {
    document.querySelectorAll(`[id^="${prefix}-"]`).forEach(el => {
        if (el.classList.contains('tab-pane')) el.classList.add('d-none');
    });
    const pane = document.getElementById(tabId);
    if (pane) pane.classList.remove('d-none');
}

function switchLostTab(sub, btn) {
    document.querySelectorAll('#lost .tab-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    switchTab('lost', 'lost-' + sub);
    if (sub === 'browse') loadLostBrowse();
    if (sub === 'mine') loadLostMine();
}

function switchFoundTab(sub, btn) {
    document.querySelectorAll('#found .tab-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    switchTab('found', 'found-' + sub);
    if (sub === 'browse') loadFoundBrowse();
    if (sub === 'mine') loadFoundMine();
}

function switchTeamTab(sub, btn) {
    document.querySelectorAll('#team .tab-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    switchTab('team', 'team-' + sub);
    if (sub === 'browse') loadTeamBrowse();
    if (sub === 'mine') loadTeamMine();
    if (sub === 'new') loadSkillsDropdown();
    if (sub === 'requests') loadTeamRequests();
    if (sub === 'applications') loadTeamApplications();
}

function validateImageFile(file) {
    if (!file) return 'Please choose an image file.';
    const allowed = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    if (!allowed.includes(file.type)) return 'Invalid file type. Allowed: JPG, PNG, WEBP, GIF.';
    if (file.size > 5 * 1024 * 1024) return 'File is too large. Maximum allowed size is 5 MB.';
    return null;
}

function wireImagePreview(inputId, previewId) {
    const input = document.getElementById(inputId);
    const preview = document.getElementById(previewId);
    if (!input || !preview) return;
    input.addEventListener('change', () => {
        preview.innerHTML = '';
        const file = input.files[0];
        if (!file) return;
        const err = validateImageFile(file);
        if (err) {
            toast(err, 'error');
            input.value = '';
            return;
        }
        const url = URL.createObjectURL(file);
        preview.innerHTML = `<img src="${url}" class="img-preview mt-2" alt="Preview">`;
    });
}

function renderPrivilegeBadge(privilege) {
    const p = (privilege || 'NORMAL').toUpperCase();
    let badgeClass = 'badge-normal';
    let icon = 'bi-person';
    if (p === 'TRUSTED') { badgeClass = 'badge-trusted'; icon = 'bi-patch-check-fill'; }
    else if (p === 'ADMIN') { badgeClass = 'badge-admin'; icon = 'bi-shield-fill-check'; }
    else if (p === 'MAY_BE_SPAM') { badgeClass = 'badge-spam'; icon = 'bi-exclamation-triangle-fill'; }
    else if (p === 'NEW') { badgeClass = 'badge-new'; icon = 'bi-sparkles'; }

    return `<span class="privilege-badge ${badgeClass}"><i class="bi ${icon}"></i> ${escapeHtml(p.replace(/_/g, ' '))}</span>`;
}

/* ============ PROFILE / USER ============ */
async function loadProfile() {
    try {
        const user = await api('/api/v1/user');
        currentUserEmail = user.email || null;
        currentUserRole = user.role || 'STUDENT';
        currentUserPrivilege = user.privilegeClass || 'NORMAL';

        document.getElementById('welcome-name').innerText = user.fullName || user.username || 'Student';
        document.getElementById('top-username').innerText = user.fullName || user.username || '';
        document.getElementById('top-email').innerText = user.email || '';
        document.getElementById('p-name').innerText = user.fullName || '—';
        document.getElementById('p-username').innerText = user.username || '—';
        document.getElementById('p-email').innerText = user.email || '—';

        const privContainer = document.getElementById('top-privilege-badge');
        if (privContainer) {
            privContainer.innerHTML = renderPrivilegeBadge(currentUserPrivilege);
        }

        const initial = (user.fullName || user.username || '?').charAt(0).toUpperCase();
        document.getElementById('avatar-icon').innerText = initial;
        document.getElementById('avatar-icon-mobile').innerText = initial;

        // Reveal Admin features if Role is ADMIN
        if (currentUserRole === 'ADMIN') {
            const navUsers = document.getElementById('nav-users');
            if (navUsers) navUsers.classList.remove('d-none');
            const adminAnalytics = document.getElementById('adminAnalyticsSection');
            if (adminAnalytics) adminAnalytics.classList.remove('d-none');
            loadAdminAnalytics();
        }

        updateNotifBadges();
    } catch (e) {
        toast('Could not load profile: ' + e.message, 'error');
    }
}

async function updateUsername() {
    const val = document.getElementById('newUsernameInput').value.trim();
    if (!val) return toast('Enter a username first', 'error');
    try {
        await api('/api/v1/user/username/' + encodeURIComponent(val), { method: 'PATCH' });
        toast('Username updated');
        document.getElementById('newUsernameInput').value = '';
        loadProfile();
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function updateFullname() {
    const val = document.getElementById('newFullnameInput').value.trim();
    if (!val) return toast('Enter a full name first', 'error');
    try {
        await api('/api/v1/user/fullname/' + encodeURIComponent(val), { method: 'PATCH' });
        toast('Full name updated');
        document.getElementById('newFullnameInput').value = '';
        loadProfile();
    } catch (e) {
        toast(e.message, 'error');
    }
}

/* ============ DASHBOARD STATS ============ */
async function loadDashboardStats() {
    try {
        const [myLost, myFound] = await Promise.all([
            api('/api/v1/lost-item/me'),
            api('/api/v1/found-item/me'),
        ]);
        document.getElementById('lostCount').innerText = (myLost || []).length;
        document.getElementById('foundCount').innerText = (myFound || []).length;
    } catch (e) {
        document.getElementById('lostCount').innerText = '–';
        document.getElementById('foundCount').innerText = '–';
    }
    try {
        const myTeam = await api('/api/v1/team/me');
        document.getElementById('teamStatus').innerText = myTeam ? 'Active Member' : 'No Active Team';
    } catch {
        document.getElementById('teamStatus').innerText = 'No Active Team';
    }
}

/* ============ SHARED ITEM CARD RENDERER ============ */
function renderItemCard(item, { nameKey, publicIdKey, imageKey, locationKey, extra, onClose, emailKey, privilegeKey, reportType }) {
    const wrap = document.createElement('div');
    wrap.className = 'item-card';

    const safeImg = item[imageKey] ? encodeURI(item[imageKey]) : null;
    const imgHtml = safeImg
        ? '<a href="' + safeImg + '" target="_blank" rel="noopener noreferrer"><img src="' + safeImg + '" alt="Item Photo"></a>'
        : '<div class="item-card-fallback-img"><i class="bi bi-box-seam"></i></div>';

    const locVal = (item[locationKey] && String(item[locationKey]).trim())
        ? String(item[locationKey]).trim()
        : 'Location not specified';

    const locationBadge = '<span class="meta-chip location"><i class="bi bi-geo-alt-fill text-danger me-1"></i>Location: ' + escapeHtml(locVal) + '</span>';

    const rewardBadge = (item.prize && item.prize > 0)
        ? '<span class="meta-chip reward"><i class="bi bi-gift-fill"></i>Reward: ₹' + item.prize + '</span>'
        : '';

    const emailBadge = (emailKey && item[emailKey])
        ? '<a href="mailto:' + encodeURI(item[emailKey]) + '" class="meta-chip contact"><i class="bi bi-envelope-fill"></i>' + escapeHtml(item[emailKey]) + '</a>'
        : '';

    const privilegeBadge = privilegeKey && item[privilegeKey]
        ? renderPrivilegeBadge(item[privilegeKey])
        : '';

    const reportId = item[publicIdKey];
    const viewsPill = reportId
        ? `<button class="views-pill-btn" onclick="openViewsModal('${reportId}', '${reportType || 'LOST'}')"><i class="bi bi-eye"></i> Views Audit</button>`
        : '';

    // Record view asynchronously if viewer is not the creator
    if (reportId && currentUserEmail && item[emailKey] && currentUserEmail !== item[emailKey]) {
        api(`/api/v1/report-views/${reportId}?type=${reportType || 'LOST'}`, { method: 'POST' }).catch(() => {});
    }

    // Admin universal closure check
    const canClose = onClose || (currentUserRole === 'ADMIN');
    const closeBtn = canClose
        ? `<button class="btn-danger-soft ms-auto flex-shrink-0 close-card-btn"><i class="bi bi-x-circle"></i> ${currentUserRole === 'ADMIN' && !onClose ? 'Admin Close' : 'Close'}</button>`
        : '';

    wrap.innerHTML = 
        imgHtml +
        '<div class="flex-grow-1">' +
            '<div class="item-title d-flex align-items-center gap-2 flex-wrap">' +
                '<span>' + escapeHtml(item[nameKey] || 'Untitled Item') + '</span>' +
                privilegeBadge +
                rewardBadge +
            '</div>' +
            '<div class="item-desc">' + escapeHtml(item.description || 'No description provided.') + '</div>' +
            '<div class="item-meta-row mt-2 d-flex align-items-center gap-2 flex-wrap">' +
                locationBadge +
                emailBadge +
                viewsPill +
            '</div>' +
        '</div>' +
        closeBtn;

    if (canClose) {
        const btn = wrap.querySelector('.close-card-btn');
        if (btn) {
            btn.addEventListener('click', () => {
                if (onClose) {
                    onClose(item[publicIdKey]);
                } else if (currentUserRole === 'ADMIN') {
                    adminCloseReport(item[publicIdKey], reportType || 'LOST');
                }
            });
        }
    }
    return wrap;
}

/* ============ ADMIN UNIVERSAL CLOSURE ACTIONS ============ */
async function adminCloseReport(publicId, type) {
    if (!confirm(`Are you sure you want to close this ${type.toLowerCase()} report as Administrator?`)) return;
    try {
        if (type === 'LOST') {
            await api(`/api/v1/admin/lost-item/${publicId}/close`, { method: 'PATCH' });
            toast('Lost report closed by Admin');
            loadLostBrowse();
        } else if (type === 'FOUND') {
            await api(`/api/v1/admin/found-item/${publicId}/close`, { method: 'PATCH' });
            toast('Found report closed by Admin');
            loadFoundBrowse();
        } else if (type === 'TEAM') {
            await api(`/api/v1/admin/team/${publicId}/close`, { method: 'PATCH' });
            toast('Team closed by Admin');
            loadTeamBrowse();
        }
    } catch (e) {
        toast('Admin close failed: ' + e.message, 'error');
    }
}

/* ============ VIEWS AUDIT MODAL ============ */
async function openViewsModal(reportId, type) {
    const listEl = document.getElementById('viewsModalList');
    listEl.innerHTML = '<div class="spinner-inline"></div> Fetching viewers...';
    const modalEl = document.getElementById('viewsModal');
    if (window.bootstrap && bootstrap.Modal) {
        const m = bootstrap.Modal.getOrCreateInstance(modalEl);
        m.show();
    } else {
        modalEl.classList.add('show');
        modalEl.style.display = 'block';
    }

    try {
        const views = await api(`/api/v1/report-views/${reportId}`);
        if (!views || !views.length) {
            listEl.innerHTML = '<div class="text-muted small py-3"><i class="bi bi-eye-slash me-1"></i> No one has viewed this report yet.</div>';
            return;
        }
        listEl.innerHTML = '';
        views.forEach(v => {
            const item = document.createElement('div');
            item.className = 'p-2 border border-secondary border-opacity-25 rounded bg-black bg-opacity-25 d-flex justify-content-between align-items-center';
            item.innerHTML = `
                <div>
                    <div class="fw-semibold text-white small">${escapeHtml(v.viewerName || v.viewerEmail)}</div>
                    <div class="text-muted" style="font-size: 0.75rem;">${escapeHtml(v.viewerEmail)}</div>
                </div>
                <div class="text-muted" style="font-size: 0.72rem;">${formatNotifTime(v.viewedAt)}</div>
            `;
            listEl.appendChild(item);
        });
    } catch (e) {
        listEl.innerHTML = `<div class="text-danger small">${escapeHtml(e.message)}</div>`;
    }
}

function closeViewsModal() {
    const modalEl = document.getElementById('viewsModal');
    if (window.bootstrap && bootstrap.Modal) {
        const m = bootstrap.Modal.getInstance(modalEl);
        if (m) m.hide();
    }
    modalEl.classList.remove('show');
    modalEl.style.display = 'none';
}

/* ============ LOST ITEMS ============ */
let lostPage = 0;

async function loadLostBrowse() {
    const container = document.getElementById('lostBrowseList');
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const res = await api('/api/v1/lost-item', { params: { page: lostPage, size: PAGE_SIZE } });
        const items = res.content || res.items || [];
        container.innerHTML = '';
        document.getElementById('lostPageLabel').innerText = 'Page ' + (lostPage + 1);
        updatePaginationButtons('lostPrevBtn', 'lostNextBtn', lostPage, items.length, res);
        if (!items.length) {
            container.innerHTML = `<div class="empty-state"><i class="bi bi-search"></i>No open lost item requests right now.</div>`;
            return;
        }
        items.forEach(item => {
            container.appendChild(renderItemCard(item, {
                nameKey: 'lostItemname', publicIdKey: 'publicIdOfLostRequest',
                imageKey: 'imageURLOfItem', locationKey: 'predictedLocation',
                emailKey: 'contactTo', privilegeKey: 'ownerPrivilege',
                reportType: 'LOST',
                extra: i => i.prize ? 'Reward: ' + i.prize : ''
            }));
        });
    } catch (e) {
        container.innerHTML = `<div class="empty-state text-danger">Could not load lost items: ${escapeHtml(e.message)}</div>`;
    }
}
function changeLostPage(delta) {
    const next = lostPage + delta;
    if (next < 0) return;
    lostPage = next;
    loadLostBrowse();
}

async function loadLostMine() {
    const container = document.getElementById('lostMineList');
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const items = await api('/api/v1/lost-item/me');
        container.innerHTML = '';
        if (!items.length) {
            container.innerHTML = `<div class="empty-state"><i class="bi bi-inbox"></i>You haven't reported any lost items yet.</div>`;
            return;
        }
        items.forEach(item => {
            container.appendChild(renderItemCard(item, {
                nameKey: 'lostItemname', publicIdKey: 'publicIdOfLostRequest',
                imageKey: 'imageURL', locationKey: 'predictedLocation',
                emailKey: 'ownerEmail', reportType: 'LOST',
                onClose: closeLostItem
            }));
        });
    } catch (e) {
        container.innerHTML = `<div class="empty-state text-danger">${escapeHtml(e.message)}</div>`;
    }
}

async function closeLostItem(publicId) {
    try {
        await api('/api/v1/lost-item/close', { method: 'PATCH', params: { lostItemId: publicId } });
        toast('Lost item request closed');
        loadLostMine();
        loadDashboardStats();
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function submitLostItem() {
    const name = document.getElementById('lostItemName').value.trim();
    const desc = document.getElementById('lostDescription').value.trim();
    const location = document.getElementById('lostLocation').value.trim();
    const prizeRaw = document.getElementById('lostPrize').value.trim();
    const parsedPrize = parseInt(prizeRaw, 10);
    const prize = (!isNaN(parsedPrize) && parsedPrize >= 0) ? parsedPrize : 0;
    const imageFile = document.getElementById('lostItemImage').files[0];

    if (!name || !desc) {
        return toast('Item name and description are required', 'error');
    }
    if (!location) {
        return toast('Location near college is required! Submissions without location are marked as spam.', 'error');
    }
    if (name.length < 3) {
        return toast('Item name must be at least 3 characters', 'error');
    }
    if (desc.length < 20) {
        return toast('Please provide a detailed description (at least 20 characters)', 'error');
    }
    const imgError = validateImageFile(imageFile);
    if (imgError) return toast(imgError, 'error');

    const data = { lostItemname: name, description: desc, predictedLocation: location, prize: prize };
    const fd = new FormData();
    fd.append('data', JSON.stringify(data));
    fd.append('image', imageFile);

    const btn = document.getElementById('lostSubmitBtn');
    const lostBanner = document.getElementById('lostAnalyzingBanner');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-inline me-2"></span>Analyzing…';
    if (lostBanner) lostBanner.classList.remove('d-none');

    try {
        await api('/api/v1/lost-item', { method: 'POST', formData: fd });
        toast('Lost item reported successfully');
        addNotification({
            type: 'lost',
            title: name,
            status: 'UPLOADED',
            message: 'Lost item verified and published. Location: ' + location,
            time: new Date()
        });
        document.getElementById('lostItemName').value = '';
        document.getElementById('lostDescription').value = '';
        document.getElementById('lostLocation').value = '';
        document.getElementById('lostPrize').value = '0';
        document.getElementById('lostItemImage').value = '';
        document.getElementById('lostItemImagePreview').innerHTML = '';
        lostPage = 0;
        loadDashboardStats();
        switchLostTab('mine', document.querySelectorAll('#lost .tab-btn')[1]);
    } catch (e) {
        toast(e.message, 'error');
        addNotification({
            type: 'lost',
            title: name || 'Lost Item Report',
            status: 'REJECTED',
            message: e.message || 'Report was rejected during safety verification.',
            time: new Date()
        });
    } finally {
        if (lostBanner) lostBanner.classList.add('d-none');
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-cloud-arrow-up me-1"></i> Submit Report';
    }
}

/* ============ FOUND ITEMS ============ */
let foundPage = 0;

async function loadFoundBrowse() {
    const container = document.getElementById('foundBrowseList');
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const res = await api('/api/v1/found-item', { params: { page: foundPage, size: PAGE_SIZE } });
        const items = res.content || res.items || [];
        container.innerHTML = '';
        document.getElementById('foundPageLabel').innerText = 'Page ' + (foundPage + 1);
        updatePaginationButtons('foundPrevBtn', 'foundNextBtn', foundPage, items.length, res);
        if (!items.length) {
            container.innerHTML = `<div class="empty-state"><i class="bi bi-check-lg"></i>No pending found items right now.</div>`;
            return;
        }
        items.forEach(item => {
            container.appendChild(renderItemCard(item, {
                nameKey: 'foundItemName', publicIdKey: 'publicIdOfFoundItem',
                imageKey: 'imageURL', locationKey: 'predictedLocation',
                emailKey: 'contactTo', privilegeKey: 'reporterPrivilege',
                reportType: 'FOUND'
            }));
        });
    } catch (e) {
        container.innerHTML = `<div class="empty-state text-danger">Could not load found items: ${escapeHtml(e.message)}</div>`;
    }
}
function changeFoundPage(delta) {
    const next = foundPage + delta;
    if (next < 0) return;
    foundPage = next;
    loadFoundBrowse();
}

async function loadFoundMine() {
    const container = document.getElementById('foundMineList');
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const items = await api('/api/v1/found-item/me');
        container.innerHTML = '';
        if (!items.length) {
            container.innerHTML = `<div class="empty-state"><i class="bi bi-inbox"></i>You haven't reported any found items yet.</div>`;
            return;
        }
        items.forEach(item => {
            container.appendChild(renderItemCard(item, {
                nameKey: 'foundItemName', publicIdKey: 'publicIdOfFoundItem',
                imageKey: 'imageURL', locationKey: 'predictedLocation',
                emailKey: 'contactTo', reportType: 'FOUND',
                onClose: closeFoundItem
            }));
        });
    } catch (e) {
        container.innerHTML = `<div class="empty-state text-danger">${escapeHtml(e.message)}</div>`;
    }
}

async function closeFoundItem(publicId) {
    try {
        await api('/api/v1/found-item/close', { method: 'PATCH', params: { foundItemId: publicId } });
        toast('Found item request closed');
        loadFoundMine();
        loadDashboardStats();
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function submitFoundItem() {
    const name = document.getElementById('foundItemName').value.trim();
    const desc = document.getElementById('foundDescription').value.trim();
    const location = document.getElementById('foundLocation').value.trim();
    const imageFile = document.getElementById('foundItemImage').files[0];

    if (!name || !desc) {
        return toast('Item name and description are required', 'error');
    }
    if (!location) {
        return toast('Location near college is required! Submissions without location are marked as spam.', 'error');
    }
    if (name.length < 3) {
        return toast('Item name must be at least 3 characters', 'error');
    }
    if (desc.length < 20) {
        return toast('Please provide a detailed description (at least 20 characters)', 'error');
    }
    const imgError = validateImageFile(imageFile);
    if (imgError) return toast(imgError, 'error');

    const data = { foundItemName: name, description: desc, predictedLocation: location };
    const fd = new FormData();
    fd.append('data', JSON.stringify(data));
    fd.append('image', imageFile);

    const btn = document.getElementById('foundSubmitBtn');
    const banner = document.getElementById('foundAnalyzingBanner');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-inline me-2"></span>Analyzing…';
    if (banner) banner.classList.remove('d-none');

    try {
        await api('/api/v1/found-item', { method: 'POST', formData: fd });
        toast('Found item reported successfully');
        addNotification({
            type: 'found',
            title: name,
            status: 'UPLOADED',
            message: 'Found item verified and published. Location: ' + location,
            time: new Date()
        });
        document.getElementById('foundItemName').value = '';
        document.getElementById('foundDescription').value = '';
        document.getElementById('foundLocation').value = '';
        document.getElementById('foundItemImage').value = '';
        document.getElementById('foundItemImagePreview').innerHTML = '';
        loadDashboardStats();
        switchFoundTab('mine', document.querySelectorAll('#found .tab-btn')[1]);
    } catch (e) {
        toast(e.message, 'error');
        addNotification({
            type: 'found',
            title: name || 'Found Item Report',
            status: 'REJECTED',
            message: e.message || 'Report was rejected during safety verification.',
            time: new Date()
        });
    } finally {
        if (banner) banner.classList.add('d-none');
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-cloud-arrow-up me-1"></i> Submit Found Item';
    }
}

/* ============ TEAM FINDER ============ */
let teamPage = 0;

async function loadTeamBrowse() {
    const container = document.getElementById('teamBrowseList');
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const res = await api('/api/v1/team', { params: { page: teamPage, size: PAGE_SIZE } });
        const teams = res.content || res.items || [];
        container.innerHTML = '';
        document.getElementById('teamPageLabel').innerText = 'Page ' + (teamPage + 1);
        updatePaginationButtons('teamPrevBtn', 'teamNextBtn', teamPage, teams.length, res);
        if (!teams.length) {
            container.innerHTML = `<div class="empty-state"><i class="bi bi-people"></i>No active teams looking for members right now.</div>`;
            return;
        }
        teams.forEach(t => {
            const isOwnTeam = currentUserEmail && t.createdBy === currentUserEmail;
            const wrap = document.createElement('div');
            wrap.className = 'item-card';

            const creatorBadge = t.creatorPrivilege ? renderPrivilegeBadge(t.creatorPrivilege) : '';
            const adminCloseBtn = (currentUserRole === 'ADMIN' && !isOwnTeam)
                ? `<button class="btn-danger-soft ms-2 btn-sm" onclick="adminCloseReport('${t.publicId}', 'TEAM')"><i class="bi bi-x-circle"></i> Admin Close</button>`
                : '';

            wrap.innerHTML = `
                <div class="flex-grow-1">
                    <div class="item-title d-flex align-items-center gap-2 flex-wrap">
                        <span>${escapeHtml(t.purpose || 'Team')}</span>
                        ${creatorBadge}
                    </div>
                    <div class="item-meta">Owner: ${escapeHtml(t.createdBy || '—')} · Members: ${t.currentMember ?? '—'}/${t.maxMember ?? '—'} · Privacy: ${escapeHtml(t.privacy || 'PUBLIC')}</div>
                </div>
                <div class="d-flex align-items-center gap-2">
                    ${isOwnTeam
                        ? `<span class="badge-status badge-accepted">Your Team</span>`
                        : `<button class="btn-outline-accent">Request to Join</button>`}
                    ${adminCloseBtn}
                </div>
            `;
            if (!isOwnTeam) {
                wrap.querySelector('.btn-outline-accent').addEventListener('click', () => promptJoinRequest(t.publicId));
            }
            container.appendChild(wrap);
        });
    } catch (e) {
        container.innerHTML = `<div class="empty-state text-danger">Could not load teams: ${escapeHtml(e.message)}</div>`;
    }
}
function changeTeamPage(delta) {
    const next = teamPage + delta;
    if (next < 0) return;
    teamPage = next;
    loadTeamBrowse();
}

async function loadTeamMine() {
    const container = document.getElementById('teamMineDetails');
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const team = await api('/api/v1/team/me');
        container.innerHTML = '';
        if (!team) {
            container.innerHTML = `<div class="empty-state"><i class="bi bi-inbox"></i>You don't have an active team yet. Form one in Create Team!</div>`;
            return;
        }
        const card = document.createElement('div');
        card.className = 'stat-card';
        card.innerHTML = `
            <div class="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-2">
                <div>
                    <h5 class="fw-bold text-white mb-1">${escapeHtml(team.purpose)}</h5>
                    <div class="text-muted small">Created: ${formatNotifTime(team.createdAt)} · Members: ${team.currentMember}/${team.maxMember}</div>
                </div>
                <button class="btn-danger-soft btn-sm" onclick="closeMyTeam()"><i class="bi bi-x-circle me-1"></i> Close Team</button>
            </div>
            <div class="mb-3">
                <label class="field-label">Update Team Size (3–20)</label>
                <div class="d-flex gap-2">
                    <input type="number" class="form-control-dark" id="updateTeamSizeInput" min="3" max="20" value="${team.maxMember}">
                    <button class="btn-accent" onclick="updateTeamSize()">Update Size</button>
                </div>
            </div>
        `;
        container.appendChild(card);
    } catch (e) {
        container.innerHTML = `<div class="empty-state"><i class="bi bi-people"></i>You are not currently running a team.</div>`;
    }
}

async function closeMyTeam() {
    if (!confirm('Are you sure you want to close your team?')) return;
    try {
        await api('/api/v1/team', { method: 'PATCH' });
        toast('Team closed successfully');
        loadTeamMine();
        loadDashboardStats();
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function updateTeamSize() {
    const size = document.getElementById('updateTeamSizeInput').value;
    const sizeNum = parseInt(size, 10);
    if (isNaN(sizeNum) || sizeNum < 3) return toast('Enter a valid team size (min. 3)', 'error');
    try {
        await api('/api/v1/team/team-size', { method: 'PATCH', params: { teamSize: sizeNum } });
        toast('Team size updated');
        loadTeamMine();
    } catch (e) {
        toast(e.message, 'error');
    }
}

function promptJoinRequest(teamPublicId) {
    const message = window.prompt('Add a short message to your join request:');
    if (message === null) return;
    submitJoinRequest(teamPublicId, message);
}

async function submitJoinRequest(teamPublicId, message) {
    try {
        await api('/api/v1/request-team', { method: 'POST', jsonBody: { teamId: teamPublicId, message: message } });
        toast('Join request sent successfully');
        addNotification({
            type: 'team',
            title: 'Team Join Request',
            status: 'UPLOADED',
            message: 'Application submitted with message: "' + message + '"',
            time: new Date()
        });
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function loadSkillsDropdown() {
    const select = document.getElementById('teamSkillSelect');
    if (!select || select.children.length > 0) return;
    try {
        const skills = await api('/api/v1/skill');
        if (skills && Array.isArray(skills)) {
            skills.forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.id;
                opt.textContent = s.skill;
                select.appendChild(opt);
            });
        }
    } catch {}
}

async function submitCreateTeam() {
    const purpose = document.getElementById('teamPurpose').value.trim();
    const maxMember = parseInt(document.getElementById('teamMaxMember').value, 10);
    const privacy = document.getElementById('teamPrivacy').value;
    const skillSelect = document.getElementById('teamSkillSelect');
    const skillIds = Array.from(skillSelect.selectedOptions).map(o => parseInt(o.value, 10));

    if (!purpose) return toast('Team purpose/goal is required', 'error');

    const btn = document.getElementById('teamSubmitBtn');
    const banner = document.getElementById('teamAnalyzingBanner');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-inline me-2"></span>Analyzing…';
    if (banner) banner.classList.remove('d-none');

    try {
        await api('/api/v1/team', {
            method: 'POST',
            jsonBody: { purpose, maxMember, privacy, skillIds }
        });
        toast('Team formed successfully');
        addNotification({
            type: 'team',
            title: purpose,
            status: 'UPLOADED',
            message: 'Team created and active in directory.',
            time: new Date()
        });
        document.getElementById('teamPurpose').value = '';
        loadDashboardStats();
        switchTeamTab('mine', document.querySelectorAll('#team .tab-btn')[1]);
    } catch (e) {
        toast(e.message, 'error');
        addNotification({
            type: 'team',
            title: purpose || 'Team Creation',
            status: 'REJECTED',
            message: e.message || 'Team creation rejected by moderation filter.',
            time: new Date()
        });
    } finally {
        if (banner) banner.classList.add('d-none');
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-people-fill me-1"></i> Form Team';
    }
}

async function loadTeamRequests() {
    const reqContainer = document.getElementById('teamRequestsList');
    const memContainer = document.getElementById('teamMembersList');
    reqContainer.innerHTML = '<div class="spinner-inline"></div>';
    memContainer.innerHTML = '';
    try {
        const requests = await api('/api/v1/request-team/requests');
        reqContainer.innerHTML = '';
        if (!requests || !requests.length) {
            reqContainer.innerHTML = `<div class="empty-state"><i class="bi bi-inbox"></i>No pending join requests.</div>`;
        } else {
            requests.forEach(r => {
                const wrap = document.createElement('div');
                wrap.className = 'item-card';
                const privBadge = r.applicantPrivilege ? renderPrivilegeBadge(r.applicantPrivilege) : '';
                wrap.innerHTML = `
					<div class="flex-grow-1">
						<div class="item-title d-flex align-items-center gap-2 flex-wrap">
                            <span>${escapeHtml(r.applicantFullName || r.applicantUsername || r.applicantEmail)}</span>
                            ${privBadge}
                        </div>
						<div class="item-meta mb-1">${escapeHtml(r.applicantEmail)}</div>
						<div class="item-desc">${escapeHtml(r.message || '')}</div>
					</div>
					<div class="d-flex gap-2">
						<button class="btn-success-soft">Accept</button>
						<button class="btn-danger-soft">Reject</button>
					</div>
				`;
                const [acceptBtn, rejectBtn] = wrap.querySelectorAll('button');
                acceptBtn.addEventListener('click', () => respondToRequest(r.applicantEmail, 'accept'));
                rejectBtn.addEventListener('click', () => respondToRequest(r.applicantEmail, 'reject'));
                reqContainer.appendChild(wrap);
            });
        }
    } catch (e) {
        reqContainer.innerHTML = `<div class="empty-state text-danger">${escapeHtml(e.message)}</div>`;
    }

    try {
        const members = await api('/api/v1/request-team/team-members');
        memContainer.innerHTML = '';
        if (!members || !members.length) {
            memContainer.innerHTML = `<div class="empty-state text-muted py-2"><i class="bi bi-people me-1"></i> No approved members in team yet.</div>`;
        } else {
            members.forEach(m => {
                const card = document.createElement('div');
                card.className = 'item-card py-2';
                const privBadge = m.applicantPrivilege ? renderPrivilegeBadge(m.applicantPrivilege) : '';
                card.innerHTML = `
                    <div class="flex-grow-1">
                        <div class="fw-semibold text-white d-flex align-items-center gap-2">
                            <span>${escapeHtml(m.applicantFullName || m.applicantUsername || m.applicantEmail)}</span>
                            ${privBadge}
                        </div>
                        <div class="text-muted small">${escapeHtml(m.applicantEmail)}</div>
                    </div>
                    <button class="btn-danger-soft btn-sm" onclick="kickTeamMember('${escapeHtml(m.applicantEmail)}')"><i class="bi bi-person-x"></i> Remove</button>
                `;
                memContainer.appendChild(card);
            });
        }
    } catch {}
}

async function respondToRequest(applicantEmail, action) {
    try {
        await api(`/api/v1/request-team/${action}/${encodeURIComponent(applicantEmail)}`, { method: 'PATCH' });
        toast(`Request ${action}ed`);
        loadTeamRequests();
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function kickTeamMember(memberEmail) {
    if (!confirm(`Remove ${memberEmail} from your team?`)) return;
    try {
        await api(`/api/v1/request-team/kick/${encodeURIComponent(memberEmail)}`, { method: 'PATCH' });
        toast('Member removed');
        loadTeamRequests();
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function loadTeamApplications() {
    const container = document.getElementById('teamApplicationsList');
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const apps = await api('/api/v1/request-team/my-applications');
        container.innerHTML = '';
        if (!apps || !apps.length) {
            container.innerHTML = `<div class="empty-state"><i class="bi bi-send"></i>You haven't sent any team join requests yet.</div>`;
            return;
        }
        apps.forEach(a => {
            const card = document.createElement('div');
            card.className = 'item-card';
            let statusBadge = '<span class="badge-status open">WAITING</span>';
            if (a.status === 'ACCEPTED') statusBadge = '<span class="badge-status badge-accepted">ACCEPTED</span>';
            if (a.status === 'REJECTED') statusBadge = '<span class="badge-status closed">REJECTED</span>';

            card.innerHTML = `
                <div class="flex-grow-1">
                    <div class="item-title d-flex align-items-center justify-content-between mb-1">
                        <span>Application to Team</span>
                        ${statusBadge}
                    </div>
                    <div class="item-desc mb-2">${escapeHtml(a.yourMessage || 'No application message')}</div>
                    <div class="text-muted small">Sent: ${formatNotifTime(a.requestedAt)}</div>
                </div>
            `;
            container.appendChild(card);
        });
    } catch (e) {
        container.innerHTML = `<div class="empty-state text-danger">${escapeHtml(e.message)}</div>`;
    }
}

/* ============ RESUME CHECKER ============ */
async function submitResume() {
    const role = document.getElementById('resumeRole').value.trim();
    const exp = document.getElementById('resumeExperience').value.trim() || '0';
    const file = document.getElementById('resumeFile').files[0];
    const resultEl = document.getElementById('resumeResult');

    if (!role) return toast('Please specify target role', 'error');
    if (!file) return toast('Please upload a resume file (.pdf/.docx)', 'error');

    const fd = new FormData();
    fd.append('file', file);
    fd.append('role', role);
    fd.append('exp', exp);

    const btn = document.getElementById('resumeSubmitBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-inline me-2"></span>Evaluating Resume with AI…';
    resultEl.innerHTML = '<div class="stat-card text-center py-4"><div class="spinner-inline mb-2"></div><div class="text-muted">Analyzing resume structure, keywords, and qualifications...</div></div>';

    try {
        const res = await api('/api/v1/ietscroll/resume/quality', { method: 'POST', formData: fd });
        resultEl.innerHTML = `
            <div class="stat-card border border-primary border-opacity-50 mt-4">
                <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
                    <div>
                        <h5 class="fw-bold text-white mb-1"><i class="bi bi-award-fill me-2 text-warning"></i>Resume Evaluation Results</h5>
                        <div class="text-muted small">Target Role: ${escapeHtml(role)} · Experience: ${escapeHtml(exp)} years</div>
                    </div>
                    <div class="text-end">
                        <div class="fs-1 fw-bold text-primary">${res.score || '—'}<span class="fs-6 text-muted">/100</span></div>
                        <span class="badge bg-primary bg-opacity-25 text-primary border border-primary border-opacity-50">${escapeHtml(res.overallQuality || 'Analyzed')}</span>
                    </div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <div class="p-3 bg-black bg-opacity-30 rounded border border-secondary border-opacity-25 h-100">
                            <h6 class="fw-bold text-white mb-2"><i class="bi bi-check2-all text-success me-2"></i>Strong Points</h6>
                            <p class="text-muted small mb-0">${escapeHtml(res.strongPoints || 'Clear layout and relevant qualifications.')}</p>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="p-3 bg-black bg-opacity-30 rounded border border-secondary border-opacity-25 h-100">
                            <h6 class="fw-bold text-white mb-2"><i class="bi bi-exclamation-triangle text-warning me-2"></i>Key Missing Skills / Keywords</h6>
                            <p class="text-muted small mb-0">${escapeHtml(res.missingKeywords || 'None specifically flagged.')}</p>
                        </div>
                    </div>
                </div>

                <div class="p-3 bg-black bg-opacity-30 rounded border border-secondary border-opacity-25">
                    <h6 class="fw-bold text-white mb-2"><i class="bi bi-lightbulb-fill text-warning me-2"></i>Actionable Recommendations</h6>
                    <p class="text-muted small mb-0">${escapeHtml(res.suggestions || res.improvements || 'Align projects with quantifiable metrics.')}</p>
                </div>
            </div>
        `;
        toast('Resume evaluation complete');
        addNotification({
            type: 'resume',
            title: 'Resume Review (' + role + ')',
            status: 'UPLOADED',
            message: 'Resume scored ' + (res.score || 0) + '/100 (' + (res.overallQuality || 'Evaluated') + ')',
            time: new Date()
        });
    } catch (e) {
        toast(e.message, 'error');
        resultEl.innerHTML = `<div class="stat-card border border-danger border-opacity-50 mt-4 text-danger"><i class="bi bi-x-circle me-2"></i>${escapeHtml(e.message)}</div>`;
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-magic me-1"></i> Analyze Resume';
    }
}

/* ============ NOTIFICATIONS & ACTIVITY AUDIT ============ */
function getNotifKey() {
    return 'iet_notifs_' + (currentUserEmail || 'global');
}

function getNotifications() {
    try {
        const raw = localStorage.getItem(getNotifKey());
        return raw ? JSON.parse(raw) : [];
    } catch {
        return [];
    }
}

function saveNotifications(list) {
    try {
        localStorage.setItem(getNotifKey(), JSON.stringify(list));
    } catch {}
    updateNotifBadges();
}

function addNotification({ type, title, status, message, time }) {
    const list = getNotifications();
    const entry = {
        id: 'notif_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4),
        type,
        title: title || 'Submission',
        status,
        message: message || '',
        time: time ? new Date(time).toISOString() : new Date().toISOString(),
        read: false
    };
    list.unshift(entry);
    if (list.length > 50) list.pop();
    saveNotifications(list);
    renderNotifications();
}

function formatNotifTime(isoString) {
    try {
        const d = new Date(isoString);
        return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' }) + 
               ' at ' + d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
    } catch {
        return isoString || 'Recently';
    }
}

function renderNotifications() {
    const container = document.getElementById('notificationsList');
    if (!container) return;
    const list = getNotifications();
    if (!list.length) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="bi bi-bell-slash"></i>
                <div class="fw-bold fs-5 text-white mb-1">No Activity Logged Yet</div>
                <div class="text-muted small">Your report upload history, safety approvals, and rejection notices will appear here with timestamps.</div>
            </div>`;
        return;
    }

    container.innerHTML = '';
    list.forEach(n => {
        const isSuccess = n.status === 'UPLOADED' || n.status === 'SUCCESS';
        const card = document.createElement('div');
        card.className = 'notif-card ' + (isSuccess ? 'success' : 'rejected');

        let iconClass = 'bi-file-earmark-text';
        if (n.type === 'lost') iconClass = 'bi-search-heart';
        if (n.type === 'found') iconClass = 'bi-box-seam';
        if (n.type === 'team') iconClass = 'bi-people';
        if (n.type === 'resume') iconClass = 'bi-stars';

        const statusBadge = isSuccess
            ? '<span class="badge-status open"><i class="bi bi-check-circle-fill me-1"></i> UPLOADED</span>'
            : '<span class="badge-status closed"><i class="bi bi-x-circle-fill me-1"></i> REJECTED</span>';

        card.innerHTML = `
            <div class="notif-icon-box ${isSuccess ? 'success' : 'rejected'}">
                <i class="bi ${iconClass}"></i>
            </div>
            <div class="flex-grow-1">
                <div class="notif-title d-flex justify-content-between align-items-center">
                    <span>${escapeHtml(n.title)}</span>
                    ${statusBadge}
                </div>
                <div class="notif-time">
                    <i class="bi bi-clock"></i> ${formatNotifTime(n.time)}
                </div>
                <div class="notif-desc ${isSuccess ? 'success' : 'rejected'}">
                    <strong>${isSuccess ? 'Status:' : 'Rejection Reason:'}</strong> ${escapeHtml(n.message)}
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

function updateNotifBadges() {
    const list = getNotifications();
    const unread = list.filter(n => !n.read).length;

    const b1 = document.getElementById('notifBadge');
    const b2 = document.getElementById('notifBadgeMobile');
    const b3 = document.getElementById('notifNavBadge');

    [b1, b2, b3].forEach(el => {
        if (!el) return;
        if (unread > 0) {
            el.innerText = unread;
            el.classList.remove('d-none');
        } else {
            el.classList.add('d-none');
        }
    });
}

function clearAllNotifications() {
    if (!confirm('Clear your entire upload activity history?')) return;
    saveNotifications([]);
    renderNotifications();
    toast('Activity history cleared', 'info');
}

function markAllNotificationsRead() {
    const list = getNotifications();
    list.forEach(n => n.read = true);
    saveNotifications(list);
}

/* ============ ADMIN USERS AUDIT & GOVERNANCE ============ */
let usersPage = 0;
let userSearchTimer = null;

function debounceUserSearch() {
    clearTimeout(userSearchTimer);
    userSearchTimer = setTimeout(() => {
        usersPage = 0;
        loadUsersTable();
    }, 350);
}

function changeUserPage(delta) {
    const next = usersPage + delta;
    if (next < 0) return;
    usersPage = next;
    loadUsersTable();
}

async function loadUsersTable() {
    const tbody = document.getElementById('usersAuditTableBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted"><div class="spinner-inline"></div> Searching users...</td></tr>';

    const q = (document.getElementById('userSearchInput') ? document.getElementById('userSearchInput').value.trim() : '');
    const priv = (document.getElementById('userPrivilegeFilter') ? document.getElementById('userPrivilegeFilter').value : '');
    const blk = (document.getElementById('userBlockFilter') ? document.getElementById('userBlockFilter').value : '');

    const params = { page: usersPage, size: 12 };
    if (q) params.query = q;
    if (priv) params.privilege = priv;
    if (blk !== '') params.isBlocked = blk;

    try {
        const res = await api('/api/v1/admin/users', { params });
        const users = res.content || [];
        tbody.innerHTML = '';
        document.getElementById('userPageLabel').innerText = 'Page ' + (usersPage + 1);
        updatePaginationButtons('userPrevBtn', 'userNextBtn', usersPage, users.length, res);

        if (!users.length) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted"><i class="bi bi-people-slash me-1"></i> No users matching search filter.</td></tr>';
            return;
        }

        users.forEach(u => {
            const tr = document.createElement('tr');
            const safeEmail = escapeHtml(u.email);
            const isBlocked = u.blocked;
            const privBadge = renderPrivilegeBadge(u.privilegeClass);
            const statusPill = isBlocked
                ? '<span class="user-status-pill blocked"><i class="bi bi-slash-circle me-1"></i>BLOCKED</span>'
                : '<span class="user-status-pill active"><i class="bi bi-check-circle me-1"></i>ACTIVE</span>';

            const loginTime = u.lastLoginAt ? formatNotifTime(u.lastLoginAt) : 'Never';
            const logoutTime = u.lastLogoutAt ? formatNotifTime(u.lastLogoutAt) : '—';

            tr.innerHTML = `
                <td>
                    <div class="fw-bold text-white">${escapeHtml(u.fullName || u.username)}</div>
                    <div class="text-muted small">${safeEmail}</div>
                </td>
                <td>
                    <div class="text-white small">${escapeHtml(u.course || '—')}</div>
                    <div class="text-muted" style="font-size: 0.75rem;">${escapeHtml(u.branch || '—')}</div>
                </td>
                <td>${privBadge}</td>
                <td>${statusPill}</td>
                <td style="font-size: 0.78rem;">
                    <div><span class="text-success">Login:</span> ${loginTime}</div>
                    <div><span class="text-muted">Logout:</span> ${logoutTime}</div>
                </td>
                <td style="font-size: 0.8rem;">
                    <span class="badge bg-warning bg-opacity-20 text-warning me-1">Lost: ${u.lostItemsCount || 0}</span>
                    <span class="badge bg-info bg-opacity-20 text-info me-1">Found: ${u.foundItemsCount || 0}</span>
                    <span class="badge bg-primary bg-opacity-20 text-primary">Teams: ${u.teamsCreatedCount || 0}</span>
                </td>
                <td class="text-end">
                    <div class="d-inline-flex gap-1 align-items-center">
                        <select class="form-select form-select-sm bg-dark text-white border-secondary border-opacity-50 py-0" style="font-size: 0.75rem; width: 110px;" onchange="changeUserPrivilege('${safeEmail}', this.value)">
                            <option value="TRUSTED" ${u.privilegeClass === 'TRUSTED' ? 'selected' : ''}>TRUSTED</option>
                            <option value="ADMIN" ${u.privilegeClass === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                            <option value="MAY_BE_SPAM" ${u.privilegeClass === 'MAY_BE_SPAM' ? 'selected' : ''}>MAY BE SPAM</option>
                            <option value="NEW" ${u.privilegeClass === 'NEW' ? 'selected' : ''}>NEW</option>
                            <option value="NORMAL" ${u.privilegeClass === 'NORMAL' ? 'selected' : ''}>NORMAL</option>
                        </select>
                        <button class="btn btn-sm ${isBlocked ? 'btn-success-soft' : 'btn-danger-soft'}" style="font-size: 0.75rem;" onclick="toggleUserBlock('${safeEmail}', ${isBlocked})">
                            ${isBlocked ? '<i class="bi bi-unlock"></i> Unblock' : '<i class="bi bi-lock"></i> Block'}
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-danger">${escapeHtml(e.message)}</td></tr>`;
    }
}

async function toggleUserBlock(email, currentlyBlocked) {
    const action = currentlyBlocked ? 'unblock' : 'block';
    if (!confirm(`Are you sure you want to ${action} user ${email}?`)) return;
    try {
        await api(`/api/v1/admin/users/${encodeURIComponent(email)}/${action}`, { method: 'POST' });
        toast(`User ${email} ${action}ed successfully`);
        loadUsersTable();
    } catch (e) {
        toast(`Failed to ${action} user: ` + e.message, 'error');
    }
}

async function changeUserPrivilege(email, newPrivilege) {
    try {
        await api(`/api/v1/admin/users/${encodeURIComponent(email)}/privilege?privilege=${newPrivilege}`, { method: 'PATCH' });
        toast(`Privilege updated to ${newPrivilege}`);
        loadUsersTable();
    } catch (e) {
        toast('Failed to change privilege: ' + e.message, 'error');
    }
}

/* ============ ADMIN ANALYTICS & CHARTS ============ */
let reportsChartInstance = null;
let userGrowthChartInstance = null;

async function loadAdminAnalytics() {
    try {
        const stats = await api('/api/v1/admin/analytics');
        if (!stats) return;

        document.getElementById('adminTotalUsers').innerText = stats.totalUsers || 0;
        document.getElementById('adminActiveUsers').innerText = (stats.activeUsers || 0) + ' active';
        document.getElementById('adminBlockedUsers').innerText = stats.blockedUsers || 0;
        document.getElementById('adminTotalLost').innerText = stats.totalLostReports || 0;
        document.getElementById('adminTotalFoundTeams').innerText = `${stats.totalFoundReports || 0} / ${stats.totalTeamsCreated || 0}`;

        renderAdminCharts(stats);
    } catch (e) {
        console.warn('Analytics loading error:', e);
    }
}

function renderAdminCharts(stats) {
    if (!window.Chart) return;

    // Timeline merge dates
    const dateSet = new Set([
        ...Object.keys(stats.lostReportsGrowth || {}),
        ...Object.keys(stats.foundReportsGrowth || {}),
        ...Object.keys(stats.teamCreationGrowth || {})
    ]);
    const sortedDates = Array.from(dateSet).sort();
    if (sortedDates.length === 0) sortedDates.push(new Date().toISOString().slice(0, 10));

    const lostSeries = sortedDates.map(d => (stats.lostReportsGrowth && stats.lostReportsGrowth[d]) || 0);
    const foundSeries = sortedDates.map(d => (stats.foundReportsGrowth && stats.foundReportsGrowth[d]) || 0);
    const teamSeries = sortedDates.map(d => (stats.teamCreationGrowth && stats.teamCreationGrowth[d]) || 0);

    const ctxReports = document.getElementById('reportsChart');
    if (ctxReports) {
        if (reportsChartInstance) reportsChartInstance.destroy();
        reportsChartInstance = new Chart(ctxReports, {
            type: 'bar',
            data: {
                labels: sortedDates,
                datasets: [
                    { label: 'Lost Items', data: lostSeries, backgroundColor: 'rgba(245, 158, 11, 0.7)', borderColor: '#f59e0b', borderWidth: 1 },
                    { label: 'Found Items', data: foundSeries, backgroundColor: 'rgba(6, 182, 212, 0.7)', borderColor: '#06b6d4', borderWidth: 1 },
                    { label: 'Teams', data: teamSeries, backgroundColor: 'rgba(99, 102, 241, 0.7)', borderColor: '#6366f1', borderWidth: 1 }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: { ticks: { color: '#94a3b8' }, grid: { color: '#1e293b' } },
                    y: { ticks: { color: '#94a3b8', stepSize: 1 }, grid: { color: '#1e293b' }, beginAtZero: true }
                },
                plugins: {
                    legend: { labels: { color: '#f1f5f9' } }
                }
            }
        });
    }

    // User growth chart
    const userDates = Object.keys(stats.userGrowth || {}).sort();
    if (userDates.length === 0) userDates.push(new Date().toISOString().slice(0, 10));
    const userCounts = userDates.map(d => (stats.userGrowth && stats.userGrowth[d]) || 0);

    const ctxUsers = document.getElementById('usersGrowthChart');
    if (ctxUsers) {
        if (userGrowthChartInstance) userGrowthChartInstance.destroy();
        userGrowthChartInstance = new Chart(ctxUsers, {
            type: 'line',
            data: {
                labels: userDates,
                datasets: [{
                    label: 'New Registered Users',
                    data: userCounts,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.15)',
                    fill: true,
                    tension: 0.35,
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: { ticks: { color: '#94a3b8' }, grid: { color: '#1e293b' } },
                    y: { ticks: { color: '#94a3b8', stepSize: 1 }, grid: { color: '#1e293b' }, beginAtZero: true }
                },
                plugins: {
                    legend: { labels: { color: '#f1f5f9' } }
                }
            }
        });
    }
}

function logout() {
    api('/api/v1/user/logout', { method: 'POST' }).catch(() => {});
    localStorage.removeItem('iet_token');
    window.location.replace('login.html');
}

// Startup
loadProfile();
loadDashboardStats();
updateNotifBadges();
wireImagePreview('lostItemImage', 'lostItemImagePreview');
wireImagePreview('foundItemImage', 'foundItemImagePreview');
