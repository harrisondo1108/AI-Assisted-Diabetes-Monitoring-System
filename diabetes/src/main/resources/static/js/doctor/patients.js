/**
 * Doctor Patients History JS - Pure Thymeleaf Integration
 */

document.addEventListener('DOMContentLoaded', () => {
    const chartWrapper = document.getElementById('chartWrapper');
    const chartLegend = document.getElementById('chartLegend');
    const noChartDataMessage = document.getElementById('noChartDataMessage');

    // Draw canvas line chart using dynamic trend from Thymeleaf if >= 4 entries
    if (typeof glucoseTrend !== 'undefined' && glucoseTrend && glucoseTrend.length >= 4) {
        if (chartWrapper) chartWrapper.style.display = 'block';
        if (chartLegend) chartLegend.style.display = 'flex';
        if (noChartDataMessage) noChartDataMessage.style.display = 'none';
        drawGlucoseChart(glucoseTrend);
    } else {
        if (chartWrapper) chartWrapper.style.display = 'none';
        if (chartLegend) chartLegend.style.display = 'none';
        if (noChartDataMessage) noChartDataMessage.style.display = 'block';
    }

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

// HTML5 Canvas line chart drawing function
function drawGlucoseChart(data) {
    const canvas = document.getElementById('glucoseTrendChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    // Clear previous drawing
    ctx.clearRect(0, 0, width, height);

    // Chart dimensions configuration
    const padding = { top: 20, right: 30, bottom: 30, left: 40 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    // Y-Axis limits (0 to at least 12 mmol/L, or max value in data + 1)
    let maxVal = 12;
    data.forEach(item => {
        const val = Number(item.val);
        if (val > maxVal) {
            maxVal = Math.ceil(val + 1);
        }
    });
    const yMax = maxVal;

    // Draw grid lines and Y-axis scale
    ctx.strokeStyle = '#e2e8f0';
    ctx.lineWidth = 1;
    ctx.fillStyle = '#64748b';
    ctx.font = '10px Plus Jakarta Sans';
    ctx.textAlign = 'right';

    const yLines = 4;
    for (let i = 0; i <= yLines; i++) {
        const yVal = (yMax / yLines) * i;
        const y = padding.top + chartHeight - (chartHeight * (yVal / yMax));

        // Horizontal grid line
        ctx.beginPath();
        ctx.moveTo(padding.left, y);
        ctx.lineTo(width - padding.right, y);
        ctx.stroke();

        // Y label
        ctx.fillText(yVal.toFixed(1), padding.left - 8, y + 3);
    }

    // Draw target healthy blood sugar shade (3.9 to 5.6 mmol/L)
    const normalYMin = padding.top + chartHeight - (chartHeight * (5.6 / yMax));
    const normalYMax = padding.top + chartHeight - (chartHeight * (3.9 / yMax));
    ctx.fillStyle = 'rgba(16, 185, 129, 0.08)'; // Light success green
    ctx.fillRect(padding.left, normalYMin, chartWidth, normalYMax - normalYMin);

    // Target boundary line indicators (dashed)
    ctx.strokeStyle = 'rgba(16, 185, 129, 0.4)';
    ctx.setLineDash([4, 4]);
    ctx.beginPath();
    ctx.moveTo(padding.left, normalYMin);
    ctx.lineTo(width - padding.right, normalYMin);
    ctx.moveTo(padding.left, normalYMax);
    ctx.lineTo(width - padding.right, normalYMax);
    ctx.stroke();
    ctx.setLineDash([]); // Reset line dash

    // Draw X-Axis labels
    ctx.textAlign = 'center';
    ctx.fillStyle = '#64748b';
    const xPoints = [];
    const numPoints = data.length;

    for (let i = 0; i < numPoints; i++) {
        const divisor = numPoints > 1 ? (numPoints - 1) : 1;
        const x = padding.left + (chartWidth / divisor) * i;
        xPoints.push(x);

        // X label (from data[i].date)
        const label = data[i].date || '';
        ctx.fillText(label, x, height - 10);
    }

    const dataPoints = data.map((item, idx) => {
        const val = Number(item.val);
        const x = xPoints[idx];
        const y = padding.top + chartHeight - (chartHeight * (val / yMax));
        return { x, y, val };
    });

    // Draw line connecting data points
    ctx.strokeStyle = '#0f766e'; // Medical Teal
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(dataPoints[0].x, dataPoints[0].y);
    for (let i = 1; i < dataPoints.length; i++) {
        ctx.lineTo(dataPoints[i].x, dataPoints[i].y);
    }
    ctx.stroke();

    // Draw points and values text labels
    dataPoints.forEach(point => {
        // Point node
        ctx.fillStyle = '#0f766e';
        ctx.beginPath();
        ctx.arc(point.x, point.y, 5, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#ffffff';
        ctx.beginPath();
        ctx.arc(point.x, point.y, 2.5, 0, Math.PI * 2);
        ctx.fill();

        // Value text
        ctx.fillStyle = '#0f172a';
        ctx.font = 'bold 9px Plus Jakarta Sans';
        ctx.fillText(Number(point.val).toFixed(1), point.x, point.y - 10);
    });
}

// Filter timeline list by date range input (client-side)
function filterTimeline() {
    const fromDateVal = document.getElementById('timelineFromDate').value;
    const toDateVal = document.getElementById('timelineToDate').value;

    if (fromDateVal && toDateVal && toDateVal < fromDateVal) {
        alert("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.");
        document.getElementById('timelineToDate').value = "";
        // Re-run filter with empty toDate
        filterTimeline();
        return;
    }

    const items = document.querySelectorAll('.timeline-trail .timeline-item');

    items.forEach(item => {
        const itemDate = item.getAttribute('data-exam-date'); // format: yyyy-MM-dd
        if (!itemDate) return;

        let visible = true;
        if (fromDateVal && itemDate < fromDateVal) {
            visible = false;
        }
        if (toDateVal && itemDate > toDateVal) {
            visible = false;
        }

        if (visible) {
            item.style.display = '';
        } else {
            item.style.display = 'none';
        }
    });
}

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


