/**
 * Admin User Management — UI logic (mock data until backend wired)
 * Doctor → Profile table | Patient → Patient table | Account → login credentials
 */
(function () {
    'use strict';

    const GENDER_LABEL = { false: 'Male', true: 'Female', '0': 'Male', '1': 'Female' };

    /** Chuẩn hóa Gender (BIT: 0=Nam, 1=Nữ) để lọc khớp DB */
    function normalizeGenderKey(gender) {
        if (gender === true || gender === 1 || gender === '1') return '1';
        if (gender === false || gender === 0 || gender === '0') return '0';
        return '';
    }

    function genderLabel(gender) {
        const key = normalizeGenderKey(gender);
        return key === '' ? '—' : (GENDER_LABEL[key] || '—');
    }

    /** @type {Array<object>} */
    let users = [];

    function fetchUsers() {
        // Obsolete: data is now rendered on the server side via Thymeleaf.
        // We do not fetch from the REST API anymore.
    }

    let currentPage = 1;
    const pageSize = 4;
    let activeTab = 'all';
    let searchQuery = '';
    let selectedUserId = null;
    let editingUserId = null;

    const els = {
        tableBody: document.getElementById('userTableBody'),
        pagination: document.getElementById('pagination'),
        globalSearch: document.getElementById('globalSearch'),
        filterTabs: document.querySelectorAll('.filter-tab'),
        drawerOverlay: document.getElementById('drawerOverlay'),
        detailDrawer: document.getElementById('detailDrawer'),
        modalOverlay: document.getElementById('userModalOverlay'),
        userForm: document.getElementById('userForm'),
        modalTitle: document.getElementById('modalTitle'),
        statTotal: document.getElementById('statTotal'),
        statPatients: document.getElementById('statPatients'),
        statDoctors: document.getElementById('statDoctors')
    };

    function roleLabel(role) {
        const map = { doctor: 'Doctor', patient: 'Patient', admin: 'Admin' };
        return map[role] || role;
    }

    function roleBadgeClass(role) {
        const map = { doctor: 'badge-doctor', patient: 'badge-patient', admin: 'badge-admin' };
        return map[role] || 'badge-patient';
    }

    function formatDate(iso) {
        if (!iso) return '—';
        const d = new Date(iso);
        return d.toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    // Table rendering and pagination are now handled by Thymeleaf on the server side.

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function findUser(id) {
        // Look up the row in the DOM and reconstruct the user object
        const row = document.querySelector(`tr[data-user-id="${id}"]`);
        if (!row) return null;
        return Object.assign({}, row.dataset);
    }

    function openDrawer(user) {
        selectedUserId = user.userId;
        // highlight row if needed (optional)

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
        doctorSec.forEach(function (el) {
            el.classList.toggle('visible', user.role === 'doctor');
        });
        patientSec.forEach(function (el) {
            el.classList.toggle('visible', user.role === 'patient');
        });

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
        if (el) el.textContent = value;
    }

    function closeDrawer() {
        els.drawerOverlay.classList.remove('open');
        els.detailDrawer.classList.remove('open');
    }

    function openModal(mode, user) {
        editingUserId = mode === 'edit' ? (user && user.userId) : null;
        els.modalTitle.textContent = mode === 'edit' ? 'Edit User' : 'Add New User';
        els.userForm.reset();
        resetRoleFields();

        const roleDoctor = document.getElementById('roleDoctor');
        const rolePatient = document.getElementById('rolePatient');
        const pwdGroup = document.getElementById('passwordGroup');

        if (mode === 'edit' && user) {
            document.getElementById('formUserId').value = user.userId;
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
            }
            document.querySelectorAll('input[name="userRoleType"]').forEach(function (r) {
                r.disabled = true;
            });
        } else {
            document.getElementById('formUserId').value = '';
            if (pwdGroup) pwdGroup.style.display = '';
            document.querySelectorAll('input[name="userRoleType"]').forEach(function (r) {
                r.disabled = false;
            });
            roleDoctor.checked = true;
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
        document.querySelectorAll('input[name="userRoleType"]').forEach(function (r) {
            r.disabled = false;
        });
    }

    function resetRoleFields() {
        document.getElementById('doctorFields').classList.remove('visible');
        document.getElementById('patientFields').classList.remove('visible');
    }

    function onRoleTypeChange() {
        const isDoctor = document.getElementById('roleDoctor').checked;
        document.getElementById('doctorFields').classList.toggle('visible', isDoctor);
        document.getElementById('patientFields').classList.toggle('visible', !isDoctor);
    }

    function toggleLock(userId) {
        // Status updates are now handled by form submit on the server side.
        // This function is kept for structural parity if needed, but not used.
    }

    function collectFormData() {
        const role = document.getElementById('roleDoctor').checked ? 'doctor' : 'patient';
        const data = {
            userId: document.getElementById('formUserId').value.trim() || ('USR-' + Date.now()),
            accountPhone: document.getElementById('formAccountPhone').value.trim(),
            password: document.getElementById('formPassword').value,
            role: role,
            status: 'active'
        };

        if (role === 'doctor') {
            data.fullName = document.getElementById('formFullName').value.trim();
            data.phoneNumber = document.getElementById('formContactPhone').value.trim();
            data.address = document.getElementById('formAddress').value.trim();
            data.dob = document.getElementById('formDob').value;
            data.gender = document.getElementById('formGender').value === '1';
            data.specialty = document.getElementById('formSpecialty').value.trim();
            data.roomName = document.getElementById('formRoom').value;
        } else {
            data.fullName = document.getElementById('formPatFullName').value.trim();
            data.phoneNumber = document.getElementById('formPatPhone').value.trim();
            data.address = document.getElementById('formPatAddress').value.trim();
            data.dob = document.getElementById('formPatDob').value;
            data.gender = document.getElementById('formPatGender').value === '1';
            data.height = parseInt(document.getElementById('formHeight').value, 10) || 0;
            data.weight = parseFloat(document.getElementById('formWeight').value) || 0;
            data.bloodgroup = document.getElementById('formBloodgroup').value;
            data.permanentMedicalHistory = document.getElementById('formHistory').value.trim();
            data.allergyNotes = document.getElementById('formAllergy').value.trim();
            data.supervisorName = document.getElementById('formSupervisorName').value.trim();
            data.supervisorPhone = document.getElementById('formSupervisorPhone').value.trim();
        }
        return data;
    }

    function saveUser(e) {
        // e.preventDefault(); // Removed to allow standard HTML form POST to backend
        const data = collectFormData();
        if (!data.fullName || !data.accountPhone) {
            alert('Please enter Full Name and Login Phone.');
            e.preventDefault();
            return;
        }
        if (!editingUserId && !data.password) {
            alert('Please enter a password for the new account.');
            e.preventDefault();
            return;
        }
        // Let the form submit normally
    }

    function bindRowActions() {
        document.querySelectorAll('[data-action]').forEach(function (btn) {
            btn.onclick = function () {
                const id = btn.getAttribute('data-id');
                const action = btn.getAttribute('data-action');
                const user = findUser(id);
                if (!user) return;
                if (action === 'view') openDrawer(user);
                else if (action === 'edit') openModal('edit', user);
                else if (action === 'toggle-lock') toggleLock(id);
            };
        });
    }

    function init() {
        bindRowActions();

        document.getElementById('btnAddUser').addEventListener('click', function () {
            openModal('add');
        });
        document.getElementById('btnCloseDrawer').addEventListener('click', closeDrawer);
        document.getElementById('drawerOverlay').addEventListener('click', closeDrawer);
        document.getElementById('btnCloseModal').addEventListener('click', closeModal);
        document.getElementById('btnCancelModal').addEventListener('click', closeModal);
        document.getElementById('userModalOverlay').addEventListener('click', function (e) {
            if (e.target === els.modalOverlay) closeModal();
        });
        els.userForm.addEventListener('submit', saveUser);

        document.querySelectorAll('input[name="userRoleType"]').forEach(function (r) {
            r.addEventListener('change', onRoleTypeChange);
        });

        if (els.globalSearch) {
            // Search is now handled by standard form submission
        }

        els.filterTabs.forEach(function (tab) {
            // Tabs are now links, no JS needed
        });

    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
