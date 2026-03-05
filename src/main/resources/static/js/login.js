$(function () {
    var params = new URLSearchParams(window.location.search);

    if (params.has("locked")) {
        var minutes = params.get("minutes") || "15";
        $("#lockMinutes").text(minutes);
        $("#lockedMessage").show();
    } else if (params.has("error")) {
        $("#errorMessage").show();
    }

    if (params.has("expired")) {
        $("#sessionExpiredMessage").show();
    }

    if (params.has("logout")) {
        $("#logoutMessage").show();
    }
});
