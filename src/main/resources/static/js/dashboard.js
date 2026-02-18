function checkAuth(requiredRole) {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    if (!token) {
        window.location.href = '/login';
        return;
    }

    if (requiredRole && role !== requiredRole) {
        alert('Access Denied');
        window.location.href = '/';
        return;
    }
}

function loadAdminBuses() {
    const token = localStorage.getItem('token');
    fetch('/api/buses', {
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            const container = document.getElementById('bus-list');
            container.innerHTML = '';
            data.data.content.forEach(bus => {
                container.innerHTML += `
                    <div class="card">
                        <h3>${bus.busNumber}</h3>
                        <p>${bus.source} &rarr; ${bus.destination}</p>
                        <p>Seats: ${bus.availableSeats}/${bus.totalSeats}</p>
                        <p>Price: $${bus.price}</p>
                    </div>
                `;
            });
        }
    });
}

function handleAddBus(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const busData = Object.fromEntries(formData.entries());

    fetch('/api/buses', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token'),
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(busData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('Bus added successfully!', 'success');
            loadAdminBuses();
            event.target.reset();
        } else {
            let errorMsg = data.message || 'Failed to add bus';
            if (data.data && typeof data.data === 'object') {
                // If validation errors, append them
                const validationErrors = Object.values(data.data).join(', ');
                if (validationErrors) {
                    errorMsg += ': ' + validationErrors;
                }
            }
            showToast(errorMsg, 'error');
            console.error("Add Bus Error:", data);
        }
    })
    .catch(error => {
        showToast('An error occurred while adding bus', 'error');
        console.error("Add Bus Network Error:", error);
    });
}

function loadUserBookings() {
    const token = localStorage.getItem('token');
    fetch('/api/bookings', {
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            const container = document.getElementById('booking-list');
            container.innerHTML = '';
            if(data.data.content.length === 0) {
                container.innerHTML = '<p>No bookings found.</p>';
                return;
            }
            data.data.content.forEach(booking => {
                container.innerHTML += `
                    <div class="card">
                        <h3>Booking #${booking.id}</h3>
                        <p>Bus: ${booking.bus.busNumber}</p>
                        <p>Route: ${booking.bus.source} &rarr; ${booking.bus.destination}</p>
                        <p>Seat: ${booking.seatNumber}</p>
                        <p>Status: <strong>${booking.status}</strong></p>
                    </div>
                `;
            });
        }
    });
}

function showToast(message, type) {
    let toast = document.getElementById('toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast';
        toast.className = 'toast';
        document.body.appendChild(toast);
    }
    toast.innerText = message;
    toast.className = 'toast show ' + type;
    setTimeout(function() {
        toast.className = toast.className.replace('show', '');
    }, 3000);
}
