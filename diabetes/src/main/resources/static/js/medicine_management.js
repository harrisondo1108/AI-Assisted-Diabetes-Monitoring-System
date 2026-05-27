
    // Flash messages from server
    var successMsg = /*[[${success}]]*/ null;
    var errorMsg = /*[[${error}]]*/ null;
    var deleteId = null;

    // Toast notification function
    function showToast(message, type) {
    var container = document.querySelector('.toast-container');
    if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
}
    var toast = document.createElement('div');
    toast.className = 'toast ' + type;
    var icon = type === 'success' ? 'fa-check-circle' : (type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle');
    toast.innerHTML = '<i class="fas ' + icon + '"></i> ' + message;
    container.appendChild(toast);
    setTimeout(function() { toast.remove(); }, 4000);
}

    // Show messages from server
    if (successMsg) showToast(successMsg, 'success');
    if (errorMsg) showToast(errorMsg, 'error');

    // ========== SEARCH ==========
    // Auto submit search when typing (with debounce)
    var searchTimeout;
    var searchInput = document.querySelector('.header-search input');
    if (searchInput) {
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function() {
            document.getElementById('searchForm').submit();
        }, 500);
    });
}

    // ========== DETAIL DRAWER ==========
    var detailDrawer = document.getElementById('detailDrawer');
    var detailOverlay = document.getElementById('detailDrawerOverlay');

    window.openDetailDrawer = function(id) {
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

                var formText = '';
                if (med.form === 'tablet') formText = 'Tablet';
                else if (med.form === 'capsule') formText = 'Capsule';
                else if (med.form === 'injection') formText = 'Injection';
                else formText = med.form || '--';
                document.getElementById('detailForm').innerText = formText;
                document.getElementById('detailMeta').innerText = formText;

                detailDrawer.classList.add('open');
                detailOverlay.classList.add('open');

                document.getElementById('editFromDrawerBtn').onclick = function() {
                    closeDetailDrawer();
                    openEditModal(id);
                };
                document.getElementById('deleteFromDrawerBtn').onclick = function() {
                    closeDetailDrawer();
                    showDeleteConfirm(id);
                };
            }
        })
        .catch(function() { showToast('Error loading details', 'error'); });
};

    function closeDetailDrawer() {
    detailDrawer.classList.remove('open');
    detailOverlay.classList.remove('open');
}

    document.getElementById('closeDetailDrawerBtn').addEventListener('click', closeDetailDrawer);
    detailOverlay.addEventListener('click', closeDetailDrawer);

    // ========== DELETE CONFIRM MODAL ==========
    var deleteModal = document.getElementById('deleteModal');

    window.showDeleteConfirm = function(id) {
    deleteId = id;
    fetch('/admin/medicines/api/' + id)
    .then(function(res) { return res.json(); })
    .then(function(result) {
    if (result.success) {
    var med = result.data;
    document.getElementById('deleteTitle').innerHTML = 'Delete: ' + med.medicationName;
    document.getElementById('deleteMessage').innerHTML = 'Are you sure you want to delete <strong>' + med.medicationName + '</strong>? This action cannot be undone.';
    deleteModal.classList.add('open');
    document.body.classList.add('modal-open');
}
});
};

    function closeDeleteModal() {
    deleteModal.classList.remove('open');
    document.body.classList.remove('modal-open');
    deleteId = null;
}

    function confirmDelete() {
    if (deleteId) {
    window.location.href = '/admin/medicines/delete/' + deleteId;
}
}

    document.getElementById('closeDeleteModalBtn').addEventListener('click', closeDeleteModal);
    document.getElementById('cancelDeleteBtn').addEventListener('click', closeDeleteModal);
    document.getElementById('confirmDeleteBtn').addEventListener('click', confirmDelete);
    deleteModal.addEventListener('click', function(e) {
    if (e.target === deleteModal) closeDeleteModal();
});

    // ========== ADD/EDIT MODAL ==========
    var modal = document.getElementById('medicineModal');
    var form = document.getElementById('medicineForm');

    function openAddModal() {
    document.getElementById('modalTitle').innerText = 'Add New Medicine';
    form.reset();
    document.getElementById('medicationId').value = '';
    form.action = '/admin/medicines/add';
    modal.classList.add('open');
    document.body.classList.add('modal-open');
}

    function closeModal() {
    modal.classList.remove('open');
    document.body.classList.remove('modal-open');
}

    window.openEditModal = function(id) {
    fetch('/admin/medicines/api/' + id)
        .then(function(res) { return res.json(); })
        .then(function(result) {
            if (result.success) {
                var med = result.data;
                document.getElementById('modalTitle').innerText = 'Edit Medicine: ' + med.medicationName;
                document.getElementById('medicationId').value = med.medicationId;
                document.getElementById('medicationName').value = med.medicationName;
                document.getElementById('form').value = med.form;
                document.getElementById('concentration').value = med.concentration;
                document.getElementById('administrationRoute').value = med.administrationRoute;
                document.getElementById('usageInstruction').value = med.usageInstruction;
                form.action = '/admin/medicines/edit/' + id;
                modal.classList.add('open');
                document.body.classList.add('modal-open');
            }
        })
        .catch(function() { showToast('Error loading medicine data', 'error'); });
};

    document.getElementById('btnAddMedicine').addEventListener('click', openAddModal);
    document.getElementById('closeModalBtn').addEventListener('click', closeModal);
    document.getElementById('cancelModalBtn').addEventListener('click', closeModal);
    modal.addEventListener('click', function(e) {
    if (e.target === modal) closeModal();
});

    // ========== FILTER & REFRESH ==========
    document.getElementById('btnRefresh').addEventListener('click', function() {
    window.location.reload();
});

    document.getElementById('filterFormSelect').addEventListener('change', function() {
    var formValue = this.value;
    var currentUrl = window.location.href.split('?')[0];
    if (formValue) {
    window.location.href = currentUrl + '?form=' + formValue;
} else {
    window.location.href = currentUrl;
}
});

    var urlForm = new URLSearchParams(window.location.search).get('form');
    if (urlForm) {
    var select = document.getElementById('filterFormSelect');
    if (select) select.value = urlForm;
}

    // ========== CHECK DELETE SUCCESS FROM URL ==========
    var urlParams = new URLSearchParams(window.location.search);
    var deleteSuccess = urlParams.get('deleteSuccess');
    var deleteError = urlParams.get('deleteError');
    if (deleteSuccess) showToast(deleteSuccess, 'success');
    if (deleteError) showToast(deleteError, 'error');