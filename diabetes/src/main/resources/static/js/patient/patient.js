document.addEventListener('DOMContentLoaded', function () {
    setupChat();
    setupProfileValidation();
    setupRoutineValidation();
    setupChangePasswordValidation();
    setupPasswordToggle();
});

function setupChat() {
    const chatButton = document.querySelector('.chat-input button');
    const chatInput = document.querySelector('.chat-input input');
    const messageList = document.querySelector('.message-list');

    if (!chatButton || !chatInput || !messageList) {
        return;
    }

    // Auto scroll to bottom
    messageList.scrollTop = messageList.scrollHeight;

    function sendMessage() {
        const value = chatInput.value.trim();

        if (!value) {
            return;
        }

        // 1. Append user message
        const userMessage = document.createElement('div');
        userMessage.className = 'message patient';
        userMessage.textContent = value;
        messageList.appendChild(userMessage);
        chatInput.value = '';
        messageList.scrollTop = messageList.scrollHeight;

        // 2. Append temporary typing indicator
        const typingMessage = document.createElement('div');
        typingMessage.className = 'message ai typing-indicator';
        typingMessage.textContent = 'Đang trả lời...';
        messageList.appendChild(typingMessage);
        messageList.scrollTop = messageList.scrollHeight;

        // 3. Send via AJAX
        fetch('/patient/chat/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                'message': value
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Lỗi kết nối mạng');
            }
            return response.json();
        })
        .then(data => {
            // Remove typing indicator
            typingMessage.remove();

            if (data.success) {
                const aiMessage = document.createElement('div');
                aiMessage.className = 'message ai';
                aiMessage.textContent = data.reply;
                messageList.appendChild(aiMessage);
            } else {
                const errorMessage = document.createElement('div');
                errorMessage.className = 'message ai';
                errorMessage.textContent = 'Xin lỗi, tôi đã gặp lỗi. Vui lòng thử lại.';
                messageList.appendChild(errorMessage);
            }
            messageList.scrollTop = messageList.scrollHeight;
        })
        .catch(err => {
            typingMessage.remove();
            const errorMessage = document.createElement('div');
            errorMessage.className = 'message ai';
            errorMessage.textContent = 'Lỗi kết nối. Vui lòng thử lại sau.';
            messageList.appendChild(errorMessage);
            messageList.scrollTop = messageList.scrollHeight;
        });
    }

    chatButton.addEventListener('click', sendMessage);
    chatInput.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendMessage();
        }
    });
}

function setupProfileValidation() {
    const form = document.getElementById('profileForm');

    if (!form) {
        return;
    }

    form.addEventListener('submit', function (event) {
        clearValidation(form, 'profileValidationMessage');

        const fullName = getValue(form, 'fullName');
        const phoneNumber = getValue(form, 'phoneNumber');
        const address = getValue(form, 'address');
        const dob = getValue(form, 'dob');
        const height = getValue(form, 'height');
        const weight = getValue(form, 'weight');
        const bloodgroup = getValue(form, 'bloodgroup');
        const permanentMedicalHistory = getValue(form, 'permanentMedicalHistory');
        const allergyNotes = getValue(form, 'allergyNotes');
        const supervisorName = getValue(form, 'supervisorName');
        const supervisorPhone = getValue(form, 'supervisorPhone');
        const email = getValue(form, 'email');

        const errors = [];

        if (!fullName) {
            errors.push('Họ và tên là bắt buộc.');
            markInvalid(form, 'fullName');
        } else {
            if (fullName.length < 2 || fullName.length > 60) {
                errors.push('Họ và tên phải từ 2 đến 60 ký tự.');
                markInvalid(form, 'fullName');
            }

            if (!isValidName(fullName)) {
                errors.push('Họ và tên chỉ được chứa chữ cái và khoảng trắng.');
                markInvalid(form, 'fullName');
            }
        }

        if (!phoneNumber) {
            errors.push('Số điện thoại là bắt buộc.');
            markInvalid(form, 'phoneNumber');
        } else if (!isValidPhone(phoneNumber)) {
            errors.push('Số điện thoại phải gồm 10 chữ số.');
            markInvalid(form, 'phoneNumber');
        }

        if (email) {
            if (email.length > 100) {
                errors.push('Email không được vượt quá 100 ký tự.');
                markInvalid(form, 'email');
            } else if (!isValidEmail(email)) {
                errors.push('Địa chỉ email không hợp lệ.');
                markInvalid(form, 'email');
            }
        }

        if (address && address.length > 200) {
            errors.push('Địa chỉ không được vượt quá 200 ký tự.');
            markInvalid(form, 'address');
        }

        if (dob) {
            const dobDate = new Date(dob);
            const today = new Date();
            const minDate = new Date();
            minDate.setFullYear(today.getFullYear() - 120);

            today.setHours(0, 0, 0, 0);
            dobDate.setHours(0, 0, 0, 0);

            if (dobDate > today) {
                errors.push('Ngày sinh không thể ở tương lai.');
                markInvalid(form, 'dob');
            }

            if (dobDate < minDate) {
                errors.push('Ngày sinh không hợp lệ.');
                markInvalid(form, 'dob');
            }
        }

        if (height) {
            const heightNumber = Number(height);

            if (Number.isNaN(heightNumber) || heightNumber < 50 || heightNumber > 250) {
                errors.push('Chiều cao phải từ 50 đến 250 cm.');
                markInvalid(form, 'height');
            }
        }

        if (weight) {
            const weightNumber = Number(weight);

            if (Number.isNaN(weightNumber) || weightNumber < 1 || weightNumber > 300) {
                errors.push('Cân nặng phải từ 1 đến 300 kg.');
                markInvalid(form, 'weight');
            }
        }

        if (bloodgroup && !isValidBloodGroup(bloodgroup)) {
            errors.push('Nhóm máu không hợp lệ.');
            markInvalid(form, 'bloodgroup');
        }

        if (permanentMedicalHistory && permanentMedicalHistory.length > 500) {
            errors.push('Tiền sử bệnh án không được vượt quá 500 ký tự.');
            markInvalid(form, 'permanentMedicalHistory');
        }

        if (allergyNotes && allergyNotes.length > 500) {
            errors.push('Lưu ý dị ứng không được vượt quá 500 ký tự.');
            markInvalid(form, 'allergyNotes');
        }

        if (supervisorName) {
            if (supervisorName.length > 90) {
                errors.push('Tên người giám hộ không được vượt quá 90 ký tự.');
                markInvalid(form, 'supervisorName');
            }

            if (!isValidName(supervisorName)) {
                errors.push('Tên người giám hộ chỉ được chứa chữ cái và khoảng trắng.');
                markInvalid(form, 'supervisorName');
            }
        }

        if (supervisorPhone && !isValidPhone(supervisorPhone)) {
            errors.push('Số điện thoại người giám hộ phải gồm 10 chữ số.');
            markInvalid(form, 'supervisorPhone');
        }

        const imageFileInput = form.querySelector('input[name="imageFile"]');
        if (imageFileInput && imageFileInput.files && imageFileInput.files[0]) {
            const imageFile = imageFileInput.files[0];
            if (imageFile.size > 2 * 1024 * 1024) {
                errors.push('Ảnh đại diện không được vượt quá 2MB.');
                markInvalid(form, 'imageFile');
            }
            if (!imageFile.type.startsWith('image/')) {
                errors.push('Định dạng tệp không hợp lệ. Chỉ chấp nhận các tệp ảnh.');
                markInvalid(form, 'imageFile');
            }
        }

        if (errors.length > 0) {
            event.preventDefault();
            showValidationMessage('profileValidationMessage', errors);
            scrollToValidationMessage('profileValidationMessage');
        }
    });
}

function setupRoutineValidation() {
    const form = document.getElementById('routineForm');

    if (!form) {
        return;
    }

    form.addEventListener('submit', function (event) {
        clearValidation(form, 'routineValidationMessage');

        const wakeUpTime = getValue(form, 'wakeUpTime');
        const breakfastTime = getValue(form, 'breakfastTime');
        const lunchTime = getValue(form, 'lunchTime');
        const dinnerTime = getValue(form, 'dinnerTime');
        const sleepTime = getValue(form, 'sleepTime');

        const errors = [];

        if (!wakeUpTime) {
            errors.push('Thời gian thức dậy là bắt buộc.');
            markInvalid(form, 'wakeUpTime');
        }

        if (!breakfastTime) {
            errors.push('Thời gian ăn sáng là bắt buộc.');
            markInvalid(form, 'breakfastTime');
        }

        if (!lunchTime) {
            errors.push('Thời gian ăn trưa là bắt buộc.');
            markInvalid(form, 'lunchTime');
        }

        if (!dinnerTime) {
            errors.push('Thời gian ăn tối là bắt buộc.');
            markInvalid(form, 'dinnerTime');
        }

        if (!sleepTime) {
            errors.push('Thời gian đi ngủ là bắt buộc.');
            markInvalid(form, 'sleepTime');
        }

        if (errors.length === 0) {
            if (!isTimeBefore(wakeUpTime, breakfastTime)) {
                errors.push('Thời gian ăn sáng phải sau thời gian thức dậy.');
                markInvalid(form, 'wakeUpTime');
                markInvalid(form, 'breakfastTime');
            }

            if (!isTimeBefore(breakfastTime, lunchTime)) {
                errors.push('Thời gian ăn trưa phải sau thời gian ăn sáng.');
                markInvalid(form, 'breakfastTime');
                markInvalid(form, 'lunchTime');
            }

            if (!isTimeBefore(lunchTime, dinnerTime)) {
                errors.push('Thời gian ăn tối phải sau thời gian ăn trưa.');
                markInvalid(form, 'lunchTime');
                markInvalid(form, 'dinnerTime');
            }

            if (!isTimeBefore(wakeUpTime, sleepTime)) {
                errors.push('Thời gian đi ngủ phải sau thời gian thức dậy.');
                markInvalid(form, 'wakeUpTime');
                markInvalid(form, 'sleepTime');
            }
        }

        if (errors.length > 0) {
            event.preventDefault();
            showValidationMessage('routineValidationMessage', errors);
            scrollToValidationMessage('routineValidationMessage');
        }
    });
}

function getValue(form, fieldName) {
    const field = form.querySelector(`[name="${fieldName}"]`);

    if (!field) {
        return '';
    }

    return field.value.trim();
}

function isValidPhone(value) {
    return /^[0-9]{10}$/.test(value);
}

function isValidEmail(value) {
    return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value);
}

function isValidName(value) {
    return /^[A-Za-zÀ-ỹ\s]+$/.test(value);
}

function isValidBloodGroup(value) {
    return ['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-'].includes(value);
}

function isTimeBefore(first, second) {
    return convertTimeToMinutes(first) < convertTimeToMinutes(second);
}

function convertTimeToMinutes(value) {
    const parts = value.split(':');

    if (parts.length !== 2) {
        return -1;
    }

    const hour = Number(parts[0]);
    const minute = Number(parts[1]);

    return hour * 60 + minute;
}

function markInvalid(form, fieldName) {
    const field = form.querySelector(`[name="${fieldName}"]`);

    if (field) {
        field.classList.add('input-invalid');
    }
}

function clearValidation(form, messageId) {
    const message = document.getElementById(messageId);

    if (message) {
        message.innerHTML = '';
        message.classList.remove('show');
    }

    form.querySelectorAll('.input-invalid').forEach(input => {
        input.classList.remove('input-invalid');
    });
}

function showValidationMessage(messageId, errors) {
    const message = document.getElementById(messageId);

    if (!message) {
        alert(errors.join('\n'));
        return;
    }

    const listItems = errors.map(error => `<li>${escapeHtml(error)}</li>`).join('');

    message.innerHTML = `
        <strong>Vui lòng kiểm tra thông tin sau:</strong>
        <ul>${listItems}</ul>
    `;

    message.classList.add('show');
}

function scrollToValidationMessage(messageId) {
    const message = document.getElementById(messageId);

    if (message) {
        message.scrollIntoView({
            behavior: 'smooth',
            block: 'center'
        });
    }
}

function escapeHtml(value) {
    return value
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function openPatientModal(id) {
    const modal = document.getElementById(id);

    if (!modal) {
        return;
    }

    if (id === 'changePasswordModal') {
        const form = document.getElementById('changePasswordForm');
        if (form) {
            clearValidation(form, 'changePasswordValidationMessage');
            form.reset();
        }
    }

    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closePatientModal(id) {
    const modal = document.getElementById(id);

    if (!modal) {
        return;
    }

    modal.classList.remove('show');
    document.body.style.overflow = '';
}

document.addEventListener('click', function (event) {
    if (event.target.classList.contains('patient-modal-overlay')) {
        event.target.classList.remove('show');
        document.body.style.overflow = '';
    }
});

document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
        document.querySelectorAll('.patient-modal-overlay.show').forEach(modal => {
            modal.classList.remove('show');
        });

        document.body.style.overflow = '';
    }
});

function setupChangePasswordValidation() {
    const form = document.getElementById('changePasswordForm');

    if (!form) {
        return;
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault(); // Always prevent default form submission

        clearValidation(form, 'changePasswordValidationMessage');

        const currentPassword = getValue(form, 'currentPassword');
        const newPassword = getValue(form, 'newPassword');
        const confirmPassword = getValue(form, 'confirmPassword');

        const errors = [];

        if (!currentPassword) {
            errors.push('Mật khẩu hiện tại là bắt buộc.');
            markInvalid(form, 'currentPassword');
        }

        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$]).{8,}$/;
        if (!newPassword) {
            errors.push('Mật khẩu mới là bắt buộc.');
            markInvalid(form, 'newPassword');
        } else if (!passwordRegex.test(newPassword)) {
            errors.push('Mật khẩu mới phải từ 8 ký tự trở lên, gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt (!@#$).');
            markInvalid(form, 'newPassword');
        }

        if (!confirmPassword) {
            errors.push('Vui lòng xác nhận mật khẩu mới.');
            markInvalid(form, 'confirmPassword');
        } else if (newPassword !== confirmPassword) {
            errors.push('Mật khẩu mới và xác nhận mật khẩu không khớp.');
            markInvalid(form, 'confirmPassword');
        }

        if (errors.length > 0) {
            showValidationMessage('changePasswordValidationMessage', errors);
            scrollToValidationMessage('changePasswordValidationMessage');
            return;
        }

        // AJAX Request
        const formData = new FormData(form);
        fetch('/patient/profile/change-password', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(errData => {
                    throw new Error(errData.message || 'Thay đổi mật khẩu thất bại.');
                });
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                // Success - Close modal and reset form
                closePatientModal('changePasswordModal');
                form.reset();
                // Show success alert message directly without page reload
                let pageHeader = document.querySelector('.page-header');
                if (pageHeader) {
                    let oldAlert = document.querySelector('.patient-pwd-alert');
                    if (oldAlert) oldAlert.remove();
                    let alertDiv = document.createElement('div');
                    alertDiv.className = 'alert alert-success patient-pwd-alert';
                    alertDiv.style.marginTop = '15px';
                    alertDiv.textContent = data.message || 'Thay đổi mật khẩu thành công!';
                    pageHeader.after(alertDiv);
                    setTimeout(function() { alertDiv.remove(); }, 6000);
                }
            } else {
                // Failure - Show error inside modal, keep modal open
                showValidationMessage('changePasswordValidationMessage', [data.message]);
                scrollToValidationMessage('changePasswordValidationMessage');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showValidationMessage('changePasswordValidationMessage', [error.message || 'Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.']);
            scrollToValidationMessage('changePasswordValidationMessage');
        });
    });
}

function changePageSize(size) {
    const urlParams = new URLSearchParams(window.location.search);
    urlParams.set('size', size);
    urlParams.set('page', '0'); // Reset to first page
    window.location.search = urlParams.toString();
}

function setupPasswordToggle() {
    document.querySelectorAll('.toggle-password').forEach(function (icon) {
        icon.addEventListener('click', function () {
            const targetId = this.getAttribute('data-target');
            const input = document.getElementById(targetId);
            if (input) {
                if (input.type === 'password') {
                    input.type = 'text';
                    this.classList.remove('fa-eye');
                    this.classList.add('fa-eye-slash');
                } else {
                    input.type = 'password';
                    this.classList.remove('fa-eye-slash');
                    this.classList.add('fa-eye');
                }
            }
        });
    });
}
