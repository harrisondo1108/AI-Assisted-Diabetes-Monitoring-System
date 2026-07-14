document.addEventListener('DOMContentLoaded', function() {
    // Refresh button logic
    const refreshBtn = document.getElementById('refreshDashboardBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            // Simple visual feedback
            const icon = this.querySelector('i');
            icon.classList.add('fa-spin');
            
            setTimeout(() => {
                window.location.reload();
            }, 500);
        });
    }

});