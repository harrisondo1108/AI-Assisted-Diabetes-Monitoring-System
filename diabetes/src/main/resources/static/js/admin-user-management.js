/**
 * Admin User Management — UI logic (mock data until backend wired)
 * Doctor → Profile table | Patient → Patient table | Account → login credentials
 */
(function () {
    'use strict';

    const GENDER_LABEL = { false: 'Nam', true: 'Nữ', '0': 'Nam', '1': 'Nữ' };

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
    let users = [
        {
            userId: 'USR-001-DOC',
            accountPhone: '+84901234567',
            role: 'doctor',
            status: 'active',
            fullName: 'BS. Nguyễn Văn An',
            phoneNumber: '0901234567',
            address: '12 Lê Lợi, Q.1, TP.HCM',
            dob: '1985-03-15',
            gender: false,
            roomName: 'Phòng khám A',
            specialty: 'Nội tiết'
        },
        {
            userId: 'USR-002-PAT',
            accountPhone: '+84987654321',
            role: 'patient',
            status: 'active',
            fullName: 'Trần Thị Bình',
            phoneNumber: '0987654321',
            address: '45 Nguyễn Huệ, Q.1, TP.HCM',
            dob: '1975-09-22',
            gender: true,
            height: 160,
            weight: 62.5,
            bloodgroup: 'O+',
            permanentMedicalHistory: 'Tiểu đường type 2 từ 2018',
            allergyNotes: 'Không dị ứng penicillin',
            supervisorName: 'Nguyễn Văn Cường',
            supervisorPhone: '0911222333'
        },
        {
            userId: 'USR-003-DOC',
            accountPhone: '+84911222333',
            role: 'doctor',
            status: 'locked',
            fullName: 'BS. Lê Minh Châu',
            phoneNumber: '0911222333',
            address: '88 Pasteur, Q.3, TP.HCM',
            dob: '1990-07-08',
            gender: false,
            roomName: 'Phòng khám B',
            specialty: 'Lâm sàng'
        },
        {
            userId: 'USR-004-PAT',
            accountPhone: '+84955443322',
            role: 'patient',
            status: 'locked',
            fullName: 'Phạm Đức Dũng',
            phoneNumber: '0955443322',
            address: '10 Võ Văn Tần, Q.3, TP.HCM',
            dob: '1968-12-01',
            gender: false,
            height: 172,
            weight: 78.0,
            bloodgroup: 'A+',
            permanentMedicalHistory: 'Tăng huyết áp',
            allergyNotes: '',
            supervisorName: 'Trần Thị Lan',
            supervisorPhone: '0933445566'
        }
    ];

    let currentPage = 1;
    const pageSize = 4;
    let activeTab = 'all';
    let searchQuery = '';
    let filterRole = '';
    let filterGender = '';
    let selectedUserId = null;
    let editingUserId = null;

    const els = {
        tableBody: document.getElementById('userTableBody'),
        tableInfo: document.getElementById('tableInfo'),
        pagination: document.getElementById('pagination'),
        globalSearch: document.getElementById('globalSearch'),
        filterTabs: document.querySelectorAll('.filter-tab'),
        filterRole: document.getElementById('filterRole'),
        filterGender: document.getElementById('filterGender'),
        drawerOverlay: document.getElementById('drawerOverlay'),
        detailDrawer: document.getElementById('detailDrawer'),
        modalOverlay: document.getElementById('userModalOverlay'),
        userForm: document.getElementById('userForm'),
        modalTitle: document.getElementById('modalTitle'),
        statTotal: document.getElementById('statTotal'),
        statPatients: document.getElementById('statPatients'),
        statDoctors: document.getElementById('statDoctors'),
        statAdmins: document.getElementById('statAdmins')
    };

    function roleLabel(role) {
        const map = { doctor: 'Bác sĩ', patient: 'Bệnh nhân', admin: 'Quản trị' };
        return map[role] || role;
    }

    function roleBadgeClass(role) {
        const map = { doctor: 'badge-doctor', patient: 'badge-patient', admin: 'badge-admin' };
        return map[role] || 'badge-patient';
    }

    function formatDate(iso) {
        if (!iso) return '—';
        const d = new Date(iso);
        return d.toLocaleDateString('vi-VN', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    function getFilteredUsers() {
        return users.filter(function (u) {
            if (activeTab === 'patient' && u.role !== 'patient') return false;
            if (activeTab === 'doctor' && u.role !== 'doctor') return false;
            if (filterRole && u.role !== filterRole) return false;
            if (filterGender !== '' && normalizeGenderKey(u.gender) !== filterGender) return false;
            if (searchQuery) {
                const q = searchQuery.toLowerCase();
                const hay = [u.fullName, u.phoneNumber, u.accountPhone, roleLabel(u.role)].join(' ').toLowerCase();
                if (!hay.includes(q)) return false;
            }
            return true;
        });
    }

    function updateStats() {
        const total = users.length;
        const patients = users.filter(function (u) { return u.role === 'patient'; }).length;
        const doctors = users.filter(function (u) { return u.role === 'doctor'; }).length;
        const admins = users.filter(function (u) { return u.role === 'admin'; }).length;
        if (els.statTotal) els.statTotal.textContent = total;
        if (els.statPatients) els.statPatients.textContent = patients;
        if (els.statDoctors) els.statDoctors.textContent = doctors;
        if (els.statAdmins) els.statAdmins.textContent = admins;
    }

    function renderTable() {
        const filtered = getFilteredUsers();
        const total = filtered.length;
        const totalPages = Math.max(1, Math.ceil(total / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;
        const start = (currentPage - 1) * pageSize;
        const pageItems = filtered.slice(start, start + pageSize);

        if (!els.tableBody) return;

        els.tableBody.innerHTML = pageItems.map(function (u) {
            const isLocked = u.status === 'locked';
            const lockBtnClass = isLocked ? 'unlock' : 'lock';
            const lockIcon = isLocked ? 'fa-lock-open' : 'fa-lock';
            const lockTitle = isLocked ? 'Mở khóa tài khoản' : 'Khóa tài khoản';
            const rowClass = selectedUserId === u.userId ? 'selected' : '';

            return (
                '<tr class="' + rowClass + '" data-user-id="' + u.userId + '">' +
                '<td><div class="user-cell">' +
                '<div class="user-name">' + escapeHtml(u.fullName) + '</div>' +
                '<div class="user-phone">' + escapeHtml(u.accountPhone || u.phoneNumber) + '</div></div></td>' +
                '<td><span class="badge ' + roleBadgeClass(u.role) + '">' + roleLabel(u.role) + '</span></td>' +
                '<td>' + genderLabel(u.gender) + '</td>' +
                '<td><span class="status-badge ' + (isLocked ? 'locked' : 'active') + '">' +
                (isLocked ? 'Locked' : 'Active') + '</span></td>' +
                '<td><div class="action-group">' +
                '<button type="button" class="action-btn view" title="Xem chi tiết" data-action="view" data-id="' + u.userId + '"><i class="fas fa-eye"></i></button>' +
                '<button type="button" class="action-btn edit" title="Chỉnh sửa" data-action="edit" data-id="' + u.userId + '"><i class="fas fa-pen"></i></button>' +
                '<button type="button" class="action-btn ' + lockBtnClass + '" title="' + lockTitle + '" data-action="toggle-lock" data-id="' + u.userId + '">' +
                '<i class="fas ' + lockIcon + '"></i></button></div></td></tr>'
            );
        }).join('');

        const end = Math.min(start + pageSize, total);
        if (els.tableInfo) {
            els.tableInfo.textContent = total
                ? 'Hiển thị ' + (start + 1) + '–' + end + ' trong tổng ' + total + ' người dùng'
                : 'Không có dữ liệu phù hợp';
        }
        renderPagination(totalPages);
        bindRowActions();
    }

    function renderPagination(totalPages) {
        if (!els.pagination) return;
        let html = '<button type="button" data-page="prev"' + (currentPage <= 1 ? ' disabled' : '') + '><i class="fas fa-chevron-left"></i></button>';
        for (let i = 1; i <= totalPages; i++) {
            html += '<button type="button" data-page="' + i + '"' + (i === currentPage ? ' class="active"' : '') + '>' + i + '</button>';
        }
        html += '<button type="button" data-page="next"' + (currentPage >= totalPages ? ' disabled' : '') + '><i class="fas fa-chevron-right"></i></button>';
        els.pagination.innerHTML = html;

        els.pagination.querySelectorAll('button').forEach(function (btn) {
            btn.addEventListener('click', function () {
                const p = btn.getAttribute('data-page');
                if (p === 'prev' && currentPage > 1) currentPage--;
                else if (p === 'next') currentPage++;
                else if (!isNaN(Number(p))) currentPage = Number(p);
                renderTable();
            });
        });
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function findUser(id) {
        return users.find(function (u) { return u.userId === id; });
    }

    function openDrawer(user) {
        selectedUserId = user.userId;
        renderTable();

        document.getElementById('drawerName').textContent = user.fullName;
        document.getElementById('drawerMeta').textContent =
            roleLabel(user.role) + ' • ' + (user.status === 'locked' ? 'Đã khóa' : 'Đang hoạt động');

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

        document.getElementById('drawerEditBtn').dataset.userId = user.userId;
        document.getElementById('drawerLockBtn').dataset.userId = user.userId;
        document.getElementById('drawerLockBtn').textContent =
            user.status === 'locked' ? 'Mở khóa' : 'Khóa tài khoản';

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
        els.modalTitle.textContent = mode === 'edit' ? 'Chỉnh sửa người dùng' : 'Thêm người dùng mới';
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
        const u = findUser(userId);
        if (!u) return;
        u.status = u.status === 'locked' ? 'active' : 'locked';
        if (selectedUserId === userId && els.detailDrawer.classList.contains('open')) {
            openDrawer(u);
        }
        updateStats();
        renderTable();
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
        e.preventDefault();
        const data = collectFormData();
        if (!data.fullName || !data.accountPhone) {
            alert('Vui lòng nhập Họ tên và Số điện thoại đăng nhập (Account).');
            return;
        }
        if (!editingUserId && !data.password) {
            alert('Vui lòng nhập mật khẩu cho tài khoản mới.');
            return;
        }
        if (editingUserId) {
            const idx = users.findIndex(function (u) { return u.userId === editingUserId; });
            if (idx >= 0) users[idx] = Object.assign({}, users[idx], data);
        } else {
            users.push(data);
        }
        closeModal();
        updateStats();
        renderTable();
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
        updateStats();
        renderTable();

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

        document.getElementById('drawerEditBtn').addEventListener('click', function () {
            const id = this.dataset.userId;
            const user = findUser(id);
            if (user) {
                closeDrawer();
                openModal('edit', user);
            }
        });
        document.getElementById('drawerLockBtn').addEventListener('click', function () {
            toggleLock(this.dataset.userId);
        });

        if (els.globalSearch) {
            els.globalSearch.addEventListener('input', function () {
                searchQuery = this.value.trim().toLowerCase();
                currentPage = 1;
                renderTable();
            });
        }

        els.filterTabs.forEach(function (tab) {
            tab.addEventListener('click', function () {
                els.filterTabs.forEach(function (t) { t.classList.remove('active'); });
                tab.classList.add('active');
                activeTab = tab.getAttribute('data-tab');
                currentPage = 1;
                renderTable();
            });
        });

        if (els.filterRole) {
            els.filterRole.addEventListener('change', function () {
                filterRole = this.value;
                currentPage = 1;
                renderTable();
            });
        }
        if (els.filterGender) {
            els.filterGender.addEventListener('change', function () {
                filterGender = this.value;
                currentPage = 1;
                renderTable();
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
