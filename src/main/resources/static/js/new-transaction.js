/**
 * SimplyBank — New Transaction Page Controller
 *
 * 3-step wizard:
 *   1. Choose transaction type
 *   2. Enter details (accounts, amount) with live validation
 *   3. Review & confirm -> POST to API
 *
 * Shared utilities (ajax, formatCurrency, escapeHtml, etc.) are in common.js.
 *
 * Client-side validation mirrors the backend:
 *   TransactionValidation, BankAccountValidation,
 *   CurrencyValidation, IdValidation
 */

// ============================================================
// CONFIG
// ============================================================

var TxAPI = {
    AUTH_ME:       "/api/auth/me",
    BANK_ACCOUNT:  "/api/bank_account",
    TRANSFER:      "/api/transaction/transfer",
    DEPOSIT:       "/api/transaction/deposit",
    WITHDRAW:      "/api/transaction/withdraw"
};

var VALID_CURRENCIES = ["EUR", "USD", "GBP"];
var VALID_TX_TYPES   = ["TRANSFER", "DEPOSIT", "WITHDRAWAL", "PAYMENT"];

// ============================================================
// STATE
// ============================================================

var FormState = {
    currentStep: 1,
    transactionType: null,
    fromAccountId: null,
    toAccountId: null,
    externalAccount: null,
    amount: null,
    currency: "EUR",
    description: "",
    category: "",
    accounts: [],
    currentUserId: null,
    submitting: false
};

// ============================================================
// VALIDATION  (mirrors Java backend validators)
// ============================================================

var TxValidation = {

    errors: {},

    clearAll: function () {
        this.errors = {};
        $(".field-error").text("");
        $(".field-input, .field-select").removeClass("invalid valid");
        $(".amount-input-wrap").removeClass("invalid");
        $("#typeError").text("");
    },

    addError: function (field, message) {
        this.errors[field] = message;
    },

    hasErrors: function () {
        return Object.keys(this.errors).length > 0;
    },

    showErrors: function () {
        var self = this;
        Object.keys(this.errors).forEach(function (field) {
            $("#" + field + "Error").text(self.errors[field]);
            var $el = $("#" + field);
            if ($el.length) $el.addClass("invalid").removeClass("valid");
        });
    },

    markValid: function (field) {
        var $el = $("#" + field);
        if ($el.length) $el.addClass("valid").removeClass("invalid");
        $("#" + field + "Error").text("");
    },

    // --- IdValidation.java ---
    ensureIdPresent: function (id, field, label) {
        if (id === null || id === undefined || id === "") {
            this.addError(field, label + " is required");
            return false;
        }
        return true;
    },

    // --- TransactionValidation.java ---
    validateTransactionAccounts: function (fromId, toId) {
        if (fromId && toId && String(fromId) === String(toId)) {
            this.addError("toAccount", "Transaction cannot have same from and to accounts");
            return false;
        }
        return true;
    },

    validateAmount: function (amount) {
        if (amount === null || amount === undefined || amount === "") {
            this.addError("amount", "Amount is required");
            return false;
        }
        var num = parseFloat(amount);
        if (isNaN(num)) {
            this.addError("amount", "Amount must be a valid number");
            return false;
        }
        if (num <= 0) {
            this.addError("amount", "Amount must be greater than zero");
            return false;
        }
        if (num > 999999999) {
            this.addError("amount", "Amount exceeds maximum limit");
            return false;
        }
        return true;
    },

    validateSufficientBalance: function (account, amount) {
        if (!account) return true;
        var balance = parseFloat(account.balance) || 0;
        var amt = parseFloat(amount) || 0;
        if (balance < amt) {
            this.addError("amount", "Insufficient balance on account " + maskAccount(account.number || account.accountNumber) + ". Available: " + formatCurrency(balance, account.currency || "EUR"));
            return false;
        }
        return true;
    },

    // --- CurrencyValidation.java ---
    validateCurrency: function (currency) {
        if (!currency || currency.trim() === "") {
            this.addError("currency", "Currency is required");
            return false;
        }
        if (VALID_CURRENCIES.indexOf(currency.toUpperCase()) === -1) {
            this.addError("currency", "Invalid currency: " + currency);
            return false;
        }
        return true;
    },

    // --- Transaction Type ---
    validateTransactionType: function (type) {
        if (!type) {
            this.addError("type", "Please select a transaction type");
            return false;
        }
        if (VALID_TX_TYPES.indexOf(type) === -1) {
            this.addError("type", "Invalid transaction type");
            return false;
        }
        return true;
    },

    // --- External account number (for PAYMENT) ---
    validateExternalAccount: function (value) {
        if (!value || value.trim() === "") {
            this.addError("externalAccount", "Recipient account number is required");
            return false;
        }
        var cleaned = value.replace(/\s/g, "");
        if (cleaned.length < 8) {
            this.addError("externalAccount", "Account number is too short");
            return false;
        }
        if (!/^[A-Za-z0-9]+$/.test(cleaned)) {
            this.addError("externalAccount", "Account number contains invalid characters");
            return false;
        }
        return true;
    },

    // ---- Aggregate: validate Step 2 ----
    validateStep2: function (formState) {
        this.clearAll();
        var type = formState.transactionType;
        var valid = true;

        // From Account — required for TRANSFER, WITHDRAWAL, PAYMENT
        if (type !== "DEPOSIT") {
            if (!this.ensureIdPresent(formState.fromAccountId, "fromAccount", "Source account")) {
                valid = false;
            }
        }

        // To Account — required for TRANSFER, DEPOSIT
        if (type === "TRANSFER" || type === "DEPOSIT") {
            if (!this.ensureIdPresent(formState.toAccountId, "toAccount", "Destination account")) {
                valid = false;
            }
        }

        // External account — required for PAYMENT
        if (type === "PAYMENT") {
            if (!this.validateExternalAccount(formState.externalAccount)) {
                valid = false;
            }
        }

        // Same-account check for TRANSFER
        if (type === "TRANSFER" && formState.fromAccountId && formState.toAccountId) {
            if (!this.validateTransactionAccounts(formState.fromAccountId, formState.toAccountId)) {
                valid = false;
            }
        }

        // Amount
        if (!this.validateAmount(formState.amount)) {
            valid = false;
        }

        // Currency
        if (!this.validateCurrency(formState.currency)) {
            valid = false;
        }

        // Sufficient balance (for outgoing types)
        if (valid && (type === "TRANSFER" || type === "WITHDRAWAL" || type === "PAYMENT")) {
            var fromAcct = formState.accounts.find(function (a) { return String(a.id) === String(formState.fromAccountId); });
            if (!this.validateSufficientBalance(fromAcct, formState.amount)) {
                valid = false;
            }
        }

        if (!valid) this.showErrors();
        return valid;
    }
};

// ============================================================
// STEPPER
// ============================================================

function updateStepper(step) {
    $(".step").each(function () {
        var s = parseInt($(this).data("step"));
        $(this).toggleClass("active", s === step)
               .toggleClass("done", s < step);
    });
    $(".step-line").each(function (i) {
        $(this).toggleClass("done", (i + 1) < step);
    });
}

function goToStep(step) {
    FormState.currentStep = step;
    updateStepper(step);

    $("#step1, #step2, #step3, #stepSuccess").addClass("hidden");
    if (step === 1) $("#step1").removeClass("hidden");
    if (step === 2) $("#step2").removeClass("hidden");
    if (step === 3) {
        renderConfirmation();
        $("#step3").removeClass("hidden");
    }
    if (step === 4) $("#stepSuccess").removeClass("hidden");

    var $card = $(".form-card:not(.hidden)");
    $card.css("animation", "none");
    if ($card[0]) $card[0].offsetHeight; // reflow
    $card.css("animation", "");

    window.scrollTo({ top: 0, behavior: "smooth" });
}

// ============================================================
// STEP 1 — TYPE SELECTION
// ============================================================

function initTypeSelection() {
    $(".type-option").on("click", function () {
        $(".type-option").removeClass("selected");
        $(this).addClass("selected");
        FormState.transactionType = $(this).data("type");
        $("#typeError").text("");
        $("#btnToStep2").prop("disabled", false);
    });

    $("#btnToStep2").on("click", function () {
        TxValidation.clearAll();
        if (!TxValidation.validateTransactionType(FormState.transactionType)) {
            $("#typeError").text(TxValidation.errors.type || "Please select a type");
            return;
        }
        configureStep2ForType(FormState.transactionType);
        goToStep(2);
    });
}

// ============================================================
// STEP 2 — DETAILS  (field visibility per type)
// ============================================================

function configureStep2ForType(type) {
    var titles = {
        TRANSFER:   "Transfer Details",
        DEPOSIT:    "Deposit Details",
        WITHDRAWAL: "Withdrawal Details",
        PAYMENT:    "Payment Details"
    };
    var descs = {
        TRANSFER:   "Select source and destination accounts",
        DEPOSIT:    "Select the account to deposit into",
        WITHDRAWAL: "Select the account to withdraw from",
        PAYMENT:    "Enter payment recipient and amount"
    };

    $("#step2Title").text(titles[type] || "Transaction Details");
    $("#step2Desc").text(descs[type] || "Enter the details below");

    // Reset all labels to defaults first
    $("#fromAccountGroup .field-label").html('From Account <span class="req">*</span>');
    $("#toAccountGroup .field-label").html('To Account <span class="req">*</span>');

    // Show/hide and relabel fields based on type
    switch (type) {
        case "TRANSFER":
            $("#fromAccountGroup").removeClass("hidden");
            $("#toAccountGroup").removeClass("hidden");
            $("#externalAccountGroup").addClass("hidden");
            break;
        case "DEPOSIT":
            $("#fromAccountGroup").addClass("hidden");
            $("#toAccountGroup").removeClass("hidden");
            $("#toAccountGroup .field-label").html('Deposit Into <span class="req">*</span>');
            $("#externalAccountGroup").addClass("hidden");
            break;
        case "WITHDRAWAL":
            $("#fromAccountGroup").removeClass("hidden");
            $("#fromAccountGroup .field-label").html('Withdraw From <span class="req">*</span>');
            $("#toAccountGroup").addClass("hidden");
            $("#externalAccountGroup").addClass("hidden");
            break;
        case "PAYMENT":
            $("#fromAccountGroup").removeClass("hidden");
            $("#fromAccountGroup .field-label").html('Pay From <span class="req">*</span>');
            $("#toAccountGroup").addClass("hidden");
            $("#externalAccountGroup").removeClass("hidden");
            break;
    }

    TxValidation.clearAll();
}

// ============================================================
// LOAD USER ACCOUNTS
// ============================================================

function loadAccounts() {
    ajax(TxAPI.AUTH_ME, "GET")
        .done(function (user) {
            FormState.currentUserId = user.id;
            renderUserHeader(user);
            fetchAccounts(user.id);
        })
        .fail(function () {
            fetchAccounts(null);
        });
}

function fetchAccounts(userId) {
    var url = userId
        ? TxAPI.BANK_ACCOUNT + "?clientId=" + userId
        : TxAPI.BANK_ACCOUNT;

    ajax(url, "GET")
        .done(function (accounts) {
            FormState.accounts = Array.isArray(accounts) ? accounts : [];
            populateAccountDropdowns(FormState.accounts);
        })
        .fail(function () {
            ajax(TxAPI.BANK_ACCOUNT, "GET")
                .done(function (accounts) {
                    FormState.accounts = Array.isArray(accounts) ? accounts : [];
                    populateAccountDropdowns(FormState.accounts);
                })
                .fail(function () {
                    notify("Could not load accounts", "error");
                });
        });
}

function populateAccountDropdowns(accounts) {
    var $from = $("#fromAccount");
    var $to   = $("#toAccount");

    $from.find("option:not(:first)").remove();
    $to.find("option:not(:first)").remove();

    accounts.forEach(function (acct) {
        var label = buildAccountLabel(acct);
        var opt = '<option value="' + escapeHtml(String(acct.id)) + '">' + escapeHtml(label) + '</option>';
        $from.append(opt);
        $to.append(opt);
    });
}

function buildAccountLabel(acct) {
    var type = (acct.bankAccountType || acct.type || "Account")
        .replace(/_/g, " ")
        .replace(/\b\w/g, function (c) { return c.toUpperCase(); });
    var num  = maskAccount(acct.number || acct.accountNumber);
    var bal  = formatCurrency(acct.balance, acct.currency || acct.bankAccountCurrency || "EUR");
    return type + " " + num + "  \u2014  " + bal;
}

// ============================================================
// LIVE FIELD EVENTS
// ============================================================

function initFieldEvents() {

    $("#fromAccount").on("change", function () {
        var id = $(this).val();
        FormState.fromAccountId = id || null;
        var acct = FormState.accounts.find(function (a) { return String(a.id) === String(id); });
        if (acct) {
            var bal = formatCurrency(acct.balance, acct.currency || "EUR");
            $("#fromBalance").html('Available balance: <span class="bal-num">' + bal + '</span>');
            TxValidation.markValid("fromAccount");
        } else {
            $("#fromBalance").text("");
        }
        liveValidateSameAccount();
    });

    $("#toAccount").on("change", function () {
        FormState.toAccountId = $(this).val() || null;
        TxValidation.markValid("toAccount");
        liveValidateSameAccount();
    });

    $("#externalAccount").on("input", function () {
        FormState.externalAccount = $(this).val();
    });

    $("#currency").on("change", function () {
        FormState.currency = $(this).val();
    });

    $("#amount").on("input", function () {
        var raw = $(this).val().replace(/[^0-9.,]/g, "");
        var parts = raw.split(/[.,]/);
        if (parts.length > 2) {
            raw = parts[0] + "." + parts.slice(1).join("");
        }
        $(this).val(raw);
        FormState.amount = raw;

        if (raw !== "") {
            var num = parseFloat(raw);
            if (!isNaN(num) && num > 0) {
                $(".amount-input-wrap").removeClass("invalid");
                $("#amountError").text("");
            }
        }
    });

    $("#amount").on("blur", function () {
        var num = parseFloat($(this).val());
        if (!isNaN(num) && num > 0) {
            $(this).val(num.toFixed(2));
            FormState.amount = num.toFixed(2);
        }
    });

    $("#description").on("input", function () {
        FormState.description = $(this).val();
    });

    $("#category").on("change", function () {
        FormState.category = $(this).val();
    });
}

function liveValidateSameAccount() {
    if (FormState.transactionType === "TRANSFER" && FormState.fromAccountId && FormState.toAccountId) {
        if (String(FormState.fromAccountId) === String(FormState.toAccountId)) {
            $("#toAccountError").text("Transaction cannot have same from and to accounts");
            $("#toAccount").addClass("invalid").removeClass("valid");
        } else {
            $("#toAccountError").text("");
            TxValidation.markValid("toAccount");
        }
    }
}

// ============================================================
// STEP 2 -> 3  (validate & move)
// ============================================================

function initStep2Actions() {
    $("#btnToStep3").on("click", function () {
        FormState.fromAccountId   = $("#fromAccount").val() || null;
        FormState.toAccountId     = $("#toAccount").val()   || null;
        FormState.externalAccount = $("#externalAccount").val() || null;
        FormState.amount          = $("#amount").val() || null;
        FormState.currency        = $("#currency").val();
        FormState.description     = $("#description").val();
        FormState.category        = $("#category").val();

        if (TxValidation.validateStep2(FormState)) {
            goToStep(3);
        } else {
            if (TxValidation.errors.amount) {
                $(".amount-input-wrap").addClass("invalid");
            }
        }
    });

    $("#btnBackToStep1").on("click", function () {
        goToStep(1);
    });
}

// ============================================================
// STEP 3 — CONFIRMATION RENDER
// ============================================================

function renderConfirmation() {
    var type = FormState.transactionType;
    var fromAcct = FormState.accounts.find(function (a) { return String(a.id) === String(FormState.fromAccountId); });
    var toAcct   = FormState.accounts.find(function (a) { return String(a.id) === String(FormState.toAccountId); });

    var rows = [];

    rows.push({ label: "Transaction Type", value: '<span class="confirm-type-badge ' + type + '">' + capitalize(type) + '</span>', isHtml: true });

    if (type !== "DEPOSIT") {
        rows.push({ label: "From", value: fromAcct ? buildAccountLabel(fromAcct) : "\u2014" });
    }

    if (type === "TRANSFER" || type === "DEPOSIT") {
        var lbl = type === "DEPOSIT" ? "Deposit Into" : "To";
        rows.push({ label: lbl, value: toAcct ? buildAccountLabel(toAcct) : "\u2014" });
    }

    if (type === "PAYMENT") {
        rows.push({ label: "Recipient Account", value: FormState.externalAccount || "\u2014" });
    }

    rows.push({ label: "Currency", value: FormState.currency.toUpperCase() });
    rows.push({ label: "Amount", value: formatCurrency(FormState.amount, FormState.currency), isAmount: true, highlight: true });

    if (FormState.description) {
        rows.push({ label: "Description", value: FormState.description });
    }
    if (FormState.category) {
        rows.push({ label: "Category", value: FormState.category });
    }

    var html = "";
    rows.forEach(function (r) {
        var cls = r.highlight ? " highlight" : "";
        var valCls = r.isAmount ? "confirm-value amount-large" : "confirm-value";
        var val = r.isHtml ? r.value : escapeHtml(r.value);
        html += '<div class="confirm-row' + cls + '">' +
                    '<span class="confirm-label">' + escapeHtml(r.label) + '</span>' +
                    '<span class="' + valCls + '">' + val + '</span>' +
                '</div>';
    });

    $("#confirmSummary").html(html);
}

// ============================================================
// STEP 3 — SUBMIT
// ============================================================

function initSubmit() {

    $("#btnBackToStep2").on("click", function () {
        goToStep(2);
    });

    $("#btnSubmit").on("click", function () {
        if (FormState.submitting) return;
        FormState.submitting = true;

        var $btn = $(this);
        var originalText = $btn.html();
        $btn.prop("disabled", true).html('<div class="spinner-sm"></div> Processing\u2026');

        var payload = buildPayload();
        var url = getTransactionEndpoint(FormState.transactionType);

        ajax(url, "POST", payload)
            .done(function () {
                var typeName = capitalize(FormState.transactionType);
                $("#successMessage").text("Your " + typeName.toLowerCase() + " of " + formatCurrency(FormState.amount, FormState.currency) + " has been processed successfully.");
                goToStep(4);
            })
            .fail(function (jqxhr) {
                var msg = (jqxhr.responseJSON && jqxhr.responseJSON.message) || "Transaction failed. Please try again.";
                notify(msg, "error");
                $btn.prop("disabled", false).html(originalText);
            })
            .always(function () {
                FormState.submitting = false;
            });
    });
}

function getTransactionEndpoint(type) {
    switch (type) {
        case "TRANSFER":   return TxAPI.TRANSFER;
        case "DEPOSIT":    return TxAPI.DEPOSIT;
        case "WITHDRAWAL": return TxAPI.WITHDRAW;
        default:           return TxAPI.TRANSFER;
    }
}

function buildPayload() {
    var base = {
        amount: parseFloat(FormState.amount),
        currency: FormState.currency.toUpperCase()
    };

    if (FormState.description) base.description = FormState.description;

    switch (FormState.transactionType) {
        case "TRANSFER":
            base.bankAccountFromId = parseInt(FormState.fromAccountId);
            base.bankAccountToId   = parseInt(FormState.toAccountId);
            break;
        case "DEPOSIT":
            base.bankAccountToId = parseInt(FormState.toAccountId);
            break;
        case "WITHDRAWAL":
            base.bankAccountFromId = parseInt(FormState.fromAccountId);
            break;
        case "PAYMENT":
            base.bankAccountFromId     = parseInt(FormState.fromAccountId);
            base.externalAccountNumber = FormState.externalAccount;
            break;
    }

    return base;
}

// ============================================================
// RESET (New Transaction after success)
// ============================================================

function resetForm() {
    FormState.transactionType = null;
    FormState.fromAccountId   = null;
    FormState.toAccountId     = null;
    FormState.externalAccount = null;
    FormState.amount          = null;
    FormState.currency        = "EUR";
    FormState.description     = "";
    FormState.category        = "";
    FormState.submitting      = false;

    $(".type-option").removeClass("selected");
    $("#btnToStep2").prop("disabled", true);
    $("#fromAccount").val("");
    $("#toAccount").val("");
    $("#externalAccount").val("");
    $("#amount").val("");
    $("#currency").val("EUR");
    $("#description").val("");
    $("#category").val("");
    $("#fromBalance").text("");
    TxValidation.clearAll();

    goToStep(1);
}

// ============================================================
// INIT
// ============================================================

$(document).ready(function () {
    loadAccounts();
    initTypeSelection();
    initFieldEvents();
    initStep2Actions();
    initSubmit();

    $("#btnNewTransaction").on("click", function () {
        resetForm();
    });

    var params = new URLSearchParams(window.location.search);
    var preselectedType = params.get("type");
    if (preselectedType && VALID_TX_TYPES.indexOf(preselectedType) !== -1) {
        $(".type-option[data-type='" + preselectedType + "']").addClass("selected");
        FormState.transactionType = preselectedType;
        $("#btnToStep2").prop("disabled", false);
        configureStep2ForType(preselectedType);
        goToStep(2);
    }
});
