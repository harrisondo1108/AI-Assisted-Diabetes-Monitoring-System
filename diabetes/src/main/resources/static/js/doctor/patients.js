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






