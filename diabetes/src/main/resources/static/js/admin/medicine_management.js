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

// Live search (debounced) - update table via AJAX JSON endpoint
var searchKeyword = document.getElementById('searchKeyword');
var searchForm = document.getElementById('searchForm');
if (searchForm) {
    searchForm.addEventListener('submit', function(e) {
        e.preventDefault();
        fetchAndRender(0);
    });
}

if (searchKeyword) {
    var debounceTimer;
    searchKeyword.addEventListener('input', function() {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(function() {
            fetchAndRender(0);
        }, 300);
    });
    searchKeyword.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            fetchAndRender(0);
        }
    });
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
        var formClass = med.form === 'tablet' ? 'badge-tablet' : (med.form === 'capsule' ? 'badge-capsule' : 'badge-injection');
        var formText = med.form === 'tablet' ? 'Tablet' : (med.form === 'capsule' ? 'Capsule' : 'Injection');
        var routeText = med.administrationRoute ? med.administrationRoute : '—';
        var instrText = med.usageInstruction ? med.usageInstruction : '—';
        var instrAbbrev = instrText.length > 80 ? instrText.substring(0, 80) + '...' : instrText;
        var statusClass = med.status === 'Active' ? 'badge-active' : 'badge-clocked';
        var lockIcon = med.status === 'Active' ? 'fas fa-lock' : 'fas fa-lock-open';
        var lockTitle = med.status === 'Active' ? 'Clock Medicine' : 'Restore Medicine';

        return `
            <tr>
                <td>
                    <div class="medicine-cell">
                        <span class="medicine-name">${escapeHtml(med.medicationName)}</span>
                        <span class="medicine-id">${escapeHtml(med.medicationId)} — ${escapeHtml(med.concentration || '')}</span>
                    </div>
                </td>
                <td>
                    <span class="${formClass}">${formText}</span>
                </td>
                <td>${escapeHtml(routeText)}</td>
                <td class="usage-instruction-text" title="${escapeHtml(instrText)}">${escapeHtml(instrAbbrev)}</td>
                <td>
                    <span class="${statusClass}">${escapeHtml(med.status)}</span>
                </td>
                <td class="action-group">
                    <button class="action-btn view" data-id="${escapeHtml(med.medicationId)}" onclick="viewDetail(this.getAttribute('data-id'))" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="action-btn edit" data-id="${escapeHtml(med.medicationId)}" onclick="openEditModal(this.getAttribute('data-id'))" title="Edit">
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
        var sortField = urlParams.get('sortField') || 'medicationName';
        var sortDirection = urlParams.get('sortDirection') || 'asc';

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
    var pageSize = urlParams.get('size') || '5';
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
                document.getElementById('detailInstruction').innerText = med.usageInstruction || 'Không có hướng dẫn sử dụng';
                document.getElementById('detailStatus').innerText = med.status ? 'Hoạt động' : 'Tạm khóa';
                var formText = med.form === 'tablet' ? 'Viên nén' : (med.form === 'capsule' ? 'Viên nang' : 'Thuốc tiêm');
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