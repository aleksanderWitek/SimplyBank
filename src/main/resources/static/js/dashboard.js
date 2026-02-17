/**
 * SimplyBank Dashboard
 * Loads account cards and recent transactions, renders UI.
 * Shared utilities (ajax, formatCurrency, escapeHtml, etc.) are in common.js.
 */

// ============================================================
// CONFIGURATION
// ============================================================

var DashboardAPI = {
    BANK_ACCOUNT: "/api/bank_account",
    TRANSACTION:  "/api/transaction"
};

// ============================================================
// BANK ACCOUNT SERVICE
// ============================================================

var BankAccountService = {

    findAll: function () {
        return ajax(DashboardAPI.BANK_ACCOUNT, "GET")
            .fail(function (jqxhr) {
                var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                    ? jqxhr.responseJSON.message
                    : "Failed to load bank accounts";
                notify(msg, "error");
            });
    },

    findById: function (id) {
        if (id === null || id === undefined) {
            notify("Account ID is required", "error");
            return $.Deferred().reject("Id is null").promise();
        }
        return ajax(DashboardAPI.BANK_ACCOUNT + "/" + id, "GET")
            .fail(function (jqxhr) {
                var msg = jqxhr.responseJSON && jqxhr.responseJSON.message
                    ? jqxhr.responseJSON.message
                    : "Bank account not found";
                notify(msg, "error");
            });
    }
};

// ============================================================
// DASHBOARD RENDERING
// ============================================================

var DashboardRenderer = {

    renderAccountCards: function (accounts) {
        var $section = $(".accounts-section");
        $section.empty();

        if (!accounts || accounts.length === 0) {
            $section.html('<p style="color:#94a3b8;padding:20px;">No accounts found.</p>');
            return;
        }

        accounts.forEach(function (account, index) {
            var isPrimary = index === 0;
            var isCredit = (account.bankAccountType || account.type || "").toUpperCase() === "CREDIT";
            var currency = (account.currency || account.bankAccountCurrency || "EUR").toUpperCase();
            var balance = parseFloat(account.balance) || 0;
            var formattedBalance = formatCurrency(balance, currency);
            var displayNumber = maskAccount(account.number || account.accountNumber);
            var typeName = (account.bankAccountType || account.type || "Account")
                .replace(/_/g, " ")
                .replace(/\b\w/g, function (c) { return c.toUpperCase(); });

            var footerStatLabel, footerStatValue;
            if (isCredit) {
                footerStatLabel = "Credit Limit";
                footerStatValue = formatCurrency(account.creditLimit || 0, currency);
            } else if (typeName.toUpperCase().indexOf("SAVING") !== -1) {
                footerStatLabel = "Interest Rate";
                footerStatValue = (account.interestRate || "2.5") + "% APY";
            } else {
                footerStatLabel = "Available";
                footerStatValue = formattedBalance;
            }

            var symbols = { EUR: "\u20AC", USD: "$", GBP: "\u00A3" };
            var sym = symbols[currency] || currency;

            var cardHtml =
                '<div class="account-card' + (isPrimary ? ' primary' : '') + '" data-account-id="' + escapeHtml(String(account.id)) + '">' +
                    '<div class="account-header">' +
                        '<div>' +
                            '<p class="account-label">' + escapeHtml(typeName) + '</p>' +
                            '<p class="account-number">' + escapeHtml(displayNumber) + '</p>' +
                        '</div>' +
                        (isPrimary ? '<div class="account-badge">Primary</div>' : '') +
                    '</div>' +
                    '<div class="account-balance' + (isCredit ? ' credit' : '') + '">' +
                        '<span class="currency">' + escapeHtml(sym) + '</span>' +
                        '<span class="amount">' + balance.toLocaleString("en-IE", { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + '</span>' +
                    '</div>' +
                    '<div class="account-footer">' +
                        '<div class="account-stat">' +
                            '<span class="stat-label">' + escapeHtml(footerStatLabel) + '</span>' +
                            '<span class="stat-value">' + escapeHtml(footerStatValue) + '</span>' +
                        '</div>' +
                        '<button class="btn-link" data-account-id="' + escapeHtml(String(account.id)) + '">View Details \u2192</button>' +
                    '</div>' +
                '</div>';

            $section.append(cardHtml);
        });

        // Bind click handlers via event delegation instead of inline onclick
        $section.on("click", ".btn-link[data-account-id]", function () {
            var accountId = $(this).data("account-id");
            DashboardRenderer.onViewAccountDetails(accountId);
        });
    },

    renderTransactions: function (transactions) {
        var $tbody = $(".transactions-table tbody");
        $tbody.empty();

        if (!transactions || transactions.length === 0) {
            $tbody.html('<tr><td colspan="5" style="text-align:center;color:#94a3b8;padding:24px;">No recent transactions.</td></tr>');
            return;
        }

        transactions.forEach(function (tx) {
            var amount = parseFloat(tx.amount) || 0;
            var isPositive = amount >= 0;
            var formattedAmount = (isPositive ? "+" : "-") + formatCurrency(Math.abs(amount), tx.currency || "EUR");
            var statusClass = (tx.status || "completed").toLowerCase();

            var rowHtml =
                '<tr>' +
                    '<td>' +
                        '<div class="transaction-desc">' +
                            '<div class="transaction-icon blue">' +
                                '<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><circle cx="10" cy="10" r="8"/></svg>' +
                            '</div>' +
                            '<div>' +
                                '<p class="transaction-name">' + escapeHtml(tx.description || tx.name || "Transaction") + '</p>' +
                                '<p class="transaction-category">' + escapeHtml(tx.category || "General") + '</p>' +
                            '</div>' +
                        '</div>' +
                    '</td>' +
                    '<td>' + formatDate(tx.date || tx.createdAt) + '</td>' +
                    '<td>' + escapeHtml(tx.accountLabel || maskAccount(tx.accountNumber)) + '</td>' +
                    '<td class="amount ' + (isPositive ? 'positive' : 'negative') + '">' + formattedAmount + '</td>' +
                    '<td><span class="status-badge ' + escapeHtml(statusClass) + '">' + capitalize(statusClass) + '</span></td>' +
                '</tr>';

            $tbody.append(rowHtml);
        });
    },

    renderWelcome: function (user) {
        if (user && user.firstName) {
            $(".page-title").text("Welcome back, " + user.firstName);
        }
        if (user && user.lastName) {
            var initials = (user.firstName || "").charAt(0) + (user.lastName || "").charAt(0);
            $(".user-avatar").text(initials.toUpperCase());
            $(".user-name").text(user.firstName + " " + user.lastName.charAt(0) + ".");
        }
    },

    onViewAccountDetails: function (accountId) {
        BankAccountService.findById(accountId)
            .done(function (account) {
                notify("Viewing account " + maskAccount(account.number || account.accountNumber), "info");
            });
    }
};

// ============================================================
// DATA LOADERS
// ============================================================

function loadTransactions() {
    ajax(DashboardAPI.TRANSACTION, "GET")
        .done(function (data) {
            var list = Array.isArray(data) ? data : [];
            DashboardRenderer.renderTransactions(list.slice(0, 10));
        })
        .fail(function () {
            // Endpoint not available yet — keep template defaults
        });
}

function loadAccountBalances() {
    BankAccountService.findAll()
        .done(function (data) {
            DashboardRenderer.renderAccountCards(Array.isArray(data) ? data : []);
        });
}

function loadCurrentUser() {
    ajax("/api/auth/me", "GET")
        .done(function (user) {
            DashboardRenderer.renderWelcome(user);
        })
        .fail(function () {
            // User info endpoint not available — keep template defaults
        });
}

// ============================================================
// NAV LINK HANDLERS
// ============================================================

function initNavigation() {
    $(".nav-link").on("click", function (e) {
        e.preventDefault();
        $(".nav-link").removeClass("active");
        $(this).addClass("active");

        var page = $(this).text().trim().toLowerCase();
        switch (page) {
            case "dashboard":
                loadAccountBalances();
                loadTransactions();
                break;
            case "accounts":
                BankAccountService.findAll().done(function (data) {
                    DashboardRenderer.renderAccountCards(Array.isArray(data) ? data : []);
                });
                break;
            case "transactions":
                loadTransactions();
                break;
            case "transfer":
                notify("Transfer page — coming soon", "info");
                break;
        }
    });
}

// ============================================================
// QUICK ACTION HANDLERS
// ============================================================

function initQuickActions() {
    $(".action-card").on("click", function () {
        var action = $(this).find(".action-label").text().trim();
        switch (action) {
            case "Transfer Money":
                notify("Opening transfer form...", "info");
                break;
            case "Deposit":
                notify("Opening deposit form...", "info");
                break;
            case "Pay Bills":
                notify("Opening bill payment...", "info");
                break;
            case "Analytics":
                notify("Loading analytics...", "info");
                break;
        }
    });
}

// ============================================================
// INITIALIZATION
// ============================================================

$(document).ready(function () {
    loadCurrentUser();
    loadAccountBalances();
    loadTransactions();

    initNavigation();
    initQuickActions();

    $(".btn-primary").on("click", function () {
        notify("Opening new transaction form...", "info");
    });

    $(".transactions-section .btn-secondary").on("click", function () {
        loadTransactions();
    });
});
