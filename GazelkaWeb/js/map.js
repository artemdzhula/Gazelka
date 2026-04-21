let map;
let marker = null;
let geocoder;
let activeInputId = null;

export function initGoogleMap() {
  if (map) return;

  geocoder = new google.maps.Geocoder();

  map = new google.maps.Map(document.getElementById("map-canvas"), {
    center: { lat: 52.2297, lng: 21.0122 }, // Warsaw
    zoom: 12,
    disableDefaultUI: true,
  });

  map.addListener("click", (e) => {
    const latLng = e.latLng;

    if (marker) marker.setMap(null);

    marker = new google.maps.Marker({
      position: latLng,
      map,
    });

    reverseGeocode(latLng);
  });
}

function reverseGeocode(latLng) {
  geocoder.geocode({ location: latLng }, (results, status) => {
    if (status === "OK" && results[0]) {
      const address = results[0].formatted_address;
      if (activeInputId) {
        const input = document.getElementById(activeInputId);
        if (input) input.value = address;
      }
    } else {
      console.error("Geocoder failed:", status);
    }
  });
}

export function openMapOverlay(inputId) {
  activeInputId = inputId;
  const overlay = document.getElementById("map-overlay");
  overlay.style.display = "block";

  setTimeout(() => {
    initGoogleMap();
    google.maps.event.trigger(map, "resize");
  }, 100);
}

export function closeMapOverlay() {
  document.getElementById("map-overlay").style.display = "none";

  if (marker) {
    marker.setMap(null);
    marker = null;
  }

  activeInputId = null;
}

export function initMapButtons() {
  const confirmBtn = document.getElementById("confirm-location");
  const closeBtn = document.getElementById("close-map");

  if (confirmBtn) confirmBtn.addEventListener("click", closeMapOverlay);
  if (closeBtn) closeBtn.addEventListener("click", closeMapOverlay);
}