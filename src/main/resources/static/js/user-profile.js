/**
 * SimplyBank — User Profile Page Controller
 *
 * Loads user profile data and renders role-appropriate fields.
 * - If URL has ?id=X, fetches the user account to determine role,
 *   then calls the appropriate profile endpoint:
 *     CLIENT   → GET /api/client/profile?userAccountId=X
 *     EMPLOYEE → GET /api/employee/profile?userAccountId=X
 *     ADMIN    → GET /api/employee/admin-profile?userAccountId=X
 * - Otherwise, calls GET /api/auth/me
 *
 * Shared utilities (ajax, formatDate, escapeHtml, etc.) are in common.js.
 */

// ============================================================
// CONFIGURATION
// ============================================================

var ProfileAPI = {
    USER_ACCOUNT:    "/api/user_account/{id}",
    CLIENT_PROFILE:  "/api/client/profile?userAccountId={id}",
    EMPLOYEE_PROFILE: "/api/employee/profile?userAccountId={id}",
    ADMIN_PROFILE:   "/api/employee/admin-profile?userAccountId={id}",
    AUTH_ME:         "/api/auth/me"
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
        loadProfileByUserAccountId(userId);
    } else {
        loadProfile(ProfileAPI.AUTH_ME);
    }
}

// ============================================================
// DATA LOADING
// ============================================================

function loadProfileByUserAccountId(userAccountId) {
    showLoading(true);

    var userAccountUrl = ProfileAPI.USER_ACCOUNT.replace("{id}", userAccountId);
    ajax(userAccountUrl, "GET")
        .done(function (userAccount) {
            var role = (userAccount.role || "").toUpperCase();
            var profileUrl;

            if (role === "CLIENT") {
                profileUrl = ProfileAPI.CLIENT_PROFILE.replace("{id}", userAccountId);
            } else if (role === "EMPLOYEE") {
                profileUrl = ProfileAPI.EMPLOYEE_PROFILE.replace("{id}", userAccountId);
            } else if (role === "ADMIN") {
                profileUrl = ProfileAPI.ADMIN_PROFILE.replace("{id}", userAccountId);
            } else {
                showLoading(false);
                showError("Unknown role: " + role);
                return;
            }

            loadProfile(profileUrl);
        })
        .fail(function (jqxhr) {
            showLoading(false);
            initProfileLinks();
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Failed to load user account";
            showError(msg);
            notify(msg, "error");
        });
}

function loadProfile(url) {
    showLoading(true);

    ajax(url, "GET")
        .done(function (profile) {
            renderProfile(profile);
            renderUserHeader(profile);
            initProfileLinks(profile.userAccountId || profile.id);
            showLoading(false);
            $("#profileContent").show();
        })
        .fail(function (jqxhr) {
            showLoading(false);
            initProfileLinks();
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
    appendField($fields, "Account Created", formatDate(profile.accountCreateDate || profile.createDate));
}

function renderPersonalInfo(profile) {
    var $fields = $("#personalInfoFields");
    $fields.empty();

    if (profile.role === "CLIENT") {
        appendField($fields, "First Name",             escapeHtml(profile.firstName || "N/A"));
        appendField($fields, "Last Name",              escapeHtml(profile.lastName || "N/A"));
        appendField($fields, "City",                   escapeHtml(profile.city || "N/A"));
        appendField($fields, "Street",                 escapeHtml(profile.street || "N/A"));
        appendField($fields, "House Number",           escapeHtml(profile.houseNumber || "N/A"));
        appendField($fields, "Identification Number",  escapeHtml(profile.identificationNumber || "N/A"));
    } else if (profile.role === "EMPLOYEE") {
        appendField($fields, "First Name", escapeHtml(profile.firstName || "N/A"));
        appendField($fields, "Last Name",  escapeHtml(profile.lastName || "N/A"));
    } else if (profile.role === "ADMIN") {
        appendField($fields, "First Name", escapeHtml(profile.firstName || "N/A"));
        appendField($fields, "Last Name",  escapeHtml(profile.lastName || "N/A"));
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
