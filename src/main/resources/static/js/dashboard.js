/**
 * SimplyBank Dashboard - API Service & Validation Layer
 * Handles all AJAX calls, DOM rendering, and client-side validation
 */

// ============================================================
// CONFIGURATION
// ============================================================

const API = {
    BANK_ACCOUNT: "/api/bank_account",
    CLIENT: "/api/client",
    EMPLOYEE: "/api/employee",
    USER_ACCOUNT: "/api/user_account",
    TRANSACTIONS_RECENT: "/api/transactions/recent",
    ACCOUNTS_SUMMARY: "/api/accounts/summary"
};

const BANK_ACCOUNT_TYPES = ["CHECKING", "SAVINGS", "CREDIT"];
const CURRENCIES = ["EUR", "USD", "GBP"];
const USER_ACCOUNT_ROLES = ["CLIENT", "EMPLOYEE", "ADMIN"];
const PASSWORD_MIN_LENGTH = 8;

// ============================================================
// VALIDATION MODULE
// ============================================================

const Validation = {

    // --- ID Validation (matches IdValidation.java) ---
    ensureIdPresent(id) {
        if (id === null || id === undefined) {
            throw new Error("Id is null");
        }
    },

    // --- Password Validation (matches PasswordValidation.java) ---
    ensurePasswordMeetsRequirements(password, passwordLength = PASSWORD_MIN_LENGTH) {
        if (password === null || password === undefined || password.length < passwordLength) {
            throw new Error(`Password must be at least ${passwordLength} characters long`);
        }
        if (!/[A-Z]/.test(password)) {
            throw new Error("Password must contain at least one uppercase letter");
        }
        if (!/[a-z]/.test(password)) {
            throw new Error("Password must contain at least one lowercase letter");
        }
        if (!/\d/.test(password)) {
            throw new Error("Password must contain at least one digit");
        }
        if (!/[!@#$%^&*]/.test(password)) {
            throw new Error("Password must contain at least one special character");
        }
    },

    ensurePasswordExists(password) {
        if (password === null || password === undefined) {
            throw new Error("Provided password cannot be null");
        }
    },

    ensureNewPasswordDifferentFromOld(newPassword, oldPassword) {
        this.ensurePasswordExists(newPassword);
        if (newPassword === oldPassword) {
            throw new Error("New password must be different from current password");
        }
    },

    // --- BankAccount Validation (matches BankAccountValidation.java) ---
    ensureBankAccountPresent(bankAccount) {
        if (bankAccount === null || bankAccount === undefined) {
            throw new Error("Bank Account is null");
        }
    },

    validateBankAccountType(bankAccountType) {
        if (bankAccountType === null || bankAccountType === undefined || bankAccountType.trim() === "") {
            throw new Error("Bank Account Type is null or empty");
        }
        if (!BANK_ACCOUNT_TYPES.includes(bankAccountType.toUpperCase())) {
            throw new Error(`Invalid Bank Account Type: ${bankAccountType}`);
        }
    },

    // --- Currency Validation (matches CurrencyValidation.java) ---
    validateCurrency(currency) {
        if (currency === null || currency === undefined || currency.trim() === "") {
            throw new Error("Currency is null or empty");
        }
        if (!CURRENCIES.includes(currency.toUpperCase())) {
            throw new Error(`Invalid Currency: ${currency}`);
        }
    },

    // --- Date Validation (matches DateValidation.java) ---
    validateDate(date) {
        if (date === null || date === undefined) {
            throw new Error("Date is null");
        }
    },

    // --- Client Validation (matches ClientValidation.java) ---
    ensureClientPresent(client) {
        if (client === null || client === undefined) {
            throw new Error("Client is null");
        }
    },

    // --- Employee Validation (matches EmployeeValidation.java) ---
    ensureEmployeePresent(employee) {
        if (employee === null || employee === undefined) {
            throw new Error("Employee is null");
        }
    },

    // --- UserAccount Validation (matches UserAccountValidation.java) ---
    ensureUserAccountPresent(userAccount) {
        if (userAccount === null || userAccount === undefined) {
            throw new Error("UserAccount is null");
        }
    },

    ensureFirstNamePresent(firstName) {
        if (firstName === null || firstName === undefined || firstName.trim() === "") {
            throw new Error("First Name is null or empty");
        }
    },

    ensureLastNamePresent(lastName) {
        if (lastName === null || lastName === undefined || lastName.trim() === "") {
            throw new Error("Last Name is null or empty");
        }
    },

    ensureUserAccountRoleIsCorrect(role) {
        if (role === null || role === undefined) {
            throw new Error("User Account Role is null");
        }
        if (!USER_ACCOUNT_ROLES.includes(role.toUpperCase())) {
            throw new Error(`Invalid User Account Role: ${role}`);
        }
    },

    // --- Transaction Validation (matches TransactionValidation.java) ---
    validateTransactionAccounts(bankAccountIdFrom, bankAccountIdTo) {
        if (bankAccountIdFrom === bankAccountIdTo) {
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
            throw new Error(`Insufficient balance on account: ${accountNumber}`);
        }
    },

    // --- SaveBankAccountRequest Validation ---
    validateSaveBankAccountRequest(request) {
        this.ensureIdPresent(request.clientId);
        this.validateBankAccountType(request.bankAccountType);
        this.validateCurrency(request.bankAccountCurrency);
    },

    // --- Client Save/Update Validation ---
    validateClientData(client) {
        this.ensureClientPresent(client);
        this.ensureFirstNamePresent(client.firstName);
        this.ensureLastNamePresent(client.lastName);
    },

    // --- Employee Save/Update Validation ---
    validateEmployeeData(employee) {
        this.ensureEmployeePresent(employee);
        this.ensureFirstNamePresent(employee.firstName);
        this.ensureLastNamePresent(employee.lastName);
    },

    // --- Password Change Validation ---
    validatePasswordChange(passwordObj) {
        this.ensurePasswordExists(passwordObj.currentPassword);
        this.ensurePasswordMeetsRequirements(passwordObj.newPassword);
    }
};

// ============================================================
// AJAX HELPER
// ============================================================

function ajaxRequest(url, method, data) {
    const options = {
        url: url,
        type: method,
        dataType: "json",
        contentType: "application/json; charset=UTF-8"
    };
    if (data && (method === "POST" || method === "PUT")) {
        options.data = JSON.stringify(data);
    }
    return $.ajax(options);
}

// ============================================================
// NOTIFICATION HELPER
// ============================================================

function showNotification(message, type = "info") {
    const existing = $(".notification");
    if (existing.length) existing.remove();

    const bgColors = {
        success: "#059669",
        error: "#dc2626",
        info: "#2563eb",
        warning: "#d97706"
    };

    const $notification = $(`
        <div class="notification" style="
            position: fixed; top: 20px; right: 20px; z-index: 9999;
            padding: 14px 24px; border-radius: 8px; color: white;
            background: ${bgColors[type] || bgColors.info};
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            font-family: 'Inter', sans-serif; font-size: 14px;
            animation: slideIn 0.3s ease-out;
        ">${message}</div>
    `);

    $("body").append($notification);
    setTimeout(() => $notification.fadeOut(300, function () { $(this).remove(); }), 4000);
}

// ============================================================
// FORMAT HELPERS
// ============================================================

function formatCurrency(amount, currency = "EUR") {
    const symbols = { EUR: "€", USD: "$", GBP: "£" };
    const symbol = symbols[currency] || currency;
    const num = parseFloat(amount) || 0;
    return symbol + num.toLocaleString("en-IE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(dateStr) {
    if (!dateStr) return "N/A";
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-IE", { year: "numeric", month: "short", day: "numeric" });
}

function maskAccountNumber(number) {
    if (!number || number.length < 4) return number || "N/A";
    return "••••" + number.slice(-4);
}

// ============================================================
// BANK ACCOUNT SERVICE (BankAccountController endpoints)
// ============================================================

const BankAccountService = {

    /**
     * POST /api/bank_account
     * Create a new bank account
     */
    save(clientId, bankAccountType, bankAccountCurrency) {
        const request = { clientId, bankAccountType, bankAccountCurrency };
        try {
            Validation.validateSaveBankAccountRequest(request);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(API.BANK_ACCOUNT, "POST", request)
            .done(function (data) {
                showNotification("Bank account created successfully", "success");
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to create bank account";
                showNotification(msg, "error");
            });
    },

    /**
     * GET /api/bank_account/{id}
     * Find bank account by ID
     */
    findById(id) {
        try {
            Validation.ensureIdPresent(id);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(`${API.BANK_ACCOUNT}/${id}`, "GET")
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Bank account not found";
                showNotification(msg, "error");
            });
    },

    /**
     * GET /api/bank_account
     * Find all bank accounts
     */
    findAll() {
        return ajaxRequest(API.BANK_ACCOUNT, "GET")
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to load bank accounts";
                showNotification(msg, "error");
            });
    }
};

// ============================================================
// CLIENT SERVICE (ClientController endpoints)
// ============================================================

const ClientService = {

    /**
     * POST /api/client
     * Create a new client
     */
    save(client) {
        try {
            Validation.validateClientData(client);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(API.CLIENT, "POST", client)
            .done(function () {
                showNotification("Client created successfully", "success");
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to create client";
                showNotification(msg, "error");
            });
    },

    /**
     * PUT /api/client/{id}
     * Update an existing client
     */
    updateById(id, client) {
        try {
            Validation.ensureIdPresent(id);
            Validation.validateClientData(client);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(`${API.CLIENT}/${id}`, "PUT", client)
            .done(function () {
                showNotification("Client updated successfully", "success");
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to update client";
                showNotification(msg, "error");
            });
    },

    /**
     * GET /api/client/{id}
     * Find client by ID
     */
    findById(id) {
        try {
            Validation.ensureIdPresent(id);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(`${API.CLIENT}/${id}`, "GET")
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Client not found";
                showNotification(msg, "error");
            });
    },

    /**
     * GET /api/client
     * Find all clients
     */
    findAll() {
        return ajaxRequest(API.CLIENT, "GET")
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to load clients";
                showNotification(msg, "error");
            });
    },

    /**
     * DELETE /api/client/{id}
     * Delete client by ID
     */
    deleteById(id) {
        try {
            Validation.ensureIdPresent(id);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return $.ajax({
            url: `${API.CLIENT}/${id}`,
            type: "DELETE"
        })
            .done(function () {
                showNotification("Client deleted successfully", "success");
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to delete client";
                showNotification(msg, "error");
            });
    },

    /**
     * PUT /api/client/{clientId}/password
     * Update client password
     */
    updatePassword(clientId, passwordObj) {
        try {
            Validation.ensureIdPresent(clientId);
            Validation.validatePasswordChange(passwordObj);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(`${API.CLIENT}/${clientId}/password`, "PUT", passwordObj)
            .done(function () {
                showNotification("Password updated successfully", "success");
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to update password";
                showNotification(msg, "error");
            });
    }
};

// ============================================================
// EMPLOYEE SERVICE (EmployeeController endpoints)
// ============================================================

const EmployeeService = {

    /**
     * POST /api/employee
     * Create a new employee
     */
    save(employee) {
        try {
            Validation.validateEmployeeData(employee);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(API.EMPLOYEE, "POST", employee)
            .done(function () {
                showNotification("Employee created successfully", "success");
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to create employee";
                showNotification(msg, "error");
            });
    },

    /**
     * PUT /api/employee/{id}
     * Update an existing employee
     */
    updateById(id, employee) {
        try {
            Validation.ensureIdPresent(id);
            Validation.validateEmployeeData(employee);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(`${API.EMPLOYEE}/${id}`, "PUT", employee)
            .done(function () {
                showNotification("Employee updated successfully", "success");
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to update employee";
                showNotification(msg, "error");
            });
    },

    /**
     * GET /api/employee/{id}
     * Find employee by ID
     */
    findById(id) {
        try {
            Validation.ensureIdPresent(id);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(`${API.EMPLOYEE}/${id}`, "GET")
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Employee not found";
                showNotification(msg, "error");
            });
    },

    /**
     * GET /api/employee
     * Find all employees
     */
    findAll() {
        return ajaxRequest(API.EMPLOYEE, "GET")
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to load employees";
                showNotification(msg, "error");
            });
    },

    /**
     * DELETE /api/employee/{id}
     * Delete employee by ID
     */
    deleteById(id) {
        try {
            Validation.ensureIdPresent(id);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return $.ajax({
            url: `${API.EMPLOYEE}/${id}`,
            type: "DELETE"
        })
            .done(function () {
                showNotification("Employee deleted successfully", "success");
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to delete employee";
                showNotification(msg, "error");
            });
    },

    /**
     * PUT /api/employee/{employeeId}/password
     * Update employee password
     */
    updatePassword(employeeId, passwordObj) {
        try {
            Validation.ensureIdPresent(employeeId);
            Validation.validatePasswordChange(passwordObj);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(`${API.EMPLOYEE}/${employeeId}/password`, "PUT", passwordObj)
            .done(function () {
                showNotification("Password updated successfully", "success");
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to update password";
                showNotification(msg, "error");
            });
    }
};

// ============================================================
// USER ACCOUNT SERVICE (UserAccountController endpoints)
// ============================================================

const UserAccountService = {

    /**
     * GET /api/user_account/{id}
     * Find user account by ID
     */
    findById(id) {
        try {
            Validation.ensureIdPresent(id);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }
        return ajaxRequest(`${API.USER_ACCOUNT}/${id}`, "GET")
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "User account not found";
                showNotification(msg, "error");
            });
    },

    /**
     * GET /api/user_account
     * Find all user accounts
     */
    findAll() {
        return ajaxRequest(API.USER_ACCOUNT, "GET")
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Failed to load user accounts";
                showNotification(msg, "error");
            });
    }
};

// ============================================================
// DASHBOARD DOM RENDERING
// ============================================================

const DashboardRenderer = {

    /**
     * Render account cards in .accounts-section
     * Expects array of bank account objects with: type, number, balance, currency, interestRate, creditLimit
     */
    renderAccountCards(accounts) {
        const $section = $(".accounts-section");
        $section.empty();

        if (!accounts || accounts.length === 0) {
            $section.html('<p style="color: #94a3b8; padding: 20px;">No accounts found.</p>');
            return;
        }

        accounts.forEach(function (account, index) {
            const isPrimary = index === 0;
            const isCredit = (account.bankAccountType || account.type || "").toUpperCase() === "CREDIT";
            const currency = (account.currency || account.bankAccountCurrency || "EUR").toUpperCase();
            const balance = parseFloat(account.balance) || 0;
            const formattedBalance = formatCurrency(balance, currency);
            const displayNumber = maskAccountNumber(account.number || account.accountNumber);
            const typeName = (account.bankAccountType || account.type || "Account")
                .replace(/_/g, " ")
                .replace(/\b\w/g, c => c.toUpperCase());

            let footerStatLabel, footerStatValue;
            if (isCredit) {
                footerStatLabel = "Credit Limit";
                footerStatValue = formatCurrency(account.creditLimit || 0, currency);
            } else if (typeName.toUpperCase().includes("SAVING")) {
                footerStatLabel = "Interest Rate";
                footerStatValue = (account.interestRate || "2.5") + "% APY";
            } else {
                footerStatLabel = "Available";
                footerStatValue = formattedBalance;
            }

            const cardHtml = `
                <div class="account-card${isPrimary ? ' primary' : ''}" data-account-id="${account.id}">
                    <div class="account-header">
                        <div>
                            <p class="account-label">${typeName}</p>
                            <p class="account-number">${displayNumber}</p>
                        </div>
                        ${isPrimary ? '<div class="account-badge">Primary</div>' : ''}
                    </div>
                    <div class="account-balance${isCredit ? ' credit' : ''}">
                        <span class="currency">${{ EUR: "€", USD: "$", GBP: "£" }[currency] || currency}</span>
                        <span class="amount">${balance.toLocaleString("en-IE", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                    </div>
                    <div class="account-footer">
                        <div class="account-stat">
                            <span class="stat-label">${footerStatLabel}</span>
                            <span class="stat-value">${footerStatValue}</span>
                        </div>
                        <button class="btn-link" onclick="DashboardRenderer.onViewAccountDetails(${account.id})">View Details →</button>
                    </div>
                </div>
            `;
            $section.append(cardHtml);
        });
    },

    /**
     * Render transactions in the transactions table tbody
     * Expects array of transaction objects with: description, category, date, accountNumber, amount, status
     */
    renderTransactions(transactions) {
        const $tbody = $(".transactions-table tbody");
        $tbody.empty();

        if (!transactions || transactions.length === 0) {
            $tbody.html('<tr><td colspan="5" style="text-align:center; color:#94a3b8; padding:24px;">No recent transactions.</td></tr>');
            return;
        }

        const iconColors = {
            "Income": "green",
            "Online Shopping": "blue",
            "Utilities": "purple",
            "Housing": "orange",
            "Food & Dining": "blue",
            "Transfer": "blue"
        };

        transactions.forEach(function (tx) {
            const amount = parseFloat(tx.amount) || 0;
            const isPositive = amount >= 0;
            const iconColor = iconColors[tx.category] || "blue";
            const formattedAmount = (isPositive ? "+" : "-") + formatCurrency(Math.abs(amount), tx.currency || "EUR");
            const statusClass = (tx.status || "completed").toLowerCase();

            const rowHtml = `
                <tr>
                    <td>
                        <div class="transaction-desc">
                            <div class="transaction-icon ${iconColor}">
                                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                                    <circle cx="10" cy="10" r="8"/>
                                </svg>
                            </div>
                            <div>
                                <p class="transaction-name">${escapeHtml(tx.description || tx.name || "Transaction")}</p>
                                <p class="transaction-category">${escapeHtml(tx.category || "General")}</p>
                            </div>
                        </div>
                    </td>
                    <td>${formatDate(tx.date || tx.createdAt)}</td>
                    <td>${escapeHtml(tx.accountLabel || maskAccountNumber(tx.accountNumber))}</td>
                    <td class="amount ${isPositive ? 'positive' : 'negative'}">${formattedAmount}</td>
                    <td><span class="status-badge ${statusClass}">${capitalize(statusClass)}</span></td>
                </tr>
            `;
            $tbody.append(rowHtml);
        });
    },

    /**
     * Render welcome section with user info
     */
    renderWelcome(user) {
        if (user && user.firstName) {
            $(".page-title").text(`Welcome back, ${escapeHtml(user.firstName)}`);
        }
        if (user && user.lastName) {
            const initials = (user.firstName || "").charAt(0) + (user.lastName || "").charAt(0);
            $(".user-avatar").text(initials.toUpperCase());
            $(".user-name").text(`${user.firstName} ${user.lastName.charAt(0)}.`);
        }
    },

    /**
     * Callback for "View Details" button on account cards
     */
    onViewAccountDetails(accountId) {
        BankAccountService.findById(accountId)
            .done(function (account) {
                console.log("Account details:", account);
                showNotification(`Viewing account ${maskAccountNumber(account.number || account.accountNumber)}`, "info");
            });
    }
};

// ============================================================
// TRANSACTION TRANSFER HELPER
// ============================================================

const TransferService = {

    /**
     * Validate and initiate a transfer between two accounts
     * This performs client-side validation matching TransactionValidation.java
     * The actual endpoint would be your transaction API
     */
    validateAndTransfer(fromAccountId, toAccountId, amount, fromAccountBalance, fromAccountNumber) {
        try {
            Validation.ensureIdPresent(fromAccountId);
            Validation.ensureIdPresent(toAccountId);
            Validation.validateTransactionAccounts(fromAccountId, toAccountId);
            Validation.validateAmount(amount);
            Validation.validateSufficientBalance(fromAccountBalance, amount, fromAccountNumber);
        } catch (e) {
            showNotification(e.message, "error");
            return $.Deferred().reject(e.message).promise();
        }

        return ajaxRequest("/api/transaction", "POST", {
            bankAccountIdFrom: fromAccountId,
            bankAccountIdTo: toAccountId,
            amount: amount
        })
            .done(function () {
                showNotification("Transfer completed successfully", "success");
                loadAccountBalances(); // refresh accounts
                loadTransactions();    // refresh transactions
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Transfer failed";
                showNotification(msg, "error");
            });
    }
};

// ============================================================
// UTILITY FUNCTIONS
// ============================================================

function escapeHtml(str) {
    if (!str) return "";
    const div = document.createElement("div");
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
}

function capitalize(str) {
    if (!str) return "";
    return str.charAt(0).toUpperCase() + str.slice(1);
}

// ============================================================
// DASHBOARD DATA LOADERS
// ============================================================

/**
 * Load and render recent transactions
 */
function loadTransactions() {
    ajaxRequest(API.TRANSACTIONS_RECENT, "GET")
        .done(function (data) {
            DashboardRenderer.renderTransactions(data);
        })
        .fail(function (jqxhr, textStatus, errorThrown) {
            console.error("Failed to load transactions:", errorThrown);
        });
}

/**
 * Load and render account balances / cards
 * Falls back to /api/bank_account if /api/accounts/summary is not available
 */
function loadAccountBalances() {
    ajaxRequest(API.ACCOUNTS_SUMMARY, "GET")
        .done(function (data) {
            DashboardRenderer.renderAccountCards(data);
        })
        .fail(function () {
            // Fallback: try loading from bank account endpoint
            BankAccountService.findAll()
                .done(function (data) {
                    DashboardRenderer.renderAccountCards(data);
                });
        });
}

/**
 * Load current user info and update the welcome section
 */
function loadCurrentUser() {
    ajaxRequest("/api/auth/me", "GET")
        .done(function (user) {
            DashboardRenderer.renderWelcome(user);
        })
        .fail(function () {
            // User info not available - keep default from template
            console.log("User info endpoint not available, using template defaults");
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

        const page = $(this).text().trim().toLowerCase();
        switch (page) {
            case "dashboard":
                loadAccountBalances();
                loadTransactions();
                break;
            case "accounts":
                BankAccountService.findAll().done(function (data) {
                    DashboardRenderer.renderAccountCards(data);
                });
                break;
            case "transactions":
                loadTransactions();
                break;
            case "transfer":
                showNotification("Transfer page - coming soon", "info");
                break;
        }
    });
}

// ============================================================
// QUICK ACTION HANDLERS
// ============================================================

function initQuickActions() {
    $(".action-card").on("click", function () {
        const action = $(this).find(".action-label").text().trim();
        switch (action) {
            case "Transfer Money":
                showNotification("Opening transfer form...", "info");
                break;
            case "Deposit":
                showNotification("Opening deposit form...", "info");
                break;
            case "Pay Bills":
                showNotification("Opening bill payment...", "info");
                break;
            case "Analytics":
                showNotification("Loading analytics...", "info");
                break;
        }
    });
}

// ============================================================
// INITIALIZATION
// ============================================================

$(document).ready(function () {
    // Load dashboard data
    loadCurrentUser();
    loadAccountBalances();
    loadTransactions();

    // Set up event handlers
    initNavigation();
    initQuickActions();

    // "New Transaction" button
    $(".btn-primary").on("click", function () {
        showNotification("Opening new transaction form...", "info");
    });

    // "View All" transactions button
    $(".transactions-section .btn-secondary").on("click", function () {
        loadTransactions();
    });

    console.log("SimplyBank Dashboard initialized");
});
