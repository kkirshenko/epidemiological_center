// Organizations JavaScript for form handling and AJAX operations

const API_BASE = '/api/organizations';

// Handle form submission (create/update)
async function handleSave(event) {
    event.preventDefault();
    
    const form = document.getElementById('organizationForm');
    const orgId = document.getElementById('orgId').value;
    const errorMessage = document.getElementById('errorMessage');
    
    // Client-side validation
    const name = document.getElementById('name').value.trim();
    const city = document.getElementById('city').value.trim();
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    
    if (!name || name.length < 3) {
        showError(errorMessage, 'Название должно быть не менее 3 символов');
        return;
    }
    
    if (!city) {
        showError(errorMessage, 'Введите город');
        return;
    }
    
    if (!email || !isValidEmail(email)) {
        showError(errorMessage, 'Введите корректный email');
        return;
    }
    
    if (!phone || phone.length < 10) {
        showError(errorMessage, 'Введите корректный телефон');
        return;
    }
    
    const employeeCount = parseInt(document.getElementById('employeeCount').value) || 0;
    if (employeeCount < 0) {
        showError(errorMessage, 'Количество сотрудников не может быть отрицательным');
        return;
    }
    
    const organizationData = {
        name: name,
        shortName: document.getElementById('shortName').value.trim(),
        registrationNumber: document.getElementById('registrationNumber').value.trim(),
        type: { id: parseInt(document.getElementById('typeId').value) },
        address: document.getElementById('address').value.trim(),
        city: city,
        directorName: document.getElementById('directorName').value.trim(),
        phone: phone,
        email: email,
        employeeCount: employeeCount,
        riskCategory: document.getElementById('riskCategory').value,
        notes: document.getElementById('notes').value.trim()
    };
    
    try {
        const token = getToken();
        const url = orgId ? `${API_BASE}/${orgId}` : API_BASE;
        const method = orgId ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                ...(token && { 'Authorization': `Bearer ${token}` })
            },
            body: JSON.stringify(organizationData)
        });
        
        if (response.ok) {
            window.location.href = '/organizations';
        } else {
            const errorText = await response.text();
            let message = 'Ошибка сохранения';
            try {
                const error = JSON.parse(errorText);
                message = error.message || error.error || message;
            } catch (e) {
                if (errorText) {
                    message = errorText;
                }
            }
            showError(errorMessage, message);
        }
    } catch (error) {
        showError(errorMessage, 'Ошибка подключения к серверу');
        console.error('Save error:', error);
    }
}

// Load organization data for editing
async function loadOrganization(id) {
    try {
        const token = getToken();
        const response = await fetch(`${API_BASE}/${id}`, {
            headers: token ? { 'Authorization': `Bearer ${token}` } : {}
        });
        
        if (response.ok) {
            const org = await response.json();
            document.getElementById('orgId').value = org.id;
            document.getElementById('name').value = org.name;
            document.getElementById('shortName').value = org.shortName;
            document.getElementById('registrationNumber').value = org.registrationNumber;
            document.getElementById('typeId').value = org.type?.id || '';
            document.getElementById('address').value = org.address;
            document.getElementById('city').value = org.city;
            document.getElementById('directorName').value = org.directorName;
            document.getElementById('phone').value = org.phone;
            document.getElementById('email').value = org.email;
            document.getElementById('employeeCount').value = org.employeeCount;
            document.getElementById('riskCategory').value = org.riskCategory;
            document.getElementById('notes').value = org.notes;
        }
    } catch (error) {
        console.error('Load error:', error);
    }
}

// Delete organization
async function deleteOrganization(id) {
    if (!confirm('Вы уверены, что хотите удалить эту организацию?')) {
        return;
    }
    
    try {
        const token = getToken();
        const response = await fetch(`${API_BASE}/${id}`, {
            method: 'DELETE',
            headers: token ? { 'Authorization': `Bearer ${token}` } : {}
        });
        
        if (response.ok) {
            window.location.reload();
        } else {
            alert('Ошибка удаления организации');
        }
    } catch (error) {
        alert('Ошибка подключения к серверу');
        console.error('Delete error:', error);
    }
}

// Search organizations with AJAX
async function searchOrganizations(query) {
    try {
        const token = getToken();
        const response = await fetch(`${API_BASE}/search?query=${encodeURIComponent(query)}`, {
            headers: token ? { 'Authorization': `Bearer ${token}` } : {}
        });
        
        if (response.ok) {
            const organizations = await response.json();
            renderOrganizationsList(organizations);
        }
    } catch (error) {
        console.error('Search error:', error);
    }
}

// Render organizations list dynamically
function renderOrganizationsList(organizations) {
    const tbody = document.querySelector('.data-table tbody');
    if (!tbody) return;
    
    if (organizations.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="no-data">Нет организаций</td></tr>';
        return;
    }
    
    tbody.innerHTML = organizations.map(org => `
        <tr>
            <td>${escapeHtml(org.name)}</td>
            <td>${escapeHtml(org.shortName || '-')}</td>
            <td>${escapeHtml(org.city)}</td>
            <td>${escapeHtml(org.type?.name || '-')}</td>
            <td>${getRiskBadge(org.riskCategory)}</td>
            <td class="actions">
                <a href="/organizations/${org.id}" class="btn-sm">Просмотр</a>
                <a href="/organizations/${org.id}/edit" class="btn-sm">Редактировать</a>
                <button onclick="deleteOrganization('${org.id}')" class="btn-sm btn-danger">Удалить</button>
            </td>
        </tr>
    `).join('');
}

// Helper functions
function showError(element, message) {
    element.textContent = message;
    element.style.display = 'block';
    setTimeout(() => {
        element.style.display = 'none';
    }, 5000);
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getRiskBadge(risk) {
    const colors = {
        'low': '#28a745',
        'medium': '#ffc107',
        'high': '#fd7e14',
        'critical': '#dc3545'
    };
    const labels = {
        'low': 'Низкий',
        'medium': 'Средний',
        'high': 'Высокий',
        'critical': 'Критический'
    };
    return `<span style="background:${colors[risk]}; color: white; padding: 2px 8px; border-radius: 10px; font-size: 12px;">${labels[risk]}</span>`;
}

function getToken() {
    return sessionStorage.getItem('authToken');
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    const orgId = document.getElementById('orgId')?.value;
    if (orgId) {
        loadOrganization(orgId);
    }
});
