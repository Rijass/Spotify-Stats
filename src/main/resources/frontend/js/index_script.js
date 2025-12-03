document.addEventListener('DOMContentLoaded', () => {
    const tabButtons = document.querySelectorAll('.tab-button');
    const forms = document.querySelectorAll('.form');
    const feedback = document.getElementById('form-feedback');
    const tabTriggers = document.querySelectorAll('[data-tab-trigger]');

    const switchTab = (tabName) => {
        tabButtons.forEach((button) => {
            const isActive = button.dataset.tab === tabName;
            button.classList.toggle('active', isActive);
            button.setAttribute('aria-selected', isActive);
        });

        forms.forEach((form) => {
            form.classList.toggle('active', form.id.includes(tabName));
        });

        feedback.classList.remove('show');
    };

    tabButtons.forEach((button) => {
        button.addEventListener('click', () => switchTab(button.dataset.tab));
    });

    tabTriggers.forEach((trigger) => {
        trigger.addEventListener('click', () => switchTab(trigger.dataset.tabTrigger));
    });

    const apiBase = 'http://localhost:8080/api/users';

    const redirectToApp = () => {
        window.location.href = 'page.html';
    };

    const checkExistingSession = async () => {
        try {
            const response = await fetch(`${apiBase}/session`, { credentials: 'include' });
            if (response.ok) {
                redirectToApp();
            }
        } catch (error) {
            console.error('Session check failed', error);
        }
    };

    const showFeedback = (message) => {
        feedback.textContent = message;
        feedback.classList.add('show');
    };

    const handleSuccess = (action) => {
        showFeedback(`${action} erfolgreich! Du wirst weitergeleitet ...`);
        setTimeout(redirectToApp, 900);
    };

    const persistSession = (data) => {
        if (data?.sessionToken) {
            localStorage.setItem('spotify-tracker-session', data.sessionToken);
        }
        if (data?.username) {
            localStorage.setItem('spotify-tracker-user', data.username);
        }
    };

    const postJson = async (path, payload) => {
        const response = await fetch(`${apiBase}${path}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload),
            credentials: 'include'
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || 'Unbekannter Fehler');
        }

        return response.json();
    };

    const loginForm = document.getElementById('login-form');
    loginForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const identifier = loginForm.identifier.value.trim();
        const password = loginForm.password.value.trim();

        if (!identifier || !password) {
            showFeedback('Bitte fülle alle Felder aus.');
            return;
        }

        postJson('/login', { identifier, password })
            .then((data) => {
                persistSession(data);
                handleSuccess('Login');
            })
            .catch(() => {
                showFeedback('Login fehlgeschlagen. Bitte überprüfe deine Daten.');
            });
    });

    const registerForm = document.getElementById('register-form');
    registerForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const username = registerForm.username.value.trim();
        const email = registerForm.email.value.trim();
        const password = registerForm.password.value.trim();

        if (!username || !email || !password) {
            showFeedback('Bitte alle Registrierungsfelder ausfüllen.');
            return;
        }

        postJson('', { username, email, password })
            .then((data) => {
                persistSession(data);
                handleSuccess('Registrierung');
            })
            .catch(() => {
                showFeedback('Registrierung fehlgeschlagen. Bitte erneut versuchen.');
            });
    });

    checkExistingSession();
});