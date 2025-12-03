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

    const showFeedback = (message) => {
        feedback.textContent = message;
        feedback.classList.add('show');
    };

    const handleSuccess = (action) => {
        showFeedback(`${action} erfolgreich! Du wirst weitergeleitet ...`);
        setTimeout(() => {
            window.location.href = 'test.html';
        }, 900);
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

        // Hier könnte ein API-Request für den Login folgen.
        localStorage.setItem('spotify-tracker-user', identifier);
        handleSuccess('Login');
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

        // Hier könnte ein API-Request für die Registrierung folgen.
        localStorage.setItem('spotify-tracker-user', username);
        handleSuccess('Registrierung');
    });
});