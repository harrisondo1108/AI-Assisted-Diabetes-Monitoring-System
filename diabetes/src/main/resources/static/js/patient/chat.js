// ============================================================
// STATE
// ============================================================
let state = {
    messages: [],
    conversations: [],
    currentConversationId: null,
    isLoading: false,
};

// ============================================================
// DOM REFS
// ============================================================
const DOM = {
    messagesContainer: document.getElementById('messagesContainer'),
    messageInput: document.getElementById('messageInput'),
    sendBtn: document.getElementById('sendBtn'),
    welcomeMessage: document.getElementById('welcomeMessage'),
    loadingIndicator: document.getElementById('loadingIndicator'),
    historyItems: document.getElementById('historyItems'),
    emptyHistory: document.getElementById('emptyHistory'),
    statusText: document.getElementById('statusText'),
    newChatBtn: document.getElementById('newChatBtn'),
};

// ============================================================
// API FUNCTIONS
// ============================================================
const api = {
    sendMessage: async (request) => {
        const response = await fetch(`${API_BASE_URL}/send`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to send message');
        }
        return response.json();
    },
    sendMessageStream: async (request, onToken, onDone, onError) => {
        try {
            const response = await fetch(`${API_BASE_URL}/stream`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(request)
            });
            if (!response.ok) {
                throw new Error('Failed to start stream');
            }
            const reader = response.body.getReader();
            const decoder = new TextDecoder('utf-8');
            let buffer = '';

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;
                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop();

                let currentEvent = null;
                for (let i = 0; i < lines.length; i++) {
                    const line = lines[i].trim();
                    if (line.startsWith('event:')) {
                        currentEvent = line.substring(6).trim();
                    } else if (line.startsWith('data:')) {
                        const rawData = line.substring(5).trim();
                        if (currentEvent === 'token') {
                            try {
                                const obj = JSON.parse(rawData);
                                if (obj && obj.t !== undefined) {
                                    onToken(obj.t);
                                } else {
                                    onToken(rawData);
                                }
                            } catch (e) {
                                onToken(rawData);
                            }
                        } else if (currentEvent === 'done') {
                            try {
                                const doneObj = JSON.parse(rawData);
                                onDone(doneObj);
                            } catch (e) {
                                onDone({ success: true, message: rawData });
                            }
                        }
                        currentEvent = null;
                    }
                }
            }
        } catch (error) {
            if (onError) onError(error);
        }
    },
    getConversationHistory: async (conversationId) => {
        const response = await fetch(`${API_BASE_URL}/history/${conversationId}`);
        if (!response.ok) throw new Error('Failed to load conversation history');
        return response.json();
    },
    getPatientConversations: async (patientId) => {
        const response = await fetch(`${API_BASE_URL}/patient/${patientId}/conversations`);
        if (!response.ok) throw new Error('Failed to load conversations');
        return response.json();
    },
    deleteConversation: async (conversationId) => {
        const response = await fetch(`${API_BASE_URL}/conversation/${conversationId}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete conversation');
    },
};

// ============================================================
// HELPER FUNCTIONS
// ============================================================
function formatTime(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffMins = Math.floor((now - date) / 60000);
    if (diffMins < 1) return 'Vừa xong';
    if (diffMins < 60) return `${diffMins} phút trước`;
    if (diffMins < 1440) return `${Math.floor(diffMins / 60)} giờ trước`;
    return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

function formatTimeShort(dateString) {
    return new Date(dateString).toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function renderMarkdown(text) {
    let html = escapeHtml(text);
    html = html.replace(/```([\s\S]*?)```/g, (m, code) => `<pre><code>${escapeHtml(code)}</code></pre>`);
    html = html.replace(/`([^`]+)`/g, (m, code) => `<code>${escapeHtml(code)}</code>`);
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
    html = html.replace(/\n/g, '<br>');
    return html;
}

function scrollToBottom() {
    const container = document.getElementById('messageList');
    container.scrollTop = container.scrollHeight;
}

function updateStatus(text) {
    if (DOM.statusText) DOM.statusText.textContent = text;
}

function showError(message) {
    const div = document.createElement('div');
    div.className = 'error-message';
    div.innerHTML = `<i class="fas fa-exclamation-circle"></i> ${escapeHtml(message)}`;
    DOM.messagesContainer.appendChild(div);
    scrollToBottom();
    setTimeout(() => div.remove(), 5000);
}

// ============================================================
// RENDER FUNCTIONS
// ============================================================
function renderMessage(message) {
    const isUser = message.sender === 'User' || message.sender === 'Patient';

    const wrapper = document.createElement('div');
    wrapper.className = `message-item ${isUser ? 'user' : 'ai'}`;

    // Avatar
    const avatar = document.createElement('div');
    avatar.className = `message-avatar ${isUser ? 'user' : 'ai'}`;
    avatar.innerHTML = isUser ? '<i class="fas fa-user"></i>' : '<i class="fas fa-robot"></i>';

    // Content wrapper
    const contentWrapper = document.createElement('div');
    contentWrapper.className = 'message-content-wrapper';

    // Bubble
    const bubble = document.createElement('div');
    bubble.className = 'message-bubble';

    const content = document.createElement('div');
    if (isUser) {
        content.innerHTML = escapeHtml(message.content);
    } else {
        content.innerHTML = renderMarkdown(message.content);
    }
    bubble.appendChild(content);

    // Time
    const timeWrapper = document.createElement('div');
    timeWrapper.className = 'message-time-wrapper';
    const timeSpan = document.createElement('span');
    timeSpan.className = 'time';
    timeSpan.textContent = formatTimeShort(message.time);
    timeWrapper.appendChild(timeSpan);

    contentWrapper.appendChild(bubble);
    contentWrapper.appendChild(timeWrapper);

    // User: avatar bên phải, content bên trái
    if (isUser) {
        wrapper.appendChild(contentWrapper);
        wrapper.appendChild(avatar);
    } else {
        // AI: avatar bên trái, content bên phải
        wrapper.appendChild(avatar);
        wrapper.appendChild(contentWrapper);
    }

    return wrapper;
}

function renderMessages(messages) {
    DOM.messagesContainer.innerHTML = '';
    if (messages.length === 0) {
        DOM.welcomeMessage.style.display = 'block';
        return;
    }
    DOM.welcomeMessage.style.display = 'none';
    messages.forEach(msg => DOM.messagesContainer.appendChild(renderMessage(msg)));
    scrollToBottom();
}

function typeAiMessage(message) {
    return new Promise((resolve) => {
        const wrapper = document.createElement('div');
        wrapper.className = 'message-item ai';

        const avatar = document.createElement('div');
        avatar.className = 'message-avatar ai';
        avatar.innerHTML = '<i class="fas fa-robot"></i>';

        const contentWrapper = document.createElement('div');
        contentWrapper.className = 'message-content-wrapper';

        const bubble = document.createElement('div');
        bubble.className = 'message-bubble';

        const content = document.createElement('div');
        bubble.appendChild(content);

        const timeWrapper = document.createElement('div');
        timeWrapper.className = 'message-time-wrapper';
        const timeSpan = document.createElement('span');
        timeSpan.className = 'time';
        timeSpan.textContent = formatTimeShort(message.time);
        timeWrapper.appendChild(timeSpan);

        contentWrapper.appendChild(bubble);
        contentWrapper.appendChild(timeWrapper);

        wrapper.appendChild(avatar);
        wrapper.appendChild(contentWrapper);

        DOM.messagesContainer.appendChild(wrapper);
        scrollToBottom();

        const fullText = message.content || '';
        let currentIndex = 0;
        const chunkSize = 3;

        const timer = setInterval(() => {
            currentIndex += chunkSize;
            if (currentIndex >= fullText.length) {
                currentIndex = fullText.length;
                content.innerHTML = renderMarkdown(fullText);
                clearInterval(timer);
                scrollToBottom();
                resolve();
            } else {
                const currentText = fullText.substring(0, currentIndex);
                content.innerHTML = renderMarkdown(currentText) + '<span class="typing-cursor"></span>';
                scrollToBottom();
            }
        }, 12);
    });
}

function renderConversations(conversations) {
    const container = DOM.historyItems;
    container.innerHTML = '';

    if (conversations.length === 0) {
        const empty = document.createElement('div');
        empty.className = 'empty-message';
        empty.innerHTML = '<i class="fas fa-comment-slash"></i> Không tìm thấy lịch sử trò chuyện nào.';
        container.appendChild(empty);
        return;
    }

    conversations.forEach(conv => {
        const item = document.createElement('div');
        item.className = `history-item ${state.currentConversationId === conv.conversationId ? 'active' : ''}`;
        item.style.cursor = 'pointer';

        const label = document.createElement('div');
        label.className = 'item-label';
        label.innerHTML = `
                    <i class="fas fa-message"></i>
                    <span>${conv.messages.length > 0 ? conv.messages[0].content.substring(0, 30) + (conv.messages[0].content.length > 30 ? '...' : '') : 'Cuộc trò chuyện'}</span>
                `;

        const value = document.createElement('div');
        value.className = 'item-value';
        value.textContent = conv.messages.length > 0 ? formatTime(conv.messages[conv.messages.length - 1].time) : 'Mới';

        const del = document.createElement('button');
        del.className = 'delete-btn';
        del.innerHTML = '<i class="fas fa-times"></i>';
        del.addEventListener('click', (e) => {
            e.stopPropagation();
            showConfirmModal(conv.conversationId);
        });

        item.appendChild(label);
        item.appendChild(value);
        item.appendChild(del);

        item.addEventListener('click', () => {
            loadConversation(conv.conversationId);
        });

        container.appendChild(item);
    });
}

// ============================================================
// MAIN FUNCTIONS
// ============================================================
async function loadConversation(conversationId) {
    try {
        state.currentConversationId = conversationId;
        const history = await api.getConversationHistory(conversationId);
        state.messages = history.messages;
        renderMessages(state.messages);
        renderConversations(state.conversations);
    } catch (error) {
        console.error('Failed to load conversation:', error);
        showError('Không thể tải cuộc trò chuyện');
    }
}

async function loadConversations() {
    try {
        const conversations = await api.getPatientConversations(PATIENT_ID);
        state.conversations = conversations;
        renderConversations(conversations);
    } catch (error) {
        console.error('Failed to load conversations:', error);
    }
}

async function sendMessage(question) {
    if (!question.trim() || state.isLoading) return;

    const userMsg = {
        sender: 'User',
        content: question.trim(),
        time: new Date().toISOString()
    };
    state.messages.push(userMsg);
    renderMessages(state.messages);
    DOM.messageInput.value = '';
    DOM.welcomeMessage.style.display = 'none';

    state.isLoading = true;
    DOM.sendBtn.disabled = true;
    DOM.loadingIndicator.style.display = 'flex';
    updateStatus('Đang đọc dữ liệu và suy nghĩ...');

    const request = {
        question: question.trim(),
        patientId: PATIENT_ID,
        conversationId: state.currentConversationId || undefined,
    };

    const aiMsg = {
        sender: 'AI',
        content: '',
        time: new Date().toISOString()
    };
    state.messages.push(aiMsg);

    let isFirstChunk = true;
    let contentElement = null;

    try {
        await api.sendMessageStream(request,
            (token) => {
                if (isFirstChunk) {
                    isFirstChunk = false;
                    DOM.loadingIndicator.style.display = 'none';
                    updateStatus('Đang trả lời...');

                    const wrapper = document.createElement('div');
                    wrapper.className = 'message-item ai';

                    const avatar = document.createElement('div');
                    avatar.className = 'message-avatar ai';
                    avatar.innerHTML = '<i class="fas fa-robot"></i>';

                    const contentWrapper = document.createElement('div');
                    contentWrapper.className = 'message-content-wrapper';

                    const bubble = document.createElement('div');
                    bubble.className = 'message-bubble';

                    contentElement = document.createElement('div');
                    bubble.appendChild(contentElement);

                    const timeWrapper = document.createElement('div');
                    timeWrapper.className = 'message-time-wrapper';
                    const timeSpan = document.createElement('span');
                    timeSpan.className = 'time';
                    timeSpan.textContent = formatTimeShort(aiMsg.time);
                    timeWrapper.appendChild(timeSpan);

                    contentWrapper.appendChild(bubble);
                    contentWrapper.appendChild(timeWrapper);

                    wrapper.appendChild(avatar);
                    wrapper.appendChild(contentWrapper);

                    DOM.messagesContainer.appendChild(wrapper);
                }

                aiMsg.content += token;
                if (contentElement) {
                    contentElement.innerHTML = renderMarkdown(aiMsg.content) + '<span class="typing-cursor"></span>';
                    scrollToBottom();
                }
            },
            async (doneResponse) => {
                if (isFirstChunk) {
                    isFirstChunk = false;
                    DOM.loadingIndicator.style.display = 'none';
                }
                if (doneResponse && doneResponse.conversationId) {
                    state.currentConversationId = doneResponse.conversationId;
                }
                if (doneResponse && doneResponse.message) {
                    aiMsg.content = doneResponse.message;
                }
                if (contentElement) {
                    contentElement.innerHTML = renderMarkdown(aiMsg.content);
                } else {
                    renderMessages(state.messages);
                }
                scrollToBottom();
                await loadConversations();

                state.isLoading = false;
                DOM.sendBtn.disabled = false;
                DOM.loadingIndicator.style.display = 'none';
                updateStatus('Trực tuyến');
                DOM.messageInput.focus();
            },
            (error) => {
                console.error('SSE Error:', error);
                if (isFirstChunk) {
                    state.messages.pop();
                    state.messages.pop();
                    renderMessages(state.messages);
                    showError(error.message || 'Có lỗi xảy ra khi kết nối tới AI');
                } else if (contentElement) {
                    contentElement.innerHTML = renderMarkdown(aiMsg.content + '\n\n⚠️ *(Kết nối bị gián đoạn)*');
                }
                state.isLoading = false;
                DOM.sendBtn.disabled = false;
                DOM.loadingIndicator.style.display = 'none';
                updateStatus('Trực tuyến');
                DOM.messageInput.focus();
            }
        );
    } catch (error) {
        state.messages.pop();
        state.messages.pop();
        renderMessages(state.messages);
        showError(error.message || 'Có lỗi xảy ra khi gửi tin nhắn');
        state.isLoading = false;
        DOM.sendBtn.disabled = false;
        DOM.loadingIndicator.style.display = 'none';
        updateStatus('Trực tuyến');
        DOM.messageInput.focus();
    }
}

let conversationToDelete = null;

function showConfirmModal(conversationId) {
    conversationToDelete = conversationId;
    document.getElementById('deleteConfirmModal').classList.add('active');
}

function hideConfirmModal() {
    conversationToDelete = null;
    document.getElementById('deleteConfirmModal').classList.remove('active');
}

document.getElementById('btnCancelDelete').addEventListener('click', hideConfirmModal);
document.getElementById('btnConfirmDelete').addEventListener('click', () => {
    if (conversationToDelete) {
        deleteConversation(conversationToDelete);
        hideConfirmModal();
    }
});

async function deleteConversation(conversationId) {
    try {
        await api.deleteConversation(conversationId);
        if (state.currentConversationId === conversationId) {
            state.currentConversationId = null;
            state.messages = [];
            renderMessages(state.messages);
            DOM.welcomeMessage.style.display = 'block';
        }
        await loadConversations();
    } catch (error) {
        console.error('Failed to delete conversation:', error);
        showError('Không thể xóa cuộc trò chuyện');
    }
}

function newChat() {
    state.currentConversationId = null;
    state.messages = [];
    renderMessages(state.messages);
    DOM.welcomeMessage.style.display = 'block';
    DOM.messageInput.value = '';
    DOM.messageInput.focus();
    document.querySelectorAll('.history-item').forEach(el => el.classList.remove('active'));
}

// ============================================================
// EVENT LISTENERS
// ============================================================
DOM.sendBtn.addEventListener('click', () => sendMessage(DOM.messageInput.value));
DOM.newChatBtn.addEventListener('click', newChat);

DOM.messageInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage(DOM.messageInput.value);
    }
});

document.querySelectorAll('.suggestion-chip').forEach(chip => {
    chip.addEventListener('click', () => {
        const question = chip.dataset.question;
        if (question) {
            DOM.messageInput.value = question;
            sendMessage(question);
        }
    });
});

// ============================================================
// INIT
// ============================================================
async function init() {
    await loadConversations();
    if (state.conversations.length > 0) {
        loadConversation(state.conversations[0].conversationId);
    }
    DOM.messageInput.focus();
    console.log('AI Chat initialized!');
}

init();
