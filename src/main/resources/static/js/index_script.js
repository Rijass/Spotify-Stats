import { USERS_API_BASE } from './modules/api.js';
import { clearSession, getAccessToken, persistSession } from './modules/session.js';

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

    const redirectToApp = () => {
        window.location.href = 'page.html';
    };

    const checkExistingSession = async () => {

        const accessToken = getAccessToken();
        if (!accessToken) {
            return;
        }

        try {
            const response = await fetch(`${USERS_API_BASE}/session`, {
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            });
            if (response.ok) {
                redirectToApp();
            }
            if (response.status === 401) {
                clearSession();
                console.log('Session expired');
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

    const postJson = async (path, payload) => {
        const response = await fetch(`${USERS_API_BASE}${path}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
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