/**
 * Doctor Queue JS - Pure Thymeleaf Integration
 */

document.addEventListener('DOMContentLoaded', () => {
    // Check url param for toasts
    const urlParams = new URLSearchParams(window.location.search);
    const toastType = urlParams.get('toast');
    if (toastType) {
        if (toastType === 'completed') {
            showToast('Hoàn thành ca khám thành công!', 'success');
        } else if (toastType === 'updated') {
            showToast('Cập nhật ca khám thành công!', 'success');
        } else if (toastType === 'success') {
            showToast('Thao tác thành công!', 'success');
        } else if (toastType === 'not_today') {
            showToast('Chỉ được phép chỉnh sửa ca khám được thực hiện trong ngày hôm nay.', 'error');
        }
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    // Automatically submit search after a short delay on typing (optional enhancement)
    const searchInput = document.getElementById('queueSearchInput');
    if (searchInput) {
        let timeout = null;
        searchInput.addEventListener('input', () => {
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                searchInput.form.submit();
            }, 600); // 600ms debounce
        });
    }
});

// Toast Notification System
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    while (container.children.length >= 4) {
        const oldest = container.firstChild;
        if (oldest) {
            container.removeChild(oldest);
        }
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    let icon = type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-times-circle' : 'fa-exclamation-triangle';
    toast.innerHTML = `
        <i class="fas ${icon}"></i> 
        <span>${message}</span>
        <button type="button" class="toast-close-btn"><i class="fas fa-times"></i></button>
    `;

    container.appendChild(toast);

    const closeBtn = toast.querySelector('.toast-close-btn');
    closeBtn.addEventListener('click', () => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    });

    setTimeout(() => {
        toast.classList.add('show');
    }, 10);

    setTimeout(() => {
        if (toast.parentNode) {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }
    }, 4000);
}
