/* ============================================================
   notifications-history.js – Lịch sử Thông báo page scripts
   ============================================================ */

let currentFilter = 'all';

function filterNotifications(filter) {
    currentFilter = filter;

    // Toggle Active Tab Styling
    document.getElementById('tab-all').classList.toggle('active', filter === 'all');
    document.getElementById('tab-unread').classList.toggle('active', filter === 'unread');

    const groups = document.querySelectorAll('.notification-group');
    let visibleCount = 0;

    groups.forEach(group => {
        let visibleInGroup = 0;
        const groupCards = group.querySelectorAll('.notification-card');

        groupCards.forEach(card => {
            const isRead = card.getAttribute('data-read') === 'true';
            if (filter === 'all' || (filter === 'unread' && !isRead)) {
                card.style.display = 'flex';
                visibleInGroup++;
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        // Hide the entire group header if no visible cards are inside
        group.style.display = visibleInGroup > 0 ? '' : 'none';
    });

    // Show/hide empty state
    const emptyState = document.getElementById('empty-state');
    if (visibleCount === 0) {
        emptyState.style.display = 'block';
        if (filter === 'unread') {
            document.getElementById('empty-state-title').innerText = 'Không có thông báo chưa đọc';
            document.getElementById('empty-state-desc').innerText = 'Chúc mừng! Bạn đã đọc toàn bộ thông báo của mình.';
        } else {
            document.getElementById('empty-state-title').innerText = 'Không có thông báo';
            document.getElementById('empty-state-desc').innerText = 'Bạn chưa nhận được bất kỳ thông báo hoặc cập nhật y khoa nào.';
        }
    } else {
        emptyState.style.display = 'none';
    }
}

function markAllAsRead() {
    fetch('/patient/notifications/mark-all-read', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Update frontend cards locally
            const cards = document.querySelectorAll('.notification-card');
            cards.forEach(card => {
                card.classList.remove('unread');
                card.classList.add('read');
                card.setAttribute('data-read', 'true');

                const dot = card.querySelector('.notif-dot');
                if (dot) {
                    dot.remove();
                }
            });

            // Reset counts
            document.getElementById('count-unread').innerText = '0';

            // Hide header notification bell dot
            const headerDot = document.querySelector('.header-actions .notification-dot');
            if (headerDot) {
                headerDot.remove();
            }

            // Hide mark-all-read button
            const markBtn = document.getElementById('mark-all-read-btn');
            if (markBtn) {
                markBtn.style.display = 'none';
            }

            // If currently viewing unread tab, re-apply filter to update display
            if (currentFilter === 'unread') {
                filterNotifications('unread');
            }
        } else {
            alert(data.message || 'Có lỗi xảy ra khi cập nhật thông báo.');
        }
    })
    .catch(err => {
        console.error('Error marking all as read:', err);
        alert('Không thể kết nối đến máy chủ. Vui lòng thử lại sau.');
    });
}
