'use strict';

let currentPage  = 1;
const PAGE_SIZE  = 7;
let activeFilter = 'all';
let searchKeyword = '';

document.addEventListener('DOMContentLoaded', function () {
    markAllRowsVisible();
    initSearch();
    initCreateModal();
    initEditModal();
    initViewModal();
    initValidation();
    initConfirmModal();
    renderPagination();
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
        if (msg === 'duplicate') msg = 'Tên xét nghiệm đã tồn tại trong hệ thống!';
        else if (msg === 'empty') msg = 'Vui lòng nhập đầy đủ các trường thông tin!';
        else if (msg === 'room') msg = 'Vui lòng chọn phòng xét nghiệm!';
        showToast(msg, 'error');
    }

    if (successMsg) {
        let msg = decodeURIComponent(successMsg).replace(/\+/g, ' ');
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
function openModal(modal)  { modal.classList.add('show');    document.body.style.overflow = 'hidden'; }
function closeModal(modal) { modal.classList.remove('show'); document.body.style.overflow = ''; }

function addCloseHandlers(modal, ...triggers) {
    triggers.forEach(function(el) { if (el) el.addEventListener('click', function() { closeModal(modal); }); });
    modal.addEventListener('click', function(e) { if (e.target === modal) closeModal(modal); });
    document.addEventListener('keydown', function(e) { if (e.key === 'Escape' && modal.classList.contains('show')) closeModal(modal); });
}

/* ── Custom Confirm Modal ── */
let confirmAction = null;
function initConfirmModal() {
    const modal = document.getElementById('customConfirmModal');
    if (!modal) return;
    addCloseHandlers(modal, document.getElementById('btnCloseConfirm'), document.getElementById('btnCancelConfirm'));
    document.getElementById('btnOkConfirm').addEventListener('click', function() {
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

function buildThresholdData(p) {
    return {
        young:    { min: val(p+'YoungMin'),    max: val(p+'YoungMax') },
        middle:   { min: val(p+'MiddleMin'),   max: val(p+'MiddleMax') },
        elder:    { min: val(p+'ElderMin'),    max: val(p+'ElderMax') },
        pregnant: { min: val(p+'PregnantMin'), max: val(p+'PregnantMax') },
        children: { min: val(p+'ChildrenMin'), max: val(p+'ChildrenMax') }
    };
}

function fillThresholdInputs(p, data, fbMin, fbMax) {
    setVal(p+'YoungMin',    data?.young?.min    ?? fbMin ?? 0);
    setVal(p+'YoungMax',    data?.young?.max    ?? fbMax ?? 0);
    setVal(p+'MiddleMin',   data?.middle?.min   ?? fbMin ?? 0);
    setVal(p+'MiddleMax',   data?.middle?.max   ?? fbMax ?? 0);
    setVal(p+'ElderMin',    data?.elder?.min    ?? fbMin ?? 0);
    setVal(p+'ElderMax',    data?.elder?.max    ?? fbMax ?? 0);
    setVal(p+'PregnantMin', data?.pregnant?.min ?? fbMin ?? 0);
    setVal(p+'PregnantMax', data?.pregnant?.max ?? fbMax ?? 0);
    setVal(p+'ChildrenMin', data?.children?.min ?? fbMin ?? 0);
    setVal(p+'ChildrenMax', data?.children?.max ?? fbMax ?? 0);
}

function val(id)        { const el = document.getElementById(id); return el ? el.value : ''; }
function setVal(id, v)  { const el = document.getElementById(id); if (el) el.value = v; }
function setText(id, v) { const el = document.getElementById(id); if (el) el.textContent = v ?? '---'; }

/* ── Setup Character Limit & Validation ── */
function setupInputValidation(inputId, countId, errorId, maxLen, pattern, errMsg) {
    const input = document.getElementById(inputId);
    const count = document.getElementById(countId);
    const err   = document.getElementById(errorId);
    if (!input) return;

    input.addEventListener('input', function() {
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
    const unitPattern = /^[^<>;'"\\`$^`{}~|\[\]]+$/;
    const descPattern = /^[^<>;'"\\`$^`{}~|\[\]]+$/;
    
    setupInputValidation('inputCreateTestName', 'countCreateTestName', 'errorCreateTestName', 100, namePattern, 'Không chứa ký tự đặc biệt nguy hiểm (< > ; \' " \\ `).');
    setupInputValidation('inputCreateUnit',     'countCreateUnit',     'errorCreateUnit',     20,  unitPattern, 'Đơn vị không phù hợp.');
    setupInputValidation('inputCreateDesc',     'countCreateDesc',     'errorCreateDesc',     255, descPattern, 'Mô tả chứa ký tự đặc biệt nguy hiểm.');
    
    setupInputValidation('editTestName',        'countEditTestName',   'errorEditTestName',   100, namePattern, 'Không chứa ký tự đặc biệt nguy hiểm.');
    setupInputValidation('editUnit',            'countEditUnit',       'errorEditUnit',       20,  unitPattern, 'Đơn vị không phù hợp.');
    setupInputValidation('editDescription',     'countEditDesc',       'errorEditDesc',       255, descPattern, 'Mô tả chứa ký tự đặc biệt nguy hiểm.');
}

/* ── CREATE MODAL ── */
function initCreateModal() {
    const modal = document.getElementById('defineTestModal');
    const form  = document.getElementById('createTestForm');
    if (!modal) return;
    document.getElementById('btnDefineTest')?.addEventListener('click', function() { 
        form?.reset(); 
        document.getElementById('countCreateTestName').textContent = '0 / 100';
        document.getElementById('countCreateUnit').textContent = '0 / 20';
        document.getElementById('countCreateDesc').textContent = '0 / 255';
        openModal(modal); 
    });
    addCloseHandlers(modal, document.getElementById('btnCloseModal'), document.getElementById('btnCancelModal'));
}

/* ── EDIT MODAL ── */
function initEditModal() {
    const modal = document.getElementById('editTestModal');
    const form  = document.getElementById('editTestForm');
    if (!modal || !form) return;
    addCloseHandlers(modal, document.getElementById('btnCloseEditModal'), document.getElementById('btnCancelEditModal'));
    document.querySelectorAll('.btn-edit').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const id = btn.dataset.id || '';
            setVal('editLabTestId',   id);
            setVal('editTestName',    btn.dataset.name        || '');
            setVal('editUnit',        btn.dataset.unit        || '');
            setVal('editRoomId',      btn.dataset.room        || '');
            setVal('editDescription', btn.dataset.description || '');
            
            fillThresholdInputs('edit', null);

            fetch('/admin/lab-tests/detail/' + id)
                .then(function(r) { return r.ok ? r.json() : Promise.reject(); })
                .then(function(d) {
                    setVal('editYoungMin',    d.youngMin ?? 0);
                    setVal('editYoungMax',    d.youngMax ?? 0);
                    setVal('editMiddleMin',   d.middleMin ?? 0);
                    setVal('editMiddleMax',   d.middleMax ?? 0);
                    setVal('editElderMin',    d.elderMin ?? 0);
                    setVal('editElderMax',    d.elderMax ?? 0);
                    setVal('editPregnantMin', d.pregnantMin ?? 0);
                    setVal('editPregnantMax', d.pregnantMax ?? 0);
                    setVal('editChildrenMin', d.childrenMin ?? 0);
                    setVal('editChildrenMax', d.childrenMax ?? 0);
                })
                .catch(function(err) { console.error('fetch threshold error', err); });

            form.action = '/admin/lab-tests/update/' + id;
            
            document.getElementById('editTestName').dispatchEvent(new Event('input'));
            document.getElementById('editUnit').dispatchEvent(new Event('input'));
            document.getElementById('editDescription').dispatchEvent(new Event('input'));
            
            openModal(modal);
        });
    });
}

/* ── VIEW MODAL ── */
function initViewModal() {
    const modal = document.getElementById('viewDetailModal');
    if (!modal) return;
    addCloseHandlers(modal, document.getElementById('btnCloseViewModal'));
    document.querySelectorAll('.btn-view').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const id = btn.dataset.id;
            if (!id) return;
            
            setText('detailId',          btn.dataset.id);
            setText('detailName',        btn.dataset.name);
            setText('detailUnit',        btn.dataset.unit);
            setText('detailRoom',        btn.dataset.room);
            setText('detailStatus',      btn.dataset.status === 'true' ? 'Hoạt động' : 'Tạm ngưng');
            setText('detailDescription', btn.dataset.description || '---');
            setText('youngMin',          '...'); setText('youngMax',          '...');
            setText('middleMin',         '...'); setText('middleMax',         '...');
            setText('elderMin',          '...'); setText('elderMax',          '...');
            setText('pregnantMin',       '...'); setText('pregnantMax',       '...');
            setText('childrenMin',       '...'); setText('childrenMax',       '...');

            openModal(modal);
            fetch('/admin/lab-tests/detail/' + id)
                .then(function(r) { return r.ok ? r.json() : Promise.reject(); })
                .then(function(d) { fillViewFromApi(d); })
                .catch(function(err) { console.error('detail error', err); });
        });
    });
}

function fillViewFromApi(d) {
    setText('detailId',          d.labTestId);
    setText('detailName',        d.testName);
    setText('detailUnit',        d.unit);
    setText('detailRoom',        d.roomId);
    setText('detailStatus',      d.status === true ? 'Hoạt động' : 'Tạm ngưng');
    setText('detailDescription', d.description || '---');
    setText('youngMin',          d.youngMin ?? 0); 
    setText('youngMax',          d.youngMax ?? 0);
    setText('middleMin',         d.middleMin ?? 0); 
    setText('middleMax',         d.middleMax ?? 0);
    setText('elderMin',          d.elderMin ?? 0); 
    setText('elderMax',          d.elderMax ?? 0);
    setText('pregnantMin',       d.pregnantMin ?? 0); 
    setText('pregnantMax',       d.pregnantMax ?? 0);
    setText('childrenMin',       d.childrenMin ?? 0); 
    setText('childrenMax',       d.childrenMax ?? 0);
}

/* ── SEARCH (Submit form sang Controller khi ấn Enter hoặc click nút Tìm kiếm) ── */
function initSearch() {
    const icon = document.getElementById('labSearchIcon');
    const form = document.getElementById('labTestSearchForm');
    if (icon && form) {
        icon.addEventListener('click', function() {
            form.submit();
        });
    }
}

/* ── PAGINATION ── */
function markAllRowsVisible() {
    document.querySelectorAll('#labTestTableBody tr.data-row').forEach(function(r) { r.dataset.filtered = 'visible'; });
}

/* ── PAGINATION ── */
function renderPagination() {
    const all     = Array.from(document.querySelectorAll('#labTestTableBody tr'));
    const visible = all.filter(function(r) { return r.dataset.filtered !== 'hidden'; });
    const total   = visible.length;
    const pages   = Math.max(1, Math.ceil(total / PAGE_SIZE));
    if (currentPage > pages) currentPage = pages;
    all.forEach(function(r) { r.style.display = 'none'; });
    visible.slice((currentPage-1)*PAGE_SIZE, currentPage*PAGE_SIZE).forEach(function(r) { r.style.display = ''; });
    updateUI(total, pages);
}

function updateUI(total, pages) {
    const info = document.getElementById('paginationInfo');
    const prev = document.getElementById('prevPageBtn');
    const next = document.getElementById('nextPageBtn');
    const nums = document.getElementById('pageNumbers');
    if (!info||!prev||!next||!nums) return;
    const s = total === 0 ? 0 : (currentPage-1)*PAGE_SIZE+1;
    const e = Math.min(currentPage*PAGE_SIZE, total);
    info.textContent = 'Hiển thị ' + s + ' – ' + e + ' / ' + total + ' xét nghiệm';
    prev.disabled = currentPage <= 1;
    next.disabled = currentPage >= pages;
    prev.onclick = function() { if (currentPage > 1)     { currentPage--; renderPagination(); } };
    next.onclick = function() { if (currentPage < pages) { currentPage++; renderPagination(); } };
    nums.innerHTML = '';
    for (let i = 1; i <= pages; i++) {
        const b = document.createElement('button');
        b.type = 'button';
        b.className = 'page-number' + (i === currentPage ? ' active' : '');
        b.textContent = i;
        b.addEventListener('click', (function(p) { return function() { currentPage = p; renderPagination(); }; })(i));
        nums.appendChild(b);
    }

    // Bind custom confirm to toggle status
    document.querySelectorAll('.btn-toggle-status').forEach(function(btn) {
        btn.onclick = function(e) {
            e.preventDefault();
            const id = btn.dataset.id;
            const name = btn.dataset.name;
            const currentStatus = btn.dataset.status === 'true';
            const actionText = currentStatus ? 'vô hiệu hóa' : 'kích hoạt';
            
            showConfirm(
                'Xác nhận trạng thái',
                `Bạn có chắc chắn muốn ${actionText} xét nghiệm "${name}"?`,
                function() {
                    const form = document.createElement('form');
                    form.method = 'post';
                    form.action = `/admin/lab-tests/toggle-status/${id}`;
                    document.body.appendChild(form);
                    form.submit();
                }
            );
        };
    });
}