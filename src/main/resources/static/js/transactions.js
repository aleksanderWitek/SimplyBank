/**
 * SimplyBank — Transactions Page Controller
 * Loads transactions for current user, paginates (20/page),
 * color-codes incoming/outgoing, and shows detail modal.
 *
 * Shared utilities (ajax, formatCurrency, escapeHtml, etc.) are in common.js.
 */

// ============================================================
// CONFIGURATION
// ============================================================

var TxListAPI = {
    TRANSACTION:     "/api/transaction",
    TRANSACTION_FROM: "/api/transaction/bank_account_from",
    TRANSACTION_TO:   "/api/transaction/bank_account_to",
    AUTH_ME:          "/api/auth/me",
    BANK_ACCOUNT:     "/api/bank_account"
};

var PAGE_SIZE = 20;

// ============================================================
// STATE
// ============================================================

var State = {
    allTransactions: [],
    filtered: [],
    currentPage: 1,
    totalPages: 1,
    currentUserId: null,
    currentUserBankAccountIds: []
};

// ============================================================
// DETERMINE DIRECTION  (incoming vs outgoing)
// ============================================================

function getDirection(tx) {
    var userIds = State.currentUserBankAccountIds;

    var toId   = (tx.bankAccountTo && tx.bankAccountTo.id) || null;
    var fromId = (tx.bankAccountFrom && tx.bankAccountFrom.id) || null;

    if (userIds.length > 0) {
        if (toId !== null && userIds.indexOf(toId) !== -1)   return "incoming";
        if (fromId !== null && userIds.indexOf(fromId) !== -1) return "outgoing";
    }

    var amt = parseFloat(tx.amount) || 0;
    return amt >= 0 ? "incoming" : "outgoing";
}

// ============================================================
// DATA LOADING
// ============================================================

function init() {
    showLoading(true);

    ajax(TxListAPI.AUTH_ME, "GET")
        .done(function (user) {
            State.currentUserId = user.id;
            renderUserHeader(user);

            loadUserBankAccounts(user.id)
                .always(function () {
                    loadTransactions();
                });
        })
        .fail(function () {
            loadTransactions();
        });
}

function loadUserBankAccounts(userId) {
    var url = userId
        ? TxListAPI.BANK_ACCOUNT + "?clientId=" + userId
        : TxListAPI.BANK_ACCOUNT;

    return ajax(url, "GET")
        .done(function (accounts) {
            State.currentUserBankAccountIds = (accounts || []).map(function (a) {
                return a.id;
            });
        })
        .fail(function () {
            State.currentUserBankAccountIds = [];
        });
}

function loadTransactions() {
    showLoading(true);

    // Load transactions from user's accounts or fall back to all transactions
    var promises = [];

    if (State.currentUserBankAccountIds.length > 0) {
        State.currentUserBankAccountIds.forEach(function (accId) {
            promises.push(ajax(TxListAPI.TRANSACTION_FROM + "/" + accId, "GET"));
            promises.push(ajax(TxListAPI.TRANSACTION_TO + "/" + accId, "GET"));
        });

        $.when.apply($, promises)
            .done(function () {
                var allTx = [];
                var seenIds = {};
                var results = promises.length === 1 ? [arguments] : arguments;
                for (var i = 0; i < results.length; i++) {
                    var data = Array.isArray(results[i]) ? results[i][0] : results[i];
                    if (Array.isArray(data)) {
                        data.forEach(function (tx) {
                            if (!seenIds[tx.id]) {
                                seenIds[tx.id] = true;
                                allTx.push(tx);
                            }
                        });
                    }
                }
                State.allTransactions = allTx;
                applyFiltersAndRender();
            })
            .fail(function () {
                loadAllTransactionsFallback();
            });
    } else {
        loadAllTransactionsFallback();
    }
}

function loadAllTransactionsFallback() {
    ajax(TxListAPI.TRANSACTION, "GET")
        .done(function (data) {
            State.allTransactions = Array.isArray(data) ? data : [];
            applyFiltersAndRender();
        })
        .fail(function () {
            State.allTransactions = [];
            applyFiltersAndRender();
            notify("Could not load transactions", "error");
        });
}

// ============================================================
// FILTERING & PAGINATION
// ============================================================

function applyFiltersAndRender() {
    var typeFilter   = $("#filterType").val();
    var statusFilter = $("#filterStatus").val();

    var list = State.allTransactions.slice();

    list.forEach(function (tx) {
        if (!tx._direction) tx._direction = getDirection(tx);
    });

    if (typeFilter === "INCOMING") {
        list = list.filter(function (tx) { return tx._direction === "incoming"; });
    } else if (typeFilter === "OUTGOING") {
        list = list.filter(function (tx) { return tx._direction === "outgoing"; });
    }

    if (statusFilter !== "ALL") {
        list = list.filter(function (tx) {
            return (tx.status || "").toUpperCase() === statusFilter;
        });
    }

    State.filtered = list;
    State.currentPage = 1;
    State.totalPages = Math.max(1, Math.ceil(list.length / PAGE_SIZE));

    renderSummary(list);
    renderPage();
    showLoading(false);
}

function renderPage() {
    var start = (State.currentPage - 1) * PAGE_SIZE;
    var page  = State.filtered.slice(start, start + PAGE_SIZE);

    renderTransactionRows(page);
    renderPagination();
}

// ============================================================
// RENDER: Table Rows
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
    $("#pagination").show();

    transactions.forEach(function (tx) {
        var dir       = tx._direction || getDirection(tx);
        var isIn      = dir === "incoming";
        var amount    = parseFloat(tx.amount) || 0;
        var currency  = tx.currency || "EUR";
        var sign      = isIn ? "+" : "-";
        var formatted = sign + formatCurrency(amount, currency);
        var status    = "completed";

        var fromLabel = (tx.bankAccountFrom && tx.bankAccountFrom.number) ? maskAccount(tx.bankAccountFrom.number) : "\u2014";
        var toLabel   = (tx.bankAccountTo && tx.bankAccountTo.number) ? maskAccount(tx.bankAccountTo.number) : "\u2014";
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
                            '<p class="tx-name">' + escapeHtml(tx.description || "Transaction") + '</p>' +
                            '<p class="tx-category">' + escapeHtml(capitalize(dir)) + '</p>' +
                        '</div>' +
                    '</div>' +
                '</td>' +
                '<td>' + formatDate(tx.createDate) + '</td>' +
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
// RENDER: Summary Strip
// ============================================================

function renderSummary(list) {
    var inTotal  = 0;
    var outTotal = 0;

    list.forEach(function (tx) {
        var amt = Math.abs(parseFloat(tx.amount) || 0);
        if (tx._direction === "incoming") inTotal  += amt;
        else                              outTotal += amt;
    });

    $("#totalCount").text(list.length);
    $("#totalIncoming").text(formatCurrency(inTotal, "EUR"));
    $("#totalOutgoing").text(formatCurrency(outTotal, "EUR"));
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
// LOADING STATE
// ============================================================

function showLoading(show) {
    if (show) {
        $("#loadingState").show();
        $(".transactions-table-wrapper").hide();
        $("#emptyState").hide();
        $("#pagination").hide();
    } else {
        $("#loadingState").hide();
    }
}

// ============================================================
// DETAIL MODAL
// ============================================================

function openDetail(transactionId) {
    if (transactionId === null || transactionId === undefined) {
        notify("Transaction ID is required", "error");
        return;
    }

    ajax(TxListAPI.TRANSACTION + "/" + transactionId, "GET")
        .done(function (tx) {
            renderModal(tx);
        })
        .fail(function () {
            var local = State.allTransactions.find(function (t) {
                return t.id === transactionId || String(t.id) === String(transactionId);
            });
            if (local) {
                renderModal(local);
            } else {
                notify("Transaction details not found", "error");
            }
        });
}

function renderModal(tx) {
    var dir      = tx._direction || getDirection(tx);
    var isIn     = dir === "incoming";
    var amount   = parseFloat(tx.amount) || 0;
    var currency = tx.currency || "EUR";
    var sign     = isIn ? "+" : "-";

    var arrowSvg = isIn
        ? '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg>'
        : '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>';

    var fields = [];

    addField(fields, "Transaction ID",   tx.id);
    addField(fields, "Description",      tx.description, true);
    addField(fields, "Date",             formatDateTime(tx.createDate));
    addField(fields, "From Account",     tx.bankAccountFrom ? maskAccount(tx.bankAccountFrom.number) || formatId(tx.bankAccountFrom.id) : null);
    addField(fields, "To Account",       tx.bankAccountTo ? maskAccount(tx.bankAccountTo.number) || formatId(tx.bankAccountTo.id) : null);
    addField(fields, "Currency",         (currency || "").toUpperCase());

    var gridHtml = "";
    fields.forEach(function (f) {
        var cls = f.fullWidth ? ' full-width' : '';
        gridHtml +=
            '<div class="detail-item' + cls + '">' +
                '<div class="detail-label">' + escapeHtml(f.label) + '</div>' +
                '<div class="detail-value">' + escapeHtml(String(f.value)) + '</div>' +
            '</div>';
    });

    var html =
        '<div class="detail-direction">' +
            '<span class="detail-direction-badge ' + dir + '">' +
                arrowSvg +
                (isIn ? " Incoming" : " Outgoing") + ' Transaction' +
            '</span>' +
        '</div>' +
        '<div class="detail-amount-hero">' +
            '<span class="amount ' + (isIn ? 'positive' : 'negative') + '">' + sign + formatCurrency(amount, currency) + '</span>' +
        '</div>' +
        '<div class="detail-grid">' +
            gridHtml +
        '</div>';

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

    $("#filterType, #filterStatus").on("change", function () {
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

    // Details button — event delegation instead of inline onclick
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
        scrollTop: $(".transactions-section").offset().top - 80
    }, 250);
}
