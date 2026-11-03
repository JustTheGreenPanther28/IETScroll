// static/js/overview.js
// Dashboard Overview Statistics & Admin Chart.js Visualizations

let reportsChartInstance = null;
let userGrowthChartInstance = null;

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

async function loadAdminAnalytics() {
    if (!isAdmin()) return;
    try {
        const stats = await api('/api/v1/admin/analytics');
        if (!stats) return;

        const totalUsersEl = document.getElementById('adminTotalUsers');
        const activeUsersEl = document.getElementById('adminActiveUsers');
        const blockedUsersEl = document.getElementById('adminBlockedUsers');
        const totalLostEl = document.getElementById('adminTotalLost');
        const totalFoundTeamsEl = document.getElementById('adminTotalFoundTeams');

        if (totalUsersEl) totalUsersEl.innerText = stats.totalUsers || 0;
        if (activeUsersEl) activeUsersEl.innerText = (stats.activeUsers || 0) + ' active';
        if (blockedUsersEl) blockedUsersEl.innerText = stats.blockedUsers || 0;
        if (totalLostEl) totalLostEl.innerText = stats.totalLostReports || 0;
        if (totalFoundTeamsEl) totalFoundTeamsEl.innerText = `${stats.totalFoundReports || 0} / ${stats.totalTeamsCreated || 0}`;

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
        if (reportsChartInstance) {
            reportsChartInstance.destroy();
            reportsChartInstance = null;
        }
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
        if (userGrowthChartInstance) {
            userGrowthChartInstance.destroy();
            userGrowthChartInstance = null;
        }
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
