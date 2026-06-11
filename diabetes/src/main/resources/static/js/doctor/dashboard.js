/**
 * Doctor Dashboard JS - Pure Thymeleaf Integration
 */

let allQueueRows = [];
let currentFilter = 'all';
let currentPage = 1;
const pageSize = 10;
let currentSearchQuery = '';

document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.getElementById('queueTableBody');
    if (tableBody) {
        // Capture all rows rendered by Thymeleaf
        allQueueRows = Array.from(tableBody.querySelectorAll('tr'));
    }

    renderQueue();
    updateMetrics();

    // Setup global top header search mapping if present
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

    // Filter rows based on search and status
    const filteredRows = allQueueRows.filter(tr => {
        const status = tr.getAttribute('data-status') || '';
        const nameEl = tr.querySelector('.patient-name');
        const idEl = tr.querySelector('.patient-id');
        const name = nameEl ? nameEl.textContent.toLowerCase() : '';
        const id = idEl ? idEl.textContent.toLowerCase() : '';

        // Status filter
        if (currentFilter !== 'all' && status.toLowerCase() !== currentFilter.toLowerCase()) {
            return false;
        }
        // Search query filter
        if (currentSearchQuery) {
            return name.includes(currentSearchQuery) || id.includes(currentSearchQuery);
        }
        return true;
    });

    const totalCount = filteredRows.length;
    const totalPages = Math.ceil(totalCount / pageSize);

    // Reset current page boundaries if filtered length shrinks
    if (currentPage > totalPages && totalPages > 0) {
        currentPage = totalPages;
    }

    // Clear and re-append only the rows for the current page
    tableBody.innerHTML = '';

    if (totalCount === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--doctor-text-muted); padding: 30px;">No patients found in queue</td></tr>`;
        renderPagination(0);
        return;
    }

    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const pagedRows = filteredRows.slice(startIndex, endIndex);

    pagedRows.forEach(tr => {
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

// Update Dashboard Statistics counts from table data
function updateMetrics() {
    let pending = 0;
    let inProgress = 0;
    let completed = 0;

    allQueueRows.forEach(tr => {
        const status = tr.getAttribute('data-status');
        if (status === 'Pending') pending++;
        else if (status === 'InProgress') inProgress++;
        else if (status === 'Completed') completed++;
    });

    const queueCountEl = document.getElementById('queueCount');
    const completedCountEl = document.getElementById('completedCount');

    if (queueCountEl) queueCountEl.textContent = `${pending + inProgress} Patients`;
    if (completedCountEl) completedCountEl.textContent = `${completed} Cases`;
}

// Show completed exam details modal using Thymeleaf Fragment loaded via AJAX
function viewCompletedExam(examId) {
    const modal = document.getElementById('completedExamModal');
    if (modal) modal.classList.add('open');

    const modalBody = document.getElementById('completedExamModalBody');
    if (modalBody) {
        modalBody.innerHTML = '<div style="text-align: center; padding: 40px; color: var(--doctor-text-muted);"><i class="fas fa-spinner fa-spin fa-2x"></i><p style="margin-top: 10px;">Loading exam details...</p></div>';

        fetch(`/doctor/dashboard/view-exam/${examId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load exam details');
                }
                return response.text();
            })
            .then(html => {
                modalBody.innerHTML = html;
            })
            .catch(error => {
                console.error(error);
                modalBody.innerHTML = '<div style="text-align: center; padding: 40px; color: var(--doctor-danger);"><i class="fas fa-exclamation-triangle fa-2x"></i><p style="margin-top: 10px;">Error loading exam details.</p></div>';
            });
    }
}

function closeCompletedExam() {
    const modal = document.getElementById('completedExamModal');
    if (modal) modal.classList.remove('open');
}

// Navigate to patient medical history timeline page
function viewPatientHistory(patientId) {
    window.location.href = `/doctor/examine/patients?patientId=${patientId}`;
}

function viewPatientHistoryFromModal() {
    const modalPatientIdEl = document.getElementById('modalPatientId');
    if (modalPatientIdEl && modalPatientIdEl.value) {
        viewPatientHistory(modalPatientIdEl.value);
    }
}
