document.getElementById('loginForm').addEventListener('submit', function(e) {
    const phoneNumber = document.getElementById('phoneNumber').value.trim();
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('errorMsg');

    errorDiv.style.display = 'none';

    if (!phoneNumber || !password) {
        e.preventDefault();
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Please enter your phone number and password!';
    }
});

document.querySelectorAll('.toggle-password, .password-toggle').forEach(function(icon) {
    icon.addEventListener('click', function() {
        const targetId = this.getAttribute('data-target') || 'password';
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
