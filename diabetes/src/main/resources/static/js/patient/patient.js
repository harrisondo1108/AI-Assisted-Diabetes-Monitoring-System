document.addEventListener('DOMContentLoaded', function () {
    setupChat();
    setupProfileValidation();
    setupRoutineValidation();
    setupChangePasswordValidation();
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
        typingMessage.textContent = 'Typing...';
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
                throw new Error('Network error');
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
                errorMessage.textContent = 'Sorry, I encountered an error. Please try again.';
                messageList.appendChild(errorMessage);
            }
            messageList.scrollTop = messageList.scrollHeight;
        })
        .catch(err => {
            typingMessage.remove();
            const errorMessage = document.createElement('div');
            errorMessage.className = 'message ai';
            errorMessage.textContent = 'Connection error. Please try again later.';
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

        const errors = [];

        if (!fullName) {
            errors.push('Full name is required.');
            markInvalid(form, 'fullName');
        } else {
            if (fullName.length < 2 || fullName.length > 60) {
                errors.push('Full name must be between 2 and 60 characters.');
                markInvalid(form, 'fullName');
            }

            if (!isValidName(fullName)) {
                errors.push('Full name must contain letters and spaces only.');
                markInvalid(form, 'fullName');
            }
        }

        if (!phoneNumber) {
            errors.push('Phone number is required.');
            markInvalid(form, 'phoneNumber');
        } else if (!isValidPhone(phoneNumber)) {
            errors.push('Phone number must contain 10 to 15 digits.');
            markInvalid(form, 'phoneNumber');
        }

        if (address && address.length > 200) {
            errors.push('Address must not exceed 200 characters.');
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
                errors.push('Date of birth cannot be in the future.');
                markInvalid(form, 'dob');
            }

            if (dobDate < minDate) {
                errors.push('Date of birth is not valid.');
                markInvalid(form, 'dob');
            }
        }

        if (height) {
            const heightNumber = Number(height);

            if (Number.isNaN(heightNumber) || heightNumber < 50 || heightNumber > 250) {
                errors.push('Height must be between 50 and 250 cm.');
                markInvalid(form, 'height');
            }
        }

        if (weight) {
            const weightNumber = Number(weight);

            if (Number.isNaN(weightNumber) || weightNumber < 1 || weightNumber > 300) {
                errors.push('Weight must be between 1 and 300 kg.');
                markInvalid(form, 'weight');
            }
        }

        if (bloodgroup && !isValidBloodGroup(bloodgroup)) {
            errors.push('Blood group is not valid.');
            markInvalid(form, 'bloodgroup');
        }

        if (permanentMedicalHistory && permanentMedicalHistory.length > 500) {
            errors.push('Medical history must not exceed 500 characters.');
            markInvalid(form, 'permanentMedicalHistory');
        }

        if (allergyNotes && allergyNotes.length > 500) {
            errors.push('Allergy notes must not exceed 500 characters.');
            markInvalid(form, 'allergyNotes');
        }

        if (supervisorName) {
            if (supervisorName.length > 90) {
                errors.push('Supervisor name must not exceed 90 characters.');
                markInvalid(form, 'supervisorName');
            }

            if (!isValidName(supervisorName)) {
                errors.push('Supervisor name must contain letters and spaces only.');
                markInvalid(form, 'supervisorName');
            }
        }

        if (supervisorPhone && !isValidPhone(supervisorPhone)) {
            errors.push('Supervisor phone must contain 10 to 15 digits.');
            markInvalid(form, 'supervisorPhone');
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
            errors.push('Wake up time is required.');
            markInvalid(form, 'wakeUpTime');
        }

        if (!breakfastTime) {
            errors.push('Breakfast time is required.');
            markInvalid(form, 'breakfastTime');
        }

        if (!lunchTime) {
            errors.push('Lunch time is required.');
            markInvalid(form, 'lunchTime');
        }

        if (!dinnerTime) {
            errors.push('Dinner time is required.');
            markInvalid(form, 'dinnerTime');
        }

        if (!sleepTime) {
            errors.push('Sleep time is required.');
            markInvalid(form, 'sleepTime');
        }

        if (errors.length === 0) {
            if (!isTimeBefore(wakeUpTime, breakfastTime)) {
                errors.push('Breakfast time must be after wake up time.');
                markInvalid(form, 'wakeUpTime');
                markInvalid(form, 'breakfastTime');
            }

            if (!isTimeBefore(breakfastTime, lunchTime)) {
                errors.push('Lunch time must be after breakfast time.');
                markInvalid(form, 'breakfastTime');
                markInvalid(form, 'lunchTime');
            }

            if (!isTimeBefore(lunchTime, dinnerTime)) {
                errors.push('Dinner time must be after lunch time.');
                markInvalid(form, 'lunchTime');
                markInvalid(form, 'dinnerTime');
            }

            if (!isTimeBefore(wakeUpTime, sleepTime)) {
                errors.push('Sleep time must be after wake up time.');
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
    return /^[0-9]{10,15}$/.test(value);
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
        <strong>Please check the following information:</strong>
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
        clearValidation(form, 'changePasswordValidationMessage');

        const currentPassword = getValue(form, 'currentPassword');
        const newPassword = getValue(form, 'newPassword');
        const confirmPassword = getValue(form, 'confirmPassword');

        const errors = [];

        if (!currentPassword) {
            errors.push('Current password is required.');
            markInvalid(form, 'currentPassword');
        }

        if (!newPassword) {
            errors.push('New password is required.');
            markInvalid(form, 'newPassword');
        } else if (newPassword.length < 6) {
            errors.push('New password must be at least 6 characters.');
            markInvalid(form, 'newPassword');
        }

        if (!confirmPassword) {
            errors.push('Please confirm your new password.');
            markInvalid(form, 'confirmPassword');
        } else if (newPassword !== confirmPassword) {
            errors.push('New password and confirmation password do not match.');
            markInvalid(form, 'confirmPassword');
        }

        if (errors.length > 0) {
            event.preventDefault();
            showValidationMessage('changePasswordValidationMessage', errors);
            scrollToValidationMessage('changePasswordValidationMessage');
        }
    });
}