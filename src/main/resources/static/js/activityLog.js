// static/js/activityLog.js
// Notifications & Activity Audit Log (cross-device server sync + local cache fallback)

let serverActivities = [];

async function loadServerActivities() {
    try {
        const list = await api('/api/v1/user/activities');
        if (list && Array.isArray(list)) {
            serverActivities = list;
            renderNotifications();
            updateNotifBadges();
        }
    } catch (e) {
        // Fall back to client local storage
        renderNotifications();
    }
}

function getNotifKey() {
    return 'iet_notifs_' + (currentUserEmail || 'global');
}

function getNotifications() {
    if (serverActivities && serverActivities.length > 0) {
        return serverActivities.map(a => ({
            id: 'act_' + a.id,
            type: (a.activityType || '').toLowerCase(),
            title: a.title,
            status: a.status,
            message: a.message,
            time: a.timestamp,
            read: true
        }));
    }
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
        status, // 'UPLOADED', 'REJECTED_BY_MODERATION', 'FAILED_TO_SUBMIT'
        message: message || '',
        time: time ? new Date(time).toISOString() : new Date().toISOString(),
        read: false
    };
    list.unshift(entry);
    if (list.length > 50) list.pop();
    saveNotifications(list);
    renderNotifications();
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
                <div class="text-muted small">Your report upload history, safety approvals, and rejection notices will appear here with cross-device sync.</div>
            </div>`;
        return;
    }

    container.innerHTML = '';
    list.forEach(n => {
        const isSuccess = n.status === 'UPLOADED' || n.status === 'SUCCESS';
        const isNetFail = n.status === 'FAILED_TO_SUBMIT';
        const card = document.createElement('div');
        card.className = 'notif-card ' + (isSuccess ? 'success' : 'rejected');

        let iconClass = 'bi-file-earmark-text';
        if (n.type === 'lost') iconClass = 'bi-search-heart';
        if (n.type === 'found') iconClass = 'bi-box-seam';
        if (n.type === 'team') iconClass = 'bi-people';
        if (n.type === 'team_join') iconClass = 'bi-person-plus';
        if (n.type === 'resume') iconClass = 'bi-stars';

        let statusBadge = '<span class="badge-status open"><i class="bi bi-check-circle-fill me-1"></i> UPLOADED</span>';
        if (isNetFail) {
            statusBadge = '<span class="badge-status" style="background: rgba(245, 158, 11, 0.2); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.5);"><i class="bi bi-wifi-off me-1"></i> NETWORK FAILED</span>';
        } else if (!isSuccess) {
            statusBadge = '<span class="badge-status closed"><i class="bi bi-x-circle-fill me-1"></i> REJECTED</span>';
        }

        const label = isSuccess ? 'Status:' : (isNetFail ? 'Error Details:' : 'Rejection Reason:');

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
                    <strong>${label}</strong> ${escapeHtml(n.message)}
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

async function clearAllNotifications() {
    if (!confirm('Clear your entire upload activity history?')) return;
    try {
        await api('/api/v1/user/activities', { method: 'DELETE' });
    } catch {}
    serverActivities = [];
    saveNotifications([]);
    renderNotifications();
    toast('Activity history cleared', 'info');
}

function markAllNotificationsRead() {
    const list = getNotifications();
    list.forEach(n => n.read = true);
    saveNotifications(list);
}
