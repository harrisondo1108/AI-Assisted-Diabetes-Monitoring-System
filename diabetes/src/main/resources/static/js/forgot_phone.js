const phoneInput = document.getElementById('phoneNumber');
const phoneHint = document.getElementById('phoneHint');

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

document.getElementById('forgotPhoneForm').addEventListener('submit', function(e) {
    const errorDiv = document.getElementById('errorMsg');
    const phoneNumber = phoneInput.value.trim();
    errorDiv.style.display = 'none';

    if (!phoneNumber || phoneNumber.length < 10) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Số điện thoại không hợp lệ';
    }
});
