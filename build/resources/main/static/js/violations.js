async function deleteViolation(id) {
    if (!confirm('Вы уверены, что хотите удалить это нарушение?')) {
        return;
    }

    try {
        const token = getToken();
        const response = await fetch(`/api/violations/${id}`, {
            method: 'DELETE',
            headers: {
                ...(token && { 'Authorization': `Bearer ${token}` })
            }
        });

        if (response.ok) {
            window.location.reload();
            return;
        }

        const errorText = await response.text();
        let message = 'Ошибка удаления нарушения';
        if (errorText) {
            try {
                const parsed = JSON.parse(errorText);
                message = parsed.error || parsed.message || message;
            } catch (e) {
                message = errorText;
            }
        }
        alert(message);
    } catch (e) {
        alert('Ошибка подключения к серверу');
    }
}

async function editViolation(id) {
    window.location.href = `/violations/${id}/edit`;
}

function cancelEdit() {
    const createSection = document.getElementById('violationCreateSection');
    if (createSection) {
        createSection.style.display = 'none';
    }
}

function showCreateSection() {
    cancelEdit();
    const createSection = document.getElementById('violationCreateSection');
    if (createSection) {
        createSection.style.display = 'block';
        createSection.scrollIntoView({ behavior: 'smooth' });
    }
}

function preselectInspectionFromUrl() {
    const params = new URLSearchParams(window.location.search);
    const inspectionId = params.get('inspectionId');
    const select = document.getElementById('inspectionId');
    if (inspectionId && select) {
        select.value = inspectionId;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    preselectInspectionFromUrl();
});
