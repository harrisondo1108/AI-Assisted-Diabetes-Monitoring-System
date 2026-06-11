/**
 * Doctor Patients History JS - Pure Thymeleaf Integration
 */

document.addEventListener('DOMContentLoaded', () => {
    // Draw canvas line chart using dynamic trend from Thymeleaf
    if (typeof glucoseTrend !== 'undefined' && glucoseTrend && glucoseTrend.length > 0) {
        drawGlucoseChart(glucoseTrend);
    } else {
        // Fallback default trend if empty
        drawGlucoseChart([6.8, 7.2, 6.5, 7.0, 6.2, 5.8]);
    }

    // Adjust Back Button dynamically based on origin
    const backBtn = document.getElementById('backBtn');
    if (backBtn) {
        const fromExamineRoom = sessionStorage.getItem('fromExamineRoom') === 'true';
        if (fromExamineRoom) {
            backBtn.href = '/doctor/examine';
            backBtn.innerHTML = '<i class="fas fa-arrow-left"></i> Back to Checkup';
        } else {
            backBtn.href = '/doctor/dashboard';
            backBtn.innerHTML = '<i class="fas fa-arrow-left"></i> Back to Dashboard';
        }
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

    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'];

    // Y-Axis limits (0 to 12 mmol/L)
    const yMax = 12;

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

    for (let i = 0; i < months.length; i++) {
        const x = padding.left + (chartWidth / (months.length - 1)) * i;
        xPoints.push(x);

        // X label
        ctx.fillText(months[i], x, height - 10);
    }

    // Plot glucose trend lines (mapping the last 6 months)
    // Trim or pad data to exactly 6 entries
    let plottedData = [...data];
    if (plottedData.length > 6) {
        plottedData = plottedData.slice(-6);
    } else {
        while (plottedData.length < 6) {
            plottedData.unshift(5.5); // Pad with default normal value
        }
    }

    const dataPoints = plottedData.map((val, idx) => {
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

// Filter timeline list by date input (client-side)
function filterTimeline() {
    const query = document.getElementById('timelineSearchInput').value.toLowerCase().trim();
    const items = document.querySelectorAll('.timeline-trail .timeline-item');

    items.forEach(item => {
        const dateAttr = item.getAttribute('data-date') ? item.getAttribute('data-date').toLowerCase() : '';
        if (!query || dateAttr.includes(query)) {
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
        modalBody.innerHTML = '<div style="text-align: center; padding: 40px; color: var(--doctor-text-muted);"><i class="fas fa-spinner fa-spin fa-2x"></i><p style="margin-top: 10px;">Loading visit details...</p></div>';

        fetch(`/doctor/examine/patients/view-exam/${examId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load visit details');
                }
                return response.text();
            })
            .then(html => {
                modalBody.innerHTML = html;
            })
            .catch(error => {
                console.error(error);
                modalBody.innerHTML = '<div style="text-align: center; padding: 40px; color: var(--doctor-danger);"><i class="fas fa-exclamation-triangle fa-2x"></i><p style="margin-top: 10px;">Error loading visit details.</p></div>';
            });
    }
}

// Close timeline detail modal
function closeTimelineDetail() {
    const modal = document.getElementById('timelineDetailModal');
    if (modal) modal.classList.remove('open');
}
