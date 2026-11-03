// static/js/app.js
// Main application router, shared component renderers, role-based view guarding & initialization

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
    const canClose = onClose || isAdmin();
    const closeBtn = canClose
        ? `<button class="btn-danger-soft ms-auto flex-shrink-0 close-card-btn"><i class="bi bi-x-circle"></i> ${isAdmin() && !onClose ? 'Admin Close' : 'Close'}</button>`
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
                } else if (isAdmin()) {
                    adminCloseReport(item[publicIdKey], reportType || 'LOST');
                }
            });
        }
    }
    return wrap;
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    if (!sidebar) return;
    const isOpen = sidebar.classList.toggle('open');
    if (overlay) overlay.classList.toggle('active', isOpen);
}

// Router & Section switcher with Role-Based Access Guard
function show(id, el) {
    // Role check: Only ADMIN can view users section
    if (id === 'users' && !isAdmin()) {
        toast('Access denied: Campus User Governance is restricted to Administrators.', 'error');
        id = 'dashboard';
        el = document.querySelector(`.nav-link[onclick*="'dashboard'"]`);
    }

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

    if (id === 'dashboard') {
        loadDashboardStats();
        if (isAdmin()) loadAdminAnalytics();
    }
    if (id === 'users') loadUsersTable();
    if (id === 'notifications') {
        markAllNotificationsRead();
        renderNotifications();
    }
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

// Global window registration for all HTML onclick handlers
window.show = show;
window.toggleSidebar = toggleSidebar;
window.switchTab = switchTab;
window.switchLostTab = switchLostTab;
window.switchFoundTab = switchFoundTab;
window.switchTeamTab = switchTeamTab;
window.logout = logout;
window.loadProfile = loadProfile;
window.updateUsername = updateUsername;
window.updateFullname = updateFullname;
window.loadDashboardStats = loadDashboardStats;
window.loadAdminAnalytics = loadAdminAnalytics;
window.loadLostBrowse = loadLostBrowse;
window.changeLostPage = changeLostPage;
window.loadLostMine = loadLostMine;
window.closeLostItem = closeLostItem;
window.submitLostItem = submitLostItem;
window.loadFoundBrowse = loadFoundBrowse;
window.changeFoundPage = changeFoundPage;
window.loadFoundMine = loadFoundMine;
window.closeFoundItem = closeFoundItem;
window.submitFoundItem = submitFoundItem;
window.openViewsModal = openViewsModal;
window.closeViewsModal = closeViewsModal;
window.loadTeamBrowse = loadTeamBrowse;
window.changeTeamPage = changeTeamPage;
window.loadTeamMine = loadTeamMine;
window.closeMyTeam = closeMyTeam;
window.updateTeamSize = updateTeamSize;
window.submitCreateTeam = submitCreateTeam;
window.loadTeamRequests = loadTeamRequests;
window.respondToRequest = respondToRequest;
window.kickTeamMember = kickTeamMember;
window.loadTeamApplications = loadTeamApplications;
window.submitResume = submitResume;
window.clearAllNotifications = clearAllNotifications;
window.loadUsersTable = loadUsersTable;
window.debounceUserSearch = debounceUserSearch;
window.changeUserPage = changeUserPage;
window.toggleUserBlock = toggleUserBlock;
window.changeUserPrivilege = changeUserPrivilege;
window.adminCloseReport = adminCloseReport;

// Initialize — scripts are at end of <body> so DOM is already ready, no need to wait
(async function initDashboard() {
    try {
        await loadProfile();
        await loadDashboardStats();
        await loadServerActivities();
        updateNotifBadges();
        wireImagePreview('lostItemImage', 'lostItemImagePreview');
        wireImagePreview('foundItemImage', 'foundItemImagePreview');
    } catch (e) {
        console.error('[IETScroll] Initialization error:', e);
    }
})();
