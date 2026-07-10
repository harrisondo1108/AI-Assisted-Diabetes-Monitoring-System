// Set date display in header dynamically
(function () {
    const days = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
    const months = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
    const now = new Date();
    const formattedDate = `${days[now.getDay()]}, ngày ${now.getDate()} ${months[now.getMonth()]} năm ${now.getFullYear()}`;
    const dateEl = document.getElementById('headerDate');
    if (dateEl) {
        dateEl.innerHTML = `<i class="far fa-calendar-alt"></i> ${formattedDate}`;
    }
})();
