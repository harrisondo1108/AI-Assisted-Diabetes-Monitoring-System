document.addEventListener('DOMContentLoaded', () => {
    const otpInputs = document.querySelectorAll('.otp-input');
    const countdownEl = document.getElementById('countdown');
    const resendBtn = document.getElementById('resendBtn');
    let timeLeft = 59;
    let timer;

    // --- OTP Input Logic ---
    otpInputs.forEach((input, index) => {
        // Handle input (typing numbers)
        input.addEventListener('input', (e) => {
            const val = e.data || e.target.value;
            
            // Allow only numbers
            if (!/^[0-9]*$/.test(val)) {
                input.value = '';
                return;
            }

            // If value is entered, move to next
            if (input.value !== '' && index < otpInputs.length - 1) {
                otpInputs[index + 1].focus();
            }
        });

        // Handle Backspace for moving backward
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace') {
                if (input.value === '' && index > 0) {
                    otpInputs[index - 1].focus();
                } else {
                    input.value = ''; // Clear if not already empty
                }
            } else if (e.key === 'ArrowLeft' && index > 0) {
                otpInputs[index - 1].focus();
            } else if (e.key === 'ArrowRight' && index < otpInputs.length - 1) {
                otpInputs[index + 1].focus();
            }
        });

        // Clear input on focus to make re-entry easier
        input.addEventListener('focus', () => {
            input.select();
        });

        // Handle paste event
        input.addEventListener('paste', (e) => {
            e.preventDefault();
            const pastedData = e.clipboardData.getData('text').replace(/[^0-9]/g, '').slice(0, 6);

            for (let i = 0; i < pastedData.length; i++) {
                if (otpInputs[i]) {
                    otpInputs[i].value = pastedData[i];
                    if (i < otpInputs.length - 1 && i === pastedData.length - 1) {
                        otpInputs[i + 1].focus();
                    }
                }
            }
            
            // Focus the last filled input or the next one
            const nextIndex = Math.min(pastedData.length, otpInputs.length - 1);
            otpInputs[nextIndex].focus();
        });
    });

    // --- Countdown Logic ---
    function startTimer() {
        clearInterval(timer);
        timeLeft = 59;
        updateTimerDisplay();

        timer = setInterval(() => {
            timeLeft--;
            updateTimerDisplay();

            if (timeLeft <= 0) {
                clearInterval(timer);
                countdownEl.style.display = 'none';
                resendBtn.style.display = 'inline-block';
            }
        }, 1000);
    }

    function updateTimerDisplay() {
        countdownEl.textContent = `Gửi lại sau ${timeLeft}s`;
    }

    resendBtn.addEventListener('click', (e) => {
        e.preventDefault();
        resendBtn.style.display = 'none';
        countdownEl.style.display = 'inline-block';
        startTimer();
        
        // Add your AJAX call to resend OTP here
        console.log("OTP Resent!");
    });

    // Initialize timer
    startTimer();
});