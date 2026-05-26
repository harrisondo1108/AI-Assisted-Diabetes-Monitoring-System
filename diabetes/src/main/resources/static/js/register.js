const roleSelect = document.getElementById('role');
const patientFields = document.getElementById('patientFields');
const doctorFields = document.getElementById('doctorFields');
const phoneInput = document.getElementById('phoneNumber');
const passwordInput = document.getElementById('password');
const confirmInput = document.getElementById('confirmPassword');
const phoneHint = document.getElementById('phoneHint');
const passwordHint = document.getElementById('passwordHint');
const confirmHint = document.getElementById('confirmHint');

// Hiển thị form theo role
// roleSelect.addEventListener('change', function() {
//     if (this.value === 'DOC') {
//         patientFields.style.display = 'none';
//         doctorFields.style.display = 'block';
//     } else {
//         patientFields.style.display = 'block';
//         doctorFields.style.display = 'none';
//     }
// });

// Validate phone
phoneInput.addEventListener('input', function() {
    this.value = this.value.replace(/[^0-9]/g, '').slice(0, 11);
    if (this.value.length > 0 && (this.value.length < 10 || this.value.length > 11)) {
        phoneHint.style.display = 'block';
        this.classList.add('error');
    } else {
        phoneHint.style.display = 'none';
        this.classList.remove('error');
    }
});

// Validate password
passwordInput.addEventListener('input', function() {
    if (this.value.length > 0 && this.value.length < 6) {
        passwordHint.style.display = 'block';
        this.classList.add('error');
    } else {
        passwordHint.style.display = 'none';
        this.classList.remove('error');
    }
    if (confirmInput.value.length > 0 && confirmInput.value !== this.value) {
        confirmHint.style.display = 'block';
        confirmInput.classList.add('error');
    } else {
        confirmHint.style.display = 'none';
        confirmInput.classList.remove('error');
    }
});

confirmInput.addEventListener('input', function() {
    if (this.value !== passwordInput.value) {
        confirmHint.style.display = 'block';
        this.classList.add('error');
    } else {
        confirmHint.style.display = 'none';
        this.classList.remove('error');
    }
});

// Submit - chỉ validate, để form tự submit
document.getElementById('registerForm').addEventListener('submit', function(e) {
    const roleId = document.getElementById('role').value;
    const fullName = document.getElementById('fullName').value.trim();
    const phoneNumber = phoneInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmInput.value;
    const terms = document.getElementById('terms').checked;
    const errorDiv = document.getElementById('errorMsg');

    errorDiv.style.display = 'none';

    if (!fullName) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Vui lòng nhập họ và tên!';
        return;
    }
    if (!phoneNumber || phoneNumber.length < 10) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Số điện thoại không hợp lệ!';
        return;
    }
    if (!password || password.length < 6) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Mật khẩu phải có ít nhất 6 ký tự!';
        return;
    }
    if (password !== confirmPassword) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Mật khẩu xác nhận không khớp!';
        return;
    }
    if (!terms) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Vui lòng đồng ý với Điều khoản sử dụng!';
        return;
    }
});
