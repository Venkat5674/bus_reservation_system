// ==============================================================================
// Bus Search, Interactive Coach Seat Selection & Route Map Integration
// ==============================================================================

let currentSelectedBus = null;
let currentSelectedSeat = null;
let leafletMap = null;

// City Coordinate Lookup for Interactive Map
const CITY_COORDINATES = {
    "hyderabad": [17.3850, 78.4867],
    "bangalore": [12.9716, 77.5946],
    "bengaluru": [12.9716, 77.5946],
    "mumbai": [19.0760, 72.8777],
    "pune": [18.5204, 73.8567],
    "chennai": [13.0827, 80.2707],
    "delhi": [28.6139, 77.2090],
    "jaipur": [26.9124, 75.7873],
    "goa": [15.2993, 74.1240],
    "vijayawada": [16.5062, 80.6480],
    "visakhapatnam": [17.6868, 83.2185],
    "vizag": [17.6868, 83.2185],
    "tirupati": [13.6288, 79.4192],
    "coimbatore": [11.0168, 76.9558],
    "mysore": [12.2958, 76.6394],
    "new york": [40.7128, -74.0060],
    "boston": [42.3601, -71.0589]
};

function swapCities() {
    const srcInput = document.getElementById('source');
    const destInput = document.getElementById('destination');
    const temp = srcInput.value;
    srcInput.value = destInput.value;
    destInput.value = temp;
}

function fillRoute(source, dest) {
    document.getElementById('source').value = source;
    document.getElementById('destination').value = dest;
    const searchForm = document.getElementById('search-form');
    if (searchForm) {
        handleSearch(new Event('submit'));
    }
}

function handleSearch(event) {
    if (event) event.preventDefault();
    const source = document.getElementById('source').value.trim();
    const destination = document.getElementById('destination').value.trim();
    const container = document.getElementById('search-results');
    const token = localStorage.getItem('token');

    container.innerHTML = `
        <div style="text-align: center; padding: 3rem;">
            <i class="fa-solid fa-spinner fa-spin fa-2x" style="color: var(--primary);"></i>
            <p style="margin-top: 1rem; color: var(--text-muted);">Finding luxury and express buses...</p>
        </div>
    `;

    fetch(`/api/buses/search?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}`)
        .then(res => res.json())
        .then(data => {
            container.innerHTML = '';
            if (data.success && data.data && data.data.length > 0) {
                document.getElementById('results-count').innerText = `${data.data.length} Buses Found`;
                data.data.forEach(bus => {
                    container.innerHTML += renderBusCard(bus);
                });
            } else {
                document.getElementById('results-count').innerText = `0 Buses Found`;
                container.innerHTML = `
                    <div style="text-align: center; background: white; padding: 4rem 2rem; border-radius: var(--radius-lg); border: 1px solid var(--border-color);">
                        <i class="fa-solid fa-bus-simple fa-3x" style="color: #cbd5e1; margin-bottom: 1rem;"></i>
                        <h3>No direct buses found for ${source} &rarr; ${destination}</h3>
                        <p style="color: var(--text-muted); margin-top: 0.5rem;">Try searching for popular routes like Hyderabad to Bangalore or Mumbai to Pune.</p>
                        <a href="/bus-search" class="btn btn-secondary" style="margin-top: 1.5rem;">View All Available Routes</a>
                    </div>
                `;
            }
        })
        .catch(err => {
            console.error("Search error:", err);
            container.innerHTML = `<p style="color: var(--danger); text-align: center;">Unable to load buses. Please try again.</p>`;
        });
}

function renderBusCard(bus) {
    const depDate = new Date(bus.departureTime);
    const arrDate = new Date(bus.arrivalTime);

    // Calculate duration
    const diffMs = arrDate - depDate;
    const diffHrs = Math.floor(diffMs / 3600000);
    const diffMins = Math.round((diffMs % 3600000) / 60000);
    const durationStr = diffHrs > 0 ? `${diffHrs}h ${diffMins}m` : `${diffMins}m`;

    const depTimeFormatted = depDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const arrTimeFormatted = arrDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    return `
        <div class="bus-card">
            <div class="bus-card-header">
                <div class="bus-operator-info">
                    <h3><i class="fa-solid fa-bus" style="color: var(--primary);"></i> ${bus.busNumber}</h3>
                    <div class="bus-type-badge">Bharat Benz AC Multi-Axle Sleeper (2+2)</div>
                    <div class="bus-rating">
                        <i class="fa-solid fa-star"></i> 4.8 <span>(520)</span>
                    </div>
                </div>

                <div class="journey-timeline">
                    <div class="timeline-point">
                        <div class="point-time">${depTimeFormatted}</div>
                        <div class="point-city">${bus.source}</div>
                    </div>
                    <div class="timeline-duration">
                        <span class="duration-text">${durationStr}</span>
                        <div class="duration-line"></div>
                        <span style="font-size: 0.7rem; color: var(--success);"><i class="fa-solid fa-wifi"></i> On Time</span>
                    </div>
                    <div class="timeline-point">
                        <div class="point-time">${arrTimeFormatted}</div>
                        <div class="point-city">${bus.destination}</div>
                    </div>
                </div>

                <div class="bus-pricing">
                    <div style="font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase;">Starting from</div>
                    <div class="price-val">$${bus.price.toFixed(2)}</div>
                    <div class="seats-avail">${bus.availableSeats} Seats Left</div>
                </div>

                <div class="bus-actions">
                    <button onclick="openSeatModal(${JSON.stringify(bus).replace(/"/g, '&quot;')})" class="btn btn-primary">
                        <i class="fa-solid fa-couch"></i> Select Seats
                    </button>
                    <button onclick="openRouteMap('${bus.source}', '${bus.destination}')" class="btn btn-secondary btn-sm">
                        <i class="fa-solid fa-map-location-dot"></i> View Route
                    </button>
                </div>
            </div>

            <div class="bus-card-footer">
                <div class="amenities-list">
                    <div class="amenity-item"><i class="fa-solid fa-snowflake"></i> AC</div>
                    <div class="amenity-item"><i class="fa-solid fa-wifi"></i> Free Wi-Fi</div>
                    <div class="amenity-item"><i class="fa-solid fa-plug"></i> Charging Port</div>
                    <div class="amenity-item"><i class="fa-solid fa-bottle-water"></i> Mineral Water</div>
                    <div class="amenity-item"><i class="fa-solid fa-location-crosshairs"></i> Live GPS</div>
                </div>
                <div style="color: var(--primary); font-weight: 600; cursor: pointer;" onclick="openRouteMap('${bus.source}', '${bus.destination}')">
                    Route & Stops <i class="fa-solid fa-chevron-right" style="font-size: 0.8rem;"></i>
                </div>
            </div>
        </div>
    `;
}

// ==============================================================================
// Interactive Seat Selection Modal
// ==============================================================================
function openSeatModal(bus) {
    const token = localStorage.getItem('token');
    if (!token) {
        showToast("Please login to select seats and book tickets.", "error");
        setTimeout(() => { window.location.href = '/login'; }, 1500);
        return;
    }

    currentSelectedBus = bus;
    currentSelectedSeat = null;

    document.getElementById('modal-bus-title').innerText = `${bus.busNumber} (${bus.source} \u2192 ${bus.destination})`;
    document.getElementById('summary-bus-no').innerText = bus.busNumber;
    document.getElementById('summary-base-fare').innerText = `$${bus.price.toFixed(2)}`;
    document.getElementById('summary-selected-seat').innerText = "None";
    document.getElementById('summary-total-fare').innerText = "$0.00";
    document.getElementById('confirm-booking-btn').disabled = true;

    const seatGrid = document.getElementById('seat-grid');
    seatGrid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 2rem;"><i class="fa-solid fa-spinner fa-spin"></i> Loading real-time seat availability...</div>`;

    document.getElementById('seat-modal-backdrop').classList.add('active');

    // Fetch live booked seats from backend
    fetch(`/api/bookings/bus/${bus.id}/booked-seats`, {
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(res => res.json())
        .then(data => {
            const bookedSeats = (data.success && Array.isArray(data.data)) ? data.data : [];
            buildSeatCoachLayout(bus.totalSeats, bookedSeats);
        })
        .catch(() => {
            buildSeatCoachLayout(bus.totalSeats, []);
        });
}

function closeSeatModal() {
    document.getElementById('seat-modal-backdrop').classList.remove('active');
    currentSelectedSeat = null;
}

function buildSeatCoachLayout(totalSeats, bookedSeats) {
    const seatGrid = document.getElementById('seat-grid');
    seatGrid.innerHTML = '';

    const total = totalSeats || 40;
    const rows = Math.ceil(total / 4);

    let seatNo = 1;
    for (let r = 1; r <= rows; r++) {
        // Seat Left 1
        seatGrid.appendChild(createSeatElement(seatNo++, bookedSeats));
        // Seat Left 2
        seatGrid.appendChild(createSeatElement(seatNo++, bookedSeats));

        // Aisle
        const aisle = document.createElement('div');
        aisle.className = 'seat-aisle';
        aisle.innerText = `${r}`;
        seatGrid.appendChild(aisle);

        // Seat Right 1
        seatGrid.appendChild(createSeatElement(seatNo++, bookedSeats));
        // Seat Right 2
        seatGrid.appendChild(createSeatElement(seatNo++, bookedSeats));
    }
}

function createSeatElement(num, bookedSeats) {
    const seat = document.createElement('div');
    seat.className = 'seat-item';
    seat.id = `seat-${num}`;

    const isBooked = bookedSeats.includes(num);

    if (isBooked) {
        seat.classList.add('booked');
        seat.innerHTML = `<i class="fa-solid fa-lock"></i><span>${num}</span>`;
        seat.title = `Seat ${num} is already booked`;
    } else {
        seat.innerHTML = `<i class="fa-solid fa-couch"></i><span>${num}</span>`;
        seat.title = `Seat ${num} - Click to select`;
        seat.onclick = () => selectSeat(num);
    }

    return seat;
}

function selectSeat(num) {
    if (currentSelectedSeat) {
        const prevSeat = document.getElementById(`seat-${currentSelectedSeat}`);
        if (prevSeat) prevSeat.classList.remove('selected');
    }

    currentSelectedSeat = num;
    const newSeat = document.getElementById(`seat-${num}`);
    if (newSeat) newSeat.classList.add('selected');

    document.getElementById('summary-selected-seat').innerText = `Seat #${num}`;
    const fare = currentSelectedBus.price;
    const tax = fare * 0.05; // 5% GST/Taxes
    const total = fare + tax;

    document.getElementById('summary-tax').innerText = `$${tax.toFixed(2)}`;
    document.getElementById('summary-total-fare').innerText = `$${total.toFixed(2)}`;
    document.getElementById('confirm-booking-btn').disabled = false;
}

function confirmBooking() {
    if (!currentSelectedBus || !currentSelectedSeat) {
        showToast("Please select a seat first.", "error");
        return;
    }

    const token = localStorage.getItem('token');
    const userId = getUserIdFromToken(token);

    if (!userId) {
        showToast("Please log in to complete your seat reservation.", "info");
        sessionStorage.setItem('redirect_after_login', window.location.href);
        setTimeout(() => {
            window.location.href = '/login';
        }, 1000);
        return;
    }

    const confirmBtn = document.getElementById('confirm-booking-btn');
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Securing Seat...`;

    fetch(`/api/bookings?userId=${userId}&busId=${currentSelectedBus.id}&seatNumber=${currentSelectedSeat}`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                closeSeatModal();
                showToast("🎉 Booking Confirmed! Redirecting to ticket...", "success");
                setTimeout(() => {
                    window.location.href = '/user-dashboard';
                }, 1200);
            } else {
                showToast(data.message || "Failed to book seat. Please try another seat.", "error");
                confirmBtn.disabled = false;
                confirmBtn.innerHTML = `<i class="fa-solid fa-check-circle"></i> Confirm & Book Seat`;
            }
        })
        .catch(err => {
            showToast("Network error. Please try again.", "error");
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = `<i class="fa-solid fa-check-circle"></i> Confirm & Book Seat`;
        });
}

// ==============================================================================
// Interactive Map Modal (Leaflet.js)
// ==============================================================================
function openRouteMap(sourceCity, destCity) {
    const modal = document.getElementById('map-modal-backdrop');
    modal.classList.add('active');
    document.getElementById('map-modal-title').innerText = `Route: ${sourceCity} \u2192 ${destCity}`;

    setTimeout(() => {
        initLeafletMap(sourceCity, destCity);
    }, 200);
}

function closeRouteMap() {
    document.getElementById('map-modal-backdrop').classList.remove('active');
}

function initLeafletMap(source, dest) {
    const mapContainer = document.getElementById('route-map-container');
    if (leafletMap) {
        leafletMap.remove();
        leafletMap = null;
    }

    const srcCoord = CITY_COORDINATES[source.toLowerCase()] || [17.3850, 78.4867];
    const destCoord = CITY_COORDINATES[dest.toLowerCase()] || [12.9716, 77.5946];

    leafletMap = L.map('route-map-container').setView(srcCoord, 6);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(leafletMap);

    const srcMarker = L.marker(srcCoord).addTo(leafletMap)
        .bindPopup(`<b>Boarding Point</b><br>${source}`)
        .openPopup();

    const destMarker = L.marker(destCoord).addTo(leafletMap)
        .bindPopup(`<b>Destination Point</b><br>${dest}`);

    const polyline = L.polyline([srcCoord, destCoord], {
        color: '#d84e55',
        weight: 5,
        opacity: 0.8,
        dashArray: '10, 10'
    }).addTo(leafletMap);

    leafletMap.fitBounds(polyline.getBounds(), { padding: [50, 50] });
}

// Helper: Decode user ID from JWT token with expiration check
function getUserIdFromToken(token) {
    try {
        if (!token) return null;
        const payload = JSON.parse(atob(token.split('.')[1]));
        const currentTime = Math.floor(Date.now() / 1000);
        if (payload.exp && payload.exp < currentTime) {
            console.warn("Session token expired");
            localStorage.removeItem('token');
            localStorage.removeItem('role');
            return null;
        }
        return payload.userId || null;
    } catch (e) {
        return null;
    }
}
