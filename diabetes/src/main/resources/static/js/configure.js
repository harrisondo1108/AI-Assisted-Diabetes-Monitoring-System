'use strict';

let roomPage = 1, timingPage = 1;
const PAGE_SIZE = 7;
let roomKw = '', timingKw = '';

document.addEventListener('DOMContentLoaded', function () {
    initTabs();
    initSearch();
    initRoomModals();
    initTimingModals();
    initValidation();
    initConfirmModal();

    markVisible('#roomTableBody', '.room-row');
    markVisible('#timingTableBody', '.timing-row');
    renderPagination('#roomTableBody', '.room-row', roomPage, roomKw,
        'paginationInfo', 'prevPageBtn', 'nextPageBtn', 'pageNumbers', 'phòng',
        function (p) { roomPage = p; });
    renderPagination('#timingTableBody', '.timing-row', timingPage, timingKw,
        'timingPaginationInfo', 'timingPrevPageBtn', 'timingNextPageBtn', 'timingPageNumbers', 'khung giờ',
        function (p) { timingPage = p; });

    handleUrlMessages();
});

/* ── Toast Notifications ── */
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function showToast(message, type) {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = 'toast ' + (type === 'error' ? 'error' : 'success');

    const icon = type === 'error' ? 'fa-exclamation-circle' : 'fa-check-circle';
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas ${icon} toast-icon"></i>
            <span>${escapeHtml(message)}</span>
        </div>
        <button type="button" class="toast-close" title="Đóng">&times;</button>
    `;

    const removeToast = function() {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(function() { toast.remove(); }, 300);
    };

    toast.querySelector('.toast-close').addEventListener('click', removeToast);
    container.appendChild(toast);
    setTimeout(removeToast, 4500);
}

function handleUrlMessages() {
    const urlParams = new URLSearchParams(window.location.search);
    const successMsg = urlParams.get('success');
    const errorMsg = urlParams.get('error');

    if (errorMsg) {
        let msg = decodeURIComponent(errorMsg).replace(/\+/g, ' ');
        if (msg === 'duplicate') msg = 'Tên đã tồn tại trong hệ thống.';
        else if (msg === 'empty') msg = 'Vui lòng nhập tên.';
        else if (msg === 'notfound') msg = 'Không tìm thấy dữ liệu.';
        else if (msg === 'inuse_room') msg = 'Không thể xóa phòng này vì đang được phân công cho bác sĩ hoặc phòng xét nghiệm!';
        else if (msg === 'inuse_timing') msg = 'Không thể xóa thời gian dùng thuốc này vì đang được sử dụng trong đơn thuốc hoặc lịch nhắc nhở!';
        else if (msg === 'delete_failed') msg = 'Xóa thất bại. Vui lòng thử lại sau.';
        showToast(msg, 'error');
    }

    if (successMsg) {
        let msg = decodeURIComponent(successMsg).replace(/\+/g, ' ');
        if (msg === 'create_room') msg = 'Thêm phòng khám thành công!';
        else if (msg === 'update_room') msg = 'Cập nhật phòng khám thành công!';
        else if (msg === 'delete_room') msg = 'Xóa phòng khám thành công!';
        else if (msg === 'create_timing') msg = 'Thêm khung giờ dùng thuốc thành công!';
        else if (msg === 'update_timing') msg = 'Cập nhật khung giờ dùng thuốc thành công!';
        else if (msg === 'delete_timing') msg = 'Xóa khung giờ dùng thuốc thành công!';
        showToast(msg, 'success');
    }

    if (successMsg || errorMsg) {
        const url = new URL(window.location.href);
        url.searchParams.delete('success');
        url.searchParams.delete('error');
        window.history.replaceState({}, document.title, url.toString());
    }
}

/* ── Modal helpers ── */
function openModal(m) { m.classList.add('show'); document.body.style.overflow = 'hidden'; }
function closeModal(m) { m.classList.remove('show'); document.body.style.overflow = ''; }
function addClose(modal) {
    Array.prototype.slice.call(arguments, 1).forEach(function (el) {
        if (el) el.addEventListener('click', function () { closeModal(modal); });
    });
    modal.addEventListener('click', function (e) { if (e.target === modal) closeModal(modal); });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal.classList.contains('show')) closeModal(modal);
    });
}

/* ── Custom Confirm Modal ── */
let confirmAction = null;
function initConfirmModal() {
    const modal = document.getElementById('customConfirmModal');
    if (!modal) return;
    addClose(modal, document.getElementById('btnCloseConfirm'), document.getElementById('btnCancelConfirm'));
    document.getElementById('btnOkConfirm').addEventListener('click', function () {
        if (confirmAction) {
            confirmAction();
        }
        closeModal(modal);
    });
}

function showConfirm(title, message, callback) {
    const modal = document.getElementById('customConfirmModal');
    if (!modal) return;
    document.getElementById('confirmTitle').textContent = title;
    document.getElementById('confirmMessage').textContent = message;
    confirmAction = callback;
    openModal(modal);
}

/* ── TABS ── */
function initTabs() {
    var tabs = document.querySelectorAll('.tab');
    var panels = document.querySelectorAll('.tab-panel');
    tabs.forEach(function (tab) {
        tab.addEventListener('click', function () {
            tabs.forEach(function (t) { t.classList.remove('active'); });
            panels.forEach(function (p) { p.classList.remove('active'); });
            tab.classList.add('active');
            var panel = document.getElementById('panel' + cap(tab.dataset.tab));
            if (panel) panel.classList.add('active');
        });
    });
}
function cap(s) { return s ? s.charAt(0).toUpperCase() + s.slice(1) : ''; }

/* ── Backend Search — Enter key trigger ── */
function initSearch() {
    var rInput = document.getElementById('roomSearch');
    var tInput = document.getElementById('timingSearch');

    if (rInput) {
        rInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter' || e.keyCode === 13) {
                e.preventDefault();
                var val = rInput.value.trim();
                window.location.href = '/admin/configure?tab=room&search=' + encodeURIComponent(val);
            }
        });
        let rSearchTimeout = null;
        rInput.addEventListener('input', function () {
            if (rSearchTimeout) {
                clearTimeout(rSearchTimeout);
            }
            if (this.value.trim() === '') {
                rSearchTimeout = setTimeout(function () {
                    if (rInput.value.trim() === '') {
                        window.location.href = '/admin/configure?tab=room';
                    }
                }, 400);
            }
        });
        rInput.addEventListener('search', function () {
            if (this.value.trim() === '') {
                window.location.href = '/admin/configure?tab=room';
            }
        });
        var rIcon = document.querySelector('.room-search-icon');
        if (rIcon) {
            rIcon.addEventListener('click', function () {
                var val = rInput.value.trim();
                window.location.href = '/admin/configure?tab=room&search=' + encodeURIComponent(val);
            });
        }
    }

    if (tInput) {
        tInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter' || e.keyCode === 13) {
                e.preventDefault();
                var val = tInput.value.trim();
                window.location.href = '/admin/configure?tab=timing&search=' + encodeURIComponent(val);
            }
        });
        let tSearchTimeout = null;
        tInput.addEventListener('input', function () {
            if (tSearchTimeout) {
                clearTimeout(tSearchTimeout);
            }
            if (this.value.trim() === '') {
                tSearchTimeout = setTimeout(function () {
                    if (tInput.value.trim() === '') {
                        window.location.href = '/admin/configure?tab=timing';
                    }
                }, 400);
            }
        });
        tInput.addEventListener('search', function () {
            if (this.value.trim() === '') {
                window.location.href = '/admin/configure?tab=timing';
            }
        });
        var tIcon = document.querySelector('.timing-search-icon');
        if (tIcon) {
            tIcon.addEventListener('click', function () {
                var val = tInput.value.trim();
                window.location.href = '/admin/configure?tab=timing&search=' + encodeURIComponent(val);
            });
        }
    }
}

function markVisible(tbody, rowSel) {
    document.querySelectorAll(tbody + ' ' + rowSel).forEach(function (r) { r.dataset.filtered = 'visible'; });
}

/* ── PAGINATION ── */
function renderPagination(tbody, rowSel, curPage, kw, infoId, prevId, nextId, numsId, unit, setPage) {
    var all = Array.from(document.querySelectorAll(tbody + ' ' + rowSel));
    var visible = all.filter(function (r) { return r.dataset.filtered !== 'hidden'; });
    var total = visible.length;
    var pages = Math.max(1, Math.ceil(total / PAGE_SIZE));
    if (curPage > pages) curPage = pages;

    all.forEach(function (r) { r.style.display = 'none'; });
    visible.slice((curPage - 1) * PAGE_SIZE, curPage * PAGE_SIZE).forEach(function (r) { r.style.display = ''; });

    var info = document.getElementById(infoId);
    var prev = document.getElementById(prevId);
    var next = document.getElementById(nextId);
    var nums = document.getElementById(numsId);
    if (!info || !prev || !next || !nums) return;

    var s = total === 0 ? 0 : (curPage - 1) * PAGE_SIZE + 1;
    var e = Math.min(curPage * PAGE_SIZE, total);
    info.textContent = 'Hiển thị ' + s + ' – ' + e + ' / ' + total + ' ' + unit;
    prev.disabled = curPage <= 1;
    next.disabled = curPage >= pages;
    prev.onclick = function () { if (curPage > 1) { setPage(curPage - 1); reDraw(tbody, rowSel, curPage - 1, kw, infoId, prevId, nextId, numsId, unit, setPage); } };
    next.onclick = function () { if (curPage < pages) { setPage(curPage + 1); reDraw(tbody, rowSel, curPage + 1, kw, infoId, prevId, nextId, numsId, unit, setPage); } };

    nums.innerHTML = '';
    for (var i = 1; i <= pages; i++) {
        (function (p) {
            var btn = document.createElement('button');
            btn.type = 'button'; btn.className = 'page-number' + (p === curPage ? ' active' : '');
            btn.textContent = p;
            btn.addEventListener('click', function () { setPage(p); reDraw(tbody, rowSel, p, kw, infoId, prevId, nextId, numsId, unit, setPage); });
            nums.appendChild(btn);
        })(i);
    }
}
function reDraw(tbody, rowSel, p, kw, infoId, prevId, nextId, numsId, unit, setPage) {
    renderPagination(tbody, rowSel, p, kw, infoId, prevId, nextId, numsId, unit, setPage);
}

/* ── Validation & Character Limit Counter ── */
function setupInputValidation(inputId, countId, errorId, maxLen, pattern, errMsg) {
    const input = document.getElementById(inputId);
    const count = document.getElementById(countId);
    const err = document.getElementById(errorId);
    if (!input) return;

    input.addEventListener('input', function () {
        const value = input.value;
        if (count) count.textContent = `${value.length} / ${maxLen}`;

        if (value.length > maxLen) {
            input.value = value.substring(0, maxLen);
            if (count) count.textContent = `${maxLen} / ${maxLen}`;
        }

        if (err) {
            if (pattern && value.trim() !== '' && !pattern.test(value)) {
                err.textContent = errMsg;
            } else {
                err.textContent = '';
            }
        }
    });
}

function initValidation() {
    const namePattern = /^[^<>;'"\\`$^`{}~|\[\]]+$/;
    const descPattern = /^[^<>;'"\\`$^`{}~|\[\]]+$/;
    const nameErrMsg = 'Không chứa các ký tự đặc biệt nguy hiểm (< > ; \' " \\ `).';

    setupInputValidation('inputAddRoomName', 'countAddRoomName', 'errorAddRoomName', 100, namePattern, nameErrMsg);
    setupInputValidation('inputAddRoomDesc', 'countAddRoomDesc', 'errorAddRoomDesc', 255, descPattern, nameErrMsg);
    setupInputValidation('editRoomName', 'countEditRoomName', 'errorEditRoomName', 100, namePattern, nameErrMsg);
    setupInputValidation('editRoomDesc', 'countEditRoomDesc', 'errorEditRoomDesc', 255, descPattern, nameErrMsg);
    setupInputValidation('inputAddTimingName', 'countAddTimingName', 'errorAddTimingName', 100, namePattern, nameErrMsg);
    setupInputValidation('editTimingName', 'countEditTimingName', 'errorEditTimingName', 100, namePattern, nameErrMsg);
}

function validateFormBeforeSubmit(nameId, descId, errorNameId) {
    const name = document.getElementById(nameId)?.value.trim() || '';
    const err = document.getElementById(errorNameId);
    if (!name) {
        if (err) err.textContent = 'Trường này không được để trống.';
        return false;
    }
    const errText = err?.textContent || '';
    if (errText !== '') {
        return false;
    }
    return true;
}

/* ── ROOM MODALS ── */
function initRoomModals() {
    var addModal = document.getElementById('addRoomModal');
    var editModal = document.getElementById('editRoomModal');
    var editForm = document.getElementById('editRoomForm');

    if (addModal) {
        var open = document.getElementById('btnAddRoom');
        if (open) open.addEventListener('click', function () { openModal(addModal); });
        addClose(addModal, document.getElementById('btnCloseAddRoom'), document.getElementById('btnCancelAddRoom'));

        document.getElementById('addRoomForm')?.addEventListener('submit', function (e) {
            if (!validateFormBeforeSubmit('inputAddRoomName', 'inputAddRoomDesc', 'errorAddRoomName')) {
                e.preventDefault();
            }
        });
    }

    if (editModal && editForm) {
        addClose(editModal, document.getElementById('btnCloseEditRoom'), document.getElementById('btnCancelEditRoom'));
        document.querySelectorAll('.edit-room-btn').forEach(function (btn) {
            btn.addEventListener('click', function () {
                document.getElementById('editRoomName').value = btn.dataset.name || '';
                document.getElementById('editRoomDesc').value = (btn.dataset.desc && btn.dataset.desc !== 'null') ? btn.dataset.desc : '';
                editForm.action = '/admin/configure/room/update/' + btn.dataset.id;

                // Trigger input for counter updates
                document.getElementById('editRoomName').dispatchEvent(new Event('input'));
                document.getElementById('editRoomDesc').dispatchEvent(new Event('input'));
                openModal(editModal);
            });
        });

        editForm.addEventListener('submit', function (e) {
            if (!validateFormBeforeSubmit('editRoomName', 'editRoomDesc', 'errorEditRoomName')) {
                e.preventDefault();
            }
        });
    }

    // Delete confirm custom
    document.querySelectorAll('.delete-room-btn').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const id = btn.dataset.id;
            const name = btn.dataset.name;
            showConfirm(
                'Xác nhận xóa phòng',
                `Bạn có chắc chắn muốn xóa phòng "${name}"?`,
                function () {
                    const form = document.createElement('form');
                    form.method = 'post';
                    form.action = `/admin/configure/room/delete/${id}`;
                    document.body.appendChild(form);
                    form.submit();
                }
            );
        });
    });
}

/* ── TIMING MODALS ── */
function initTimingModals() {
    var addModal = document.getElementById('addTimingModal');
    var editModal = document.getElementById('editTimingModal');
    var editForm = document.getElementById('editTimingForm');

    if (addModal) {
        var open = document.getElementById('btnAddTiming');
        if (open) open.addEventListener('click', function () { openModal(addModal); });
        addClose(addModal, document.getElementById('btnCloseAddTiming'), document.getElementById('btnCancelAddTiming'));

        document.getElementById('addTimingForm')?.addEventListener('submit', function (e) {
            if (!validateFormBeforeSubmit('inputAddTimingName', null, 'errorAddTimingName')) {
                e.preventDefault();
            }
        });
    }

    if (editModal && editForm) {
        addClose(editModal, document.getElementById('btnCloseEditTiming'), document.getElementById('btnCancelEditTiming'));
        document.querySelectorAll('.edit-timing-btn').forEach(function (btn) {
            btn.addEventListener('click', function () {
                document.getElementById('editTimingName').value = btn.dataset.name || '';
                editForm.action = '/admin/configure/timing/update/' + btn.dataset.id;

                document.getElementById('editTimingName').dispatchEvent(new Event('input'));
                openModal(editModal);
            });
        });

        editForm.addEventListener('submit', function (e) {
            if (!validateFormBeforeSubmit('editTimingName', null, 'errorEditTimingName')) {
                e.preventDefault();
            }
        });
    }

    // Delete confirm custom
    document.querySelectorAll('.delete-timing-btn').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const id = btn.dataset.id;
            const name = btn.dataset.name;
            showConfirm(
                'Xác nhận xóa thời điểm',
                `Bạn có chắc chắn muốn xóa thời điểm dùng thuốc "${name}"?`,
                function () {
                    const form = document.createElement('form');
                    form.method = 'post';
                    form.action = `/admin/configure/timing/delete/${id}`;
                    document.body.appendChild(form);
                    form.submit();
                }
            );
        });
    });
}
