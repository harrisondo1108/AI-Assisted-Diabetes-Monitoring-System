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
    let pendingUserId = null;
    let pendingUserStatus = null;
    let pendingUserName = null;
    let resetValidationState = null;

    window.showConfirmModal = function(id, currentStatus, fullName) {
        const isActive = currentStatus === 'Active';
        const title = isActive ? 'Clock Account' : 'Unlock Account';
        const message = isActive
            ? 'Are you sure you want to clock "' + fullName + '"?'
            : 'Are you sure you want to unlock "' + fullName + '"?';
        const subMessage = isActive
            ? 'This user will be hidden from active lists and cannot perform actions.'
            : 'This user will become active again.';

        document.getElementById('confirmModalTitle').innerHTML = '<i class="fas fa-shield-alt" style="margin-right: 8px; color: #f59e0b;"></i> ' + title;
        document.getElementById('confirmMessage').innerHTML = '<i class="fas fa-user-shield" style="margin-right: 8px; color: #f59e0b;"></i> ' + message;
        document.getElementById('confirmSubMessage').innerText = subMessage;

        const iconElement = document.querySelector('#confirmModal .confirm-icon i');
        const iconDiv = document.querySelector('#confirmModal .confirm-icon');
        const okBtn = document.getElementById('okConfirmBtn');

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

        pendingUserId = id;
        pendingUserStatus = currentStatus;
        pendingUserName = fullName;

        document.getElementById('confirmModal').classList.add('open');
        document.body.classList.add('modal-open');
    };

    function closeConfirmModal() {
        document.getElementById('confirmModal').classList.remove('open');
        document.body.classList.remove('modal-open');
        setTimeout(function() {
            pendingUserId = null;
            pendingUserStatus = null;
            pendingUserName = null;
        }, 300);
    }

    function executeAction() {
        if (pendingUserId) {
            const url = '/admin/users/toggle-lock/' + pendingUserId;
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = url;
            document.body.appendChild(form);

            const okBtn = document.getElementById('okConfirmBtn');
            okBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
            okBtn.disabled = true;

            setTimeout(function() {
                form.submit();
            }, 500);
        }
        closeConfirmModal();
    }

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
        const isClocked = user.status && user.status.toLowerCase() === 'clocked';
        document.getElementById('drawerMeta').textContent =
            roleLabel(user.role) + ' • ' + (isClocked ? 'Clocked' : 'Active');

        setDrawerRow('drawerUserId', user.userId);
        setDrawerRow('drawerAccountPhone', user.accountPhone);
        setDrawerRow('drawerFullName', user.fullName);
        setDrawerRow('drawerGender', genderLabel(user.gender));
        setDrawerRow('drawerDob', formatDate(user.dob));
        setDrawerRow('drawerAddress', user.address || '—');
        setDrawerRow('drawerContactPhone', user.phoneNumber || '—');
        setDrawerRow('drawerRole', roleLabel(user.role));
        setDrawerRow('drawerStatus', isClocked ? 'Clocked' : 'Active');

        const doctorSec = document.querySelectorAll('.doctor-only-section');
        const patientSec = document.querySelectorAll('.patient-only-section');
        const isDoc = user.role && (user.role.toLowerCase() === 'doctor' || user.role.toLowerCase() === 'doc');
        const isPat = user.role && (user.role.toLowerCase() === 'patient' || user.role.toLowerCase() === 'pat');
        doctorSec.forEach(el => el.classList.toggle('visible', isDoc));
        patientSec.forEach(el => el.classList.toggle('visible', isPat));

        if (isDoc) {
            setDrawerRow('drawerRoom', user.roomName || '—');
            setDrawerRow('drawerSpecialty', user.specialty || '—');
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
        if (resetValidationState) resetValidationState();

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

            if (user.role && (user.role.toLowerCase() === 'doctor' || user.role.toLowerCase() === 'doc')) {
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

        // Confirm Modal Event Listeners
        const closeConfirmModalBtn = document.getElementById('closeConfirmModalBtn');
        if (closeConfirmModalBtn) closeConfirmModalBtn.onclick = closeConfirmModal;

        const cancelConfirmBtn = document.getElementById('cancelConfirmBtn');
        if (cancelConfirmBtn) cancelConfirmBtn.onclick = closeConfirmModal;

        const okConfirmBtn = document.getElementById('okConfirmBtn');
        if (okConfirmBtn) okConfirmBtn.onclick = executeAction;

        const confirmModal = document.getElementById('confirmModal');
        if (confirmModal) {
            confirmModal.onclick = function(e) { if (e.target === confirmModal) closeConfirmModal(); };
        }

        const roleRadios = document.querySelectorAll('input[name="role"]');
        roleRadios.forEach(r => r.addEventListener('change', onRoleTypeChange));

        // Submit form validation
        if (els.userForm) {

            const nameRegex = /^[\p{L}\s]+$/u;
            const phoneRegex = /^(0[35789])[0-9]{8}$/;

            const show = (input, msg) => {
                showError(input, msg);
                return true;
            };

            const isFutureDate = (dateStr) => {
                return new Date(dateStr) >= new Date();
            };

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

            // Live/inline validators: validate while typing and disable submit if any error
            (function attachLiveValidators() {
                const phoneRegex = /^(0[35789])[0-9]{8}$/;
                const nameRegex = /^[\p{L}\s]+$/u;

                // Only perform validation checks after the user starts interacting
                let phoneTouched = false;
                let debouncedRemoteCheck = null;
                const touched = {}; // track interactions per-field

                const phoneInput = document.getElementById('formAccountPhone');
                const pwdInput = document.getElementById('formPassword');
                const docName = document.getElementById('formFullName');
                const patName = document.getElementById('formPatFullName');
                const dobDoc = document.getElementById('formDob');
                const dobPat = document.getElementById('formPatDob');
                const submitBtn = els.userForm.querySelector('button[type="submit"]');

                const setSubmitState = () => {
                    if (!submitBtn) return;
                    submitBtn.disabled = !!els.userForm.querySelector('.is-invalid');
                };

                resetValidationState = function() {
                    phoneTouched = false;
                    for (const key in touched) {
                        delete touched[key];
                    }
                    clearErrors();
                    setSubmitState();
                };

                const validatePhone = () => {
                    if (!phoneInput) return false;
                    const v = (phoneInput.value || '').trim();
                    if (!v) {
                        if (phoneTouched) {
                            showError(phoneInput, 'Account phone number must not be empty');
                            return true;
                        }
                        return false;
                    }
                    if (!phoneRegex.test(v)) {
                        if (phoneTouched) {
                            showError(phoneInput, 'Phone number must be a valid Vietnamese mobile number (10 digits, starting with 03, 05, 07, 08, 09)');
                            return true;
                        }
                        return false;
                    }
                    // clear local format error then (only if user interacted) run remote uniqueness check
                    phoneInput.classList.remove('is-invalid');
                    const f = phoneInput.parentNode.querySelector('.error-feedback'); if (f) f.textContent = '';
                    if (phoneTouched && debouncedRemoteCheck) debouncedRemoteCheck(v);
                    return false;
                };

                const validatePassword = () => {
                    if (!pwdInput) return false;
                    const v = pwdInput.value || '';
                    const isEdit = !!editingUserId;
                    const isTouched = touched['formPassword'];
                    if (!isEdit) {
                        if (v.trim().length === 0) {
                            if (isTouched) {
                                showError(pwdInput, 'Password must not be empty');
                                return true;
                            }
                        } else if (v.length < 6) {
                            if (isTouched) {
                                showError(pwdInput, 'Password must be at least 6 characters');
                                return true;
                            }
                        }
                    } else {
                        // edit mode: only validate if provided (and only after interaction)
                        if (v && v.length > 0 && v.length < 6) {
                            if (isTouched) {
                                showError(pwdInput, 'Password must be at least 6 characters');
                                return true;
                            }
                        }
                    }
                    pwdInput.classList.remove('is-invalid');
                    const f = pwdInput.parentNode.querySelector('.error-feedback'); if (f) f.textContent = '';
                    return false;
                };

                const validateName = (inputEl, label) => {
                    if (!inputEl) return false;
                    const id = inputEl.id;
                    const v = (inputEl.value || '').trim();
                    const isTouched = touched[id];
                    if (!v) {
                        if (isTouched) {
                            showError(inputEl, `${label} must not be empty`);
                            return true;
                        }
                        return false;
                    }
                    if (!nameRegex.test(v)) {
                        if (isTouched) {
                            showError(inputEl, 'Full name must contain only letters and spaces');
                            return true;
                        }
                        return false;
                    }
                    if (v.length > 60) {
                        if (isTouched) {
                            showError(inputEl, 'Full name must not exceed 60 characters');
                            return true;
                        }
                        return false;
                    }

                    inputEl.classList.remove('is-invalid');
                    const f = inputEl.parentNode.querySelector('.error-feedback'); if (f) f.textContent = '';
                    return false;
                };

                const validateDob = (inputEl) => {
                    if (!inputEl) return false;
                    const id = inputEl.id;
                    const v = inputEl.value;
                    const isTouched = touched[id];
                    if (!v) {
                        if (isTouched) {
                            showError(inputEl, 'Please select date of birth');
                            return true;
                        }
                        return false;
                    }
                    if (new Date(v) >= new Date()) {
                        if (isTouched) {
                            showError(inputEl, 'Date of birth must be in the past');
                            return true;
                        }
                        return false;
                    }
                    inputEl.classList.remove('is-invalid');
                    const f = inputEl.parentNode.querySelector('.error-feedback'); if (f) f.textContent = '';
                    return false;
                };

                // Attach events
                if (phoneInput) {
                    phoneInput.addEventListener('keydown', () => { phoneTouched = true; touched['formAccountPhone'] = true; });
                    phoneInput.addEventListener('mousedown', () => { phoneTouched = true; touched['formAccountPhone'] = true; });
                    phoneInput.addEventListener('input', (e) => { validatePhone(); setSubmitState(); });
                    phoneInput.addEventListener('blur', () => { validatePhone(); setSubmitState(); });
                }
                if (pwdInput) {
                    pwdInput.addEventListener('keydown', () => { touched['formPassword'] = true; });
                    pwdInput.addEventListener('mousedown', () => { touched['formPassword'] = true; });
                    pwdInput.addEventListener('input', (e) => { validatePassword(); setSubmitState(); });
                    pwdInput.addEventListener('blur', () => { validatePassword(); setSubmitState(); });
                }
                if (docName) {
                    docName.addEventListener('keydown', () => { touched['formFullName'] = true; });
                    docName.addEventListener('mousedown', () => { touched['formFullName'] = true; });
                    docName.addEventListener('input', (e) => { validateName(docName, 'Doctor full name'); setSubmitState(); });
                    docName.addEventListener('blur', () => { validateName(docName, 'Doctor full name'); setSubmitState(); });
                }
                if (patName) {
                    patName.addEventListener('keydown', () => { touched['formPatFullName'] = true; });
                    patName.addEventListener('mousedown', () => { touched['formPatFullName'] = true; });
                    patName.addEventListener('input', (e) => { validateName(patName, 'Patient full name'); setSubmitState(); });
                    patName.addEventListener('blur', () => { validateName(patName, 'Patient full name'); setSubmitState(); });
                }
                if (dobDoc) {
                    dobDoc.addEventListener('keydown', () => { touched['formDob'] = true; });
                    dobDoc.addEventListener('mousedown', () => { touched['formDob'] = true; });
                    dobDoc.addEventListener('change', (e) => { touched['formDob'] = true; validateDob(dobDoc); setSubmitState(); });
                    dobDoc.addEventListener('blur', () => { validateDob(dobDoc); setSubmitState(); });
                }
                if (dobPat) {
                    dobPat.addEventListener('keydown', () => { touched['formPatDob'] = true; });
                    dobPat.addEventListener('mousedown', () => { touched['formPatDob'] = true; });
                    dobPat.addEventListener('change', (e) => { touched['formPatDob'] = true; validateDob(dobPat); setSubmitState(); });
                    dobPat.addEventListener('blur', () => { validateDob(dobPat); setSubmitState(); });
                }

                // Submit validation check
                els.userForm.addEventListener('submit', function (e) {
                    phoneTouched = true;
                    touched['formAccountPhone'] = true;
                    touched['formPassword'] = true;
                    touched['formFullName'] = true;
                    touched['formPatFullName'] = true;
                    touched['formDob'] = true;
                    touched['formPatDob'] = true;

                    const hasPhoneError = validatePhone();
                    const hasPwdError = validatePassword();
                    const isDocRole = document.getElementById('roleDoctor').checked;
                    let hasNameError = false;
                    let hasDobError = false;

                    if (isDocRole) {
                        hasNameError = validateName(docName, 'Doctor full name');
                        hasDobError = validateDob(dobDoc);
                    } else {
                        hasNameError = validateName(patName, 'Patient full name');
                        hasDobError = validateDob(dobPat);
                    }

                    if (hasPhoneError || hasPwdError || hasNameError || hasDobError || els.userForm.querySelector('.is-invalid')) {
                        e.preventDefault();
                        setSubmitState();
                        const firstError = els.userForm.querySelector('.is-invalid');
                        if (firstError) {
                            firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                            try { firstError.focus(); } catch (err) { /* ignore */ }
                        }
                    }
                });

                // Remote uniqueness helper and initial checks
                let phoneAvailable = true;
                const debounce = (fn, wait) => {
                    let t;
                    return function (...args) {
                        clearTimeout(t);
                        t = setTimeout(() => fn.apply(this, args), wait);
                    };
                };

                const remoteCheck = async (val) => {
                    if (!phoneInput) return;
                    if (!val) { phoneAvailable = true; setSubmitState(); return; }
                    try {
                        const url = '/admin/users/check-phone?phone=' + encodeURIComponent(val) + (editingUserId ? '&userId=' + encodeURIComponent(editingUserId) : '');
                        const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
                        if (!res.ok) { phoneAvailable = true; setSubmitState(); return; }
                        const data = await res.json();
                        
                        // Prevent race condition: if the user changed the input value since this request was sent, ignore the result.
                        if (phoneInput.value.trim() !== val) {
                            return;
                        }

                        if (!data.available) {
                            showError(phoneInput, 'Phone Number is already in use by another account');
                            phoneAvailable = false;
                        } else {
                            phoneAvailable = true;
                            const f2 = phoneInput.parentNode.querySelector('.error-feedback'); if (f2) f2.textContent = '';
                            phoneInput.classList.remove('is-invalid');
                        }
                    } catch (e) {
                        phoneAvailable = true;
                    }
                    setSubmitState();
                };

                debouncedRemoteCheck = debounce(remoteCheck, 350);
            })();
        }

        // Live search (debounced) - update table via AJAX JSON endpoint
        const searchInput = document.getElementById('globalSearch');
        const toolbarForm = document.querySelector('.toolbar-search');
        if (toolbarForm) {
            toolbarForm.addEventListener('submit', function (e) { e.preventDefault(); });
        }

        if (searchInput) {
            const debounce = (fn, wait) => {
                let t;
                return function (...args) {
                    clearTimeout(t);
                    t = setTimeout(() => fn.apply(this, args), wait);
                };
            };

            const fetchAndRender = async (page = 0) => {
                try {
                    const roleInput = document.querySelector('.toolbar-search input[name="role"]');
                    const role = roleInput ? roleInput.value || 'all' : 'all';
                    const search = (document.getElementById('globalSearch').value || '').trim();
                    const size = parseInt(document.getElementById('pagination')?.dataset.pageSize || '8', 10) || 8;
                    const url = `/admin/users/list?role=${encodeURIComponent(role)}&search=${encodeURIComponent(search)}&page=${page}&size=${size}`;
                    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
                    if (!res.ok) return;
                    const data = await res.json();
                    renderUsers(data.content || []);
                    const container = document.getElementById('pagination');
                    if (container) {
                        container.dataset.currentPage = data.currentPage;
                        container.dataset.totalPages = data.totalPages;
                        container.dataset.pageSize = data.pageSize;
                        container.dataset.search = role === 'all' ? (document.getElementById('globalSearch').value || '') : (document.getElementById('globalSearch').value || '');
                        renderPagination();
                    }
                } catch (e) {
                    console.error('Live search failed', e);
                }
            };

            const debouncedFetch = debounce(() => fetchAndRender(0), 300);
            searchInput.addEventListener('input', debouncedFetch);
        }
    }

    function renderUsers(list) {
        const tbody = document.getElementById('userTableBody');
        if (!tbody) return;
        const rows = list.map(user => {
            const genderText = (user.gender == null) ? 'Empty' : (user.gender ? 'Female' : 'Male');
            const roleText = user.role ? (user.role.charAt(0).toUpperCase() + user.role.slice(1)) : '—';
            const statusClass = user.status === 'Active' ? 'active' : 'clocked';
            return `
            <tr data-user-id="${user.userId || ''}"
                data-full-name="${escapeHtml(user.fullName || '')}"
                data-account-phone="${escapeHtml(user.accountPhone || '')}"
                data-role="${escapeHtml(user.role || '')}"
                data-gender="${user.gender}"
                data-status="${escapeHtml(user.status || '')}"
                data-dob="${escapeHtml(user.dob || '')}"
                data-address="${escapeHtml(user.address || '')}"
                data-specialty="${escapeHtml(user.specialty || '')}"
                data-room-name="${escapeHtml(user.roomName || '')}"
                data-height="${user.height || ''}"
                data-weight="${user.weight || ''}"
                data-bloodgroup="${escapeHtml(user.bloodgroup || '')}"
                data-permanent-medical-history="${escapeHtml(user.permanentMedicalHistory || '')}"
                data-allergy-notes="${escapeHtml(user.allergyNotes || '')}"
                data-supervisor-name="${escapeHtml(user.supervisorName || '')}"
                data-supervisor-phone="${escapeHtml(user.supervisorPhone || '')}">
                <td>
                    <div class="user-cell">
                        <div class="user-name">${escapeHtml(user.fullName || '')}</div>
                        <div class="user-phone">${escapeHtml(user.accountPhone || '')}</div>
                    </div>
                </td>
                <td><span class="badge">${escapeHtml(roleText)}</span></td>
                <td>${escapeHtml(genderText)}</td>
                <td><span class="status-badge ${statusClass}">${escapeHtml(user.status === 'Active' ? 'Active' : 'Clocked')}</span></td>
                <td>
                    <div class="action-group">
                        <button type="button" class="action-btn view" title="View details" data-action="view" data-id="${user.userId || ''}"><i class="fas fa-eye"></i></button>
                        ${(user.role === 'patient' || user.role === 'PAT') ? '' : `<button type="button" class="action-btn edit" title="Edit" data-action="edit" data-id="${user.userId || ''}"><i class="fas fa-pen"></i></button>`}
                        <button type="button" class="action-btn ${user.status === 'Active' ? 'lock' : 'unlock'}" 
                            data-id="${user.userId || ''}" 
                            data-status="${user.status || ''}" 
                            data-name="${escapeHtml(user.fullName || '')}" 
                            onclick="showConfirmModal(this.getAttribute('data-id'), this.getAttribute('data-status'), this.getAttribute('data-name'))" 
                            title="${user.status === 'Active' ? 'Clock account' : 'Unlock account'}">
                            <i class="fas ${user.status === 'Active' ? 'fa-lock' : 'fa-lock-open'}"></i>
                        </button>
                    </div>
                </td>
            </tr>`;
        }).join('\n');
        tbody.innerHTML = rows;
        // rebind actions
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
    }

    function escapeHtml(str) {
        return String(str).replace(/[&<>"'`]/g, function (s) {
            return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;', '`': '&#96;' })[s];
        });
    }

    // Re-use existing renderer if present; otherwise, provide a minimal one
    function renderPagination() {
        const container = document.getElementById('pagination');
        if (!container) return;
        const total = parseInt(container.dataset.totalPages || '0', 10);
        const current = parseInt(container.dataset.currentPage || '0', 10);
        if (isNaN(total) || total <= 1) {
            container.innerHTML = '';
            return;
        }
        const range = 2;
        const buildPageUrl = (page) => {
            const role = document.querySelector('.toolbar-search input[name="role"]')?.value || 'all';
            const search = document.getElementById('globalSearch')?.value || '';
            const params = new URLSearchParams();
            if (role && role !== 'all') params.set('role', role);
            if (search) params.set('search', search);
            params.set('page', page);
            return '/admin/users?' + params.toString();
        };
        let html = '';
        if (current > 0) html += `<a href="${buildPageUrl(current - 1)}" class="page-link prev">« Prev</a>`;
        else html += `<span class="page-link prev disabled">« Prev</span>`;
        if (0 < current - range) {
            html += `<a href="${buildPageUrl(0)}" class="page-num">1</a>`;
            if (1 < current - range) html += `<span class="ellipsis">…</span>`;
        }
        const start = Math.max(0, current - range);
        const end = Math.min(total - 1, current + range);
        for (let i = start; i <= end; i++) {
            const cls = i === current ? 'page-num active' : 'page-num';
            html += `<a href="${buildPageUrl(i)}" class="${cls}" data-page="${i}">${i + 1}</a>`;
        }
        if (current + range < total - 1) {
            if (current + range + 1 < total - 1) html += `<span class="ellipsis">…</span>`;
            html += `<a href="${buildPageUrl(total - 1)}" class="page-num">${total}</a>`;
        }
        if (current + 1 < total) html += `<a href="${buildPageUrl(current + 1)}" class="page-link next">Next »</a>`;
        else html += `<span class="page-link next disabled">Next »</span>`;
        container.innerHTML = html;
        container.querySelectorAll('a').forEach(a => {
            a.addEventListener('click', function (e) {
                e.preventDefault();
                const href = this.getAttribute('href');
                window.scrollTo({ top: 0, behavior: 'smooth' });
                setTimeout(() => { window.location.href = href; }, 120);
            });
        });
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