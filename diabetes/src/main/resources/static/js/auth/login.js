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
