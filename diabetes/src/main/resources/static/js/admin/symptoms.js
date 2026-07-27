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

// ==================== CONFIRM MODAL (Lock/Unlock/Delete) ====================
let pendingId = null;
let pendingStatus = null;   // 'Active' or 'Clocked'
let pendingName = null;
let pendingAction = null;   // 'lock' or 'delete'

window.showConfirmModal = function(id, currentStatus, symptomName) {
    const isActive = currentStatus === 'Active';
    const title = isActive ? 'Khóa triệu chứng' : 'Khôi phục triệu chứng';
    const message = isActive
        ? `Bạn có chắc chắn muốn khóa triệu chứng "${symptomName}"?`
        : `Bạn có chắc chắn muốn khôi phục triệu chứng "${symptomName}"?`;
    const subMessage = isActive
        ? 'Triệu chứng này sẽ bị tạm ẩn khỏi danh sách hoạt động.'
        : 'Triệu chứng này sẽ hoạt động trở lại bình thường.';

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
    pendingAction = 'lock';
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
        pendingAction = null;
    }, 300);
}

function executeAction() {
    if (pendingId && pendingAction) {
        const isActive = pendingStatus === 'Active';
        const url = isActive ? `/admin/symptoms/soft-delete/${pendingId}` : `/admin/symptoms/restore/${pendingId}`;
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = url;
        document.body.appendChild(form);
        // Disable button to prevent double submit
        const okBtn = document.getElementById('okConfirmBtn');
        okBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
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

if (editModal) {
    // Nếu modal đang mở sẵn (do backend render có editSymptom), khóa cuộn trang
    if (editModal.classList.contains('open')) {
        document.body.classList.add('modal-open');
    }

    // Khi click vào vùng overlay bên ngoài, xóa query parameter để đóng modal
    editModal.onclick = (e) => {
        if (e.target === editModal) {
            const url = new URL(window.location.href);
            url.searchParams.delete('editId');
            window.location.href = url.toString();
        }
    };
}
// ==================== BACKEND SEARCH & AUTO-RESET ON CLEAR ====================
const searchInput = document.getElementById('searchKeyword');
const searchForm = document.getElementById('symptomSearchForm');

if (searchForm && searchInput) {
    // Tự động submit để hiển thị lại tất cả khi xóa trắng ô tìm kiếm (dùng debounce để tránh lỗi mất tiêu điểm/reload khi gõ tiếng Việt IME)
    let searchTimeout = null;
    searchInput.addEventListener('input', function() {
        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }
        if (this.value.trim() === '') {
            searchTimeout = setTimeout(() => {
                if (searchInput.value.trim() === '') {
                    searchForm.submit();
                }
            }, 400);
        }
    });

    searchInput.addEventListener('search', function() {
        if (this.value.trim() === '') {
            searchForm.submit();
        }
    });

    const searchIcon = document.getElementById('searchIcon');
    if (searchIcon) {
        searchIcon.addEventListener('click', function() {
            searchForm.submit();
        });
    }
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