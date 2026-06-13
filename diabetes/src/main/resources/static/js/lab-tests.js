document.addEventListener('DOMContentLoaded', function () {
    initCreateModal();
    initEditModal();
    initViewDetailModal();
    initTableSearch();
    initPagination();
    initValidation();
});

let currentPage = 1;
const rowsPerPage = 8;

function openModal(modal) {
    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closeModal(modal) {
    modal.classList.remove('show');
    document.body.style.overflow = '';
}

function saveThreshold(testId, data) {
    localStorage.setItem('threshold_' + testId, JSON.stringify(data));
}

function getThreshold(testId) {
    const data = localStorage.getItem('threshold_' + testId);
    return data ? JSON.parse(data) : null;
}

function buildThresholdData(prefix) {
    return {
        young: {
            min: document.getElementById(prefix + 'YoungMin').value,
            max: document.getElementById(prefix + 'YoungMax').value
        },
        middle: {
            min: document.getElementById(prefix + 'MiddleMin').value,
            max: document.getElementById(prefix + 'MiddleMax').value
        },
        elder: {
            min: document.getElementById(prefix + 'ElderMin').value,
            max: document.getElementById(prefix + 'ElderMax').value
        },
        pregnant: {
            min: document.getElementById(prefix + 'PregnantMin').value,
            max: document.getElementById(prefix + 'PregnantMax').value
        }
    };
}

function fillThresholdInputs(prefix, data, fallbackMin, fallbackMax) {
    document.getElementById(prefix + 'YoungMin').value = data?.young?.min ?? fallbackMin;
    document.getElementById(prefix + 'YoungMax').value = data?.young?.max ?? fallbackMax;

    document.getElementById(prefix + 'MiddleMin').value = data?.middle?.min ?? fallbackMin;
    document.getElementById(prefix + 'MiddleMax').value = data?.middle?.max ?? fallbackMax;

    document.getElementById(prefix + 'ElderMin').value = data?.elder?.min ?? fallbackMin;
    document.getElementById(prefix + 'ElderMax').value = data?.elder?.max ?? fallbackMax;

    document.getElementById(prefix + 'PregnantMin').value = data?.pregnant?.min ?? fallbackMin;
    document.getElementById(prefix + 'PregnantMax').value = data?.pregnant?.max ?? fallbackMax;
}

function validateThresholdData(data) {
    const groups = ['young', 'middle', 'elder', 'pregnant'];

    for (const group of groups) {
        const min = Number(data[group].min);
        const max = Number(data[group].max);

        if (data[group].min === '' || data[group].max === '') {
            alert('Vui lòng nhập đầy đủ Min/Max cho tất cả nhóm bệnh nhân');
            return false;
        }

        if (min < 0 || max < 0) {
            alert('Min/Max không được nhỏ hơn 0');
            return false;
        }

        if (min > max) {
            alert('Min không được lớn hơn Max trong từng nhóm bệnh nhân');
            return false;
        }
    }

    return true;
}

function validateBaseForm(form) {
    const testName = form.querySelector('[name="testName"]');
    const unit = form.querySelector('[name="unit"]');
    const roomId = form.querySelector('[name="roomId"]');

    if (!testName || testName.value.trim() === '') {
        alert('Vui lòng nhập Test Name');
        return false;
    }

    if (testName.value.trim().length > 100) {
        alert('Test Name tối đa 100 ký tự');
        return false;
    }

    if (!unit || unit.value.trim() === '') {
        alert('Vui lòng nhập Unit');
        return false;
    }

    if (unit.value.trim().length > 20) {
        alert('Unit tối đa 20 ký tự');
        return false;
    }

    if (!roomId || roomId.value === '') {
        alert('Vui lòng chọn phòng xét nghiệm');
        return false;
    }

    return true;
}

function initCreateModal() {
    const openBtn = document.getElementById('btnDefineTest');
    const modal = document.getElementById('defineTestModal');
    const closeBtn = document.getElementById('btnCloseModal');
    const cancelBtn = document.getElementById('btnCancelModal');

    if (!openBtn || !modal) {
        return;
    }

    openBtn.addEventListener('click', function () {
        document.getElementById('createTestForm').reset();
        openModal(modal);
    });

    closeBtn?.addEventListener('click', function () {
        closeModal(modal);
    });

    cancelBtn?.addEventListener('click', function () {
        closeModal(modal);
    });

    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            closeModal(modal);
        }
    });
}

function initEditModal() {
    const modal = document.getElementById('editTestModal');
    const form = document.getElementById('editTestForm');
    const closeBtn = document.getElementById('btnCloseEditModal');
    const cancelBtn = document.getElementById('btnCancelEditModal');

    if (!modal || !form) {
        return;
    }

    document.querySelectorAll('.edit-btn').forEach(function (button) {
        button.addEventListener('click', function () {
            const id = button.dataset.id;
            const oldThreshold = getThreshold(id);

            document.getElementById('editLabTestId').value = id || '';
            document.getElementById('editTestName').value = button.dataset.name || '';
            document.getElementById('editUnit').value = button.dataset.unit || '';
            document.getElementById('editRoomId').value = button.dataset.room || '';
            document.getElementById('editDescription').value = button.dataset.description || '';

            fillThresholdInputs(
                'edit',
                oldThreshold,
                button.dataset.min || 0,
                button.dataset.max || 0
            );

            form.action = '/admin/lab-tests/update/' + id;

            openModal(modal);
        });
    });

    closeBtn?.addEventListener('click', function () {
        closeModal(modal);
    });

    cancelBtn?.addEventListener('click', function () {
        closeModal(modal);
    });

    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            closeModal(modal);
        }
    });
}

function initValidation() {
    const createForm = document.getElementById('createTestForm');
    const editForm = document.getElementById('editTestForm');

    if (createForm) {
        createForm.addEventListener('submit', function (event) {
            const data = buildThresholdData('create');

            if (!validateBaseForm(createForm) || !validateThresholdData(data)) {
                event.preventDefault();
                return;
            }

            document.getElementById('createMainMinValue').value = data.young.min;
            document.getElementById('createMainMaxValue').value = data.young.max;

            localStorage.setItem('pending_create_threshold', JSON.stringify(data));
        });
    }

    if (editForm) {
        editForm.addEventListener('submit', function (event) {
            const data = buildThresholdData('edit');
            const id = document.getElementById('editLabTestId').value;

            if (!validateBaseForm(editForm) || !validateThresholdData(data)) {
                event.preventDefault();
                return;
            }

            document.getElementById('editMainMinValue').value = data.young.min;
            document.getElementById('editMainMaxValue').value = data.young.max;

            saveThreshold(id, data);
        });
    }
}

function initViewDetailModal() {
    const modal = document.getElementById('viewDetailModal');
    const closeBtn = document.getElementById('btnCloseViewModal');

    if (!modal) {
        return;
    }

    document.querySelectorAll('.view-btn').forEach(function (button) {
        button.addEventListener('click', function () {
            const id = button.dataset.id;
            const statusText = button.dataset.status === 'true' ? 'Active' : 'Inactive';

            let data = getThreshold(id);

            if (!data) {
                data = {
                    young: {
                        min: button.dataset.min || 0,
                        max: button.dataset.max || 0
                    },
                    middle: {
                        min: button.dataset.min || 0,
                        max: button.dataset.max || 0
                    },
                    elder: {
                        min: button.dataset.min || 0,
                        max: button.dataset.max || 0
                    },
                    pregnant: {
                        min: button.dataset.min || 0,
                        max: button.dataset.max || 0
                    }
                };
            }

            document.getElementById('detailId').innerText = id || '---';
            document.getElementById('detailName').innerText = button.dataset.name || '---';
            document.getElementById('detailUnit').innerText = button.dataset.unit || '---';
            document.getElementById('detailRoom').innerText = button.dataset.room || '---';
            document.getElementById('detailStatus').innerText = statusText;
            document.getElementById('detailDescription').innerText =
                button.dataset.description || '---';

            document.getElementById('youngMin').innerText = data.young.min;
            document.getElementById('youngMax').innerText = data.young.max;

            document.getElementById('middleMin').innerText = data.middle.min;
            document.getElementById('middleMax').innerText = data.middle.max;

            document.getElementById('elderMin').innerText = data.elder.min;
            document.getElementById('elderMax').innerText = data.elder.max;

            document.getElementById('pregnantMin').innerText = data.pregnant.min;
            document.getElementById('pregnantMax').innerText = data.pregnant.max;

            openModal(modal);
        });
    });

    closeBtn?.addEventListener('click', function () {
        closeModal(modal);
    });

    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            closeModal(modal);
        }
    });
}

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

            row.dataset.filtered = rowText.includes(keyword)
                ? 'visible'
                : 'hidden';
        });

        currentPage = 1;
        renderPagination();
    });
}

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