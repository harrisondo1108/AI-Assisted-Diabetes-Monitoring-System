(function() {
        // Draw line chart with green color
        const canvas = document.getElementById('glucoseChart');
        if (canvas) {
            const ctx = canvas.getContext('2d');
            const width = canvas.clientWidth;
            const height = 300;
            canvas.width = width;
            canvas.height = height;

            const days = ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ Nhật'];
            const values = [12, 18, 15, 24, 20, 16, 10];
            const padding = { top: 20, right: 25, bottom: 30, left: 45 };
            const chartWidth = width - padding.left - padding.right;
            const chartHeight = height - padding.top - padding.bottom;

            const maxVal = Math.max(...values) + 5;
            const minVal = Math.min(...values) - 2;
            const yStep = chartHeight / (maxVal - minVal);

            function getX(index) {
                return padding.left + (index / (days.length - 1)) * chartWidth;
            }
            function getY(value) {
                return padding.top + chartHeight - (value - minVal) * yStep;
            }

            // Clear and draw axes
            ctx.clearRect(0, 0, width, height);
            ctx.strokeStyle = '#e2ebe7';
            ctx.lineWidth = 1;
            // Y-axis
            ctx.beginPath();
            ctx.moveTo(padding.left, padding.top);
            ctx.lineTo(padding.left, padding.top + chartHeight);
            ctx.stroke();
            // X-axis
            ctx.beginPath();
            ctx.moveTo(padding.left, padding.top + chartHeight);
            ctx.lineTo(padding.left + chartWidth, padding.top + chartHeight);
            ctx.stroke();

            // Draw grid lines (horizontal)
            for (let i = 0; i <= 4; i++) {
                const yVal = minVal + (i * (maxVal - minVal) / 4);
                const y = getY(yVal);
                ctx.beginPath();
                ctx.moveTo(padding.left, y);
                ctx.lineTo(padding.left + chartWidth, y);
                ctx.strokeStyle = '#e9efec';
                ctx.stroke();
                ctx.fillStyle = '#8a9a94';
                ctx.font = '10px Inter';
                ctx.fillText(Math.round(yVal), padding.left - 28, y + 3);
            }

            // Draw line and points
            ctx.beginPath();
            ctx.strokeStyle = '#1a6b56';
            ctx.lineWidth = 2.5;
            let first = true;
            for (let i = 0; i < days.length; i++) {
                const x = getX(i);
                const y = getY(values[i]);
                if (first) {
                    ctx.moveTo(x, y);
                    first = false;
                } else {
                    ctx.lineTo(x, y);
                }
            }
            ctx.stroke();

            // Draw points
            for (let i = 0; i < days.length; i++) {
                const x = getX(i);
                const y = getY(values[i]);
                ctx.beginPath();
                ctx.fillStyle = '#7ee8b5';
                ctx.arc(x, y, 5, 0, 2 * Math.PI);
                ctx.fill();
                ctx.fillStyle = '#0f4a3d';
                ctx.arc(x, y, 2, 0, 2 * Math.PI);
                ctx.fill();
                ctx.fillStyle = '#1a6b56';
                ctx.font = 'bold 11px Inter';
                ctx.fillText(values[i], x - 6, y - 6);
            }
        }

        // Refresh button
        const refreshBtn = document.getElementById('refreshDashboardBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', function(e) {
                e.preventDefault();
                showToast('Đã cập nhật dữ liệu bảng điều khiển thành công', 'success');
                const elements = document.querySelectorAll('.stat-number, .item-value');
                elements.forEach(el => {
                    el.style.transform = 'scale(1.02)';
                    setTimeout(() => { el.style.transform = ''; }, 200);
                });
            });
        }

        function showToast(message, type) {
            let container = document.querySelector('.toast-container');
            if (!container) {
                container = document.createElement('div');
                container.className = 'toast-container';
                document.body.appendChild(container);
            }
            const toast = document.createElement('div');
            toast.className = `toast ${type === 'success' ? 'success' : 'info'}`;
            toast.innerHTML = `<i class="fas fa-check-circle"></i><span>${message}</span>`;
            container.appendChild(toast);
            setTimeout(() => {
                toast.style.animation = 'fadeOutRight 0.3s ease';
                setTimeout(() => toast.remove(), 300);
            }, 2800);
        }
    })();