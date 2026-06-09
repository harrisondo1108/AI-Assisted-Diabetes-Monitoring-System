// symptom_management.js

// ==================== TOAST NOTIFICATION ====================
function showToast(message, type) {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
    toast.innerHTML = `<i class="fas ${icon}"></i> ${message}`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('hide');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Xử lý message từ URL (sau redirect)
function handleUrlMessages() {
    const urlParams = new URLSearchParams(window.location.search);
    const successMsg = urlParams.get('success');
    const errorMsg = urlParams.get('error');
    if (errorMsg) {
        showToast(decodeURIComponent(errorMsg).replace(/\+/g, ' '), 'error');
    }
    if (successMsg) {
        showToast(decodeURIComponent(successMsg).replace(/\+/g, ' '), 'success');
    }
    if (successMsg || errorMsg) {
        const url = new URL(window.location.href);
        url.searchParams.delete('success');
        url.searchParams.delete('error');
        window.history.replaceState({}, document.title, url.toString());
    }
}

// ==================== CONFIRM MODAL (Lock/Unlock) ====================
let pendingId = null;
let pendingStatus = null;   // 'Active' or 'Clocked'
let pendingName = null;

window.showConfirmModal = function(id, currentStatus, symptomName) {
    const isActive = currentStatus === 'Active';
    const title = isActive ? 'Clock Symptom' : 'Restore Symptom';
    const message = isActive
        ? `Are you sure you want to clock "${symptomName}"?`
        : `Are you sure you want to restore "${symptomName}"?`;
    const subMessage = isActive
        ? 'This symptom will be hidden from active lists.'
        : 'This symptom will become available again.';

    document.getElementById('confirmModalTitle').innerHTML = `<i class="fas fa-shield-alt" style="margin-right: 8px; color: #f59e0b;"></i> ${title}`;
    document.getElementById('confirmMessage').innerHTML = `<i class="fas fa-head-side-medical" style="margin-right: 8px; color: #f59e0b;"></i> ${message}`;
    document.getElementById('confirmSubMessage').innerText = subMessage;

    const iconElement = document.querySelector('#confirmModal .confirm-icon i');
    const iconDiv = document.querySelector('#confirmModal .confirm-icon');
    const okBtn = document.getElementById('okConfirmBtn');

    if (isActive) {
        iconElement.className = 'fas fa-lock';
        iconElement.style.color = '#d97706';
        iconDiv.style.background = 'linear-gradient(135deg, #fff3e0, #ffe8cc)';
        okBtn.style.background = 'linear-gradient(135deg, #f59e0b, #d97706)';
    } else {
        iconElement.className = 'fas fa-lock-open';
        iconElement.style.color = '#10b981';
        iconDiv.style.background = 'linear-gradient(135deg, #d1fae5, #a7f3d0)';
        okBtn.style.background = 'linear-gradient(135deg, #10b981, #059669)';
    }

    pendingId = id;
    pendingStatus = currentStatus;
    pendingName = symptomName;
    document.getElementById('confirmModal').classList.add('open');
    document.body.classList.add('modal-open');
};

function closeConfirmModal() {
    document.getElementById('confirmModal').classList.remove('open');
    document.body.classList.remove('modal-open');
    setTimeout(() => {
        pendingId = null;
        pendingStatus = null;
        pendingName = null;
    }, 300);
}

function executeAction() {
    if (pendingId && pendingStatus) {
        const isActive = pendingStatus === 'Active';
        const url = isActive ? `/admin/symptoms/soft-delete/${pendingId}` : `/admin/symptoms/restore/${pendingId}`;
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = url;
        document.body.appendChild(form);
        // Disable button to prevent double submit
        const okBtn = document.getElementById('okConfirmBtn');
        const originalText = okBtn.innerHTML;
        okBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
        okBtn.disabled = true;
        setTimeout(() => form.submit(), 100);
    }
    closeConfirmModal();
}

// ==================== ADD MODAL ====================
const addModal = document.getElementById('addModal');
const addForm = document.getElementById('addForm');

document.getElementById('btnShowAddModal').onclick = () => {
    addModal.classList.add('open');
    document.body.classList.add('modal-open');
};
document.getElementById('closeAddModalBtn').onclick = () => {
    addModal.classList.remove('open');
    document.body.classList.remove('modal-open');
    if (addForm) addForm.reset();
};
document.getElementById('cancelAddModalBtn').onclick = () => {
    addModal.classList.remove('open');
    document.body.classList.remove('modal-open');
    if (addForm) addForm.reset();
};
addModal.onclick = (e) => {
    if (e.target === addModal) {
        addModal.classList.remove('open');
        document.body.classList.remove('modal-open');
        if (addForm) addForm.reset();
    }
};

// ==================== EDIT MODAL ====================
const editModal = document.getElementById('editModal');
const editForm = document.getElementById('editForm');

window.openEditModal = function(id) {
    fetch(`/admin/symptoms/api/${id}`)
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                const sym = result.data;
                document.getElementById('editModalTitle').innerText = `Edit Symptom: ${sym.symptomName}`;
                document.getElementById('editSymptomId').value = sym.symptomId;
                document.getElementById('editSymptomName').value = sym.symptomName;
                editForm.action = `/admin/symptoms/edit/${id}`;
                editModal.classList.add('open');
                document.body.classList.add('modal-open');
            } else {
                showToast('Failed to load symptom data', 'error');
            }
        })
        .catch(err => {
            console.error('Error loading symptom:', err);
            showToast('Error loading symptom data', 'error');
        });
};

function closeEditModal() {
    editModal.classList.remove('open');
    document.body.classList.remove('modal-open');
}

document.getElementById('closeEditModalBtn').onclick = closeEditModal;
document.getElementById('cancelEditModalBtn').onclick = closeEditModal;
editModal.onclick = (e) => {
    if (e.target === editModal) closeEditModal();
};
// ==================== THÊM MỚI: REAL-TIME SEARCH ====================
// Lấy các phần tử
const searchInput = document.getElementById('searchKeyword');
const symptomTableBody = document.getElementById('symptomTableBody');
let searchTimeout = null;

// Hàm tải dữ liệu từ API
function fetchSymptomsByKeyword(keyword) {
    const statusSelect = document.getElementById('statusSelect');
    const currentStatus = statusSelect ? statusSelect.value : '';
    const url = `/admin/symptoms/list?keyword=${encodeURIComponent(keyword)}&status=${currentStatus}&page=0&size=8`;
    fetch(url)
        .then(res => res.json())
        .then(data => {
            // Cập nhật bảng
            if (data.content && data.content.length > 0) {
                let html = '';
                data.content.forEach(sym => {
                    const statusClass = sym.status ? 'badge-active' : 'badge-clocked';
                    const statusText = sym.status ? 'Active' : 'Clocked';
                    const lockIcon = sym.status ? 'fas fa-lock' : 'fas fa-lock-open';
                    const lockTitle = sym.status ? 'Clock Symptom' : 'Restore Symptom';
                    html += `
                        <tr>
                            <td>
                                <div class="symptom-cell">
                                    <span class="symptom-name">${escapeHtml(sym.symptomName)}</span>
                                    <span class="symptom-id">${escapeHtml(sym.symptomId)}</span>
                                </div>
                            </td>
                            <td><span class="${statusClass}">${statusText}</span></td>
                            <td class="action-group">
                                <button class="action-btn edit" data-id="${escapeHtml(sym.symptomId)}" onclick="openEditModal(this.getAttribute('data-id'))"><i class="fas fa-pen"></i></button>
                                <button class="action-btn soft-delete" data-id="${escapeHtml(sym.symptomId)}" data-status="${statusText}" data-name="${escapeHtml(sym.symptomName)}" onclick="showConfirmModal(this.getAttribute('data-id'), this.getAttribute('data-status'), this.getAttribute('data-name'))" title="${lockTitle}"><i class="${lockIcon}"></i></button>
                            </td>
                        </tr>
                    `;
                });
                symptomTableBody.innerHTML = html;
            } else {
                symptomTableBody.innerHTML = `<tr><td colspan="3" class="empty-row"><i class="fas fa-head-side-medical"></i><p>No symptoms found</p></td></tr>`;
            }
            // Cập nhật thống kê (nếu cần)
            fetchStats();
        })
        .catch(err => console.error('Search error:', err));
}

function fetchStats() {
    fetch('/admin/symptoms/stats')
        .then(res => res.json())
        .then(stats => {
            document.getElementById('statTotal').innerText = stats.totalSymptoms || 0;
            document.getElementById('statActive').innerText = stats.activeSymptoms || 0;
            document.getElementById('statClocked').innerText = stats.clockedSymptoms || 0;
        })
        .catch(err => console.error('Stats error:', err));
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"'`]/g, function(s) {
        return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;','`':'&#96;'})[s];
    });
}

// Gắn sự kiện real-time cho ô tìm kiếm
if (searchInput) {
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        const keyword = this.value.trim();
        searchTimeout = setTimeout(() => {
            fetchSymptomsByKeyword(keyword);
        }, 300);
    });
}

// Khi trang load, nếu có từ khóa trong URL (do filter status submit) thì vẫn hiển thị đúng
// Không cần thêm gì vì Thymeleaf đã render sẵn.
// ==================== INITIALIZE ====================
document.addEventListener('DOMContentLoaded', () => {
    handleUrlMessages();

    // Confirm modal event listeners
    const confirmModal = document.getElementById('confirmModal');
    document.getElementById('closeConfirmModalBtn').onclick = closeConfirmModal;
    document.getElementById('cancelConfirmBtn').onclick = closeConfirmModal;
    document.getElementById('okConfirmBtn').onclick = executeAction;
    confirmModal.onclick = (e) => {
        if (e.target === confirmModal) closeConfirmModal();
    };
});