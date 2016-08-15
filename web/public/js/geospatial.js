$(function () {
    $.geodemo = {};
    $.geodemo.refreshRate = 2000;
    $.geodemo.circleRadius = 500; // meters
    $.geodemo.markers = {};
    $.geodemo.nearestOrders = {};
    $.geodemo.map = initMap();
    $.geodemo.circle = initCircle();
    $.geodemo.selectedMarker = "";
    $.geodemo.info = initInfo();
    refreshMap();
});

function initMap() {
    var map = new GMaps({
      div: '#map',
      lat: 40.7128,
      lng: -74.0059,
      click: function(e) {
        submitOrder(e.latLng.lat(), e.latLng.lng());
      },
    });
    map.setZoom(14);
    return map;
}

function initCircle() {
    return $.geodemo.map.drawCircle({
        lat: 0,
        lng: 0,
        radius: $.geodemo.circleRadius,
        fillColor: "#00ff00",
        fillOpacity: 0.2,
        strokeWeight: 0,
        clickable: false,
        visible: false
    });
}

function initInfo() {
    return new google.maps.InfoWindow({
        content: "",
        fillOpacity: 0.5
    });
}

function refreshMap() {
    var map = $.geodemo.map;
    var route = jsRoutes.controllers.RestEndpoint.allOrders();
    $.getJSON(route.url, function(data) {

        var currentOrders = {};

        $.each(data, function(index, value) { currentOrders[value.id] = value; });

        $.each(currentOrders, function(id, value) {
          if (id in $.geodemo.markers) {
            // redefine marker if it was submitted by user
            var marker = $.geodemo.markers[id];
            if (marker.state == "new") marker.setState("normal");
          } else {
            // create new marker
            createMarker(id, "normal", value.location.y, value.location.x);
          }
        });

        // delete old markers
        $.each($.geodemo.markers, function(id, marker) {
          if (!(id in currentOrders) && marker.state != "new") {
            marker.destroy();
          }
        });

    });
    setTimeout(function() {refreshMap();}, $.geodemo.refreshRate);
}

function createMarker(id, state, lat, lng) {
    var markerData = {
      lat: lat,
      lng: lng,
      animation: google.maps.Animation.DROP
    };
    var marker = $.geodemo.map.addMarker(markerData);
    marker.markerId = id;
    marker.state = "none";

    marker.setState = function(newState) {
        var oldState = marker.state;
        marker.state = newState;
        if (newState == "normal") {
            marker.setIcon("http://maps.google.com/mapfiles/ms/icons/red.png");
        } else if (newState == "new") {
            marker.setTitle("Placing order...");
            marker.setIcon("http://maps.google.com/mapfiles/ms/icons/blue.png");
            $.geodemo.selectedMarker = id;
        } else if (newState == "selected") {
            marker.setIcon("http://maps.google.com/mapfiles/ms/icons/green-dot.png");
        } else if (newState == "near") {
            marker.setIcon("http://maps.google.com/mapfiles/ms/icons/yellow-dot.png");
        } else if (newState == "removed") {
            marker.setTitle("Removing order...");
            marker.setIcon("http://maps.google.com/mapfiles/ms/micons/lightblue.png");
        }

        if (newState != "new") {
            marker.setTitle("Taxi Order (click for details)");
            google.maps.event.clearInstanceListeners(marker);
            google.maps.event.addListener(marker, 'click', function () {showDetails(id);});
            if (oldState == "new" && $.geodemo.selectedMarker == id) showDetails(id);
        }
    }

    marker.destroy = function() {
        marker.setMap(null);
        if ($.geodemo.circle.markerId == marker.markerId) $.geodemo.circle.setVisible(false);
        delete $.geodemo.markers[marker.markerId];
    }

    marker.setState(state);
    $.geodemo.markers[id] = marker;
}

function showDetails(orderId) {
    var map = $.geodemo.map;

    $.each($.geodemo.markers, function(id, marker) {
      if (id == orderId) marker.setState("selected");
      else if (marker.state == "selected" || marker.state == "near") marker.setState("normal");
    });

    var route = jsRoutes.controllers.RestEndpoint.orderById(orderId);
    $.getJSON(route.url, function(data) {
        showDetailsContent($.geodemo.markers[data.id], data);

        $.geodemo.circle.setCenter({lat: data.location.y, lng: data.location.x});
        $.geodemo.circle.markerId = data.id;
        $.geodemo.circle.setVisible(true);

        $.each(data.nearOrderIds, function(index, value) {
          if (value in $.geodemo.markers) $.geodemo.markers[value].setState("near");
        });
    });
}

function submitOrder(latitude, longitude) {
    var route = jsRoutes.controllers.RestEndpoint.createOrder();
    $.ajax({
        url: route.url,
        type: route.type,
        data: JSON.stringify({latitude: latitude, longitude: longitude}),
        contentType: "application/json",
        success: function(newId) {createMarker(newId, "new", latitude, longitude);}
    });
}

function submitPickup(id) {
    var route = jsRoutes.controllers.RestEndpoint.removeOrderById(id);
    hideDetailsContent();

    $.geodemo.circle.setVisible(false);
    $.each($.geodemo.markers, function(id, marker) {
      if (marker.state == "selected" || marker.state == "near") marker.setState("normal");
    });

    $.ajax({
        url: route.url,
        type: route.type,
        success: function(data) {$.geodemo.markers[id].setState("removed");}
    });
}

function showDetailsContent(marker, data) {
    var info = $.geodemo.info;
    info.setContent($("#infoTemplate").html());
    info.open($.geodemo.map, marker);

    $(".orderId").html(data.id);
    $(".orderTime").html(new Date(data.time).toLocaleString());
    $(".orderLatitude").html(data.location.y);
    $(".orderLongitude").html(data.location.x);
    $(".orderPriceSurge").html(surgeText(data.priceFactor, data.nearOrderIds.length));

    $(".orderActionPickup").unbind('click').click(function() {submitPickup(data.id);});
    $.geodemo.selectedMarker = marker.markerId;
}
function hideDetailsContent() {
    $.geodemo.info.close();
}
function surgeText(price, count) {
    var countText = "(affected by <code>" + count + "</code> previous order" + (count == 1 ? "" : "s") + ")";
    var surgeText = "<code>" + Number(price).toFixed(1) + "x</code>";
    return surgeText + " " + countText;
}