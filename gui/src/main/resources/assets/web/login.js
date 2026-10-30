document.getElementById('login-form').addEventListener('submit', async (e) => {
    // 1. Prevent the browser's default full-page POST submission
    e.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('error-message');
    const submitBtn = document.getElementById('submit-btn');

    // Reset error display and disable button during request
    errorDiv.style.display = 'none';
    errorDiv.textContent = '';
    submitBtn.disabled = true;
    submitBtn.textContent = 'Signing in...';

    try {
        // 2. Send credentials as JSON using fetch
        const response = await fetch(`/api/login?username=${username}&password=${password}`, {
            method: 'POST',
        });
        console.log("Test")
        console.log(response)

        if (response.ok) {
            // 3. Handle Success (2xx status)
            const data = await response.json();
            console.log(data)
            window.location.href = data.redirect || '/';
        } else if (response.status === 401) {
            // 4. Handle Bad Credentials (401 Unauthorized)
            const data = await response.json();
            errorDiv.textContent = data.error || 'Invalid username or password.';
            errorDiv.style.display = 'block';
        } else {
            // 5. Handle other unexpected server errors
            errorDiv.textContent = 'Something went wrong. Please try again later.';
            errorDiv.style.display = 'block';
        }
    } catch (err) {
        // 6. Handle network-level failures (e.g., user is offline)
        errorDiv.textContent = 'Network error. Please check your connection.';
        errorDiv.style.display = 'block';
    } finally {
        // Re-enable the submit button
        submitBtn.disabled = false;
        submitBtn.textContent = 'Log In';
    }
});