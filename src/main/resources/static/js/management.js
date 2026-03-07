/**
 * SimplyBank — Management Page Controller
 *
 * Full CRUD management for Clients, Employees, and Bank Accounts.
 * Shared utilities (ajax, escapeHtml, notify, formatDate, etc.) are in common.js.
 */

// ============================================================
// CONFIGURATION
// ============================================================

var ManagementAPI = {
    CLIENT:           "/api/client",
    CLIENT_PROFILE:   "/api/client/profile",
    EMPLOYEE:         "/api/employee",
    EMPLOYEE_PROFILE: "/api/employee/profile",
    BANK_ACCOUNT:     "/api/bank_account",
    AUTH_ME:          "/api/auth/me"
};

// ============================================================
// CONFIRM MODAL
// ============================================================

var pendingConfirmCallback = null;

function confirmAction(title, message, callback) {
    pendingConfirmCallback = callback;
    $("#confirmModalTitle").text(title);
    $("#confirmModalMessage").text(message);
    $("#confirmModalOverlay").addClass("open");
}

function closeConfirmModal() {
    $("#confirmModalOverlay").removeClass("open");
    pendingConfirmCallback = null;
}

// ============================================================
// TAB SWITCHING
// ============================================================

function initTabs() {
    $(".mgmt-tab").on("click", function () {
        var tabId = $(this).data("tab");
        $(".mgmt-tab").removeClass("active");
        $(this).addClass("active");
        $(".mgmt-tab-content").removeClass("active");
        $("#tab-" + tabId).addClass("active");
    });
}

// ============================================================
// CLIENT: Add (existing)
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
// CLIENT: Find by UserAccount ID
// ============================================================

function findClientByAccountId() {
    var accountId = $.trim($("#findClientAccountId").val());
    $("#findClientAccountIdError").text("");
    $("#findClientAccountId").removeClass("invalid");

    if (!accountId) {
        $("#findClientAccountIdError").text("User Account ID is required");
        $("#findClientAccountId").addClass("invalid");
        return;
    }

    $("#btnFindClient").prop("disabled", true).html("Searching\u2026");

    ajax(ManagementAPI.CLIENT_PROFILE + "?userAccountId=" + encodeURIComponent(accountId), "GET")
        .done(function (profile) {
            var html =
                '<h3 class="result-card-title">Client Profile</h3>' +
                '<div class="result-grid">' +
                    resultField("Login", profile.login) +
                    resultField("First Name", profile.firstName) +
                    resultField("Last Name", profile.lastName) +
                    resultField("Identification Number", profile.identificationNumber) +
                    resultField("City", profile.city) +
                    resultField("Street", profile.street) +
                    resultField("House Number", profile.houseNumber) +
                    resultField("Created", formatDate(profile.clientCreateDate || profile.createDate)) +
                '</div>';
            $("#findClientResult").html(html).show();
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Client not found";
            notify(msg, "error");
            $("#findClientResult").hide();
        })
        .always(function () {
            $("#btnFindClient").prop("disabled", false).html(
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                    '<circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>' +
                '</svg> Find Client'
            );
        });
}

// ============================================================
// CLIENT: Find All
// ============================================================

function findAllClients() {
    $("#btnFindAllClients").prop("disabled", true).html("Loading\u2026");

    ajax(ManagementAPI.CLIENT, "GET")
        .done(function (clients) {
            var $tbody = $("#allClientsBody");
            $tbody.empty();

            if (!clients || clients.length === 0) {
                $tbody.html('<tr><td colspan="5" style="text-align:center;color:var(--gray-400);padding:24px;">No clients found</td></tr>');
            } else {
                clients.forEach(function (c) {
                    $tbody.append(
                        '<tr>' +
                            '<td>' + escapeHtml(String(c.id)) + '</td>' +
                            '<td>' + escapeHtml(c.firstName || "") + '</td>' +
                            '<td>' + escapeHtml(c.lastName || "") + '</td>' +
                            '<td>' + escapeHtml(c.identificationNumber || "") + '</td>' +
                            '<td>' + formatDate(c.createDate) + '</td>' +
                        '</tr>'
                    );
                });
            }
            $("#allClientsTableWrapper").show();
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Failed to load clients";
            notify(msg, "error");
        })
        .always(function () {
            $("#btnFindAllClients").prop("disabled", false).html(
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                    '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>' +
                '</svg> Load All Clients'
            );
        });
}

// ============================================================
// CLIENT: Edit (Load + Save)
// ============================================================

function loadClientForEdit() {
    var clientId = $.trim($("#editClientId").val());
    $("#editClientIdError").text("");
    $("#editClientId").removeClass("invalid");

    if (!clientId) {
        $("#editClientIdError").text("Client ID is required");
        $("#editClientId").addClass("invalid");
        return;
    }

    $("#btnLoadClient").prop("disabled", true).text("Loading\u2026");

    ajax(ManagementAPI.CLIENT + "/" + encodeURIComponent(clientId), "GET")
        .done(function (client) {
            $("#editClientFirstName").val(client.firstName || "").prop("disabled", false);
            $("#editClientLastName").val(client.lastName || "").prop("disabled", false);
            $("#editClientCity").val(client.city || "").prop("disabled", false);
            $("#editClientStreet").val(client.street || "").prop("disabled", false);
            $("#editClientHouseNumber").val(client.houseNumber || "").prop("disabled", false);
            $("#editClientIdNumber").val(client.identificationNumber || "").prop("disabled", false);
            $("#btnEditClient").prop("disabled", false);
            notify("Client data loaded", "info");
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Client not found";
            notify(msg, "error");
        })
        .always(function () {
            $("#btnLoadClient").prop("disabled", false).text("Load");
        });
}

function validateEditClientForm() {
    var valid = true;

    $(".field-error", "#editClientForm").text("");
    $(".field-input", "#editClientForm").removeClass("invalid");

    var fields = [
        { id: "editClientFirstName",   label: "First name" },
        { id: "editClientLastName",    label: "Last name" },
        { id: "editClientCity",        label: "City" },
        { id: "editClientStreet",      label: "Street" },
        { id: "editClientHouseNumber", label: "House number" },
        { id: "editClientIdNumber",    label: "Identification number" }
    ];

    fields.forEach(function (f) {
        var val = $.trim($("#" + f.id).val());
        if (!val) {
            $("#" + f.id + "Error").text(f.label + " is required");
            $("#" + f.id).addClass("invalid");
            valid = false;
        }
    });

    var clientId = $.trim($("#editClientId").val());
    if (!clientId) {
        $("#editClientIdError").text("Client ID is required");
        $("#editClientId").addClass("invalid");
        valid = false;
    }

    return valid;
}

function submitEditClient() {
    if (!validateEditClientForm()) return;

    var clientId = $.trim($("#editClientId").val());
    var data = {
        firstName:            $.trim($("#editClientFirstName").val()),
        lastName:             $.trim($("#editClientLastName").val()),
        city:                 $.trim($("#editClientCity").val()),
        street:               $.trim($("#editClientStreet").val()),
        houseNumber:          $.trim($("#editClientHouseNumber").val()),
        identificationNumber: $.trim($("#editClientIdNumber").val())
    };

    $("#btnEditClient").prop("disabled", true).html("Saving\u2026");

    ajax(ManagementAPI.CLIENT + "/" + encodeURIComponent(clientId), "PUT", data)
        .done(function () {
            notify("Client updated successfully", "success");
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Failed to update client";
            notify(msg, "error");
        })
        .always(function () {
            $("#btnEditClient").prop("disabled", false).html(
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                    '<path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>' +
                    '<polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/>' +
                '</svg> Save Changes'
            );
        });
}

// ============================================================
// CLIENT: Delete
// ============================================================

function deleteClient() {
    var clientId = $.trim($("#deleteClientId").val());
    $("#deleteClientIdError").text("");
    $("#deleteClientId").removeClass("invalid");

    if (!clientId) {
        $("#deleteClientIdError").text("Client ID is required");
        $("#deleteClientId").addClass("invalid");
        return;
    }

    confirmAction("Delete Client", "Are you sure you want to delete client #" + clientId + "? This action cannot be undone.", function () {
        $("#btnDeleteClient").prop("disabled", true).html("Deleting\u2026");

        ajax(ManagementAPI.CLIENT + "/" + encodeURIComponent(clientId), "DELETE")
            .done(function () {
                notify("Client deleted successfully", "success");
                $("#deleteClientId").val("");
            })
            .fail(function (jqxhr) {
                var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                    ? jqxhr.responseJSON.message
                    : "Failed to delete client";
                notify(msg, "error");
            })
            .always(function () {
                $("#btnDeleteClient").prop("disabled", false).html(
                    '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                        '<polyline points="3 6 5 6 21 6"/>' +
                        '<path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>' +
                    '</svg> Delete Client'
                );
            });
    });
}

// ============================================================
// EMPLOYEE: Add (existing)
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
// EMPLOYEE: Find by UserAccount ID
// ============================================================

function findEmployeeByAccountId() {
    var accountId = $.trim($("#findEmpAccountId").val());
    $("#findEmpAccountIdError").text("");
    $("#findEmpAccountId").removeClass("invalid");

    if (!accountId) {
        $("#findEmpAccountIdError").text("User Account ID is required");
        $("#findEmpAccountId").addClass("invalid");
        return;
    }

    $("#btnFindEmployee").prop("disabled", true).html("Searching\u2026");

    ajax(ManagementAPI.EMPLOYEE_PROFILE + "?userAccountId=" + encodeURIComponent(accountId), "GET")
        .done(function (profile) {
            var html =
                '<h3 class="result-card-title">Employee Profile</h3>' +
                '<div class="result-grid">' +
                    resultField("Login", profile.login) +
                    resultField("First Name", profile.firstName) +
                    resultField("Last Name", profile.lastName) +
                    resultField("Role", capitalize(profile.role || "")) +
                    resultField("Created", formatDate(profile.employeeCreateDate || profile.createDate)) +
                '</div>';
            $("#findEmployeeResult").html(html).show();
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Employee not found";
            notify(msg, "error");
            $("#findEmployeeResult").hide();
        })
        .always(function () {
            $("#btnFindEmployee").prop("disabled", false).html(
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                    '<circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>' +
                '</svg> Find Employee'
            );
        });
}

// ============================================================
// EMPLOYEE: Find All
// ============================================================

function findAllEmployees() {
    $("#btnFindAllEmployees").prop("disabled", true).html("Loading\u2026");

    ajax(ManagementAPI.EMPLOYEE, "GET")
        .done(function (employees) {
            var $tbody = $("#allEmployeesBody");
            $tbody.empty();

            if (!employees || employees.length === 0) {
                $tbody.html('<tr><td colspan="4" style="text-align:center;color:var(--gray-400);padding:24px;">No employees found</td></tr>');
            } else {
                employees.forEach(function (e) {
                    $tbody.append(
                        '<tr>' +
                            '<td>' + escapeHtml(String(e.id)) + '</td>' +
                            '<td>' + escapeHtml(e.firstName || "") + '</td>' +
                            '<td>' + escapeHtml(e.lastName || "") + '</td>' +
                            '<td>' + formatDate(e.createDate) + '</td>' +
                        '</tr>'
                    );
                });
            }
            $("#allEmployeesTableWrapper").show();
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Failed to load employees";
            notify(msg, "error");
        })
        .always(function () {
            $("#btnFindAllEmployees").prop("disabled", false).html(
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                    '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>' +
                '</svg> Load All Employees'
            );
        });
}

// ============================================================
// EMPLOYEE: Edit (Load + Save)
// ============================================================

function loadEmployeeForEdit() {
    var empId = $.trim($("#editEmpId").val());
    $("#editEmpIdError").text("");
    $("#editEmpId").removeClass("invalid");

    if (!empId) {
        $("#editEmpIdError").text("Employee ID is required");
        $("#editEmpId").addClass("invalid");
        return;
    }

    $("#btnLoadEmployee").prop("disabled", true).text("Loading\u2026");

    ajax(ManagementAPI.EMPLOYEE + "/" + encodeURIComponent(empId), "GET")
        .done(function (employee) {
            $("#editEmpFirstName").val(employee.firstName || "").prop("disabled", false);
            $("#editEmpLastName").val(employee.lastName || "").prop("disabled", false);
            $("#btnEditEmployee").prop("disabled", false);
            notify("Employee data loaded", "info");
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Employee not found";
            notify(msg, "error");
        })
        .always(function () {
            $("#btnLoadEmployee").prop("disabled", false).text("Load");
        });
}

function validateEditEmployeeForm() {
    var valid = true;

    $(".field-error", "#editEmployeeForm").text("");
    $(".field-input", "#editEmployeeForm").removeClass("invalid");

    var fields = [
        { id: "editEmpFirstName", label: "First name" },
        { id: "editEmpLastName",  label: "Last name" }
    ];

    fields.forEach(function (f) {
        var val = $.trim($("#" + f.id).val());
        if (!val) {
            $("#" + f.id + "Error").text(f.label + " is required");
            $("#" + f.id).addClass("invalid");
            valid = false;
        }
    });

    var empId = $.trim($("#editEmpId").val());
    if (!empId) {
        $("#editEmpIdError").text("Employee ID is required");
        $("#editEmpId").addClass("invalid");
        valid = false;
    }

    return valid;
}

function submitEditEmployee() {
    if (!validateEditEmployeeForm()) return;

    var empId = $.trim($("#editEmpId").val());
    var data = {
        firstName: $.trim($("#editEmpFirstName").val()),
        lastName:  $.trim($("#editEmpLastName").val())
    };

    $("#btnEditEmployee").prop("disabled", true).html("Saving\u2026");

    ajax(ManagementAPI.EMPLOYEE + "/" + encodeURIComponent(empId), "PUT", data)
        .done(function () {
            notify("Employee updated successfully", "success");
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Failed to update employee";
            notify(msg, "error");
        })
        .always(function () {
            $("#btnEditEmployee").prop("disabled", false).html(
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                    '<path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>' +
                    '<polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/>' +
                '</svg> Save Changes'
            );
        });
}

// ============================================================
// EMPLOYEE: Delete
// ============================================================

function deleteEmployee() {
    var empId = $.trim($("#deleteEmpId").val());
    $("#deleteEmpIdError").text("");
    $("#deleteEmpId").removeClass("invalid");

    if (!empId) {
        $("#deleteEmpIdError").text("Employee ID is required");
        $("#deleteEmpId").addClass("invalid");
        return;
    }

    confirmAction("Delete Employee", "Are you sure you want to delete employee #" + empId + "? This action cannot be undone.", function () {
        $("#btnDeleteEmployee").prop("disabled", true).html("Deleting\u2026");

        ajax(ManagementAPI.EMPLOYEE + "/" + encodeURIComponent(empId), "DELETE")
            .done(function () {
                notify("Employee deleted successfully", "success");
                $("#deleteEmpId").val("");
            })
            .fail(function (jqxhr) {
                var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                    ? jqxhr.responseJSON.message
                    : "Failed to delete employee";
                notify(msg, "error");
            })
            .always(function () {
                $("#btnDeleteEmployee").prop("disabled", false).html(
                    '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                        '<polyline points="3 6 5 6 21 6"/>' +
                        '<path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>' +
                    '</svg> Delete Employee'
                );
            });
    });
}

// ============================================================
// BANK ACCOUNT: Create
// ============================================================

function validateBankAccountForm() {
    var valid = true;

    $(".field-error", "#createBankAccountForm").text("");
    $(".field-input", "#createBankAccountForm").removeClass("invalid");

    if (!$.trim($("#baClientId").val())) {
        $("#baClientIdError").text("Client ID is required");
        $("#baClientId").addClass("invalid");
        valid = false;
    }
    if (!$("#baType").val()) {
        $("#baTypeError").text("Account type is required");
        $("#baType").addClass("invalid");
        valid = false;
    }
    if (!$("#baCurrency").val()) {
        $("#baCurrencyError").text("Currency is required");
        $("#baCurrency").addClass("invalid");
        valid = false;
    }

    return valid;
}

function submitCreateBankAccount() {
    if (!validateBankAccountForm()) return;

    var data = {
        clientId:            parseInt($.trim($("#baClientId").val()), 10),
        bankAccountType:     $("#baType").val(),
        bankAccountCurrency: $("#baCurrency").val()
    };

    $("#btnCreateBankAccount").prop("disabled", true).html("Creating\u2026");

    ajax(ManagementAPI.BANK_ACCOUNT, "POST", data)
        .done(function () {
            notify("Bank account created successfully", "success");
            $("#createBankAccountForm")[0].reset();
            $(".field-input", "#createBankAccountForm").removeClass("invalid");
            $(".field-error", "#createBankAccountForm").text("");
        })
        .fail(function (jqxhr) {
            var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                ? jqxhr.responseJSON.message
                : "Failed to create bank account";
            notify(msg, "error");
        })
        .always(function () {
            $("#btnCreateBankAccount").prop("disabled", false).html(
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                    '<line x1="12" y1="5" x2="12" y2="19"/>' +
                    '<line x1="5" y1="12" x2="19" y2="12"/>' +
                '</svg> Create Account'
            );
        });
}

// ============================================================
// BANK ACCOUNT: Delete
// ============================================================

function deleteBankAccount() {
    var baId = $.trim($("#deleteBaId").val());
    $("#deleteBaIdError").text("");
    $("#deleteBaId").removeClass("invalid");

    if (!baId) {
        $("#deleteBaIdError").text("Bank Account ID is required");
        $("#deleteBaId").addClass("invalid");
        return;
    }

    confirmAction("Delete Bank Account", "Are you sure you want to delete bank account #" + baId + "? This action cannot be undone.", function () {
        $("#btnDeleteBankAccount").prop("disabled", true).html("Deleting\u2026");

        ajax(ManagementAPI.BANK_ACCOUNT + "/" + encodeURIComponent(baId), "DELETE")
            .done(function () {
                notify("Bank account deleted successfully", "success");
                $("#deleteBaId").val("");
            })
            .fail(function (jqxhr) {
                var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                    ? jqxhr.responseJSON.message
                    : "Failed to delete bank account";
                notify(msg, "error");
            })
            .always(function () {
                $("#btnDeleteBankAccount").prop("disabled", false).html(
                    '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                        '<polyline points="3 6 5 6 21 6"/>' +
                        '<path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>' +
                    '</svg> Delete Account'
                );
            });
    });
}

// ============================================================
// HELPERS
// ============================================================

function resultField(label, value) {
    return '<div class="result-field">' +
               '<span class="result-field-label">' + escapeHtml(label) + '</span>' +
               '<span class="result-field-value">' + escapeHtml(String(value || "N/A")) + '</span>' +
           '</div>';
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
    initTabs();

    // --- Client ---
    $("#clientForm").on("submit", function (e) {
        e.preventDefault();
        submitClient();
    });
    $("#btnFindClient").on("click", findClientByAccountId);
    $("#btnFindAllClients").on("click", findAllClients);
    $("#btnLoadClient").on("click", loadClientForEdit);
    $("#editClientForm").on("submit", function (e) {
        e.preventDefault();
        submitEditClient();
    });
    $("#btnDeleteClient").on("click", deleteClient);

    // --- Employee ---
    $("#employeeForm").on("submit", function (e) {
        e.preventDefault();
        submitEmployee();
    });
    $("#btnFindEmployee").on("click", findEmployeeByAccountId);
    $("#btnFindAllEmployees").on("click", findAllEmployees);
    $("#btnLoadEmployee").on("click", loadEmployeeForEdit);
    $("#editEmployeeForm").on("submit", function (e) {
        e.preventDefault();
        submitEditEmployee();
    });
    $("#btnDeleteEmployee").on("click", deleteEmployee);

    // --- Bank Account ---
    $("#createBankAccountForm").on("submit", function (e) {
        e.preventDefault();
        submitCreateBankAccount();
    });
    $("#btnDeleteBankAccount").on("click", deleteBankAccount);

    // --- Confirm Modal ---
    $("#confirmModalOk").on("click", function () {
        closeConfirmModal();
        if (typeof pendingConfirmCallback === "function") {
            var cb = pendingConfirmCallback;
            pendingConfirmCallback = null;
            cb();
        }
    });
    $("#confirmModalCancel, #confirmModalClose").on("click", closeConfirmModal);
    $("#confirmModalOverlay").on("click", function (e) {
        if (e.target === this) closeConfirmModal();
    });
    $(document).on("keydown", function (e) {
        if (e.key === "Escape" && $("#confirmModalOverlay").hasClass("open")) {
            closeConfirmModal();
        }
    });
});
