/**
 * SimplyBank — Accounts Page Controller
 * Loads all accounts with per-account transaction stats,
 * renders as clickable rows. Clicking navigates to /account?id=X.
 *
 * Shared utilities (ajax, formatCurrency, escapeHtml, etc.) are in common.js.
 */

// ============================================================
// CONFIGURATION
// ============================================================

var AccountsAPI = {
    BANK_ACCOUNT:     "/api/bank_account",
    BY_NUMBER:        "/api/bank_account/by-number",
    BY_CLIENT:        "/api/bank_account/by-client",
    OWNERS_SUFFIX:    "/owners",
    TRANSACTION_FROM: "/api/transaction/bank_account_from",
    TRANSACTION_TO:   "/api/transaction/bank_account_to",
    AUTH_ME:          "/api/auth/me",
    CLIENT:           "/api/client"
};

var MAX_ACCOUNTS_PER_CLIENT = 10;

var AccountsState = {
    role: null,
    allAccounts: []
};

// ============================================================
// INIT
// ============================================================

function init() {
    showLoading(true);

    ajax(AccountsAPI.AUTH_ME, "GET")
        .done(function (user) {
            AccountsState.role = (user.role || "").toUpperCase();
            renderUserHeader(user);
            initProfileLinks(user.id);
            applyRoleVisibility();
        })
        .fail(function (jqxhr) {
            console.error("[init] GET " + AccountsAPI.AUTH_ME + " failed:", jqxhr);
            initProfileLinks();
        })
        .always(function () {
            if (isStaff()) {
                showStaffSearchPrompt();
            } else {
                loadAccounts();
            }
        });
}

function applyRoleVisibility() {
    if (AccountsState.role === "CLIENT") {
        $("#createAccountWrap").css("display", "flex");
        $("#accountSearchWrap").hide();
    } else if (AccountsState.role === "EMPLOYEE" || AccountsState.role === "ADMIN") {
        $("#createAccountWrap").hide();
        $("#accountSearchWrap").css("display", "flex");
        $("#pageTitle").text("Accounts");
        $("#pageSubtitle").text("Look up bank accounts by number or by client ID");
    }
}

function showStaffSearchPrompt() {
    showLoading(false);
    $("#accountsList").hide();
    $("#emptyState").hide();
    $("#staffSearchResults")
        .empty()
        .append($('<div class="staff-search-empty"></div>')
            .text("Search by bank account number or client ID to view accounts."))
        .show();
}

function isStaff() {
    return AccountsState.role === "EMPLOYEE" || AccountsState.role === "ADMIN";
}

// ============================================================
// DATA LOADING
// ============================================================

function loadAccounts() {
    ajax(AccountsAPI.BANK_ACCOUNT, "GET")
        .done(function (accounts) {
            AccountsState.allAccounts = Array.isArray(accounts) ? accounts : [];
            updateCreateButtonState();
            renderFilteredAccounts();
        })
        .fail(function (jqxhr) {
            console.error("[loadAccounts] GET " + AccountsAPI.BANK_ACCOUNT + " failed:", jqxhr);
            showLoading(false);
            notify("Could not load accounts", "error");
        });
}

function renderFilteredAccounts() {
    var list = AccountsState.allAccounts;

    $("#accountsList").empty();
    if (!list.length) {
        showLoading(false);
        $("#accountsList").hide();
        $("#emptyState").show();
        return;
    }
    $("#emptyState").hide();
    renderAccountRows(list);
    loadAllStats(list);
}

function updateCreateButtonState() {
    if (AccountsState.role !== "CLIENT") return;
    var atLimit = AccountsState.allAccounts.length >= MAX_ACCOUNTS_PER_CLIENT;
    $("#btnOpenCreate").prop("disabled", atLimit);
    $("#limitReachedNotice").toggle(atLimit);
}

function loadAllStats(accounts) {
    accounts.forEach(function (account) {
        var id       = account.id;
        var currency = (account.currency || "EUR").toUpperCase();

        var fromReq = ajax(AccountsAPI.TRANSACTION_FROM + "/" + id, "GET");
        var toReq   = ajax(AccountsAPI.TRANSACTION_TO   + "/" + id, "GET");

        $.when(fromReq, toReq)
            .done(function (fromRes, toRes) {
                var fromTxs = Array.isArray(fromRes[0]) ? fromRes[0] : [];
                var toTxs   = Array.isArray(toRes[0])   ? toRes[0]   : [];

                var outgoing = 0;
                fromTxs.forEach(function (tx) { outgoing += Math.abs(parseFloat(tx.amount) || 0); });

                var incoming = 0;
                toTxs.forEach(function (tx) { incoming += Math.abs(parseFloat(tx.amount) || 0); });

                // Deduplicate for total count
                var seenIds = {};
                var total = 0;
                fromTxs.concat(toTxs).forEach(function (tx) {
                    if (!seenIds[tx.id]) { seenIds[tx.id] = true; total++; }
                });

                $("#count-"    + id).text(total);
                $("#incoming-" + id).text(formatCurrency(incoming, currency));
                $("#outgoing-" + id).text(formatCurrency(outgoing, currency));
            })
            .fail(function (jqxhr) {
                console.error("[loadAllStats] failed to load tx stats for account " + id + " (FROM/TO):", jqxhr);
                // Stats remain as "—" in the UI
            });
    });
}

// ============================================================
// RENDERING
// ============================================================

function renderAccountRows(accounts) {
    var $list = $("#accountsList");
    var allRowsHtml = "";

    accounts.forEach(function (account) {
        var id            = account.id;
        var currency      = (account.currency || "EUR").toUpperCase();
        var balance       = parseFloat(account.balance) || 0;
        var typeName      = formatAccountType(account.accountType);
        var displayNumber = maskAccount(account.number);
        var iconClass     = getTypeIconClass(account.accountType || "");

        allRowsHtml +=
            '<div class="account-row" data-account-id="' + escapeHtml(String(id)) + '">' +

                // ── Left: account info ──
                '<div class="account-left">' +
                    '<div class="account-type-icon ' + iconClass + '">' +
                        '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                            '<rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/>' +
                        '</svg>' +
                    '</div>' +
                    '<div class="account-info">' +
                        '<span class="account-type">' + escapeHtml(typeName) + '</span>' +
                        '<span class="account-number">' + escapeHtml(displayNumber) + '</span>' +
                    '</div>' +
                    '<div class="account-balance-section">' +
                        '<span class="account-balance">' + escapeHtml(formatCurrency(balance, currency)) + '</span>' +
                        '<span class="account-currency">' + escapeHtml(currency) + '</span>' +
                    '</div>' +
                '</div>' +

                // ── Right: stats ──
                '<div class="account-right">' +
                    '<div class="account-stat">' +
                        '<div class="stat-icon total">' +
                            '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                                '<line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>' +
                            '</svg>' +
                        '</div>' +
                        '<div class="stat-info">' +
                            '<span class="stat-label">Total Transactions</span>' +
                            '<span class="stat-value" id="count-' + escapeHtml(String(id)) + '">\u2014</span>' +
                        '</div>' +
                    '</div>' +
                    '<div class="account-stat">' +
                        '<div class="stat-icon incoming">' +
                            '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                                '<polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/>' +
                            '</svg>' +
                        '</div>' +
                        '<div class="stat-info">' +
                            '<span class="stat-label">Total Incoming</span>' +
                            '<span class="stat-value incoming-val" id="incoming-' + escapeHtml(String(id)) + '">\u2014</span>' +
                        '</div>' +
                    '</div>' +
                    '<div class="account-stat">' +
                        '<div class="stat-icon outgoing">' +
                            '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                                '<polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="17 18 23 18 23 12"/>' +
                            '</svg>' +
                        '</div>' +
                        '<div class="stat-info">' +
                            '<span class="stat-label">Total Outgoing</span>' +
                            '<span class="stat-value outgoing-val" id="outgoing-' + escapeHtml(String(id)) + '">\u2014</span>' +
                        '</div>' +
                    '</div>' +
                '</div>' +

                // ── Arrow ──
                '<div class="account-arrow">' +
                    '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">' +
                        '<polyline points="9 6 15 12 9 18"/>' +
                    '</svg>' +
                '</div>' +

            '</div>';
    });

    $list.html(allRowsHtml);

    showLoading(false);
    $list.show();
}

// ============================================================
// STAFF SEARCH (by bank account number / by client id)
// ============================================================

function showStaffResults($html) {
    $("#accountsList").hide();
    $("#emptyState").hide();
    $("#staffSearchResults").empty().append($html).show();
    showLoading(false);
}

function clearStaffSearch() {
    $("#filterAccountNumber").val("");
    $("#filterClientId").val("");
    $("#btnClearAccountNumber").hide();
    $("#btnClearClientId").hide();
    showStaffSearchPrompt();
}

function searchByAccountNumber() {
    var number = $.trim($("#filterAccountNumber").val() || "");
    if (!number) {
        clearStaffSearch();
        return;
    }
    $("#filterClientId").val("");
    $("#btnClearClientId").hide();

    showLoading(true);
    ajax(AccountsAPI.BY_NUMBER + "/" + encodeURIComponent(number), "GET")
        .done(function (account) {
            ajax(AccountsAPI.BANK_ACCOUNT + "/" + account.id + AccountsAPI.OWNERS_SUFFIX, "GET")
                .done(function (owners) {
                    var $card = buildAccountByNumberResult(account, Array.isArray(owners) ? owners : []);
                    $("#btnClearAccountNumber").show();
                    showStaffResults($card);
                })
                .fail(function (jqxhr) {
                    console.error("[searchByAccountNumber] GET owners for account id=" + account.id + " failed:", jqxhr);
                    var $card = buildAccountByNumberResult(account, []);
                    $("#btnClearAccountNumber").show();
                    showStaffResults($card);
                });
        })
        .fail(function (jqxhr) {
            showLoading(false);
            if (jqxhr.status === 404) {
                var $msg = $('<div class="staff-search-empty"></div>')
                    .text("No bank account found with number: " + number);
                $("#btnClearAccountNumber").show();
                showStaffResults($msg);
                notify("No bank account found with number: " + number, "warning");
            } else {
                console.error("[searchByAccountNumber] GET " + AccountsAPI.BY_NUMBER + "/" + number + " failed:", jqxhr);
                notify("Failed to search bank account", "error");
            }
        });
}

function searchByClientId() {
    var raw = $.trim($("#filterClientId").val() || "");
    if (!raw) {
        clearStaffSearch();
        return;
    }
    if (!/^\d+$/.test(raw)) {
        notify("Client ID must be numeric", "warning");
        return;
    }
    $("#filterAccountNumber").val("");
    $("#btnClearAccountNumber").hide();

    showLoading(true);
    var profileReq  = ajax(AccountsAPI.CLIENT + "/" + raw + "/profile", "GET");
    var accountsReq = ajax(AccountsAPI.BY_CLIENT + "/" + raw, "GET");

    $.when(profileReq, accountsReq)
        .done(function (profileRes, accountsRes) {
            var profile  = profileRes[0];
            var accounts = Array.isArray(accountsRes[0]) ? accountsRes[0] : [];
            var $card = buildAccountsByClientResult(profile, accounts);
            $("#btnClearClientId").show();
            showStaffResults($card);
        })
        .fail(function (jqxhr) {
            showLoading(false);
            if (jqxhr.status === 404) {
                var $msg = $('<div class="staff-search-empty"></div>')
                    .text("No client found with ID: " + raw);
                $("#btnClearClientId").show();
                showStaffResults($msg);
                notify("No client found with ID: " + raw, "warning");
            } else {
                console.error("[searchByClientId] GET failed for clientId=" + raw + ":", jqxhr);
                notify("Failed to look up client", "error");
            }
        });
}

function buildAccountByNumberResult(account, owners) {
    var $card     = $('<div class="staff-search-card"></div>');
    var $header   = $('<div class="staff-search-header"></div>');
    var ownerLine = owners.length
        ? owners.map(function (o) { return (o.firstName || "") + " " + (o.lastName || ""); }).join(", ")
        : "Unknown owner";
    $header.text("Owner: " + ownerLine);
    $card.append($header);

    var currency = (account.currency || "EUR").toUpperCase();
    var balance  = parseFloat(account.balance) || 0;

    var $row = $(
        '<div class="staff-search-account">' +
            '<div class="staff-search-account-info">' +
                '<span class="staff-search-account-label">Account number</span>' +
                '<span class="staff-search-account-number"></span>' +
                '<span class="staff-search-account-type"></span>' +
            '</div>' +
            '<div class="staff-search-account-balance">' +
                '<span class="staff-search-account-balance-label">Balance</span>' +
                '<span class="staff-search-account-balance-value"></span>' +
            '</div>' +
        '</div>'
    );
    $row.find(".staff-search-account-number").text(account.number || "—");
    $row.find(".staff-search-account-type").text(formatAccountType(account.accountType || ""));
    $row.find(".staff-search-account-balance-value").text(formatCurrency(balance, currency) + " " + currency);
    $card.append($row);
    return $card;
}

function buildAccountsByClientResult(profile, accounts) {
    var $card   = $('<div class="staff-search-card"></div>');
    var $header = $('<div class="staff-search-header"></div>');
    $header.text("Client: " + (profile.firstName || "") + " " + (profile.lastName || "") +
                 " (ID " + profile.clientId + ")");
    $card.append($header);

    if (!accounts.length) {
        $card.append($('<div class="staff-search-empty"></div>').text("This client has no bank accounts."));
        return $card;
    }

    var $list = $('<div class="staff-search-accounts-list"></div>');
    accounts.forEach(function (account) {
        var currency = (account.currency || "EUR").toUpperCase();
        var balance  = parseFloat(account.balance) || 0;

        var $row = $(
            '<div class="staff-search-account">' +
                '<div class="staff-search-account-info">' +
                    '<span class="staff-search-account-label">Account number</span>' +
                    '<span class="staff-search-account-number"></span>' +
                    '<span class="staff-search-account-type"></span>' +
                '</div>' +
                '<div class="staff-search-account-balance">' +
                    '<span class="staff-search-account-balance-label">Balance</span>' +
                    '<span class="staff-search-account-balance-value"></span>' +
                '</div>' +
            '</div>'
        );
        $row.find(".staff-search-account-number").text(account.number || "—");
        $row.find(".staff-search-account-type").text(formatAccountType(account.accountType || ""));
        $row.find(".staff-search-account-balance-value").text(formatCurrency(balance, currency) + " " + currency);
        $list.append($row);
    });
    $card.append($list);
    return $card;
}

// ============================================================
// LOADING STATE
// ============================================================

function showLoading(show) {
    if (show) {
        $("#loadingState").show();
        $("#accountsList").hide();
        $("#emptyState").hide();
    } else {
        $("#loadingState").hide();
    }
}

// ============================================================
// ENTRY POINT
// ============================================================

function openCreateModal() {
    if ($("#btnOpenCreate").is(":disabled")) return;
    $("#newAccountType").val("CHECKING");
    $("#newAccountCurrency").val("EUR");
    toggleCurrencyRow();
    $("#createModalOverlay").addClass("open");
    setTimeout(function () { $("#newAccountType").trigger("focus"); }, 0);
}

function closeCreateModal() {
    $("#createModalOverlay").removeClass("open");
}

function toggleCurrencyRow() {
    var type = $("#newAccountType").val();
    if (type === "FOREIGN_CURRENCY") {
        $("#currencyRow").show();
    } else {
        $("#currencyRow").hide();
    }
}

function submitCreateAccount() {
    var type = $("#newAccountType").val();
    var currency = (type === "FOREIGN_CURRENCY") ? $("#newAccountCurrency").val() : "EUR";

    var $btn = $("#createSubmit");
    $btn.prop("disabled", true);

    ajax(AccountsAPI.BANK_ACCOUNT, "POST", {
        bankAccountType: type,
        bankAccountCurrency: currency
    })
        .done(function () {
            notify("Account created", "success");
            closeCreateModal();
            // Refresh page so the user sees the new account row + stats from scratch.
            window.location.reload();
        })
        .fail(function (jqxhr) {
            var msg = "Could not create account";
            if (jqxhr.responseJSON && jqxhr.responseJSON.message) {
                msg = jqxhr.responseJSON.message;
            } else if (jqxhr.status === 409) {
                msg = "Bank account limit reached";
            }
            console.error("[submitCreateAccount] POST " + AccountsAPI.BANK_ACCOUNT + " failed:", jqxhr);
            notify(msg, "error");
        })
        .always(function () {
            $btn.prop("disabled", false);
        });
}

$(document).ready(function () {
    init();

    // Navigate to individual account page on row click (event delegation)
    $("#accountsList").on("click", ".account-row", function () {
        window.location.href = "/account?id=" + $(this).data("account-id");
    });

    // Create-account modal
    $("#btnOpenCreate").on("click", openCreateModal);
    $("#createModalClose, #createCancel").on("click", closeCreateModal);
    $("#createModalOverlay").on("click", function (e) {
        if (e.target === this) closeCreateModal();
    });
    $(document).on("keydown", function (e) {
        if (e.key === "Escape") closeCreateModal();
    });
    $("#newAccountType").on("change", toggleCurrencyRow);
    $("#createSubmit").on("click", submitCreateAccount);

    // Staff search: by account number
    $("#btnSearchAccountNumber").on("click", searchByAccountNumber);
    $("#filterAccountNumber").on("keydown", function (e) {
        if (e.key === "Enter") { e.preventDefault(); searchByAccountNumber(); }
    });
    $("#btnClearAccountNumber").on("click", clearStaffSearch);

    // Staff search: by client id
    $("#btnSearchClientId").on("click", searchByClientId);
    $("#filterClientId").on("keydown", function (e) {
        if (e.key === "Enter") { e.preventDefault(); searchByClientId(); }
    });
    $("#btnClearClientId").on("click", clearStaffSearch);
});