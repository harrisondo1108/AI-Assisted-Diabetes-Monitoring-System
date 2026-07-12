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
let orderedLabs = {};
let prescriptionLines = [];
let isSubmitting = false;
let simulatedResults = {};

document.addEventListener('DOMContentLoaded', () => {
    loadSessionPatient();
    setupNextAppointmentMinDate();

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
    const activeExamInProgress = (typeof thActiveExam !== 'undefined' && thActiveExam && thActiveExam.status === 'InProgress');
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
    } else if (isEditMode || activeExamInProgress) {
        simulateLabResults();
        renderPrescriptionLines();
    }

    // Handle cancel form submission to clear draft
    const isInProgress = thActiveExam && thActiveExam.status === 'InProgress';
    if (isInProgress) {
        const cancelForm = document.getElementById('cancelForm');
        if (cancelForm) {
            cancelForm.addEventListener('submit', () => {
                isSubmitting = true;
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

    // Check for validation error attribute
    if (typeof thValidationError !== 'undefined' && thValidationError) {
        showToast(thValidationError, 'error');
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
    const activeExamInProgress = (typeof thActiveExam !== 'undefined' && thActiveExam && thActiveExam.status === 'InProgress');
    // Load active exam / view only / edit properties
    if ((viewOnlyMode || isEditMode || activeExamInProgress) && typeof thLastExam !== 'undefined' && thLastExam) {
        document.getElementById('examDiagnosis').value = thLastExam.diagnosisNote || '';
        document.getElementById('examHistory').value = thLastExam.medicalHistory || '';
        document.getElementById('examNextDate').value = thLastExam.nextAppointment ? thLastExam.nextAppointment.substring(0, 10) : '';

        if (thLastExam.treatmentPlan) {
            document.getElementById('planGoal').value = thLastExam.treatmentPlan.treatmentGoal || '';
            document.getElementById('planDiet').value = thLastExam.treatmentPlan.dietPlan || '';
            document.getElementById('planExercise').value = thLastExam.treatmentPlan.exercisePlan || '';
            document.getElementById('planGlucose').value = thLastExam.treatmentPlan.glucoseMonitoringPlan || '';
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

    const unit = selectedMed ? getMedicationUnit(selectedMed.form) : "viên";
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
        const unit = getMedicationUnit(line.form);

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
                    <button type="button" class="btn-presc-action edit-btn" onclick="editPrescriptionLine(${index})" title="Sửa" style="background-color: #f3f4f6; color: var(--doctor-primary); border: 1px solid #d1d5db; border-radius: 6px; width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s;"><i class="fas fa-pen"></i></button>
                    <button type="button" class="btn-presc-action delete-btn" onclick="if(confirm('Bạn có chắc chắn muốn xóa thuốc này khỏi đơn?')) removePrescriptionLine(${index})" title="Xóa" style="background-color: #f3f4f6; color: var(--doctor-danger); border: 1px solid #d1d5db; border-radius: 6px; width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s;"><i class="fas fa-trash"></i></button>
                </div>
            </div>
            <div class="presc-card-dropdown" id="presc-dropdown-${index}" style="display: block; padding: 12px 16px; background-color: #f9fafb; border-top: 1px solid #e5e7eb; font-size: 0.85rem; color: #374151; line-height: 1.6;">
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

// Complete Clinical Checkup
function saveExam() {
    isSubmitting = true;
    if (viewOnlyMode) {
        showToast('Ca khám này ở chế độ chỉ đọc. Đang quay lại trang tổng quan.', 'warning');
        setTimeout(() => window.location.href = '/doctor/dashboard', 1500);
        return;
    }

    // Serialize prescription list to hidden JSON input
    serializePrescription();


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

    form.submit();
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
    if (viewOnlyMode) {
        window.location.href = `/doctor/history?patientId=${currentPatient.id}&from=examine`;
        return;
    }

    if (currentPatient) {
        // Serialize prescription list
        serializePrescription();


        // Serialize lab results
        const labResults = [];
        const selectedIds = Object.keys(orderedLabs);
        selectedIds.forEach(id => {
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
        
        // Change action to draft
        form.action = `/doctor/examine/${currentPatient.id}/draft`;

        // Append selected labTestIds
        form.querySelectorAll('input[name="labTestIds"]').forEach(el => el.remove());
        Object.keys(orderedLabs).forEach(id => {
            const labInput = document.createElement('input');
            labInput.type = 'hidden';
            labInput.name = 'labTestIds';
            labInput.value = id;
            form.appendChild(labInput);
        });

        // Submit form
        form.submit();
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
    const getEl = id => document.getElementById(id);
    const getVal = id => getEl(id)?.value?.trim();
    const showErr = (id, msg) => {
        const err = getEl('error-' + id);
        if (err) { err.textContent = msg; err.style.display = 'block'; }
        isValid = false;
    };

    ['medDosage', 'medDuration', 'medQuantity', 'medStartDate', 'medTiming'].forEach(id => {
        const err = getEl('error-' + id);
        if (err) err.style.display = 'none';
    });

    const dosage = getVal('medDosage');
    if (!dosage) showErr('medDosage', 'Vui lòng nhập liều lượng mỗi lần dùng');
    else if (isNaN(dosage) || Number(dosage) <= 0) showErr('medDosage', 'Liều lượng phải là số dương');

    const duration = getVal('medDuration');
    if (!duration) showErr('medDuration', 'Vui lòng nhập số ngày sử dụng');
    else if (isNaN(duration) || !Number.isInteger(Number(duration)) || Number(duration) <= 0) showErr('medDuration', 'Số ngày sử dụng phải là số nguyên dương');

    if (!getVal('medStartDate')) showErr('medStartDate', 'Vui lòng chọn ngày bắt đầu sử dụng thuốc');

    const timingContainer = getEl('medTimingContainer');
    if (timingContainer) {
        const checked = Array.from(timingContainer.querySelectorAll('.custom-multiselect-option input[type="checkbox"]')).some(cb => cb.checked);
        if (!checked) showErr('medTiming', 'Vui lòng chọn ít nhất một thời điểm sử dụng thuốc');
    }
    return isValid;
}

function updateEndDate() {
    const startVal = document.getElementById('medStartDate')?.value;
    const durationVal = parseInt(document.getElementById('medDuration')?.value?.trim(), 10);
    const endEl = document.getElementById('medEndDate');
    if (!endEl) return;

    if (!startVal || isNaN(durationVal) || durationVal <= 0) {
        endEl.value = '';
        return;
    }
    const d = new Date(startVal);
    d.setDate(d.getDate() + durationVal - 1);
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    endEl.value = `${d.getFullYear()}-${mm}-${dd}`;
}

function formatDateDMY(dateStr) {
    if (!dateStr) return 'N/A';
    const parts = dateStr.split('-');
    return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : dateStr;
}

function toggleTimingDropdown(event) {
    if (event) event.stopPropagation();
    document.getElementById('medTimingContainer')?.classList.toggle('open');
}

function selectTimingOption(optionDiv, event) {
    if (event) event.stopPropagation();
    const checkbox = optionDiv.querySelector('input[type="checkbox"]');
    if (checkbox) {
        checkbox.checked = !checkbox.checked;
        updateTimingPlaceholder();
    }
}

function updateTimingPlaceholder() {
    const container = document.getElementById('medTimingContainer');
    if (!container) return;
    const selectedTexts = Array.from(container.querySelectorAll('.custom-multiselect-option input[type="checkbox"]:checked'))
        .map(cb => cb.parentNode.querySelector('label')?.textContent?.trim() || cb.value);

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

document.addEventListener('click', function(event) {
    const container = document.getElementById('medTimingContainer');
    if (container && !container.contains(event.target)) {
        container.classList.remove('open');
    }
});



function calculateTotalQuantity() {
    const dosageInput = document.getElementById('medDosage');
    const durationInput = document.getElementById('medDuration');
    const quantityInput = document.getElementById('medQuantity');
    const timingContainer = document.getElementById('medTimingContainer');

    if (!dosageInput || !durationInput || !quantityInput || !timingContainer) return;

    const dosageVal = parseFloat(dosageInput.value) || 0;
    const durationVal = parseInt(durationInput.value, 10) || 0;
    const checkedTimings = timingContainer.querySelectorAll('.custom-multiselect-option input[type="checkbox"]:checked').length;

    quantityInput.value = Math.ceil(dosageVal * checkedTimings * durationVal);
}

function parseDosagePerDose(dosageStr) {
    if (!dosageStr || dosageStr === 'Auto') return 1;
    const match = dosageStr.match(/^([\d.]+)/);
    return match && !isNaN(parseFloat(match[1])) ? parseFloat(match[1]) : 1;
}

function clearMedicationErrors() {
    ['error-medDosage', 'error-medDuration', 'error-medQuantity', 'error-medStartDate', 'error-medTiming'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });
}

function getMedicationUnit(form) {
    if (!form) return "viên";
    const formLower = form.toLowerCase();
    if (formLower.includes("viên") || formLower.includes("nén") || formLower.includes("nang")) return "viên";
    if (formLower.includes("gói")) return "gói";
    if (formLower.includes("chai")) return "chai";
    if (formLower.includes("ống")) return "ống";
    if (formLower.includes("tablet")) return "tablet";
    if (formLower.includes("capsule")) return "capsule";
    return form.trim().length > 0 ? form.trim() : "viên";
}

function serializePrescription() {
    const prescriptionList = prescriptionLines.map(line => ({
        medId: line.medId,
        dosage: line.dosage,
        duration: parseInt(line.duration),
        quantity: parseInt(line.quantity),
        timingText: line.timingText,
        medicationPlan: line.medicationPlan,
        startDate: line.startDate,
        endDate: line.endDate
    }));
    const hiddenInput = document.getElementById('prescriptionJson');
    if (hiddenInput) {
        hiddenInput.value = JSON.stringify(prescriptionList);
    }
}


