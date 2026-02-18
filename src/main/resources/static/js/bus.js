function handleSearch(event) {
    event.preventDefault();
    const source = document.getElementById('source').value;
    const destination = document.getElementById('destination').value;
    const token = localStorage.getItem('token');

    fetch(`/api/buses/search?source=${source}&destination=${destination}`, {
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(res => changeStatus(res))
    .then(data => {
        const container = document.getElementById('search-results');
        container.innerHTML = '';
        if (data.success && data.data.length > 0) {
            data.data.forEach(bus => {
                container.innerHTML += `
                    <div class="card">
                        <h3>${bus.busNumber}</h3>
                        <p>${bus.source} &rarr; ${bus.destination}</p>
                        <p>Departure: ${new Date(bus.departureTime).toLocaleString()}</p>
                        <p>Price: $${bus.price}</p>
                        <p>Available: ${bus.availableSeats}</p>
                        <button onclick="bookSeat(${bus.id})" class="btn">Book Seat</button>
                    </div>
                `;
            });
        } else {
            container.innerHTML = '<p>No buses found.</p>';
        }
    });
}

function bookSeat(busId) {
    const seatNumber = prompt("Enter Seat Number to book:");
    if (!seatNumber) return;

    const token = localStorage.getItem('token');
    const userId = getUserIdFromToken(token); // Ideally decoding token or fetching profile
    
    // Quick fix: decode token payload manually since we don't have a library
    // Or simpler: We need userId. Our current backend requires userId in POST body/param
    // Ideally backend should extract it from token context.
    // For now, let's assume we can parse it from token payload
    
    const payload = JSON.parse(atob(token.split('.')[1]));
    const currentUserId = payload.userId;

    fetch(`/api/bookings?userId=${currentUserId}&busId=${busId}&seatNumber=${seatNumber}`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(res => changeStatus(res))
    .then(data => {
        if (data.success) {
            alert('Booking Confirmed!');
            window.location.href = '/user-dashboard';
        } else {
            alert(data.message || 'Booking Failed');
        }
    });
}

function changeStatus(response) {
    if (response.status === 401) {
        window.location.href = '/login';
        throw new Error("Unauthorized");
    }
    return response.json();
}

function getUserIdFromToken(token) {
     try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.userId;
    } catch (e) {
        return null;
    }
}
