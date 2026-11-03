// static/js/adminUsers.js
// Admin-only Campus User Governance, activity audits, block/unblock, and privilege tier control

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
    if (!isAdmin()) {
        toast('Access denied: Admin privileges required.', 'error');
        return;
    }

    const tbody = document.getElementById('usersAuditTableBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted"><div class="spinner-inline"></div> Searching users...</td></tr>';

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
                    <div class="user-name-cell">${escapeHtml(u.fullName || u.username)}</div>
                    <div class="user-email-cell">${safeEmail}</div>
                </td>
                <td>
                    <div style="color:#e2e8f0;font-size:0.88rem;">${escapeHtml(u.course || '—')}</div>
                    <div style="color:#64748b;font-size:0.78rem;margin-top:2px;">${escapeHtml(u.branch || '—')}</div>
                </td>
                <td>${privBadge}</td>
                <td>${statusPill}</td>
                <td>
                    <div class="ts-line"><i class="bi bi-box-arrow-in-right"></i> ${loginTime}</div>
                    <div class="ts-line logout" style="margin-top:4px;"><i class="bi bi-box-arrow-right"></i> ${logoutTime}</div>
                </td>
                <td>
                    <div class="report-count-cell">
                        <span class="report-mini-badge lost"><i class="bi bi-search-heart"></i> Lost: ${u.lostItemsCount || 0}</span>
                        <span class="report-mini-badge found"><i class="bi bi-check2-circle"></i> Found: ${u.foundItemsCount || 0}</span>
                        <span class="report-mini-badge team"><i class="bi bi-people"></i> Teams: ${u.teamsCreatedCount || 0}</span>
                    </div>
                </td>
                <td class="text-end">
                    <div style="display:inline-flex;gap:8px;align-items:center;flex-wrap:nowrap;justify-content:flex-end;">
                        <select class="privilege-select" onchange="changeUserPrivilege('${safeEmail}', this.value)">
                            <option value="TRUSTED" ${u.privilegeClass === 'TRUSTED' ? 'selected' : ''}>✦ TRUSTED</option>
                            <option value="ADMIN" ${u.privilegeClass === 'ADMIN' ? 'selected' : ''}>🛡 ADMIN</option>
                            <option value="MAY_BE_SPAM" ${u.privilegeClass === 'MAY_BE_SPAM' ? 'selected' : ''}>⚠ SPAM?</option>
                            <option value="NEW" ${u.privilegeClass === 'NEW' ? 'selected' : ''}>★ NEW</option>
                            <option value="NORMAL" ${u.privilegeClass === 'NORMAL' ? 'selected' : ''}>● NORMAL</option>
                        </select>
                        <button class="tbl-action-btn ${isBlocked ? 'unblock' : 'block'}" onclick="toggleUserBlock('${safeEmail}', ${isBlocked})">
                            ${isBlocked ? '<i class="bi bi-unlock-fill"></i> Unblock' : '<i class="bi bi-lock-fill"></i> Block'}
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
