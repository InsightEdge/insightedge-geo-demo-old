$(function () {
    var map = initMap();
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