/**
 * Admin User Management — Minimal JS (Backend & Thymeleaf First)
 */
(function () {
    'use strict';

    let editingUserId = null;

    const els = {
        drawerOverlay: document.getElementById('drawerOverlay'),
        detailDrawer: document.getElementById('detailDrawer'),
        modalOverlay: document.getElementById('userModalOverlay'),
        userForm: document.getElementById('userForm'),
        modalTitle: document.getElementById('modalTitle'),
    };

    function findUser(id) {
        const row = document.querySelector(`tr[data-user-id="${id}"]`);
        return row ? Object.assign({}, row.dataset) : null;
    }

    function setDrawerRow(id, value) {
        const el = document.getElementById(id);
        if (el) el.textContent = value || '—';
    }

    function openDrawer(user) {
        setDrawerRow('drawerUserId', user.userId);
        setDrawerRow('drawerAccountPhone', user.accountPhone);
        setDrawerRow('drawerFullName', user.fullName);
        setDrawerRow('drawerGender', user.genderLabel);
        setDrawerRow('drawerDob', user.dobFormatted);
        setDrawerRow('drawerAddress', user.address || '—');
        setDrawerRow('drawerContactPhone', user.accountPhone || '—');
        setDrawerRow('drawerRole', user.roleLabel);
        setDrawerRow('drawerStatus', user.statusLabel);

        const isDoc = user.isDoc === 'true';
        const isPat = user.isPat === 'true';

        document.querySelectorAll('.doctor-only-section').forEach(el => el.classList.toggle('visible', isDoc));
        document.querySelectorAll('.patient-only-section').forEach(el => el.classList.toggle('visible', isPat));

        if (isDoc) {
            setDrawerRow('drawerRoom', user.roomName || '—');
            setDrawerRow('drawerSpecialty', user.specialty || '—');
            setDrawerRow('drawerDoctorEmail', user.email || '—');
        }
        if (isPat) {
            setDrawerRow('drawerHeight', user.heightLabel);
            setDrawerRow('drawerWeight', user.weightLabel);
            setDrawerRow('drawerBloodgroup', user.bloodgroup || '—');
            setDrawerRow('drawerHistory', user.permanentMedicalHistory || '—');
            setDrawerRow('drawerAllergy', user.allergyNotes || '—');
            setDrawerRow('drawerSupervisor', user.supervisorName || '—');
            setDrawerRow('drawerSupervisorPhone', user.supervisorPhone || '—');
        }

        els.drawerOverlay.classList.add('open');
        els.detailDrawer.classList.add('open');
    }

    function closeDrawer() {
        els.drawerOverlay.classList.remove('open');
        els.detailDrawer.classList.remove('open');
    }

    function openModal(mode, user) {
        editingUserId = mode === 'edit' ? (user && user.userId) : null;
        if (els.modalTitle) els.modalTitle.textContent = mode === 'edit' ? 'Chỉnh sửa Bác sĩ' : 'Thêm Bác sĩ Mới';
        els.userForm.reset();
        clearErrors();

        const pwdGroup = document.getElementById('passwordGroup');
        const userIdInput = document.getElementById('formUserId');

        if (mode === 'edit' && user) {
            if (userIdInput) userIdInput.value = user.userId || '';
            document.getElementById('formAccountPhone').value = user.accountPhone || '';
            if (pwdGroup) pwdGroup.style.display = 'none';
            document.getElementById('formPassword').removeAttribute('required');

            document.getElementById('formFullName').value = user.fullName || '';
            document.getElementById('formAddress').value = user.address || '';
            document.getElementById('formDob').value = user.dob || '';
            document.getElementById('formGender').value = user.genderVal || '0';
            document.getElementById('formSpecialty').value = user.specialty || '';
            document.getElementById('formDoctorEmail').value = user.email || '';
            document.getElementById('formRoom').value = user.roomName || '';
        } else {
            if (userIdInput) userIdInput.value = '';
            if (pwdGroup) pwdGroup.style.display = '';
            document.getElementById('formPassword').setAttribute('required', 'required');
        }

        els.modalOverlay.classList.add('open');
        document.body.classList.add('modal-open');
        const body = els.modalOverlay.querySelector('.modal-body');
        if (body) body.scrollTop = 0;
    }

    function clearErrors() {
        if (!els.userForm) return;
        els.userForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        els.userForm.querySelectorAll('.error-feedback').forEach(el => { el.textContent = ''; });
    }

    function closeModal() {
        els.modalOverlay.classList.remove('open');
        document.body.classList.remove('modal-open');
        editingUserId = null;
        clearErrors();
    }

    function init() {
        document.querySelectorAll('[data-action="view"]').forEach(btn => {
            btn.onclick = function () {
                const user = findUser(this.getAttribute('data-id'));
                if (user) openDrawer(user);
            };
        });

        document.querySelectorAll('[data-action="edit"]').forEach(btn => {
            btn.onclick = function () {
                const user = findUser(this.getAttribute('data-id'));
                if (user) openModal('edit', user);
            };
        });

        const btnAdd = document.getElementById('btnAddUser');
        if (btnAdd) btnAdd.addEventListener('click', () => openModal('add'));

        const closeDrawerBtn = document.getElementById('btnCloseDrawer');
        if (closeDrawerBtn) closeDrawerBtn.addEventListener('click', closeDrawer);
        if (els.drawerOverlay) els.drawerOverlay.addEventListener('click', closeDrawer);

        const closeModalBtn = document.getElementById('btnCloseModal');
        const cancelModalBtn = document.getElementById('btnCancelModal');
        if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
        if (cancelModalBtn) cancelModalBtn.addEventListener('click', closeModal);
        if (els.modalOverlay) els.modalOverlay.addEventListener('click', e => {
            if (e.target === els.modalOverlay) closeModal();
        });

        // Xóa class lỗi khi người dùng gõ
        if (els.userForm) {
            els.userForm.querySelectorAll('input, select, textarea').forEach(field => {
                const clear = function () {
                    this.classList.remove('is-invalid');
                    const feedback = this.parentNode.querySelector('.error-feedback');
                    if (feedback) feedback.textContent = '';
                };
                field.addEventListener('input', clear);
                field.addEventListener('change', clear);
            });
        }
    }

    // Tự động đóng Toast thông báo flash sau 4s
    document.addEventListener('DOMContentLoaded', function () {
        const handleToast = (toast, closeBtnId) => {
            if (!toast) return;
            setTimeout(() => toast.classList.add('visible'), 50);
            const hide = () => {
                toast.classList.remove('visible');
                setTimeout(() => { if (toast && toast.parentNode) toast.parentNode.removeChild(toast); }, 220);
            };
            const timer = setTimeout(hide, 4000);
            const btn = document.getElementById(closeBtnId);
            if (btn) btn.addEventListener('click', () => { clearTimeout(timer); hide(); });
        };

        handleToast(document.getElementById('flashMessage'), 'toastClose');
        handleToast(document.getElementById('flashErrorMessage'), 'toastErrorClose');
    });

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();