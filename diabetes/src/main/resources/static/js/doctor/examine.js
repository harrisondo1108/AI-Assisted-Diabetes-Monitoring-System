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
    },
    'P102938': {
        id: 'P102938', name: 'Nguyen Thi G', age: 33, gender: 'Female', bloodGroup: 'A-', height: 160, weight: 52.4,
        allergies: 'Sulfa Drugs', history: 'Gestational diabetes history. Pre-diabetes.',
        routine: { breakfast: '07:00 AM', lunch: '12:00 PM', dinner: '06:30 PM', sleep: '10:30 PM', wake: '06:30 AM' },
        pastDiagnosis: 'Impaired fasting glucose. Borderline gestational risk.'
    },
    'P112390': {
        id: 'P112390', name: 'Tran Minh H', age: 49, gender: 'Male', bloodGroup: 'O-', height: 175, weight: 82.0,
        allergies: 'None', history: 'Type 2 Diabetes diagnosed 1 year ago. Obese.',
        routine: { breakfast: '08:00 AM', lunch: '01:00 PM', dinner: '07:30 PM', sleep: '11:00 PM', wake: '06:30 AM' },
        pastDiagnosis: 'Mild hyperglycemia. Impaired lipids. Obesity Class 1.'
    },
    'P123490': {
        id: 'P123490', name: 'Le Van I', age: 60, gender: 'Male', bloodGroup: 'B-', height: 165, weight: 65.5,
        allergies: 'None', history: 'Type 2 Diabetes (6 years). Hypertension.',
        routine: { breakfast: '07:15 AM', lunch: '12:15 PM', dinner: '06:45 PM', sleep: '10:00 PM', wake: '05:30 AM' },
        pastDiagnosis: 'Type 2 Diabetes. Stage 1 Hypertension.'
    },
    'P139402': {
        id: 'P139402', name: 'Pham Thi K', age: 27, gender: 'Female', bloodGroup: 'AB+', height: 158, weight: 50.0,
        allergies: 'Aspirin', history: 'Suspected early-onset Type 2 Diabetes.',
        routine: { breakfast: '07:30 AM', lunch: '12:00 PM', dinner: '07:00 PM', sleep: '10:30 PM', wake: '06:30 AM' },
        pastDiagnosis: 'Borderline high blood glucose. Needs diet control.'
    },
    'P148201': {
        id: 'P148201', name: 'Hoang Minh L', age: 42, gender: 'Male', bloodGroup: 'A+', height: 170, weight: 74.0,
        allergies: 'Penicillin', history: 'Pre-diabetes, family history of type 2 diabetes.',
        routine: { breakfast: '07:30 AM', lunch: '12:30 PM', dinner: '07:00 PM', sleep: '10:00 PM', wake: '06:00 AM' },
        pastDiagnosis: 'Borderline impaired fasting glucose. Advised lifestyle changes.'
    },
    'P159203': {
        id: 'P159203', name: 'Vu Van M', age: 68, gender: 'Male', bloodGroup: 'O+', height: 168, weight: 61.2,
        allergies: 'None', history: 'Type 2 Diabetes (12 years). Neuropathy symptoms.',
        routine: { breakfast: '07:00 AM', lunch: '11:45 AM', dinner: '06:15 PM', sleep: '09:30 PM', wake: '05:00 AM' },
        pastDiagnosis: 'Diabetes with mild peripheral neuropathy. High neuropathy risk.'
    },
    'P162901': {
        id: 'P162901', name: 'Tran Thi N', age: 50, gender: 'Female', bloodGroup: 'B-', height: 155, weight: 56.5,
        allergies: 'None', history: 'Type 2 Diabetes (4 years). Stable.',
        routine: { breakfast: '07:00 AM', lunch: '12:00 PM', dinner: '06:30 PM', sleep: '10:00 PM', wake: '05:45 AM' },
        pastDiagnosis: 'Stable Type 2 Diabetes on Metformin monotherapy.'
    },
    'P172039': {
        id: 'P172039', name: 'Nguyen Thi P', age: 65, gender: 'Female', bloodGroup: 'A-', height: 152, weight: 54.0,
        allergies: 'Sulfa Drugs', history: 'Type 2 Diabetes (7 years). Mild retinopathy.',
        routine: { breakfast: '07:30 AM', lunch: '12:15 PM', dinner: '06:30 PM', sleep: '09:45 PM', wake: '05:30 AM' },
        pastDiagnosis: 'Type 2 Diabetes. Dry eyes & early non-proliferative retinopathy.'
    },
    'P182390': {
        id: 'P182390', name: 'Le Hoang Q', age: 58, gender: 'Male', bloodGroup: 'O+', height: 172, weight: 79.5,
        allergies: 'None', history: 'Type 2 Diabetes (5 years). Mild chronic kidney disease.',
        routine: { breakfast: '07:00 AM', lunch: '12:00 PM', dinner: '07:00 PM', sleep: '10:00 PM', wake: '06:00 AM' },
        pastDiagnosis: 'Type 2 Diabetes. Stage 1 Kidney Disease (CKD stage 1).'
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
    }
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

    // Check status to display overlay if Pending
    const localQueue = JSON.parse(sessionStorage.getItem('mockQueue') || 'null');
    let isPending = false;
    if (localQueue) {
        const qPat = localQueue.find(p => p.id === currentPatient.id);
        if (qPat && qPat.status === 'Pending') {
            isPending = true;
        }
    }

    if (!viewOnlyMode && isPending) {
        const overlay = document.getElementById('examStartOverlay');
        if (overlay) {
            document.getElementById('overlayPatName').textContent = currentPatient.name;
            overlay.style.display = 'flex';
            
            // Disable workspace except for the cancel button
            const workspace = document.querySelector('.examination-workspace');
            const tabs = workspace.querySelectorAll('.diagnostic-tabs, .tab-content, .btn-primary');
            tabs.forEach(el => {
                if(el.closest('#examStartOverlay')) return; // keep overlay buttons active
                el.style.pointerEvents = 'none';
                el.style.opacity = '0.4';
            });
            
            // Ensure cancel button is clickable
            const cancelBtn = workspace.querySelector('.btn-outline-danger');
            if (cancelBtn) {
                cancelBtn.style.pointerEvents = 'auto';
                cancelBtn.style.opacity = '1';
                cancelBtn.style.position = 'relative';
                cancelBtn.style.zIndex = '1000';
            }
        }
    }

    // If view only mode, disable form elements
    if (viewOnlyMode) {
        document.querySelectorAll('textarea, input, select, button').forEach(el => {
            if (!el.classList.contains('ai-close-btn') && !el.classList.contains('nav-item') && !el.classList.contains('logout-btn')) {
                el.disabled = true;
            }
        });
        
        // Attempt to load dynamic consultation
        const dynamicConsultations = JSON.parse(sessionStorage.getItem('dynamicConsultations') || '{}');
        const lastExam = (dynamicConsultations[currentPatient.id] && dynamicConsultations[currentPatient.id][0]);
        if (lastExam) {
            document.getElementById('examDiagnosis').value = lastExam.diagnosis || '';
            document.getElementById('examHistory').value = lastExam.clinicalNotes || '';
            document.getElementById('examNextDate').value = lastExam.nextAppointmentRaw || '';
            
            // Populate treatment plan
            if (lastExam.treatmentPlan) {
                document.getElementById('planGoal').value = lastExam.treatmentPlan.goal || '';
                document.getElementById('planDiet').value = lastExam.treatmentPlan.diet || '';
                document.getElementById('planExercise').value = lastExam.treatmentPlan.exercise || '';
                document.getElementById('planGlucose').value = lastExam.treatmentPlan.glucose || '';
                document.getElementById('planMedication').value = lastExam.treatmentPlan.medication || '';
            }
            
            // Map symptoms
            selectedSymptoms = {};
            if (lastExam.symptoms) {
                lastExam.symptoms.forEach(name => {
                    const sym = symptomsCatalog.find(s => s.name === name);
                    if (sym) {
                        selectedSymptoms[sym.id] = '';
                    }
                });
            }
            
            // Map ordered labs
            orderedLabs = {};
            if (lastExam.labResults) {
                lastExam.labResults.forEach(res => {
                    const test = labTestsCatalog.find(l => l.name === res.name);
                    if (test) {
                        orderedLabs[test.id] = true;
                    }
                });
            }
            
            // Map prescription lines
            prescriptionLines = [];
            if (lastExam.prescription) {
                prescriptionLines = lastExam.prescription.map(p => {
                    let baseName = p.name;
                    let conc = '';
                    const match = p.name.match(/^(.*?)\s*\((.*?)\)$/);
                    if (match) {
                        baseName = match[1];
                        conc = match[2];
                    }
                    const med = medicationsCatalog.find(m => m.name === baseName) || {};
                    return {
                        medId: med.id || '',
                        name: baseName,
                        concentration: conc,
                        form: med.form || '',
                        dosage: p.dosage,
                        duration: p.duration,
                        quantity: p.quantity,
                        timing: p.timing,
                        timingText: p.timing === '07:30:00' ? 'Breakfast Time (07:30 AM)' : 
                                    p.timing === '12:15:00' ? 'Lunch Time (12:15 PM)' : 
                                    p.timing === '19:00:00' ? 'Dinner Time (07:00 PM)' : 
                                    p.timing === '06:00:00' ? 'Wake Up Time (06:00 AM)' : 
                                    p.timing === '22:00:00' ? 'Sleep Time (10:00 PM)' : 'Custom Time'
                    };
                });
            }
        } else {
            document.getElementById('examDiagnosis').value = currentPatient.pastDiagnosis;
            document.getElementById('examHistory').value = 'Reviewed patient complaints and automated test schedules.';
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
                <input type="checkbox" id="chk-${s.id}" onchange="toggleSymptom('${s.id}')" ${isChecked ? 'checked' : ''}>
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
                // Open confirmation modal instead of blocking completely
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
        
        // Clear transition state first
        pendingTabTransition = null;
        
        // Close modal
        const skipModal = document.getElementById('skipLabConfirmModal');
        if (skipModal) skipModal.classList.remove('open');
        
        // Execute the switch
        executeTabSwitch(targetBtn, targetTabId);
    }
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
    isSubmitting = true;
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

    // Validate that at least one field of the Treatment Plan is filled
    const planGoal = document.getElementById('planGoal').value.trim();
    const planDiet = document.getElementById('planDiet').value.trim();
    const planExercise = document.getElementById('planExercise').value.trim();
    const planGlucose = document.getElementById('planGlucose').value.trim();
    const planMedication = document.getElementById('planMedication').value.trim();

    if (!planGoal && !planDiet && !planExercise && !planGlucose && !planMedication) {
        showToast('At least 1 field of the Treatment Plan is required to complete.', 'error');
        // Switch to the Treatment & Prescription tab (index 2)
        if (!document.getElementById('prescription-tab').classList.contains('active')) {
             switchTab({currentTarget: document.querySelectorAll('.tab-btn')[2]}, 'prescription-tab');
        }
        return;
    }

    // Validate if Prescription is filled
    const prescribedCount = prescriptionLines.length;
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

    // Gather and save dynamic consultation details to sessionStorage
    const selectedSymptomNames = [];
    const checkedChks = document.querySelectorAll('#symptomsGrid input:checked');
    checkedChks.forEach(chk => {
        const id = chk.id.replace('chk-', '');
        const symptom = symptomsCatalog.find(s => s.id === id);
        if (symptom) {
            selectedSymptomNames.push(symptom.name);
        }
    });

    const labResultsList = [];
    Object.keys(orderedLabs).forEach(id => {
        const test = labTestsCatalog.find(l => l.id === id);
        if (test) {
            let val = 0.0;
            let flag = 'NORMAL';
            if (test.id === 'L01') {
                val = currentPatient.id === 'P048590' ? 6.1 : (currentPatient.id === 'P089123' ? 5.1 : 9.4);
                if (val > 5.6) flag = 'High';
            } else if (test.id === 'L02') {
                val = currentPatient.id === 'P048590' ? 6.1 : (currentPatient.id === 'P089123' ? 5.5 : 8.2);
                if (val > 5.6) flag = 'High';
            } else if (test.id === 'L03') {
                val = currentPatient.id === 'P089123' ? 8.4 : 11.2;
                if (val >= 7.8) flag = 'High';
            } else if (test.id === 'L05') {
                val = currentPatient.id === 'P067823' ? 128.0 : 85.0;
                if (val > 115) flag = 'High';
            } else {
                val = 4.8;
                flag = 'Normal';
            }
            labResultsList.push({
                name: test.name,
                value: val.toString(),
                unit: test.unit,
                range: test.range,
                flag: flag
            });
        }
    });

    const prescriptionList = prescriptionLines.map(line => {
        return {
            name: `${line.name} (${line.concentration})`,
            dosage: line.dosage,
            timing: line.timing,
            quantity: parseInt(line.quantity),
            duration: parseInt(line.duration)
        };
    });

    let nextApptVal = document.getElementById('examNextDate').value;
    let nextApptText = "None scheduled";
    if (nextApptVal) {
        const d = new Date(nextApptVal);
        nextApptText = d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    }

    const today = new Date();
    const formattedDate = today.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });

    let dynamicConsultations = JSON.parse(sessionStorage.getItem('dynamicConsultations') || '{}');
    if (!dynamicConsultations[patientId]) {
        dynamicConsultations[patientId] = [];
    }

    const newConsultation = {
        date: formattedDate,
        doctor: 'Dr. Harrison',
        diagnosis: diagnosis,
        clinicalNotes: document.getElementById('examHistory').value.trim() || 'No additional clinical notes recorded.',
        symptoms: selectedSymptomNames,
        labResults: labResultsList,
        prescription: prescriptionList,
        nextAppointment: nextApptText,
        nextAppointmentRaw: nextApptVal,
        treatmentPlan: {
            goal: planGoal,
            diet: planDiet,
            exercise: planExercise,
            glucose: planGlucose,
            medication: planMedication
        }
    };

    dynamicConsultations[patientId].unshift(newConsultation);
    sessionStorage.setItem('dynamicConsultations', JSON.stringify(dynamicConsultations));

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
    const reasonInput = document.getElementById('cancelReason');
    if (reasonInput) reasonInput.value = ''; // Reset reason field
    const modal = document.getElementById('cancelConfirmModal');
    if (modal) modal.classList.add('open');
}

function closeCancelModal() {
    const modal = document.getElementById('cancelConfirmModal');
    if (modal) modal.classList.remove('open');
}

function confirmCancelExam() {
    const reasonVal = document.getElementById('cancelReason').value.trim();
    if (!reasonVal) {
        showToast('Please enter a cancellation reason before proceeding.', 'error');
        return;
    }

    closeCancelModal();
    
    if (!viewOnlyMode) {
        isSubmitting = true;
        const patientId = currentPatient.id;
        
        // Update status in Session mockQueue to Cancelled
        const localQueue = JSON.parse(sessionStorage.getItem('mockQueue') || 'null');
        if (localQueue) {
            const patient = localQueue.find(p => p.id === patientId);
            if (patient) {
                patient.status = 'Cancelled';
                sessionStorage.setItem('mockQueue', JSON.stringify(localQueue));
            }
        }

        // Gather clinical notes with cancellation reason
        const notes = `Cancelled: ${reasonVal}`;
        
        // Create a cancelled consultation record
        let dynamicConsultations = JSON.parse(sessionStorage.getItem('dynamicConsultations') || '{}');
        if (!dynamicConsultations[patientId]) {
            dynamicConsultations[patientId] = [];
        }
        const today = new Date();
        const formattedDate = today.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
        
        const cancelledExam = {
            date: formattedDate,
            doctor: 'Dr. Harrison',
            status: 'Cancelled',
            diagnosis: 'Cancelled',
            clinicalNotes: notes,
            symptoms: [],
            labResults: [],
            prescription: [],
            nextAppointment: 'None scheduled',
            nextAppointmentRaw: ''
        };
        dynamicConsultations[patientId].unshift(cancelledExam);
        sessionStorage.setItem('dynamicConsultations', JSON.stringify(dynamicConsultations));
    }

    showToast('Examination cancelled. Status updated to Cancelled.', 'error');
    setTimeout(() => {
        window.location.href = '/doctor/dashboard';
    }, 1500);
}

// Start Examination Logic
function startExamination() {
    const overlay = document.getElementById('examStartOverlay');
    if (overlay) {
        overlay.style.display = 'none';
    }
    
    // Restore workspace interactions
    const workspace = document.querySelector('.examination-workspace');
    if (workspace) {
        const tabs = workspace.querySelectorAll('.diagnostic-tabs, .tab-content, .btn-primary');
        tabs.forEach(el => {
            el.style.pointerEvents = '';
            el.style.opacity = '';
        });
    }

    // Update session status to InProgress
    const patientId = currentPatient.id;
    const localQueue = JSON.parse(sessionStorage.getItem('mockQueue') || 'null');
    if (localQueue) {
        const patient = localQueue.find(p => p.id === patientId);
        if (patient && patient.status === 'Pending') {
            patient.status = 'InProgress';
            sessionStorage.setItem('mockQueue', JSON.stringify(localQueue));
        }
    }
    
    showToast('Examination started. Status updated to In Progress.', 'success');
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
    isSubmitting = true; // Bypass beforeunload warning for medical history view
    sessionStorage.setItem('fromExamineRoom', 'true');
    window.location.href = '/doctor/examine/patients';
}

// Warn when leaving page with unsaved changes
window.addEventListener('beforeunload', (e) => {
    if (!viewOnlyMode && !isSubmitting) {
        const overlay = document.getElementById('examStartOverlay');
        const isExamActive = overlay && overlay.style.display === 'none';
        if (isExamActive) {
            e.preventDefault();
            e.returnValue = 'You have unsaved changes. Are you sure you want to leave?';
            return e.returnValue;
        }
    }
});
