// Patients list with details, history, and mock blood glucose logs for the past 6 months
const patientsDb = [
    {
        id: 'P012932', name: 'Nguyen Van A', age: 45, gender: 'Male', bloodGroup: 'O+', status: 'Active',
        supervisor: 'Nguyen Van B', supervisorPhone: '0912345678',
        glucoseTrend: [6.8, 7.2, 6.5, 7.0, 6.2, 5.8], // Jan to Jun
        timeline: [
            {
                date: 'May 12, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Type 2 Diabetes mellitus under control. Stage 1 Hypertension.',
                clinicalNotes: 'Fasting blood glucose is well controlled. Blood pressure remains in Stage 1, but stable. Patient is compliant with the daily Metformin regimen. Advised continuing current dose and follow-up in 1 month.',
                symptoms: ['Fatigue & Lethargy', 'Dry skin & Itchiness'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '5.8', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'HbA1c (Glycated Hemoglobin)', value: '5.9', unit: '%', range: '4.0 - 5.6', flag: 'High' },
                    { name: 'Serum Creatinine', value: '78', unit: 'µmol/L', range: '62 - 115', flag: 'Normal' }
                ],
                prescription: [
                    { name: 'Metformin Hydrochloride (500mg)', dosage: '1 Tablet', timing: 'After breakfast', quantity: 30, duration: 30 },
                    { name: 'Amlodipine Besylate (5mg)', dosage: '1 Tablet', timing: 'Before sleep', quantity: 30, duration: 30 }
                ]
            },
            {
                date: 'Mar 10, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Newly diagnosed Type 2 Diabetes. Moderate hyperglycemia.',
                clinicalNotes: 'Newly diagnosed. Hyperglycemia symptoms (thirst, frequent urination) present. Initiated Metformin therapy. Patient educated on low glycemic diet, weight management, and home blood glucose monitoring.',
                symptoms: ['Polyuria (Frequent Urination)', 'Polydipsia (Extreme Thirst)'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '8.4', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'HbA1c (Glycated Hemoglobin)', value: '7.2', unit: '%', range: '4.0 - 5.6', flag: 'High' }
                ],
                prescription: [
                    { name: 'Metformin Hydrochloride (500mg)', dosage: '1 Tablet', timing: 'After breakfast', quantity: 30, duration: 30 }
                ]
            },
            {
                date: 'Jan 15, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Suspected Type 2 Diabetes Mellitus. Borderline Hypertension.',
                clinicalNotes: 'Fasting glucose was high during initial screening. Ordered a standard 2-hour 75g Oral Glucose Tolerance Test (OGTT) and HbA1c for clinical confirmation. Advised initial low carbohydrate diet.',
                symptoms: ['Polyuria (Frequent Urination)', 'Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '7.2', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' }
                ],
                prescription: []
            },
            {
                date: 'Nov 20, 2025', doctor: 'Dr. Harrison',
                diagnosis: 'Impaired Fasting Glucose (Pre-diabetes). Stable.',
                clinicalNotes: 'Routine follow-up. Blood sugar exhibits minor elevation. Encouraged physical workouts for 30 mins daily and sugar restriction. Recheck values in 2 months.',
                symptoms: ['Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '5.9', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' }
                ],
                prescription: []
            },
            {
                date: 'Sep 05, 2025', doctor: 'Dr. Harrison',
                diagnosis: 'Initial consultation. Health screening & fatigue report.',
                clinicalNotes: 'Patient complains of chronic lethargy and dry skin. Normal vitals. Initiated baseline blood screening panels including fasting glucose, lipid, and renal tests.',
                symptoms: ['Fatigue & Lethargy', 'Dry skin & Itchiness'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '5.5', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'Normal' }
                ],
                prescription: []
            }
        ]
    },
    {
        id: 'P023945', name: 'Tran Thi B', age: 62, gender: 'Female', bloodGroup: 'A+', status: 'Active',
        supervisor: 'Le Van X', supervisorPhone: '0988776655',
        glucoseTrend: [8.5, 8.2, 7.9, 8.4, 7.8, 8.0],
        timeline: [
            {
                date: 'May 20, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Type 2 Diabetes mellitus with peripheral neuropathy. Stable values.',
                clinicalNotes: 'Complains of mild numbness in feet. Fasting levels show minor fluctuations. Increased Metformin to XR formulation and added Gliclazide. Emphasized diabetic foot care and daily inspection.',
                symptoms: ['Numbness/Tingling in extremities', 'Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '8.0', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'HbA1c (Glycated Hemoglobin)', value: '7.8', unit: '%', range: '4.0 - 5.6', flag: 'High' }
                ],
                prescription: [
                    { name: 'Metformin XR (1000mg)', dosage: '1 Tablet', timing: 'After dinner', quantity: 30, duration: 30 },
                    { name: 'Gliclazide MR (30mg)', dosage: '1 Tablet', timing: 'Before breakfast', quantity: 30, duration: 30 }
                ]
            },
            {
                date: 'Mar 18, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Type 2 Diabetes with early peripheral neuropathy. Metformin dose optimization.',
                clinicalNotes: 'Patient complained of foot tingling. Optimization of Metformin is necessary due to subpar glucose logs. Added Vitamin B12 and scheduled podiatry exam.',
                symptoms: ['Numbness/Tingling in extremities'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '8.2', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'HbA1c (Glycated Hemoglobin)', value: '8.0', unit: '%', range: '4.0 - 5.6', flag: 'High' }
                ],
                prescription: [
                    { name: 'Metformin Hydrochloride (500mg)', dosage: '1 Tablet', timing: 'After breakfast', quantity: 60, duration: 30 }
                ]
            },
            {
                date: 'Jan 10, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Type 2 Diabetes Mellitus. Stable.',
                clinicalNotes: 'Follow-up consultation. Fasting glucose ranges look stable, but HbA1c remains slightly above the target. Encouraged stricter adherence to insulin-sensitizing diets.',
                symptoms: ['Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '7.5', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'HbA1c (Glycated Hemoglobin)', value: '7.4', unit: '%', range: '4.0 - 5.6', flag: 'High' }
                ],
                prescription: [
                    { name: 'Metformin Hydrochloride (500mg)', dosage: '1 Tablet', timing: 'After breakfast', quantity: 30, duration: 30 }
                ]
            },
            {
                date: 'Nov 05, 2025', doctor: 'Dr. Harrison',
                diagnosis: 'Type 2 Diabetes Mellitus. Sub-optimal glucose control.',
                clinicalNotes: 'First checkup. Fasting readings are elevated. Prescribed initial low-dose Metformin and scheduled follow-up checks. Provided glucose logs diary.',
                symptoms: ['Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '8.5', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' }
                ],
                prescription: [
                    { name: 'Metformin Hydrochloride (500mg)', dosage: '1 Tablet', timing: 'After breakfast', quantity: 30, duration: 30 }
                ]
            }
        ]
    },
    {
        id: 'P048590', name: 'Pham Minh C', age: 38, gender: 'Male', bloodGroup: 'AB-', status: 'Active',
        supervisor: 'Pham Thu Y', supervisorPhone: '0944332211',
        glucoseTrend: [5.8, 6.0, 5.9, 6.2, 6.1, 5.9],
        timeline: [
            {
                date: 'Apr 05, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Impaired Fasting Glucose (Pre-diabetes). Dyslipidemia. Lifestyle counseling given.',
                clinicalNotes: 'Fasting glucose is in pre-diabetic range. Dyslipidemia present with elevated total cholesterol. Discussed intensive lifestyle modifications (diet & exercise) and initiated low-dose Metformin to prevent progression.',
                symptoms: ['Unexplained Weight Loss'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '5.9', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'HbA1c (Glycated Hemoglobin)', value: '5.8', unit: '%', range: '4.0 - 5.6', flag: 'High' },
                    { name: 'Lipid Profile (Total Cholesterol)', value: '5.8', unit: 'mmol/L', range: '< 5.2', flag: 'High' }
                ],
                prescription: [
                    { name: 'Metformin Hydrochloride (500mg)', dosage: '1 Tablet', timing: 'After breakfast', quantity: 30, duration: 30 }
                ]
            },
            {
                date: 'Feb 12, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Impaired Glucose Tolerance. Dyslipidemia & hypercholesterolemia.',
                clinicalNotes: 'High cholesterol and borderline blood glucose. Emphasized low cholesterol diets and structured aerobic exercises to prevent transition to full Type 2 Diabetes.',
                symptoms: ['Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '5.8', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'Lipid Profile (Total Cholesterol)', value: '6.1', unit: 'mmol/L', range: '< 5.2', flag: 'High' }
                ],
                prescription: []
            },
            {
                date: 'Dec 10, 2025', doctor: 'Dr. Harrison',
                diagnosis: 'Borderline Fasting Hyperglycemia. Stable weight.',
                clinicalNotes: 'Glucose is slightly elevated. Weight management discussion. Patient will keep a daily log of dietary intake. Follow up scheduled in 2 months.',
                symptoms: [],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '5.7', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' }
                ],
                prescription: []
            },
            {
                date: 'Oct 15, 2025', doctor: 'Dr. Harrison',
                diagnosis: 'Routine Health Screening. Borderline indicators.',
                clinicalNotes: 'Initial checkup. Fasting glucose is at the upper limit of normal range. Advised reducing processed sugar consumption and monitoring weight.',
                symptoms: [],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '5.6', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'Normal' }
                ],
                prescription: []
            }
        ]
    },
    {
        id: 'P067823', name: 'Le Hoang D', age: 55, gender: 'Male', bloodGroup: 'B+', status: 'Active',
        supervisor: 'Le Hoang E', supervisorPhone: '0901234567',
        glucoseTrend: [9.2, 8.8, 9.5, 8.2, 8.9, 9.4],
        timeline: [
            {
                date: 'May 05, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Uncontrolled Type 2 Diabetes. Diabetic Nephropathy stage 2. Elevated fasting glucose.',
                clinicalNotes: 'Fasting glucose remains uncontrolled. Elevated urine microalbumin indicates stage 2 diabetic nephropathy. Initiated long-acting Insulin Glargine to stabilize nocturnal levels. Advised follow-up in 2 weeks.',
                symptoms: ['Blurry Vision', 'Numbness/Tingling in extremities', 'Polyuria (Frequent Urination)'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '9.4', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'HbA1c (Glycated Hemoglobin)', value: '8.9', unit: '%', range: '4.0 - 5.6', flag: 'High' },
                    { name: 'Microalbuminuria (Urine)', value: '45', unit: 'mg/24h', range: '< 30', flag: 'High' },
                    { name: 'Serum Creatinine', value: '118', unit: 'µmol/L', range: '62 - 115', flag: 'High' }
                ],
                prescription: [
                    { name: 'Metformin XR (1000mg)', dosage: '1 Tablet', timing: 'After dinner', quantity: 30, duration: 30 },
                    { name: 'Insulin Glargine (Lantus)', dosage: '10 IU', timing: 'Before sleep', quantity: 10, duration: 30 }
                ]
            },
            {
                date: 'Mar 03, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Uncontrolled Type 2 Diabetes. Early Diabetic Nephropathy detection.',
                clinicalNotes: 'Urine microalbumin results show microalbuminuria. Target blood pressure control needed to protect kidneys. Optimized Metformin dosage and added dietary guidelines.',
                symptoms: ['Numbness/Tingling in extremities', 'Blurry Vision'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '9.0', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'HbA1c (Glycated Hemoglobin)', value: '8.5', unit: '%', range: '4.0 - 5.6', flag: 'High' },
                    { name: 'Microalbuminuria (Urine)', value: '42', unit: 'mg/24h', range: '< 30', flag: 'High' }
                ],
                prescription: [
                    { name: 'Metformin XR (1000mg)', dosage: '1 Tablet', timing: 'After dinner', quantity: 30, duration: 30 }
                ]
            },
            {
                date: 'Jan 08, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Type 2 Diabetes Mellitus. High fasting glucose values.',
                clinicalNotes: 'Follow-up checks. Glucose levels are persistently above target values. Educated patient on diabetes self-management and warned about microvascular complications.',
                symptoms: ['Fatigue & Lethargy', 'Polyuria (Frequent Urination)'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '8.5', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' }
                ],
                prescription: [
                    { name: 'Metformin Hydrochloride (500mg)', dosage: '1 Tablet', timing: 'After breakfast', quantity: 60, duration: 30 }
                ]
            },
            {
                date: 'Nov 12, 2025', doctor: 'Dr. Harrison',
                diagnosis: 'Type 2 Diabetes Mellitus. Initial staging & diagnostics review.',
                clinicalNotes: 'Fasting glucose indicates hyperglycemia. Prescribed Metformin and initiated kidney baseline screening panels. Recommended home blood sugar logs.',
                symptoms: ['Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '8.1', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' }
                ],
                prescription: [
                    { name: 'Metformin Hydrochloride (500mg)', dosage: '1 Tablet', timing: 'After breakfast', quantity: 30, duration: 30 }
                ]
            }
        ]
    },
    {
        id: 'P089123', name: 'Vu Thi E', age: 29, gender: 'Female', bloodGroup: 'O-', status: 'Active',
        supervisor: 'Tran Quoc F', supervisorPhone: '0933445566',
        glucoseTrend: [5.1, 5.3, 5.2, 5.4, 5.1, 5.2],
        timeline: [
            {
                date: 'May 22, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Gestational Diabetes Mellitus (GDM) diagnosed at 24 weeks. Stabilized with dietary modification.',
                clinicalNotes: 'Gestational diabetes diagnosed at 24 weeks following OGTT. Stabilized with dietary modification. Patient is keeping a daily blood sugar log. Fasting levels are normal. No insulin needed currently.',
                symptoms: ['Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '5.2', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'Normal' },
                    { name: 'OGTT (Oral Glucose Tolerance)', value: '8.2', unit: 'mmol/L', range: '< 7.8', flag: 'High' }
                ],
                prescription: []
            },
            {
                date: 'Apr 20, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Pregnancy 20 weeks. Suspected Gestational Diabetes.',
                clinicalNotes: 'Oral Glucose Tolerance Test scheduled due to slightly elevated fasting values. Recommended meeting with gestational nutritionist.',
                symptoms: ['Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '5.3', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'Normal' }
                ],
                prescription: []
            },
            {
                date: 'Mar 15, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Pregnancy 15 weeks. Normal blood glucose levels.',
                clinicalNotes: 'Routine screening. Blood glucose and HbA1c values are within physiological targets. Continued standard prenatal care and vitamins.',
                symptoms: [],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '4.7', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'Normal' }
                ],
                prescription: []
            },
            {
                date: 'Jan 10, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Early Pregnancy Screening. Good glycemic baseline.',
                clinicalNotes: 'First prenatal check. Vitals normal. Glycemic baseline shows low risk indices. Emphasized general dietary hygiene during pregnancy.',
                symptoms: [],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '4.6', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'Normal' }
                ],
                prescription: []
            }
        ]
    },
    {
        id: 'P091102', name: 'Hoang Van F', age: 70, gender: 'Male', bloodGroup: 'AB+', status: 'Clocked',
        supervisor: 'Hoang Quoc G', supervisorPhone: '0977665544',
        glucoseTrend: [7.8, 6.9, 7.5, 8.1, 7.2, 7.0],
        timeline: [
            {
                date: 'May 10, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Type 1 Diabetes Mellitus. History of frequent hypoglycemia. Normal diabetic retinopathy.',
                clinicalNotes: 'Type 1 Diabetes under insulin therapy. Experiences nocturnal hypoglycemia occasionally. Diabetic retinopathy stable. Adjusted insulin dosages: increased basal and recalibrated bolus ratios.',
                symptoms: ['Fatigue & Lethargy', 'Blurry Vision'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '7.0', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'HbA1c (Glycated Hemoglobin)', value: '7.2', unit: '%', range: '4.0 - 5.6', flag: 'High' },
                    { name: 'Serum Creatinine', value: '85', unit: 'µmol/L', range: '62 - 115', flag: 'Normal' }
                ],
                prescription: [
                    { name: 'Insulin Aspart (Novorapid)', dosage: '6 IU', timing: 'Before breakfast/lunch/dinner', quantity: 3, duration: 30 },
                    { name: 'Insulin Glargine (Lantus)', dosage: '14 IU', timing: 'Before sleep', quantity: 2, duration: 30 }
                ]
            },
            {
                date: 'Mar 08, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Type 1 Diabetes Mellitus. Recurrent nocturnal hypoglycemia.',
                clinicalNotes: 'Patient reported sweating and shakiness during early morning hours. Reduced basal insulin Glargine to prevent hypoglycemic episodes and modified bolus ratios.',
                symptoms: ['Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '6.8', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' }
                ],
                prescription: [
                    { name: 'Insulin Aspart (Novorapid)', dosage: '6 IU', timing: 'Before meals', quantity: 3, duration: 30 },
                    { name: 'Insulin Glargine (Lantus)', dosage: '12 IU', timing: 'Before sleep', quantity: 2, duration: 30 }
                ]
            },
            {
                date: 'Jan 05, 2026', doctor: 'Dr. Harrison',
                diagnosis: 'Type 1 Diabetes Mellitus. Stable Retinopathy checks.',
                clinicalNotes: 'Annual ophthalmology review indicates stable, non-proliferative retinopathy. Glucose values fluctuate but remain acceptable on current insulin regimen.',
                symptoms: ['Blurry Vision'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '7.5', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'HbA1c (Glycated Hemoglobin)', value: '7.4', unit: '%', range: '4.0 - 5.6', flag: 'High' }
                ],
                prescription: [
                    { name: 'Insulin Aspart (Novorapid)', dosage: '6 IU', timing: 'Before breakfast/lunch/dinner', quantity: 3, duration: 30 },
                    { name: 'Insulin Glargine (Lantus)', dosage: '14 IU', timing: 'Before sleep', quantity: 2, duration: 30 }
                ]
            },
            {
                date: 'Nov 10, 2025', doctor: 'Dr. Harrison',
                diagnosis: 'Type 1 Diabetes Mellitus. Stable renal values.',
                clinicalNotes: 'Glucosuria screening and renal profile review. Serum creatinine is normal. Patient recommended continuous glucose sensor logs.',
                symptoms: ['Fatigue & Lethargy'],
                labResults: [
                    { name: 'Fasting Blood Glucose', value: '7.8', unit: 'mmol/L', range: '3.9 - 5.6', flag: 'High' },
                    { name: 'Serum Creatinine', value: '88', unit: 'µmol/L', range: '62 - 115', flag: 'Normal' }
                ],
                prescription: [
                    { name: 'Insulin Aspart (Novorapid)', dosage: '6 IU', timing: 'Before breakfast/lunch/dinner', quantity: 3, duration: 30 },
                    { name: 'Insulin Glargine (Lantus)', dosage: '14 IU', timing: 'Before sleep', quantity: 2, duration: 30 }
                ]
            }
        ]
    }
];

let selectedPatient = null;

document.addEventListener('DOMContentLoaded', () => {
    // Check if there is already an active patient from the session
    const activePatientId = sessionStorage.getItem('selectedPatientId') || 'P067823'; // Default to Le Hoang D
    selectPatient(activePatientId);
});

// Select a patient and load history details
function selectPatient(id) {
    selectedPatient = patientsDb.find(p => p.id === id) || patientsDb[3]; // Default to Le Hoang D if not found
    if (!selectedPatient) return;

    // Populate patient records
    document.getElementById('detName').textContent = selectedPatient.name;
    document.getElementById('detId').textContent = selectedPatient.id;
    document.getElementById('detAgeGender').textContent = `${selectedPatient.age} yrs / ${selectedPatient.gender}`;
    document.getElementById('detBlood').textContent = selectedPatient.bloodGroup;
    document.getElementById('detSupervisor').textContent = selectedPatient.supervisor;
    document.getElementById('detSupervisorPhone').textContent = selectedPatient.supervisorPhone;

    // Draw Chart.js equivalent HTML5 Canvas Line Chart
    drawGlucoseChart(selectedPatient.glucoseTrend);

    // Load Timeline items
    renderTimeline(selectedPatient.timeline);
}

// Render Medical Timeline Entries
function renderTimeline(timeline) {
    const container = document.getElementById('consultationTimeline');
    if (!container) return;

    container.innerHTML = '';
    
    if (!timeline || timeline.length === 0) {
        container.innerHTML = `<div style="color: var(--doctor-text-muted); font-size: 0.85rem; padding: 20px 0;">No consultation history found matching the filters.</div>`;
        return;
    }

    timeline.forEach((t, index) => {
        const item = document.createElement('div');
        item.className = `timeline-item ${index === 0 ? 'active' : ''}`;
        
        item.innerHTML = `
            <div class="timeline-node"></div>
            <div class="timeline-content-card">
                <div class="timeline-header-meta" style="margin-bottom: 6px;">
                    <span class="timeline-date">${t.date}</span>
                    <span class="timeline-doc">Examiner: ${t.doctor}</span>
                </div>
                <div class="timeline-diagnosis" style="margin-bottom: 12px; font-size: 0.85rem;">
                    <strong>Diagnosis:</strong> ${t.diagnosis}
                </div>
                <button type="button" class="btn btn-secondary btn-sm" onclick="openTimelineDetail('${t.date}', ${index})" style="font-size: 0.7rem; gap: 4px; padding: 4px 8px;">
                    <i class="fas fa-eye"></i> View Detail
                </button>
            </div>
        `;
        container.appendChild(item);
    });
}

// HTML5 Canvas line chart drawing function
function drawGlucoseChart(data) {
    const canvas = document.getElementById('glucoseTrendChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    // Clear previous drawing
    ctx.clearRect(0, 0, width, height);

    // Chart dimensions configuration
    const padding = { top: 20, right: 30, bottom: 30, left: 40 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'];

    // Y-Axis limits (0 to 12 mmol/L)
    const yMax = 12;
    const yMin = 0;

    // Draw grid lines and Y-axis scale
    ctx.strokeStyle = '#e2e8f0';
    ctx.lineWidth = 1;
    ctx.fillStyle = '#64748b';
    ctx.font = '10px Plus Jakarta Sans';
    ctx.textAlign = 'right';

    const yLines = 4;
    for (let i = 0; i <= yLines; i++) {
        const yVal = (yMax / yLines) * i;
        const y = padding.top + chartHeight - (chartHeight * (yVal / yMax));
        
        // Horizontal line
        ctx.beginPath();
        ctx.moveTo(padding.left, y);
        ctx.lineTo(width - padding.right, y);
        ctx.stroke();

        // Y label
        ctx.fillText(yVal.toFixed(1), padding.left - 8, y + 3);
    }

    // Draw target healthy blood sugar shade (3.9 to 5.6 mmol/L)
    const normalYMin = padding.top + chartHeight - (chartHeight * (5.6 / yMax));
    const normalYMax = padding.top + chartHeight - (chartHeight * (3.9 / yMax));
    ctx.fillStyle = 'rgba(16, 185, 129, 0.08)'; // Light success green
    ctx.fillRect(padding.left, normalYMin, chartWidth, normalYMax - normalYMin);

    // Target boundary line indicators (dashed)
    ctx.strokeStyle = 'rgba(16, 185, 129, 0.4)';
    ctx.setLineDash([4, 4]);
    ctx.beginPath();
    ctx.moveTo(padding.left, normalYMin);
    ctx.lineTo(width - padding.right, normalYMin);
    ctx.moveTo(padding.left, normalYMax);
    ctx.lineTo(width - padding.right, normalYMax);
    ctx.stroke();
    ctx.setLineDash([]); // Reset line dash

    // Draw X-Axis labels
    ctx.textAlign = 'center';
    ctx.fillStyle = '#64748b';
    const xPoints = [];
    
    for (let i = 0; i < months.length; i++) {
        const x = padding.left + (chartWidth / (months.length - 1)) * i;
        xPoints.push(x);

        // X label
        ctx.fillText(months[i], x, height - 10);
    }

    // Plot glucose trend lines
    const dataPoints = data.map((val, idx) => {
        const x = xPoints[idx];
        const y = padding.top + chartHeight - (chartHeight * (val / yMax));
        return { x, y, val };
    });

    // Draw line connecting data points
    ctx.strokeStyle = '#0f766e'; // Medical Teal
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(dataPoints[0].x, dataPoints[0].y);
    for (let i = 1; i < dataPoints.length; i++) {
        ctx.lineTo(dataPoints[i].x, dataPoints[i].y);
    }
    ctx.stroke();

    // Draw points and values text labels
    dataPoints.forEach(point => {
        // Point node
        ctx.fillStyle = '#0f766e';
        ctx.beginPath();
        ctx.arc(point.x, point.y, 5, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#ffffff';
        ctx.beginPath();
        ctx.arc(point.x, point.y, 2.5, 0, Math.PI * 2);
        ctx.fill();

        // Value text
        ctx.fillStyle = '#0f172a';
        ctx.font = 'bold 9px Plus Jakarta Sans';
        ctx.fillText(point.val.toFixed(1), point.x, point.y - 10);
    });
}

// Filter timeline list by date input
function filterTimeline() {
    const query = document.getElementById('timelineSearchInput').value.toLowerCase().trim();
    if (!selectedPatient) return;

    if (!query) {
        renderTimeline(selectedPatient.timeline);
        return;
    }

    const filtered = selectedPatient.timeline.filter(t => t.date.toLowerCase().includes(query));
    renderTimeline(filtered);
}

// Open timeline detail modal
function openTimelineDetail(date, index) {
    if (!selectedPatient) return;

    // Since index corresponds to the filtered view, find target by date in the patient's timeline list
    const visit = selectedPatient.timeline.find(t => t.date === date);
    if (!visit) return;

    document.getElementById('modalTitle').textContent = `Consultation Details - ${visit.date}`;
    document.getElementById('modalDate').textContent = visit.date;
    document.getElementById('modalDoctor').textContent = visit.doctor;
    document.getElementById('modalDiagnosis').textContent = visit.diagnosis;
    document.getElementById('modalNotes').textContent = visit.clinicalNotes || 'No additional clinical notes recorded.';

    // Symptoms Badges
    const symptomsContainer = document.getElementById('modalSymptoms');
    symptomsContainer.innerHTML = '';
    if (visit.symptoms && visit.symptoms.length > 0) {
        visit.symptoms.forEach(s => {
            const span = document.createElement('span');
            span.className = 'timeline-symptom-badge';
            span.textContent = s;
            symptomsContainer.appendChild(span);
        });
    } else {
        symptomsContainer.innerHTML = '<span style="font-size: 0.8rem; color: var(--doctor-text-muted);">No symptoms logged</span>';
    }

    // Lab Results Table
    const labsTableBody = document.getElementById('modalLabsTableBody');
    labsTableBody.innerHTML = '';
    if (visit.labResults && visit.labResults.length > 0) {
        visit.labResults.forEach(l => {
            const tr = document.createElement('tr');
            
            let flagClass = '';
            if (l.flag === 'High' || l.flag === 'Low') {
                flagClass = 'style="color: var(--doctor-danger); font-weight: bold;"';
            } else {
                flagClass = 'style="color: var(--doctor-success);"';
            }

            tr.innerHTML = `
                <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border);">${l.name}</td>
                <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border); font-weight: 600;">${l.value} <span style="font-size: 0.75rem; color: var(--doctor-text-muted); font-weight: normal;">${l.unit}</span></td>
                <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border); color: var(--doctor-text-muted);">${l.range}</td>
                <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border);" ${flagClass}>${l.flag}</td>
            `;
            labsTableBody.appendChild(tr);
        });
    } else {
        labsTableBody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 15px; color: var(--doctor-text-muted); font-size: 0.8rem;">No lab tests ordered in this session</td></tr>';
    }

    // Prescription Table
    const prescriptionTableBody = document.getElementById('modalPrescriptionTableBody');
    prescriptionTableBody.innerHTML = '';
    if (visit.prescription && visit.prescription.length > 0) {
        visit.prescription.forEach(p => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border); font-weight: 600;">${p.name}</td>
                <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border);">${p.dosage}</td>
                <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border); font-style: italic; color: var(--doctor-primary); font-weight: 500;">${p.timing}</td>
                <td style="padding: 10px 12px; border-bottom: 1px solid var(--doctor-border);">${p.duration} days / ${p.quantity} pcs</td>
            `;
            prescriptionTableBody.appendChild(tr);
        });
    } else {
        prescriptionTableBody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 15px; color: var(--doctor-text-muted); font-size: 0.8rem;">No medications prescribed in this session</td></tr>';
    }

    // Open Modal Overlay
    document.getElementById('timelineDetailModal').classList.add('open');
}

// Close timeline detail modal
function closeTimelineDetail() {
    document.getElementById('timelineDetailModal').classList.remove('open');
}
