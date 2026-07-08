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

    // Initialize Chart.js
    const ctx = document.getElementById('systemActivityChart');
    if (ctx) {
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật'],
                datasets: [
                    {
                        label: 'Tương tác AI',
                        data: typeof aiDataFromBackend !== 'undefined' ? aiDataFromBackend : [120, 190, 150, 220, 180, 250, 210],
                        backgroundColor: '#145c4a',
                        borderWidth: 1
                    },
                    {
                        label: 'Nhắc nhở đã gửi',
                        data: typeof reminderDataFromBackend !== 'undefined' ? reminderDataFromBackend : [80, 110, 95, 140, 120, 160, 130],
                        backgroundColor: '#7ee8b5',
                        borderWidth: 1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {
                            usePointStyle: true,
                            font: {
                                family: "'Inter', sans-serif"
                            }
                        }
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        backgroundColor: 'rgba(15, 23, 42, 0.9)',
                        titleFont: { family: "'Inter', sans-serif" },
                        bodyFont: { family: "'Inter', sans-serif" },
                        padding: 12,
                        cornerRadius: 8
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false,
                            drawBorder: false
                        },
                        ticks: {
                            font: { family: "'Inter', sans-serif" }
                        }
                    },
                    y: {
                        grid: {
                            borderDash: [5, 5],
                            color: '#e2e8f0',
                            drawBorder: false
                        },
                        ticks: {
                            font: { family: "'Inter', sans-serif" }
                        }
                    }
                },
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                }
            }
        });
    }
});