// static/js/adminUsers.js
// Google Admin Console styled User Governance, Directory Audit, and Role Management

let usersPage = 0;
let userSearchTimer = null;

// Deterministic Google Material colors for directory avatars
function getGoogleAvatarColor(str) {
    const palette = [
        '#1a73e8', // Google Blue
        '#0d904f', // Google Green
        '#e37400', // Google Amber
        '#d93025', // Google Red
        '#8430ce', // Google Purple
        '#129eaf', // Google Teal
        '#e52592', // Google Pink
        '#3949ab', // Google Indigo
    ];
    let hash = 0;
    const text = (str || 'User').trim();
    for (let i = 0; i < text.length; i++) {
        hash = text.charCodeAt(i) + ((hash << 5) - hash);
    }
    return palette[Math.abs(hash) % palette.length];
}

function debounceUserSearch() {
    const searchEl = document.getElementById('userSearchInput');
    const clearBtn = document.getElementById('userSearchClear');
    if (clearBtn && searchEl) {
        clearBtn.style.display = searchEl.value.trim().length > 0 ? 'block' : 'none';
    }
    clearTimeout(userSearchTimer);
    userSearchTimer = setTimeout(() => {
        usersPage = 0;
        loadUsersTable();
    }, 300);
}

function clearUserSearch() {
    const searchEl = document.getElementById('userSearchInput');
    const clearBtn = document.getElementById('userSearchClear');
    if (searchEl) searchEl.value = '';
    if (clearBtn) clearBtn.style.display = 'none';
    usersPage = 0;
    loadUsersTable();
}

function changeUserPage(delta) {
    const next = usersPage + delta;
    if (next < 0) return;
    usersPage = next;
    loadUsersTable();
}

async function loadGoogleDirectoryStats() {
    try {
        const stats = await api('/api/v1/admin/analytics');
        if (!stats) return;

        const totalEl = document.getElementById('gStatTotalUsers');
        const activeEl = document.getElementById('gStatActiveUsers');
        const blockedEl = document.getElementById('gStatBlockedUsers');
        const adminEl = document.getElementById('gStatAdminUsers');

        if (totalEl) totalEl.innerText = stats.totalUsers || 0;
        if (activeEl) activeEl.innerText = stats.activeUsers || 0;
        if (blockedEl) blockedEl.innerText = stats.blockedUsers || 0;
        if (adminEl) {
            // Count from active search or approximate
            adminEl.innerText = 'Staff Active';
        }
    } catch {
        /* silent failure for stats */
    }
}

async function loadUsersTable(refreshStats = false) {
    if (!isAdmin()) {
        toast('Access denied: Administrator privileges required.', 'error');
        return;
    }

    // Always fetch / update KPI stats
    loadGoogleDirectoryStats();

    const tbody = document.getElementById('usersAuditTableBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-5 text-muted"><div class="spinner-inline me-2"></div> Fetching directory accounts…</td></tr>';

    const searchEl = document.getElementById('userSearchInput');
    const privEl = document.getElementById('userPrivilegeFilter');
    const blkEl = document.getElementById('userBlockFilter');

    const q = searchEl ? searchEl.value.trim() : '';
    const priv = privEl ? privEl.value : '';
    const blk = blkEl ? blkEl.value : '';

    const params = { page: usersPage, size: 12 };
    if (q) params.query = q;
    if (priv) params.privilege = priv;
    if (blk !== '') params.isBlocked = blk;

    try {
        const res = await api('/api/v1/admin/users', { params });
        const users = res.content || [];
        tbody.innerHTML = '';

        const pageLabel = document.getElementById('userPageLabel');
        if (pageLabel) pageLabel.innerText = 'Page ' + (usersPage + 1);

        const summaryEl = document.getElementById('userPageSummary');
        if (summaryEl) {
            const totalElements = res.totalElements || users.length;
            const start = totalElements === 0 ? 0 : usersPage * 12 + 1;
            const end = Math.min((usersPage + 1) * 12, totalElements);
            summaryEl.innerText = `Showing ${start}–${end} of ${totalElements} accounts`;
        }

        updatePaginationButtons('userPrevBtn', 'userNextBtn', usersPage, users.length, res);

        if (!users.length) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center py-5 text-muted">
                        <i class="bi bi-people fs-2 d-block mb-2 text-secondary"></i>
                        No directory users matching the active search filters.
                    </td>
                </tr>`;
            return;
        }

        users.forEach(u => {
            const tr = document.createElement('tr');
            const safeEmail = escapeHtml(u.email);
            const fullName = escapeHtml(u.fullName || u.username || 'Directory User');
            const username = escapeHtml(u.username || 'user');
            const initial = (fullName.charAt(0) || 'U').toUpperCase();
            const avatarBg = getGoogleAvatarColor(fullName);

            const isBlocked = u.blocked;
            const role = (u.role || 'STUDENT').toUpperCase();
            const privClass = (u.privilegeClass || 'NORMAL').toUpperCase();

            // Google Material Role Chip
            let roleChipHtml = '';
            if (role === 'SENIOR_ADMIN') {
                roleChipHtml = `<span class="google-role-chip senior-admin"><i class="bi bi-crown-fill text-warning"></i> Senior Admin</span>`;
            } else if (role === 'ADMIN') {
                roleChipHtml = `<span class="google-role-chip admin"><i class="bi bi-shield-lock-fill"></i> Admin</span>`;
            } else {
                roleChipHtml = `<span class="google-role-chip student"><i class="bi bi-person"></i> Student</span>`;
            }

            // Google Status Chip
            const statusChipHtml = isBlocked
                ? `<span class="google-status-chip blocked"><span class="status-dot"></span> Suspended</span>`
                : `<span class="google-status-chip active"><span class="status-dot"></span> Active</span>`;

            // Showcase Badge
            const privBadgeHtml = renderPrivilegeBadge(privClass);

            // Timestamps
            const loginTime = u.lastLoginAt ? formatNotifTime(u.lastLoginAt) : 'Never';
            const logoutTime = u.lastLogoutAt ? formatNotifTime(u.lastLogoutAt) : '—';

            tr.innerHTML = `
                <td>
                    <div class="user-identity-wrap">
                        <div class="google-avatar" style="background-color:${avatarBg};">${initial}</div>
                        <div class="user-identity-text">
                            <span class="user-identity-name">${fullName}</span>
                            <span class="user-identity-sub">
                                <span>${safeEmail}</span>
                                <button class="email-copy-btn" onclick="copyToClipboard('${safeEmail}')" title="Copy Email"><i class="bi bi-clipboard"></i></button>
                                <span class="text-secondary opacity-50">(@${username})</span>
                            </span>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="academic-chip">
                        <span class="academic-degree">${escapeHtml(u.course || 'BTech')}</span>
                        <span class="academic-branch">${escapeHtml(u.branch ? u.branch.replace(/_/g, ' ') : 'General')}</span>
                    </div>
                </td>
                <td>${roleChipHtml}</td>
                <td>${privBadgeHtml}</td>
                <td>${statusChipHtml}</td>
                <td>
                    <div class="ts-line"><i class="bi bi-box-arrow-in-right"></i> ${loginTime}</div>
                    <div class="ts-line logout" style="margin-top:3px;"><i class="bi bi-box-arrow-right"></i> ${logoutTime}</div>
                </td>
                <td>
                    <div class="report-count-cell">
                        <span class="report-mini-badge lost" title="Lost Items Reported"><i class="bi bi-search-heart"></i> ${u.lostItemsCount || 0}</span>
                        <span class="report-mini-badge found" title="Found Items Logged"><i class="bi bi-check2-circle"></i> ${u.foundItemsCount || 0}</span>
                        <span class="report-mini-badge team" title="Teams Created"><i class="bi bi-people"></i> ${u.teamsCreatedCount || 0}</span>
                    </div>
                </td>
                <td class="text-end">
                    <div style="display:inline-flex;gap:6px;align-items:center;flex-wrap:nowrap;justify-content:flex-end;">
                        ${isSeniorAdmin() ? `
                        <select class="google-select-pill" onchange="changeUserRole('${safeEmail}', this.value)" title="Assign System Role (Senior Admin Only)">
                            <option value="STUDENT" ${role === 'STUDENT' ? 'selected' : ''}>🎓 STUDENT</option>
                            <option value="ADMIN" ${role === 'ADMIN' ? 'selected' : ''}>🛡 ADMIN</option>
                            <option value="SENIOR_ADMIN" ${role === 'SENIOR_ADMIN' ? 'selected' : ''}>👑 SENIOR ADMIN</option>
                        </select>
                        ` : ''}
                        <select class="google-select-pill" onchange="changeUserPrivilege('${safeEmail}', this.value)" title="Showcase Reputation Badge">
                            <option value="TRUSTED" ${privClass === 'TRUSTED' ? 'selected' : ''}>✦ TRUSTED</option>
                            <option value="NORMAL" ${privClass === 'NORMAL' ? 'selected' : ''}>● NORMAL</option>
                            <option value="NEW" ${privClass === 'NEW' ? 'selected' : ''}>★ NEW</option>
                            <option value="MAY_BE_SPAM" ${privClass === 'MAY_BE_SPAM' ? 'selected' : ''}>⚠ SPAM?</option>
                        </select>
                        <button class="google-action-btn ${isBlocked ? 'unblock' : 'block'}" onclick="toggleUserBlock('${safeEmail}', ${isBlocked})">
                            ${isBlocked ? '<i class="bi bi-unlock"></i> Unsuspend' : '<i class="bi bi-slash-circle"></i> Suspend'}
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-danger">${escapeHtml(e.message)}</td></tr>`;
    }
}

function copyToClipboard(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
            toast('Email copied to clipboard');
        }).catch(() => {});
    }
}

async function changeUserRole(email, newRole) {
    if (!isSeniorAdmin()) {
        toast('Permission denied: Only Senior Administrators can assign system roles.', 'error');
        return;
    }
    try {
        await api(`/api/v1/admin/users/${encodeURIComponent(email)}/role?role=${newRole}`, { method: 'PATCH' });
        toast(`Role for ${email} updated to ${newRole}`);
        loadUsersTable();
    } catch (e) {
        toast('Failed to change role: ' + e.message, 'error');
    }
}

async function toggleUserBlock(email, currentlyBlocked) {
    const action = currentlyBlocked ? 'unblock' : 'block';
    const label = currentlyBlocked ? 'unsuspend' : 'suspend';
    if (!confirm(`Are you sure you want to ${label} user account: ${email}?`)) return;
    try {
        await api(`/api/v1/admin/users/${encodeURIComponent(email)}/${action}`, { method: 'POST' });
        toast(`Account ${email} ${currentlyBlocked ? 'unsuspended' : 'suspended'} successfully`);
        loadUsersTable(true);
    } catch (e) {
        toast(`Action failed: ` + e.message, 'error');
    }
}

async function changeUserPrivilege(email, newPrivilege) {
    try {
        await api(`/api/v1/admin/users/${encodeURIComponent(email)}/privilege?privilege=${newPrivilege}`, { method: 'PATCH' });
        toast(`Reputation badge updated to ${newPrivilege}`);
        loadUsersTable();
    } catch (e) {
        toast('Failed to update badge: ' + e.message, 'error');
    }
}

async function adminCloseReport(publicId, type) {
    if (!isAdmin()) return;
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

// Window bindings for HTML event listeners
window.loadUsersTable = loadUsersTable;
window.changeUserPage = changeUserPage;
window.debounceUserSearch = debounceUserSearch;
window.clearUserSearch = clearUserSearch;
window.copyToClipboard = copyToClipboard;
window.changeUserRole = changeUserRole;
window.changeUserPrivilege = changeUserPrivilege;
window.toggleUserBlock = toggleUserBlock;
window.adminCloseReport = adminCloseReport;
window.loadGoogleDirectoryStats = loadGoogleDirectoryStats;

// Auto-trigger directory KPI stats on module init
document.addEventListener('DOMContentLoaded', () => {
    if (typeof isAdmin === 'function' && isAdmin()) {
        loadGoogleDirectoryStats();
    }
});
