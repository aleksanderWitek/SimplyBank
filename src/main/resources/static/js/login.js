$(function () {
    var params = new URLSearchParams(window.location.search);

    if (params.has("error")) {
        $("#errorMessage").show();
    }

    if (params.has("logout")) {
        $("#logoutMessage").show();
    }
});
