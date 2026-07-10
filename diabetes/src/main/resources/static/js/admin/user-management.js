/**
 * Admin User Management — final fix, role never null or duplicate
 */
(function () {
    'use strict';

    const GENDER_LABEL = { false: 'Nam', true: 'Nữ', '0': 'Nam', '1': 'Nữ' };

    function normalizeGenderKey(gender) {
        if (gender === true || gender === 1 || gender === '1' || gender === 'true') return '1';
        if (gender === false || gender === 0 || gender === '0' || gender === 'false') return '0';
        return '';
    }

    function genderLabel(gender) {
        const key = normalizeGenderKey(gender);
        return key === '' ? '—' : (GENDER_LABEL[key] || '—');
    }

    let editingUserId = null;

    const els = {
        drawerOverlay: document.getElementById('drawerOverlay'),
        detailDrawer: document.getElementById('detailDrawer'),
        modalOverlay: document.getElementById('userModalOverlay'),
        userForm: document.getElementById('userForm'),
        modalTitle: document.getElementById('modalTitle'),
    };

    function roleLabel(role) {
        const map = { doctor: 'Bác sĩ', patient: 'Bệnh nhân', admin: 'Quản trị viên', DOC: 'Bác sĩ', PAT: 'Bệnh nhân' };
        return map[role] || role;
    }

    function formatDate(iso) {
        if (!iso) return '—';
        const d = new Date(iso);
        return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    }

    function findUser(id) {
        const row = document.querySelector(`tr[data-user-id="${id}"]`);
        if (!row) return null;
        return Object.assign({}, row.dataset);
    }

    function openDrawer(user) {
        document.getElementById('drawerName').textContent = user.fullName;
        const isClocked = user.status && user.status.toLowerCase() === 'clocked';
        document.getElementById('drawerMeta').textContent =
            roleLabel(user.role) + ' • ' + (isClocked ? 'Đang khóa' : 'Hoạt động');

        setDrawerRow('drawerUserId', user.userId);
        setDrawerRow('drawerAccountPhone', user.accountPhone);
        setDrawerRow('drawerFullName', user.fullName);
        setDrawerRow('drawerGender', genderLabel(user.gender));
        setDrawerRow('drawerDob', formatDate(user.dob));
        setDrawerRow('drawerAddress', user.address || '—');
        setDrawerRow('drawerContactPhone', user.phoneNumber || '—');
        setDrawerRow('drawerRole', roleLabel(user.role));
        setDrawerRow('drawerStatus', isClocked ? 'Đang khóa' : 'Hoạt động');

        const doctorSec = document.querySelectorAll('.doctor-only-section');
        const patientSec = document.querySelectorAll('.patient-only-section');
        const isDoc = user.role && (user.role.toLowerCase() === 'doctor' || user.role.toLowerCase() === 'doc');
        const isPat = user.role && (user.role.toLowerCase() === 'patient' || user.role.toLowerCase() === 'pat');
        doctorSec.forEach(el => el.classList.toggle('visible', isDoc));
        patientSec.forEach(el => el.classList.toggle('visible', isPat));

        if (isDoc) {
            setDrawerRow('drawerRoom', user.roomName || '—');
            setDrawerRow('drawerSpecialty', user.specialty || '—');
            setDrawerRow('drawerDoctorEmail', user.email || '—');
        }
        if (isPat) {
            setDrawerRow('drawerHeight', user.height ? user.height + ' cm' : '—');
            setDrawerRow('drawerWeight', user.weight != null ? user.weight + ' kg' : '—');
            setDrawerRow('drawerBloodgroup', user.bloodgroup || '—');
            setDrawerRow('drawerHistory', user.permanentMedicalHistory || '—');
            setDrawerRow('drawerAllergy', user.allergyNotes || '—');
            setDrawerRow('drawerSupervisor', user.supervisorName || '—');
            setDrawerRow('drawerSupervisorPhone', user.supervisorPhone || '—');
        }

        els.drawerOverlay.classList.add('open');
        els.detailDrawer.classList.add('open');
    }

    function setDrawerRow(id, value) {
        const el = document.getElementById(id);
        if (el) el.textContent = value || '—';
    }

    function closeDrawer() {
        els.drawerOverlay.classList.remove('open');
        els.detailDrawer.classList.remove('open');
    }

    function openModal(mode, user) {
        editingUserId = mode === 'edit' ? (user && user.userId) : null;
        els.modalTitle.textContent = mode === 'edit' ? 'Chỉnh sửa Bác sĩ' : 'Thêm Bác sĩ Mới';
        els.userForm.reset();
        clearErrors();

        const pwdGroup = document.getElementById('passwordGroup');
        const userIdInput = document.getElementById('formUserId');

        if (mode === 'edit' && user) {
            if (userIdInput) userIdInput.value = user.userId || '';
            document.getElementById('formAccountPhone').value = user.accountPhone || '';
            if (pwdGroup) pwdGroup.style.display = 'none';
            document.getElementById('formPassword').removeAttribute('required');

            const genderVal = normalizeGenderKey(user.gender) || '0';
            
            document.getElementById('formFullName').value = user.fullName || '';
            document.getElementById('formAddress').value = user.address || '';
            document.getElementById('formDob').value = user.dob || '';
            document.getElementById('formGender').value = genderVal;
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
        const invalidFields = els.userForm.querySelectorAll('.is-invalid');
        invalidFields.forEach(el => el.classList.remove('is-invalid'));
        const feedbacks = els.userForm.querySelectorAll('.error-feedback');
        feedbacks.forEach(el => {
            el.textContent = '';
        });
    }

    function closeModal() {
        els.modalOverlay.classList.remove('open');
        document.body.classList.remove('modal-open');
        editingUserId = null;
        clearErrors();
    }

    function init() {
        // Nếu có lỗi validation từ server, xác định xem là edit hay add từ dữ liệu ẩn
        const userIdInput = document.getElementById('formUserId');
        if (userIdInput && userIdInput.value) {
            editingUserId = userIdInput.value;
        }

        document.querySelectorAll('[data-action="view"]').forEach(btn => {
            btn.onclick = function () {
                const id = this.getAttribute('data-id');
                const user = findUser(id);
                if (user) openDrawer(user);
            };
        });
        document.querySelectorAll('[data-action="edit"]').forEach(btn => {
            btn.onclick = function () {
                const id = this.getAttribute('data-id');
                const user = findUser(id);
                if (user) openModal('edit', user);
            };
        });

        const btnAdd = document.getElementById('btnAddUser');
        if (btnAdd) btnAdd.addEventListener('click', () => openModal('add'));

        // If server-side validation produced errors (rendered into .error-feedback), re-apply UI
        (function applyServerErrors() {
            try {
                const feedbackSpans = document.querySelectorAll('.error-feedback span');
                let first = null;
                feedbackSpans.forEach(span => {
                    if (span && span.textContent && span.textContent.trim().length > 0) {
                        const grp = span.closest('.form-group');
                        if (grp) {
                            const input = grp.querySelector('input, select, textarea');
                            if (input) {
                                input.classList.add('is-invalid');
                                if (!first) first = input;
                            }
                        }
                    }
                });
                if (first) { first.scrollIntoView({ behavior: 'smooth', block: 'center' }); try { first.focus(); } catch (e) { } }
            } catch (e) { /* ignore */ }
        })();

        const closeDrawerBtn = document.getElementById('btnCloseDrawer');
        if (closeDrawerBtn) closeDrawerBtn.addEventListener('click', closeDrawer);
        if (els.drawerOverlay) els.drawerOverlay.addEventListener('click', closeDrawer);

        const closeModalBtn = document.getElementById('btnCloseModal');
        const cancelModalBtn = document.getElementById('btnCancelModal');
        if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
        if (cancelModalBtn) cancelModalBtn.addEventListener('click', closeModal);
        if (els.modalOverlay) els.modalOverlay.addEventListener('click', function (e) {
            if (e.target === els.modalOverlay) closeModal();
        });

        // Submit form validation logic
        if (els.userForm) {
            // Auto clear error when user edits
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

        // Backend search - submit form when clicking the search icon
        const toolbarForm = document.querySelector('.toolbar-search');
        if (toolbarForm) {
            const searchIcon = toolbarForm.querySelector('.fa-search');
            if (searchIcon) {
                searchIcon.style.cursor = 'pointer';
                searchIcon.addEventListener('click', function () {
                    toolbarForm.submit();
                });
            }
        }
    }

    // Flash toast auto-show and auto-hide (4s)
    (function setupFlash() {
        document.addEventListener('DOMContentLoaded', function () {
            const successToast = document.getElementById('flashMessage');
            const errorToast = document.getElementById('flashErrorMessage');

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

            handleToast(successToast, 'toastClose');
            handleToast(errorToast, 'toastErrorClose');
        });
    })();

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();