/**
 * queue-cancel-modal.js
 * Controls the Cancel Examination modal on the Doctor Queue page.
 */

const cancelBaseUrl = document.getElementById('cancelModal').dataset.baseUrl;

function openCancelModal(patientId, patientName) {
    const modal = document.getElementById('cancelModal');
    const form = document.getElementById('cancelForm');
    const nameEl = document.getElementById('cancelPatientName');
    const textarea = document.getElementById('cancelReason');
    const errorEl = document.getElementById('cancelReasonError');

    // Reset state
    textarea.value = '';
    textarea.style.borderColor = '#e5e7eb';
    errorEl.style.display = 'none';

    // Set patient info
    nameEl.textContent = 'Bệnh nhân: ' + patientName;

    // Set form POST action dynamically
    form.action = cancelBaseUrl + '/' + patientId + '/cancel';

    // Show modal
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';

    // Focus textarea after animation completes
    setTimeout(() => textarea.focus(), 250);
}

function closeCancelModal() {
    document.getElementById('cancelModal').style.display = 'none';
    document.body.style.overflow = '';
}

// Validate on submit — block if reason is empty
document.getElementById('cancelForm').addEventListener('submit', function (e) {
    const textarea = document.getElementById('cancelReason');
    const errorEl = document.getElementById('cancelReasonError');

    if (!textarea.value.trim()) {
        e.preventDefault();
        errorEl.style.display = 'flex';
        textarea.style.borderColor = '#dc2626';
        textarea.focus();
        return;
    }
    errorEl.style.display = 'none';
});

// Close on backdrop click
document.getElementById('cancelModal').addEventListener('click', function (e) {
    if (e.target === this) closeCancelModal();
});

// Close on Escape key
document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') closeCancelModal();
});
