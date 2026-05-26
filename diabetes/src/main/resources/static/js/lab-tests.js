document.addEventListener('DOMContentLoaded', function () {
    initIcons();
    initTabs();
    initModalCloseByBackground();
    initDeleteConfirm();
    initSearchSubmit();
    initExportButton();
});

/* ICON */
function initIcons() {
    if (window.lucide) {
        lucide.createIcons();
    }
}

/* MODAL */
function openModal(id) {
    const modal = document.getElementById(id);

    if (!modal) return;

    modal.classList.add('show');

    if (window.lucide) {
        lucide.createIcons();
    }
}

function closeModal(id) {
    const modal = document.getElementById(id);

    if (!modal) return;

    modal.classList.remove('show');
}

function initModalCloseByBackground() {
    const modals = document.querySelectorAll('.modal');

    modals.forEach(function (modal) {
        modal.addEventListener('click', function (event) {
            if (event.target === modal) {
                modal.classList.remove('show');
            }
        });
    });
}

/* TAB */
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    tabButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const tabId = button.dataset.tab;

            tabButtons.forEach(function (btn) {
                btn.classList.remove('tab-active');
                btn.classList.add('text-gray-700', 'border-transparent');
            });

            button.classList.add('tab-active');
            button.classList.remove('text-gray-700', 'border-transparent');

            tabContents.forEach(function (content) {
                content.classList.remove('active');

                if (content.id === tabId) {
                    content.classList.add('active');
                }
            });
        });
    });
}

/* DETAIL MODAL */
function openDetailFromButton(button) {
    const name = button.dataset.name || '';
    const unit = button.dataset.unit || '';
    const min = button.dataset.min || '';
    const max = button.dataset.max || '';
    const description = button.dataset.description || '';

    showToast(`
        <b>${name}</b><br>
        Unit: ${unit}<br>
        Min: ${min}<br>
        Max: ${max}<br>
        ${description}
    `);
}

/* EDIT MODAL */
function openEditFromButton(button) {
    const id = button.dataset.id;

    showToast('Mở chức năng sửa xét nghiệm ID: ' + id);

    // Nếu sau này bạn có modal edit thì thay đoạn này bằng fill dữ liệu vào modal
    // Ví dụ:
    // document.getElementById('editName').value = button.dataset.name;
    // openModal('editModal');
}

/* DELETE CONFIRM */
function initDeleteConfirm() {
    document.querySelectorAll('.delete-form').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            const confirmed = confirm('Bạn có chắc chắn muốn xóa xét nghiệm này không?');

            if (!confirmed) {
                event.preventDefault();
            }
        });
    });
}

/* SEARCH */
function initSearchSubmit() {
    const searchInput = document.querySelector('input[name="query"]');

    if (!searchInput) return;

    searchInput.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
            showToast('Đang tìm kiếm: ' + searchInput.value);
        }
    });
}

/* EXPORT */
function initExportButton() {
    const exportButton = document.querySelector('a[href*="/export"]');

    if (!exportButton) return;

    exportButton.addEventListener('click', function () {
        showToast('Đang xuất dữ liệu CSV...');
    });
}

/* TOAST */
function showToast(message) {
    let container = document.getElementById('toast-container');

    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.style.position = 'fixed';
        container.style.right = '24px';
        container.style.bottom = '24px';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');

    toast.className = `
        bg-green-900
        text-white
        px-5
        py-4
        rounded-xl
        shadow-lg
        mb-3
        text-sm
        max-w-sm
        animate-toast
    `;

    toast.innerHTML = message;

    container.appendChild(toast);

    setTimeout(function () {
        toast.remove();
    }, 3000);
}