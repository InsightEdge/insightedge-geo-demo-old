$(function () {
    hideDetailsLoading();

    $.geodemo = {};
    $.geodemo.markers = {};
    $.geodemo.nearestOrders = {};
    $.geodemo.map = initMap();
    refreshMap();

    $("#orderActionDeselect").click(function(){hideDetails();});
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
    map.setZoom(12);
    return map;
}

function refreshMap() {
    var map = $.geodemo.map;
    var route = jsRoutes.controllers.RestEndpoint.allOrders();
    $.getJSON(route.url, function(data) {

        var currentOrders = {};

        $.each(data, function(index, value) { currentOrders[value.id] = value; });

        $.each(currentOrders, function(id, value) {
          if (id in $.geodemo.markers) {
            // skip the marker, it already exists
          } else if("newOrderId" in $.geodemo && id == $.geodemo.newOrderId) {
            // redefine marker if it was submitted by user
            var marker = $.geodemo.newOrderMarker;
            delete $.geodemo.newOrderMarker;
            delete $.geodemo.newOrderId;

            marker.setTitle("Taxi Order (click for details)");
            google.maps.event.addListener(marker, 'click', function () {
               showDetails(id);
            });

            $.geodemo.markers[id] = marker;
            showDetails(id);
          } else {
            // create new marker
            var markerData = {
              lat: value.location.y,
              lng: value.location.x,
              title: "Taxi Order (click for details)",
              icon: "http://maps.google.com/mapfiles/ms/icons/red.png",
              animation: google.maps.Animation.DROP,
              click: function(e) {
                showDetails(value.id);
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

function showDetails(orderId) {
    var map = $.geodemo.map;

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
        $("#orderPriceSurge").html(surgeText(data.priceFactor, data.nearOrderIds.length));

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

        $("#orderActionPickup").removeAttr("disabled").unbind('click').click(function() {
            submitPickup(data.id);
            hideDetails();
        });
    });
}

function hideDetails() {
    $("#orderId").html("Nothing to show");
    $("#orderTime").html("Nothing to show");
    $("#orderLatitude").html("Nothing to show");
    $("#orderLongitude").html("Nothing to show");
    $("#orderPriceSurge").html("Nothing to show");
    $("#orderActionPickup").attr("disabled", "disabled");
    if ("circle" in $.geodemo) {
        $.geodemo.circle.setMap(null);
        delete $.geodemo.circle;
    }
    delete $.geodemo.selectedId;
    $.each($.geodemo.markers, function(id, value) {
      value.setIcon("http://maps.google.com/mapfiles/ms/icons/red.png")
    });
}

function submitOrder(latitude, longitude) {
    var map = $.geodemo.map;
    var route = jsRoutes.controllers.RestEndpoint.createOrder();

    showDetailsLoading();
    $("#orderId").html("Loading...");
    $("#orderTime").html("Loading...");
    $("#orderLatitude").html("Loading...");
    $("#orderLongitude").html("Loading...");
    $("#orderPriceSurge").html("Loading...");

    $.ajax({
        url: route.url,
        type: route.type,
        data: JSON.stringify({latitude: latitude, longitude: longitude}),
        contentType: "application/json",
        success: function(newId) {$.geodemo.newOrderId = newId;},
        error: function() {hideDetailsLoading();}
    });
    var markerData = {
      lat: latitude,
      lng: longitude,
      title: "Placing order...",
      icon: "http://maps.google.com/mapfiles/ms/icons/blue.png",
      animation: google.maps.Animation.DROP
    };
    if ("newOrderMarker" in $.geodemo) {
      $.geodemo.newOrderMarker.setMap(null);
    }
    $.geodemo.newOrderMarker = map.addMarker(markerData);
}

function submitPickup(id) {
    var route = jsRoutes.controllers.RestEndpoint.removeOrderById(id);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function(data) {
            var marker = $.geodemo.markers[id];
            marker.setMap(null);
            delete $.geodemo.markers[id];
        }
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