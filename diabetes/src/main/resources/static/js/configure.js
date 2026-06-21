'use strict';

document.addEventListener('DOMContentLoaded', function () {
    initTabs();
    initAddRoomModal();
    initEditRoomModal();
    initSearch();
});

/* ── Modal helpers ── */
function openModal(m)  { m.classList.add('show');    document.body.style.overflow = 'hidden'; }
function closeModal(m) { m.classList.remove('show'); document.body.style.overflow = ''; }

function addClose(modal, ...triggers) {
    triggers.forEach(function (el) {
        if (el) el.addEventListener('click', function () { closeModal(modal); });
    });
    modal.addEventListener('click', function (e) { if (e.target === modal) closeModal(modal); });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal.classList.contains('show')) closeModal(modal);
    });
}

/* ── TABS ── */
function initTabs() {
    const tabs   = document.querySelectorAll('.cfg-tab');
    const panels = document.querySelectorAll('.tab-panel');

    tabs.forEach(function (tab) {
        tab.addEventListener('click', function () {
            tabs.forEach(function (t)   { t.classList.remove('active'); });
            panels.forEach(function (p) { p.classList.remove('active'); });

            tab.classList.add('active');
            const target = document.getElementById('panel' + capitalise(tab.dataset.tab));
            if (target) target.classList.add('active');

            /* update URL without reload */
            const url = new URL(window.location.href);
            url.searchParams.set('tab', tab.dataset.tab);
            window.history.replaceState({}, '', url.toString());
        });
    });
}

function capitalise(str) {
    if (!str) return '';
    return str.charAt(0).toUpperCase() + str.slice(1);
}

/* ── ADD ROOM MODAL ── */
function initAddRoomModal() {
    const modal     = document.getElementById('addRoomModal');
    const openBtn   = document.getElementById('btnAddRoom');
    const closeBtn  = document.getElementById('btnCloseAddRoom');
    const cancelBtn = document.getElementById('btnCancelAddRoom');

    if (!modal || !openBtn) return;

    openBtn.addEventListener('click', function () { openModal(modal); });
    addClose(modal, closeBtn, cancelBtn);
}

/* ── EDIT ROOM MODAL ── */
function initEditRoomModal() {
    const modal     = document.getElementById('editRoomModal');
    const form      = document.getElementById('editRoomForm');
    const closeBtn  = document.getElementById('btnCloseEditRoom');
    const cancelBtn = document.getElementById('btnCancelEditRoom');

    if (!modal || !form) return;

    addClose(modal, closeBtn, cancelBtn);

    document.querySelectorAll('.edit-room-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const id   = btn.dataset.id;
            const name = btn.dataset.name || '';
            const desc = btn.dataset.desc || '';

            document.getElementById('editRoomName').value = name;
            document.getElementById('editRoomDesc').value = desc !== 'null' ? desc : '';
            form.action = '/admin/configure/room/update/' + id;

            openModal(modal);
        });
    });
}

/* ── SEARCH (Room table) ── */
function initSearch() {
    const input = document.getElementById('roomSearch');
    if (!input) return;

    input.addEventListener('input', function () {
        const kw = input.value.toLowerCase().trim();
        document.querySelectorAll('#roomTableBody .room-row').forEach(function (row) {
            row.style.display = row.innerText.toLowerCase().includes(kw) ? '' : 'none';
        });
    });
}
