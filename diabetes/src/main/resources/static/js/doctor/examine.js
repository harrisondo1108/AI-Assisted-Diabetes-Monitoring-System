/**
 * Doctor Examine Room JS - Pure Thymeleaf Integration
 */

// Dynamically populated catalogs from Thymeleaf
const symptomsCatalog = (typeof rawSymptomsCatalog !== 'undefined' && rawSymptomsCatalog ? rawSymptomsCatalog : []).map(s => ({
    id: s.symptomId,
    name: s.symptomName
}));

let labTestsCatalog = (typeof rawLabTestsCatalog !== 'undefined' && rawLabTestsCatalog ? rawLabTestsCatalog : []).map(l => ({
    id: l.testId,
    name: l.testName,
    unit: l.unit,
    range: l.referenceRange,
    room: 'Phòng xét nghiệm',
    minValue: l.minValue,
    maxValue: l.maxValue
}));

const medicationsCatalog = (typeof rawMedicationsCatalog !== 'undefined' && rawMedicationsCatalog ? rawMedicationsCatalog : []).map(m => ({
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
let simulatedResults = {};

document.addEventListener('DOMContentLoaded', () => {
    loadSessionPatient();
    renderSymptomsGrid();
    setupNextAppointmentMinDate();
    restoreExamineDraft();

    // Setup keypress block and paste block for medDuration & medQuantity
    const numericInputs = ['medDuration', 'medQuantity'];
    numericInputs.forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener('keypress', (e) => {
                if (e.key.length > 1 || e.ctrlKey || e.metaKey) {
                    return;
                }
                if (!/[0-9]/.test(e.key)) {
                    e.preventDefault();
                }
            });
            el.addEventListener('paste', (e) => {
                const pasteData = e.clipboardData.getData('text');
                if (!/^\d+$/.test(pasteData)) {
                    e.preventDefault();
                }
            });
        }
    });

    // Update End Date & quantity when inputs change
    const startDateInput = document.getElementById('medStartDate');
    const durationInput = document.getElementById('medDuration');
    const dosageInput = document.getElementById('medDosage');

    if (startDateInput) {
        startDateInput.addEventListener('change', updateEndDate);
    }
    if (durationInput) {
        durationInput.addEventListener('input', () => {
            updateEndDate();
            calculateTotalQuantity();
        });
        durationInput.addEventListener('change', () => {
            updateEndDate();
            calculateTotalQuantity();
        });
    }
    if (dosageInput) {
        dosageInput.addEventListener('input', calculateTotalQuantity);
        dosageInput.addEventListener('change', calculateTotalQuantity);
    }
    
    const isEditMode = (typeof thIsEditMode !== 'undefined' ? thIsEditMode : false);
    if (viewOnlyMode) {
        simulateLabResults();
        renderPrescriptionLines();
        
        // In view only mode, disable all interactable form inputs
        document.querySelectorAll('textarea, input, select, button').forEach(el => {
            // Keep common navigation, close/logout and close modal buttons active
            if (!el.classList.contains('modal-close') && 
                !el.classList.contains('logout-link-btn') && 
                el.id !== 'viewHistoryBtn') {
                el.disabled = true;
            }
        });
    } else if (isEditMode) {
        renderPrescriptionLines();
    }

    // Handle cancel form submission to clear draft
    const isInProgress = thActiveExam && thActiveExam.status === 'InProgress';
    if (isInProgress) {
        const cancelForm = document.getElementById('cancelForm');
        if (cancelForm) {
            cancelForm.addEventListener('submit', () => {
                isSubmitting = true;
                sessionStorage.removeItem('examineDraft');
            });
        }
    }

    // Handle isPregnant checkbox change dynamically
    const isPregnantCheckbox = document.getElementById('isPregnant');
    if (isPregnantCheckbox) {
        isPregnantCheckbox.addEventListener('change', (e) => {
            updateLabCatalog(e.target.checked);
        });
        // Initial sync on page load or draft restore
        updateLabCatalog(isPregnantCheckbox.checked, true);
    }

    // Check for warning query parameter
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('warning') === 'in-progress') {
        showToast('Bạn đang có một ca khám chưa hoàn thành! Vui lòng tiếp tục ca khám.', 'warning');
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
            let bmiCategory = 'Bình thường';
            if (bmi >= 25 && bmi < 30) bmiCategory = 'Thừa cân';
            else if (bmi >= 30) bmiCategory = 'Béo phì';
            else if (bmi < 18.5) bmiCategory = 'Thiếu cân';
            bmiVal = `${bmi.toFixed(1)} (${bmiCategory})`;
        }

        currentPatient = {
            id: thPatient.userId,
            name: thPatient.fullName,
            age: ageStr,
            gender: thPatient.gender !== null ? (thPatient.gender ? 'Nữ' : 'Nam') : 'N/A',
            bloodGroup: thPatient.bloodgroup || 'N/A',
            height: thPatient.height || 0,
            weight: thPatient.weight || 0,
            bmi: bmiVal,
            allergies: thPatient.allergyNotes || 'Không có dị ứng thuốc nào được ghi nhận',
            history: thPatient.permanentMedicalHistory || 'Không có tiền sử bệnh án được ghi nhận',
            routine: {
                breakfast: thRoutine && thRoutine.breakfastTime ? formatLocalTime(thRoutine.breakfastTime) : '07:00 AM',
                lunch: thRoutine && thRoutine.lunchTime ? formatLocalTime(thRoutine.lunchTime) : '11:00 AM',
                dinner: thRoutine && thRoutine.dinnerTime ? formatLocalTime(thRoutine.dinnerTime) : '06:00 PM',
                sleep: thRoutine && thRoutine.sleepTime ? formatLocalTime(thRoutine.sleepTime) : '10:00 PM',
                wake: thRoutine && thRoutine.wakeUpTime ? formatLocalTime(thRoutine.wakeUpTime) : '06:00 AM'
            }
        };
    }

    const isEditMode = (typeof thIsEditMode !== 'undefined' ? thIsEditMode : false);
    // Load active exam / view only / edit properties
    if ((viewOnlyMode || isEditMode) && typeof thLastExam !== 'undefined' && thLastExam) {
        document.getElementById('examDiagnosis').value = thLastExam.diagnosisNote || '';
        document.getElementById('examHistory').value = thLastExam.medicalHistory || '';
        document.getElementById('examNextDate').value = thLastExam.nextAppointment ? thLastExam.nextAppointment.substring(0, 10) : '';

        if (thLastExam.treatmentPlan) {
            document.getElementById('planGoal').value = thLastExam.treatmentPlan.treatmentGoal || '';
            document.getElementById('planDiet').value = thLastExam.treatmentPlan.dietPlan || '';
            document.getElementById('planExercise').value = thLastExam.treatmentPlan.exercisePlan || '';
            document.getElementById('planGlucose').value = thLastExam.treatmentPlan.glucoseMonitoringPlan || '';
        }
    }

    selectedSymptoms = {};
    if (typeof thChosenSymptomNotes !== 'undefined' && thChosenSymptomNotes) {
        Object.keys(thChosenSymptomNotes).forEach(id => {
            selectedSymptoms[id] = thChosenSymptomNotes[id];
        });
    }

    orderedLabs = {};
    simulatedResults = {};
    if (typeof thLastExamLabResults !== 'undefined' && thLastExamLabResults) {
        thLastExamLabResults.forEach(res => {
            const test = labTestsCatalog.find(l => l.name === res.labTest.testName);
            if (test) {
                orderedLabs[test.id] = true;
                simulatedResults[test.id] = {
                    val: res.resultValue,
                    flag: res.flag
                };
            }
        });
    }

    prescriptionLines = [];
    if (typeof thLastExamPrescriptionDetails !== 'undefined' && thLastExamPrescriptionDetails) {
        prescriptionLines = thLastExamPrescriptionDetails.map(p => {
            const tNames = p.prescriptionTimings && p.prescriptionTimings.length > 0
                ? p.prescriptionTimings.map(pt => pt.timing.timingName)
                : ['Breakfast Time (07:30 AM)'];
            const tText = tNames.join(', ');
            return {
                medId: p.medication.medicationId,
                name: p.medication.medicationName,
                concentration: p.medication.concentration,
                form: p.medication.form,
                dosage: p.dosage,
                dosagePerDose: parseDosagePerDose(p.dosage),
                duration: p.durationDays,
                quantity: p.totalQuantity,
                timing: tNames, // standard timing mapped to options
                timingText: tText,
                medicationPlan: p.medicationPlan || '',
                startDate: p.startDate || '',
                endDate: p.endDate || ''
            };
        });
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
        const noteVal = selectedSymptoms[s.id] || '';
        const div = document.createElement('div');
        div.className = `symptom-card ${isChecked ? 'selected' : ''}`;
        div.setAttribute('data-id', s.id);
        div.innerHTML = `
            <div class="symptom-card-header">
                <input type="checkbox" name="symptomIds" id="chk-${s.id}" value="${s.id}" onchange="toggleSymptom('${s.id}')" ${isChecked ? 'checked' : ''}>
                <label for="chk-${s.id}">${s.name}</label>
            </div>
            <div class="symptom-comment-box">
                <input type="text" placeholder="Thêm ghi chú cụ thể (mức độ, thời gian)..." id="comment-${s.id}" value="${noteVal}" oninput="updateSymptomComment('${s.id}')">
            </div>
        `;
        grid.appendChild(div);
    });
}

function updateSymptomComment(id) {
    const input = document.getElementById(`comment-${id}`);
    if (input && selectedSymptoms[id] !== undefined) {
        selectedSymptoms[id] = input.value;
    }
}

function toggleSymptom(id) {
    const card = document.querySelector(`.symptom-card[data-id="${id}"]`);
    const chk = document.getElementById(`chk-${id}`);

    if (chk.checked) {
        card.classList.add('selected');
        const input = document.getElementById(`comment-${id}`);
        selectedSymptoms[id] = input ? input.value : '';
    } else {
        card.classList.remove('selected');
        delete selectedSymptoms[id];
    }
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
                    <td><span class="flag-badge ${flagClass}">${l.flag === 'HIGH' ? 'CAO' : (l.flag === 'NORMAL' ? 'BÌNH THƯỜNG' : l.flag)}</span></td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--doctor-text-muted); padding: 30px;">Không có chỉ định xét nghiệm nào trong ca khám này</td></tr>`;
        }
        return;
    }

    const selectedIds = Object.keys(orderedLabs);
    if (selectedIds.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--doctor-text-muted); padding: 30px;">Vui lòng chỉ định một hoặc nhiều xét nghiệm từ danh mục phía trên để xem kết quả</td></tr>`;
        return;
    }

    tbody.innerHTML = '';
    selectedIds.forEach(id => {
        const test = labTestsCatalog.find(l => l.id === id);
        if (!test) return;

        let val = 4.8;
        let flag = 'NORMAL';
        let flagClass = 'flag-normal';

        if (!simulatedResults[id]) {
            const rand = new RandomVal();
            let minVal = parseFloat(test.minValue);
            let maxVal = parseFloat(test.maxValue);
            let hasDbThreshold = !isNaN(minVal) && !isNaN(maxVal);

            if (hasDbThreshold) {
                const randType = rand.next();
                if (randType < 0.30) { // 30% chance of LOW
                    val = parseFloat((minVal - 0.5 - rand.next() * (minVal * 0.15)).toFixed(1));
                    if (val < 0) val = 0;
                } else if (randType > 0.60) { // 40% chance of HIGH
                    const scale = maxVal > 20 ? (maxVal * 0.3) : 4.0;
                    val = parseFloat((maxVal + 0.1 + rand.next() * scale).toFixed(1));
                } else { // 30% chance of NORMAL
                    val = parseFloat((minVal + rand.next() * (maxVal - minVal)).toFixed(1));
                }

                if (val < minVal) {
                    flag = 'LOW';
                } else if (val > maxVal) {
                    flag = 'HIGH';
                } else {
                    flag = 'NORMAL';
                }
            } else {
                val = 0.0;
                flag = 'NORMAL';
            }
            
            val = parseFloat(val);
            simulatedResults[id] = { val, flag };
        }

        val = simulatedResults[id].val;
        flag = simulatedResults[id].flag;
        flagClass = flag === 'HIGH' ? 'flag-high' : (flag === 'LOW' ? 'flag-low' : 'flag-normal');
        const flagText = flag === 'HIGH' ? 'CAO' : (flag === 'LOW' ? 'THẤP' : 'BÌNH THƯỜNG');

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${test.name}</strong></td>
            <td><span style="font-weight:500; color: var(--doctor-text-muted);">${test.range} ${test.unit}</span></td>
            <td><strong>${val} ${test.unit}</strong></td>
            <td><span class="flag-badge ${flagClass}">${flagText}</span></td>
        `;
        tbody.appendChild(tr);
    });
}

function updateLabCatalog(isPregnant, isInitialLoad = false) {
    const hasAssignedBefore = Object.keys(orderedLabs).length > 0;

    const rawCatalog = (isPregnant && typeof rawLabTestsPregnantCatalog !== 'undefined')
        ? rawLabTestsPregnantCatalog
        : (typeof rawLabTestsCatalog !== 'undefined' ? rawLabTestsCatalog : []);

    labTestsCatalog = rawCatalog.map(l => ({
        id: l.testId,
        name: l.testName,
        unit: l.unit,
        range: l.referenceRange,
        room: 'Phòng xét nghiệm',
        minValue: l.minValue,
        maxValue: l.maxValue
    }));

    if (!isInitialLoad) {
        // Clear simulated results cache
        simulatedResults = {};

        if (hasAssignedBefore) {
            // Automatically re-assign all matching tests in the new catalog and recalculate
            orderedLabs = {};
            labTestsCatalog.forEach(test => {
                orderedLabs[test.id] = true;
            });
            simulateLabResults();
        }
    } else {
        simulateLabResults();
    }
}

function assignAllLabTests() {
    orderedLabs = {};
    labTestsCatalog.forEach(test => {
        orderedLabs[test.id] = true;
    });
    simulateLabResults();
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

let editingMedIndex = -1;

function showMedicationModal() {
    const modal = document.getElementById('medicationModal');
    if (modal) modal.classList.add('open');
    document.getElementById('medSearch').value = '';

    // Clear error spans
    clearMedicationErrors();

    // Deselect all timings by default
    const timingContainer = document.getElementById('medTimingContainer');
    if (timingContainer) {
        timingContainer.classList.remove('open');
        const checkboxes = timingContainer.querySelectorAll('.custom-multiselect-option input[type="checkbox"]');
        checkboxes.forEach(cb => cb.checked = false);
        updateTimingPlaceholder();
    }

    editingMedIndex = -1;
    selectedMed = null;
    document.getElementById('medDetailFields').style.display = 'none';
    const btn = document.getElementById('addMedBtn');
    if (btn) btn.textContent = 'Thêm vào đơn thuốc';

    document.getElementById('medDosage').value = '1';
    document.getElementById('medDuration').value = '30';
    document.getElementById('medQuantity').value = '0';
    document.getElementById('medPlan').value = '';
    calculateTotalQuantity();

    // Mặc định ngày bắt đầu khi mở modal là ngày hiện tại
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    const todayStr = `${yyyy}-${mm}-${dd}`;
    document.getElementById('medStartDate').value = todayStr;
    updateEndDate();

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
    
    // Loại bỏ các thuốc đã kê khỏi gợi ý, ngoại trừ thuốc đang sửa
    const addedMedIds = prescriptionLines
        .filter((_, idx) => idx !== editingMedIndex)
        .map(line => line.medId);

    const filtered = medicationsCatalog.filter(m => 
        !addedMedIds.includes(m.id) &&
        (m.name.toLowerCase().includes(query) || m.concentration.toLowerCase().includes(query))
    );

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
    if (detailFields) detailFields.style.display = 'block';

    // Update banner details
    document.getElementById('medBannerName').textContent = `${med.name} (${med.concentration})`;
    document.getElementById('medBannerForm').textContent = `${med.form} / ${med.route || 'Đường dùng'}`;

    document.getElementById('medDuration').focus();
    updateEndDate();
}

function addMedicationLine() {
    if (!selectedMed) {
        alert('Vui lòng tìm kiếm và chọn thuốc từ danh mục gợi ý');
        return;
    }

    if (!validateMedicationFields()) {
        return;
    }

    const duration = parseInt(document.getElementById('medDuration').value.trim());
    const quantity = parseInt(document.getElementById('medQuantity').value.trim());
    const startDate = document.getElementById('medStartDate').value;
    const endDate = document.getElementById('medEndDate').value;
    const dosageVal = parseFloat(document.getElementById('medDosage').value.trim()) || 1;

    const timingContainer = document.getElementById('medTimingContainer');
    let timing = [];
    let timingText = '';
    let timingsCount = 0;
    if (timingContainer) {
        const checkedOptions = Array.from(timingContainer.querySelectorAll('.custom-multiselect-option input[type="checkbox"]')).filter(cb => cb.checked);
        timing = checkedOptions.map(cb => cb.value);
        timingsCount = checkedOptions.length;
        timingText = checkedOptions.map(cb => {
            const label = cb.parentNode.querySelector('label');
            return label ? label.textContent.trim() : cb.value;
        }).join(', ');
    }

    let unit = "viên";
    if (selectedMed && selectedMed.form) {
        const formLower = selectedMed.form.toLowerCase();
        if (formLower.includes("viên") || formLower.includes("nén") || formLower.includes("nang")) {
            unit = "viên";
        } else if (formLower.includes("gói")) {
            unit = "gói";
        } else if (formLower.includes("chai")) {
            unit = "chai";
        } else if (formLower.includes("ống")) {
            unit = "ống";
        } else if (formLower.includes("tablet")) {
            unit = "tablet";
        } else if (formLower.includes("capsule")) {
            unit = "capsule";
        } else if (formLower.trim().length > 0) {
            unit = formLower.trim();
        }
    }
    const dosage = `${dosageVal} ${unit}/lần, ${timingsCount} lần/ngày`;

    const medPlan = document.getElementById('medPlan').value.trim();

    const line = {
        medId: selectedMed.id,
        name: selectedMed.name,
        concentration: selectedMed.concentration,
        form: selectedMed.form,
        dosage: dosage,
        dosagePerDose: dosageVal,
        duration: duration,
        quantity: quantity,
        timing: timing,
        timingText: timingText,
        medicationPlan: medPlan,
        startDate: startDate,
        endDate: endDate
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

    // Clear error spans
    clearMedicationErrors();

    selectedMed = medicationsCatalog.find(m => m.id === line.medId);
    editingMedIndex = index;

    const modal = document.getElementById('medicationModal');
    if (modal) modal.classList.add('open');

    document.getElementById('medSearch').value = selectedMed ? `${selectedMed.name} (${selectedMed.concentration})` : line.name;
    document.getElementById('medAutocompleteList').style.display = 'none';

    document.getElementById('medDosage').value = line.dosagePerDose || parseDosagePerDose(line.dosage);
    document.getElementById('medDuration').value = line.duration;
    document.getElementById('medQuantity').value = line.quantity;
    
    const timingContainer = document.getElementById('medTimingContainer');
    if (timingContainer) {
        timingContainer.classList.remove('open');
        const checkboxes = timingContainer.querySelectorAll('.custom-multiselect-option input[type="checkbox"]');
        checkboxes.forEach(cb => cb.checked = false);
        
        let selectedValues = [];
        if (Array.isArray(line.timing)) {
            selectedValues = line.timing;
        } else if (typeof line.timing === 'string') {
            selectedValues = line.timing.split(',').map(s => s.trim());
        }
        checkboxes.forEach(cb => {
            if (selectedValues.includes(cb.value)) {
                cb.checked = true;
            }
        });
        updateTimingPlaceholder();
    }

    document.getElementById('medPlan').value = line.medicationPlan || '';
    document.getElementById('medStartDate').value = line.startDate || '';
    document.getElementById('medEndDate').value = line.endDate || '';

    const detailFields = document.getElementById('medDetailFields');
    if (detailFields) detailFields.style.display = 'block';

    if (selectedMed) {
        document.getElementById('medBannerName').textContent = `${selectedMed.name} (${selectedMed.concentration})`;
        document.getElementById('medBannerForm').textContent = `${selectedMed.form} / ${selectedMed.route || 'Đường dùng'}`;
    } else {
        document.getElementById('medBannerName').textContent = line.name || '';
        document.getElementById('medBannerForm').textContent = `${line.form || ''} (${line.concentration || ''})`;
    }

    const btn = document.getElementById('addMedBtn');
    if (btn) btn.textContent = 'Cập nhật đơn thuốc';
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
        let unit = "viên";
        if (line.form) {
            const formLower = line.form.toLowerCase();
            if (formLower.includes("viên") || formLower.includes("nén") || formLower.includes("nang")) {
                unit = "viên";
            } else if (formLower.includes("gói")) {
                unit = "gói";
            } else if (formLower.includes("chai")) {
                unit = "chai";
            } else if (formLower.includes("ống")) {
                unit = "ống";
            } else if (formLower.includes("tablet")) {
                unit = "tablet";
            } else if (formLower.includes("capsule")) {
                unit = "capsule";
            } else if (formLower.trim().length > 0) {
                unit = formLower.trim();
            }
        }

        const div = document.createElement('div');
        div.className = 'prescription-line';
        div.style.padding = '0';
        div.style.border = '1px solid var(--doctor-border)';
        div.style.borderRadius = '8px';
        div.style.marginBottom = '10px';
        div.style.overflow = 'hidden';
        div.style.backgroundColor = 'var(--doctor-card-bg)';

        div.innerHTML = `
            <div class="presc-card-main" style="display: flex; justify-content: space-between; align-items: center; padding: 8px 16px;">
                <div class="presc-med-info" style="display: flex; flex-direction: row; align-items: center; gap: 10px;">
                    <span style="font-size: 1.25rem; color: var(--doctor-primary);"><i class="fas fa-pills"></i></span>
                    <div class="presc-med-name" style="font-weight: 700; color: var(--doctor-text-main); font-size: 0.95rem;">
                        ${line.name} <span class="presc-med-conc" style="font-weight: 500; color: var(--doctor-text-muted); font-size: 0.85rem;">(${line.concentration})</span>
                    </div>
                </div>
                <div class="presc-card-actions" style="display: flex; gap: 8px;">
                    <button type="button" class="btn-presc-action detail-btn" onclick="togglePrescriptionDetail(${index})" title="Chi tiết" style="background-color: #f3f4f6; color: #4b5563; border: 1px solid #d1d5db; border-radius: 6px; width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s;"><i class="fas fa-eye"></i></button>
                    <button type="button" class="btn-presc-action edit-btn" onclick="editPrescriptionLine(${index})" title="Sửa" style="background-color: #f3f4f6; color: var(--doctor-primary); border: 1px solid #d1d5db; border-radius: 6px; width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s;"><i class="fas fa-pen"></i></button>
                    <button type="button" class="btn-presc-action delete-btn" onclick="if(confirm('Bạn có chắc chắn muốn xóa thuốc này khỏi đơn?')) removePrescriptionLine(${index})" title="Xóa" style="background-color: #f3f4f6; color: var(--doctor-danger); border: 1px solid #d1d5db; border-radius: 6px; width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s;"><i class="fas fa-trash"></i></button>
                </div>
            </div>
            <div class="presc-card-dropdown" id="presc-dropdown-${index}" style="display: none; padding: 12px 16px; background-color: #f9fafb; border-top: 1px solid #e5e7eb; font-size: 0.85rem; color: #374151; line-height: 1.6;">
                <div style="margin-bottom: 6px;"><strong style="color: var(--doctor-text-muted);"><i class="fas fa-cubes"></i> Dạng bào chế:</strong> ${line.form}</div>
                ${line.startDate && line.endDate ? `<div style="margin-bottom: 6px;"><strong style="color: var(--doctor-text-muted);"><i class="far fa-calendar-alt"></i> Liệu trình:</strong> Từ ${formatDateDMY(line.startDate)} đến ${formatDateDMY(line.endDate)}</div>` : ''}
                <div style="margin-bottom: 6px;"><strong style="color: var(--doctor-text-muted);"><i class="fas fa-prescription-bottle"></i> Liều dùng:</strong> ${line.dosage}</div>
                <div style="margin-bottom: 6px;"><strong style="color: var(--doctor-text-muted);"><i class="far fa-calendar-days"></i> Thời gian sử dụng:</strong> ${line.duration} ngày</div>
                <div style="margin-bottom: 6px;"><strong style="color: var(--doctor-text-muted);"><i class="fas fa-calculator"></i> Số lượng:</strong> ${line.quantity} ${unit}</div>
                <div style="margin-bottom: 6px;"><strong style="color: var(--doctor-text-muted);"><i class="far fa-clock"></i> Thời điểm sử dụng:</strong> ${line.timingText}</div>
                ${line.medicationPlan ? `<div style="margin-bottom: 0;"><strong style="color: var(--doctor-text-muted);"><i class="fas fa-info-circle"></i> Hướng dẫn:</strong> ${line.medicationPlan}</div>` : ''}
            </div>
        `;
        list.appendChild(div);
    });
}

function togglePrescriptionDetail(index) {
    const dropdown = document.getElementById(`presc-dropdown-${index}`);
    if (dropdown) {
        const btn = dropdown.previousElementSibling.querySelector('.detail-btn i');
        if (dropdown.style.display === 'none') {
            dropdown.style.display = 'block';
            if (btn) btn.className = 'fas fa-eye-slash';
        } else {
            dropdown.style.display = 'none';
            if (btn) btn.className = 'fas fa-eye';
        }
    }
}

// Complete Clinical Checkup
function saveExam() {
    isSubmitting = true;
    if (viewOnlyMode) {
        showToast('Ca khám này ở chế độ chỉ đọc. Đang quay lại trang tổng quan.', 'warning');
        setTimeout(() => window.location.href = '/doctor/dashboard', 1500);
        return;
    }

    // Serialize prescription list to hidden JSON input
    const prescriptionList = prescriptionLines.map(line => {
        return {
            medId: line.medId,
            dosage: line.dosage,
            duration: parseInt(line.duration),
            quantity: parseInt(line.quantity),
            timingText: line.timingText,
            medicationPlan: line.medicationPlan,
            startDate: line.startDate,
            endDate: line.endDate
        };
    });
    
    document.getElementById('prescriptionJson').value = JSON.stringify(prescriptionList);

    // Serialize symptom comments
    const symptomComments = {};
    Object.keys(selectedSymptoms).forEach(id => {
        const input = document.getElementById(`comment-${id}`);
        symptomComments[id] = input ? input.value.trim() : (selectedSymptoms[id] || '');
    });
    document.getElementById('symptomCommentsJson').value = JSON.stringify(symptomComments);

    // Serialize lab results
    const labResults = [];
    const selectedIds = Object.keys(orderedLabs);
    selectedIds.forEach(id => {
        if (!simulatedResults[id]) {
            const test = labTestsCatalog.find(l => l.id === id);
            if (test) {
                let val = 4.8;
                let flag = 'NORMAL';
                let minVal = parseFloat(test.minValue);
                let maxVal = parseFloat(test.maxValue);
                if (!isNaN(minVal) && !isNaN(maxVal)) {
                    val = minVal;
                }
                simulatedResults[id] = { val, flag };
            }
        }
        if (simulatedResults[id]) {
            labResults.push({
                testId: id,
                val: simulatedResults[id].val,
                flag: simulatedResults[id].flag
            });
        }
    });
    document.getElementById('labResultsJson').value = JSON.stringify(labResults);

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

    showToast(`Đang lưu kết quả khám lâm sàng...`, 'success');
    sessionStorage.removeItem('examineDraft');
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
        // Collect current state
        const draft = {
            patientId: currentPatient.id,
            isPregnant: document.getElementById('isPregnant') ? document.getElementById('isPregnant').checked : false,
            examDiagnosis: document.getElementById('examDiagnosis') ? document.getElementById('examDiagnosis').value : '',
            examHistory: document.getElementById('examHistory') ? document.getElementById('examHistory').value : '',
            examNextDate: document.getElementById('examNextDate') ? document.getElementById('examNextDate').value : '',
            planGoal: document.getElementById('planGoal') ? document.getElementById('planGoal').value : '',
            planDiet: document.getElementById('planDiet') ? document.getElementById('planDiet').value : '',
            planExercise: document.getElementById('planExercise') ? document.getElementById('planExercise').value : '',
            planGlucose: document.getElementById('planGlucose') ? document.getElementById('planGlucose').value : '',
            prescriptionLines: prescriptionLines,
            selectedSymptoms: selectedSymptoms,
            orderedLabs: orderedLabs,
            simulatedResults: simulatedResults
        };
        // Capture symptom comments dynamically
        Object.keys(selectedSymptoms).forEach(id => {
            const input = document.getElementById(`comment-${id}`);
            if (input) {
                draft.selectedSymptoms[id] = input.value;
            }
        });

        sessionStorage.setItem('examineDraft', JSON.stringify(draft));
        window.location.href = `/doctor/history?patientId=${currentPatient.id}&from=examine`;
    }
}

// Navigation blocked modals removed

// Warn when leaving page with unsaved changes
window.addEventListener('beforeunload', (e) => {
    if (!viewOnlyMode && !isSubmitting) {
        const overlay = document.getElementById('examStartOverlay');
        const isExamActive = overlay && overlay.style.display === 'none';
        if (isExamActive) {
            e.preventDefault();
            e.returnValue = 'Bạn đang thực hiện khám bệnh dở dang. Bạn có chắc chắn muốn rời đi?';
            return e.returnValue;
        }
    }
});

function validateMedicationFields() {
    let isValid = true;

    const dosageInput = document.getElementById('medDosage');
    const durationInput = document.getElementById('medDuration');
    const quantityInput = document.getElementById('medQuantity');
    const startDateInput = document.getElementById('medStartDate');

    const dosageErr = document.getElementById('error-medDosage');
    const durationErr = document.getElementById('error-medDuration');
    const quantityErr = document.getElementById('error-medQuantity');
    const startDateErr = document.getElementById('error-medStartDate');

    // Reset errors
    if (dosageErr) dosageErr.style.display = 'none';
    if (durationErr) durationErr.style.display = 'none';
    if (quantityErr) quantityErr.style.display = 'none';
    if (startDateErr) startDateErr.style.display = 'none';

    // Validate Dosage
    if (dosageInput) {
        const dosageVal = dosageInput.value.trim();
        if (!dosageVal) {
            if (dosageErr) {
                dosageErr.textContent = 'Vui lòng nhập liều lượng mỗi lần dùng';
                dosageErr.style.display = 'block';
            }
            isValid = false;
        } else {
            const num = Number(dosageVal);
            if (isNaN(num) || num <= 0) {
                if (dosageErr) {
                    dosageErr.textContent = 'Liều lượng phải là số dương';
                    dosageErr.style.display = 'block';
                }
                isValid = false;
            }
        }
    }

    // Validate Duration
    if (durationInput) {
        const durationVal = durationInput.value;
        if (!durationVal) {
            if (durationErr) {
                durationErr.textContent = 'Vui lòng nhập số ngày sử dụng';
                durationErr.style.display = 'block';
            }
            isValid = false;
        } else {
            const num = Number(durationVal);
            if (isNaN(num) || !Number.isInteger(num) || num <= 0) {
                if (durationErr) {
                    durationErr.textContent = 'Số ngày sử dụng phải là số nguyên dương';
                    durationErr.style.display = 'block';
                }
                isValid = false;
            }
        }
    }

    // Validate Start Date
    if (startDateInput) {
        const startDateVal = startDateInput.value;
        if (!startDateVal) {
            if (startDateErr) {
                startDateErr.textContent = 'Vui lòng chọn ngày bắt đầu sử dụng thuốc';
                startDateErr.style.display = 'block';
            }
            isValid = false;
        }
    }

    // Validate Medication Timing
    const timingContainer = document.getElementById('medTimingContainer');
    const timingErr = document.getElementById('error-medTiming');
    if (timingErr) timingErr.style.display = 'none';
    if (timingContainer) {
        const checkedCount = Array.from(timingContainer.querySelectorAll('.custom-multiselect-option input[type="checkbox"]')).filter(cb => cb.checked).length;
        if (checkedCount === 0) {
            if (timingErr) {
                timingErr.textContent = 'Vui lòng chọn ít nhất một thời điểm sử dụng thuốc';
                timingErr.style.display = 'block';
            }
            isValid = false;
        }
    }

    return isValid;
}

function updateEndDate() {
    const startDateInput = document.getElementById('medStartDate');
    const durationInput = document.getElementById('medDuration');
    const endDateInput = document.getElementById('medEndDate');

    if (!startDateInput || !durationInput || !endDateInput) return;

    const startDateVal = startDateInput.value;
    const durationVal = durationInput.value.trim();

    if (!startDateVal || !durationVal) {
        endDateInput.value = '';
        return;
    }

    const durationDays = parseInt(durationVal, 10);
    if (isNaN(durationDays) || durationDays <= 0) {
        endDateInput.value = '';
        return;
    }

    const startDate = new Date(startDateVal);
    if (isNaN(startDate.getTime())) {
        endDateInput.value = '';
        return;
    }

    const endDate = new Date(startDate);
    endDate.setDate(startDate.getDate() + durationDays - 1);

    const yyyy = endDate.getFullYear();
    const mm = String(endDate.getMonth() + 1).padStart(2, '0');
    const dd = String(endDate.getDate()).padStart(2, '0');

    endDateInput.value = `${yyyy}-${mm}-${dd}`;
}

function formatDateDMY(dateStr) {
    if (!dateStr) return 'N/A';
    const parts = dateStr.split('-');
    if (parts.length === 3) {
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateStr;
}

// Toggle timing dropdown
function toggleTimingDropdown(event) {
    if (event) event.stopPropagation();
    const container = document.getElementById('medTimingContainer');
    if (container) {
        container.classList.toggle('open');
    }
}

// Select/toggle timing checkbox
function selectTimingOption(optionDiv, event) {
    if (event) event.stopPropagation();
    const checkbox = optionDiv.querySelector('input[type="checkbox"]');
    if (checkbox) {
        checkbox.checked = !checkbox.checked;
        updateTimingPlaceholder();
    }
}

// Update select placeholder text
function updateTimingPlaceholder() {
    const container = document.getElementById('medTimingContainer');
    if (!container) return;
    const checkboxes = container.querySelectorAll('.custom-multiselect-option input[type="checkbox"]');
    const selectedTexts = [];
    checkboxes.forEach(cb => {
        if (cb.checked) {
            const label = cb.parentNode.querySelector('label');
            if (label) {
                selectedTexts.push(label.textContent.trim());
            }
        }
    });

    const placeholder = document.getElementById('medTimingPlaceholder');
    if (placeholder) {
        if (selectedTexts.length > 0) {
            placeholder.innerHTML = selectedTexts.join('<br>');
            placeholder.style.color = 'var(--doctor-text-main)';
        } else {
            placeholder.textContent = 'Chọn thời điểm...';
            placeholder.style.color = '#888';
        }
    }
    calculateTotalQuantity();
}

// Close dropdown on click outside
document.addEventListener('click', function(event) {
    const container = document.getElementById('medTimingContainer');
    if (container && !container.contains(event.target)) {
        container.classList.remove('open');
    }
});

function restoreExamineDraft() {
    const draftStr = sessionStorage.getItem('examineDraft');
    if (!draftStr) return;

    try {
        const draft = JSON.parse(draftStr);
        if (currentPatient && draft.patientId === currentPatient.id) {
            if (draft.isPregnant !== undefined) {
                const chk = document.getElementById('isPregnant');
                if (chk) {
                    chk.checked = draft.isPregnant;
                }
            }
            if (draft.examDiagnosis) {
                const el = document.getElementById('examDiagnosis');
                if (el) el.value = draft.examDiagnosis;
            }
            if (draft.examHistory) {
                const el = document.getElementById('examHistory');
                if (el) el.value = draft.examHistory;
            }
            if (draft.examNextDate) {
                const el = document.getElementById('examNextDate');
                if (el) el.value = draft.examNextDate;
            }
            if (draft.planGoal) {
                const el = document.getElementById('planGoal');
                if (el) el.value = draft.planGoal;
            }
            if (draft.planDiet) {
                const el = document.getElementById('planDiet');
                if (el) el.value = draft.planDiet;
            }
            if (draft.planExercise) {
                const el = document.getElementById('planExercise');
                if (el) el.value = draft.planExercise;
            }
            if (draft.planGlucose) {
                const el = document.getElementById('planGlucose');
                if (el) el.value = draft.planGlucose;
            }

            if (draft.prescriptionLines) {
                prescriptionLines = draft.prescriptionLines;
                renderPrescriptionLines();
            }
            if (draft.selectedSymptoms) {
                selectedSymptoms = draft.selectedSymptoms;
                renderSymptomsGrid();
            }
            if (draft.orderedLabs) {
                orderedLabs = draft.orderedLabs;
            }
            if (draft.simulatedResults) {
                simulatedResults = draft.simulatedResults;
                simulateLabResults();
            }
        } else {
            sessionStorage.removeItem('examineDraft');
        }
    } catch (e) {
        console.error('Error restoring examine draft:', e);
    }
}

function calculateTotalQuantity() {
    const dosageInput = document.getElementById('medDosage');
    const durationInput = document.getElementById('medDuration');
    const quantityInput = document.getElementById('medQuantity');
    const timingContainer = document.getElementById('medTimingContainer');

    if (!dosageInput || !durationInput || !quantityInput || !timingContainer) return;

    const dosageVal = parseFloat(dosageInput.value) || 0;
    const durationVal = parseInt(durationInput.value, 10) || 0;
    const checkedTimings = timingContainer.querySelectorAll('.custom-multiselect-option input[type="checkbox"]:checked').length;

    const total = Math.ceil(dosageVal * checkedTimings * durationVal);
    quantityInput.value = total;
}

function parseDosagePerDose(dosageStr) {
    if (!dosageStr || dosageStr === 'Auto') return 1;
    const match = dosageStr.match(/^([\d.]+)/);
    if (match) {
        const val = parseFloat(match[1]);
        return isNaN(val) ? 1 : val;
    }
    return 1;
}

function clearMedicationErrors() {
    const ids = ['error-medDosage', 'error-medDuration', 'error-medQuantity', 'error-medStartDate', 'error-medTiming'];
    ids.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });
}


