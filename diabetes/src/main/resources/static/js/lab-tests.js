'use strict';

let currentPage  = 1;
const PAGE_SIZE  = 7;
let activeFilter = 'all';
let searchKeyword = '';

document.addEventListener('DOMContentLoaded', function () {
    markAllRowsVisible();
    initTabs();
    initSearch();
    initCreateModal();
    initEditModal();
    initViewModal();
    initValidation();
    initConfirmModal();
    renderPagination();
});

/* ── Modal helpers ── */
function openModal(modal)  { modal.classList.add('show');    document.body.style.overflow = 'hidden'; }
function closeModal(modal) { modal.classList.remove('show'); document.body.style.overflow = ''; }

function addCloseHandlers(modal, ...triggers) {
    triggers.forEach(function(el) { if (el) el.addEventListener('click', function() { closeModal(modal); }); });
    modal.addEventListener('click', function(e) { if (e.target === modal) closeModal(modal); });
    document.addEventListener('keydown', function(e) { if (e.key === 'Escape' && modal.classList.contains('show')) closeModal(modal); });
}

/* ── LocalStorage ── */
function saveThreshold(id, data) { localStorage.setItem('threshold_' + id, JSON.stringify(data)); }
function getThreshold(id)        { const r = localStorage.getItem('threshold_' + id); return r ? JSON.parse(r) : null; }

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
        pregnant: { min: val(p+'PregnantMin'), max: val(p+'PregnantMax') }
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

function validateBaseForm(form, isEdit) {
    const prefix = isEdit ? 'edit' : 'Create';
    const errName = document.getElementById(isEdit ? 'errorEditTestName' : 'errorCreateTestName')?.textContent;
    const errUnit = document.getElementById(isEdit ? 'errorEditUnit' : 'errorCreateUnit')?.textContent;
    const errDesc = document.getElementById(isEdit ? 'errorEditDesc' : 'errorCreateDesc')?.textContent;

    if (errName || errUnit || errDesc) {
        alert('Dữ liệu nhập vào chứa lỗi, vui lòng kiểm tra lại.');
        return false;
    }

    const testName = form.querySelector('[name="testName"]');
    const unit     = form.querySelector('[name="unit"]');
    const roomId   = form.querySelector('[name="roomId"]');
    if (!testName?.value.trim())           return alert('Vui lòng nhập Tên xét nghiệm'), false;
    if (testName.value.trim().length > 100) return alert('Tên xét nghiệm tối đa 100 ký tự'), false;
    if (!unit?.value.trim())               return alert('Vui lòng nhập Đơn vị'), false;
    if (unit.value.trim().length > 20)     return alert('Đơn vị tối đa 20 ký tự'), false;
    if (!roomId?.value)                    return alert('Vui lòng chọn phòng xét nghiệm'), false;
    return true;
}

function validateThresholdData(data) {
    for (const g of ['young','middle','elder','pregnant']) {
        const min = Number(data[g].min), max = Number(data[g].max);
        if (data[g].min === '' || data[g].max === '') return alert('Vui lòng nhập đầy đủ Min/Max'), false;
        if (min < 0 || max < 0) return alert('Min/Max không được nhỏ hơn 0'), false;
        if (min > max)          return alert('Min không được lớn hơn Max'), false;
    }
    return true;
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
    form?.addEventListener('submit', function(e) {
        const data = buildThresholdData('create');
        if (!validateBaseForm(form, false) || !validateThresholdData(data)) { e.preventDefault(); return; }
        setVal('createMainMinValue', data.young.min);
        setVal('createMainMaxValue', data.young.max);
        localStorage.setItem('pending_create_threshold', JSON.stringify(data));
    });
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
            fillThresholdInputs('edit', getThreshold(id), btn.dataset.min, btn.dataset.max);
            form.action = '/admin/lab-tests/update/' + id;
            
            document.getElementById('editTestName').dispatchEvent(new Event('input'));
            document.getElementById('editUnit').dispatchEvent(new Event('input'));
            document.getElementById('editDescription').dispatchEvent(new Event('input'));
            
            openModal(modal);
        });
    });
    form.addEventListener('submit', function(e) {
        const data = buildThresholdData('edit');
        const id   = val('editLabTestId');
        if (!validateBaseForm(form, true) || !validateThresholdData(data)) { e.preventDefault(); return; }
        setVal('editMainMinValue', data.young.min);
        setVal('editMainMaxValue', data.young.max);
        saveThreshold(id, data);
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
            fillViewFromDataset(btn.dataset, getThreshold(id));
            openModal(modal);
            fetch('/admin/lab-tests/detail/' + id)
                .then(function(r) { return r.ok ? r.json() : Promise.reject(); })
                .then(function(d) { fillViewFromApi(d, getThreshold(id)); })
                .catch(function(err) { console.error('detail error', err); });
        });
    });
}

function fillViewFromDataset(ds, thr) {
    setText('detailId',          ds.id);
    setText('detailName',        ds.name);
    setText('detailUnit',        ds.unit);
    setText('detailRoom',        ds.room);
    setText('detailStatus',      ds.status === 'true' ? 'Hoạt động' : 'Tạm ngưng');
    setText('detailDescription', ds.description || '---');
    const t = thr || { young:{min:ds.min??0,max:ds.max??0}, middle:{min:ds.min??0,max:ds.max??0}, elder:{min:ds.min??0,max:ds.max??0}, pregnant:{min:ds.min??0,max:ds.max??0} };
    setText('youngMin',t.young.min); setText('youngMax',t.young.max);
    setText('middleMin',t.middle.min); setText('middleMax',t.middle.max);
    setText('elderMin',t.elder.min); setText('elderMax',t.elder.max);
    setText('pregnantMin',t.pregnant.min); setText('pregnantMax',t.pregnant.max);
}

function fillViewFromApi(d, thr) {
    setText('detailId',          d.labTestId);
    setText('detailName',        d.testName);
    setText('detailUnit',        d.unit);
    setText('detailRoom',        d.roomId);
    setText('detailStatus',      d.status === true ? 'Hoạt động' : 'Tạm ngưng');
    setText('detailDescription', d.description || '---');
    const t = thr || { young:{min:d.minValue??0,max:d.maxValue??0}, middle:{min:d.minValue??0,max:d.maxValue??0}, elder:{min:d.minValue??0,max:d.maxValue??0}, pregnant:{min:d.minValue??0,max:d.maxValue??0} };
    setText('youngMin',t.young.min); setText('youngMax',t.young.max);
    setText('middleMin',t.middle.min); setText('middleMax',t.middle.max);
    setText('elderMin',t.elder.min); setText('elderMax',t.elder.max);
    setText('pregnantMin',t.pregnant.min); setText('pregnantMax',t.pregnant.max);
}

/* ── TABS ── */
function initTabs() {
    const tabs = { all: document.getElementById('tabAll'), active: document.getElementById('tabActive'), inactive: document.getElementById('tabInactive') };
    if (!tabs.all) return;
    Object.entries(tabs).forEach(function([filter, tab]) {
        if (!tab) return;
        tab.addEventListener('click', function() {
            Object.values(tabs).forEach(function(t) { if (t) t.classList.remove('active'); });
            tab.classList.add('active');
            activeFilter = filter;
            currentPage  = 1;
            applyFilters();
        });
    });
}

/* ── SEARCH (Backend-side search, reset on clear) ── */
function initSearch() {
    const input = document.getElementById('tableSearch');
    if (input) {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' || e.keyCode === 13) {
                e.preventDefault();
                const status = activeFilter;
                window.location.href = `/admin/lab-tests?status=${status}&keyword=${encodeURIComponent(input.value.trim())}`;
            }
        });

        input.addEventListener('input', function() {
            if (this.value.trim() === '') {
                const status = activeFilter;
                window.location.href = `/admin/lab-tests?status=${status}`;
            }
        });

        input.addEventListener('search', function() {
            if (this.value.trim() === '') {
                const status = activeFilter;
                window.location.href = `/admin/lab-tests?status=${status}`;
            }
        });

        const icon = document.querySelector('.lab-search-icon');
        if (icon) {
            icon.addEventListener('click', function() {
                const status = activeFilter;
                window.location.href = `/admin/lab-tests?status=${status}&keyword=${encodeURIComponent(input.value.trim())}`;
            });
        }
    }
}

/* ── FILTER ENGINE ── */
function markAllRowsVisible() {
    document.querySelectorAll('#labTestTableBody tr').forEach(function(r) { r.dataset.filtered = 'visible'; });
}

function applyFilters() {
    document.querySelectorAll('#labTestTableBody tr').forEach(function(row) {
        if (!row.classList.contains('data-row')) { row.dataset.filtered = 'hidden'; return; }
        const kwOk  = searchKeyword === '' || row.innerText.toLowerCase().includes(searchKeyword);
        const tabOk = activeFilter === 'all'
            || (activeFilter === 'active'   && !!row.querySelector('.badge--active'))
            || (activeFilter === 'inactive' && !!row.querySelector('.badge--inactive'));
        row.dataset.filtered = (kwOk && tabOk) ? 'visible' : 'hidden';
    });
    renderPagination();
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