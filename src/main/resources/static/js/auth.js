const API_BASE_URL = '/api/auth';

function handleRegister(event) {
    event.preventDefault();
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    fetch(`${API_BASE_URL}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, password })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success && data.data && data.data.token) {
            console.log("Registration successful", data);
            localStorage.setItem('token', data.data.token);
            localStorage.setItem('role', data.data.role);
            window.location.href = '/user-dashboard';
        } else {
            console.error("Registration failed or Token missing", data);
            showError(data.message || 'Registration failed');
        }
    })
    .catch(error => {
        console.error("Error during registration:", error);
        showError('An error occurred. Check console for details.');
    });
}

function handleLogin(event, roleCheck) {
    event.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    fetch(`${API_BASE_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success && data.data && data.data.token) {
            console.log("Login successful", data);
            const role = data.data.role;
            if(roleCheck && role !== roleCheck) {
                showError('Access Denied: Incorrect Role');
                return;
            }
            localStorage.setItem('token', data.data.token);
            localStorage.setItem('role', role);
            
            if (role === 'ROLE_ADMIN') {
                window.location.href = '/admin-dashboard';
            } else {
                window.location.href = '/user-dashboard';
            }
        } else {
            console.error("Login failed or Token missing", data);
            showError(data.message || 'Login failed');
        }
    })
    .catch(error => {
         console.error("Error during login:", error);
         showError('An error occurred. Check console for details.');
    });
}

function showError(message) {
    const alert = document.getElementById('error-alert');
    alert.innerText = message;
    alert.style.display = 'block';
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    window.location.href = '/';
}
