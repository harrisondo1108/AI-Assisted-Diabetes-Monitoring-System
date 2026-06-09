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
const strengthContainer = document.getElementById('strengthContainer');
const line1 = document.getElementById('line1');
const line2 = document.getElementById('line2');
const line3 = document.getElementById('line3');
const strengthText = document.getElementById('strengthText');

newPassword.addEventListener('input', function() {
    const val = this.value;

    strengthContainer.style.display = val.length > 0 ? 'block' : 'none';

    let strength = 0;
    if (val.length >= 6) strength += 1;
    if (val.match(/[a-z]/) && val.match(/[A-Z]/)) strength += 1;
    if (val.match(/\d/) && val.match(/[^a-zA-Z\d]/)) strength += 1;

    [line1, line2, line3].forEach(el => el.className = 'line');

    if (strength === 3) {
        line1.classList.add('strong'); line2.classList.add('strong'); line3.classList.add('strong');
        strengthText.textContent = 'Mạnh'; strengthText.style.color = '#10b981';
    } else if (strength === 2) {
        line1.classList.add('medium'); line2.classList.add('medium');
        strengthText.textContent = 'Trung bình'; strengthText.style.color = '#f59e0b';
    } else if (strength === 1 || val.length > 0) {
        line1.classList.add('weak');
        strengthText.textContent = 'Yếu'; strengthText.style.color = '#ef4444';
    }

    if (val.length > 0 && val.length < 6) {
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

    if (!newPassword.value || newPassword.value.length < 6) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Mật khẩu phải có ít nhất 6 ký tự';
        return;
    }
    if (newPassword.value !== confirmPassword.value) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Mật khẩu xác nhận không khớp';
    }
});
