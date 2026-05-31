/**
 * Admin User Management — final fix, role never null or duplicate
 */
(function () {
    'use strict';

    const GENDER_LABEL = { false: 'Male', true: 'Female', '0': 'Male', '1': 'Female' };

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
        const map = { doctor: 'Doctor', patient: 'Patient', admin: 'Admin', DOC: 'Doctor', PAT: 'Patient' };
        return map[role] || role;
    }

    function formatDate(iso) {
        if (!iso) return '—';
        const d = new Date(iso);
        return d.toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    function findUser(id) {
        const row = document.querySelector(`tr[data-user-id="${id}"]`);
        if (!row) return null;
        return Object.assign({}, row.dataset);
    }

    function openDrawer(user) {
        document.getElementById('drawerName').textContent = user.fullName;
        const isLocked = user.status && user.status.toLowerCase() === 'locked';
        document.getElementById('drawerMeta').textContent =
            roleLabel(user.role) + ' • ' + (isLocked ? 'Locked' : 'Active');

        setDrawerRow('drawerUserId', user.userId);
        setDrawerRow('drawerAccountPhone', user.accountPhone);
        setDrawerRow('drawerFullName', user.fullName);
        setDrawerRow('drawerGender', genderLabel(user.gender));
        setDrawerRow('drawerDob', formatDate(user.dob));
        setDrawerRow('drawerAddress', user.address || '—');
        setDrawerRow('drawerContactPhone', user.phoneNumber || '—');
        setDrawerRow('drawerRole', roleLabel(user.role));
        setDrawerRow('drawerStatus', isLocked ? 'Locked' : 'Active');

        const doctorSec = document.querySelectorAll('.doctor-only-section');
        const patientSec = document.querySelectorAll('.patient-only-section');
        doctorSec.forEach(el => el.classList.toggle('visible', user.role === 'doctor'));
        patientSec.forEach(el => el.classList.toggle('visible', user.role === 'patient'));

        if (user.role === 'doctor') {
            setDrawerRow('drawerRoom', user.roomName || '—');
            setDrawerRow('drawerSpecialty', user.specialty || '—');
        }
        if (user.role === 'patient') {
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

    function resetRoleFields() {
        document.getElementById('doctorFields').classList.remove('visible');
        document.getElementById('patientFields').classList.remove('visible');
    }

    function setFieldsDisabled(container, disabled) {
        const fields = container.querySelectorAll('input, select, textarea');
        fields.forEach(field => {
            if (disabled) {
                field.setAttribute('disabled', 'disabled');
            } else {
                field.removeAttribute('disabled');
            }
        });
    }

    function syncDisabledFieldsByRole() {
        const isDoctor = document.getElementById('roleDoctor').checked;
        const doctorContainer = document.getElementById('doctorFields');
        const patientContainer = document.getElementById('patientFields');
        setFieldsDisabled(doctorContainer, !isDoctor);
        setFieldsDisabled(patientContainer, isDoctor);
    }

    function onRoleTypeChange() {
        const isDoctor = document.getElementById('roleDoctor').checked;
        document.getElementById('doctorFields').classList.toggle('visible', isDoctor);
        document.getElementById('patientFields').classList.toggle('visible', !isDoctor);
        syncDisabledFieldsByRole();
    }

    function enableAllFields() {
        const doctorContainer = document.getElementById('doctorFields');
        const patientContainer = document.getElementById('patientFields');
        setFieldsDisabled(doctorContainer, false);
        setFieldsDisabled(patientContainer, false);
    }

    // Xóa hidden role cũ (nếu có)
    function removeHiddenRole() {
        const hidden = document.querySelector('#userForm input[name="role"][type="hidden"]');
        if (hidden) hidden.remove();
    }

    // Tạo hidden role với giá trị (chỉ dùng khi edit)
    function addHiddenRole(value) {
        removeHiddenRole();
        const hidden = document.createElement('input');
        hidden.type = 'hidden';
        hidden.name = 'role';
        hidden.value = value;
        document.getElementById('userForm').appendChild(hidden);
    }

    function openModal(mode, user) {
        editingUserId = mode === 'edit' ? (user && user.userId) : null;
        els.modalTitle.textContent = mode === 'edit' ? 'Edit User' : 'Add New User';
        els.userForm.reset();

        enableAllFields();
        resetRoleFields();
        removeHiddenRole(); // xóa hidden cũ

        const roleDoctor = document.getElementById('roleDoctor');
        const rolePatient = document.getElementById('rolePatient');
        const pwdGroup = document.getElementById('passwordGroup');
        const userIdInput = document.getElementById('formUserId');

        if (mode === 'edit' && user) {
            if (userIdInput) userIdInput.value = user.userId || '';
            document.getElementById('formAccountPhone').value = user.accountPhone || '';
            if (pwdGroup) pwdGroup.style.display = 'none';
            document.getElementById('formPassword').removeAttribute('required');

            const genderVal = normalizeGenderKey(user.gender) || '0';

            if (user.role === 'doctor') {
                roleDoctor.checked = true;
                document.getElementById('formFullName').value = user.fullName || '';
                const contactPhoneInput = document.getElementById('formContactPhone');
                if (contactPhoneInput) contactPhoneInput.value = user.phoneNumber || '';
                document.getElementById('formAddress').value = user.address || '';
                document.getElementById('formDob').value = user.dob || '';
                document.getElementById('formGender').value = genderVal;
                document.getElementById('formSpecialty').value = user.specialty || '';
                document.getElementById('formRoom').value = user.roomName || '';
                addHiddenRole('DOC');
            } else {
                rolePatient.checked = true;
                document.getElementById('formPatFullName').value = user.fullName || '';
                const patPhoneInput = document.getElementById('formPatPhone');
                if (patPhoneInput) patPhoneInput.value = user.phoneNumber || '';
                document.getElementById('formPatAddress').value = user.address || '';
                document.getElementById('formPatDob').value = user.dob || '';
                document.getElementById('formPatGender').value = genderVal;
                document.getElementById('formHeight').value = user.height || '';
                document.getElementById('formWeight').value = user.weight ?? '';
                document.getElementById('formBloodgroup').value = user.bloodgroup || '';
                document.getElementById('formHistory').value = user.permanentMedicalHistory || '';
                document.getElementById('formAllergy').value = user.allergyNotes || '';
                document.getElementById('formSupervisorName').value = user.supervisorName || '';
                document.getElementById('formSupervisorPhone').value = user.supervisorPhone || '';
                addHiddenRole('PAT');
            }
            // Disable radio để chúng không được gửi
            roleDoctor.disabled = true;
            rolePatient.disabled = true;
        } else {
            // Add mode: radio hoạt động bình thường, không có hidden
            if (userIdInput) userIdInput.value = '';
            if (pwdGroup) pwdGroup.style.display = '';
            document.getElementById('formPassword').setAttribute('required', 'required');
            roleDoctor.disabled = false;
            rolePatient.disabled = false;
            roleDoctor.checked = true; // mặc định doctor
            removeHiddenRole(); // chắc chắn không có hidden
        }

        onRoleTypeChange();
        els.modalOverlay.classList.add('open');
        document.body.classList.add('modal-open');
        const body = els.modalOverlay.querySelector('.modal-body');
        if (body) body.scrollTop = 0;
    }

    function showError(inputEl, message) {
        if (!inputEl) return;
        inputEl.classList.add('is-invalid');
        const feedback = inputEl.parentNode.querySelector('.error-feedback');
        if (feedback) {
            feedback.textContent = message;
        }
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

        const roleDoctor = document.getElementById('roleDoctor');
        const rolePatient = document.getElementById('rolePatient');
        if (roleDoctor) roleDoctor.disabled = false;
        if (rolePatient) rolePatient.disabled = false;

        removeHiddenRole();
        enableAllFields();
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

        const roleRadios = document.querySelectorAll('input[name="role"]');
        roleRadios.forEach(r => r.addEventListener('change', onRoleTypeChange));

        // Submit form validation
        if (els.userForm) {

            const nameRegex = /^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂÂÊÔƠƯưăâêôơư\s]+$/;
            const phoneRegex = /^(0[35789])[0-9]{8}$/;

            const show = (input, msg) => {
                showError(input, msg);
                return true;
            };

            const isFutureDate = (dateStr) => {
                return new Date(dateStr) >= new Date();
            };

            els.userForm.addEventListener('submit', function (e) {
                clearErrors();
                let hasError = false;

                const isEdit = !!editingUserId;
                const isDoctor = document.getElementById('roleDoctor').checked;

                // 1. Account Phone
                const phoneInput = document.getElementById('formAccountPhone');
                const phoneVal = phoneInput.value.trim();

                if (!phoneVal) {
                    hasError = show(phoneInput, 'Account phone number must not be empty');
                } else if (!phoneRegex.test(phoneVal)) {
                    hasError = show(phoneInput, 'Phone number must be a valid Vietnamese mobile number (10 digits, starting with 03, 05, 07, 08, 09)');
                }

                // 2. Password (only for create)
                if (!isEdit) {
                    const pwdInput = document.getElementById('formPassword');
                    const pwdVal = pwdInput.value;

                    if (!pwdVal) {
                        hasError = show(pwdInput, 'Password must not be empty');
                    } else if (pwdVal.length < 6) {
                        hasError = show(pwdInput, 'Password must be at least 6 characters');
                    }
                }

                // 3. Validation by role
                if (isDoctor) {

                    const nameInput = document.getElementById('formFullName');
                    const nameVal = nameInput.value.trim();

                    if (!nameVal) {
                        hasError = show(nameInput, 'Doctor full name must not be empty');
                    } else if (!nameRegex.test(nameVal)) {
                        hasError = show(nameInput, 'Full name must contain only letters and spaces');
                    } else if (nameVal.length > 60) {
                        hasError = show(nameInput, 'Full name must not exceed 60 characters');
                    }

                    const dobInput = document.getElementById('formDob');
                    const dobVal = dobInput.value;

                    if (!dobVal) {
                        hasError = show(dobInput, 'Please select date of birth');
                    } else if (isFutureDate(dobVal)) {
                        hasError = show(dobInput, 'Date of birth must be in the past');
                    }

                    const specialtyInput = document.getElementById('formSpecialty');
                    if (specialtyInput && specialtyInput.value.trim().length > 60) {
                        hasError = show(specialtyInput, 'Specialty must not exceed 60 characters');
                    }

                } else {

                    const nameInput = document.getElementById('formPatFullName');
                    const nameVal = nameInput.value.trim();

                    if (!nameVal) {
                        hasError = show(nameInput, 'Patient full name must not be empty');
                    } else if (!nameRegex.test(nameVal)) {
                        hasError = show(nameInput, 'Full name must contain only letters and spaces');
                    } else if (nameVal.length > 60) {
                        hasError = show(nameInput, 'Full name must not exceed 60 characters');
                    }

                    const dobInput = document.getElementById('formPatDob');
                    const dobVal = dobInput.value;

                    if (!dobVal) {
                        hasError = show(dobInput, 'Please select date of birth');
                    } else if (isFutureDate(dobVal)) {
                        hasError = show(dobInput, 'Date of birth must be in the past');
                    }

                    const addrInput = document.getElementById('formPatAddress');
                    const addrVal = addrInput.value.trim();

                    if (!addrVal) {
                        hasError = show(addrInput, 'Patient address must not be empty');
                    } else if (addrVal.length > 200) {
                        hasError = show(addrInput, 'Address must not exceed 200 characters');
                    }

                    const heightInput = document.getElementById('formHeight');
                    const heightVal = heightInput.value ? parseInt(heightInput.value, 10) : null;

                    if (heightVal !== null && !isNaN(heightVal)) {
                        if (heightVal < 30 || heightVal > 250) {
                            hasError = show(heightInput, 'Height must be between 30 cm and 250 cm');
                        }
                    }

                    const weightInput = document.getElementById('formWeight');
                    const weightVal = weightInput.value ? parseFloat(weightInput.value) : null;

                    if (weightVal !== null && !isNaN(weightVal)) {
                        if (weightVal < 5.0 || weightVal > 300.0) {
                            hasError = show(weightInput, 'Weight must be between 5.0 kg and 300.0 kg');
                        }
                    }

                    const svNameInput = document.getElementById('formSupervisorName');
                    const svNameVal = svNameInput.value.trim();

                    if (svNameVal) {
                        if (!nameRegex.test(svNameVal)) {
                            hasError = show(svNameInput, 'Guardian name must contain only letters and spaces');
                        } else if (svNameVal.length > 90) {
                            hasError = show(svNameInput, 'Guardian name must not exceed 90 characters');
                        }
                    }

                    const svPhoneInput = document.getElementById('formSupervisorPhone');
                    const svPhoneVal = svPhoneInput.value.trim();

                    if (svPhoneVal && !phoneRegex.test(svPhoneVal)) {
                        hasError = show(svPhoneInput, 'Guardian phone number is invalid');
                    }
                }

                // prevent submit
                if (hasError) {
                    e.preventDefault();

                    const firstError = els.userForm.querySelector('.is-invalid');
                    if (firstError) {
                        firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        firstError.focus();
                    }
                }
            });

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
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();