/* ============================================================
   notifications.js – Nhắc nhở Dùng thuốc page scripts
   ============================================================ */

function requestBrowserNotificationPermission() {
    if (!("Notification" in window)) {
        alert("Trình duyệt này không hỗ trợ thông báo trên màn hình máy tính.");
        return;
    }

    Notification.requestPermission().then(function (permission) {
        if (permission === "granted") {
            alert("Thông báo dùng thuốc trên trình duyệt đã được bật.");
        } else {
            alert("Quyền thông báo không được cấp phép.");
        }
    });
}

function showMedicationAlert(title, message) {
    if ("Notification" in window && Notification.permission === "granted") {
        new Notification(title, {
            body: message
        });
    } else {
        alert(title + "\n" + message);
    }
}

function scheduleMedicationReminders() {
    const reminderCards = document.querySelectorAll(".medication-reminder-card");
    const now = new Date();

    reminderCards.forEach(function (card) {
        const reminderTimeText = card.dataset.reminderTime;
        const title = card.dataset.title || "Nhắc nhở dùng thuốc";
        const message = card.dataset.message || "Đã đến lúc chuẩn bị dùng thuốc của bạn.";

        if (!reminderTimeText) {
            return;
        }

        const reminderTime = new Date(reminderTimeText);
        const delay = reminderTime.getTime() - now.getTime();

        if (delay > 0 && delay <= 24 * 60 * 60 * 1000) {
            setTimeout(function () {
                showMedicationAlert(title, message);
                card.classList.add("reminder-due-now");
            }, delay);
        }
    });
}

document.addEventListener("DOMContentLoaded", function() {
    scheduleMedicationReminders();
});
