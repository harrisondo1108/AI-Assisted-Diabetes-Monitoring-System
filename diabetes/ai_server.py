from fastapi import FastAPI
from pydantic import BaseModel
from llama_cpp import Llama
import json
import re
import uvicorn
import asyncio # Thư viện bắt buộc để mở khóa luồng chạy của FastAPI

app = FastAPI()

# 1. Trỏ đường dẫn tới file GGUF Q6_K mới nhất của bạn
model_path = r"C:\Users\Acer\diabeteModel\diabetes-consultant-q6_k.gguf"

print("Đang khởi động AI...")
# Đẩy 100% lên GPU (n_gpu_layers=-1) để giải quyết lỗi chậm 30s
llm = Llama(
    model_path=model_path, 
    n_ctx=4096,       
    n_gpu_layers=-1,  
    n_threads=4,      
    verbose=False
)
print("Khởi động hoàn tất!")

class ChatRequest(BaseModel):
    patient_id: str
    message: str
    context_data: str = ""
    conversation_history: str = ""

# =====================================================================
# SYSTEM PROMPT 1: LUỒNG ĐIỀU HƯỚNG VÀ TRẢ LỜI KIẾN THỨC CHUNG
# =====================================================================
SYSTEM_PROMPT_TOOL = """Bạn là một Bác sĩ nội tiết và trợ lý AI thông minh, chuyên tư vấn về bệnh tiểu đường.

[HƯỚNG DẪN XỬ LÝ - ĐỌC KỸ VÀ LÀM THEO LÀ NHIỆM VỤ TỐI THƯỢNG]
Dựa vào câu hỏi của người dùng, hãy thực hiện 1 trong 3 hành động sau:

1. NẾU LÀ CÂU HỎI KIẾN THỨC, GIẢI THÍCH CƠ CHẾ SINH HỌC HOẶC GIAO TIẾP THÔNG THƯỜNG:
- Ví dụ: "Đường trong cơ thể tôi sinh ra từ đâu?", "Tiểu đường ăn gì?", "Tại sao tôi hay khát nước?", "Chào bác sĩ".
- Hành động: TRẢ LỜI TRỰC TIẾP, chi tiết, khoa học và thân thiện. KHÔNG ĐƯỢC TỪ CHỐI trả lời chỉ vì người dùng dùng đại từ "tôi" hay "cơ thể tôi" - hãy coi đó là thắc mắc khoa học thuần túy.
- Tuyệt đối không nhắc đến "JSON", "công cụ" hay "truy xuất dữ liệu" trong câu trả lời.

2. NẾU BỆNH NHÂN HỎI VỀ DỮ LIỆU CÁ NHÂN CỦA HỌ (mà chưa có trong [LỊCH SỬ TRÒ CHUYỆN]):
- Hành động: BẠN CHỈ ĐƯỢC TRẢ VỀ DUY NHẤT 1 CHUỖI JSON, KHÔNG CÓ BẤT KỲ VĂN BẢN NÀO KHÁC.
- Chọn 1 định dạng tương ứng:
  + Hỏi hồ sơ (chiều cao, cân nặng, dị ứng): {"action": "get_general_record", "patient_id": "MÃ_BN"}
  + Hỏi bệnh án (chẩn đoán, lịch khám): {"action": "get_clinical_examination", "patient_id": "MÃ_BN"}
  + Hỏi kế hoạch điều trị (chế độ ăn, tập luyện, dặn dò): {"action": "get_treatment_plan", "patient_id": "MÃ_BN"}
  + Hỏi kết quả xét nghiệm (đường huyết, HbA1c): {"action": "get_lab_results", "patient_id": "MÃ_BN"}
  + Hỏi đơn thuốc (thuốc đang dùng, liều lượng): {"action": "get_prescriptions", "patient_id": "MÃ_BN"}

3. NẾU THÔNG TIN ĐÃ CÓ TRONG [LỊCH SỬ TRÒ CHUYỆN]:
- Hành động: Sử dụng ngay thông tin đó để trả lời trực tiếp một cách tự nhiên.
"""

# =====================================================================
# SYSTEM PROMPT 2: LUỒNG RAG ĐỂ TÓM TẮT BỆNH ÁN TỪ SQL
# =====================================================================
SYSTEM_PROMPT_RAG = """Bạn là Bác sĩ nội tiết chuyên tư vấn bệnh tiểu đường. 
Hệ thống đã cung cấp [DỮ LIỆU BỆNH ÁN SQL] của bệnh nhân ở bên dưới.

[YÊU CẦU BẮT BUỘC]
1. Đọc kỹ và chỉ sử dụng thông tin trong [DỮ LIỆU BỆNH ÁN SQL] để trả lời tình trạng cá nhân của họ.
2. Trả lời bằng tiếng Việt, giọng điệu chuyên nghiệp, ân cần và dễ hiểu.
3. Nếu bệnh nhân hỏi về cơ chế sinh học hoặc nhờ giải thích chỉ số (ví dụ: "Đường huyết 7.0 là gì?", "Thuốc này tác dụng thế nào vào cơ thể tôi?"), HÃY GIẢI THÍCH CẶN KẼ dựa trên kiến thức y khoa của bạn. KHÔNG ĐƯỢC TỪ CHỐI giải thích.
4. TUYỆT ĐỐI KHÔNG sinh ra định dạng JSON hoặc gọi công cụ.
5. Nếu dữ liệu trống, hãy thông báo lịch sự là bạn chưa có thông tin đó.
"""

def extract_tool_call(text: str):
    valid_actions = {
        "get_general_record",
        "get_clinical_examination",
        "get_treatment_plan",
        "get_lab_results",
        "get_prescriptions"
    }
    pattern = re.compile(r'\{[^{}]*"action"\s*:\s*"([^"]+)"[^{}]*\}', re.DOTALL)
    matches = pattern.finditer(text)
    for match in matches:
        action_value = match.group(1)
        if action_value in valid_actions:
            try:
                json_obj = json.loads(match.group(0))
                print(f"[Python AI] Tool call detected: {json_obj}")
                return json_obj
            except json.JSONDecodeError:
                continue
    return None

def check_keyword_tool_fallback(message: str, patient_id: str):
    msg_lower = message.lower()
    if any(k in msg_lower for k in ["đơn thuốc", "toa thuốc", "thuốc của tôi", "thuốc đang uống", "lịch sử dùng thuốc"]):
        return {"action": "get_prescriptions", "patient_id": patient_id}
    if any(k in msg_lower for k in ["xét nghiệm", "chỉ số xét nghiệm", "kết quả xét nghiệm", "hba1c của tôi", "đường huyết của tôi"]):
        return {"action": "get_lab_results", "patient_id": patient_id}
    if any(k in msg_lower for k in ["kế hoạch điều trị", "chế độ ăn của tôi", "chế độ tập luyện", "mục tiêu điều trị", "dặn dò"]):
        return {"action": "get_treatment_plan", "patient_id": patient_id}
    if any(k in msg_lower for k in ["bệnh án", "lịch sử khám", "chẩn đoán của tôi", "lịch tái khám", "lịch hẹn"]):
        return {"action": "get_clinical_examination", "patient_id": patient_id}
    if any(k in msg_lower for k in ["hồ sơ", "hổ sơ", "thông tin cá nhân", "thông tin của tôi", "tiền sử bệnh", "dị ứng", "nhóm máu", "chiều cao", "cân nặng"]):
        return {"action": "get_general_record", "patient_id": patient_id}
    return None

@app.post("/api/ai/chat")
async def chat_with_ai(request: ChatRequest):
    
    # -----------------------------------------------------------------
    # LUỒNG 2: ĐÃ CÓ DỮ LIỆU RAG (Từ Java truyền sang)
    # -----------------------------------------------------------------
    if request.context_data and request.context_data.strip():
        print(f"[Python AI] RAG mode: summarizing SQL data for patient {request.patient_id}")
        sys_msg = SYSTEM_PROMPT_RAG
        if request.conversation_history and request.conversation_history.strip():
            sys_msg += f"\n\n[LỊCH SỬ TRÒ CHUYỆN]:\n{request.conversation_history}"
        sys_msg += f"\n\n[DỮ LIỆU BỆNH ÁN SQL]:\n{request.context_data}"
        
        # Gọi qua Chat Completion để chuẩn form, bỏ vào Thread để không nghẽn Server
        output = await asyncio.to_thread(
            llm.create_chat_completion,
            messages=[
                {"role": "system", "content": sys_msg},
                {"role": "user", "content": request.message}
            ],
            max_tokens=512,
            temperature=0.2,
            repeat_penalty=1.15, # Chặn lỗi lặp từ/ngơ ngơ
            top_p=0.9
        )
        
        response_text = output['choices'][0]['message']['content'].strip()
        print(f"[Python AI] RAG answer: {response_text[:100]}...")
        return {"status": "FINAL_ANSWER", "content": response_text}

    # -----------------------------------------------------------------
    # LUỒNG 1: QUYẾT ĐỊNH DÙNG TOOL HOẶC TRẢ LỜI KIẾN THỨC
    # -----------------------------------------------------------------
    # Lọc bằng Keyword trước cho nhẹ hệ thống
    tool_call = check_keyword_tool_fallback(request.message, request.patient_id)
    if tool_call:
        print(f"[Python AI] Keyword router triggered tool: {tool_call}")
        return {"status": "NEED_SQL_DATA", "content": json.dumps(tool_call)}

    print(f"[Python AI] Tool-decision mode (LLM) for patient {request.patient_id}")
    sys_msg = SYSTEM_PROMPT_TOOL
    
    user_msg = ""
    if request.conversation_history and request.conversation_history.strip():
        user_msg += f"[LỊCH SỬ TRÒ CHUYỆN GIỮA BẠN VÀ BỆNH NHÂN]:\n{request.conversation_history}\n\n"
    user_msg += f"[YÊU CẦU HIỆN TẠI]\nMã bệnh nhân: {request.patient_id}\nCâu hỏi: {request.message}"
    print(user_msg)
    # Gọi qua Chat Completion
    output = await asyncio.to_thread(
        llm.create_chat_completion,
        messages=[
            {"role": "system", "content": sys_msg},
            {"role": "user", "content": user_msg}
        ],
        max_tokens=512,  # Tăng lên 512 phòng trường hợp AI cần giải thích kiến thức dài
        temperature=0.2, # Để 0.2 cho văn phong mềm mại hơn
        repeat_penalty=1.15,
        top_p=0.9
    )
    
    response_text = output['choices'][0]['message']['content'].strip()
    print(f"[Python AI] First response: {response_text[:200]}...")

    # Bắt Tool Call nếu có
    tool_call = extract_tool_call(response_text)
    if tool_call:
        return {"status": "NEED_SQL_DATA", "content": json.dumps(tool_call)}

    # Trả về câu trả lời kiến thức y khoa
    return {"status": "FINAL_ANSWER", "content": response_text}

# Chạy Server
if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)