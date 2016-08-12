$(function () {
    $.geodemo = {};
    $.geodemo.markers = {};
    $.geodemo.nearestOrders = {};

    hideDetailsLoading();
    var map = initMap();
    refreshMap(map);
});

function initMap() {
    var map = new GMaps({
      div: '#map',
      lat: 40.7128,
      lng: -74.0059
    });
    map.setZoom(13);
    return map;
}

function refreshMap(map) {
    var route = jsRoutes.controllers.RestEndpoint.allOrders();
    $.getJSON(route.url, function(data) {

        var currentOrders = {};

        $.each(data, function(index, value) { currentOrders[value.id] = value; });

        $.each(currentOrders, function(id, value) {
          if (id in $.geodemo.markers) {
            // skip the marker, it already exists
          } else {
            // create new marker
            var markerData = {
              lat: value.location.y,
              lng: value.location.x,
              title: "Taxi Order (click for details)",
              icon: "http://maps.google.com/mapfiles/ms/icons/red.png",
              animation: google.maps.Animation.DROP,
              click: function(e) {
                showDetails(value.id, map);
              }
            };
            $.geodemo.markers[id] = map.addMarker(markerData);
          }
        });

        // delete old markers
        $.each($.geodemo.markers, function(id, value) {
          if (!(id in currentOrders)) {
            value.setMap(null);
            delete $.geodemo.markers[id];
          }
        });

    });
    setTimeout(function() {refreshMap(map);}, 2000);
}

function showDetails(orderId, map) {
    showDetailsLoading();
    $.geodemo.selectedId = orderId;
    $.each($.geodemo.markers, function(id, value) {
      if (id == orderId) value.setIcon("http://maps.google.com/mapfiles/ms/icons/green-dot.png")
      else value.setIcon("http://maps.google.com/mapfiles/ms/icons/red.png");
    });
    $.geodemo.markers[orderId]

    var route = jsRoutes.controllers.RestEndpoint.orderById(orderId);
    $.getJSON(route.url, function(data) {
        hideDetailsLoading();
        $("#orderId").html(data.id);
        $("#orderTime").html(new Date(data.time).toLocaleString());
        $("#orderLatitude").html(data.location.y);
        $("#orderLongitude").html(data.location.x);
        $("#orderPriceSurge").html(surgeText(1.0, data.nearOrderIds.length));

        var newCircle = map.drawCircle({
            lat: data.location.y,
            lng: data.location.x,
            radius: 3000,
            fillColor: "#00ff00",
            fillOpacity: 0.2,
            strokeWeight: 0,
            clickable: false
        });
        if ("circle" in $.geodemo) {
            $.geodemo.circle.setMap(null);
        }
        $.geodemo.circle = newCircle;

        $.each(data.nearOrderIds, function(index, value) {
          if (value in $.geodemo.markers) {
            $.geodemo.markers[value].setIcon("http://maps.google.com/mapfiles/ms/icons/yellow-dot.png")
          }
        });

    });
}

function showDetailsLoading() {
    $("#orderLoadingBar").show();
}
function hideDetailsLoading() {
    $("#orderLoadingBar").hide();
}
function surgeText(price, count) {
    var countText = "(affected by <span class='text-primary'>" + count + "</span> previous order" + (count == 1 ? "" : "s") + ")";
    var surgeText = "<span class='text-success'>" + Number(price).toFixed(1) + "x</span>";
    return surgeText + " " + countText;
}