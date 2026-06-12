/**
 * Doctor Examine Room JS - Pure Thymeleaf Integration
 */

// Dynamically populated catalogs from Thymeleaf
const symptomsCatalog = (typeof rawSymptomsCatalog !== 'undefined' ? rawSymptomsCatalog : []).map(s => ({
    id: s.symptomId,
    name: s.symptomName
}));

const labTestsCatalog = (typeof rawLabTestsCatalog !== 'undefined' ? rawLabTestsCatalog : []).map(l => ({
    id: l.testId,
    name: l.testName,
    unit: l.unit,
    range: l.referenceRange,
    room: 'Laboratory'
}));

const medicationsCatalog = (typeof rawMedicationsCatalog !== 'undefined' ? rawMedicationsCatalog : []).map(m => ({
    id: m.medicationId,
    name: m.medicationName,
    form: m.form,
    concentration: m.concentration,
    route: m.route
}));

// State variables for current session
let currentPatient = null;
let viewOnlyMode = false;
let selectedSymptoms = {};
let orderedLabs = {};
let prescriptionLines = [];
let isSubmitting = false;

document.addEventListener('DOMContentLoaded', () => {
    loadSessionPatient();
    renderSymptomsGrid();
    initLabTestChecklist();
    renderOrderedLabsList();
    setupNextAppointmentMinDate();
    
    if (viewOnlyMode) {
        simulateLabResults();
        renderPrescriptionLines();
        
        // In view only mode, disable all interactable form inputs
        document.querySelectorAll('textarea, input, select, button').forEach(el => {
            // Keep common navigation, tabs, close/logout and close modal buttons active
            if (!el.classList.contains('tab-btn') && 
                !el.classList.contains('modal-close') && 
                !el.classList.contains('logout-link-btn') && 
                el.id !== 'viewHistoryBtn') {
                el.disabled = true;
            }
        });
    }

    // Intercept navigation if checkup is InProgress
    const isInProgress = thActiveExam && thActiveExam.status === 'InProgress';
    if (isInProgress) {
        const navLinks = document.querySelectorAll('.header-nav-item, .logout-link-btn, .doctor-profile-block');
        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                const href = link.getAttribute('href');
                if (href && (href.includes('dashboard') || href.includes('login') || href.includes('profile') || href === '/')) {
                    e.preventDefault();
                    showNavigationBlockedModal();
                }
            });
        });

        const cancelForm = document.getElementById('cancelForm');
        if (cancelForm) {
            cancelForm.addEventListener('submit', () => {
                isSubmitting = true;
            });
        }
    }
});

function formatLocalTime(timeStr) {
    if (!timeStr) return '';
    const parts = timeStr.split(':');
    if (parts.length < 2) return timeStr;
    let hrs = parseInt(parts[0], 10);
    const mins = parts[1];
    const ampm = hrs >= 12 ? 'PM' : 'AM';
    hrs = hrs % 12;
    hrs = hrs ? hrs : 12;
    return `${hrs.toString().padStart(2, '0')}:${mins} ${ampm}`;
}

// Load patient dynamic details
function loadSessionPatient() {
    viewOnlyMode = (typeof thViewOnly !== 'undefined' ? thViewOnly : false);

    if (typeof thPatient !== 'undefined' && thPatient) {
        let ageStr = 'N/A';
        if (thPatient.dob) {
            const birthDate = new Date(thPatient.dob);
            const today = new Date();
            let age = today.getFullYear() - birthDate.getFullYear();
            const m = today.getMonth() - birthDate.getMonth();
            if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
                age--;
            }
            ageStr = age;
        }

        let bmiVal = 'N/A';
        if (thPatient.weight && thPatient.height) {
            const h_m = thPatient.height / 100.0;
            const bmi = thPatient.weight / (h_m * h_m);
            let bmiCategory = 'Normal';
            if (bmi >= 25 && bmi < 30) bmiCategory = 'Overweight';
            else if (bmi >= 30) bmiCategory = 'Obese';
            else if (bmi < 18.5) bmiCategory = 'Underweight';
            bmiVal = `${bmi.toFixed(1)} (${bmiCategory})`;
        }

        currentPatient = {
            id: thPatient.userId,
            name: thPatient.fullName,
            age: ageStr,
            gender: thPatient.gender !== null ? (thPatient.gender ? 'Female' : 'Male') : 'N/A',
            bloodGroup: thPatient.bloodgroup || 'N/A',
            height: thPatient.height || 0,
            weight: thPatient.weight || 0,
            bmi: bmiVal,
            allergies: thPatient.allergyNotes || 'No known drug allergies',
            history: thPatient.permanentMedicalHistory || 'No recorded medical history',
            routine: {
                breakfast: thRoutine && thRoutine.breakfastTime ? formatLocalTime(thRoutine.breakfastTime) : '07:00 AM',
                lunch: thRoutine && thRoutine.lunchTime ? formatLocalTime(thRoutine.lunchTime) : '11:00 AM',
                dinner: thRoutine && thRoutine.dinnerTime ? formatLocalTime(thRoutine.dinnerTime) : '06:00 PM',
                sleep: thRoutine && thRoutine.sleepTime ? formatLocalTime(thRoutine.sleepTime) : '10:00 PM',
                wake: thRoutine && thRoutine.wakeUpTime ? formatLocalTime(thRoutine.wakeUpTime) : '06:00 AM'
            }
        };
    }

    // Load active exam / view only properties
    if (viewOnlyMode && typeof thLastExam !== 'undefined' && thLastExam) {
        document.getElementById('examDiagnosis').value = thLastExam.diagnosisNote || '';
        document.getElementById('examHistory').value = thLastExam.medicalHistory || '';
        document.getElementById('examNextDate').value = thLastExam.nextAppointment ? thLastExam.nextAppointment.substring(0, 10) : '';

        if (thLastExam.treatmentPlan) {
            document.getElementById('planGoal').value = thLastExam.treatmentPlan.treatmentGoal || '';
            document.getElementById('planDiet').value = thLastExam.treatmentPlan.dietPlan || '';
            document.getElementById('planExercise').value = thLastExam.treatmentPlan.exercisePlan || '';
            document.getElementById('planGlucose').value = thLastExam.treatmentPlan.glucoseMonitoringPlan || '';
            document.getElementById('planMedication').value = thLastExam.treatmentPlan.medicationPlan || '';
        }

        selectedSymptoms = {};
        if (typeof thChosenSymptomIds !== 'undefined' && thChosenSymptomIds) {
            thChosenSymptomIds.forEach(id => {
                selectedSymptoms[id] = '';
            });
        }

        orderedLabs = {};
        if (typeof thLastExamLabResults !== 'undefined' && thLastExamLabResults) {
            thLastExamLabResults.forEach(res => {
                const test = labTestsCatalog.find(l => l.name === res.labTest.testName);
                if (test) {
                    orderedLabs[test.id] = true;
                }
            });
        }

        prescriptionLines = [];
        if (typeof thLastExamPrescriptionDetails !== 'undefined' && thLastExamPrescriptionDetails) {
            prescriptionLines = thLastExamPrescriptionDetails.map(p => {
                const tName = p.prescriptionTimings && p.prescriptionTimings.length > 0 ? p.prescriptionTimings[0].timing.timingName : 'Breakfast Time (07:30 AM)';
                return {
                    medId: p.medication.medicationId,
                    name: p.medication.medicationName,
                    concentration: p.medication.concentration,
                    form: p.medication.form,
                    dosage: p.dosage,
                    duration: p.durationDays,
                    quantity: p.totalQuantity,
                    timing: tName, // standard timing mapped to options
                    timingText: tName
                };
            });
        }
    }
}

// Setup min date for next appointment (today + 1 day)
function setupNextAppointmentMinDate() {
    const nextDateInput = document.getElementById('examNextDate');
    if (nextDateInput) {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const yyyy = tomorrow.getFullYear();
        const mm = String(tomorrow.getMonth() + 1).padStart(2, '0');
        const dd = String(tomorrow.getDate()).padStart(2, '0');
        nextDateInput.min = `${yyyy}-${mm}-${dd}`;
    }
}

// Render Symptoms List
function renderSymptomsGrid() {
    const grid = document.getElementById('symptomsGrid');
    if (!grid) return;

    grid.innerHTML = '';
    symptomsCatalog.forEach(s => {
        const isChecked = selectedSymptoms[s.id] !== undefined;
        const div = document.createElement('div');
        div.className = `symptom-card ${isChecked ? 'selected' : ''}`;
        div.setAttribute('data-id', s.id);
        div.innerHTML = `
            <div class="symptom-card-header">
                <input type="checkbox" name="symptomIds" id="chk-${s.id}" value="${s.id}" onchange="toggleSymptom('${s.id}')" ${isChecked ? 'checked' : ''}>
                <label for="chk-${s.id}">${s.name}</label>
            </div>
            <div class="symptom-comment-box">
                <input type="text" placeholder="Add specific notes (severity, duration)..." id="comment-${s.id}">
            </div>
        `;
        grid.appendChild(div);
    });
}

function toggleSymptom(id) {
    const card = document.querySelector(`.symptom-card[data-id="${id}"]`);
    const chk = document.getElementById(`chk-${id}`);

    if (chk.checked) {
        card.classList.add('selected');
        selectedSymptoms[id] = '';
    } else {
        card.classList.remove('selected');
        delete selectedSymptoms[id];
    }
}

// Initialize lab checklist inside modal
function initLabTestChecklist() {
    const checklist = document.getElementById('labChecklist');
    if (!checklist) return;

    checklist.innerHTML = '';
    labTestsCatalog.forEach(l => {
        const div = document.createElement('div');
        div.className = 'lab-checkbox-item';
        div.setAttribute('data-id', l.id);
        div.setAttribute('data-name', l.name.toLowerCase());

        div.onclick = (e) => {
            if (e.target.tagName !== 'INPUT') {
                const chk = document.getElementById(`modal-chk-${l.id}`);
                if (chk) chk.checked = !chk.checked;
            }
        };

        div.innerHTML = `
            <input type="checkbox" id="modal-chk-${l.id}" value="${l.id}">
            <label for="modal-chk-${l.id}" onclick="event.stopPropagation()">
                <span class="lab-checkbox-name">${l.name}</span>
                <span class="lab-checkbox-room">${l.room}</span>
            </label>
        `;
        checklist.appendChild(div);
    });
}

// Show/Hide Laboratory Test Modal
function showLabTestModal() {
    if (viewOnlyMode) return;

    document.getElementById('labSearch').value = '';
    filterLabTestsInModal();

    labTestsCatalog.forEach(l => {
        const chk = document.getElementById(`modal-chk-${l.id}`);
        if (chk) {
            chk.checked = !!orderedLabs[l.id];
        }
    });

    const modal = document.getElementById('labTestModal');
    if (modal) modal.classList.add('open');
}

function closeLabTestModal() {
    const modal = document.getElementById('labTestModal');
    if (modal) modal.classList.remove('open');
}

// Filter lab checklist in modal in real-time
function filterLabTestsInModal() {
    const query = document.getElementById('labSearch').value.toLowerCase().trim();
    const items = document.querySelectorAll('.lab-checkbox-item');

    items.forEach(item => {
        const name = item.getAttribute('data-name');
        if (!query || name.includes(query)) {
            item.style.display = 'flex';
        } else {
            item.style.display = 'none';
        }
    });
}

// Confirm Lab Orders on modal save
function confirmLabOrders() {
    if (viewOnlyMode) return;

    orderedLabs = {};
    labTestsCatalog.forEach(l => {
        const chk = document.getElementById(`modal-chk-${l.id}`);
        if (chk && chk.checked) {
            orderedLabs[l.id] = true;
        }
    });

    renderOrderedLabsList();
    closeLabTestModal();
    simulateLabProcessing();
}

// Render summary of ordered labs in page
function renderOrderedLabsList() {
    const container = document.getElementById('orderedLabsSummary');
    if (!container) return;

    const oldTags = container.querySelectorAll('.ordered-lab-tag');
    oldTags.forEach(t => t.remove());

    const selectedIds = Object.keys(orderedLabs);
    const emptyMsg = document.getElementById('emptyLabs');

    if (selectedIds.length === 0) {
        if (emptyMsg) emptyMsg.style.display = 'flex';
        return;
    }

    if (emptyMsg) emptyMsg.style.display = 'none';

    selectedIds.forEach(id => {
        const test = labTestsCatalog.find(l => l.id === id);
        if (!test) return;

        const div = document.createElement('div');
        div.className = 'ordered-lab-tag';
        div.innerHTML = `
            <div class="lab-tag-info">
                <i class="fas fa-flask" style="color: var(--doctor-primary);"></i>
                <strong>${test.name}</strong>
                <span class="lab-tag-room">${test.room}</span>
            </div>
            <button type="button" class="btn-remove-lab" onclick="removeLabOrder('${test.id}')" title="Remove Order"><i class="fas fa-times"></i></button>
        `;
        container.appendChild(div);
    });
}

// Remove lab order from tag list on main page
function removeLabOrder(id) {
    if (viewOnlyMode) return;
    delete orderedLabs[id];
    renderOrderedLabsList();

    if (!isLabProcessing) {
        simulateLabResults();
    }
}

let isLabProcessing = false;

// Simulate the waiting time for the lab
function simulateLabProcessing() {
    const tbody = document.getElementById('labResultsTableBody');
    if (!tbody) return;

    const selectedIds = Object.keys(orderedLabs);
    if (selectedIds.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--doctor-text-muted); padding: 30px;">Please select one or more tests from the catalog above to view results</td></tr>`;
        return;
    }

    isLabProcessing = true;

    tbody.innerHTML = `
        <tr>
            <td colspan="4" style="text-align: center; padding: 40px 20px;">
                <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 15px;">
                    <i class="fa-solid fa-spinner fa-spin fa-2x" style="color: var(--doctor-primary);"></i>
                    <strong style="color: var(--doctor-text-main); font-size: 1.1rem;">Processing samples at the Laboratory...</strong>
                    <span style="color: var(--doctor-text-muted); font-size: 0.9rem;">Please wait a moment</span>
                </div>
            </td>
        </tr>
    `;

    setTimeout(() => {
        isLabProcessing = false;
        if (Object.keys(orderedLabs).length > 0) {
            simulateLabResults();
        } else {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--doctor-text-muted); padding: 30px;">Please select one or more tests from the catalog above to view results</td></tr>`;
        }
    }, 2000); // reduced processing time to 2s for premium UI responsiveness
}

// Laboratory simulator mapping results
function simulateLabResults() {
    const tbody = document.getElementById('labResultsTableBody');
    if (!tbody) return;

    if (viewOnlyMode) {
        tbody.innerHTML = '';
        if (typeof thLastExamLabResults !== 'undefined' && thLastExamLabResults && thLastExamLabResults.length > 0) {
            thLastExamLabResults.forEach(l => {
                const tr = document.createElement('tr');
                const flagClass = l.flag === 'HIGH' ? 'flag-high' : 'flag-normal';
                tr.innerHTML = `
                    <td><strong>${l.labTest.testName}</strong></td>
                    <td><span style="font-weight:500; color: var(--doctor-text-muted);">${l.referenceRange} ${l.labTest.unit}</span></td>
                    <td><strong>${l.resultValue} ${l.labTest.unit}</strong></td>
                    <td><span class="flag-badge ${flagClass}">${l.flag}</span></td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--doctor-text-muted); padding: 30px;">No laboratory tests ordered in this session</td></tr>`;
        }
        return;
    }

    const selectedIds = Object.keys(orderedLabs);
    if (selectedIds.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--doctor-text-muted); padding: 30px;">Please select one or more tests from the catalog above to view results</td></tr>`;
        return;
    }

    tbody.innerHTML = '';
    selectedIds.forEach(id => {
        const test = labTestsCatalog.find(l => l.id === id);
        if (!test) return;

        let val = 4.8;
        let flag = 'NORMAL';
        let flagClass = 'flag-normal';

        const rand = new RandomVal();
        if (test.name.toLowerCase().includes("fasting blood glucose")) {
            val = (4.0 + rand.next() * 5.5).toFixed(1);
            if (val > 5.6) { flag = 'HIGH'; flagClass = 'flag-high'; }
        } else if (test.name.toLowerCase().includes("hba1c")) {
            val = (4.5 + rand.next() * 4.0).toFixed(1);
            if (val > 5.6) { flag = 'HIGH'; flagClass = 'flag-high'; }
        } else if (test.name.toLowerCase().includes("ogtt")) {
            val = (6.5 + rand.next() * 5.0).toFixed(1);
            if (val >= 7.8) { flag = 'HIGH'; flagClass = 'flag-high'; }
        } else if (test.name.toLowerCase().includes("creatinine")) {
            val = Math.round(60 + rand.next() * 75);
            if (val > 115) { flag = 'HIGH'; flagClass = 'flag-high'; }
        } else if (test.name.toLowerCase().includes("cholesterol")) {
            val = (4.0 + rand.next() * 2.5).toFixed(1);
            if (val >= 5.2) { flag = 'HIGH'; flagClass = 'flag-high'; }
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${test.name}</strong></td>
            <td><span style="font-weight:500; color: var(--doctor-text-muted);">${test.range} ${test.unit}</span></td>
            <td><strong>${val} ${test.unit}</strong></td>
            <td><span class="flag-badge ${flagClass}">${flag}</span></td>
        `;
        tbody.appendChild(tr);
    });
}

class RandomVal {
    constructor() {
        this.seed = 42; // static seed for relative consistency
    }
    next() {
        let x = Math.sin(this.seed++) * 10000;
        return x - Math.floor(x);
    }
}

let pendingTabTransition = null;

// Diagnostic Switch Tab Logic
function switchTab(evt, tabId) {
    if (!viewOnlyMode) {
        if (tabId === 'labs-tab' || tabId === 'prescription-tab') {
            const history = document.getElementById('examHistory').value.trim();
            const diagnosis = document.getElementById('examDiagnosis').value.trim();
            const checkedSymptoms = document.querySelectorAll('#symptomsGrid input:checked').length;
            if (!history || !diagnosis || checkedSymptoms === 0) {
                showToast('Please complete Symptoms, History, and Diagnosis first.', 'warning');
                return;
            }
        }

        if (tabId === 'prescription-tab') {
            const orderedCount = Object.keys(orderedLabs).length;
            if (orderedCount === 0) {
                pendingTabTransition = {
                    tabId: tabId,
                    targetBtn: evt ? evt.currentTarget : null
                };
                const skipModal = document.getElementById('skipLabConfirmModal');
                if (skipModal) {
                    skipModal.classList.add('open');
                }
                return;
            }
            if (isLabProcessing) {
                showToast('Please wait for laboratory tests to finish processing.', 'warning');
                return;
            }
        }
    }

    executeTabSwitch(evt ? evt.currentTarget : null, tabId);
}

const tabIndexMap = {
    'symptoms-tab': 0,
    'labs-tab': 1,
    'prescription-tab': 2
};

function executeTabSwitch(targetBtn, tabId) {
    const tabcontents = document.getElementsByClassName('tab-content');
    for (let i = 0; i < tabcontents.length; i++) {
        tabcontents[i].classList.remove('active');
    }

    const tablinks = document.getElementsByClassName('tab-btn');
    for (let i = 0; i < tablinks.length; i++) {
        tablinks[i].classList.remove('active');
    }

    document.getElementById(tabId).classList.add('active');
    if (targetBtn) {
        targetBtn.classList.add('active');
    } else {
        const btns = document.querySelectorAll('.tab-btn');
        const idx = tabIndexMap[tabId];
        if (btns[idx]) {
            btns[idx].classList.add('active');
        }
    }
}

function closeSkipLabModal() {
    const skipModal = document.getElementById('skipLabConfirmModal');
    if (skipModal) skipModal.classList.remove('open');
    pendingTabTransition = null;
}

function confirmSkipLab() {
    if (pendingTabTransition) {
        const targetTabId = pendingTabTransition.tabId;
        const targetBtn = pendingTabTransition.targetBtn;
        pendingTabTransition = null;

        const skipModal = document.getElementById('skipLabConfirmModal');
        if (skipModal) skipModal.classList.remove('open');

        executeTabSwitch(targetBtn, targetTabId);
    }
}

let editingMedIndex = -1;

function showMedicationModal() {
    const modal = document.getElementById('medicationModal');
    if (modal) modal.classList.add('open');
    document.getElementById('medSearch').value = '';

    editingMedIndex = -1;
    selectedMed = null;
    document.getElementById('medDetailFields').style.display = 'none';
    const btn = document.getElementById('addMedBtn');
    if (btn) btn.textContent = 'Add to Prescription';

    document.getElementById('medDosage').value = '';
    document.getElementById('medDuration').value = '30';
    document.getElementById('medQuantity').value = '30';

    document.getElementById('medSearch').focus();
    filterMedications();
}

function closeMedicationModal() {
    const modal = document.getElementById('medicationModal');
    if (modal) modal.classList.remove('open');
}

// Autocomplete and filtering for medication dropdown
let selectedMed = null;
function filterMedications() {
    const query = document.getElementById('medSearch').value.toLowerCase();
    const list = document.getElementById('medAutocompleteList');
    if (!list) return;

    list.innerHTML = '';
    const filtered = medicationsCatalog.filter(m => m.name.toLowerCase().includes(query) || m.concentration.toLowerCase().includes(query));

    if (filtered.length > 0) {
        list.style.display = 'block';
        filtered.forEach(m => {
            const item = document.createElement('div');
            item.className = 'autocomplete-item';
            item.innerHTML = `
                <span><strong>${m.name}</strong> (${m.concentration})</span>
                <span class="autocomplete-form">${m.form} - ${m.route}</span>
            `;
            item.onclick = () => selectMedication(m);
            list.appendChild(item);
        });
    } else {
        list.style.display = 'none';
    }
}

function selectMedication(med) {
    selectedMed = med;
    document.getElementById('medSearch').value = `${med.name} (${med.concentration})`;
    document.getElementById('medAutocompleteList').style.display = 'none';

    const detailFields = document.getElementById('medDetailFields');
    if (detailFields) detailFields.style.display = 'grid';

    document.getElementById('medDosage').focus();
}

function addMedicationLine() {
    if (!selectedMed) {
        alert('Please search and select a medication from the dropdown');
        return;
    }

    const dosage = document.getElementById('medDosage').value.trim();
    const duration = document.getElementById('medDuration').value;
    const quantity = document.getElementById('medQuantity').value;
    const timingSelect = document.getElementById('medTiming');
    const timing = timingSelect.value;
    const timingText = timingSelect.options[timingSelect.selectedIndex].text;

    if (!dosage) {
        alert('Please specify dosage rate');
        return;
    }

    const line = {
        medId: selectedMed.id,
        name: selectedMed.name,
        concentration: selectedMed.concentration,
        form: selectedMed.form,
        dosage: dosage,
        duration: duration,
        quantity: quantity,
        timing: timing,
        timingText: timingText
    };

    if (editingMedIndex >= 0) {
        prescriptionLines[editingMedIndex] = line;
        editingMedIndex = -1;
    } else {
        prescriptionLines.push(line);
    }

    renderPrescriptionLines();
    closeMedicationModal();
    selectedMed = null;
}

function removePrescriptionLine(index) {
    prescriptionLines.splice(index, 1);
    renderPrescriptionLines();
}

function editPrescriptionLine(index) {
    const line = prescriptionLines[index];
    if (!line) return;

    selectedMed = medicationsCatalog.find(m => m.id === line.medId);
    editingMedIndex = index;

    const modal = document.getElementById('medicationModal');
    if (modal) modal.classList.add('open');

    document.getElementById('medSearch').value = selectedMed ? `${selectedMed.name} (${selectedMed.concentration})` : line.name;
    document.getElementById('medAutocompleteList').style.display = 'none';

    document.getElementById('medDosage').value = line.dosage;
    document.getElementById('medDuration').value = line.duration;
    document.getElementById('medQuantity').value = line.quantity;
    document.getElementById('medTiming').value = line.timing;

    const detailFields = document.getElementById('medDetailFields');
    if (detailFields) detailFields.style.display = 'grid';

    const btn = document.getElementById('addMedBtn');
    if (btn) btn.textContent = 'Update Prescription';
}

function renderPrescriptionLines() {
    const list = document.getElementById('prescriptionLines');
    const emptyMsg = document.getElementById('emptyPrescription');
    if (!list) return;

    const items = list.querySelectorAll('.prescription-line');
    items.forEach(i => i.remove());

    if (prescriptionLines.length === 0) {
        if (emptyMsg) emptyMsg.style.display = 'flex';
        return;
    }

    if (emptyMsg) emptyMsg.style.display = 'none';

    prescriptionLines.forEach((line, index) => {
        const div = document.createElement('div');
        div.className = 'prescription-line';
        div.innerHTML = `
            <div class="presc-med-name">${line.name} (${line.concentration})</div>
            <div class="presc-detail-item">
                <span class="presc-lbl">Dosage</span>
                <span class="presc-val">${line.dosage}</span>
            </div>
            <div class="presc-detail-item">
                <span class="presc-lbl">Duration</span>
                <span class="presc-val">${line.duration} Days</span>
            </div>
            <div class="presc-detail-item">
                <span class="presc-lbl">Qty</span>
                <span class="presc-val">${line.quantity} pcs</span>
            </div>
            <div class="presc-detail-item">
                <span class="presc-lbl">Intake Timing</span>
                <span class="presc-val" style="font-size: 0.7rem;">${line.timingText}</span>
            </div>
            <div style="display: flex; gap: 4px; justify-content: flex-end;">
                <button type="button" class="btn-remove-line" style="color: var(--doctor-primary);" onclick="editPrescriptionLine(${index})" title="Edit"><i class="fas fa-pen"></i></button>
                <button type="button" class="btn-remove-line" onclick="removePrescriptionLine(${index})" title="Remove"><i class="fas fa-trash"></i></button>
            </div>
        `;
        list.appendChild(div);
    });
}

// Complete Clinical Checkup
function saveExam() {
    isSubmitting = true;
    if (viewOnlyMode) {
        showToast('This checkup session is read-only. Returning to dashboard.', 'warning');
        setTimeout(() => window.location.href = '/doctor/dashboard', 1500);
        return;
    }

    const diagnosis = document.getElementById('examDiagnosis').value.trim();
    if (!diagnosis) {
        showToast('Please fill out the Diagnosis Note before completing checkup.', 'error');
        isSubmitting = false;
        return;
    }

    // Validate that at least one field of the Treatment Plan is filled
    const planGoal = document.getElementById('planGoal').value.trim();
    const planDiet = document.getElementById('planDiet').value.trim();
    const planExercise = document.getElementById('planExercise').value.trim();
    const planGlucose = document.getElementById('planGlucose').value.trim();
    const planMedication = document.getElementById('planMedication').value.trim();

    if (!planGoal && !planDiet && !planExercise && !planGlucose && !planMedication) {
        showToast('At least 1 field of the Treatment Plan is required to complete.', 'error');
        isSubmitting = false;
        if (!document.getElementById('prescription-tab').classList.contains('active')) {
             switchTab({currentTarget: document.querySelectorAll('.tab-btn')[2]}, 'prescription-tab');
        }
        return;
    }

    // Validate if Prescription is filled
    const prescribedCount = prescriptionLines.length;
    if (prescribedCount === 0) {
        showToast('Please prescribe at least 1 medicine before completing.', 'warning');
        isSubmitting = false;
        if (!document.getElementById('prescription-tab').classList.contains('active')) {
             switchTab({currentTarget: document.querySelectorAll('.tab-btn')[2]}, 'prescription-tab');
        }
        return;
    }

    // Serialize prescription list to hidden JSON input
    const prescriptionList = prescriptionLines.map(line => {
        return {
            medId: line.medId,
            dosage: line.dosage,
            duration: parseInt(line.duration),
            quantity: parseInt(line.quantity),
            timingText: line.timingText
        };
    });
    
    document.getElementById('prescriptionJson').value = JSON.stringify(prescriptionList);

    const form = document.getElementById('examineForm');
    
    // Clear and append selected labTestIds as hidden inputs to ensure they are submitted
    form.querySelectorAll('input[name="labTestIds"]').forEach(el => el.remove());
    Object.keys(orderedLabs).forEach(id => {
        const labInput = document.createElement('input');
        labInput.type = 'hidden';
        labInput.name = 'labTestIds';
        labInput.value = id;
        form.appendChild(labInput);
    });

    showToast(`Saving Consultation and Clinical Checkup...`, 'success');
    setTimeout(() => {
        form.submit();
    }, 1000);
}

function cancelExam() {
    if (viewOnlyMode) {
        window.location.href = '/doctor/dashboard';
        return;
    }
    const reasonInput = document.getElementById('cancelReason');
    if (reasonInput) reasonInput.value = '';
    const modal = document.getElementById('cancelConfirmModal');
    if (modal) modal.classList.add('open');
}

function closeCancelModal() {
    const modal = document.getElementById('cancelConfirmModal');
    if (modal) modal.classList.remove('open');
}

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
        setTimeout(() => { if (toast.parentNode) toast.remove(); }, 300);
    });

    setTimeout(() => toast.classList.add('show'), 10);

    setTimeout(() => {
        if (toast.parentNode) {
            toast.classList.remove('show');
            setTimeout(() => { if (toast.parentNode) toast.remove(); }, 300);
        }
    }, 4000);
}

function goToHistory() {
    isSubmitting = true;
    if (currentPatient) {
        window.location.href = `/doctor/examine/patients?patientId=${currentPatient.id}`;
    }
}

function showNavigationBlockedModal() {
    const modal = document.getElementById('navigationBlockedModal');
    if (modal) modal.classList.add('open');
}

function closeNavigationBlockedModal() {
    const modal = document.getElementById('navigationBlockedModal');
    if (modal) modal.classList.remove('open');
}

// Warn when leaving page with unsaved changes
window.addEventListener('beforeunload', (e) => {
    if (!viewOnlyMode && !isSubmitting) {
        const overlay = document.getElementById('examStartOverlay');
        const isExamActive = overlay && overlay.style.display === 'none';
        if (isExamActive) {
            e.preventDefault();
            e.returnValue = 'You have an active examination in progress. Are you sure you want to leave?';
            return e.returnValue;
        }
    }
});
