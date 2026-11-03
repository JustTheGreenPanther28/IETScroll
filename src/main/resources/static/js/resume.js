// static/js/resume.js
// AI Resume Checker submission and scoring view

async function submitResume() {
    const roleEl = document.getElementById('resumeRole');
    const expEl = document.getElementById('resumeExperience');
    const fileEl = document.getElementById('resumeFile');
    const resultEl = document.getElementById('resumeResult');

    const role = roleEl ? roleEl.value.trim() : '';
    const exp = expEl ? (expEl.value.trim() || '0') : '0';
    const file = fileEl && fileEl.files ? fileEl.files[0] : null;

    if (!role) return toast('Please specify target role', 'error');
    if (!file) return toast('Please upload a resume file (.pdf/.docx)', 'error');

    const fd = new FormData();
    fd.append('file', file);
    fd.append('role', role);
    fd.append('exp', exp);

    const btn = document.getElementById('resumeSubmitBtn');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-inline me-2"></span>Evaluating Resume with AI…';
    }
    if (resultEl) {
        resultEl.innerHTML = '<div class="stat-card text-center py-4"><div class="spinner-inline mb-2"></div><div class="text-muted">Analyzing resume structure, keywords, and qualifications...</div></div>';
    }

    try {
        const res = await api('/api/v1/ietscroll/resume/quality', { method: 'POST', formData: fd });
        if (resultEl) {
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
        }
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
        if (resultEl) {
            resultEl.innerHTML = `<div class="stat-card border border-danger border-opacity-50 mt-4 text-danger"><i class="bi bi-x-circle me-2"></i>${escapeHtml(e.message)}</div>`;
        }
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-magic me-1"></i> Analyze Resume';
        }
    }
}
