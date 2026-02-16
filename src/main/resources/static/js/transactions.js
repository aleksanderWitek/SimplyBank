/**
 * SimplyBank — Transactions Page Controller
 * Loads transactions for current user, paginates (20/page),
 * color-codes incoming/outgoing, and shows detail modal.
 */

// ============================================================
// CONFIGURATION
// ============================================================

const API = {
    TRANSACTIONS:       "/api/transaction",
    TRANSACTIONS_USER:  "/api/transaction/user",   // GET current user's transactions
    TRANSACTION_BY_ID:  "/api/transaction",         // GET /api/transaction/{id}
    AUTH_ME:            "/api/auth/me",
    BANK_ACCOUNT:       "/api/bank_account"
};

const PAGE_SIZE = 20;

// ============================================================
// STATE
// ============================================================

const State = {
    allTransactions: [],    // full fetched list
    filtered: [],           // after type/status filters
    currentPage: 1,
    totalPages: 1,
    currentUserId: null,
    currentUserBankAccountIds: []   // to determine incoming vs outgoing
};

// ============================================================
// VALIDATION  (mirrors backend TransactionValidation.java)
// ============================================================

const Validation = {

    ensureIdPresent(id) {
        if (id === null || id === undefined) {
            throw new Error("Id is null");
        }
    },

    validateTransactionAccounts(fromId, toId) {
        if (fromId === toId) {
            throw new Error("Transaction cannot have same from and to accounts");
        }
    },

    validateAmount(amount) {
        if (amount === null || amount === undefined || parseFloat(amount) <= 0) {
            throw new Error("Amount must be greater than zero");
        }
    },

    validateSufficientBalance(balance, amount, accountNumber) {
        if (parseFloat(balance) < parseFloat(amount)) {
            throw new Error("Insufficient balance on account: " + accountNumber);
        }
    }
};

// ============================================================
// AJAX HELPER
// ============================================================

function ajax(url, method, data) {
    const opts = {
        url,
        type: method || "GET",
        dataType: "json",
        contentType: "application/json; charset=UTF-8"
    };
    if (data && (method === "POST" || method === "PUT")) {
        opts.data = JSON.stringify(data);
    }
    return $.ajax(opts);
}

// ============================================================
// FORMAT HELPERS
// ============================================================

function formatCurrency(amount, currency) {
    currency = (currency || "EUR").toUpperCase();
    const symbols = { EUR: "€", USD: "$", GBP: "£" };
    const sym = symbols[currency] || currency + " ";
    const num = parseFloat(amount) || 0;
    return sym + Math.abs(num).toLocaleString("en-IE", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function formatDate(dateStr) {
    if (!dateStr) return "N/A";
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString("en-IE", { year: "numeric", month: "short", day: "numeric" });
}

function formatDateTime(dateStr) {
    if (!dateStr) return "N/A";
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString("en-IE", {
        year: "numeric", month: "short", day: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}

function maskAccount(number) {
    if (!number) return "N/A";
    if (number.length <= 4) return number;
    return "••••" + number.slice(-4);
}

function escapeHtml(str) {
    if (!str) return "";
    const el = document.createElement("div");
    el.appendChild(document.createTextNode(str));
    return el.innerHTML;
}

function capitalize(str) {
    if (!str) return "";
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

// ============================================================
// DETERMINE DIRECTION  (incoming vs outgoing)
// ============================================================

/**
 * A transaction is "incoming" if the receiving account belongs to
 * the current user, "outgoing" if the sending account does.
 *
 * Supports several DTO shapes:
 *   { bankAccountIdTo, bankAccountIdFrom }
 *   { toAccountId, fromAccountId }
 *   { receiverAccountId, senderAccountId }
 *   { amount } — positive = incoming, negative = outgoing (fallback)
 */
function getDirection(tx) {
    const userIds = State.currentUserBankAccountIds;

    const toId   = tx.bankAccountIdTo   || tx.toAccountId   || tx.receiverAccountId || null;
    const fromId = tx.bankAccountIdFrom || tx.fromAccountId  || tx.senderAccountId   || null;

    if (userIds.length > 0) {
        if (toId !== null && userIds.includes(toId))   return "incoming";
        if (fromId !== null && userIds.includes(fromId)) return "outgoing";
    }

    // Fallback: use sign of amount
    const amt = parseFloat(tx.amount) || 0;
    return amt >= 0 ? "incoming" : "outgoing";
}

// ============================================================
// NOTIFICATION
// ============================================================

function notify(message, type) {
    type = type || "info";
    $(".notification").remove();
    const colors = { success: "#059669", error: "#dc2626", info: "#2563eb", warning: "#d97706" };
    const $n = $(`<div class="notification" style="background:${colors[type] || colors.info}">${escapeHtml(message)}</div>`);
    $("body").append($n);
    setTimeout(() => $n.fadeOut(300, function () { $(this).remove(); }), 4000);
}

// ============================================================
// DATA LOADING
// ============================================================

/**
 * Load current user info, then their bank account IDs, then transactions.
 */
function init() {
    showLoading(true);

    // Step 1 — current user
    ajax(API.AUTH_ME, "GET")
        .done(function (user) {
            State.currentUserId = user.id;
            renderUserHeader(user);

            // Step 2 — user's bank accounts (to know direction)
            loadUserBankAccounts(user.id)
                .always(function () {
                    // Step 3 — transactions
                    loadTransactions();
                });
        })
        .fail(function () {
            // Auth endpoint may not exist yet — proceed anyway
            loadTransactions();
        });
}

function loadUserBankAccounts(userId) {
    const url = userId
        ? API.BANK_ACCOUNT + "?clientId=" + userId
        : API.BANK_ACCOUNT;

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

    const url = State.currentUserId
        ? API.TRANSACTIONS_USER + "/" + State.currentUserId
        : API.TRANSACTIONS;

    ajax(url, "GET")
        .done(function (data) {
            State.allTransactions = Array.isArray(data) ? data : [];
            applyFiltersAndRender();
        })
        .fail(function (jqxhr) {
            // Fallback: try generic endpoint
            ajax(API.TRANSACTIONS, "GET")
                .done(function (data) {
                    State.allTransactions = Array.isArray(data) ? data : [];
                    applyFiltersAndRender();
                })
                .fail(function () {
                    State.allTransactions = [];
                    applyFiltersAndRender();
                    notify("Could not load transactions", "error");
                });
        });
}

// ============================================================
// FILTERING & PAGINATION
// ============================================================

function applyFiltersAndRender() {
    const typeFilter   = $("#filterType").val();
    const statusFilter = $("#filterStatus").val();

    let list = State.allTransactions.slice();

    // Attach direction to each (cached for perf)
    list.forEach(function (tx) {
        if (!tx._direction) tx._direction = getDirection(tx);
    });

    // Type filter
    if (typeFilter === "INCOMING") {
        list = list.filter(function (tx) { return tx._direction === "incoming"; });
    } else if (typeFilter === "OUTGOING") {
        list = list.filter(function (tx) { return tx._direction === "outgoing"; });
    }

    // Status filter
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
    const start = (State.currentPage - 1) * PAGE_SIZE;
    const page  = State.filtered.slice(start, start + PAGE_SIZE);

    renderTransactionRows(page);
    renderPagination();
}

// ============================================================
// RENDER: Table Rows
// ============================================================

function renderTransactionRows(transactions) {
    const $tbody = $("#transactionsBody");
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
        const dir       = tx._direction || getDirection(tx);
        const isIn      = dir === "incoming";
        const amount    = parseFloat(tx.amount) || 0;
        const currency  = tx.currency || tx.bankAccountCurrency || "EUR";
        const sign      = isIn ? "+" : "-";
        const formatted = sign + formatCurrency(amount, currency);
        const status    = (tx.status || "completed").toLowerCase();

        const fromLabel = tx.fromAccountNumber || tx.senderAccountNumber || maskAccount(tx.bankAccountNumberFrom) || "—";
        const toLabel   = tx.toAccountNumber   || tx.receiverAccountNumber || maskAccount(tx.bankAccountNumberTo) || "—";
        const counterparty = isIn ? ("From " + maskAccount(fromLabel)) : ("To " + maskAccount(toLabel));

        // Arrow icon
        const arrowSvg = isIn
            ? '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg>'
            : '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>';

        const row = `
            <tr class="row-${dir}" data-tx-id="${tx.id}">
                <td>
                    <div class="tx-desc">
                        <div class="tx-icon ${dir}">${arrowSvg}</div>
                        <div>
                            <p class="tx-name">${escapeHtml(tx.description || tx.name || tx.type || "Transaction")}</p>
                            <p class="tx-category">${escapeHtml(tx.category || capitalize(dir))}</p>
                        </div>
                    </div>
                </td>
                <td>${formatDate(tx.date || tx.createdAt || tx.timestamp)}</td>
                <td>${escapeHtml(counterparty)}</td>
                <td class="amount-cell ${isIn ? 'positive' : 'negative'}">${formatted}</td>
                <td><span class="status-badge ${status}">${capitalize(status)}</span></td>
                <td style="text-align:center">
                    <button class="btn-details" onclick="openDetail(${tx.id})">Details</button>
                </td>
            </tr>`;
        $tbody.append(row);
    });
}

// ============================================================
// RENDER: Summary Strip
// ============================================================

function renderSummary(list) {
    let inTotal  = 0;
    let outTotal = 0;

    list.forEach(function (tx) {
        const amt = Math.abs(parseFloat(tx.amount) || 0);
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
// RENDER: User header
// ============================================================

function renderUserHeader(user) {
    if (!user) return;
    const first = user.firstName || "";
    const last  = user.lastName  || "";
    if (first || last) {
        const initials = (first.charAt(0) + last.charAt(0)).toUpperCase();
        $("#headerUserAvatar").text(initials);
        $("#headerUserName").text(first + " " + last.charAt(0) + ".");
    }
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

/**
 * Open detail modal for a transaction.
 * First tries fetching full details from API, falls back to local data.
 */
function openDetail(transactionId) {
    try {
        Validation.ensureIdPresent(transactionId);
    } catch (e) {
        notify(e.message, "error");
        return;
    }

    // Try API first
    ajax(API.TRANSACTION_BY_ID + "/" + transactionId, "GET")
        .done(function (tx) {
            renderModal(tx);
        })
        .fail(function () {
            // Fallback: find in local data
            const local = State.allTransactions.find(function (t) {
                return t.id === transactionId || t.id === String(transactionId);
            });
            if (local) {
                renderModal(local);
            } else {
                notify("Transaction details not found", "error");
            }
        });
}

function renderModal(tx) {
    const dir      = tx._direction || getDirection(tx);
    const isIn     = dir === "incoming";
    const amount   = parseFloat(tx.amount) || 0;
    const currency = tx.currency || tx.bankAccountCurrency || "EUR";
    const sign     = isIn ? "+" : "-";

    const arrowSvg = isIn
        ? '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg>'
        : '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>';

    // Build detail fields dynamically from whatever fields exist
    const fields = [];

    addField(fields, "Transaction ID",   tx.id);
    addField(fields, "Description",      tx.description || tx.name || tx.type, true);
    addField(fields, "Category",         tx.category);
    addField(fields, "Status",           capitalize(tx.status));
    addField(fields, "Date",             formatDateTime(tx.date || tx.createdAt || tx.timestamp));
    addField(fields, "From Account",     tx.fromAccountNumber || tx.senderAccountNumber || maskAccount(tx.bankAccountNumberFrom) || formatId(tx.bankAccountIdFrom || tx.fromAccountId || tx.senderAccountId));
    addField(fields, "To Account",       tx.toAccountNumber || tx.receiverAccountNumber || maskAccount(tx.bankAccountNumberTo) || formatId(tx.bankAccountIdTo || tx.toAccountId || tx.receiverAccountId));
    addField(fields, "Currency",         (currency || "").toUpperCase());
    addField(fields, "Bank Account Type", tx.bankAccountType || tx.accountType);
    addField(fields, "Reference",        tx.reference || tx.referenceNumber);
    addField(fields, "Note",             tx.note || tx.notes || tx.memo, true);
    addField(fields, "Created By",       tx.createdBy || tx.initiatedBy);
    addField(fields, "Updated At",       formatDateTime(tx.updatedAt || tx.modifiedAt));

    // Build grid HTML
    let gridHtml = "";
    fields.forEach(function (f) {
        const cls = f.fullWidth ? ' full-width' : '';
        gridHtml += `
            <div class="detail-item${cls}">
                <div class="detail-label">${escapeHtml(f.label)}</div>
                <div class="detail-value">${escapeHtml(String(f.value))}</div>
            </div>`;
    });

    const html = `
        <div class="detail-direction">
            <span class="detail-direction-badge ${dir}">
                ${arrowSvg}
                ${isIn ? "Incoming" : "Outgoing"} Transaction
            </span>
        </div>
        <div class="detail-amount-hero">
            <span class="amount ${isIn ? 'positive' : 'negative'}">${sign}${formatCurrency(amount, currency)}</span>
        </div>
        <div class="detail-grid">
            ${gridHtml}
        </div>`;

    $("#modalBody").html(html);
    $("#modalOverlay").addClass("open");
}

function addField(arr, label, value, fullWidth) {
    if (value !== null && value !== undefined && value !== "" && value !== "N/A" && value !== "null") {
        arr.push({ label: label, value: value, fullWidth: !!fullWidth });
    }
}

function formatId(id) {
    if (id === null || id === undefined) return null;
    return "#" + id;
}

function closeModal() {
    $("#modalOverlay").removeClass("open");
}

// ============================================================
// EVENT HANDLERS
// ============================================================

$(document).ready(function () {

    // --- Init ---
    init();

    // --- Filters ---
    $("#filterType, #filterStatus").on("change", function () {
        applyFiltersAndRender();
    });

    // --- Pagination ---
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

    // --- Modal close ---
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
