/**
 * Doctor Dashboard JS - Pure Thymeleaf Integration
 */

let searchTimeout;

// Debounced search on input
function handleQueueSearch() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        const activePill = document.querySelector('.filter-pills .pill.active');
        const status = activePill ? activePill.getAttribute('data-status') : 'all';
        const searchVal = document.getElementById('queueSearchInput').value.trim();
        window.location.href = `/doctor/dashboard?page=1&status=${status}&search=${encodeURIComponent(searchVal)}`;
    }, 600);
}

// Redirect when changing filter pills
function filterQueue(status) {
    const searchVal = document.getElementById('queueSearchInput').value.trim();
    window.location.href = `/doctor/dashboard?page=1&status=${status}&search=${encodeURIComponent(searchVal)}`;
}

// Redirect when clicking page numbers
function navigatePage(page) {
    const activePill = document.querySelector('.filter-pills .pill.active');
    const status = activePill ? activePill.getAttribute('data-status') : 'all';
    const searchVal = document.getElementById('queueSearchInput').value.trim();
    window.location.href = `/doctor/dashboard?page=${page}&status=${status}&search=${encodeURIComponent(searchVal)}`;
}

document.addEventListener('DOMContentLoaded', () => {
    // Setup global top header search mapping if present
    const quickSearch = document.getElementById('quickSearchPatients');
    if (quickSearch) {
        // Read search parameter from URL to populate top search bar
        const urlParams = new URLSearchParams(window.location.search);
        const searchParam = urlParams.get('search') || '';
        quickSearch.value = searchParam;

        quickSearch.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                const activePill = document.querySelector('.filter-pills .pill.active');
                const status = activePill ? activePill.getAttribute('data-status') : 'all';
                const searchVal = e.target.value.trim();
                window.location.href = `/doctor/dashboard?page=1&status=${status}&search=${encodeURIComponent(searchVal)}`;
            }, 600);
        });
    }

    // Check url param for toasts
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('toast') === 'updated') {
        showToast('Cập nhật ca khám thành công!', 'success');
        window.history.replaceState({}, document.title, window.location.pathname);
    } else if (urlParams.get('toast') === 'completed') {
        showToast('Lưu ca khám thành công! Ca khám đã được hoàn thành.', 'success');
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});

// Show completed exam details modal using Thymeleaf Fragment loaded via AJAX
function viewCompletedExam(examId) {
    const modal = document.getElementById('completedExamModal');
    if (modal) modal.classList.add('open');

    const modalBody = document.getElementById('completedExamModalBody');
    if (modalBody) {
        modalBody.innerHTML = '<div style="text-align: center; padding: 40px; color: var(--doctor-text-muted);"><i class="fas fa-spinner fa-spin fa-2x"></i><p style="margin-top: 10px;">Đang tải chi tiết ca khám...</p></div>';

        fetch(`/doctor/dashboard/view-exam/${examId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Không thể tải chi tiết ca khám');
                }
                return response.text();
            })
            .then(html => {
                modalBody.innerHTML = html;
            })
            .catch(error => {
                console.error(error);
                modalBody.innerHTML = '<div style="text-align: center; padding: 40px; color: var(--doctor-danger);"><i class="fas fa-exclamation-triangle fa-2x"></i><p style="margin-top: 10px;">Lỗi khi tải chi tiết ca khám.</p></div>';
            });
    }
}

function closeCompletedExam() {
    const modal = document.getElementById('completedExamModal');
    if (modal) modal.classList.remove('open');
}

// Navigate to patient medical history timeline page
function viewPatientHistory(patientId) {
    window.location.href = `/doctor/history?patientId=${patientId}&from=dashboard`;
}

function viewPatientHistoryFromModal() {
    const modalPatientIdEl = document.getElementById('modalPatientId');
    if (modalPatientIdEl && modalPatientIdEl.value) {
        viewPatientHistory(modalPatientIdEl.value);
    }
}

function toggleDashboardPrescDetail(index) {
    const dropdown = document.getElementById(`presc-dashboard-dropdown-${index}`);
    if (dropdown) {
        const btn = dropdown.previousElementSibling.querySelector('.detail-btn i');
        if (dropdown.style.display === 'none') {
            dropdown.style.display = 'block';
            if (btn) btn.className = 'fas fa-eye-slash';
        } else {
            dropdown.style.display = 'none';
            if (btn) btn.className = 'fas fa-eye';
        }
    }
}

// Toast Notification System
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    while (container.children.length >= 4) {
        const oldest = container.firstChild;
        if (oldest) {
            container.removeChild(oldest);
        }
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    let icon = type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-times-circle' : 'fa-exclamation-triangle';
    toast.innerHTML = `
        <i class="fas ${icon}"></i> 
        <span>${message}</span>
        <button type="button" class="toast-close-btn"><i class="fas fa-times"></i></button>
    `;

    container.appendChild(toast);

    const closeBtn = toast.querySelector('.toast-close-btn');
    closeBtn.addEventListener('click', () => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    });

    setTimeout(() => {
        toast.classList.add('show');
    }, 10);

    setTimeout(() => {
        if (toast.parentNode) {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }
    }, 4000);
}
