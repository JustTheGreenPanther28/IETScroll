// static/js/foundItems.js
// Found Items browsing, user reports, views tracking modal, and non-blocking submission

let foundPage = 0;

async function loadFoundBrowse() {
    const container = document.getElementById('foundBrowseList');
    if (!container) return;
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const res = await api('/api/v1/found-item', { params: { page: foundPage, size: PAGE_SIZE } });
        const items = res.content || res.items || [];
        container.innerHTML = '';
        const pageLabel = document.getElementById('foundPageLabel');
        if (pageLabel) pageLabel.innerText = 'Page ' + (foundPage + 1);
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
    if (!container) return;
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const items = await api('/api/v1/found-item/me');
        container.innerHTML = '';
        if (!items || !items.length) {
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
    const nameEl = document.getElementById('foundItemName');
    const descEl = document.getElementById('foundDescription');
    const locEl = document.getElementById('foundLocation');
    const imgEl = document.getElementById('foundItemImage');
    const previewEl = document.getElementById('foundItemImagePreview');

    const name = nameEl ? nameEl.value.trim() : '';
    const desc = descEl ? descEl.value.trim() : '';
    const location = locEl ? locEl.value.trim() : '';
    const imageFile = imgEl && imgEl.files ? imgEl.files[0] : null;

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
    if (imageFile) {
        const imgError = validateImageFile(imageFile);
        if (imgError) return toast(imgError, 'error');
    }

    const data = { foundItemName: name, description: desc, predictedLocation: location };
    const fd = new FormData();
    fd.append('data', JSON.stringify(data));
    if (imageFile) {
        fd.append('image', imageFile);
    }

    // Non-blocking UX: immediately inform the user, clear form, switch to "My Found Reports"
    toast('Your request has been submitted. Once analysis of your request is complete, we will take action.', 'info');

    if (nameEl) nameEl.value = '';
    if (descEl) descEl.value = '';
    if (locEl) locEl.value = '';
    if (imgEl) imgEl.value = '';
    if (previewEl) previewEl.innerHTML = '';

    const tabs = document.querySelectorAll('#found .tab-btn');
    if (tabs && tabs[1]) switchFoundTab('mine', tabs[1]);

    // Async submit in background without blocking UI
    (async () => {
        try {
            await api('/api/v1/found-item', { method: 'POST', formData: fd });
            toast('Found item report verified and listed successfully!', 'success');
            addNotification({
                type: 'found',
                title: name,
                status: 'UPLOADED',
                message: 'Found item verified and published. Location: ' + location,
                time: new Date()
            });
            foundPage = 0;
            loadDashboardStats();
            loadFoundMine();
        } catch (e) {
            toast('Found report update: ' + e.message, 'error');
            const isNet = e.message && e.message.toLowerCase().includes('reach the server');
            addNotification({
                type: 'found',
                title: name || 'Found Item Report',
                status: isNet ? 'FAILED_TO_SUBMIT' : 'REJECTED_BY_MODERATION',
                message: e.message || 'Report was rejected during safety verification.',
                time: new Date()
            });
        }
    })();
}

/* ============ VIEWS AUDIT MODAL ============ */
async function openViewsModal(reportId, type) {
    const listEl = document.getElementById('viewsModalList');
    if (!listEl) return;
    listEl.innerHTML = '<div class="spinner-inline"></div> Fetching viewers...';
    const modalEl = document.getElementById('viewsModal');
    if (window.bootstrap && bootstrap.Modal) {
        const m = bootstrap.Modal.getOrCreateInstance(modalEl);
        m.show();
    } else if (modalEl) {
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
            item.className = 'p-2 border border-secondary border-opacity-25 rounded bg-black bg-opacity-25 d-flex justify-content-between align-items-center mb-2';
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
    if (!modalEl) return;
    if (window.bootstrap && bootstrap.Modal) {
        const m = bootstrap.Modal.getInstance(modalEl);
        if (m) m.hide();
    }
    modalEl.classList.remove('show');
    modalEl.style.display = 'none';
}
