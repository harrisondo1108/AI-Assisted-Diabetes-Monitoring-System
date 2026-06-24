document.addEventListener('DOMContentLoaded', function() {
    const otpInputs = document.querySelectorAll('.otp-input');
    const otpForm = document.getElementById('verifyOtpForm');
    const otpHidden = document.getElementById('otpValue');
    const countdownSpan = document.getElementById('countdown');
    const resendBtn = document.getElementById('resendBtn');

    let countdownInterval;
    let timeLeft = 60;

    // Auto focus and move between OTP fields
    otpInputs.forEach((input, index) => {
        input.addEventListener('input', function(e) {
            if (this.value.length === 1 && index < otpInputs.length - 1) {
                otpInputs[index + 1].focus();
            }
            updateOtpValue();
        });

        input.addEventListener('keydown', function(e) {
            if (e.key === 'Backspace' && this.value.length === 0 && index > 0) {
                otpInputs[index - 1].focus();
            }
        });
    });

    function updateOtpValue() {
        let otp = '';
        otpInputs.forEach(input => {
            otp += input.value;
        });
        otpHidden.value = otp;
    }

    // Form submission: combine OTP before submit
    otpForm.addEventListener('submit', function(e) {
        updateOtpValue();
        if (otpHidden.value.length !== 6) {
            e.preventDefault();
            showError('Please enter full 6-digit OTP code');
        }
    });

    // Countdown timer
    function startCountdown() {
        clearInterval(countdownInterval);
        timeLeft = 60;
        countdownSpan.style.display = 'inline';
        resendBtn.style.display = 'none';

        countdownInterval = setInterval(() => {
            timeLeft--;
            if (timeLeft <= 0) {
                clearInterval(countdownInterval);
                countdownSpan.style.display = 'none';
                resendBtn.style.display = 'inline';
            } else {
                countdownSpan.textContent = `Resend in ${timeLeft}s`;
            }
        }, 1000);
    }

    // Resend OTP
    resendBtn.addEventListener('click', async function(e) {
        e.preventDefault();
        try {
            const response = await fetch('/register/resend-otp', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            });
            if (response.ok) {
                startCountdown();
                // Clear OTP inputs
                otpInputs.forEach(input => input.value = '');
                otpInputs[0].focus();
                showSuccess('A new OTP code has been sent.');
            } else {
                const errorText = await response.text();
                showError(errorText || 'Cannot resend OTP. Please try again.');
            }
        } catch (err) {
            showError('Network error. Please try again.');
        }
    });

    function showError(message) {
        let errorDiv = document.getElementById('errorMsg');
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 5000);
    }

    function showSuccess(message) {
        let successDiv = document.createElement('div');
        successDiv.className = 'success-message';
        successDiv.textContent = message;
        successDiv.style.marginBottom = '15px';
        const form = document.getElementById('verifyOtpForm');
        form.insertBefore(successDiv, form.firstChild);
        setTimeout(() => successDiv.remove(), 5000);
    }

    startCountdown();
});