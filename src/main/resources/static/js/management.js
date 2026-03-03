/**
 * SimplyBank — Management Page Controller
 *
 * Provides forms to add new Client and Employee records.
 * POST /api/client   — create client
 * POST /api/employee — create employee
 *
 * Shared utilities (ajax, escapeHtml, notify, etc.) are in common.js.
 */

// ============================================================
// CONFIGURATION
// ============================================================

var ManagementAPI = {
    CLIENT:   "/api/client",
    EMPLOYEE: "/api/employee",
    AUTH_ME:  "/api/auth/me"
};

// ============================================================
// CLIENT FORM
// ============================================================

function validateClientForm() {
    var valid = true;

    $(".field-error", "#clientForm").text("");
    $(".field-input", "#clientForm").removeClass("invalid");

    var fields = [
        { id: "clientFirstName",  label: "First name" },
        { id: "clientLastName",   label: "Last name" },
        { id: "clientCity",       label: "City" },
        { id: "clientStreet",     label: "Street" },
        { id: "clientHouseNumber", label: "House number" },
        { id: "clientIdNumber",   label: "Identification number" }
    ];

    fields.forEach(function (f) {
        var val = $.trim($("#" + f.id).val());
        if (!val) {
            $("#" + f.id + "Error").text(f.label + " is required");
            $("#" + f.id).addClass("invalid");
            valid = false;
        }
    });

    return valid;
}

function submitClient() {
    if (!validateClientForm()) return;

    var data = {
        firstName:            $.trim($("#clientFirstName").val()),
        lastName:             $.trim($("#clientLastName").val()),
        city:                 $.trim($("#clientCity").val()),
        street:               $.trim($("#clientStreet").val()),
        houseNumber:          $.trim($("#clientHouseNumber").val()),
        identificationNumber: $.trim($("#clientIdNumber").val())
    };

    $("#btnAddClient").prop("disabled", true).html("Adding\u2026");

    ajax(ManagementAPI.CLIENT, "POST", data)
        .done(function () {
            notify("Client added successfully", "success");
            $("#clientForm")[0].reset();
            $(".field-input", "#clientForm").removeClass("invalid");
            $(".field-error", "#clientForm").text("");
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Failed to add client";
            notify(msg, "error");
        })
        .always(function () {
            $("#btnAddClient").prop("disabled", false).html(
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                    '<line x1="12" y1="5" x2="12" y2="19"/>' +
                    '<line x1="5" y1="12" x2="19" y2="12"/>' +
                '</svg> Add Client'
            );
        });
}

// ============================================================
// EMPLOYEE FORM
// ============================================================

function validateEmployeeForm() {
    var valid = true;

    $(".field-error", "#employeeForm").text("");
    $(".field-input", "#employeeForm").removeClass("invalid");

    var fields = [
        { id: "empFirstName", label: "First name" },
        { id: "empLastName",  label: "Last name" }
    ];

    fields.forEach(function (f) {
        var val = $.trim($("#" + f.id).val());
        if (!val) {
            $("#" + f.id + "Error").text(f.label + " is required");
            $("#" + f.id).addClass("invalid");
            valid = false;
        }
    });

    return valid;
}

function submitEmployee() {
    if (!validateEmployeeForm()) return;

    var data = {
        firstName: $.trim($("#empFirstName").val()),
        lastName:  $.trim($("#empLastName").val())
    };

    $("#btnAddEmployee").prop("disabled", true).html("Adding\u2026");

    ajax(ManagementAPI.EMPLOYEE, "POST", data)
        .done(function () {
            notify("Employee added successfully", "success");
            $("#employeeForm")[0].reset();
            $(".field-input", "#employeeForm").removeClass("invalid");
            $(".field-error", "#employeeForm").text("");
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Failed to add employee";
            notify(msg, "error");
        })
        .always(function () {
            $("#btnAddEmployee").prop("disabled", false).html(
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                    '<line x1="12" y1="5" x2="12" y2="19"/>' +
                    '<line x1="5" y1="12" x2="19" y2="12"/>' +
                '</svg> Add Employee'
            );
        });
}

// ============================================================
// INITIALIZATION
// ============================================================

function loadCurrentUser() {
    ajax(ManagementAPI.AUTH_ME, "GET")
        .done(function (user) {
            renderUserHeader(user);
            initProfileLinks(user.id);
        })
        .fail(function () {
            initProfileLinks();
        });
}

$(document).ready(function () {
    loadCurrentUser();

    $("#clientForm").on("submit", function (e) {
        e.preventDefault();
        submitClient();
    });

    $("#employeeForm").on("submit", function (e) {
        e.preventDefault();
        submitEmployee();
    });
});
