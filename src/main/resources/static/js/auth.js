const TOKEN_KEY = 'authToken';
const USER_KEY = 'currentUser';

async function handleLogin(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorMessage = document.getElementById('errorMessage');

    if (!username || username.length < 3) {
        showError(errorMessage, 'Имя пользователя должно быть не менее 3 символов');
        return;
    }

    if (!password || password.length < 3) {
        showError(errorMessage, 'Пароль должен быть не менее 3 символов');
        return;
    }

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            sessionStorage.setItem(TOKEN_KEY, data.token);
            sessionStorage.setItem(USER_KEY, JSON.stringify({
                username: data.username,
                role: data.role,
                fullName: data.fullName
            }));
            window.location.href = '/';
            return;
        }

        showError(errorMessage, data.error || 'Ошибка входа');
    } catch (error) {
        showError(errorMessage, 'Ошибка подключения к серверу');
    }
}

async function handleRegister(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const fullName = document.getElementById('fullName').value;
    const phone = document.getElementById('phone').value;
    const position = document.getElementById('position').value;
    const role = document.getElementById('role')?.value || 'ROLE_INSPECTOR';

    const errorMessage = document.getElementById('errorMessage');
    const successMessage = document.getElementById('successMessage');

    if (!username || username.length < 3) {
        showError(errorMessage, 'Имя пользователя должно быть не менее 3 символов');
        return;
    }

    if (!password || password.length < 3) {
        showError(errorMessage, 'Пароль должен быть не менее 3 символов');
        return;
    }

    if (password !== confirmPassword) {
        showError(errorMessage, 'Пароли не совпадают');
        return;
    }

    if (!fullName) {
        showError(errorMessage, 'Введите ФИО');
        return;
    }

    try {
        const token = getToken();
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(token && { 'Authorization': `Bearer ${token}` })
            },
            body: JSON.stringify({ username, password, fullName, phone: phone || '', position: position || '', role })
        });

        const responseText = await response.text();
        let data = {};
        if (responseText) {
            try {
                data = JSON.parse(responseText);
            } catch (e) {
                data = { error: responseText };
            }
        }

        if (response.ok) {
            showSuccess(successMessage, `Пользователь ${data.username || username} создан`);
            event.target.reset();
            return;
        }

        showError(errorMessage, data.error || data.message || 'Ошибка регистрации');
    } catch (error) {
        showError(errorMessage, 'Ошибка подключения к серверу');
    }
}

function showError(element, message) {
    if (!element) return;
    element.textContent = message;
    element.style.display = 'block';
    setTimeout(() => {
        element.style.display = 'none';
    }, 5000);
}

function showSuccess(element, message) {
    if (!element) return;
    element.textContent = message;
    element.style.display = 'block';
}

function getToken() {
    return sessionStorage.getItem(TOKEN_KEY);
}

function getCurrentUser() {
    const user = sessionStorage.getItem(USER_KEY);
    if (!user) return null;
    const parsed = JSON.parse(user);
    return {
        ...parsed,
        role: normalizeRole(parsed.role)
    };
}

function isAuthenticated() {
    return !!getToken();
}

function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

function logout() {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    window.location.href = '/login';
}

function roleLabel(role) {
    const normalized = normalizeRole(role);
    const labels = {
        ROLE_ADMIN: 'Администратор',
        ROLE_INSPECTOR: 'Проверяющий',
        ROLE_LABORANT: 'Лаборант'
    };
    return labels[normalized] || normalized;
}

function normalizeRole(role) {
    if (!role) return '';
    const upper = String(role).toUpperCase().trim();
    return upper.startsWith('ROLE_') ? upper : `ROLE_${upper}`;
}

function hasAnyRole(user, roles) {
    if (!user) return false;
    const normalizedUserRole = normalizeRole(user.role);
    return roles.map(normalizeRole).includes(normalizedUserRole);
}

function enforcePageAccess() {
    const path = window.location.pathname;
    const user = getCurrentUser();

    if (path === '/login') {
        return true;
    }

    if (!user) {
        window.location.href = '/login';
        return false;
    }

    // Админские страницы
    if ((path.startsWith('/users') || path === '/register') && !hasAnyRole(user, ['ROLE_ADMIN'])) {
        window.location.href = '/';
        return false;
    }

    return true;
}

function applyRoleVisibility(user) {
    const isAdmin = user?.role === 'ROLE_ADMIN';
    const canManageOrganizations = isAdmin || user?.role === 'ROLE_INSPECTOR';
    const canManageInspections = isAdmin || user?.role === 'ROLE_INSPECTOR';
    const canManageViolations = isAdmin || user?.role === 'ROLE_LABORANT';

    document.querySelectorAll('.admin-only').forEach(el => {
        el.style.display = isAdmin ? '' : 'none';
    });

    document.querySelectorAll('.manage-organizations').forEach(el => {
        el.style.display = canManageOrganizations ? '' : 'none';
    });

    document.querySelectorAll('.manage-inspections').forEach(el => {
        el.style.display = canManageInspections ? '' : 'none';
    });

    document.querySelectorAll('.manage-violations').forEach(el => {
        el.style.display = canManageViolations ? '' : 'none';
    });
}

function updateAuthUI() {
    const user = getCurrentUser();
    const navLinks = document.querySelector('.nav-links');

    if (user && navLinks) {
        applyRoleVisibility(user);

        let authLi = navLinks.querySelector('.auth-user-info');
        if (!authLi) {
            authLi = document.createElement('li');
            authLi.className = 'auth-user-info';
            authLi.innerHTML = `
                <span style="color: white;">${user.fullName} (${roleLabel(user.role)})</span>
                <a href="#" onclick="logout(); return false;" class="btn-sm" style="margin-left: 10px;">Выход</a>
            `;
            navLinks.appendChild(authLi);
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    if (!enforcePageAccess()) {
        return;
    }
    updateAuthUI();
});
