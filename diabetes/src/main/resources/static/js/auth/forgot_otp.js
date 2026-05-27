const otpInputs = document.querySelectorAll('.otp-input');

// Auto-focus logic for OTP inputs
otpInputs.forEach((input, index) => {
    input.addEventListener('input', () => {
        input.value = input.value.replace(/[^0-9]/g, '');
        if (input.value !== '' && index < otpInputs.length - 1) {
            otpInputs[index + 1].focus();
        }
    });

    input.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && input.value === '' && index > 0) {
            otpInputs[index - 1].focus();
        }
    });

    // Handle paste event
    input.addEventListener('paste', (e) => {
        e.preventDefault();
        const pastedData = e.clipboardData.getData('text').replace(/[^0-9]/g, '').slice(0, 6);
        for (let i = 0; i < pastedData.length; i++) {
            if (otpInputs[i]) {
                otpInputs[i].value = pastedData[i];
                if (i < 5) otpInputs[i + 1].focus();
            }
        }
    });
});

// Countdown Logic
let timeLeft = 59;
const countdownEl = document.getElementById('countdown');
const resendBtn = document.getElementById('resendBtn');

const timer = setInterval(() => {
    timeLeft--;
    countdownEl.textContent = `Gửi lại sau ${timeLeft}s`;
    if (timeLeft <= 0) {
        clearInterval(timer);
        countdownEl.style.display = 'none';
        resendBtn.style.display = 'inline-block';
    }
}, 1000);

resendBtn.addEventListener('click', (e) => {
    e.preventDefault();
    resendBtn.style.display = 'none';
    countdownEl.style.display = 'inline-block';
    timeLeft = 59;
    countdownEl.textContent = `Gửi lại sau ${timeLeft}s`;
});

// Submit - gom 6 ô OTP vào hidden input rồi submit form
document.getElementById('verifyOtpForm').addEventListener('submit', function(e) {
    const errorDiv = document.getElementById('errorMsg');
    const otp = Array.from(otpInputs).map(input => input.value).join('');
    errorDiv.style.display = 'none';

    if (otp.length !== 6) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Vui lòng nhập đủ mã OTP';
        return;
    }

    // Ghi giá trị OTP đã gom vào hidden input để form gửi lên server
    document.getElementById('otpValue').value = otp;
});
