document.addEventListener('DOMContentLoaded', function () {
    initTabs();
    initDefineTestModal();
    initDeleteConfirm();
});

/* TABS */

function initTabs() {
    const tabs = document.querySelectorAll('.tab');
    const contents = document.querySelectorAll('.tab-content');

    tabs.forEach(function (tab) {
        tab.addEventListener('click', function () {
            tabs.forEach(function (item) {
                item.classList.remove('active');
            });

            contents.forEach(function (content) {
                content.classList.remove('active');
            });

            tab.classList.add('active');

            const targetId = tab.dataset.tab;
            const target = document.getElementById(targetId);

            if (target) {
                target.classList.add('active');
            }
        });
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

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && modal.classList.contains('show')) {
            closeModal();
        }
    });
}

/* DELETE CONFIRM */

function initDeleteConfirm() {
    const deleteForms = document.querySelectorAll('form[action*="/delete/"]');

    deleteForms.forEach(function (form) {
        form.addEventListener('submit', function (event) {
            const confirmed = confirm('Bạn có chắc chắn muốn xóa xét nghiệm này không?');

            if (!confirmed) {
                event.preventDefault();
            }
        });
    });
}