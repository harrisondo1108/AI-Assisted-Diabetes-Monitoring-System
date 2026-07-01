'use strict';

let roomPage = 1, timingPage = 1;
const PAGE_SIZE = 8;
let roomKw = '', timingKw = '';

document.addEventListener('DOMContentLoaded', function () {
    initTabs();
    initSearch();
    initRoomModals();
    initTimingModals();
    markVisible('#roomTableBody',   '.room-row');
    markVisible('#timingTableBody', '.timing-row');
    renderPagination('#roomTableBody',   '.room-row',   roomPage,   roomKw,
                     'paginationInfo',   'prevPageBtn',    'nextPageBtn',    'pageNumbers',       'phòng',
                     function(p){ roomPage=p; });
    renderPagination('#timingTableBody', '.timing-row', timingPage, timingKw,
                     'timingPaginationInfo','timingPrevBtn','timingNextBtn','timingPageNumbers','timing',
                     function(p){ timingPage=p; });
});

/* ── Modal helpers ── */
function openModal(m)  { m.classList.add('show');    document.body.style.overflow='hidden'; }
function closeModal(m) { m.classList.remove('show'); document.body.style.overflow=''; }
function addClose(modal) {
    Array.prototype.slice.call(arguments,1).forEach(function(el){
        if(el) el.addEventListener('click',function(){ closeModal(modal); });
    });
    modal.addEventListener('click',function(e){ if(e.target===modal) closeModal(modal); });
    document.addEventListener('keydown',function(e){
        if(e.key==='Escape'&&modal.classList.contains('show')) closeModal(modal);
    });
}

/* ── TABS ── */
function initTabs() {
    var tabs   = document.querySelectorAll('.tab');
    var panels = document.querySelectorAll('.tab-panel');
    tabs.forEach(function(tab){
        tab.addEventListener('click',function(){
            tabs.forEach(function(t){ t.classList.remove('active'); });
            panels.forEach(function(p){ p.classList.remove('active'); });
            tab.classList.add('active');
            var panel = document.getElementById('panel'+cap(tab.dataset.tab));
            if(panel) panel.classList.add('active');
        });
    });
}
function cap(s){ return s ? s.charAt(0).toUpperCase()+s.slice(1) : ''; }

function initSearch() {
    var rInput = document.getElementById('roomSearch');
    var tInput = document.getElementById('timingSearch');
    
    if(rInput) {
        rInput.addEventListener('input', function(){
            roomKw = rInput.value.toLowerCase().trim();
            roomPage = 1;
            applyFilter('#roomTableBody', '.room-row', roomKw);
            renderPagination('#roomTableBody', '.room-row', roomPage, roomKw,
                             'paginationInfo', 'prevPageBtn', 'nextPageBtn', 'pageNumbers', 'phòng',
                             function(p){ roomPage = p; });
        });
    }
    
    if(tInput) {
        tInput.addEventListener('input', function(){
            timingKw = tInput.value.toLowerCase().trim();
            timingPage = 1;
            applyFilter('#timingTableBody', '.timing-row', timingKw);
            renderPagination('#timingTableBody', '.timing-row', timingPage, timingKw,
                'timingPaginationInfo','timingPrevBtn','timingNextBtn','timingPageNumbers','timing',
                function(p){ timingPage = p; });
        });
    }
}

function markVisible(tbody, rowSel){
    document.querySelectorAll(tbody+' '+rowSel).forEach(function(r){ r.dataset.filtered='visible'; });
}
function applyFilter(tbody, rowSel, kw){
    document.querySelectorAll(tbody+' '+rowSel).forEach(function(r){
        r.dataset.filtered = (!kw || r.innerText.toLowerCase().includes(kw)) ? 'visible' : 'hidden';
    });
}

/* ── PAGINATION ── */
function renderPagination(tbody, rowSel, curPage, kw, infoId, prevId, nextId, numsId, unit, setPage){
    var all     = Array.from(document.querySelectorAll(tbody+' '+rowSel));
    var visible = all.filter(function(r){ return r.dataset.filtered !== 'hidden'; });
    var total   = visible.length;
    var pages   = Math.max(1, Math.ceil(total/PAGE_SIZE));
    if(curPage > pages) curPage = pages;

    all.forEach(function(r){ r.style.display='none'; });
    visible.slice((curPage-1)*PAGE_SIZE, curPage*PAGE_SIZE).forEach(function(r){ r.style.display=''; });

    var info = document.getElementById(infoId);
    var prev = document.getElementById(prevId);
    var next = document.getElementById(nextId);
    var nums = document.getElementById(numsId);
    if(!info||!prev||!next||!nums) return;

    var s = total===0 ? 0 : (curPage-1)*PAGE_SIZE+1;
    var e = Math.min(curPage*PAGE_SIZE, total);
    info.textContent = 'Hiển thị '+s+' – '+e+' / '+total+' '+unit;
    prev.disabled = curPage<=1;
    next.disabled = curPage>=pages;
    prev.onclick = function(){ if(curPage>1){ setPage(curPage-1); reDraw(tbody,rowSel,curPage-1,kw,infoId,prevId,nextId,numsId,unit,setPage); } };
    next.onclick = function(){ if(curPage<pages){ setPage(curPage+1); reDraw(tbody,rowSel,curPage+1,kw,infoId,prevId,nextId,numsId,unit,setPage); } };

    nums.innerHTML='';
    for(var i=1;i<=pages;i++){
        (function(p){
            var btn=document.createElement('button');
            btn.type='button'; btn.className='page-number'+(p===curPage?' active':'');
            btn.textContent=p;
            btn.addEventListener('click',function(){ setPage(p); reDraw(tbody,rowSel,p,kw,infoId,prevId,nextId,numsId,unit,setPage); });
            nums.appendChild(btn);
        })(i);
    }
}
function reDraw(tbody,rowSel,p,kw,infoId,prevId,nextId,numsId,unit,setPage){
    renderPagination(tbody,rowSel,p,kw,infoId,prevId,nextId,numsId,unit,setPage);
}

/* ── ROOM MODALS ── */
function initRoomModals(){
    var addModal  = document.getElementById('addRoomModal');
    var editModal = document.getElementById('editRoomModal');
    var editForm  = document.getElementById('editRoomForm');
    if(addModal){
        var open = document.getElementById('btnAddRoom');
        if(open) open.addEventListener('click',function(){ openModal(addModal); });
        addClose(addModal, document.getElementById('btnCloseAddRoom'), document.getElementById('btnCancelAddRoom'));
    }
    if(editModal&&editForm){
        addClose(editModal, document.getElementById('btnCloseEditRoom'), document.getElementById('btnCancelEditRoom'));
        document.querySelectorAll('.edit-room-btn').forEach(function(btn){
            btn.addEventListener('click',function(){
                document.getElementById('editRoomName').value = btn.dataset.name||'';
                document.getElementById('editRoomDesc').value = (btn.dataset.desc&&btn.dataset.desc!=='null') ? btn.dataset.desc : '';
                editForm.action = '/admin/configure/room/update/'+btn.dataset.id;
                openModal(editModal);
            });
        });
    }
}

/* ── TIMING MODALS ── */
function initTimingModals(){
    var addModal  = document.getElementById('addTimingModal');
    var editModal = document.getElementById('editTimingModal');
    var editForm  = document.getElementById('editTimingForm');
    if(addModal){
        var open = document.getElementById('btnAddTiming');
        if(open) open.addEventListener('click',function(){ openModal(addModal); });
        addClose(addModal, document.getElementById('btnCloseAddTiming'), document.getElementById('btnCancelAddTiming'));
    }
    if(editModal&&editForm){
        addClose(editModal, document.getElementById('btnCloseEditTiming'), document.getElementById('btnCancelEditTiming'));
        document.querySelectorAll('.edit-timing-btn').forEach(function(btn){
            btn.addEventListener('click',function(){
                document.getElementById('editTimingName').value = btn.dataset.name||'';
                editForm.action = '/admin/configure/timing/update/'+btn.dataset.id;
                openModal(editModal);
            });
        });
    }
}
