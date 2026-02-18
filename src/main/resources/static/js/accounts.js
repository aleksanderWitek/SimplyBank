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
    TRANSACTION_FROM: "/api/transaction/bank_account_from",
    TRANSACTION_TO:   "/api/transaction/bank_account_to",
    AUTH_ME:          "/api/auth/me"
};

// ============================================================
// INIT
// ============================================================

function init() {
    showLoading(true);

    ajax(AccountsAPI.AUTH_ME, "GET")
        .done(function (user) {
            renderUserHeader(user);
        })
        .always(function () {
            loadAccounts();
        });
}

// ============================================================
// DATA LOADING
// ============================================================

function loadAccounts() {
    ajax(AccountsAPI.BANK_ACCOUNT, "GET")
        .done(function (accounts) {
            if (!accounts || accounts.length === 0) {
                showLoading(false);
                $("#emptyState").show();
                return;
            }
            renderAccountRows(accounts);
            loadAllStats(accounts);
        })
        .fail(function () {
            showLoading(false);
            notify("Could not load accounts", "error");
        });
}

function loadAllStats(accounts) {
    accounts.forEach(function (account) {
        var id       = account.id;
        var currency = (account.currency || account.bankAccountCurrency || "EUR").toUpperCase();

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
            });
        // On fail stats remain as "—"
    });
}

// ============================================================
// RENDERING
// ============================================================

function renderAccountRows(accounts) {
    var $list = $("#accountsList");
    $list.empty();

    accounts.forEach(function (account) {
        var id            = account.id;
        var currency      = (account.currency || account.bankAccountCurrency || "EUR").toUpperCase();
        var balance       = parseFloat(account.balance) || 0;
        var typeName      = (account.bankAccountType || account.type || "Account")
            .replace(/_/g, " ")
            .replace(/\b\w/g, function (c) { return c.toUpperCase(); });
        var displayNumber = maskAccount(account.number || account.accountNumber);
        var iconClass     = getTypeIconClass(account.bankAccountType || account.type || "");

        var row =
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

        $list.append(row);
    });

    showLoading(false);
    $list.show();
}

function getTypeIconClass(type) {
    var t = (type || "").toUpperCase();
    if (t === "CHECKING")         return "blue";
    if (t === "SAVING")           return "green";
    if (t === "BUSINESS")         return "purple";
    if (t === "FOREIGN_CURRENCY") return "amber";
    return "blue";
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

$(document).ready(function () {
    init();

    // Navigate to individual account page on row click (event delegation)
    $("#accountsList").on("click", ".account-row", function () {
        window.location.href = "/account?id=" + $(this).data("account-id");
    });
});