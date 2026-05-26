/**
 * Medicine Management - Full Features
 */

const API_BASE = '/admin/medicines/api';

let medicines = [];
let currentTab = 'all';
let searchKeyword = '';
let filterRoute = '';
let currentPage = 1;
const rowsPerPage = 5;
let selectedMedId = null;

// DOM Elements
const medicineTableBody = document.getElementById('medicineTableBody');
const tableInfo = document.getElementById('tableInfo');
const paginationContainer = document.getElementById('pagination');
const globalSearch = document.getElementById('globalSearch');
const filterRouteSelect = document.getElementById('filterRoute');
const toastContainer = document.getElementById('toastContainer');

// Stats
const statTotalMedications = document.getElementById('statTotalMedications');
const statOral = document.getElementById('statOral');
const statInjectable = document.getElementById('statInjectable');

// Modal
const btnAddMedicine = document.getElementById('btnAddMedicine');
const btnCloseModal = document.getElementById('btnCloseModal');
const btnCancelModal = document.getElementById('btnCancelModal');
const medicineModalOverlay = document.getElementById('medicineModalOverlay');
const medicineForm = document.getElementById('medicineForm');
const modalTitle = document.getElementById('modalTitle');
const formAction = document.getElementById('formAction');
const formMedIdInput = document.getElementById('formMedId');
const formNameInput = document.getElementById('formName');
const formConcentrationInput = document.getElementById('formConcentration');
const formFormSelect = document.getElementById('formForm');
const formRouteSelect = document.getElementById('formRoute');
const formInstructionInput = document.getElementById('formInstruction');

// Drawer
const detailDrawer = document.getElementById('detailDrawer');
const drawerOverlay = document.getElementById('drawerOverlay');
const btnCloseDrawer = document.getElementById('btnCloseDrawer');
const drawerName = document.getElementById('drawerName');
const drawerMeta = document.getElementById('drawerMeta');
const drawerMedId = document.getElementById('drawerMedId');
const drawerMedName = document.getElementById('drawerMedName');
const drawerConcentration = document.getElementById('drawerConcentration');
const drawerForm = document.getElementById('drawerForm');
const drawerRoute = document.getElementById('drawerRoute');
const drawerInstruction = document.getElementById('drawerInstruction');
const drawerEditBtn = document.getElementById('drawerEditBtn');
const drawerDeleteBtn = document.getElementById('drawerDeleteBtn');

// ========== LOAD DATA ==========
async function loadMedications() {
    try {
        const response = await fetch(API_BASE);
        const result = await response.json();
        if (result.success) {
            medicines = result.data;
            updateStats();
            loadRoutes();
            renderTable();
        }
    } catch (error) {
        showToast('Cannot connect to server', 'error');
    }
}

async function loadRoutes() {
    try {
        const response = await fetch(`${API_BASE}/routes`);
        const result = await response.json();
        if (result.success && result.data) {
            filterRouteSelect.innerHTML = '<option value="">Administration Route</option>';
            result.data.forEach(route => {
                filterRouteSelect.innerHTML += `<option value="${escapeHtml(route)}">${escapeHtml(route)}</option>`;
            });
        }
    } catch (error) {}
}

async function updateStats() {
    try {
        const response = await fetch(`${API_BASE}/summary`);
        const result = await response.json();
        if (result.success && result.data) {
            statTotalMedications.textContent = result.data.totalMedications || 0;
            statOral.textContent = result.data.oralFormulations || 0;
            statInjectable.textContent = result.data.injectableFormulations || 0;
        }
    } catch (error) {}
}

// ========== RENDER ==========
function renderTable() {
    let filtered = [...medicines];

    if (currentTab === 'tablet') {
        filtered = filtered.filter(m => m.form === 'tablet' || m.form === 'capsule');
    } else if (currentTab === 'injection') {
        filtered = filtered.filter(m => m.form === 'injection');
    }
    if (filterRoute) {
        filtered = filtered.filter(m => m.administrationRoute === filterRoute);
    }
    if (searchKeyword) {
        const kw = searchKeyword.toLowerCase();
        filtered = filtered.filter(m =>
            (m.medicationName && m.medicationName.toLowerCase().includes(kw)) ||
            (m.concentration && m.concentration.toLowerCase().includes(kw)) ||
            (m.administrationRoute && m.administrationRoute.toLowerCase().includes(kw))
        );
    }

    const totalCount = filtered.length;
    const totalPages = Math.ceil(totalCount / rowsPerPage) || 1;
    if (currentPage > totalPages) currentPage = totalPages;
    const start = (currentPage - 1) * rowsPerPage;
    const paginated = filtered.slice(start, start + rowsPerPage);

    if (paginated.length === 0) {
        medicineTableBody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:40px;">No medications found</td></tr>`;
        tableInfo.textContent = 'Showing 0 results';
        renderPagination(totalPages);
        return;
    }

    medicineTableBody.innerHTML = '';
    paginated.forEach(med => {
        let badgeClass = 'badge-tablet', formText = 'Tablet';
        if (med.form === 'capsule') { badgeClass = 'badge-capsule'; formText = 'Capsule'; }
        else if (med.form === 'injection') { badgeClass = 'badge-injection'; formText = 'Injection'; }

        const row = document.createElement('tr');
        if (selectedMedId === med.medicationId) row.classList.add('selected');
        row.innerHTML = `
            <td style="cursor:pointer;" onclick="openDetails('${med.medicationId}')">
                <div><strong>${escapeHtml(med.medicationName || '')}</strong></div>
                <small>${med.medicationId} — ${escapeHtml(med.concentration || 'N/A')}</small>
            </td>
            <td style="cursor:pointer;" onclick="openDetails('${med.medicationId}')"><span class="badge ${badgeClass}">${formText}</span></td>
            <td style="cursor:pointer;" onclick="openDetails('${med.medicationId}')"><strong>${escapeHtml(med.administrationRoute || '—')}</strong></td>
            <td style="cursor:pointer;" onclick="openDetails('${med.medicationId}')">${escapeHtml((med.usageInstruction || '').substring(0, 80))}${(med.usageInstruction || '').length > 80 ? '...' : ''}</td>
            <td class="action-group">
                <button class="action-btn view" onclick="event.stopPropagation(); openDetails('${med.medicationId}')" title="View"><i class="fas fa-eye"></i></button>
                <button class="action-btn edit" onclick="event.stopPropagation(); editMedicine('${med.medicationId}')" title="Edit"><i class="fas fa-pen"></i></button>
                <button class="action-btn delete" onclick="event.stopPropagation(); deleteMedicine('${med.medicationId}')" title="Delete"><i class="fas fa-trash"></i></button>
            </td>
        `;
        medicineTableBody.appendChild(row);
    });

    tableInfo.textContent = `Showing ${start+1} to ${Math.min(start+rowsPerPage, totalCount)} of ${totalCount} medicines`;
    renderPagination(totalPages);
}

function renderPagination(totalPages) {
    paginationContainer.innerHTML = '';
    const prev = document.createElement('button');
    prev.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prev.disabled = currentPage === 1;
    prev.onclick = () => { currentPage--; renderTable(); };
    paginationContainer.appendChild(prev);
    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i;
        if (i === currentPage) btn.classList.add('active');
        btn.onclick = () => { currentPage = i; renderTable(); };
        paginationContainer.appendChild(btn);
    }
    const next = document.createElement('button');
    next.innerHTML = '<i class="fas fa-chevron-right"></i>';
    next.disabled = currentPage === totalPages;
    next.onclick = () => { currentPage++; renderTable(); };
    paginationContainer.appendChild(next);
}

// ========== FILTERS ==========
function initFilters() {
    document.querySelectorAll('.filter-tab').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.filter-tab').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentTab = btn.dataset.tab;
            currentPage = 1;
            renderTable();
        });
    });
    globalSearch.addEventListener('input', (e) => {
        searchKeyword = e.target.value;
        currentPage = 1;
        renderTable();
    });
    filterRouteSelect.addEventListener('change', (e) => {
        filterRoute = e.target.value;
        currentPage = 1;
        renderTable();
    });
}

// ========== MODAL (ADD) ==========
function openAddModal() {
    modalTitle.textContent = 'Add New Medicine';
    formAction.value = 'add';
    formMedIdInput.removeAttribute('disabled');
    formMedIdInput.value = '';
    medicineForm.reset();
    medicineModalOverlay.classList.add('open');
    document.body.style.overflow = 'hidden';
}

function hideModal() {
    medicineModalOverlay.classList.remove('open');
    document.body.style.overflow = '';
}

btnAddMedicine.addEventListener('click', openAddModal);
btnCloseModal.addEventListener('click', hideModal);
btnCancelModal.addEventListener('click', hideModal);
medicineModalOverlay.addEventListener('click', (e) => {
    if (e.target === medicineModalOverlay) hideModal();
});

async function saveMedicine(medData, isEdit) {
    try {
        let response;
        if (isEdit) {
            response = await fetch(`${API_BASE}/${medData.medicationId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(medData)
            });
        } else {
            response = await fetch(API_BASE, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(medData)
            });
        }
        const result = await response.json();
        if (result.success) {
            showToast(result.message, 'success');
            loadMedications();
            return true;
        } else {
            showToast(result.message || 'Operation failed', 'error');
            return false;
        }
    } catch (error) {
        showToast('Server error', 'error');
        return false;
    }
}

medicineForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const isEdit = formAction.value === 'edit';
    const medData = {
        medicationId: formMedIdInput.value.trim(),
        medicationName: formNameInput.value.trim(),
        concentration: formConcentrationInput.value.trim(),
        form: formFormSelect.value,
        administrationRoute: formRouteSelect.value,
        usageInstruction: formInstructionInput.value.trim()
    };
    if (!medData.medicationName) {
        showToast('Medication name is required', 'error');
        return;
    }
    if (await saveMedicine(medData, isEdit)) hideModal();
});

// ========== EDIT MEDICINE ==========
window.editMedicine = async function(id) {
    try {
        const response = await fetch(`${API_BASE}/${id}`);
        const result = await response.json();
        if (result.success && result.data) {
            const med = result.data;
            modalTitle.textContent = `Edit: ${med.medicationName}`;
            formAction.value = 'edit';
            formMedIdInput.value = med.medicationId;
            formMedIdInput.setAttribute('disabled', 'disabled');
            formNameInput.value = med.medicationName || '';
            formConcentrationInput.value = med.concentration || '';
            formFormSelect.value = med.form || '';
            formRouteSelect.value = med.administrationRoute || '';
            formInstructionInput.value = med.usageInstruction || '';
            medicineModalOverlay.classList.add('open');
            document.body.style.overflow = 'hidden';
        }
    } catch (error) {
        showToast('Error loading medication data', 'error');
    }
};

// ========== DELETE MEDICINE ==========
window.deleteMedicine = async function(id) {
    const med = medicines.find(m => m.medicationId === id);
    if (!med) return;
    if (!confirm(`Delete "${med.medicationName}"?`)) return;
    try {
        const response = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });
        const result = await response.json();
        if (result.success) {
            showToast(result.message, 'success');
            if (detailDrawer.classList.contains('open')) closeDrawer();
            loadMedications();
        } else {
            showToast(result.message || 'Delete failed', 'error');
        }
    } catch (error) {
        showToast('Error deleting', 'error');
    }
};

// ========== VIEW DETAIL ==========
window.openDetails = async function(id) {
    selectedMedId = id;
    try {
        const response = await fetch(`${API_BASE}/${id}`);
        const result = await response.json();
        if (result.success && result.data) {
            const med = result.data;
            drawerName.textContent = med.medicationName || '';
            drawerMeta.textContent = `Formulation — ${med.administrationRoute || 'N/A'} Route`;
            drawerMedId.textContent = med.medicationId;
            drawerMedName.textContent = med.medicationName;
            drawerConcentration.textContent = med.concentration || 'N/A';
            let formText = 'Tablet';
            if (med.form === 'capsule') formText = 'Capsule';
            if (med.form === 'injection') formText = 'Injection';
            drawerForm.textContent = formText;
            drawerRoute.textContent = med.administrationRoute || 'N/A';
            drawerInstruction.textContent = med.usageInstruction || 'No instruction';
            detailDrawer.classList.add('open');
            drawerOverlay.classList.add('open');
            renderTable();
        }
    } catch (error) {
        showToast('Error loading details', 'error');
    }
};

function closeDrawer() {
    detailDrawer.classList.remove('open');
    drawerOverlay.classList.remove('open');
    selectedMedId = null;
    renderTable();
}

btnCloseDrawer.addEventListener('click', closeDrawer);
drawerOverlay.addEventListener('click', closeDrawer);

drawerEditBtn.addEventListener('click', () => {
    if (selectedMedId) { closeDrawer(); editMedicine(selectedMedId); }
});

drawerDeleteBtn.addEventListener('click', () => {
    if (selectedMedId) { closeDrawer(); deleteMedicine(selectedMedId); }
});

// ========== UTILITIES ==========
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showToast(msg, type = 'success') {
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed; bottom: 20px; right: 20px;
        background: ${type === 'success' ? '#10b981' : '#ef4444'}; color: white;
        padding: 12px 20px; border-radius: 8px; z-index: 10000;
        animation: slideIn 0.3s ease;
    `;
    toast.innerHTML = `<i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}"></i> ${msg}`;
    toastContainer.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// ========== INIT ==========
function init() {
    initFilters();
    loadMedications();
    if (!document.querySelector('#style')) {
        const style = document.createElement('style');
        style.id = 'style';
        style.textContent = `
            @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
            .selected { background-color: #e0f2fe !important; }
            .action-group { display: flex; gap: 8px; }
            .badge-tablet { background: #e0f2fe; color: #0369a1; padding: 4px 12px; border-radius: 30px; font-size: 12px; }
            .badge-capsule { background: #dcfce7; color: #166534; padding: 4px 12px; border-radius: 30px; font-size: 12px; }
            .badge-injection { background: #fef3c7; color: #92400e; padding: 4px 12px; border-radius: 30px; font-size: 12px; }
            .action-btn { width: 32px; height: 32px; border: none; border-radius: 8px; cursor: pointer; margin: 0 2px; }
            .action-btn.view { background: #e0f2fe; color: #0369a1; }
            .action-btn.edit { background: #fef9e6; color: #b7791f; }
            .action-btn.delete { background: #fdeaea; color: #c53030; }
            .action-btn:hover { transform: scale(1.05); }
        `;
        document.head.appendChild(style);
    }
}

init();