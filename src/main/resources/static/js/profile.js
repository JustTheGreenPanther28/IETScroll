// static/js/profile.js
// User profile loading, credential and privilege tier inspection, and settings updates

async function loadProfile() {
    try {
        const user = await api('/api/v1/user');
        currentUserEmail = user.email || null;
        currentUserRole = user.role || 'STUDENT';
        currentUserPrivilege = user.privilegeClass || 'NORMAL';

        const welcomeName = document.getElementById('welcome-name');
        const topUsername = document.getElementById('top-username');
        const topEmail = document.getElementById('top-email');
        const pName = document.getElementById('p-name');
        const pUsername = document.getElementById('p-username');
        const pEmail = document.getElementById('p-email');

        const displayName = user.fullName || user.username || 'Student';
        if (welcomeName) welcomeName.innerText = displayName;
        if (topUsername) topUsername.innerText = displayName;
        if (topEmail) topEmail.innerText = user.email || '';
        if (pName) pName.innerText = user.fullName || '—';
        if (pUsername) pUsername.innerText = user.username || '—';
        if (pEmail) pEmail.innerText = user.email || '—';

        const privContainer = document.getElementById('top-privilege-badge');
        if (privContainer) {
            privContainer.innerHTML = renderPrivilegeBadge(currentUserPrivilege);
        }

        const initial = (user.fullName || user.username || '?').charAt(0).toUpperCase();
        const av1 = document.getElementById('avatar-icon');
        const av2 = document.getElementById('avatar-icon-mobile');
        if (av1) av1.innerText = initial;
        if (av2) av2.innerText = initial;

        // Role-based UI visibility control: only reveal Admin navigation & analytics to ADMIN
        const navUsers = document.getElementById('nav-users');
        const adminAnalytics = document.getElementById('adminAnalyticsSection');

        if (isAdmin()) {
            if (navUsers) navUsers.classList.remove('d-none');
            if (adminAnalytics) adminAnalytics.classList.remove('d-none');
            loadAdminAnalytics();
        } else {
            if (navUsers) navUsers.classList.add('d-none');
            if (adminAnalytics) adminAnalytics.classList.add('d-none');
        }

        updateNotifBadges();
    } catch (e) {
        toast('Could not load profile: ' + e.message, 'error');
    }
}

async function updateUsername() {
    const input = document.getElementById('newUsernameInput');
    const val = input ? input.value.trim() : '';
    if (!val) return toast('Enter a username first', 'error');
    try {
        await api('/api/v1/user/username/' + encodeURIComponent(val), { method: 'PATCH' });
        toast('Username updated');
        if (input) input.value = '';
        loadProfile();
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function updateFullname() {
    const input = document.getElementById('newFullnameInput');
    const val = input ? input.value.trim() : '';
    if (!val) return toast('Enter a full name first', 'error');
    try {
        await api('/api/v1/user/fullname/' + encodeURIComponent(val), { method: 'PATCH' });
        toast('Full name updated');
        if (input) input.value = '';
        loadProfile();
    } catch (e) {
        toast(e.message, 'error');
    }
}
