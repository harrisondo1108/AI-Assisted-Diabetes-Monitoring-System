// Mock Patient Database for Today's Queue (Expanded to 15 items for pagination testing)
const defaultPatientsQueue = [
    { id: 'P012932', name: 'Nguyen Van A', age: 45, gender: 'Male', time: '08:30 AM', reason: 'Regular Diabetes Checkup', status: 'Completed' },
    { id: 'P023945', name: 'Tran Thi B', age: 62, gender: 'Female', time: '09:15 AM', reason: 'High glucose levels check', status: 'Completed' },
    { id: 'P048590', name: 'Pham Minh C', age: 38, gender: 'Male', time: '10:00 AM', reason: 'HbA1c test review', status: 'InProgress' },
    { id: 'P067823', name: 'Le Hoang D', age: 55, gender: 'Male', time: '10:45 AM', reason: 'Fasting glucose fluctuation', status: 'Pending' },
    { id: 'P089123', name: 'Vu Thi E', age: 29, gender: 'Female', time: '11:30 AM', reason: 'Gestational diabetes check', status: 'Pending' },
    { id: 'P091102', name: 'Hoang Van F', age: 70, gender: 'Male', time: '02:00 PM', reason: 'Insulin dosage calibration', status: 'Pending' },
    { id: 'P102938', name: 'Nguyen Thi G', age: 33, gender: 'Female', time: '02:30 PM', reason: 'Blood sugar monitoring', status: 'Pending' },
    { id: 'P112390', name: 'Tran Minh H', age: 49, gender: 'Male', time: '03:00 PM', reason: 'General consultation', status: 'Pending' },
    { id: 'P123490', name: 'Le Van I', age: 60, gender: 'Male', time: '03:15 PM', reason: 'HbA1c checkup', status: 'Pending' },
    { id: 'P139402', name: 'Pham Thi K', age: 27, gender: 'Female', time: '03:30 PM', reason: 'Follow-up consultation', status: 'Pending' },
    { id: 'P148201', name: 'Hoang Minh L', age: 42, gender: 'Male', time: '03:45 PM', reason: 'Diet plan adjustment', status: 'Pending' },
    { id: 'P159203', name: 'Vu Van M', age: 68, gender: 'Male', time: '04:00 PM', reason: 'Numbness in hands check', status: 'Pending' },
    { id: 'P162901', name: 'Tran Thi N', age: 50, gender: 'Female', time: '04:15 PM', reason: 'Insulin review', status: 'Pending' },
    { id: 'P172039', name: 'Nguyen Thi P', age: 65, gender: 'Female', time: '04:30 PM', reason: 'Routine review', status: 'Pending' },
    { id: 'P182390', name: 'Le Hoang Q', age: 58, gender: 'Male', time: '04:45 PM', reason: 'Kidney test review', status: 'Pending' }
];

// Persistent states using session storage
let patientsQueue = JSON.parse(sessionStorage.getItem('mockQueue'));
if (!patientsQueue || patientsQueue.length < defaultPatientsQueue.length) {
    patientsQueue = defaultPatientsQueue;
    sessionStorage.setItem('mockQueue', JSON.stringify(defaultPatientsQueue));
}

let currentFilter = 'all';
let currentPage = 1;
const pageSize = 10;
let currentSearchQuery = '';

// Initialize Dashboard
document.addEventListener('DOMContentLoaded', () => {
    renderQueue();
    updateMetrics();

    // Setup global top header search mapping
    const quickSearch = document.getElementById('quickSearchPatients');
    if (quickSearch) {
        quickSearch.addEventListener('input', (e) => {
            currentSearchQuery = e.target.value.toLowerCase().trim();
            currentPage = 1; // Reset to page 1
            renderQueue();
        });
    }
});

// Real-time Card Search handler
function handleQueueSearch() {
    const searchInput = document.getElementById('queueSearchInput');
    if (searchInput) {
        currentSearchQuery = searchInput.value.toLowerCase().trim();
        currentPage = 1; // Reset to page 1
        renderQueue();
    }
}

// Render Table Rows with Filters & Pagination
function renderQueue() {
    const tableBody = document.getElementById('queueTableBody');
    if (!tableBody) return;

    tableBody.innerHTML = '';

    const filtered = patientsQueue.filter(p => {
        // Status filter
        if (currentFilter !== 'all' && p.status.toLowerCase() !== currentFilter.toLowerCase()) {
            return false;
        }
        // Search query filter
        if (currentSearchQuery) {
            return p.name.toLowerCase().includes(currentSearchQuery) || p.id.toLowerCase().includes(currentSearchQuery);
        }
        return true;
    });

    const totalCount = filtered.length;
    const totalPages = Math.ceil(totalCount / pageSize);

    // Reset current page boundaries if filtered length shrink
    if (currentPage > totalPages && totalPages > 0) {
        currentPage = totalPages;
    }

    if (totalCount === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--doctor-text-muted); padding: 30px;">No patients found in queue</td></tr>`;
        renderPagination(0);
        return;
    }

    // Apply Pagination Slices (10 items max per page)
    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const pagedItems = filtered.slice(startIndex, endIndex);

    pagedItems.forEach(p => {
        const tr = document.createElement('tr');
        
        let statusBadge = '';
        let actionBtn = '';

        switch(p.status) {
            case 'Pending':
                statusBadge = `<span class="badge-status badge-pending"><i class="fa-regular fa-clock"></i> Pending</span>`;
                actionBtn = `<button class="btn btn-primary btn-sm" onclick="startExamination('${p.id}')"><i class="fas fa-stethoscope"></i> Start</button>`;
                break;
            case 'InProgress':
                statusBadge = `<span class="badge-status badge-inprogress"><i class="fa-solid fa-spinner fa-spin"></i> In Progress</span>`;
                actionBtn = `<button class="btn btn-primary btn-sm" onclick="startExamination('${p.id}')"><i class="fas fa-stethoscope"></i> Resume</button>`;
                break;
            case 'Completed':
                statusBadge = `<span class="badge-status badge-completed"><i class="fa-solid fa-circle-check"></i> Completed</span>`;
                actionBtn = `<button class="btn btn-secondary btn-sm" onclick="viewCompletedExam('${p.id}')"><i class="fas fa-eye"></i> View</button>`;
                break;
        }

        tr.innerHTML = `
            <td style="display: none;"><span class="patient-id">${p.id}</span></td>
            <td><span class="patient-name">${p.name}</span></td>
            <td>${p.age} yrs / ${p.gender}</td>
            <td><span style="font-weight: 500;">${p.time}</span></td>
            <td><span style="color: var(--doctor-text-muted);">${p.reason}</span></td>
            <td>${statusBadge}</td>
            <td>${actionBtn}</td>
        `;
        tableBody.appendChild(tr);
    });

    renderPagination(totalPages);
}

// Render dynamic pagination buttons
function renderPagination(totalPages) {
    const pagContainer = document.getElementById('queuePagination');
    if (!pagContainer) return;

    pagContainer.innerHTML = '';
    if (totalPages <= 1) return;

    // Prev Button
    const prevBtn = document.createElement('button');
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevBtn.disabled = currentPage === 1;
    prevBtn.onclick = () => {
        if (currentPage > 1) {
            currentPage--;
            renderQueue();
        }
    };
    pagContainer.appendChild(prevBtn);

    // Page Numbers
    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i;
        if (i === currentPage) {
            btn.classList.add('active');
        }
        btn.onclick = () => {
            currentPage = i;
            renderQueue();
        };
        pagContainer.appendChild(btn);
    }

    // Next Button
    const nextBtn = document.createElement('button');
    nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = currentPage === totalPages;
    nextBtn.onclick = () => {
        if (currentPage < totalPages) {
            currentPage++;
            renderQueue();
        }
    };
    pagContainer.appendChild(nextBtn);
}

// Handle Filter Pills
function filterQueue(status) {
    currentFilter = status;
    currentPage = 1; // Reset to page 1
    
    // Update active UI class
    const pills = document.querySelectorAll('.filter-pills .pill');
    pills.forEach(pill => {
        const pillText = pill.textContent.replace(/\s+/g, '').toLowerCase();
        const targetStatus = status.replace(/\s+/g, '').toLowerCase();
        
        if (pillText === targetStatus) {
            pill.classList.add('active');
        } else {
            pill.classList.remove('active');
        }
    });

    renderQueue();
}

// Update Dashboard Statistics counts
function updateMetrics() {
    const pending = patientsQueue.filter(p => p.status === 'Pending').length;
    const inProgress = patientsQueue.filter(p => p.status === 'InProgress').length;
    const completed = patientsQueue.filter(p => p.status === 'Completed').length;

    const queueCountEl = document.getElementById('queueCount');
    const completedCountEl = document.getElementById('completedCount');

    if (queueCountEl) queueCountEl.textContent = `${pending + inProgress} Patients`;
    if (completedCountEl) completedCountEl.textContent = `${completed} Cases`;
}

// Redirect and save session state for Examination Room
function startExamination(patientId, viewOnly = false) {
    sessionStorage.setItem('selectedPatientId', patientId);
    sessionStorage.setItem('examineViewOnly', viewOnly ? 'true' : 'false');
    
    // If pending, mark it as InProgress temporarily in local session store
    const localQueue = JSON.parse(sessionStorage.getItem('mockQueue')) || defaultPatientsQueue;
    const patient = localQueue.find(p => p.id === patientId);
    if (patient && patient.status === 'Pending' && !viewOnly) {
        patient.status = 'InProgress';
        sessionStorage.setItem('mockQueue', JSON.stringify(localQueue));
    }

    window.location.href = '/doctor/examine';
}

// Show completed exam details modal
function viewCompletedExam(patientId) {
    const patient = patientsQueue.find(p => p.id === patientId);
    if (!patient) return;

    // Open modal
    const modal = document.getElementById('completedExamModal');
    if (modal) modal.classList.add('open');

    document.getElementById('modalExamPatient').textContent = `${patient.name} (${patient.age} yrs / ${patient.gender})`;
    
    // Format today's date
    const today = new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    document.getElementById('modalExamDate').textContent = today;
    document.getElementById('modalExamNextAppt').textContent = "Jul 08, 2026";

    // Symptoms
    const symptomsDiv = document.getElementById('modalExamSymptoms');
    symptomsDiv.innerHTML = `
        <span style="background: #ffffff; border: 1px solid var(--doctor-border); padding: 4px 10px; border-radius: 12px; font-size: 0.75rem; color: var(--doctor-text-main); font-weight: 600;">Frequent Urination</span>
        <span style="background: #ffffff; border: 1px solid var(--doctor-border); padding: 4px 10px; border-radius: 12px; font-size: 0.75rem; color: var(--doctor-text-main); font-weight: 600;">Excessive Thirst</span>
        <span style="background: #ffffff; border: 1px solid var(--doctor-border); padding: 4px 10px; border-radius: 12px; font-size: 0.75rem; color: var(--doctor-text-main); font-weight: 600;">Fatigue</span>
    `;

    // Notes
    document.getElementById('modalExamNotes').textContent = "Patient reports feeling very tired during the afternoon. No signs of peripheral neuropathy. Blood pressure is normal.";

    // Generate contextual mock data
    document.getElementById('modalExamDiagnosis').textContent = `Reviewed ${patient.reason.toLowerCase()}. Blood glucose is slightly elevated. Advised to strictly follow diet plan.`;

    // Labs Table
    const labsBody = document.getElementById('modalExamLabsTableBody');
    labsBody.innerHTML = `
        <tr>
            <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border);">Fasting Blood Glucose</td>
            <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border); font-weight: 600;">6.2 <span style="font-size: 0.75rem; color: var(--doctor-text-muted); font-weight: normal;">mmol/L</span></td>
            <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border); color: var(--doctor-danger); font-weight: bold;">High</td>
        </tr>
        <tr>
            <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border);">HbA1c</td>
            <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border); font-weight: 600;">5.8 <span style="font-size: 0.75rem; color: var(--doctor-text-muted); font-weight: normal;">%</span></td>
            <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border); color: var(--doctor-danger); font-weight: bold;">High</td>
        </tr>
    `;

    // Prescription Table
    const prescBody = document.getElementById('modalExamPrescriptionTableBody');
    prescBody.innerHTML = `
        <tr>
            <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border); font-weight: 600;">Metformin Hydrochloride (500mg)</td>
            <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border);">1 Tablet</td>
            <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border); font-style: italic; color: var(--doctor-primary);">After breakfast</td>
        </tr>
    `;
}

function closeCompletedExam() {
    const modal = document.getElementById('completedExamModal');
    if (modal) modal.classList.remove('open');
}
