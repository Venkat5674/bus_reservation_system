// ==============================================================================
// Dashboard & Ticket Management Logic
// ==============================================================================

function checkAuth(requiredRole) {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    // 1. Missing Token Check
    if (!token) {
        sessionStorage.setItem('redirect_after_login', window.location.pathname);
        if (requiredRole === 'ROLE_ADMIN') {
            window.location.href = '/admin-login';
        } else {
            window.location.href = '/login';
        }
        return false;
    }

    // 2. JWT Expiration Check
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const currentTime = Math.floor(Date.now() / 1000);
        if (payload.exp && payload.exp < currentTime) {
            console.warn("User session has expired");
            localStorage.removeItem('token');
            localStorage.removeItem('role');
            sessionStorage.setItem('redirect_after_login', window.location.pathname);
            alert("Your session has expired. Please sign in again.");
            window.location.href = requiredRole === 'ROLE_ADMIN' ? '/admin-login' : '/login';
            return false;
        }

        // Show user email if badge exists
        const adminBadge = document.getElementById('admin-user-badge');
        if (adminBadge && payload.sub) {
            adminBadge.innerHTML = `<i class="fa-solid fa-user-shield"></i> ${payload.sub}`;
        }
    } catch (e) {
        console.error("Invalid token format:", e);
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        window.location.href = '/login';
        return false;
    }

    // 3. Role Authorization Check
    if (requiredRole && role !== requiredRole) {
        showToast('Access restricted to ' + (requiredRole === 'ROLE_ADMIN' ? 'Administrators' : 'Passengers'), 'error');
        setTimeout(() => {
            window.location.href = role === 'ROLE_ADMIN' ? '/admin-dashboard' : '/user-dashboard';
        }, 1200);
        return false;
    }

    return true;
}

// ==============================================================================
// User Dashboard: Bookings, E-Ticket & Real Cancellation
// ==============================================================================
function loadUserBookings() {
    const token = localStorage.getItem('token');
    if (!token) return;

    const payload = JSON.parse(atob(token.split('.')[1]));
    const userId = payload.userId;
    const container = document.getElementById('booking-list');
    container.innerHTML = `
        <div style="text-align: center; padding: 3rem;">
            <i class="fa-solid fa-spinner fa-spin fa-2x" style="color: var(--primary);"></i>
            <p style="margin-top: 1rem; color: var(--text-muted);">Fetching your tickets & reservations...</p>
        </div>
    `;

    // Try fetching user specific bookings first, fallback to all bookings
    const fetchUrl = userId ? `/api/bookings/user/${userId}` : `/api/bookings`;

    fetch(fetchUrl, {
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(res => res.json())
        .then(data => {
            container.innerHTML = '';
            let bookings = [];

            if (data.success && data.data) {
                bookings = Array.isArray(data.data) ? data.data : (data.data.content || []);
            }

            // Update statistics cards
            const totalTrips = bookings.length;
            const confirmedTrips = bookings.filter(b => b.status === 'CONFIRMED').length;
            const cancelledTrips = bookings.filter(b => b.status === 'CANCELLED').length;

            if (document.getElementById('stat-total-trips')) {
                document.getElementById('stat-total-trips').innerText = totalTrips;
                document.getElementById('stat-confirmed-trips').innerText = confirmedTrips;
                document.getElementById('stat-cancelled-trips').innerText = cancelledTrips;
            }

            if (bookings.length === 0) {
                container.innerHTML = `
                <div style="text-align: center; background: white; padding: 4rem 2rem; border-radius: var(--radius-lg); border: 1px solid var(--border-color);">
                    <i class="fa-solid fa-ticket-simple fa-3x" style="color: #cbd5e1; margin-bottom: 1rem;"></i>
                    <h3>No Bookings Found</h3>
                    <p style="color: var(--text-muted); margin-top: 0.5rem;">You haven't booked any bus tickets yet. Start planning your journey!</p>
                    <a href="/bus-search" class="btn btn-primary" style="margin-top: 1.5rem;">
                        <i class="fa-solid fa-magnifying-glass"></i> Search Buses
                    </a>
                </div>
            `;
                return;
            }

            bookings.forEach(booking => {
                container.innerHTML += renderTicketCard(booking);
            });
        })
        .catch(err => {
            console.error("Failed to load bookings:", err);
            container.innerHTML = `<p style="color: var(--danger); text-align: center;">Error loading reservations.</p>`;
        });
}

function renderTicketCard(booking) {
    const isConfirmed = booking.status === 'CONFIRMED';
    const statusBadge = isConfirmed
        ? `<span class="badge badge-success"><i class="fa-solid fa-circle-check"></i> Confirmed</span>`
        : `<span class="badge badge-danger"><i class="fa-solid fa-circle-xmark"></i> Cancelled</span>`;

    const depDate = booking.bus.departureTime ? new Date(booking.bus.departureTime) : new Date();
    const arrDate = booking.bus.arrivalTime ? new Date(booking.bus.arrivalTime) : new Date();

    const formattedDate = depDate.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });
    const formattedDepTime = depDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const formattedArrTime = arrDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    return `
        <div class="ticket-card" id="ticket-${booking.id}">
            <div class="ticket-main">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.25rem;">
                    <div>
                        <div style="font-size: 0.8rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px;">
                            E-Ticket / PNR: #BK-${booking.id}
                        </div>
                        <h3 style="font-size: 1.3rem; font-weight: 800; color: var(--text-main); margin-top: 0.2rem;">
                            <i class="fa-solid fa-bus" style="color: var(--primary);"></i> ${booking.bus.busNumber}
                        </h3>
                    </div>
                    <div>${statusBadge}</div>
                </div>

                <div class="journey-timeline" style="margin-bottom: 1.5rem;">
                    <div class="timeline-point" style="text-align: left;">
                        <div class="point-time">${formattedDepTime}</div>
                        <div class="point-city" style="font-size: 1rem; color: var(--text-main); font-weight: 700;">${booking.bus.source}</div>
                        <div style="font-size: 0.75rem; color: var(--text-muted);">${formattedDate}</div>
                    </div>

                    <div class="timeline-duration" style="padding: 0 1.5rem;">
                        <i class="fa-solid fa-arrow-right-long" style="color: var(--primary); font-size: 1.2rem;"></i>
                        <span class="duration-text">Direct Express</span>
                    </div>

                    <div class="timeline-point" style="text-align: right;">
                        <div class="point-time">${formattedArrTime}</div>
                        <div class="point-city" style="font-size: 1rem; color: var(--text-main); font-weight: 700;">${booking.bus.destination}</div>
                        <div style="font-size: 0.75rem; color: var(--text-muted);">Express Arrival</div>
                    </div>
                </div>

                <div style="display: flex; gap: 2rem; font-size: 0.88rem; color: var(--text-muted); border-top: 1px dashed var(--border-color); padding-top: 1rem;">
                    <div><strong style="color: var(--text-main);">Passenger:</strong> ${booking.user.name}</div>
                    <div><strong style="color: var(--text-main);">Email:</strong> ${booking.user.email}</div>
                    <div><strong style="color: var(--text-main);">Fare:</strong> $${booking.bus.price.toFixed(2)}</div>
                </div>
            </div>

            <div class="ticket-stub">
                <div style="font-size: 0.75rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Seat Number</div>
                <div style="font-size: 2.5rem; font-weight: 800; color: var(--primary); margin: 0.2rem 0 0.8rem;">
                    ${booking.seatNumber}
                </div>

                ${isConfirmed ? `
                    <button onclick="handleCancelBooking(${booking.id})" class="btn btn-danger-outline btn-sm" style="width: 100%; margin-bottom: 0.5rem;">
                        <i class="fa-solid fa-ban"></i> Cancel Ticket
                    </button>
                    <button onclick="window.print()" class="btn btn-secondary btn-sm" style="width: 100%;">
                        <i class="fa-solid fa-print"></i> Print E-Ticket
                    </button>
                ` : `
                    <div style="font-size: 0.8rem; color: var(--danger); font-weight: 600;">
                        <i class="fa-solid fa-circle-exclamation"></i> Refund Initiated
                    </div>
                `}
            </div>
        </div>
    `;
}

// ==============================================================================
// Cancellation Action Handlers
// ==============================================================================
function handleCancelBooking(bookingId) {
    if (!confirm(`Are you sure you want to cancel Ticket #BK-${bookingId}? Your seat will be released and a refund processed.`)) {
        return;
    }

    const token = localStorage.getItem('token');
    fetch(`/api/bookings/cancel/${bookingId}`, {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                showToast("Ticket cancelled successfully! Seat has been freed.", "success");
                loadUserBookings(); // Refresh user ticket list
            } else {
                showToast(data.message || "Failed to cancel booking", "error");
            }
        })
        .catch(err => {
            console.error("Cancel error:", err);
            showToast("Network error while cancelling ticket", "error");
        });
}

// ==============================================================================
// Admin Dashboard: Fleet Operations
// ==============================================================================
function loadAdminBuses() {
    const token = localStorage.getItem('token');
    fetch('/api/buses', {
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                const tableBody = document.getElementById('admin-bus-table-body');
                if (!tableBody) return;
                tableBody.innerHTML = '';

                const buses = data.data.content || [];

                if (document.getElementById('stat-total-buses')) {
                    document.getElementById('stat-total-buses').innerText = buses.length;
                    const totalSeats = buses.reduce((acc, b) => acc + b.totalSeats, 0);
                    const availSeats = buses.reduce((acc, b) => acc + b.availableSeats, 0);
                    document.getElementById('stat-total-capacity').innerText = totalSeats;
                    document.getElementById('stat-available-capacity').innerText = availSeats;
                }

                buses.forEach(bus => {
                    const depDate = new Date(bus.departureTime).toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
                    const arrDate = new Date(bus.arrivalTime).toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
                    const occupancyRate = Math.round(((bus.totalSeats - bus.availableSeats) / bus.totalSeats) * 100);

                    tableBody.innerHTML += `
                    <tr>
                        <td>
                            <strong>${bus.busNumber}</strong>
                        </td>
                        <td>
                            ${bus.source} &rarr; ${bus.destination}
                        </td>
                        <td>${depDate}</td>
                        <td>${arrDate}</td>
                        <td>
                            <div style="font-weight: 700;">$${bus.price.toFixed(2)}</div>
                        </td>
                        <td>
                            <div style="font-size: 0.85rem; font-weight: 600;">${bus.availableSeats} / ${bus.totalSeats} seats left</div>
                            <div style="width: 100%; height: 6px; background: #e2e8f0; border-radius: 3px; margin-top: 4px; overflow: hidden;">
                                <div style="width: ${occupancyRate}%; height: 100%; background: ${occupancyRate > 80 ? 'var(--danger)' : 'var(--primary)'};"></div>
                            </div>
                        </td>
                    </tr>
                `;
                });
            }
        });
}

function initAdminForm() {
    const depInput = document.getElementById('add-departure-time');
    const arrInput = document.getElementById('add-arrival-time');
    if (!depInput || !arrInput) return;

    // Helper to format Date to YYYY-MM-DDTHH:mm for datetime-local
    const pad = (n) => String(n).padStart(2, '0');
    const formatLocal = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(8, 0, 0, 0);

    const arrival = new Date(tomorrow);
    arrival.setHours(16, 0, 0, 0);

    depInput.value = formatLocal(tomorrow);
    arrInput.value = formatLocal(arrival);
}

function handleAddBus(event) {
    event.preventDefault();
    const token = localStorage.getItem('token');
    if (!token) {
        showToast('Please sign in as an administrator first.', 'error');
        setTimeout(() => window.location.href = '/admin-login', 1200);
        return;
    }

    const formData = new FormData(event.target);
    const busData = Object.fromEntries(formData.entries());

    // Strict numerical type casting for DTO validation
    busData.price = parseFloat(busData.price);
    busData.totalSeats = parseInt(busData.totalSeats, 10);

    if (isNaN(busData.price) || busData.price <= 0) {
        showToast('Please enter a valid ticket fare.', 'error');
        return;
    }
    if (isNaN(busData.totalSeats) || busData.totalSeats <= 0) {
        showToast('Please enter a valid total seating capacity.', 'error');
        return;
    }

    fetch('/api/buses', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(busData)
    })
    .then(res => {
        if (res.status === 401 || res.status === 403) {
            showToast('Session expired or unauthorized. Please re-login.', 'error');
            setTimeout(() => {
                localStorage.removeItem('token');
                window.location.href = '/admin-login';
            }, 1200);
            throw new Error('Unauthorized');
        }
        return res.json();
    })
    .then(data => {
        if (data.success) {
            showToast('🎉 Bus Service Added to Fleet Successfully!', 'success');
            event.target.reset();
            initAdminForm();
            loadAdminBuses();
        } else {
            let errorMsg = data.message || 'Failed to add bus service';
            if (data.data && typeof data.data === 'object') {
                const validationErrors = Object.values(data.data).join(', ');
                if (validationErrors) errorMsg += ': ' + validationErrors;
            }
            showToast(errorMsg, 'error');
        }
    })
    .catch(err => {
        if (err.message !== 'Unauthorized') {
            console.error("Add bus error:", err);
            showToast('Network error while scheduling bus service.', 'error');
        }
    });
}

// ==============================================================================
// Toast Notifications
// ==============================================================================
function showToast(message, type = 'info') {
    let toast = document.getElementById('toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast';
        toast.className = 'toast';
        document.body.appendChild(toast);
    }
    const icon = type === 'success' ? '<i class="fa-solid fa-circle-check"></i> ' : (type === 'error' ? '<i class="fa-solid fa-triangle-exclamation"></i> ' : '');
    toast.innerHTML = icon + message;
    toast.className = `toast show ${type}`;
    setTimeout(function () {
        toast.className = toast.className.replace('show', '');
    }, 3500);
}
