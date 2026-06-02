// Toast function
function showToast(message, type) {
    var container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        container.style.cssText = 'position:fixed;top:20px;right:20px;z-index:99999;';
        document.body.appendChild(container);
    }
    var toast = document.createElement('div');
    toast.className = 'toast ' + type;
    toast.style.cssText = 'padding:15px 20px;margin-bottom:10px;border-radius:8px;color:white;font-weight:500;min-width:300px;box-shadow:0 4px 12px rgba(0,0,0,0.15);';
    toast.style.background = type === 'success' ? '#10b981' : '#ef4444';

    var icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
    toast.innerHTML = '<i class="fas ' + icon + '" style="margin-right:8px;"></i> ' + message;
    container.appendChild(toast);

    // Tự động xóa sau 3 giây
    setTimeout(function() {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(function() { toast.remove(); }, 300);
    }, 3000);
}

// Xử lý message từ URL params (thay thế flash attributes)
function handleUrlMessages() {
    var urlParams = new URLSearchParams(window.location.search);
    var successMsg = urlParams.get('success');
    var errorMsg = urlParams.get('error');

    if (errorMsg) {
        var decodedError = decodeURIComponent(errorMsg);
        decodedError = decodedError.replace(/\+/g, ' ');
        showToast(decodedError, 'error');
    }

    if (successMsg) {
        var decodedSuccess = decodeURIComponent(successMsg);
        decodedSuccess = decodedSuccess.replace(/\+/g, ' ');
        showToast(decodedSuccess, 'success');
    }

    // Xóa params khỏi URL sau khi hiển thị toast
    if (successMsg || errorMsg) {
        var url = new URL(window.location.href);
        url.searchParams.delete('success');
        url.searchParams.delete('error');
        window.history.replaceState({}, document.title, url.toString());
    }
}

// Gọi hàm xử lý message khi trang load
document.addEventListener('DOMContentLoaded', function() {
    handleUrlMessages();
});

// Confirm modal variables
var pendingId = null;
var pendingStatus = null;
var pendingName = null;

// Show confirm modal
window.showConfirmModal = function(id, currentStatus, medicineName) {
    var isActive = currentStatus === 'Active';
    var title = isActive ? 'Clock Medicine' : 'Restore Medicine';
    var message = isActive
        ? 'Are you sure you want to clock "' + medicineName + '"?'
        : 'Are you sure you want to restore "' + medicineName + '"?';
    var subMessage = isActive
        ? 'This medicine will be hidden from active lists and cannot be prescribed to new patients.'
        : 'This medicine will become available again in active lists.';

    document.getElementById('confirmModalTitle').innerHTML = '<i class="fas fa-shield-alt" style="margin-right: 8px; color: #f59e0b;"></i> ' + title;
    document.getElementById('confirmMessage').innerHTML = '<i class="fas fa-pills" style="margin-right: 8px; color: #f59e0b;"></i> ' + message;
    document.getElementById('confirmSubMessage').innerText = subMessage;

    var iconElement = document.querySelector('#confirmModal .confirm-icon i');
    var iconDiv = document.querySelector('#confirmModal .confirm-icon');
    var okBtn = document.getElementById('okConfirmBtn');

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
    pendingName = medicineName;

    document.getElementById('confirmModal').classList.add('open');
    document.body.classList.add('modal-open');
};

function closeConfirmModal() {
    document.getElementById('confirmModal').classList.remove('open');
    document.body.classList.remove('modal-open');
    setTimeout(function() {
        pendingId = null;
        pendingStatus = null;
        pendingName = null;
    }, 300);
}

function executeAction() {
    if (pendingId && pendingStatus) {
        var isActive = pendingStatus === 'Active';
        var url = isActive ? '/admin/medicines/soft-delete/' + pendingId : '/admin/medicines/restore/' + pendingId;
        var form = document.createElement('form');
        form.method = 'POST';
        form.action = url;
        document.body.appendChild(form);

        var okBtn = document.getElementById('okConfirmBtn');
        var originalText = okBtn.innerHTML;
        okBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
        okBtn.disabled = true;

        setTimeout(function() {
            form.submit();
        }, 500);
    }
    closeConfirmModal();
}

// Sort functions
window.applySort = function(field) {
    var currentUrl = new URL(window.location.href);
    var currentSortField = currentUrl.searchParams.get('sortField') || 'medicationName';
    var currentSortDirection = currentUrl.searchParams.get('sortDirection') || 'asc';
    var newDirection = (currentSortField === field && currentSortDirection === 'asc') ? 'desc' : 'asc';
    currentUrl.searchParams.set('sortField', field);
    currentUrl.searchParams.set('sortDirection', newDirection);
    currentUrl.searchParams.set('page', '0');
    window.location.href = currentUrl.toString();
};

window.toggleDirection = function() {
    var currentUrl = new URL(window.location.href);
    var currentSortDirection = currentUrl.searchParams.get('sortDirection') || 'asc';
    var newDirection = currentSortDirection === 'asc' ? 'desc' : 'asc';
    currentUrl.searchParams.set('sortDirection', newDirection);
    currentUrl.searchParams.set('page', '0');
    window.location.href = currentUrl.toString();
};

document.addEventListener('DOMContentLoaded', function() {
    var sortBtn = document.getElementById('sortBtn');
    var sortMenu = document.getElementById('sortMenu');
    if (sortBtn && sortMenu) {
        sortBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            sortMenu.classList.toggle('open');
        });
        document.addEventListener('click', function(e) {
            if (!sortMenu.contains(e.target) && !sortBtn.contains(e.target)) {
                sortMenu.classList.remove('open');
            }
        });
    }

    document.getElementById('closeConfirmModalBtn').onclick = closeConfirmModal;
    document.getElementById('cancelConfirmBtn').onclick = closeConfirmModal;
    document.getElementById('okConfirmBtn').onclick = executeAction;
    var confirmModal = document.getElementById('confirmModal');
    confirmModal.onclick = function(e) { if (e.target === confirmModal) closeConfirmModal(); };
});

function closeSortMenu() { document.getElementById('sortMenu').classList.remove('open'); }

window.changePageSize = function() {
    var size = document.getElementById('pageSizeSelect').value;
    var currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set('size', size);
    currentUrl.searchParams.set('page', '0');
    window.location.href = currentUrl.toString();
};

var searchKeyword = document.getElementById('searchKeyword');
if (searchKeyword) {
    searchKeyword.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') { e.preventDefault(); document.getElementById('searchForm').submit(); }
    });
}

// ADD MODAL
var addModal = document.getElementById('addModal');
document.getElementById('btnShowAddModal').onclick = function() {
    addModal.classList.add('open');
    document.body.classList.add('modal-open');
};
document.getElementById('closeAddModalBtn').onclick = function() {
    addModal.classList.remove('open');
    document.body.classList.remove('modal-open');
    document.getElementById('addForm').reset();
};
document.getElementById('cancelAddModalBtn').onclick = function() {
    addModal.classList.remove('open');
    document.body.classList.remove('modal-open');
    document.getElementById('addForm').reset();
};
addModal.onclick = function(e) {
    if (e.target === addModal) {
        addModal.classList.remove('open');
        document.body.classList.remove('modal-open');
    }
};

// EDIT MODAL
var editModal = document.getElementById('editModal');
var editForm = document.getElementById('editForm');

window.openEditModal = function(id) {
    fetch('/admin/medicines/api/' + id)
        .then(function(res) { return res.json(); })
        .then(function(result) {
            if (result.success) {
                var med = result.data;
                document.getElementById('editModalTitle').innerText = 'Edit Medicine: ' + med.medicationName;
                document.getElementById('editMedicationId').value = med.medicationId;
                document.getElementById('editMedicationName').value = med.medicationName;
                document.getElementById('editFormSelect').value = med.form;
                document.getElementById('editConcentration').value = med.concentration;
                document.getElementById('editRouteSelect').value = med.administrationRoute;
                document.getElementById('editInstruction').value = med.usageInstruction;
                editForm.action = '/admin/medicines/edit/' + id;
                editModal.classList.add('open');
                document.body.classList.add('modal-open');
            }
        })
        .catch(function() { console.error('Error loading medicine data'); });
};

function closeEditModal() {
    editModal.classList.remove('open');
    document.body.classList.remove('modal-open');
}

document.getElementById('closeEditModalBtn').onclick = closeEditModal;
document.getElementById('cancelEditModalBtn').onclick = closeEditModal;
editModal.onclick = function(e) { if (e.target === editModal) closeEditModal(); };

// DETAIL DRAWER
var detailDrawer = document.getElementById('detailDrawer');
var detailOverlay = document.getElementById('detailDrawerOverlay');

window.viewDetail = function(id) {
    fetch('/admin/medicines/api/' + id)
        .then(function(res) { return res.json(); })
        .then(function(result) {
            if (result.success) {
                var med = result.data;
                document.getElementById('detailId').innerText = med.medicationId || '--';
                document.getElementById('detailName').innerText = med.medicationName || '--';
                document.getElementById('detailMedName').innerText = med.medicationName || '--';
                document.getElementById('detailConcentration').innerText = med.concentration || '--';
                document.getElementById('detailRoute').innerText = med.administrationRoute || '--';
                document.getElementById('detailInstruction').innerText = med.usageInstruction || 'No instruction available';
                document.getElementById('detailStatus').innerText = med.status || '--';
                var formText = med.form === 'tablet' ? 'Tablet' : (med.form === 'capsule' ? 'Capsule' : 'Injection');
                document.getElementById('detailForm').innerText = formText;
                document.getElementById('detailMeta').innerText = formText;
                detailDrawer.classList.add('open');
                detailOverlay.classList.add('open');
            }
        })
        .catch(function() { console.error('Error loading medicine details'); });
};

function closeDetailDrawer() {
    detailDrawer.classList.remove('open');
    detailOverlay.classList.remove('open');
}

document.getElementById('closeDetailDrawerBtn').onclick = closeDetailDrawer;
detailOverlay.onclick = closeDetailDrawer;

document.getElementById('editFromDrawerBtn').onclick = function() {
    var id = document.getElementById('detailId').innerText;
    if (id && id !== '--') {
        closeDetailDrawer();
        openEditModal(id);
    }
};

document.getElementById('deleteFromDrawerBtn').onclick = function() {
    var id = document.getElementById('detailId').innerText;
    var name = document.getElementById('detailName').innerText;
    if (id && id !== '--') {
        closeDetailDrawer();
        showConfirmModal(id, 'Active', name);
    }
};