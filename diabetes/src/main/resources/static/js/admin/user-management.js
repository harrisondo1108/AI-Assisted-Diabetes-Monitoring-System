/**
 * Admin User Management — final fix, role never null or duplicate
 */
(function () {
    'use strict';

    const GENDER_LABEL = { false: 'Male', true: 'Female', '0': 'Male', '1': 'Female' };

    function normalizeGenderKey(gender) {
        if (gender === true || gender === 1 || gender === '1') return '1';
        if (gender === false || gender === 0 || gender === '0') return '0';
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
        const map = { doctor: 'Doctor', patient: 'Patient', admin: 'Admin', DO: 'Doctor', PAT: 'Patient' };
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
        document.getElementById('drawerMeta').textContent =
            roleLabel(user.role) + ' • ' + (user.status === 'locked' ? 'Locked' : 'Active');

        setDrawerRow('drawerUserId', user.userId);
        setDrawerRow('drawerAccountPhone', user.accountPhone);
        setDrawerRow('drawerFullName', user.fullName);
        setDrawerRow('drawerGender', genderLabel(user.gender));
        setDrawerRow('drawerDob', formatDate(user.dob));
        setDrawerRow('drawerAddress', user.address || '—');
        setDrawerRow('drawerContactPhone', user.phoneNumber || '—');
        setDrawerRow('drawerRole', roleLabel(user.role));
        setDrawerRow('drawerStatus', user.status === 'locked' ? 'Locked' : 'Active');

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

            const genderVal = normalizeGenderKey(user.gender) || '0';

            if (user.role === 'doctor') {
                roleDoctor.checked = true;
                document.getElementById('formFullName').value = user.fullName || '';
                document.getElementById('formContactPhone').value = user.phoneNumber || '';
                document.getElementById('formAddress').value = user.address || '';
                document.getElementById('formDob').value = user.dob || '';
                document.getElementById('formGender').value = genderVal;
                document.getElementById('formSpecialty').value = user.specialty || '';
                document.getElementById('formRoom').value = user.roomName || '';
                addHiddenRole('DO');
            } else {
                rolePatient.checked = true;
                document.getElementById('formPatFullName').value = user.fullName || '';
                document.getElementById('formPatPhone').value = user.phoneNumber || '';
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
    }

    function init() {
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
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();