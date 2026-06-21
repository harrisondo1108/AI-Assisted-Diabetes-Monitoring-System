document.addEventListener('DOMContentLoaded', function () {
    initDefineTestModal();
    initViewDetailModal();
    initLockConfirmModal();
    initTableSearch();
    initPagination();
});

let currentPage = 1;
const rowsPerPage = 8;

/* PAGINATION */

function initPagination() {
    const rows = document.querySelectorAll('#labTestTableBody tr');

    rows.forEach(function (row) {
        row.dataset.filtered = 'visible';
    });

    currentPage = 1;
    renderPagination();
}

function renderPagination() {
    const allRows = Array.from(document.querySelectorAll('#labTestTableBody tr'));
    const visibleRows = allRows.filter(function (row) {
        return row.dataset.filtered !== 'hidden';
    });

    const totalItems = visibleRows.length;
    const totalPages = Math.ceil(totalItems / rowsPerPage);

    if (currentPage > totalPages) {
        currentPage = totalPages || 1;
    }

    allRows.forEach(function (row) {
        row.style.display = 'none';
    });

    visibleRows.forEach(function (row, index) {
        const start = (currentPage - 1) * rowsPerPage;
        const end = currentPage * rowsPerPage;

        if (index >= start && index < end) {
            row.style.display = '';
        }
    });

    renderPageButtons(totalPages, totalItems);
}

function renderPageButtons(totalPages, totalItems) {
    const pageNumbers = document.getElementById('pageNumbers');
    const paginationInfo = document.getElementById('paginationInfo');
    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');

    if (!pageNumbers || !paginationInfo || !prevBtn || !nextBtn) {
        return;
    }

    pageNumbers.innerHTML = '';

    const startItem = totalItems === 0 ? 0 : (currentPage - 1) * rowsPerPage + 1;
    const endItem = Math.min(currentPage * rowsPerPage, totalItems);

    paginationInfo.textContent =
        'Hiển thị ' + startItem + ' - ' + endItem + ' / ' + totalItems + ' xét nghiệm';

    prevBtn.disabled = currentPage <= 1;
    nextBtn.disabled = currentPage >= totalPages || totalPages === 0;

    for (let i = 1; i <= totalPages; i++) {
        const button = document.createElement('button');

        button.type = 'button';
        button.className = 'page-number';
        button.textContent = i;

        if (i === currentPage) {
            button.classList.add('active');
        }

        button.addEventListener('click', function () {
            currentPage = i;
            renderPagination();
        });

        pageNumbers.appendChild(button);
    }

    prevBtn.onclick = function () {
        if (currentPage > 1) {
            currentPage--;
            renderPagination();
        }
    };

    nextBtn.onclick = function () {
        if (currentPage < totalPages) {
            currentPage++;
            renderPagination();
        }
    };
}

/* SEARCH */

function initTableSearch() {
    const searchInput = document.getElementById('tableSearch');
    const rows = document.querySelectorAll('#labTestTableBody tr');

    if (!searchInput) {
        return;
    }

    searchInput.addEventListener('input', function () {
        const keyword = searchInput.value.toLowerCase().trim();

        rows.forEach(function (row) {
            const rowText = row.innerText.toLowerCase();

            if (rowText.includes(keyword)) {
                row.dataset.filtered = 'visible';
            } else {
                row.dataset.filtered = 'hidden';
            }
        });

        currentPage = 1;
        renderPagination();
    });
}

/* DEFINE NEW TEST MODAL */

function initDefineTestModal() {
    const btnDefineTest = document.getElementById('btnDefineTest');
    const modal = document.getElementById('defineTestModal');
    const btnCloseModal = document.getElementById('btnCloseModal');
    const btnCancelModal = document.getElementById('btnCancelModal');

    if (!btnDefineTest || !modal) {
        return;
    }

    function openModal() {
        modal.classList.add('show');
        document.body.style.overflow = 'hidden';
    }

    function closeModal() {
        modal.classList.remove('show');
        document.body.style.overflow = '';
    }

    btnDefineTest.addEventListener('click', openModal);

    if (btnCloseModal) {
        btnCloseModal.addEventListener('click', closeModal);
    }

    if (btnCancelModal) {
        btnCancelModal.addEventListener('click', closeModal);
    }

    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            closeModal();
        }
    });
}

/* VIEW DETAIL MODAL */

function initViewDetailModal() {
    const modal = document.getElementById('viewDetailModal');
    const btnClose = document.getElementById('btnCloseViewModal');
    const viewButtons = document.querySelectorAll('.view-btn');

    if (!modal) {
        return;
    }

    function closeModal() {
        modal.classList.remove('show');
        document.body.style.overflow = '';
    }

    viewButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            document.getElementById('detailId').innerText =
                button.dataset.id || '---';

            document.getElementById('detailName').innerText =
                button.dataset.name || '---';

            document.getElementById('detailUnit').innerText =
                button.dataset.unit || '---';

            document.getElementById('detailMin').innerText =
                button.dataset.min || '---';

            document.getElementById('detailMax').innerText =
                button.dataset.max || '---';

            document.getElementById('detailDescription').innerText =
                button.dataset.description || '---';

            modal.classList.add('show');
            document.body.style.overflow = 'hidden';
        });
    });

    if (btnClose) {
        btnClose.addEventListener('click', closeModal);
    }

    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            closeModal();
        }
    });
}

/* LOCK CONFIRM MODAL */

function initLockConfirmModal() {
    let selectedLockForm = null;

    const lockModal = document.getElementById('lockConfirmModal');
    const btnClose = document.getElementById('btnCloseLockModal');
    const btnCancel = document.getElementById('btnCancelLock');
    const btnConfirm = document.getElementById('btnConfirmLock');

    if (!lockModal || !btnConfirm) {
        return;
    }

    document.querySelectorAll('.lock-form').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            event.preventDefault();

            selectedLockForm = form;
            lockModal.classList.add('show');
            document.body.style.overflow = 'hidden';
        });
    });

    function closeLockModal() {
        lockModal.classList.remove('show');
        document.body.style.overflow = '';
        selectedLockForm = null;
    }

    if (btnClose) {
        btnClose.addEventListener('click', closeLockModal);
    }

    if (btnCancel) {
        btnCancel.addEventListener('click', closeLockModal);
    }

    btnConfirm.addEventListener('click', function () {
        if (selectedLockForm) {
            selectedLockForm.submit();
        }
    });

    lockModal.addEventListener('click', function (event) {
        if (event.target === lockModal) {
            closeLockModal();
        }
    });
}