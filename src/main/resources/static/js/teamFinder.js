// static/js/teamFinder.js
// Team Finder browsing, creation, join requests, and member management

let teamPage = 0;

async function loadTeamBrowse() {
    const container = document.getElementById('teamBrowseList');
    if (!container) return;
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const res = await api('/api/v1/team', { params: { page: teamPage, size: PAGE_SIZE } });
        const teams = res.content || res.items || [];
        container.innerHTML = '';
        const pageLabel = document.getElementById('teamPageLabel');
        if (pageLabel) pageLabel.innerText = 'Page ' + (teamPage + 1);
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
            const adminCloseBtn = (isAdmin() && !isOwnTeam)
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
                const joinBtn = wrap.querySelector('.btn-outline-accent');
                if (joinBtn) joinBtn.addEventListener('click', () => promptJoinRequest(t.publicId));
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
    if (!container) return;
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
    const purposeEl = document.getElementById('teamPurpose');
    const maxMemberEl = document.getElementById('teamMaxMember');
    const privacyEl = document.getElementById('teamPrivacy');
    const skillSelect = document.getElementById('teamSkillSelect');

    const purpose = purposeEl ? purposeEl.value.trim() : '';
    const maxMember = maxMemberEl ? parseInt(maxMemberEl.value, 10) : 4;
    const privacy = privacyEl ? privacyEl.value : 'PUBLIC';
    const skillIds = skillSelect ? Array.from(skillSelect.selectedOptions).map(o => parseInt(o.value, 10)) : [];

    if (!purpose) return toast('Team purpose/goal is required', 'error');

    // Non-blocking UX
    toast('Your request has been submitted. Once analysis of your request is complete, we will take action.', 'info');
    if (purposeEl) purposeEl.value = '';

    const tabs = document.querySelectorAll('#team .tab-btn');
    if (tabs && tabs[1]) switchTeamTab('mine', tabs[1]);

    (async () => {
        try {
            await api('/api/v1/team', {
                method: 'POST',
                jsonBody: { purpose, maxMember, privacy, skillIds }
            });
            toast('Team formed successfully!', 'success');
            addNotification({
                type: 'team',
                title: purpose,
                status: 'UPLOADED',
                message: 'Team created and active in directory.',
                time: new Date()
            });
            loadDashboardStats();
            loadTeamMine();
        } catch (e) {
            toast('Team creation update: ' + e.message, 'error');
            const isNet = e.message && e.message.toLowerCase().includes('reach the server');
            addNotification({
                type: 'team',
                title: purpose || 'Team Creation',
                status: isNet ? 'FAILED_TO_SUBMIT' : 'REJECTED_BY_MODERATION',
                message: e.message || 'Team creation rejected by moderation filter.',
                time: new Date()
            });
        }
    })();
}

async function loadTeamRequests() {
    const reqContainer = document.getElementById('teamRequestsList');
    const memContainer = document.getElementById('teamMembersList');
    if (reqContainer) reqContainer.innerHTML = '<div class="spinner-inline"></div>';
    if (memContainer) memContainer.innerHTML = '';
    try {
        const requests = await api('/api/v1/request-team/requests');
        if (reqContainer) {
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
        }
    } catch (e) {
        if (reqContainer) reqContainer.innerHTML = `<div class="empty-state text-danger">${escapeHtml(e.message)}</div>`;
    }

    try {
        const members = await api('/api/v1/request-team/team-members');
        if (memContainer) {
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
    if (!container) return;
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
            if (a.status === 'REMOVED') statusBadge = '<span class="badge-status closed">LEFT / REMOVED</span>';

            const leaveBtn = (a.status === 'ACCEPTED' && a.teamId)
                ? `<button class="btn-danger-soft btn-sm ms-2" onclick="leaveTeam('${escapeHtml(a.teamId)}')"><i class="bi bi-box-arrow-right me-1"></i> Leave Team</button>`
                : '';

            card.innerHTML = `
                <div class="flex-grow-1">
                    <div class="item-title d-flex align-items-center justify-content-between mb-1">
                        <span>Application to Team</span>
                        <div class="d-flex align-items-center">
                            ${statusBadge}
                            ${leaveBtn}
                        </div>
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

async function leaveTeam(teamId) {
    if (!confirm('Are you sure you want to leave this team?')) return;
    try {
        await api(`/api/v1/request-team/leave/${encodeURIComponent(teamId)}`, { method: 'PATCH' });
        toast('You have successfully left the team.');
        loadTeamApplications();
        if (typeof loadTeamMine === 'function') loadTeamMine();
    } catch (e) {
        toast(e.message, 'error');
    }
}
