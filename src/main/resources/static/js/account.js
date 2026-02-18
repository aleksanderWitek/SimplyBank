/**
 * SimplyBank — Individual Account Page Controller
 * Reads ?id= from URL, loads the account details and its transactions,
 * renders the account hero, summary strip, and transaction table.
 *
 * Shared utilities (ajax, formatCurrency, escapeHtml, etc.) are in common.js.
 */

// ============================================================
// CONFIGURATION
// ============================================================

var AccountAPI = {
    BANK_ACCOUNT:     "/api/bank_account",
    TRANSACTION_FROM: "/api/transaction/bank_account_from",
    TRANSACTION_TO:   "/api/transaction/bank_account_to",
    TRANSACTION:      "/api/transaction",
    AUTH_ME:          "/api/auth/me"
};

var PAGE_SIZE = 20;

// ============================================================
// STATE
// ============================================================

var State = {
    accountId:       null,
    account:         null,
    allTransactions: [],
    filtered:        [],
    currentPage:     1,
    totalPages:      1
};

// ============================================================
// INIT
// ============================================================

function getAccountIdFromUrl() {
    var params = new URLSearchParams(window.location.search);
    return params.get("id");
}

function init() {
    var id = getAccountIdFromUrl();
    if (!id) {
        notify("Account ID is missing from URL", "error");
        return;
    }
    State.accountId = id;

    ajax(AccountAPI.AUTH_ME, "GET")
        .done(function (user) {
            renderUserHeader(user);
        })
        .always(function () {
            loadAccount();
        });
}

// ============================================================
// DATA LOADING
// ============================================================

function loadAccount() {
    ajax(AccountAPI.BANK_ACCOUNT + "/" + State.accountId, "GET")
        .done(function (account) {
            State.account = account;
            renderAccountHero(account);
            loadTransactions(account.id);
        })
        .fail(function () {
            $("#heroLoading").html('<p style="color:#dc2626;">Could not load account details.</p>');
            notify("Could not load account", "error");
        });
}

function loadTransactions(id) {
    $("#txLoadingState").show();

    var fromReq = ajax(AccountAPI.TRANSACTION_FROM + "/" + id, "GET");
    var toReq   = ajax(AccountAPI.TRANSACTION_TO   + "/" + id, "GET");

    $.when(fromReq, toReq)
        .done(function (fromRes, toRes) {
            var fromTxs = Array.isArray(fromRes[0]) ? fromRes[0] : [];
            var toTxs   = Array.isArray(toRes[0])   ? toRes[0]   : [];

            // Merge and deduplicate, tagging each tx with direction
            var seenIds = {};
            var allTx   = [];

            fromTxs.forEach(function (tx) {
                if (!seenIds[tx.id]) {
                    seenIds[tx.id] = true;
                    tx._direction  = "outgoing";
                    allTx.push(tx);
                }
            });
            toTxs.forEach(function (tx) {
                if (!seenIds[tx.id]) {
                    seenIds[tx.id] = true;
                    tx._direction  = "incoming";
                    allTx.push(tx);
                } else {
                    // Tx exists in both from and to (same account transfer): treat as incoming
                    var existing = allTx.find(function (t) { return t.id === tx.id; });
                    if (existing) existing._direction = "incoming";
                }
            });

            State.allTransactions = allTx;
            applyFiltersAndRender();
        })
        .fail(function () {
            State.allTransactions = [];
            applyFiltersAndRender();
            notify("Could not load transactions", "error");
        });
}

// ============================================================
// RENDERING: Account Hero
// ============================================================

function renderAccountHero(account) {
    var currency      = (account.currency || account.bankAccountCurrency || "EUR").toUpperCase();
    var balance       = parseFloat(account.balance) || 0;
    var typeName      = (account.bankAccountType || account.type || "Account")
        .replace(/_/g, " ")
        .replace(/\b\w/g, function (c) { return c.toUpperCase(); });
    var displayNumber = maskAccount(account.number || account.accountNumber);
    var iconClass     = getTypeIconClass(account.bankAccountType || account.type || "");

    var heroHtml =
        '<div class="hero-left">' +
            '<div class="hero-icon ' + iconClass + '">' +
                '<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/>' +
                '</svg>' +
            '</div>' +
            '<div class="hero-info">' +
                '<span class="hero-type">'     + escapeHtml(typeName)      + '</span>' +
                '<span class="hero-number">'   + escapeHtml(displayNumber) + '</span>' +
                '<span class="hero-currency">' + escapeHtml(currency)      + '</span>' +
            '</div>' +
        '</div>' +
        '<div class="hero-right">' +
            '<span class="hero-balance-label">Current Balance</span>' +
            '<span class="hero-balance-amount">' + escapeHtml(formatCurrency(balance, currency)) + '</span>' +
        '</div>';

    $("#accountHero").html(heroHtml);

    // Show the rest of the page
    $("#txPageHeader").show();
    $("#summaryStrip").show();
    $("#transactionsSection").show();
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
// FILTERING & PAGINATION
// ============================================================

function applyFiltersAndRender() {
    var typeFilter = $("#filterType").val();

    var list = State.allTransactions.slice();

    if (typeFilter === "INCOMING") {
        list = list.filter(function (tx) { return tx._direction === "incoming"; });
    } else if (typeFilter === "OUTGOING") {
        list = list.filter(function (tx) { return tx._direction === "outgoing"; });
    }

    State.filtered    = list;
    State.currentPage = 1;
    State.totalPages  = Math.max(1, Math.ceil(list.length / PAGE_SIZE));

    renderSummary(list);
    renderPage();
    $("#txLoadingState").hide();
}

function renderPage() {
    var start = (State.currentPage - 1) * PAGE_SIZE;
    var page  = State.filtered.slice(start, start + PAGE_SIZE);
    renderTransactionRows(page);
    renderPagination();
}

// ============================================================
// RENDER: Summary Strip
// ============================================================

function renderSummary(list) {
    var currency = State.account
        ? (State.account.currency || State.account.bankAccountCurrency || "EUR").toUpperCase()
        : "EUR";
    var inTotal  = 0;
    var outTotal = 0;

    list.forEach(function (tx) {
        var amt = Math.abs(parseFloat(tx.amount) || 0);
        if (tx._direction === "incoming") inTotal  += amt;
        else                              outTotal += amt;
    });

    $("#totalCount").text(list.length);
    $("#totalIncoming").text(formatCurrency(inTotal,  currency));
    $("#totalOutgoing").text(formatCurrency(outTotal, currency));
}

// ============================================================
// RENDER: Transaction Rows
// ============================================================

function renderTransactionRows(transactions) {
    var $tbody = $("#transactionsBody");
    $tbody.empty();

    if (!transactions || transactions.length === 0) {
        $(".transactions-table-wrapper").hide();
        $("#emptyState").show();
        $("#pagination").hide();
        return;
    }

    $(".transactions-table-wrapper").show();
    $("#emptyState").hide();

    var accountCurrency = State.account
        ? (State.account.currency || State.account.bankAccountCurrency || "EUR").toUpperCase()
        : "EUR";

    transactions.forEach(function (tx) {
        var dir      = tx._direction || "outgoing";
        var isIn     = dir === "incoming";
        var amount   = parseFloat(tx.amount) || 0;
        var currency = (tx.currency || accountCurrency).toUpperCase();
        var sign     = isIn ? "+" : "-";
        var formatted = sign + formatCurrency(amount, currency);
        var status    = (tx.status || "completed").toLowerCase();

        var fromLabel    = tx.fromAccountNumber || maskAccount(tx.bankAccountNumberFrom) || "\u2014";
        var toLabel      = tx.toAccountNumber   || maskAccount(tx.bankAccountNumberTo)   || "\u2014";
        var counterparty = isIn ? ("From " + maskAccount(fromLabel)) : ("To " + maskAccount(toLabel));

        var arrowSvg = isIn
            ? '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg>'
            : '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>';

        var row =
            '<tr class="row-' + dir + '" data-tx-id="' + escapeHtml(String(tx.id)) + '">' +
                '<td>' +
                    '<div class="tx-desc">' +
                        '<div class="tx-icon ' + dir + '">' + arrowSvg + '</div>' +
                        '<div>' +
                            '<p class="tx-name">' + escapeHtml(tx.description || tx.name || tx.type || "Transaction") + '</p>' +
                            '<p class="tx-category">' + escapeHtml(tx.category || capitalize(dir)) + '</p>' +
                        '</div>' +
                    '</div>' +
                '</td>' +
                '<td>' + formatDate(tx.date || tx.createdAt || tx.timestamp) + '</td>' +
                '<td>' + escapeHtml(counterparty) + '</td>' +
                '<td class="amount-cell ' + (isIn ? 'positive' : 'negative') + '">' + formatted + '</td>' +
                '<td><span class="status-badge ' + escapeHtml(status) + '">' + capitalize(status) + '</span></td>' +
                '<td style="text-align:center">' +
                    '<button class="btn-details" data-tx-id="' + escapeHtml(String(tx.id)) + '">Details</button>' +
                '</td>' +
            '</tr>';

        $tbody.append(row);
    });
}

// ============================================================
// RENDER: Pagination
// ============================================================

function renderPagination() {
    if (State.filtered.length === 0) {
        $("#pagination").hide();
        return;
    }
    $("#pagination").show();
    $("#paginationInfo").text("Page " + State.currentPage + " of " + State.totalPages);
    $("#btnPrevPage").prop("disabled", State.currentPage <= 1);
    $("#btnNextPage").prop("disabled", State.currentPage >= State.totalPages);
}

// ============================================================
// DETAIL MODAL
// ============================================================

function openDetail(transactionId) {
    ajax(AccountAPI.TRANSACTION + "/" + transactionId, "GET")
        .done(function (tx) {
            var local = State.allTransactions.find(function (t) {
                return String(t.id) === String(transactionId);
            });
            if (local) tx._direction = local._direction;
            renderModal(tx);
        })
        .fail(function () {
            var local = State.allTransactions.find(function (t) {
                return String(t.id) === String(transactionId);
            });
            if (local) renderModal(local);
            else notify("Transaction details not found", "error");
        });
}

function renderModal(tx) {
    var dir      = tx._direction || "outgoing";
    var isIn     = dir === "incoming";
    var amount   = parseFloat(tx.amount) || 0;
    var currency = (tx.currency || (State.account ? State.account.currency : "EUR") || "EUR").toUpperCase();
    var sign     = isIn ? "+" : "-";

    var arrowSvg = isIn
        ? '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg>'
        : '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>';

    var fields = [];
    addField(fields, "Transaction ID", tx.id);
    addField(fields, "Description",    tx.description || tx.name || tx.type, true);
    addField(fields, "Category",       tx.category);
    addField(fields, "Status",         capitalize(tx.status));
    addField(fields, "Date",           formatDateTime(tx.date || tx.createdAt || tx.timestamp));
    addField(fields, "From Account",   tx.fromAccountNumber || maskAccount(tx.bankAccountNumberFrom) || formatId(tx.bankAccountIdFrom));
    addField(fields, "To Account",     tx.toAccountNumber   || maskAccount(tx.bankAccountNumberTo)   || formatId(tx.bankAccountIdTo));
    addField(fields, "Currency",       currency);

    var gridHtml = "";
    fields.forEach(function (f) {
        var cls = f.fullWidth ? " full-width" : "";
        gridHtml +=
            '<div class="detail-item' + cls + '">' +
                '<div class="detail-label">' + escapeHtml(f.label) + '</div>' +
                '<div class="detail-value">' + escapeHtml(String(f.value)) + '</div>' +
            '</div>';
    });

    var html =
        '<div class="detail-direction">' +
            '<span class="detail-direction-badge ' + dir + '">' +
                arrowSvg + (isIn ? " Incoming" : " Outgoing") + " Transaction" +
            '</span>' +
        '</div>' +
        '<div class="detail-amount-hero">' +
            '<span class="amount ' + (isIn ? "positive" : "negative") + '">' + sign + formatCurrency(amount, currency) + '</span>' +
        '</div>' +
        '<div class="detail-grid">' + gridHtml + '</div>';

    $("#modalBody").html(html);
    $("#modalOverlay").addClass("open");
}

function closeModal() {
    $("#modalOverlay").removeClass("open");
}

// ============================================================
// EVENT HANDLERS
// ============================================================

$(document).ready(function () {

    init();

    $("#filterType").on("change", function () {
        applyFiltersAndRender();
    });

    $("#btnPrevPage").on("click", function () {
        if (State.currentPage > 1) {
            State.currentPage--;
            renderPage();
            scrollToTable();
        }
    });

    $("#btnNextPage").on("click", function () {
        if (State.currentPage < State.totalPages) {
            State.currentPage++;
            renderPage();
            scrollToTable();
        }
    });

    $(document).on("click", ".btn-details[data-tx-id]", function () {
        var txId = $(this).data("tx-id");
        openDetail(txId);
    });

    $("#modalClose, #modalCloseBtn").on("click", closeModal);
    $("#modalOverlay").on("click", function (e) {
        if (e.target === this) closeModal();
    });
    $(document).on("keydown", function (e) {
        if (e.key === "Escape") closeModal();
    });
});

function scrollToTable() {
    $("html, body").animate({
        scrollTop: $("#transactionsSection").offset().top - 80
    }, 250);
}