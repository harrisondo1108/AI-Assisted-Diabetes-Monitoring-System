const roleSelect = document.getElementById('role');
const patientFields = document.getElementById('patientFields');
const doctorFields = document.getElementById('doctorFields');
const phoneInput = document.getElementById('phoneNumber');
const passwordInput = document.getElementById('password');
const confirmInput = document.getElementById('confirmPassword');
const phoneHint = document.getElementById('phoneHint');
const passwordHint = document.getElementById('passwordHint');
const confirmHint = document.getElementById('confirmHint');
const weightInput = document.getElementById("weight");
const heightInput = document.getElementById("height");
const weightHint = document.getElementById("weightHint");
const heightHint = document.getElementById("heightHint");
const emailInput = document.getElementById('email');
const emailHint = document.getElementById('emailHint');
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

// Validate weight
weightInput.addEventListener('input', function () {
    intValue = parseInt(this.value);
    if (intValue <= 0 || intValue >= 1000) {
        weightHint.style.display = 'block';
        this.classList.add('error');
    } else {
        weightHint.style.display = 'none'
        this.classList.remove('error')
    }
});

heightInput.addEventListener('input', function () {
    intValue = parseInt(this.value);
    if (intValue <= 0 || intValue >= 300) {
        heightHint.style.display = 'block';
        this.classList.add('error');
    } else {
        heightHint.style.display = 'none';
        this.classList.remove('error');
    }
});

// Validate phone
phoneInput.addEventListener('input', function () {
    this.value = this.value.replace(/[^0-9]/g, '').slice(0, 11);
    if (this.value.length > 0 && (this.value.length < 10 || this.value.length > 11)) {
        phoneHint.style.display = 'block';
        this.classList.add('error');
    } else {
        phoneHint.style.display = 'none';
        this.classList.remove('error');
    }
});

// Validate email
emailInput.addEventListener('input', function () {
    const emailVal = this.value.trim();
    if (emailVal.length > 0 && (!/^[A-Za-z0-9+_.-]+@.+$/.test(emailVal) || emailVal.length > 100)) {
        emailHint.style.display = 'block';
        this.classList.add('error');
    } else {
        emailHint.style.display = 'none';
        this.classList.remove('error');
    }
});

// Validate password
const pwdCard = document.getElementById('pwdValidatorCard');

function checkPasswordStrength(val) {
    const isLenVal = val.length >= 8;
    const isCaseVal = /[a-z]/.test(val) && /[A-Z]/.test(val);
    const isDigitVal = /\d/.test(val);
    const isSpecialVal = /[!@#$]/.test(val);

    return isLenVal && isCaseVal && isDigitVal && isSpecialVal;
}

passwordInput.addEventListener('focus', function () {
    pwdCard.classList.add('open');
});

passwordInput.addEventListener('input', function () {
    const val = this.value;
    pwdCard.classList.add('open');
    const isValid = checkPasswordStrength(val);

    if (val.length > 0 && !isValid) {
        passwordHint.style.display = 'block';
        this.classList.add('error');
    } else {
        passwordHint.style.display = 'none';
        this.classList.remove('error');
    }

    if (confirmInput.value.length > 0 && confirmInput.value !== val) {
        confirmHint.style.display = 'block';
        confirmInput.classList.add('error');
    } else {
        confirmHint.style.display = 'none';
        confirmInput.classList.remove('error');
    }
});

// Validate confirm password
confirmInput.addEventListener('input', function () {
    if (this.value !== passwordInput.value) {
        confirmHint.style.display = 'block';
        this.classList.add('error');
    } else {
        confirmHint.style.display = 'none';
        this.classList.remove('error');
    }
});
//Limit date
document.addEventListener("DOMContentLoaded", function () {
    const dobInput = document.getElementById("dob");

    // Lấy ngày hôm nay dưới định dạng YYYY-MM-DD
    const today = new Date().toISOString().split("T")[0];

    // Tính ngày cách đây 120 năm để làm mốc tối thiểu (tránh lỗi nhập năm 0001)
    const minDate = new Date();
    minDate.setFullYear(minDate.getFullYear() - 120);
    const minDateString = minDate.toISOString().split("T")[0];

    // Gán vào thuộc tính max và min của ô input
    dobInput.setAttribute("max", today);
    dobInput.setAttribute("min", minDateString);
});

// Submit - chỉ validate, để form tự submit
document.getElementById('registerForm').addEventListener('submit', function (e) {
    const roleId = document.getElementById('role').value;
    const fullName = document.getElementById('fullName').value.trim();
    const phoneNumber = phoneInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmInput.value;
    const terms = document.getElementById('terms').checked;
    const errorDiv = document.getElementById('errorMsg');
    errorDiv.style.display = 'none';
    intHeight = parseInt(heightInput.value);
    intWeight = parseInt(weightInput.value);

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

    const email = emailInput.value.trim();
    if (email.length > 0 && (!/^[A-Za-z0-9+_.-]+@.+$/.test(email) || email.length > 100)) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Địa chỉ email không hợp lệ!';
        return;
    }

    const isLenVal = password.length >= 8;
    const isCaseVal = /[a-z]/.test(password) && /[A-Z]/.test(password);
    const isDigitVal = /\d/.test(password);
    const isSpecialVal = /[!@#$]/.test(password);
    const isPasswordValid = isLenVal && isCaseVal && isDigitVal && isSpecialVal;

    if (!password || !isPasswordValid) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Mật khẩu chưa đạt yêu cầu bảo mật!';
        return;
    }
    if (password !== confirmPassword) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Mật khẩu xác nhận không khớp!';
        return;
    }
    if (intHeight <= 0 || intHeight >= 300) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Chiều cao không hợp lệ!';
        return;
    }
    if (intWeight <= 0 || intWeight >= 1000) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Cân nặng không hợp lệ!';
        return;
    }

    if (!terms) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Vui lòng đồng ý với Điều khoản sử dụng!';
        return;
    }
});

document.querySelectorAll('.toggle-password, .password-toggle').forEach(function(icon) {
    icon.addEventListener('click', function() {
        const targetId = this.getAttribute('data-target') || (this.id === 'toggleConfirm' ? 'confirmPassword' : 'password');
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
