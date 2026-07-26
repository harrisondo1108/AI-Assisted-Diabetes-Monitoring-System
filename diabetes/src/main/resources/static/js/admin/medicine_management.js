// Toast function (sử dụng class từ CSS để không chặn sự kiện click bên ngoài)
function showToast(message, type) {
    var container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    var toast = document.createElement('div');
    toast.className = 'toast ' + (type === 'success' ? 'success' : 'error');

    var icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas ${icon}"></i>
            <span>${escapeHtml(message)}</span>
        </div>
        <button type="button" class="toast-close" title="Đóng">&times;</button>
    `;

    var removeToast = function() {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(function() { toast.remove(); }, 300);
    };

    toast.addEventListener('click', removeToast);
    container.appendChild(toast);

    // Tự động xóa sau 4 giây
    setTimeout(removeToast, 4000);
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
    var isActive = currentStatus === 'true' || currentStatus === true || currentStatus === 'Active';
    var title = isActive ? 'Khóa Thuốc' : 'Khôi phục Thuốc';
    var message = isActive
        ? 'Bạn có chắc chắn muốn khóa thuốc "' + medicineName + '"?'
        : 'Bạn có chắc chắn muốn khôi phục thuốc "' + medicineName + '"?';
    var subMessage = isActive
        ? 'Thuốc này sẽ bị ẩn khỏi danh sách hoạt động và không thể kê đơn cho bệnh nhân mới.'
        : 'Thuốc này sẽ hoạt động trở lại bình thường.';

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
        var isActive = pendingStatus === 'true' || pendingStatus === true || pendingStatus === 'Active';
        var url = isActive ? '/admin/medicines/soft-delete/' + pendingId : '/admin/medicines/restore/' + pendingId;
        var form = document.createElement('form');
        form.method = 'POST';
        form.action = url;
        document.body.appendChild(form);

        var okBtn = document.getElementById('okConfirmBtn');
        var originalText = okBtn.innerHTML;
        okBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
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

// Backend search - submit form khi ấn Enter hoặc click nút Search icon
var searchForm = document.getElementById('searchForm');
var searchIcon = document.getElementById('searchIcon');

if (searchForm && searchIcon) {
    searchIcon.addEventListener('click', function() {
        searchForm.submit();
    });
}

function normalizeForm(form) {
    if (!form) return '';
    var f = String(form).trim();
    if (f === 'tablet' || f === 'Viên nén') return 'Viên nén';
    if (f === 'capsule' || f === 'Viên nang') return 'Viên nang';
    if (f === 'injection' || f === 'Thuốc tiêm') return 'Thuốc tiêm';
    return f;
}

function normalizeRoute(route) {
    if (!route) return '';
    var r = String(route).trim();
    if (r === 'Oral' || r === 'Đường uống') return 'Đường uống';
    if (r === 'Subcutaneous' || r === 'Tiêm dưới da') return 'Tiêm dưới da';
    if (r === 'Intravenous' || r === 'Tiêm tĩnh mạch') return 'Tiêm tĩnh mạch';
    if (r === 'Intramuscular' || r === 'Tiêm bắp') return 'Tiêm bắp';
    return r;
}

function renderMedicines(list) {
    var tbody = document.querySelector('.data-table tbody');
    if (!tbody) return;
    if (list.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="empty-row">
                    <i class="fas fa-pills"></i>
                    <p>No medications found</p>
                </td>
            </tr>`;
        return;
    }
    var html = list.map(function(med) {
        var formVal = normalizeForm(med.form);
        var formClass = formVal === 'Viên nén' ? 'badge-tablet' : (formVal === 'Viên nang' ? 'badge-capsule' : 'badge-injection');
        var formText = formVal || '—';
        var routeText = normalizeRoute(med.administrationRoute) || '—';
        var instrText = med.usageInstruction ? med.usageInstruction : '—';
        var instrAbbrev = instrText.length > 80 ? instrText.substring(0, 80) + '...' : instrText;
        var statusClass = med.status === 'Active' ? 'badge-active' : 'badge-clocked';
        var statusText = med.status === 'Active' ? 'Hoạt động' : 'Tạm khóa';
        var lockIcon = med.status === 'Active' ? 'fas fa-lock' : 'fas fa-lock-open';
        var lockTitle = med.status === 'Active' ? 'Khóa thuốc' : 'Khôi phục thuốc';

        return `
            <tr>
                <td>
                    <div class="medicine-cell">
                        <span class="medicine-name">${escapeHtml(med.medicationName)}</span>
                        <span class="medicine-id">${escapeHtml(med.medicationId)} — ${escapeHtml(med.concentration || '')}</span>
                    </div>
                </td>
                <td>
                    <span class="${formClass}">${escapeHtml(formText)}</span>
                </td>
                <td>${escapeHtml(routeText)}</td>
                <td class="usage-instruction-text" title="${escapeHtml(instrText)}">${escapeHtml(instrAbbrev)}</td>
                <td>
                    <span class="${statusClass}">${escapeHtml(statusText)}</span>
                </td>
                <td class="action-group">
                    <button class="action-btn view" data-id="${escapeHtml(med.medicationId)}" onclick="viewDetail(this.getAttribute('data-id'))" title="Xem chi tiết">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="action-btn edit" data-id="${escapeHtml(med.medicationId)}" onclick="openEditModal(this.getAttribute('data-id'))" title="Chỉnh sửa">
                        <i class="fas fa-pen"></i>
                    </button>
                    <button class="action-btn soft-delete" data-id="${escapeHtml(med.medicationId)}" data-status="${escapeHtml(med.status)}" data-name="${escapeHtml(med.medicationName)}" onclick="showConfirmModal(this.getAttribute('data-id'), this.getAttribute('data-status'), this.getAttribute('data-name'))" title="${lockTitle}">
                        <i class="${lockIcon}"></i>
                    </button>
                </td>
            </tr>`;
    }).join('\n');
    tbody.innerHTML = html;
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"'`]/g, function (s) {
        return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;', '`':'&#96;'})[s];
    });
}

function renderPagination(currentPage, totalPages, totalItems, pageSize) {
    var container = document.querySelector('.pagination-container');
    if (!container) return;
    if (totalPages <= 0) {
        container.style.display = 'none';
        return;
    }
    container.style.display = 'flex';

    var infoSpan = container.querySelector('.pagination-info');
    if (infoSpan) {
        infoSpan.innerHTML = 'Showing page <span>' + (currentPage + 1) + '</span> of <span>' + totalPages + '</span> (Total <span>' + totalItems + '</span> items)';
    }

    var controlsDiv = container.querySelector('.pagination-controls');
    if (controlsDiv) {
        var status = document.getElementById('statusSelect')?.value || '';
        var form = document.getElementById('filterFormSelect')?.value || '';
        var route = document.getElementById('routeSelect')?.value || '';
        var keyword = (document.getElementById('searchKeyword')?.value || '').trim();
        var urlParams = new URLSearchParams(window.location.search);
        var sortField = urlParams.get('sortField') || 'medicationId';
        var sortDirection = urlParams.get('sortDirection') || 'desc';

        var buildPageUrl = function(p) {
            var params = new URLSearchParams();
            params.set('page', p);
            params.set('size', pageSize);
            if (status) params.set('status', status);
            if (form) params.set('form', form);
            if (route) params.set('route', route);
            if (keyword) params.set('keyword', keyword);
            if (sortField) params.set('sortField', sortField);
            if (sortDirection) params.set('sortDirection', sortDirection);
            return '/admin/medicines?' + params.toString();
        };

        var html = '';
        if (currentPage > 0) {
            html += '<a href="' + buildPageUrl(0) + '" class="pagination-btn"><i class="fas fa-chevron-left"></i> First</a>';
            html += '<a href="' + buildPageUrl(currentPage - 1) + '" class="pagination-btn"><i class="fas fa-chevron-left"></i> Previous</a>';
        }

        html += '<div class="pagination-pages">';
        var startPage = Math.max(0, currentPage - 2);
        var endPage = Math.min(totalPages - 1, currentPage + 2);
        for (var i = startPage; i <= endPage; i++) {
            var activeClass = i === currentPage ? 'active' : '';
            html += '<a href="' + buildPageUrl(i) + '" class="page-number ' + activeClass + '">' + (i + 1) + '</a>';
        }
        html += '</div>';

        if (currentPage < totalPages - 1) {
            html += '<a href="' + buildPageUrl(currentPage + 1) + '" class="pagination-btn">Next <i class="fas fa-chevron-right"></i></a>';
            html += '<a href="' + buildPageUrl(totalPages - 1) + '" class="pagination-btn">Last <i class="fas fa-chevron-right"></i></a>';
        }

        controlsDiv.innerHTML = html;
        
        controlsDiv.querySelectorAll('a').forEach(function(a) {
            a.addEventListener('click', function(e) {
                e.preventDefault();
                var href = this.getAttribute('href');
                window.scrollTo({ top: 0, behavior: 'smooth' });
                setTimeout(function() { window.location.href = href; }, 120);
            });
        });
    }
}

function fetchAndRender(page) {
    var status = document.getElementById('statusSelect')?.value || '';
    var form = document.getElementById('filterFormSelect')?.value || '';
    var route = document.getElementById('routeSelect')?.value || '';
    var keyword = (document.getElementById('searchKeyword')?.value || '').trim();
    var urlParams = new URLSearchParams(window.location.search);
    var pageSize = urlParams.get('size') || '7';
    var sortField = urlParams.get('sortField') || 'medicationName';
    var sortDirection = urlParams.get('sortDirection') || 'asc';

    var url = '/admin/medicines/list?page=' + page +
              '&size=' + pageSize +
              '&status=' + status +
              '&form=' + form +
              '&route=' + route +
              '&keyword=' + encodeURIComponent(keyword) +
              '&sortField=' + sortField +
              '&sortDirection=' + sortDirection;

    fetch(url)
        .then(function(res) { return res.json(); })
        .then(function(data) {
            renderMedicines(data.content || []);
            renderPagination(data.currentPage, data.totalPages, data.totalElements, data.pageSize);
            
            var currentUrl = new URL(window.location.href);
            currentUrl.searchParams.set('page', page);
            if (keyword) currentUrl.searchParams.set('keyword', keyword);
            else currentUrl.searchParams.delete('keyword');
            window.history.replaceState({}, document.title, currentUrl.toString());
        })
        .catch(function(err) {
            console.error('Error in live search:', err);
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
                document.getElementById('editModalTitle').innerText = 'Chỉnh sửa Thuốc: ' + med.medicationName;
                document.getElementById('editMedicationId').value = med.medicationId;
                document.getElementById('editMedicationName').value = med.medicationName;
                document.getElementById('editFormSelect').value = normalizeForm(med.form);
                document.getElementById('editConcentration').value = med.concentration || '';
                document.getElementById('editRouteSelect').value = normalizeRoute(med.administrationRoute);
                document.getElementById('editInstruction').value = med.usageInstruction || '';
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

// Validation Form Frontend trước khi gửi
function validateMedicineForm(formElement) {
    var nameInput = formElement.querySelector('input[name="medicationName"]');
    var formSelect = formElement.querySelector('select[name="form"]');
    var routeSelect = formElement.querySelector('select[name="administrationRoute"]');

    if (nameInput && nameInput.value.trim() === '') {
        showToast('Tên thuốc không được để trống!', 'error');
        nameInput.focus();
        return false;
    }
    if (nameInput && nameInput.value.trim().length > 100) {
        showToast('Tên thuốc không được vượt quá 100 ký tự!', 'error');
        nameInput.focus();
        return false;
    }
    if (nameInput) {
        var validNameRegex = /^[\p{L}0-9\s\-\.\/\+]+$/u;
        if (!validNameRegex.test(nameInput.value.trim())) {
            showToast('Tên thuốc không được chứa ký tự đặc biệt (*, @, #, $, ...)!', 'error');
            nameInput.focus();
            return false;
        }
    }
    if (formSelect && !formSelect.value) {
        showToast('Vui lòng chọn dạng bào chế!', 'error');
        formSelect.focus();
        return false;
    }
    if (routeSelect && !routeSelect.value) {
        showToast('Vui lòng chọn đường dùng!', 'error');
        routeSelect.focus();
        return false;
    }
    return true;
}

var addForm = document.getElementById('addForm');
if (addForm) {
    addForm.addEventListener('submit', function(e) {
        if (!validateMedicineForm(addForm)) {
            e.preventDefault();
        }
    });
}

if (editForm) {
    editForm.addEventListener('submit', function(e) {
        if (!validateMedicineForm(editForm)) {
            e.preventDefault();
        }
    });
}

// Tự động ngăn nhập ký tự đặc biệt trong ô Tên Thuốc
function sanitizeMedicineNameInput(inputElement) {
    if (!inputElement) return;
    inputElement.addEventListener('input', function() {
        var originalValue = this.value;
        var cleanedValue = originalValue.replace(/[^\p{L}0-9\s\-\.\/\+]/gu, '');
        if (originalValue !== cleanedValue) {
            this.value = cleanedValue;
        }
    });
}
var addNameInput = document.getElementById('medicationName');
var editNameInput = document.getElementById('editMedicationName');
if (addNameInput) sanitizeMedicineNameInput(addNameInput);
if (editNameInput) sanitizeMedicineNameInput(editNameInput);

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
                document.getElementById('detailRoute').innerText = normalizeRoute(med.administrationRoute) || '--';
                document.getElementById('detailInstruction').innerText = med.usageInstruction || 'Không có hướng dẫn sử dụng';
                document.getElementById('detailStatus').innerText = (med.status === 'Active' || med.status === 'true' || med.status === true) ? 'Hoạt động' : 'Tạm khóa';
                var formText = normalizeForm(med.form) || '--';
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