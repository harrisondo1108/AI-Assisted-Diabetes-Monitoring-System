/**
 * Doctor Patients History JS - Pure Thymeleaf Integration
 */

document.addEventListener('DOMContentLoaded', () => {
    // Handle active checkup lock for other links
    const backBtn = document.getElementById('backBtn');
    const hasActive = typeof hasActiveExam !== 'undefined' && hasActiveExam;
    let isLeavingToExamine = false;

    if (hasActive) {
        if (backBtn) {
            backBtn.addEventListener('click', () => {
                isLeavingToExamine = true;
            });
        }

        window.addEventListener('beforeunload', (e) => {
            if (!isLeavingToExamine) {
                e.preventDefault();
                e.returnValue = 'Bạn đang thực hiện khám bệnh dở dang. Bạn có chắc chắn muốn rời đi?';
                return e.returnValue;
            }
        });
    }
});



// Open timeline detail modal and load detail fragment via AJAX
function openTimelineDetail(examId) {
    const modal = document.getElementById('timelineDetailModal');
    if (modal) modal.classList.add('open');

    const modalBody = document.getElementById('timelineDetailModalBody');
    if (modalBody) {
        modalBody.innerHTML = '<div style="text-align: center; padding: 40px; color: var(--doctor-text-muted);"><i class="fas fa-spinner fa-spin fa-2x"></i><p style="margin-top: 10px;">Đang tải chi tiết ca khám...</p></div>';

        fetch(`/doctor/history/view-exam/${examId}`)
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
                modalBody.innerHTML = '<div style="text-align: center; padding: 40px; color: var(--doctor-danger);"><i class="fas fa-exclamation-triangle fa-2x"></i><p style="margin-top: 10px;">Có lỗi xảy ra khi tải chi tiết ca khám.</p></div>';
            });
    }
}

// Close timeline detail modal///////////
function closeTimelineDetail() {
    const modal = document.getElementById('timelineDetailModal');
    if (modal) modal.classList.remove('open');
}

function toggleHistoryPrescDetail(index) {
    const dropdown = document.getElementById(`presc-history-dropdown-${index}`);
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


