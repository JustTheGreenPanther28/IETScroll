// static/js/lostItems.js
// Lost Items browsing, user reports, and non-blocking report submission

let lostPage = 0;

async function loadLostBrowse() {
    const container = document.getElementById('lostBrowseList');
    if (!container) return;
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const res = await api('/api/v1/lost-item', { params: { page: lostPage, size: PAGE_SIZE } });
        const items = res.content || res.items || [];
        container.innerHTML = '';
        const pageLabel = document.getElementById('lostPageLabel');
        if (pageLabel) pageLabel.innerText = 'Page ' + (lostPage + 1);
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
    if (!container) return;
    container.innerHTML = '<div class="spinner-inline"></div>';
    try {
        const items = await api('/api/v1/lost-item/me');
        container.innerHTML = '';
        if (!items || !items.length) {
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
    const nameEl = document.getElementById('lostItemName');
    const descEl = document.getElementById('lostDescription');
    const locEl = document.getElementById('lostLocation');
    const prizeEl = document.getElementById('lostPrize');
    const imgEl = document.getElementById('lostItemImage');
    const previewEl = document.getElementById('lostItemImagePreview');

    const name = nameEl ? nameEl.value.trim() : '';
    const desc = descEl ? descEl.value.trim() : '';
    const location = locEl ? locEl.value.trim() : '';
    const prizeRaw = prizeEl ? prizeEl.value.trim() : '0';
    const parsedPrize = parseInt(prizeRaw, 10);
    const prize = (!isNaN(parsedPrize) && parsedPrize >= 0) ? parsedPrize : 0;
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

    const data = { lostItemname: name, description: desc, predictedLocation: location, prize: prize };
    const fd = new FormData();
    fd.append('data', JSON.stringify(data));
    if (imageFile) {
        fd.append('image', imageFile);
    }

    // Non-blocking UX: immediately inform the user, clear form, switch to "My Requests"
    toast('Your request has been submitted. Once analysis of your request is complete, we will take action.', 'info');

    if (nameEl) nameEl.value = '';
    if (descEl) descEl.value = '';
    if (locEl) locEl.value = '';
    if (prizeEl) prizeEl.value = '0';
    if (imgEl) imgEl.value = '';
    if (previewEl) previewEl.innerHTML = '';

    const tabs = document.querySelectorAll('#lost .tab-btn');
    if (tabs && tabs[1]) switchLostTab('mine', tabs[1]);

    // Async submit in background without blocking UI
    (async () => {
        try {
            await api('/api/v1/lost-item', { method: 'POST', formData: fd });
            toast('Lost item report verified and listed successfully!', 'success');
            addNotification({
                type: 'lost',
                title: name,
                status: 'UPLOADED',
                message: 'Lost item verified and published. Location: ' + location,
                time: new Date()
            });
            lostPage = 0;
            loadDashboardStats();
            loadLostMine();
        } catch (e) {
            toast('Lost report update: ' + e.message, 'error');
            const isNet = e.message && e.message.toLowerCase().includes('reach the server');
            addNotification({
                type: 'lost',
                title: name || 'Lost Item Report',
                status: isNet ? 'FAILED_TO_SUBMIT' : 'REJECTED_BY_MODERATION',
                message: e.message || 'Report was rejected during safety verification.',
                time: new Date()
            });
        }
    })();
}
