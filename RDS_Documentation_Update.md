# SE2033_SWP391_Group1_RDS_Document - Project-Aligned Documentation Update

> **Ghi chú triển khai:** Phần tài liệu dưới đây được xây dựng và chuẩn hóa chính xác 100% dựa trên mã nguồn thực tế của dự án (*Code-First to RDS Documentation*), giải quyết sự chênh lệch giữa bản thảo tài liệu cũ và hệ thống đã triển khai. Các mã Use Case được đồng bộ nhất quán giữa Phần II (**Requirement Specifications**) và Phần III (**Design Specifications**):
> - `3.1 UC-AD-04: Manage Medication Catalog` (Bản thảo cũ ở Phần II là `UC-11`)
> - `8.1 UC-PAT-07: Chat with AI Assistant` (Bản thảo cũ ở Phần II là `UC-32`)
> - `8.4 UC-AD-09: Monitor AI System Performance` (Bản thảo cũ ở Phần II là `UC-35`)

---

# II. Requirement Specifications

## 3. System Configuration & Master Data Management

### 3.1 UC-AD-04: Manage Medication Catalog

#### a. Functional Description

* **ID and Name:** `UC-AD-04: Manage Medication Catalog`
* **Created By:** `AnhNT` (Updated from code deployment)
* **Date Created:** `01/06/2026`
* **Primary Actor:** `Admin`
* **Secondary Actors:** `Database`, `SystemLogService`
* **Description:** Admin manages the master catalog of medications (`medicationName`, `form` [Dosage Form], `concentration` [Strength], `administrationRoute`, and `usageInstruction`) that doctors select when prescribing treatment (`UC-DOC-06`) and that the AI Assistant (`UC-PAT-07`) retrieves when answering patients' medication-related questions (`get_prescriptions`).
* **Trigger:** Admin selects the **"Quản lý Thuốc"** menu option on the Admin sidebar (`/admin/medicines`).
* **Preconditions:**
  * `PRE-1.` Admin has successfully logged into the system and holds active Administrator access permissions.
* **Postconditions:**
  * `POST-1.` A new medication record (`Medication` entity) is validated, assigned a system-generated ID (`MED-NN`), and persisted to the `Medication` table, or an existing record is updated.
  * `POST-2.` The medication's `Status` reflects the requested state (`Active` or `Clocked`).
  * `POST-3.` A system audit log entry is recorded via `SystemLogService` (`CREATE`, `UPDATE`, or `DELETE`).
  * `POST-4.` Only medications with `Status = Active` are available for doctors to select when prescribing.
* **Normal Flow:**
  1. Admin navigates to the Medicine Management screen (`GET /admin/medicines`). (See `3.1.E1` if empty)
  2. System computes summary statistics (`MedicationService.getSummary()`: `totalMedications`, `activeMedications`, `clockedMedications`, `routes.size()`) and displays a paginated list of medications (`medicationsPage`, default 7 rows/page, sorted by `medicationId` descending).
  3. Admin optionally filters by `status` (`active`, `clocked`), `form` (`tablet`, `capsule`, `injection`), or `route` (`Oral`, `Subcutaneous`, `Intravenous`, `Intramuscular`), or enters a search keyword (`keyword`) matching medication names and routes. (See `3.1.A1` for editing, `3.1.A2` for locking/restoring)
  4. Admin clicks the **"Thêm Thuốc"** (`#btnShowAddModal`) button.
  5. System opens the Add Medicine modal (`#addModal`).
  6. Admin inputs `Tên Thuốc` (`medicationName`, required, max 100 chars, no special symbols), `Dạng bào chế` (`form`, required combo box), `Nồng độ/Hàm lượng` (`concentration`, optional), `Đường dùng` (`administrationRoute`, required combo box), and `Hướng dẫn sử dụng` (`usageInstruction`, optional textarea), then clicks **"Lưu Thuốc"** (`POST /admin/medicines/add`). (See `3.1.E2` if duplicate or invalid name)
  7. System trims string inputs (`validateAndTrimMedication`), validates that `medicationName` does not exist (`existsByMedicationNameIgnoreCase`), generates a sequential ID (`MED-NN`), initializes `Status = Active`, saves the entity, and logs the `CREATE` action.
  8. System redirects to the Medicine Management list (`redirect:/admin/medicines`) displaying a success notification banner (`Thêm mới thuốc ... thành công!`).
* **Alternative Flows:**
  * **`3.1.A1 Edit an existing medication`**
    1. Admin clicks the **Edit** (`fas fa-pen`) icon on a medication row (`openEditModal(medicationId)`).
    2. System displays the Edit Medicine modal pre-filled with existing record values.
    3. Admin modifies one or more fields and submits (`POST /admin/medicines/edit/{id}`). (See `3.1.E2` if duplicate name)
    4. System validates inputs, verifies name uniqueness if changed, updates the database record, logs the `UPDATE` action in `SystemLogService`, and returns to step 8 of the Normal Flow.
  * **`3.1.A2 Clock / Restore a medication (Soft Delete / Restore)`**
    1. Admin clicks the lock/unlock icon (`fas fa-lock` / `fas fa-lock-open`) on a medication row.
    2. System opens a confirmation modal (`#confirmModal`) displaying action impact and sub-message (`Hành động này không thể hoàn tác`).
    3. Admin confirms (`POST /admin/medicines/soft-delete/{id}` or `/admin/medicines/restore/{id}`).
    4. System updates `Status` to `Clocked` (if Active) or `Active` (if Clocked) via `medicationService.softDelete(id)` / `restore(id)`, and returns to step 8 of the Normal Flow.
* **Exceptions:**
  * **`3.1.E1 No medications found in catalog`**
    1. If the database table is empty or no records match active filter criteria (`medications.isEmpty()`), the table displays an empty state row: icon `fas fa-pills` with message `"No medications found"`.
  * **`3.1.E2 Duplicate medication name or invalid characters`**
    1. If `medicationName` already exists (`existsByMedicationNameIgnoreCase`), system throws `IllegalArgumentException("Tên thuốc ... đã tồn tại trong hệ thống!")`.
    2. If inputs contain special symbols (`^[\\p{L}0-9\\s\\-\\.\\/\\+]+$`), system rejects and returns `"Tên thuốc không được chứa ký tự đặc biệt!"`.
    3. System redirects back to `/admin/medicines` displaying the error alert banner (`redirectAttributes.addAttribute("error", ...)`).
* **Priority:** `High`
* **Frequency of Use:** Performed by Admin when establishing or maintaining master pharmaceutical data (several times weekly), while read heavily clinic-wide.
* **Business Rules:** `BR-MED-1`, `BR-MED-2`, `BR-MED-3`, `BR-MED-4`, `BR-MED-5`, `BR-MED-6`

#### b. Business Rules (`UC-AD-04`)

| ID | Business Rule | Business Rule Description |
| :--- | :--- | :--- |
| **BR-MED-1** | Unique Medication Name | The system must reject creating or updating a medication whose name matches (`case-insensitive`) an existing `medicationName` in the catalog (`existsByMedicationNameIgnoreCase`). |
| **BR-MED-2** | Auto-generated Medication ID | Each new medication is assigned a system-generated ID in the format `"MED-NN"`, where `NN` is the next sequential integer after the highest existing numeric suffix parsed from existing `MED-XX` IDs (`generateMedicationId()`). IDs are immutable. |
| **BR-MED-3** | Soft Delete Only | Removing a medication from active prescribing only alters its `Status` to `"Clocked"` (`/soft-delete/{id}`); the record is never physically deleted (`DELETE` is reserved for system administration cleanup only), ensuring historical prescriptions remain intact. |
| **BR-MED-4** | Default Status & Trimming | Every newly created medication is initialized with `Status = "Active"`. All input strings (`medicationName`, `form`, `concentration`, `administrationRoute`, `usageInstruction`) must be trimmed of leading/trailing whitespace before validation (`validateAndTrimMedication`). |
| **BR-MED-5** | Restricted Value Sets | `form` (Dosage Form) must be one of `{tablet, capsule, injection}`; `administrationRoute` must be one of `{Oral, Subcutaneous, Intravenous, Intramuscular}` enforced via single-choice combo box selections. |
| **BR-MED-6** | Special Character Restriction | `medicationName` must strictly conform to the regex `^[\\p{L}0-9\\s\\-\\.\\/\\+]+$` (Unicode letters, digits, spaces, hyphens, dots, slashes, and plus signs only). Special symbols are rejected. |

---

## 8. AI Assistant & System Monitoring

### 8.1 UC-PAT-07: Chat with AI Assistant

#### a. Functional Description

* **ID and Name:** `UC-PAT-07: Chat with AI Assistant`
* **Created By:** `AnhNT` (Updated from code deployment)
* **Date Created:** `01/06/2026`
* **Primary Actor:** `Patient`
* **Secondary Actors:** `AI Assistant (Dual Architecture: Ollama Local GGUF & Google Gemini API)`, `AI Hybrid RAG Intent Processing (AiTool)`, `Database`
* **Description:** Patient converses in natural Vietnamese (`100% Tiếng Việt chuẩn y khoa`) with the AI health assistant (`/patient/chat`). The system features a **Dual AI Architecture** allowing seamless switching between local models (`Ollama`) and cloud models (`Gemini API`). Every query passes through a two-stage hybrid pipeline: **Stage-1** classifies intent via fast-track keyword matching (`checkKeywordToolFallback`) and follow-up context detection (`checkFollowUp`), or calls the LLM router (`classifyAndGetToolJson`) to determine if personal medical data is required (`get_general_record`, `get_clinical_examination`, `get_treatment_plan`, `get_lab_results`, `get_prescriptions`), executing exact RAG SQL queries and automatic AOP audit logging (`AiToolAspect` -> `ai_patient_access_log`). **Stage-2** builds a comprehensive clinical prompt (`STAGE_2_SYSTEM_PROMPT`) and generates a personalized, empathetic answer formatted in clean Markdown, delivered via real-time SSE streaming (`/patient/chat/api/stream`) or synchronous REST (`/patient/chat/api/send`).
* **Trigger:** Patient opens the **"Trò chuyện với trợ lý AI"** (`/patient/chat`) screen and submits a question in the message box (`#messageInput`).
* **Preconditions:**
  * `PRE-1.` Patient has successfully logged into the system (`HttpSession` authenticated as `Patient`).
  * `PRE-2.` The global AI system switch is enabled by Admin (`aiMonitoringService.isAiEnabled() == true`). (See `8.1.E1` if disabled)
  * `PRE-3.` The selected AI Assistant model (`Ollama` or `Gemini`) is online and accessible.
* **Postconditions:**
  * `POST-1.` If this is the initial exchange of a new session, an `AIConversation` entity (`UUID` id, default topic `"Hỗ trợ chăm sóc tiểu đường"`, `CreatedAt`) is persisted.
  * `POST-2.` Patient question and AI answer are saved as two `AIMessage` records (`Sender = 'Patient'` and `'AI'`, timestamped).
  * `POST-3.` If Stage-1 RAG accessed personal medical data, exactly one audit entry (`AiPatientAccessLog`) is automatically recorded by `AiToolAspect` with exact `patientId`, `dataType`, `question`, and `latencyMs`.
  * `POST-4.` On the first exchange pair (`countByConversationId == 1`), the conversation `Topic` is auto-generated from the prompt (`generateTopic`).
* **Normal Flow:**
  1. Patient navigates to `/patient/chat`. System loads existing `AIConversation` list (`findConversationsByPatient`) and message history (`findMessagesByConversation`). If none exists, a new default conversation is created. (See `8.1.A2` for new conversation, `8.1.A3` for history review/deletion)
  2. Patient enters a question (`maxlength 2000`) and clicks Send (`#sendBtn`) or presses Enter.
  3. Frontend transmits request to `/patient/chat/api/stream` (or `/send`) with `patientId`, `conversationId`, and `question`.
  4. Backend checks `aiMonitoringService.isAiEnabled()`. System retrieves up to the last 20 messages (`getFormattedConversationHistory(conversationId, 3)` / `20`) formatted for prompt context.
  5. **Stage-1 Intent Classification:** System runs `checkKeywordToolFallback` (fast-track accent-insensitive matching for `"đơn thuốc"`, `"kết quả xét nghiệm"`, `"lịch tái khám"`, `"phác đồ"`, `"hồ sơ y tế"`). If inconclusive, it queries the LLM router (`classifyAndGetToolJson`). If the question is a follow-up (`"thuốc đó"`, `"chỉ số này"`, `"phác đồ đó"`), system auto-detects prior context and triggers the corresponding RAG action.
  6. If RAG is triggered, system executes `fetchDataFromRepository(action, patientId)` through `AiToolImpl` (`getPrescriptions`, `getLabResults`, `getClinicalExamination`, `getTreatmentPlan`, `getGeneralRecord`). `AiToolAspect` intercepts the invocation and inserts a record into `ai_patient_access_log`.
  7. **Stage-2 Response Generation:**
     * If RAG returns no data (`"(Không có dữ liệu)"`), system directly constructs a helpful Vietnamese guidance without invoking the LLM (`"Chào bạn, hiện tại trong hệ thống chưa có bản ghi... "` across SSE/REST).
     * Otherwise, system constructs `buildStage2Prompt` applying clinical rules (`STAGE_2_SYSTEM_PROMPT`: 100% Vietnamese, no JSON/raw tags, clean markdown lists, required disclaimer) and calls `aiProviderManager.generateStreamWithModel` (or `generateWithModel`).
  8. Frontend receives SSE tokens (`event: token`), rendering live markdown (`renderMarkdown`) inside `messagesContainer`.
  9. Upon completion (`event: done`), backend saves the AI's answer (`AIMessage`) and updates the conversation `Topic` if it was the first message pair.
* **Alternative Flows:**
  * **`8.1.A1 Switch AI Assistant Model (Dual Architecture)`**
    1. Patient selects or specifies a different active assistant (`/patient/chat/api/send/assistant/{assistantId}` or `/stream/assistant/{assistantId}`).
    2. System resolves `AIAssistant` (`aiAssistantService.findById`), applies the engine (`Ollama` vs `Gemini`), and processes the query under that model's context.
  * **`8.1.A2 Start New Conversation`**
    1. Patient clicks **"Bắt đầu cuộc trò chuyện mới"** (`#newChatBtn`, `fas fa-plus-circle`).
    2. Frontend clears active messages and creates a brand-new `AIConversation` (`UUID`).
  * **`8.1.A3 View / Delete Conversation History`**
    1. Patient selects a past conversation item in `#historyList`; system loads its messages via `GET /patient/chat/api/history/{conversationId}`.
    2. Patient clicks the **Delete** (`fas fa-trash-alt`) icon; system opens confirmation modal (`#deleteConfirmModal`). Upon confirmation, calls `DELETE /patient/chat/api/conversation/{conversationId}`, removing all linked messages and the conversation entity.
* **Exceptions:**
  * **`8.1.E1 AI System Disabled by Admin (Kill-Switch)`**
    1. If `aiMonitoringService.isAiEnabled() == false`, system immediately returns the token/message: `"⚠️ **Hệ thống Trợ lý AI hiện đang được tạm tắt để bảo trì hoặc giám sát.**\n\nVui lòng quay lại sau, hoặc liên hệ quản trị viên để biết thêm chi tiết."` without contacting Ollama or Gemini.
  * **`8.1.E2 AI Service Unavailable / Timeout`**
    1. If API connection times out or fails during generation, system logs the error and returns `"Xin lỗi, hiện tại hệ thống AI đang gặp sự cố khi xử lý câu hỏi của bạn. Vui lòng thử lại sau."`
* **Priority:** `High`
* **Frequency of Use:** Core patient feature used multiple times daily per session.
* **Business Rules:** `BR-CHAT-1`, `BR-CHAT-2`, `BR-CHAT-3`, `BR-CHAT-4`, `BR-CHAT-5`, `BR-CHAT-6`, `BR-CHAT-7`

#### b. Business Rules (`UC-PAT-07`)

| ID | Business Rule | Business Rule Description |
| :--- | :--- | :--- |
| **BR-CHAT-1** | AI Availability Gate (Kill-Switch) | While the AI switch (`isAiEnabled`) is set to `false` by Admin, every chat request (`send` or `stream`) must immediately receive the fixed maintenance warning (`"⚠️ **Hệ thống Trợ lý AI hiện đang được tạm tắt...**"`) without invoking LLM or RAG services. |
| **BR-CHAT-2** | Two-Stage Hybrid & Fast-Track RAG | Every query passes through Stage-1 fast-track keyword classification (`checkKeywordToolFallback`) and follow-up context detection (`checkFollowUp`). If RAG is required, `fetchDataFromRepository` queries only data matching the authenticated `patientId` (strict data isolation). Stage-2 combines RAG results and clinical system prompt (`STAGE_2_SYSTEM_PROMPT`). |
| **BR-CHAT-3** | Personal Data Isolation & AOP Auditing | RAG calls through `AiToolImpl` only accept the authenticated `patientId`. Every successful or attempted invocation of `AiTool.*(..)` is intercepted by `AiToolAspect`, recording `AiPatientAccessLog` (`patientId`, `dataType`, `question`, `latencyMs`) automatically. |
| **BR-CHAT-4** | Conversation Ownership & Auto-Topic | A `conversationId` must belong to the requesting patient (`validateConversationAccess`). On the first message pair (`messageCount == 1`), the `Topic` is auto-generated (`generateTopic`) and stored in `AIConversation`. |
| **BR-CHAT-5** | Context Window Limit | To optimize latency and token limits, system retrieves up to the last 20 messages (`TOP 20` via `aiMessageService`) as context for prompt building. |
| **BR-CHAT-6** | Mandatory Disclaimer & Vietnamese Standard | Every AI response must adhere to 100% natural Vietnamese (`Tiếng Việt chuẩn y khoa`), format tables/prescriptions clearly in Markdown, avoid raw JSON/IDs, and terminate with: `"Thông tin này mang tính tham khảo, không thay thế tư vấn của bác sĩ."` |
| **BR-CHAT-7** | Dual AI Architecture Model Selection | If an `assistantId` is specified (`/assistant/{id}`), system dynamically switches model (`Ollama Local` vs `Gemini Cloud API`) while preserving conversation continuity across `AIConversation`. |

---

### 8.4 UC-AD-09: Monitor AI System Performance

#### a. Functional Description

* **ID and Name:** `UC-AD-09: Monitor AI System Performance`
* **Created By:** `AnhNT` (Updated from code deployment)
* **Date Created:** `01/06/2026`
* **Primary Actor:** `Admin`
* **Secondary Actors:** `Database` (`ai_patient_access_log`, `AI_Assistant`), `AiMonitoringService`
* **Description:** Admin monitors the real-time operational health, response latency, distinct user load, and model configurations of the AI Assistant subsystem (`GET /admin/monitoring`). The screen displays overview stat cards (average response latency computed from real RAG queries, active model display, today's distinct user count, and a global AI toggle switch), an **AI Models Management Panel** (`Dual AI Architecture` for switching between Ollama Local and Gemini Cloud API), and a searchable, paginated RAG Patient Access Log table (`ai_patient_access_log`).
* **Trigger:** Admin selects the **"Giám sát hệ thống AI"** menu option on the Admin sidebar (`/admin/monitoring`).
* **Preconditions:**
  * `PRE-1.` Admin is successfully logged in with active Administrator access permissions.
* **Postconditions:**
  * `POST-1.` Toggling the AI switch (`POST /admin/monitoring/api/toggle-ai`) immediately updates `aiMonitoringService.setAiEnabled(enabled)` in application memory (`AtomicBoolean`), instantly enabling or blocking all patient chat queries (`UC-PAT-07`).
  * `POST-2.` Switching the active AI model (`POST /admin/monitoring/api/switch-model`) updates the active `AIAssistant` status, changing the underlying LLM engine (`Ollama` vs `Gemini`) for all subsequent patient conversations.
  * `POST-3.` The Patient Access Log table accurately displays filtered (`accessPatientId`, `dataType`, `accessFromDate`, `accessToDate`) and paginated (`accessPage`, default size 7) RAG audit entries.
* **Normal Flow:**
  1. Admin opens `/admin/monitoring`.
  2. System calculates and displays 4 overview Stat Cards:
     * **`Tốc độ trung bình`** (`avgLatencyMs` from `aiPatientAccessLogRepository.getAverageLatencyMs()`, formatted as `ms` or `s`).
     * **`Tên model AI`** (`activeAssistant.aiName + " (" + activeAssistant.modelName + ")"` or default `ollama.model`).
     * **`Người dùng hôm nay`** (`todayUsersCount` from `countDistinctPatientsToday(LocalDate.now().atStartOfDay())`, distinct `patientId != 'UNKNOWN'`).
     * **`Trạng thái AI`** (`aiEnabled` toggle state check: `"Đang hoạt động"` / `"Tạm tắt"`).
  3. System displays the **AI Models Management Panel** (`allAssistants`), distinguishing Local Ollama (`fas fa-server`) vs Cloud Gemini (`fas fa-cloud`), showing active badge (`Đang kích hoạt`) and **"Chuyển sang model này"** button (`switchActiveAssistant`).
  4. System displays the **Nhật ký truy xuất dữ liệu bệnh nhân (RAG Access Logs)** paginated table (default 7 rows/page, sorted by `accessedAt` descending). (See `8.4.E1` if empty)
  5. Admin optionally filters by `accessPatientId` (partial match), `dataType` (`get_general_record`, `get_clinical_examination`, `get_treatment_plan`, `get_lab_results`, `get_prescriptions`), and `accessFromDate` – `accessToDate`, then clicks **"Lọc"** (`GET /admin/monitoring?activeTab=access&...`). (See `8.4.A1` for toggling AI, `8.4.A2` for model switching, `8.4.A3` for filter reset)
  6. System executes JPA `Specification<AiPatientAccessLog>` with predicates (`like patientId`, `equal dataType`, `greaterThanOrEqualTo fromDate`, `lessThanOrEqualTo toDate`) and reloads the paginated table (`accessLogs`).
* **Alternative Flows:**
  * **`8.4.A1 Toggle Global AI Switch (Kill-Switch)`**
    1. Admin clicks the **Trạng thái AI** toggle (`#aiToggleInput`).
    2. System opens confirmation modal (`#aiConfirmModal`: `"Xác nhận tạm tắt AI"` / `"Xác nhận bật lại AI"`).
    3. Admin confirms (`#aiConfirmOkBtn`).
    4. Frontend calls `POST /admin/monitoring/api/toggle-ai?enabled=true/false`.
    5. Backend sets `aiMonitoringService.setAiEnabled(enabled)` (`AtomicBoolean`), updates stat card UI (`Đang hoạt động` / `Tạm tắt`), and returns success JSON.
  * **`8.4.A2 Switch Active AI Model (Dual Architecture)`**
    1. Admin clicks **"Chuyển sang model này"** on an inactive AI Assistant card (`openModelSwitchConfirmModal(assistantId)`).
    2. System opens model switch confirmation modal (`#modelSwitchConfirmModal`).
    3. Admin confirms (`POST /admin/monitoring/api/switch-model?assistantId=...`).
    4. Backend executes `aiMonitoringService.switchActiveAssistant(assistantId)`, updating database `status = 'Active'` for selected assistant and `'Inactive'` for others. System reloads page displaying new active model.
  * **`8.4.A3 Reset Filters`**
    1. Admin clicks **"Đặt lại"** (`/admin/monitoring(activeTab='access')`).
    2. System clears all search and date filter inputs and reloads page 1 of unfiltered logs.
* **Exceptions:**
  * **`8.4.E1 No matching RAG access log entries`**
    1. If no entries match filter criteria (`accessLogs.empty`), table renders empty state icon `fas fa-folder-open` with message `"Không tìm thấy dữ liệu truy xuất nào"`.
  * **`8.4.E2 Error switching model`**
    1. If `switchActiveAssistant` throws exception, API returns `ResponseEntity.badRequest()` with error details, displayed as notification to Admin.
* **Priority:** `Medium` (Operational & compliance auditing)
* **Frequency of Use:** Daily/periodic review by Admin for system health check and clinical data privacy monitoring.
* **Business Rules:** `BR-MON-1`, `BR-MON-2`, `BR-MON-3`, `BR-MON-4`, `BR-MON-5`

#### b. Business Rules (`UC-AD-09`)

| ID | Business Rule | Business Rule Description |
| :--- | :--- | :--- |
| **BR-MON-1** | Access Log Immutability & AOP Origin | Entries in `ai_patient_access_log` (`AiPatientAccessLog` entity) are written automatically and exclusively by Spring AOP (`AiToolAspect`) upon RAG method execution. They cannot be created, edited, or deleted from the Admin UI (`read-only` access). |
| **BR-MON-2** | Global Kill-Switch Scope | When `aiMonitoringService.isAiEnabled()` is set to `false`, the setting is stored in `AtomicBoolean` (in-memory) and instantly blocks all patient chat queries across both SSE streaming (`/stream`) and REST (`/send`), serving a standard maintenance alert. |
| **BR-MON-3** | Distinct Daily Users Calculation | The **`Người dùng hôm nay`** stat card (`countDistinctPatientsToday`) executes exact SQL query: `SELECT COUNT(DISTINCT a.patientId) FROM AiPatientAccessLog a WHERE a.accessedAt >= :startOfDay AND a.patientId != 'UNKNOWN'`. |
| **BR-MON-4** | Real-Time Latency Average | **`Tốc độ trung bình`** (`getAverageLatencyMs`) is computed directly from actual RAG execution durations recorded in `latencyMs`: `SELECT AVG(a.latencyMs) FROM AiPatientAccessLog a WHERE a.latencyMs IS NOT NULL`. |
| **BR-MON-5** | Default Log Pagination & Sorting | `getPatientAccessLogs` defaults to `accessSize = 7` records per page, sorted by `accessedAt` in descending order (`Sort.Direction.DESC, "accessedAt"`), ensuring the most recent clinical data accesses appear first. |

---
---

# III. Design Specifications

> Phần Thiết kế hệ thống được triển khai trực tiếp từ các đặc tả yêu cầu ở Phần II, phản ánh chính xác cấu trúc giao diện (`src/main/resources/templates/...`) và truy vấn cơ sở dữ liệu (`src/main/java/com/quan/diabetes/repository/...`) của dự án thực tế.

## 3. System Configuration & Master Data Management

### 3.1 Medication Catalog Management Screen

#### a. Medicine List
This screen allows Admin to view, filter, search, and manage the master list of medications used throughout the system. Related use case:
* `UC-AD-04: Manage Medication Catalog`

**UI Design**

| Field Name | Field Type | Description |
| :--- | :--- | :--- |
| **Search & Filter Fields** | | |
| **Status (`status`)** | Combo Box (Single-Choice) | Filter by All Status / Hoạt động (`active`) / Đang khóa (`clocked`). Default: All Status (`""`). Reloads list on change (`onchange="this.form.submit()"`). |
| **Dosage Form (`form`)** | Combo Box (Single-Choice) | Filter by All Forms / Viên nén (`tablet`) / Viên nang (`capsule`) / Thuốc tiêm (`injection`). Default: All Forms (`""`). |
| **Administration Route (`route`)** | Combo Box (Single-Choice) | Filter by All Routes / Đường uống (`Oral`) / Tiêm dưới da (`Subcutaneous`) / Tiêm tĩnh mạch (`Intravenous`) / Tiêm bắp (`Intramuscular`). Default: All Routes (`""`). |
| **Search Keyword (`keyword`)** | Text Box, String | Search by medication name or administration route (`#searchKeyword`). Default: blank. Submits on Enter or click search icon. |
| **Thêm Thuốc (`#btnShowAddModal`)**| Button | Opens the Add Medicine modal (`#addModal`). |
| **Data Table** | | |
| **Tên thuốc (`Medication Name`)**| Text | Medication name (`medicationName`), with Medication ID (`medicationId`) and Concentration (`concentration`) shown beneath (`med.medicationId + ' — ' + med.concentration`). |
| **Dạng bào chế (`Dosage Form`)**| Badge | `Tablet` / `Capsule` / `Injection`, color-coded (`badge-tablet` / `badge-capsule` / `badge-injection`). |
| **Đường dùng (`Route`)** | Text | Administration route (`administrationRoute`), or `"—"` if not set. |
| **Hướng dẫn sử dụng (`Usage Instruction`)**| Text | Truncated to 80 characters (`#strings.abbreviate`); full text shown as a tooltip (`title`) on hover. |
| **Trạng thái (`Status`)** | Badge | `Active` (green badge `badge-active`) / `Clocked` (grey badge `badge-clocked`). |
| **Data Actions** | | |
| **View (`fas fa-eye`)** | Icon Button | Opens the medication detail modal (`viewDetail(medicationId)`) in read-only mode (`GET /admin/medicines/api/{id}`). |
| **Edit (`fas fa-pen`)** | Icon Button | Opens the Edit Medicine modal (`openEditModal(medicationId)`) pre-filled with current values. |
| **Clock / Restore (`fas fa-lock` / `fas fa-lock-open`)** | Icon Button | Toggles Status between Active and Clocked after confirmation modal (`#confirmModal`); icon switches based on current status (`/soft-delete/{id}` vs `/restore/{id}`). |

**Database Access**

| Table | CRUD | Description |
| :--- | :--- | :--- |
| `Medication` | R | Query filtered, sorted, paginated list of medications and compute catalog statistics (total, active, clocked, oral/injectable counts, distinct routes). |

**SQL Commands / JPA Queries:**
1. **Query filtered, sorted, paginated list of medications (`filterMedications`)**
   ```sql
   SELECT m.* FROM Medication m 
   WHERE (:keyword = '' OR LOWER(m.MedicationName) LIKE LOWER('%' + :keyword + '%') 
          OR LOWER(m.AdministrationRoute) LIKE LOWER('%' + :keyword + '%') 
          OR LOWER(m.Concentration) LIKE LOWER('%' + :keyword + '%'))
     AND (:status = '' OR LOWER(m.Status) = LOWER(:status))
     AND (:form = '' OR LOWER(m.Form) = LOWER(:form))
     AND (:route = '' OR LOWER(m.AdministrationRoute) = LOWER(:route))
   ORDER BY m.MedicationID DESC
   OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY;
   ```
2. **Compute catalog statistics (`getSummary`)**
   ```sql
   SELECT COUNT(*) FROM Medication;
   SELECT COUNT(*) FROM Medication WHERE Status = 'Active';
   SELECT COUNT(*) FROM Medication WHERE Status = 'Clocked';
   SELECT COUNT(*) FROM Medication WHERE Form IN ('tablet', 'capsule'); -- oral formulations
   SELECT COUNT(*) FROM Medication WHERE Form = 'injection'; -- injectable formulations
   SELECT DISTINCT AdministrationRoute FROM Medication WHERE AdministrationRoute IS NOT NULL AND AdministrationRoute != '';
   ```

#### b. Add / Edit Medicine Modal
This modal lets Admin create a new medication or update an existing one. Related use case:
* `UC-AD-04: Manage Medication Catalog`

**UI Design**

| Field Name | Field Type | Description |
| :--- | :--- | :--- |
| **Tên Thuốc (`medicationName`)\*** | Text Box | Name of medication, e.g. `"Metformin"`. Must be unique (`existsByMedicationNameIgnoreCase`), required, max 100 chars, no special symbols (`^[\\p{L}0-9\\s\\-\\.\\/\\+]+$`). Error text displayed in `#nameErrorText`. |
| **Dạng bào chế (`form`)\*** | Combo Box (Single-Choice) | Single selection: `-- Chọn Dạng bào chế --` / `tablet` (Viên nén) / `capsule` (Viên nang) / `injection` (Thuốc tiêm). Required. |
| **Nồng độ/Hàm lượng (`concentration`)** | Text Box | Optional strength label, e.g. `"500mg"`, `"100IU/ml"`. Trimmed before save. |
| **Đường dùng (`administrationRoute`)\*** | Combo Box (Single-Choice) | Single selection: `-- Chọn Đường dùng --` / `Oral` (Đường uống) / `Subcutaneous` (Tiêm dưới da) / `Intravenous` (Tiêm tĩnh mạch) / `Intramuscular` (Tiêm bắp). Required. |
| **Hướng dẫn sử dụng (`usageInstruction`)** | Text Area | Free-text dosing/usage guidance, e.g. `"Uống sau bữa ăn sáng 30 phút"`. Optional, trimmed before save. |
| **Lưu Thuốc (`Save Medicine`)** | Button | Validates and submits form (`POST /admin/medicines/add` or `/admin/medicines/edit/{id}`). |

**Database Access**

| Table | CRUD | Description |
| :--- | :--- | :--- |
| `Medication` | C, U | Create a new medication (`generateMedicationId()`, `Status = Active`) or update existing record after validation. |
| `SystemLog` (`SystemLogService`) | C | Insert audit log record (`CREATE` or `UPDATE` on object `"Medicine"`). |

**SQL Commands / JPA Queries:**
1. **Check unique medication name (`existsByMedicationNameIgnoreCase`)**
   ```sql
   SELECT CASE WHEN COUNT(m) > 0 THEN 1 ELSE 0 END FROM Medication m WHERE LOWER(m.MedicationName) = LOWER(:medicationName);
   ```
2. **Generate next Medication ID (`generateMedicationId`)**
   ```sql
   SELECT MedicationID FROM Medication; -- In-memory numeric suffix max check -> format 'MED-%02d'
   ```
3. **Insert new medication (`create`)**
   ```sql
   INSERT INTO Medication (MedicationID, MedicationName, Form, Concentration, AdministrationRoute, UsageInstruction, Status)
   VALUES (:id, :name, :form, :concentration, :route, :usageInstruction, 'Active');
   ```
4. **Update existing medication (`update`)**
   ```sql
   UPDATE Medication SET MedicationName = :name, Form = :form, Concentration = :concentration, 
                         AdministrationRoute = :route, UsageInstruction = :usageInstruction
   WHERE MedicationID = :id;
   ```

---

## 6. AI Services

### 6.1 AI Health Assistant Chat Screen

#### a. Chat with AI Assistant
This screen lets a Patient converse with the AI health assistant, review past conversations, and manage conversation history. Related use cases:
* `UC-PAT-07: Chat with AI Assistant`
* `UC-AI-01: AI Hybrid RAG Intent Processing`

**UI Design**

| Field Name | Field Type | Description |
| :--- | :--- | :--- |
| **Conversation Panel** | | |
| **Status Badge (`#statusText`)** | Badge | Displays live assistant status (`Online` / dot indicator). |
| **Suggestion Chips (`.suggestion-chip`)** | Button Group | Quick-start chips shown when conversation is empty (`#welcomeMessage`): `🍎 Chế độ ăn`, `💊 Thuốc điều trị`, `🏃 Tập thể dục`, `⚠️ Biến chứng`, `📊 Theo dõi đường huyết`. |
| **Message List (`#messagesContainer`)** | Scrollable Panel | Chronological list of patient and AI messages rendered as chat bubbles (`messageList`); AI messages are rendered from Markdown (`renderMarkdown`). |
| **Loading Indicator (`#loadingIndicator`)** | Animated Panel | Displays typing dots and text `"AI đang suy nghĩ..."` during LLM generation. |
| **Message Input (`#messageInput`)\*** | Text Box | Free-text input box for typing question (`maxlength="2000"`, `placeholder="Nhập câu hỏi của bạn..."`). |
| **Send (`#sendBtn`)** | Button | Submits prompt via `POST /patient/chat/api/stream` (SSE streaming) or `/send` (`fas fa-paper-plane`). |
| **History Sidebar** | | |
| **Bắt đầu cuộc trò chuyện mới (`#newChatBtn`)**| Button | Starts a brand-new conversation for the patient (`fas fa-plus-circle`). |
| **Conversation Item (`#historyItems`)** | List Item | One entry per past conversation (`AIConversation`), showing auto-generated `Topic` (`formatTimeShort` / `formatTime`). Clicking loads message history (`GET /patient/chat/api/history/{conversationId}`). |
| **Delete (`fas fa-trash-alt`)** | Icon Button | Opens confirmation modal (`#deleteConfirmModal`), then deletes selected conversation and all messages (`DELETE /patient/chat/api/conversation/{conversationId}`). |

**Database Access**

| Table | CRUD | Description |
| :--- | :--- | :--- |
| `AI_Conversation` | C, R, U, D | Create conversation (`UUID`, `Topic`, `CreatedAt`) on first visit / new chat; read patient's conversation list; update `Topic` after first exchange (`generateTopic`); delete conversation. |
| `AI_Message` | C, R, D | Persist each patient/AI message pair (`Content`, `Sender`, `Time`, `AIConversationID`); read message history (limited to top 20 recent for prompt context); delete cascade on conversation removal. |
| `AI_Assistant` | R | Resolve which AI assistant/model to use (`Ollama Local` vs `Gemini Cloud API`). |
| `Patient`, `ClinicalExamination`, `TreatmentPlan`, `LabResult`, `PrescriptionDetail` | R (conditional) | Queried only when Stage-1 intent classification triggers RAG actions (`get_prescriptions`, `get_lab_results`, `get_clinical_examination`, `get_treatment_plan`, `get_general_record`) via `AiToolImpl`. |
| `ai_patient_access_log` | C | One row inserted per personal-data access, recorded automatically by AOP aspect around `AiTool` (`AiToolAspect`). |

**SQL Commands / JPA Queries:**
1. **Get or create active conversation (`getOrCreateConversation`)**
   ```sql
   SELECT * FROM AI_Conversation WHERE AIConversationID = :id;
   INSERT INTO AI_Conversation (AIConversationID, PatientID, AIAssistantID, Topic, CreatedAt)
   VALUES (:id, :patientId, :assistantId, :topic, :createdAt);
   ```
2. **Save chat message (`AIMessageService.create`)**
   ```sql
   INSERT INTO AI_Message (Content, Sender, Time, AIConversationID) VALUES (:content, :sender, :time, :conversationId);
   ```
3. **Load recent conversation history for prompt context (`getFormattedConversationHistory`)**
   ```sql
   SELECT TOP 20 Content, Sender, Time FROM AI_Message 
   WHERE AIConversationID = :conversationId ORDER BY Time DESC;
   ```
4. **Retrieve personal RAG data on demand (Example: `getPrescriptions` via `AiToolImpl`)**
   ```sql
   SELECT p.PrescriptionID, p.PrescriptionDate, p.Notes, pd.Quantity, pd.Unit, pd.DosageInstruction, 
          m.MedicationName, m.Concentration, m.Form, pt.TimingName
   FROM Prescription p
   JOIN PrescriptionDetail pd ON p.PrescriptionID = pd.PrescriptionID
   JOIN Medication m ON pd.MedicationID = m.MedicationID
   LEFT JOIN PrescriptionTiming pt ON pd.TimingID = pt.TimingID
   JOIN ClinicalExamination ce ON p.ClinicalExamID = ce.ClinicalExamID
   WHERE ce.PatientID = :patientId ORDER BY p.PrescriptionDate DESC;
   ```
5. **Log personal-data access automatically via AOP (`AiToolAspect`)**
   ```sql
   INSERT INTO ai_patient_access_log (patientId, dataType, accessedAt, question, latencyMs)
   VALUES (:patientId, :dataType, :accessedAt, :question, :latencyMs);
   ```

---

## 8. AI Assistant & System Monitoring

### 8.4 Monitor AI System Performance

#### a. AI System Overview & Models Management
This section gives Admin a real-time snapshot of the AI Assistant's operational health, an interactive toggle to enable/disable the assistant system-wide, and a **Dual AI Architecture** panel to switch active AI models between Ollama Local and Google Gemini Cloud API. Related use case:
* `UC-AD-09: Monitor AI System Performance`

**UI Design**

| Field Name | Field Type | Description |
| :--- | :--- | :--- |
| **Overview Stat Cards** | | |
| **Tốc độ trung bình (`avgLatencyMs`)** | Stat Card | Average RAG execution latency computed from database (`#numbers.formatDecimal`), displayed as `ms` or `s` (`fas fa-stopwatch`). |
| **Tên model AI (`activeModelName`)**| Stat Card | Name of currently active model (`fas fa-brain`), e.g. `Diabetes AI Specialist (diabetes / gemini-1.5-pro)`. |
| **Người dùng hôm nay (`todayUsersCount`)**| Stat Card | Number of distinct patient IDs served today (`fas fa-users`). |
| **Trạng thái AI (`#aiStatusCard`)** | Stat Card + Toggle Switch | Displays `"Đang hoạt động"` / `"Tạm tắt"` (`#aiStatusText`) with checkbox toggle (`#aiToggleInput`). Toggling opens `#aiConfirmModal` before submitting `POST /admin/monitoring/api/toggle-ai`. |
| **AI Models Management Panel** | | |
| **AI Assistant Card (`allAssistants`)**| Interactive Card Group | Displays each configured AI Assistant (`aiName`, `modelName`). Local Ollama (`fas fa-server`) vs Cloud Gemini (`fas fa-cloud`). |
| **Status Badge** | Badge | `Đang kích hoạt` (`Active`) vs `Tạm nghỉ` (`Inactive`). |
| **Chuyển sang model này** | Button | Available on inactive cards (`openModelSwitchConfirmModal(assistantId)`). Opens `#modelSwitchConfirmModal` to submit `POST /admin/monitoring/api/switch-model`. |

**Database Access**

| Table | CRUD | Description |
| :--- | :--- | :--- |
| `ai_patient_access_log` | R | Compute count of distinct patients served today (`todayUsersCount`) and average response latency (`avgLatencyMs`). |
| `AI_Assistant` | R, U | Query list of configured AI assistants (`getAllAssistants`); switch active model status (`switchActiveAssistant`). |

**SQL Commands / JPA Queries:**
1. **Count distinct patients served today (`countDistinctPatientsToday`)**
   ```sql
   SELECT COUNT(DISTINCT a.patientId) FROM ai_patient_access_log a 
   WHERE a.accessedAt >= :startOfToday AND a.patientId != 'UNKNOWN';
   ```
2. **Calculate average response latency (`getAverageLatencyMs`)**
   ```sql
   SELECT AVG(a.latencyMs) FROM ai_patient_access_log a WHERE a.latencyMs IS NOT NULL;
   ```
3. **Switch active AI model (`switchActiveAssistant`)**
   ```sql
   UPDATE AI_Assistant SET Status = 'Inactive' WHERE Status = 'Active';
   UPDATE AI_Assistant SET Status = 'Active' WHERE AIAssistantID = :assistantId;
   ```

#### b. Patient Access Log (RAG Access Logs)
This section lets Admin search, filter, and review every RAG data access (`get_prescriptions`, `get_lab_results`, `get_clinical_examination`, etc.) the AI Assistant has performed on behalf of patients. Related use case:
* `UC-AD-09: Monitor AI System Performance`

**UI Design**

| Field Name | Field Type | Description |
| :--- | :--- | :--- |
| **Search & Filter Controls** | | |
| **Mã Bệnh nhân (`accessPatientId`)** | Text Box | Filter by Patient ID partial match (`placeholder="Lọc theo Mã Bệnh nhân..."`). Default: blank. |
| **Loại dữ liệu (`dataType`)** | Combo Box (Single-Choice) | Filter by `-- Tất cả loại dữ liệu --` / `get_general_record` / `get_clinical_examination` / `get_treatment_plan` / `get_lab_results` / `get_prescriptions`. Default: all types (`""`). |
| **Từ / Đến (`accessFromDate`, `accessToDate`)** | Date Picker (x2) | Filter by access date range (`type="date"`). |
| **Lọc (`fas fa-filter`)** | Button | Submits filter form (`GET /admin/monitoring?activeTab=access&...`). |
| **Đặt lại (`fas fa-redo`)** | Hyperlink Button | Clears all filters (`/admin/monitoring(activeTab='access')`). |
| **Data Table (`data-table`)** | | |
| **ID** | Integer | Access-log entry ID formatted as `#' + access.id`. |
| **Mã Bệnh nhân (`Patient ID`)** | Text | Patient ID whose data was accessed (`access.patientId`). |
| **Loại dữ liệu (`Data Type`)** | Tag / Badge | The RAG action name executed (`access.dataType`, monospace tag `dataType-tag`). |
| **Câu hỏi gốc (`Question`)** | Text | The patient's original prompt (`access.question`), truncated with tooltip (`title`) or `"Không có câu hỏi"` if blank. |
| **Thời gian truy xuất (`Accessed At`)**| DateTime | Timestamp formatted `dd/MM/yyyy HH:mm:ss` (`#temporals.format`). |

**Database Access**

| Table | CRUD | Description |
| :--- | :--- | :--- |
| `ai_patient_access_log` | R | Query filtered (`Specification<AiPatientAccessLog>`), paginated (default 7 rows/page), and sorted (`accessedAt DESC`) list of RAG access audit entries. |

**SQL Commands / JPA Queries:**
1. **Query filtered, paginated patient access log entries (`getPatientAccessLogs`)**
   ```sql
   SELECT a.id, a.patientId, a.dataType, a.accessedAt, a.question, a.latencyMs, a.queryLogId 
   FROM ai_patient_access_log a
   WHERE (:patientId IS NULL OR a.patientId LIKE '%' + :patientId + '%')
     AND (:dataType IS NULL OR a.dataType = :dataType)
     AND (:fromDate IS NULL OR a.accessedAt >= :fromDate)
     AND (:toDate IS NULL OR a.accessedAt <= :toDate)
   ORDER BY a.accessedAt DESC
   OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY;
   ```
2. **Count total filtered log entries for pagination**
   ```sql
   SELECT COUNT(*) FROM ai_patient_access_log a
   WHERE (:patientId IS NULL OR a.patientId LIKE '%' + :patientId + '%')
     AND (:dataType IS NULL OR a.dataType = :dataType)
     AND (:fromDate IS NULL OR a.accessedAt >= :fromDate)
     AND (:toDate IS NULL OR a.accessedAt <= :toDate);
   ```
