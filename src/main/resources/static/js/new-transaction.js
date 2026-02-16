/**
 * SimplyBank — New Transaction Page Controller
 *
 * 3-step wizard:
 *   1. Choose transaction type
 *   2. Enter details (accounts, amount) with live validation
 *   3. Review & confirm → POST to API
 *
 * All client-side validation mirrors the backend:
 *   TransactionValidation, BankAccountValidation,
 *   CurrencyValidation, IdValidation
 */

// ============================================================
// CONFIG
// ============================================================

const API = {
    AUTH_ME:       "/api/auth/me",
    BANK_ACCOUNT:  "/api/bank_account",
    TRANSACTION:   "/api/transaction"
};

const VALID_CURRENCIES        = ["EUR", "USD", "GBP"];
const VALID_BANK_ACCOUNT_TYPES = ["CHECKING", "SAVINGS", "CREDIT"];
const VALID_TX_TYPES          = ["TRANSFER", "DEPOSIT", "WITHDRAWAL", "PAYMENT"];
const PASSWORD_MIN_LENGTH     = 8;  // kept for parity with PasswordValidation

// ============================================================
// STATE
// ============================================================

const FormState = {
    currentStep: 1,
    transactionType: null,
    fromAccountId: null,
    toAccountId: null,
    externalAccount: null,
    amount: null,
    currency: "EUR",
    description: "",
    category: "",
    accounts: [],          // user's bank accounts
    currentUserId: null,
    submitting: false
};

// ============================================================
// VALIDATION  (mirrors Java backend validators)
// ============================================================

const Validation = {

    errors: {},

    clearAll() {
        this.errors = {};
        $(".field-error").text("");
        $(".field-input, .field-select").removeClass("invalid valid");
        $(".amount-input-wrap").removeClass("invalid");
        $("#typeError").text("");
    },

    addError(field, message) {
        this.errors[field] = message;
    },

    hasErrors() {
        return Object.keys(this.errors).length > 0;
    },

    showErrors() {
        Object.keys(this.errors).forEach(field => {
            $(`#${field}Error`).text(this.errors[field]);
            const $el = $(`#${field}`);
            if ($el.length) $el.addClass("invalid").removeClass("valid");
        });
    },

    markValid(field) {
        const $el = $(`#${field}`);
        if ($el.length) $el.addClass("valid").removeClass("invalid");
        $(`#${field}Error`).text("");
    },

    // --- IdValidation.java ---
    ensureIdPresent(id, field, label) {
        if (id === null || id === undefined || id === "") {
            this.addError(field, `${label} is required`);
            return false;
        }
        return true;
    },

    // --- TransactionValidation.java ---
    validateTransactionAccounts(fromId, toId) {
        if (fromId && toId && String(fromId) === String(toId)) {
            this.addError("toAccount", "Transaction cannot have same from and to accounts");
            return false;
        }
        return true;
    },

    validateAmount(amount) {
        if (amount === null || amount === undefined || amount === "") {
            this.addError("amount", "Amount is required");
            return false;
        }
        const num = parseFloat(amount);
        if (isNaN(num)) {
            this.addError("amount", "Amount must be a valid number");
            return false;
        }
        if (num <= 0) {
            this.addError("amount", "Amount must be greater than zero");
            return false;
        }
        // Max reasonable check
        if (num > 999999999) {
            this.addError("amount", "Amount exceeds maximum limit");
            return false;
        }
        return true;
    },

    validateSufficientBalance(account, amount) {
        if (!account) return true; // can't check without account data
        const balance = parseFloat(account.balance) || 0;
        const amt     = parseFloat(amount) || 0;
        if (balance < amt) {
            this.addError("amount", `Insufficient balance on account ${maskAccount(account.number || account.accountNumber)}. Available: ${formatCurrency(balance, account.currency || "EUR")}`);
            return false;
        }
        return true;
    },

    // --- CurrencyValidation.java ---
    validateCurrency(currency) {
        if (!currency || currency.trim() === "") {
            this.addError("currency", "Currency is required");
            return false;
        }
        if (!VALID_CURRENCIES.includes(currency.toUpperCase())) {
            this.addError("currency", "Invalid currency: " + currency);
            return false;
        }
        return true;
    },

    // --- BankAccountValidation.java ---
    validateBankAccountType(type) {
        if (!type || type.trim() === "") {
            return false; // silent — not always needed
        }
        if (!VALID_BANK_ACCOUNT_TYPES.includes(type.toUpperCase())) {
            return false;
        }
        return true;
    },

    // --- Transaction Type (local) ---
    validateTransactionType(type) {
        if (!type) {
            this.addError("type", "Please select a transaction type");
            return false;
        }
        if (!VALID_TX_TYPES.includes(type)) {
            this.addError("type", "Invalid transaction type");
            return false;
        }
        return true;
    },

    // --- External account number (for PAYMENT) ---
    validateExternalAccount(value) {
        if (!value || value.trim() === "") {
            this.addError("externalAccount", "Recipient account number is required");
            return false;
        }
        // Basic IBAN-like check: at least 15 chars, alphanumeric + spaces
        const cleaned = value.replace(/\s/g, "");
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
    validateStep2(formState) {
        this.clearAll();
        const type = formState.transactionType;
        let valid = true;

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
            const fromAcct = formState.accounts.find(a => String(a.id) === String(formState.fromAccountId));
            if (!this.validateSufficientBalance(fromAcct, formState.amount)) {
                valid = false;
            }
        }

        if (!valid) this.showErrors();
        return valid;
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
    return sym + Math.abs(parseFloat(amount) || 0).toLocaleString("en-IE", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function maskAccount(number) {
    if (!number) return "N/A";
    return number.length > 4 ? "••••" + number.slice(-4) : number;
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
// NOTIFICATION
// ============================================================

function notify(message, type) {
    type = type || "info";
    $(".notification").remove();
    const colors = { success: "#059669", error: "#dc2626", info: "#2563eb", warning: "#d97706" };
    const $n = $(`<div class="notification" style="background:${colors[type] || colors.info}">${escapeHtml(message)}</div>`);
    $("body").append($n);
    setTimeout(() => $n.fadeOut(300, function () { $(this).remove(); }), 4500);
}

// ============================================================
// STEPPER
// ============================================================

function updateStepper(step) {
    $(".step").each(function () {
        const s = parseInt($(this).data("step"));
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

    // Hide all, show target
    $("#step1, #step2, #step3, #stepSuccess").addClass("hidden");
    if (step === 1) $("#step1").removeClass("hidden");
    if (step === 2) $("#step2").removeClass("hidden");
    if (step === 3) {
        renderConfirmation();
        $("#step3").removeClass("hidden");
    }
    if (step === 4) $("#stepSuccess").removeClass("hidden");

    // Re-trigger entrance animation
    const $card = $(`.form-card:not(.hidden)`);
    $card.css("animation", "none");
    $card[0]?.offsetHeight; // reflow
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
        Validation.clearAll();
        if (!Validation.validateTransactionType(FormState.transactionType)) {
            $("#typeError").text(Validation.errors.type || "Please select a type");
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
    const titles = {
        TRANSFER:   "Transfer Details",
        DEPOSIT:    "Deposit Details",
        WITHDRAWAL: "Withdrawal Details",
        PAYMENT:    "Payment Details"
    };
    const descs = {
        TRANSFER:   "Select source and destination accounts",
        DEPOSIT:    "Select the account to deposit into",
        WITHDRAWAL: "Select the account to withdraw from",
        PAYMENT:    "Enter payment recipient and amount"
    };

    $("#step2Title").text(titles[type] || "Transaction Details");
    $("#step2Desc").text(descs[type] || "Enter the details below");

    // Show/hide fields based on type
    switch (type) {
        case "TRANSFER":
            $("#fromAccountGroup").removeClass("hidden");
            $("#toAccountGroup").removeClass("hidden");
            $("#externalAccountGroup").addClass("hidden");
            break;
        case "DEPOSIT":
            $("#fromAccountGroup").addClass("hidden");
            $("#toAccountGroup").removeClass("hidden");
            // Relabel "To Account" → "Deposit Into"
            $("#toAccountGroup .field-label").html('Deposit Into <span class="req">*</span>');
            $("#externalAccountGroup").addClass("hidden");
            break;
        case "WITHDRAWAL":
            $("#fromAccountGroup").removeClass("hidden");
            // Relabel "From Account" → "Withdraw From"
            $("#fromAccountGroup .field-label").html('Withdraw From <span class="req">*</span>');
            $("#toAccountGroup").addClass("hidden");
            $("#externalAccountGroup").addClass("hidden");
            break;
        case "PAYMENT":
            $("#fromAccountGroup").removeClass("hidden");
            // Relabel
            $("#fromAccountGroup .field-label").html('Pay From <span class="req">*</span>');
            $("#toAccountGroup").addClass("hidden");
            $("#externalAccountGroup").removeClass("hidden");
            break;
    }

    // Reset labels that may have changed
    if (type === "TRANSFER") {
        $("#fromAccountGroup .field-label").html('From Account <span class="req">*</span>');
        $("#toAccountGroup .field-label").html('To Account <span class="req">*</span>');
    }
    if (type === "DEPOSIT") {
        // reset from label in case it was relabeled
        $("#fromAccountGroup .field-label").html('From Account <span class="req">*</span>');
    }

    Validation.clearAll();
}

// ============================================================
// LOAD USER ACCOUNTS
// ============================================================

function loadAccounts() {
    ajax(API.AUTH_ME, "GET")
        .done(function (user) {
            FormState.currentUserId = user.id;
            renderUserHeader(user);

            // Load accounts for this user
            const url = user.id
                ? API.BANK_ACCOUNT + "?clientId=" + user.id
                : API.BANK_ACCOUNT;

            ajax(url, "GET")
                .done(function (accounts) {
                    FormState.accounts = Array.isArray(accounts) ? accounts : [];
                    populateAccountDropdowns(FormState.accounts);
                })
                .fail(function () {
                    // Fallback: all accounts
                    ajax(API.BANK_ACCOUNT, "GET")
                        .done(function (accounts) {
                            FormState.accounts = Array.isArray(accounts) ? accounts : [];
                            populateAccountDropdowns(FormState.accounts);
                        })
                        .fail(function () {
                            notify("Could not load accounts", "error");
                        });
                });
        })
        .fail(function () {
            // Auth not available — try loading all accounts
            ajax(API.BANK_ACCOUNT, "GET")
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
    const $from = $("#fromAccount");
    const $to   = $("#toAccount");

    // Keep the placeholder
    $from.find("option:not(:first)").remove();
    $to.find("option:not(:first)").remove();

    accounts.forEach(function (acct) {
        const label = buildAccountLabel(acct);
        const opt = `<option value="${acct.id}">${escapeHtml(label)}</option>`;
        $from.append(opt);
        $to.append(opt);
    });
}

function buildAccountLabel(acct) {
    const type = (acct.bankAccountType || acct.type || "Account")
        .replace(/_/g, " ")
        .replace(/\b\w/g, c => c.toUpperCase());
    const num  = maskAccount(acct.number || acct.accountNumber);
    const bal  = formatCurrency(acct.balance, acct.currency || acct.bankAccountCurrency || "EUR");
    return `${type} ${num}  —  ${bal}`;
}

function renderUserHeader(user) {
    if (!user) return;
    const first = user.firstName || "";
    const last  = user.lastName  || "";
    if (first || last) {
        $("#headerUserAvatar").text((first.charAt(0) + last.charAt(0)).toUpperCase());
        $("#headerUserName").text(first + " " + last.charAt(0) + ".");
    }
}

// ============================================================
// LIVE FIELD EVENTS
// ============================================================

function initFieldEvents() {

    // From Account change — show balance
    $("#fromAccount").on("change", function () {
        const id = $(this).val();
        FormState.fromAccountId = id || null;
        const acct = FormState.accounts.find(a => String(a.id) === String(id));
        if (acct) {
            const bal = formatCurrency(acct.balance, acct.currency || "EUR");
            $("#fromBalance").html(`Available balance: <span class="bal-num">${bal}</span>`);
            Validation.markValid("fromAccount");
        } else {
            $("#fromBalance").text("");
        }
        liveValidateSameAccount();
    });

    // To Account change
    $("#toAccount").on("change", function () {
        FormState.toAccountId = $(this).val() || null;
        Validation.markValid("toAccount");
        liveValidateSameAccount();
    });

    // External account
    $("#externalAccount").on("input", function () {
        FormState.externalAccount = $(this).val();
    });

    // Currency
    $("#currency").on("change", function () {
        FormState.currency = $(this).val();
    });

    // Amount — live formatting & validation
    $("#amount").on("input", function () {
        let raw = $(this).val().replace(/[^0-9.,]/g, "");
        // Allow only one decimal separator
        const parts = raw.split(/[.,]/);
        if (parts.length > 2) {
            raw = parts[0] + "." + parts.slice(1).join("");
        }
        $(this).val(raw);
        FormState.amount = raw;

        // Live check
        if (raw !== "") {
            const num = parseFloat(raw);
            if (!isNaN(num) && num > 0) {
                $(".amount-input-wrap").removeClass("invalid");
                $("#amountError").text("");
            }
        }
    });

    // Amount blur — format
    $("#amount").on("blur", function () {
        const num = parseFloat($(this).val());
        if (!isNaN(num) && num > 0) {
            $(this).val(num.toFixed(2));
            FormState.amount = num.toFixed(2);
        }
    });

    // Description
    $("#description").on("input", function () {
        FormState.description = $(this).val();
    });

    // Category
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
            Validation.markValid("toAccount");
        }
    }
}

// ============================================================
// STEP 2 → 3  (validate & move)
// ============================================================

function initStep2Actions() {
    $("#btnToStep3").on("click", function () {
        // Collect latest values
        FormState.fromAccountId  = $("#fromAccount").val() || null;
        FormState.toAccountId    = $("#toAccount").val()   || null;
        FormState.externalAccount = $("#externalAccount").val() || null;
        FormState.amount         = $("#amount").val() || null;
        FormState.currency       = $("#currency").val();
        FormState.description    = $("#description").val();
        FormState.category       = $("#category").val();

        if (Validation.validateStep2(FormState)) {
            // Extra UI: highlight amount wrap if invalid
            if (Validation.errors.amount) {
                $(".amount-input-wrap").addClass("invalid");
            }
            goToStep(3);
        } else {
            if (Validation.errors.amount) {
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
    const type = FormState.transactionType;
    const fromAcct = FormState.accounts.find(a => String(a.id) === String(FormState.fromAccountId));
    const toAcct   = FormState.accounts.find(a => String(a.id) === String(FormState.toAccountId));

    const rows = [];

    // Type
    rows.push({ label: "Transaction Type", value: `<span class="confirm-type-badge ${type}">${capitalize(type)}</span>`, isHtml: true });

    // From
    if (type !== "DEPOSIT") {
        rows.push({ label: "From", value: fromAcct ? buildAccountLabel(fromAcct) : "—" });
    }

    // To
    if (type === "TRANSFER" || type === "DEPOSIT") {
        const lbl = type === "DEPOSIT" ? "Deposit Into" : "To";
        rows.push({ label: lbl, value: toAcct ? buildAccountLabel(toAcct) : "—" });
    }

    // External
    if (type === "PAYMENT") {
        rows.push({ label: "Recipient Account", value: FormState.externalAccount || "—" });
    }

    // Currency
    rows.push({ label: "Currency", value: FormState.currency.toUpperCase() });

    // Amount (highlighted)
    rows.push({
        label: "Amount",
        value: formatCurrency(FormState.amount, FormState.currency),
        isAmount: true,
        highlight: true
    });

    // Description
    if (FormState.description) {
        rows.push({ label: "Description", value: FormState.description });
    }

    // Category
    if (FormState.category) {
        rows.push({ label: "Category", value: FormState.category });
    }

    // Build HTML
    let html = "";
    rows.forEach(function (r) {
        const cls = r.highlight ? " highlight" : "";
        const valCls = r.isAmount ? "confirm-value amount-large" : "confirm-value";
        const val = r.isHtml ? r.value : escapeHtml(r.value);
        html += `<div class="confirm-row${cls}">
                    <span class="confirm-label">${escapeHtml(r.label)}</span>
                    <span class="${valCls}">${val}</span>
                 </div>`;
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

        const $btn = $(this);
        const originalText = $btn.html();
        $btn.prop("disabled", true).html('<div class="spinner-sm"></div> Processing…');

        // Build request payload
        const payload = buildPayload();

        ajax(API.TRANSACTION, "POST", payload)
            .done(function (response) {
                const typeName = capitalize(FormState.transactionType);
                $("#successMessage").text(`Your ${typeName.toLowerCase()} of ${formatCurrency(FormState.amount, FormState.currency)} has been processed successfully.`);
                goToStep(4);
            })
            .fail(function (jqxhr) {
                const msg = jqxhr.responseJSON?.message || "Transaction failed. Please try again.";
                notify(msg, "error");
                $btn.prop("disabled", false).html(originalText);
            })
            .always(function () {
                FormState.submitting = false;
            });
    });
}

function buildPayload() {
    const base = {
        type: FormState.transactionType,
        amount: parseFloat(FormState.amount),
        currency: FormState.currency.toUpperCase()
    };

    if (FormState.description) base.description = FormState.description;
    if (FormState.category)    base.category    = FormState.category;

    switch (FormState.transactionType) {
        case "TRANSFER":
            base.bankAccountIdFrom = parseInt(FormState.fromAccountId);
            base.bankAccountIdTo   = parseInt(FormState.toAccountId);
            break;
        case "DEPOSIT":
            base.bankAccountIdTo = parseInt(FormState.toAccountId);
            break;
        case "WITHDRAWAL":
            base.bankAccountIdFrom = parseInt(FormState.fromAccountId);
            break;
        case "PAYMENT":
            base.bankAccountIdFrom      = parseInt(FormState.fromAccountId);
            base.externalAccountNumber  = FormState.externalAccount;
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

    // Reset UI
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
    Validation.clearAll();

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

    // "New Transaction" button on success screen
    $("#btnNewTransaction").on("click", function () {
        resetForm();
    });
});
