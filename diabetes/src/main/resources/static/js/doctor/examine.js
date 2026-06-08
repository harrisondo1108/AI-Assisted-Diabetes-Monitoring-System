// Mock Patients Detailed Records database
const patientRecords = {
    'P012932': {
        id: 'P012932', name: 'Nguyen Van A', age: 45, gender: 'Male', bloodGroup: 'O+', height: 172, weight: 71.5,
        allergies: 'Penicillin', history: 'Type 2 Diabetes diagnosed 3 years ago. Hypertension.',
        routine: { breakfast: '07:30 AM', lunch: '12:15 PM', dinner: '07:00 PM', sleep: '10:00 PM', wake: '06:00 AM' },

        pastDiagnosis: 'Type 2 Diabetes mellitus under control. Stage 1 Hypertension.'
    },
    'P023945': {
        id: 'P023945', name: 'Tran Thi B', age: 62, gender: 'Female', bloodGroup: 'A+', height: 156, weight: 58.2,
        allergies: 'Aspirin', history: 'Type 2 Diabetes (5 years). Mild Diabetic Neuropathy.',
        routine: { breakfast: '07:00 AM', lunch: '12:00 PM', dinner: '06:30 PM', sleep: '09:30 PM', wake: '05:30 AM' },

        pastDiagnosis: 'Type 2 Diabetes with neuropathy. Needs medication adjustment.'
    },
    'P048590': {
        id: 'P048590', name: 'Pham Minh C', age: 38, gender: 'Male', bloodGroup: 'AB-', height: 180, weight: 89.0,
        allergies: 'None reported', history: 'Obesity Class 1. Recently diagnosed pre-diabetes. Family history of Type 2 Diabetes.',
        routine: { breakfast: '08:00 AM', lunch: '01:00 PM', dinner: '08:00 PM', sleep: '11:00 PM', wake: '07:00 AM' },

        pastDiagnosis: 'Impaired Fasting Glucose (Pre-diabetes). Dyslipidemia.'
    },
    'P067823': {
        id: 'P067823', name: 'Le Hoang D', age: 55, gender: 'Male', bloodGroup: 'B+', height: 168, weight: 78.3,
        allergies: 'Sulfa Drugs', history: 'Type 2 Diabetes (8 years). Chronic kidney disease stage 2.',
        routine: { breakfast: '07:15 AM', lunch: '12:00 PM', dinner: '06:45 PM', sleep: '10:00 PM', wake: '05:45 AM' },

        pastDiagnosis: 'Uncontrolled Type 2 Diabetes. Diabetic Nephropathy stage 2.'
    },
    'P089123': {
        id: 'P089123', name: 'Vu Thi E', age: 29, gender: 'Female', bloodGroup: 'O-', height: 162, weight: 64.0,
        allergies: 'None', history: '24 weeks pregnant. Gestational Diabetes diagnosed 2 weeks ago.',
        routine: { breakfast: '07:30 AM', lunch: '12:30 PM', dinner: '07:00 PM', sleep: '10:30 PM', wake: '06:30 AM' },

        pastDiagnosis: 'Gestational Diabetes Mellitus (GDM) on diet control.'
    },
    'P091102': {
        id: 'P091102', name: 'Hoang Van F', age: 70, gender: 'Male', bloodGroup: 'AB+', height: 170, weight: 62.0,
        allergies: 'None', history: 'Type 1 Diabetes diagnosed 30 years ago. Retinopathy.',
        routine: { breakfast: '07:00 AM', lunch: '12:00 PM', dinner: '06:30 PM', sleep: '09:30 PM', wake: '06:00 AM' },

        pastDiagnosis: 'Type 1 Diabetes. History of frequent nocturnal hypoglycemia. Diabetic Retinopathy.'
    }
};

// Mock Symptoms Catalog
const symptomsCatalog = [
    { id: 'S01', name: 'Polyuria (Frequent Urination)' },
    { id: 'S02', name: 'Polydipsia (Extreme Thirst)' },
    { id: 'S03', name: 'Polyphagia (Extreme Hunger)' },
    { id: 'S04', name: 'Unexplained Weight Loss' },
    { id: 'S05', name: 'Fatigue & Lethargy' },
    { id: 'S06', name: 'Blurry Vision' },
    { id: 'S07', name: 'Slow-healing cuts/sores' },
    { id: 'S08', name: 'Numbness/Tingling in extremities' },
    { id: 'S09', name: 'Dry skin & Itchiness' }
];

// Mock Laboratory Tests Catalog
const labTestsCatalog = [
    { id: 'L01', name: 'Fasting Blood Glucose', unit: 'mmol/L', range: '3.9 - 5.6', room: 'Laboratory' },
    { id: 'L02', name: 'HbA1c (Glycated Hemoglobin)', unit: '%', range: '4.0 - 5.6', room: 'Laboratory' },
    { id: 'L03', name: 'OGTT (Oral Glucose Tolerance)', unit: 'mmol/L', range: '< 7.8', room: 'Laboratory' },
    { id: 'L04', name: 'Random Blood Glucose', unit: 'mmol/L', range: '4.4 - 7.8', room: 'Laboratory' },
    { id: 'L05', name: 'Serum Creatinine', unit: 'µmol/L', range: '62 - 115', room: 'Laboratory' },
    { id: 'L06', name: 'Microalbuminuria (Urine)', unit: 'mg/24h', range: '< 30', room: 'Laboratory' },
    { id: 'L07', name: 'Lipid Profile (Total Cholesterol)', unit: 'mmol/L', range: '< 5.2', room: 'Laboratory' }
];

// Mock Medications list
const medicationsCatalog = [
    { id: 'M01', name: 'Metformin Hydrochloride', form: 'Tablet', concentration: '500mg', route: 'Oral' },
    { id: 'M02', name: 'Metformin XR (Extended Release)', form: 'Tablet', concentration: '1000mg', route: 'Oral' },
    { id: 'M03', name: 'Gliclazide MR', form: 'Tablet', concentration: '30mg', route: 'Oral' },
    { id: 'M04', name: 'Empagliflozin (Jardiance)', form: 'Tablet', concentration: '10mg', route: 'Oral' },
    { id: 'M05', name: 'Sitagliptin (Januvia)', form: 'Tablet', concentration: '100mg', route: 'Oral' },
    { id: 'M06', name: 'Insulin Glargine (Lantus)', form: 'Injection Pen', concentration: '100 IU/mL', route: 'Subcutaneous' },
    { id: 'M07', name: 'Insulin Aspart (Novorapid)', form: 'Injection Pen', concentration: '100 IU/mL', route: 'Subcutaneous' },
    { id: 'M08', name: 'Amlodipine Besylate', form: 'Tablet', concentration: '5mg', route: 'Oral' }
];

// State variables for current session
let currentPatient = null;
let viewOnlyMode = false;
let selectedSymptoms = {};
let orderedLabs = {};
let prescriptionLines = [];

document.addEventListener('DOMContentLoaded', () => {
    loadSessionPatient();
    renderSymptomsGrid();
    initLabTestChecklist();
    renderOrderedLabsList();
    setupNextAppointmentMinDate();
});

// Load patient from session storage or default
function loadSessionPatient() {
    const patientId = sessionStorage.getItem('selectedPatientId') || 'P067823'; // Default to Le Hoang D
    viewOnlyMode = sessionStorage.getItem('examineViewOnly') === 'true';

    currentPatient = patientRecords[patientId] || patientRecords['P067823'];

    // Render left panel patient card info
    document.getElementById('patName').textContent = currentPatient.name;
    document.getElementById('patId').textContent = currentPatient.id;
    document.getElementById('patAge').textContent = `${currentPatient.age} yrs`;
    document.getElementById('patGender').textContent = currentPatient.gender;
    document.getElementById('patBlood').textContent = currentPatient.bloodGroup;
    document.getElementById('patHeight').textContent = `${currentPatient.height} cm`;
    document.getElementById('patWeight').textContent = `${currentPatient.weight} kg`;
    
    // Calculate BMI
    const heightInMeters = currentPatient.height / 100;
    const bmi = (currentPatient.weight / (heightInMeters * heightInMeters)).toFixed(1);
    let bmiCategory = 'Normal';
    if (bmi >= 25 && bmi < 30) bmiCategory = 'Overweight';
    else if (bmi >= 30) bmiCategory = 'Obese';
    else if (bmi < 18.5) bmiCategory = 'Underweight';
    document.getElementById('patBmi').textContent = `${bmi} (${bmiCategory})`;

    document.getElementById('patAllergy').textContent = currentPatient.allergies;
    document.getElementById('patHistory').textContent = currentPatient.history;

    // Routine loading
    document.getElementById('patBreakfast').textContent = currentPatient.routine.breakfast;
    document.getElementById('patLunch').textContent = currentPatient.routine.lunch;
    document.getElementById('patDinner').textContent = currentPatient.routine.dinner;
    document.getElementById('patSleep').textContent = `${currentPatient.routine.sleep} / ${currentPatient.routine.wake}`;



    // If view only mode, disable form elements
    if (viewOnlyMode) {
        document.querySelectorAll('textarea, input, select, button').forEach(el => {
            if (!el.classList.contains('ai-close-btn') && !el.classList.contains('nav-item') && !el.classList.contains('logout-btn')) {
                el.disabled = true;
            }
        });
        document.getElementById('examDiagnosis').value = currentPatient.pastDiagnosis;
        document.getElementById('examHistory').value = 'Reviewed patient complaints and automated test schedules.';
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
        const div = document.createElement('div');
        div.className = 'symptom-card';
        div.setAttribute('data-id', s.id);
        div.innerHTML = `
            <div class="symptom-card-header">
                <input type="checkbox" id="chk-${s.id}" onchange="toggleSymptom('${s.id}')">
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
        
        // When clicking the row, toggle the checkbox inside it
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
    
    // Reset search
    document.getElementById('labSearch').value = '';
    filterLabTestsInModal();

    // Check currently ordered labs in checklist
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

    // Reset orderedLabs and populate from check states
    orderedLabs = {};
    labTestsCatalog.forEach(l => {
        const chk = document.getElementById(`modal-chk-${l.id}`);
        if (chk && chk.checked) {
            orderedLabs[l.id] = true;
        }
    });

    renderOrderedLabsList();
    closeLabTestModal();
    
    // Simulate realistic lab delay
    simulateLabProcessing();
}

// Render summary of ordered labs in page
function renderOrderedLabsList() {
    const container = document.getElementById('orderedLabsSummary');
    if (!container) return;

    // Remove existing tags
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
            <button class="btn-remove-lab" onclick="removeLabOrder('${test.id}')" title="Remove Order"><i class="fas fa-times"></i></button>
        `;
        container.appendChild(div);
    });
}

// Remove lab order from tag list on main page
function removeLabOrder(id) {
    if (viewOnlyMode) return;
    delete orderedLabs[id];
    renderOrderedLabsList();
    
    // If currently processing, let the timeout handle it. Otherwise, update results immediately.
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

    // Show loading UI
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

    // Wait 10 seconds then show results
    setTimeout(() => {
        isLabProcessing = false;
        // Only show results if there are still ordered labs
        if (Object.keys(orderedLabs).length > 0) {
            simulateLabResults();
        } else {
             tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--doctor-text-muted); padding: 30px;">Please select one or more tests from the catalog above to view results</td></tr>`;
        }
    }, 5000);
}

// Laboratory simulator mapping results
function simulateLabResults() {
    const tbody = document.getElementById('labResultsTableBody');
    if (!tbody) return;

    const selectedIds = Object.keys(orderedLabs);
    if (selectedIds.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--doctor-text-muted); padding: 30px;">Please select one or more tests from the catalog above to view results</td></tr>`;
        return;
    }

    tbody.innerHTML = '';
    selectedIds.forEach(id => {
        const test = labTestsCatalog.find(l => l.id === id);
        if (!test) return;

        // Generate mock test result values based on patient profile
        let val = 0.0;
        let flag = 'NORMAL';
        let flagClass = 'flag-normal';

        if (test.id === 'L01') { // Fasting Blood Glucose
            val = currentPatient.id === 'P048590' ? 6.1 : (currentPatient.id === 'P089123' ? 5.1 : 9.4); // Elevate for diabetic
            if (val > 5.6) {
                flag = 'HIGH';
                flagClass = 'flag-high';
            }
        } else if (test.id === 'L02') { // HbA1c
            val = currentPatient.id === 'P048590' ? 6.1 : (currentPatient.id === 'P089123' ? 5.5 : 8.2);
            if (val > 5.6) {
                flag = 'HIGH';
                flagClass = 'flag-high';
            }
        } else if (test.id === 'L03') { // OGTT
            val = currentPatient.id === 'P089123' ? 8.4 : 11.2;
            if (val >= 7.8) {
                flag = 'HIGH';
                flagClass = 'flag-high';
            }
        } else if (test.id === 'L05') { // Serum Creatinine
            val = currentPatient.id === 'P067823' ? 128.0 : 85.0; // Stage 2 kidney
            if (val > 115) {
                flag = 'HIGH';
                flagClass = 'flag-high';
            }
        } else { // Others general normal
            val = 4.8;
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
                showToast('At least 1 laboratory test is required for proper diagnosis.', 'warning');
                return;
            }
            if (isLabProcessing) {
                showToast('Please wait for laboratory tests to finish processing.', 'warning');
                return;
            }
        }
    }

    const tabcontents = document.getElementsByClassName('tab-content');
    for (let i = 0; i < tabcontents.length; i++) {
        tabcontents[i].classList.remove('active');
    }

    const tablinks = document.getElementsByClassName('tab-btn');
    for (let i = 0; i < tablinks.length; i++) {
        tablinks[i].classList.remove('active');
    }

    document.getElementById(tabId).classList.add('active');
    evt.currentTarget.classList.add('active');
}



let editingMedIndex = -1;

function showMedicationModal() {
    const modal = document.getElementById('medicationModal');
    if (modal) modal.classList.add('open');
    document.getElementById('medSearch').value = '';
    
    // Reset add/edit states
    editingMedIndex = -1;
    selectedMed = null;
    document.getElementById('medDetailFields').style.display = 'none';
    const btn = document.getElementById('addMedBtn');
    if (btn) btn.textContent = 'Add to Prescription';
    
    // Clear detail fields
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
    
    // Show detail fields
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

    // Open modal
    const modal = document.getElementById('medicationModal');
    if (modal) modal.classList.add('open');

    // Populate data
    document.getElementById('medSearch').value = selectedMed ? `${selectedMed.name} (${selectedMed.concentration})` : line.name;
    document.getElementById('medAutocompleteList').style.display = 'none';

    document.getElementById('medDosage').value = line.dosage;
    document.getElementById('medDuration').value = line.duration;
    document.getElementById('medQuantity').value = line.quantity;
    document.getElementById('medTiming').value = line.timing;

    // Show details
    const detailFields = document.getElementById('medDetailFields');
    if (detailFields) detailFields.style.display = 'grid';

    // Update button
    const btn = document.getElementById('addMedBtn');
    if (btn) btn.textContent = 'Update Prescription';
}

function renderPrescriptionLines() {
    const list = document.getElementById('prescriptionLines');
    const emptyMsg = document.getElementById('emptyPrescription');
    if (!list) return;

    // Clear previous items except the empty message
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
                <button class="btn-remove-line" style="color: var(--doctor-primary);" onclick="editPrescriptionLine(${index})" title="Edit"><i class="fas fa-pen"></i></button>
                <button class="btn-remove-line" onclick="removePrescriptionLine(${index})" title="Remove"><i class="fas fa-trash"></i></button>
            </div>
        `;
        list.appendChild(div);
    });
}

// Complete Clinical Checkup
function saveExam() {
    if (viewOnlyMode) {
        showToast('This checkup session is read-only. Returning to dashboard.', 'warning');
        setTimeout(() => window.location.href = '/doctor/dashboard', 1500);
        return;
    }

    const diagnosis = document.getElementById('examDiagnosis').value.trim();
    if (!diagnosis) {
        showToast('Please fill out the Diagnosis Note before completing checkup.', 'error');
        return;
    }

    // Validate if Prescription is filled
    const prescribedCount = prescribedMeds.length;
    if (prescribedCount === 0) {
        showToast('Please prescribe at least 1 medicine before completing.', 'warning');
        // Switch to prescription tab just in case
        if (!document.getElementById('prescription-tab').classList.contains('active')) {
             switchTab({currentTarget: document.querySelectorAll('.tab-btn')[2]}, 'prescription-tab');
        }
        return;
    }

    // Update status in Session mockQueue
    const patientId = currentPatient.id;
    const localQueue = JSON.parse(sessionStorage.getItem('mockQueue') || 'null');
    if (localQueue) {
        const patient = localQueue.find(p => p.id === patientId);
        if (patient) {
            patient.status = 'Completed';
            sessionStorage.setItem('mockQueue', JSON.stringify(localQueue));
        }
    }

    showToast(`Consultation and Clinical Checkup completed successfully!`, 'success');
    setTimeout(() => {
        window.location.href = '/doctor/dashboard';
    }, 1500);
}

function cancelExam() {
    if (viewOnlyMode) {
        window.location.href = '/doctor/dashboard';
        return;
    }
    const modal = document.getElementById('cancelConfirmModal');
    if (modal) modal.classList.add('open');
}

function closeCancelModal() {
    const modal = document.getElementById('cancelConfirmModal');
    if (modal) modal.classList.remove('open');
}

function confirmCancelExam() {
    closeCancelModal();
    showToast('Examination cancelled. Discarding notes...', 'error');
    setTimeout(() => {
        window.location.href = '/doctor/dashboard';
    }, 1500);
}

// --- EXAMINE ROOM JAVASCRIPT ---

// Toast Notification System
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    
    // Limit to 4 toasts
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
        <button class="toast-close-btn"><i class="fas fa-times"></i></button>
    `;
    
    container.appendChild(toast);
    
    // Close button logic
    const closeBtn = toast.querySelector('.toast-close-btn');
    closeBtn.addEventListener('click', () => {
        toast.classList.remove('show');
        setTimeout(() => { if (toast.parentNode) toast.remove(); }, 300);
    });
    
    // Trigger animation
    setTimeout(() => toast.classList.add('show'), 10);
    
    // Auto remove
    setTimeout(() => {
        if (toast.parentNode) {
            toast.classList.remove('show');
            setTimeout(() => { if (toast.parentNode) toast.remove(); }, 300);
        }
    }, 4000);
}

// Check view-only mode
const urlParams = new URLSearchParams(window.location.search);

function goToHistory() {
    window.location.href = '/doctor/examine/patients';
}
