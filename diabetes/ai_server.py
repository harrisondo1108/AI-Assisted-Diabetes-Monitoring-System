from fastapi import FastAPI
from pydantic import BaseModel
from llama_cpp import Llama
import json
import re
import uvicorn

app = FastAPI()

# 1. Trỏ đường dẫn tới file GGUF trên ổ cứng của bạn
model_path = "d:\Downloads\diabetes-consultant-q4_k_m_2.gguf"

print("Đang khởi động AI...")
# NẾU CÓ CARD NVIDIA: Thêm n_gpu_layers=-1 vào hàm dưới. NẾU CHỈ DÙNG CPU: Xóa tham số đó đi.
llm = Llama(model_path=model_path, n_ctx=4096, verbose=False)
print("Khởi động hoàn tất!")

class ChatRequest(BaseModel):
    patient_id: str
    message: str
    context_data: str = ""
    conversation_history: str = ""

# System prompt dùng cho lần 1: AI tự quyết định gọi tool hay trả lời thẳng (Fine-tuning path)
SYSTEM_PROMPT_TOOL = """Bạn là trợ lí AI thông minh. Bạn có kiến thức chuyên sâu về bệnh tiểu đường và có thể truy suất dữ liệu y tế.

[NHIỆM VỤ CỦA BẠN]
Tùy vào câu hỏi của bệnh nhân, hãy xử lý theo 1 trong 3 trường hợp sau:
1. NẾU LÀ CÂU HỎI KIẾN THỨC CHUNG HOẶC CHÀO HỎI (ví dụ: tiểu đường là gì, triệu chứng, chế độ ăn chung...): 
-> HÃY TRẢ LỜI TRỰC TIẾP bằng kiến thức y khoa của bạn. Trả lời thân thiện, tư cách là một Bác sĩ. TUYỆT ĐỐI KHÔNG nhắc đến các từ "công cụ", "truy xuất dữ liệu", hay "JSON" trong câu trả lời.
2. NẾU THÔNG TIN ĐÃ CÓ TRONG [LỊCH SỬ TRÒ CHUYỆN]:
-> Hãy dùng thông tin đó để trả lời trực tiếp bằng lời văn bình thường.
3. NẾU BỆNH NHÂN HỎI VỀ DỮ LIỆU CÁ NHÂN CỦA HỌ (mà chưa có trong lịch sử):
-> BẠN KHÔNG ĐƯỢC TRẢ LỜI BẰNG LỜI VĂN. BẠN CHỈ ĐƯỢC TRẢ VỀ DUY NHẤT 1 CHUỖI JSON ĐÚNG ĐỊNH DẠNG TỪ DANH SÁCH DƯỚI ĐÂY:
- {"action": "get_general_record", "patient_id": "MÃ_BN"} (Hỏi hồ sơ, chiều cao, cân nặng, dị ứng)
- {"action": "get_clinical_examination", "patient_id": "MÃ_BN"} (Hỏi bệnh án, chẩn đoán, lịch khám)
- {"action": "get_treatment_plan", "patient_id": "MÃ_BN"} (Hỏi kế hoạch điều trị, dặn dò)
- {"action": "get_lab_results", "patient_id": "MÃ_BN"} (Hỏi kết quả xét nghiệm, HbA1c)
- {"action": "get_prescriptions", "patient_id": "MÃ_BN"} (Hỏi đơn thuốc, liều lượng)
"""

# System prompt dùng cho lần 2: AI chỉ việc đọc dữ liệu SQL và tóm tắt (RAG path)
SYSTEM_PROMPT_RAG = """Bạn là trợ lý y khoa nội tiết thông minh, chuyên tư vấn cho bệnh nhân tiểu đường.
Hệ thống đã cung cấp cho bạn dữ liệu từ cơ sở dữ liệu của bệnh nhân ở phần [DỮ LIỆU SQL] bên dưới.

[YÊU CẦU BẮT BUỘC]
- Hãy đọc kỹ dữ liệu trong [DỮ LIỆU SQL] và trả lời câu hỏi của bệnh nhân.
- Trả lời bằng tiếng Việt, thân thiện, dễ hiểu, đóng vai trò một Bác sĩ tư vấn.
- TUYỆT ĐỐI KHÔNG trả về định dạng JSON.
- TUYỆT ĐỐI KHÔNG gọi thêm bất kỳ công cụ nào nữa.
- Nếu dữ liệu trống hoặc không có thông tin, hãy thông báo lịch sự cho bệnh nhân.
- Không được đưa ra chẩn đoán nếu có dấu hiệu nghi ngờ bệnh lý, hãy khuyến nghị bệnh nhân đi khám trực tiếp.
"""

def format_prompt(sys_msg, user_msg):
    return f"<|start_header_id|>system<|end_header_id|>\n\n{sys_msg}<|eot_id|><|start_header_id|>user<|end_header_id|>\n\n{user_msg}<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n"

def extract_tool_call(text: str):
    """
    Dùng regex để trích xuất JSON tool call một cách chính xác.
    Chỉ khớp khi JSON có field 'action' là một trong các tool đã định nghĩa.
    Tránh false-positive khi AI giải thích về JSON trong câu trả lời.
    """
    valid_actions = {
        "get_general_record",
        "get_clinical_examination",
        "get_treatment_plan",
        "get_lab_results",
        "get_prescriptions"
    }
    # Tìm tất cả các khối JSON trong response
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
    
    # 5. Prescriptions (Đơn thuốc / Toa thuốc)
    if any(k in msg_lower for k in ["đơn thuốc", "toa thuốc", "thuốc của tôi", "thuốc đang uống", "lịch sử dùng thuốc"]):
        return {"action": "get_prescriptions", "patient_id": patient_id}
        
    # 4. Lab results (Xét nghiệm)
    if any(k in msg_lower for k in ["xét nghiệm", "chỉ số xét nghiệm", "kết quả xét nghiệm", "hba1c của tôi", "đường huyết của tôi"]):
        return {"action": "get_lab_results", "patient_id": patient_id}
        
    # 3. Treatment plan (Kế hoạch điều trị)
    if any(k in msg_lower for k in ["kế hoạch điều trị", "chế độ ăn của tôi", "chế độ tập luyện", "mục tiêu điều trị", "dặn dò"]):
        return {"action": "get_treatment_plan", "patient_id": patient_id}
        
    # 2. Clinical examination (Bệnh án / Lịch khám)
    if any(k in msg_lower for k in ["bệnh án", "lịch sử khám", "chẩn đoán của tôi", "lịch tái khám", "lịch hẹn"]):
        return {"action": "get_clinical_examination", "patient_id": patient_id}
        
    # 1. General record (Hồ sơ)
    if any(k in msg_lower for k in ["hồ sơ", "hổ sơ", "thông tin cá nhân", "thông tin của tôi", "tiền sử bệnh", "dị ứng", "nhóm máu", "chiều cao", "cân nặng"]):
        return {"action": "get_general_record", "patient_id": patient_id}
        
    return None

@app.post("/api/ai/chat")
async def chat_with_ai(request: ChatRequest):
    # --- LẦN 2: Đã có dữ liệu SQL từ Java → Dùng SYSTEM_PROMPT_RAG để tóm tắt ---
    if request.context_data and request.context_data.strip():
        print(f"[Python AI] RAG mode: summarizing SQL data for patient {request.patient_id}")
        sys_msg = SYSTEM_PROMPT_RAG
        if request.conversation_history and request.conversation_history.strip():
            sys_msg += f"\n\n[LỊCH SỬ TRÒ CHUYỆN]:\n{request.conversation_history}"
        sys_msg += f"\n\n[DỮ LIỆU SQL]:\n{request.context_data}"
        
        prompt = format_prompt(sys_msg, request.message)
        output = llm(prompt, max_tokens=512, temperature=0.2, stop=["<|eot_id|>"])
        response_text = output['choices'][0]['text'].strip()
        print(f"[Python AI] RAG answer: {response_text[:100]}...")
        return {"status": "FINAL_ANSWER", "content": response_text}

    # --- LẦN 1: Không có dữ liệu SQL → AI tự quyết định hoặc dựa vào keyword router ---
    # Phân loại intent bằng từ khóa trước để tối ưu hóa hiệu năng và độ chính xác (không cần chạy CPU LLM)
    tool_call = check_keyword_tool_fallback(request.message, request.patient_id)
    if tool_call:
        print(f"[Python AI] Keyword router triggered tool: {tool_call}")
        return {"status": "NEED_SQL_DATA", "content": json.dumps(tool_call)}

    # Fallback sang LLM nếu không khớp từ khóa đặc trưng nào
    print(f"[Python AI] Tool-decision mode (LLM) for patient {request.patient_id}")
    sys_msg = SYSTEM_PROMPT_TOOL
    
    user_msg = ""
    if request.conversation_history and request.conversation_history.strip():
        user_msg += f"[LỊCH SỬ TRÒ CHUYỆN GIỮA BẠN VÀ BỆNH NHÂN]:\n{request.conversation_history}\n\n"
    
    user_msg += f"[YÊU CẦU HIỆN TẠI]\nMã bệnh nhân: {request.patient_id}\nCâu hỏi: {request.message}"
    
    prompt = format_prompt(sys_msg, user_msg)
    print(f"[Python AI] Prompt sent to LLM:\n{prompt}...")
    output = llm(prompt, max_tokens=256, temperature=0.1, stop=["<|eot_id|>"])
    response_text = output['choices'][0]['text'].strip()
    print(f"[Python AI] First response: {response_text[:200]}")

    # Kiểm tra xem AI có trả về tool call không (dùng regex để tránh false-positive)
    tool_call = extract_tool_call(response_text)
    if tool_call:
        return {"status": "NEED_SQL_DATA", "content": json.dumps(tool_call)}

    # Không có tool call → AI trả lời bằng Fine-tuning kiến thức y khoa
    return {"status": "FINAL_ANSWER", "content": response_text}

# Chạy Server ở Port 8000 của Localhost
if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)
