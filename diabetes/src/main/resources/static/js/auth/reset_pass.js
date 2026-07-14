const newPassword = document.getElementById('newPassword');
const confirmPassword = document.getElementById('confirmPassword');
const passwordHint = document.getElementById('passwordHint');
const confirmHint = document.getElementById('confirmHint');

// Toggle password visibility
function setupToggle(inputId, toggleId) {
    const toggle = document.getElementById(toggleId);
    const input = document.getElementById(inputId);
    toggle.addEventListener('click', () => {
        if (input.type === 'password') {
            input.type = 'text';
            toggle.classList.remove('fa-eye');
            toggle.classList.add('fa-eye-slash');
        } else {
            input.type = 'password';
            toggle.classList.add('fa-eye');
            toggle.classList.remove('fa-eye-slash');
        }
    });
}

setupToggle('newPassword', 'togglePswd');
setupToggle('confirmPassword', 'toggleConfirm');

// Password strength
const pwdCard = document.getElementById('pwdValidatorCard');

function checkPasswordStrength(val) {
    const isLenVal = val.length >= 8;
    const isCaseVal = /[a-z]/.test(val) && /[A-Z]/.test(val);
    const isDigitVal = /\d/.test(val);
    const isSpecialVal = /[!@#$]/.test(val);

    return isLenVal && isCaseVal && isDigitVal && isSpecialVal;
}

newPassword.addEventListener('focus', function() {
    pwdCard.classList.add('open');
});

newPassword.addEventListener('input', function() {
    const val = this.value;
    pwdCard.classList.add('open');
    const isValid = checkPasswordStrength(val);

    if (val.length > 0 && !isValid) {
        passwordHint.style.display = 'block'; this.classList.add('error');
    } else {
        passwordHint.style.display = 'none'; this.classList.remove('error');
    }

    if (confirmPassword.value.length > 0 && confirmPassword.value !== val) {
        confirmHint.style.display = 'block'; confirmPassword.classList.add('error');
    } else {
        confirmHint.style.display = 'none'; confirmPassword.classList.remove('error');
    }
});

confirmPassword.addEventListener('input', function() {
    if (this.value !== newPassword.value) {
        confirmHint.style.display = 'block'; this.classList.add('error');
    } else {
        confirmHint.style.display = 'none'; this.classList.remove('error');
    }
});

// Submit - chỉ validate, để form tự submit
document.getElementById('resetPasswordForm').addEventListener('submit', function(e) {
    const errorDiv = document.getElementById('errorMsg');
    errorDiv.style.display = 'none';

    const val = newPassword.value;
    const isLenVal = val.length >= 8;
    const isCaseVal = /[a-z]/.test(val) && /[A-Z]/.test(val);
    const isDigitVal = /\d/.test(val);
    const isSpecialVal = /[!@#$]/.test(val);
    const isPasswordValid = isLenVal && isCaseVal && isDigitVal && isSpecialVal;

    if (!val || !isPasswordValid) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Mật khẩu chưa đạt yêu cầu bảo mật!';
        return;
    }
    if (newPassword.value !== confirmPassword.value) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Mật khẩu xác nhận không khớp';
    }
});
