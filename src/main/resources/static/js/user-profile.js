/**
 * SimplyBank — User Profile Page Controller
 *
 * Loads user profile data and renders role-appropriate fields.
 * - If URL has ?id=X, calls GET /api/user_account/{id}/profile
 * - Otherwise, calls GET /api/auth/me
 *
 * Expected API response shape:
 * {
 *   "userAccountId": 1,
 *   "login": "jdoe",
 *   "role": "CLIENT",
 *   "createDate": "2024-01-15T10:30:00",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "city": "Dublin",           // CLIENT only
 *   "street": "O'Connell",      // CLIENT only
 *   "houseNumber": "42",        // CLIENT only
 *   "identificationNumber": "IE123456789"  // CLIENT only
 * }
 *
 * Shared utilities (ajax, formatDate, escapeHtml, etc.) are in common.js.
 */

// ============================================================
// CONFIGURATION
// ============================================================

var ProfileAPI = {
    PROFILE: "/api/user_account/{id}/profile",
    AUTH_ME: "/api/auth/me"
};

// ============================================================
// INITIALIZATION
// ============================================================

function getQueryParam(name) {
    var params = new URLSearchParams(window.location.search);
    return params.get(name);
}

function init() {
    var userId = getQueryParam("id");
    if (userId) {
        loadProfile(ProfileAPI.PROFILE.replace("{id}", userId));
    } else {
        loadProfile(ProfileAPI.AUTH_ME);
    }
}

// ============================================================
// DATA LOADING
// ============================================================

function loadProfile(url) {
    showLoading(true);

    ajax(url, "GET")
        .done(function (profile) {
            renderProfile(profile);
            renderUserHeader(profile);
            showLoading(false);
            $("#profileContent").show();
        })
        .fail(function (jqxhr) {
            showLoading(false);
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Failed to load profile";
            showError(msg);
            notify(msg, "error");
        });
}

// ============================================================
// RENDERING
// ============================================================

function renderProfile(profile) {
    renderProfileHeader(profile);
    renderAccountInfo(profile);
    renderPersonalInfo(profile);
}

function renderProfileHeader(profile) {
    var firstName = profile.firstName || "";
    var lastName  = profile.lastName || "";
    var initials  = ((firstName.charAt(0) || "") + (lastName.charAt(0) || "")).toUpperCase();
    var fullName  = (firstName + " " + lastName).trim() || "Unknown User";
    var roleBadge = getRoleBadge(profile.role);

    var html =
        '<div class="profile-avatar">' + escapeHtml(initials || "?") + '</div>' +
        '<div class="profile-header-info">' +
            '<h2 class="profile-name">' + escapeHtml(fullName) + '</h2>' +
            roleBadge +
        '</div>';

    $("#profileHeaderCard").html(html);
}

function renderAccountInfo(profile) {
    var $fields = $("#accountInfoFields");
    $fields.empty();

    appendField($fields, "Login",           escapeHtml(profile.login || "N/A"));
    appendField($fields, "Role",            escapeHtml(capitalize(profile.role || "N/A")));
    appendField($fields, "Account Created", formatDate(profile.createDate));
}

function renderPersonalInfo(profile) {
    var $fields = $("#personalInfoFields");
    $fields.empty();

    appendField($fields, "First Name", escapeHtml(profile.firstName || "N/A"));
    appendField($fields, "Last Name",  escapeHtml(profile.lastName || "N/A"));

    if (profile.role === "CLIENT") {
        appendField($fields, "City",                  escapeHtml(profile.city || "N/A"));
        appendField($fields, "Street",                escapeHtml(profile.street || "N/A"));
        appendField($fields, "House Number",          escapeHtml(profile.houseNumber || "N/A"));
        appendField($fields, "Identification Number", escapeHtml(profile.identificationNumber || "N/A"));
    }
}

function appendField($container, label, value) {
    var html =
        '<div class="profile-field">' +
            '<span class="field-label">' + escapeHtml(label) + '</span>' +
            '<span class="field-value">' + value + '</span>' +
        '</div>';
    $container.append(html);
}

function getRoleBadge(role) {
    var r = (role || "").toUpperCase();
    var badgeClass = "badge-client";
    if (r === "ADMIN")    badgeClass = "badge-admin";
    if (r === "EMPLOYEE") badgeClass = "badge-employee";
    return '<span class="role-badge ' + badgeClass + '">' + escapeHtml(capitalize(r || "Unknown")) + '</span>';
}

// ============================================================
// STATE MANAGEMENT
// ============================================================

function showLoading(show) {
    if (show) {
        $("#loadingState").show();
        $("#profileContent").hide();
        $("#errorState").hide();
    } else {
        $("#loadingState").hide();
    }
}

function showError(msg) {
    $("#loadingState").hide();
    $("#profileContent").hide();
    $("#errorState").show();
    if (msg) {
        $("#errorState .empty-subtitle").text(msg);
    }
}

// ============================================================
// ENTRY POINT
// ============================================================

$(document).ready(function () {
    init();
});
