/**
 * SimplyBank — Shared Utilities
 * Common helpers used across dashboard, transactions, and new-transaction pages.
 */

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
    const symbols = { EUR: "\u20AC", USD: "$", GBP: "\u00A3" };
    const sym = symbols[currency] || currency + " ";
    return sym + Math.abs(parseFloat(amount) || 0).toLocaleString("en-IE", {
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
    return number.length > 4 ? "\u2022\u2022\u2022\u2022" + number.slice(-4) : number;
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
    const $n = $('<div class="notification" style="' +
        'position:fixed;top:20px;right:20px;z-index:9999;' +
        'padding:14px 24px;border-radius:8px;color:white;' +
        'background:' + (colors[type] || colors.info) + ';' +
        'box-shadow:0 4px 12px rgba(0,0,0,0.15);' +
        'font-size:14px;animation:slideIn 0.3s ease-out">' +
        escapeHtml(message) + '</div>');
    $("body").append($n);
    setTimeout(function () { $n.fadeOut(300, function () { $(this).remove(); }); }, 4000);
}

// ============================================================
// USER HEADER
// ============================================================

function renderUserHeader(user) {
    if (!user) return;
    var first = user.firstName || "";
    var last = user.lastName || "";
    if (first || last) {
        $("#headerUserAvatar").text((first.charAt(0) + last.charAt(0)).toUpperCase());
        $("#headerUserName").text(first + " " + last.charAt(0) + ".");
    }
}

// ============================================================
// MODAL FIELD HELPERS  (shared by transactions.js and account.js)
// ============================================================

function addField(arr, label, value, fullWidth) {
    if (value !== null && value !== undefined && value !== "" && value !== "N/A" && value !== "null") {
        arr.push({ label: label, value: value, fullWidth: !!fullWidth });
    }
}

function formatId(id) {
    if (id === null || id === undefined) return null;
    return "#" + id;
}

// ============================================================
// ACCOUNT HELPERS
// ============================================================

function getTypeIconClass(type) {
    var t = (type || "").toUpperCase();
    if (t === "CHECKING")         return "blue";
    if (t === "SAVING")           return "green";
    if (t === "BUSINESS")         return "purple";
    if (t === "FOREIGN_CURRENCY") return "amber";
    return "blue";
}

function formatAccountType(type) {
    return (type || "Account")
        .replace(/_/g, " ")
        .replace(/\b\w/g, function (c) { return c.toUpperCase(); });
}

// ============================================================
// TRANSACTION HELPERS
// ============================================================

function getDirectionArrowSvg(isIncoming, size) {
    var s = size || 20;
    if (isIncoming) {
        return '<svg width="' + s + '" height="' + s + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg>';
    }
    return '<svg width="' + s + '" height="' + s + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>';
}

function buildCounterpartyLabel(tx, isIncoming) {
    var fromLabel = (tx.bankAccountFrom && tx.bankAccountFrom.number) ? maskAccount(tx.bankAccountFrom.number) : "\u2014";
    var toLabel   = (tx.bankAccountTo && tx.bankAccountTo.number) ? maskAccount(tx.bankAccountTo.number) : "\u2014";
    return isIncoming ? ("From " + fromLabel) : ("To " + toLabel);
}

function buildTransactionDetailFields(tx, currency) {
    var fields = [];
    addField(fields, "Transaction ID", tx.id);
    addField(fields, "Description",    tx.description, true);
    addField(fields, "Date",           formatDateTime(tx.createDate));
    addField(fields, "From Account",   tx.bankAccountFrom ? maskAccount(tx.bankAccountFrom.number) || formatId(tx.bankAccountFrom.id) : null);
    addField(fields, "To Account",     tx.bankAccountTo ? maskAccount(tx.bankAccountTo.number) || formatId(tx.bankAccountTo.id) : null);
    addField(fields, "Currency",       (currency || "").toUpperCase());
    return fields;
}

function renderDetailGrid(fields) {
    var html = "";
    fields.forEach(function (f) {
        var cls = f.fullWidth ? " full-width" : "";
        html +=
            '<div class="detail-item' + cls + '">' +
                '<div class="detail-label">' + escapeHtml(f.label) + '</div>' +
                '<div class="detail-value">' + escapeHtml(String(f.value)) + '</div>' +
            '</div>';
    });
    return html;
}

// ============================================================
// MODAL HELPERS
// ============================================================

function closeModal() {
    $("#modalOverlay").removeClass("open");
}

function initModalClose() {
    $("#modalClose, #modalCloseBtn").on("click", closeModal);
    $("#modalOverlay").on("click", function (e) {
        if (e.target === this) closeModal();
    });
    $(document).on("keydown", function (e) {
        if (e.key === "Escape") closeModal();
    });
}

// ============================================================
// PAGINATION & SCROLL
// ============================================================

function renderPagination(state) {
    if (state.filtered.length === 0) {
        $("#pagination").hide();
        return;
    }
    $("#pagination").show();
    $("#paginationInfo").text("Page " + state.currentPage + " of " + state.totalPages);
    $("#btnPrevPage").prop("disabled", state.currentPage <= 1);
    $("#btnNextPage").prop("disabled", state.currentPage >= state.totalPages);
}

function scrollToTable(selector) {
    var $el = $(selector);
    if ($el.length) {
        $("html, body").animate({ scrollTop: $el.offset().top - 80 }, 250);
    }
}

// ============================================================
// PROFILE NAVIGATION
// ============================================================

function initProfileLinks() {
    var profileUrl = "/user-profile?id=1";
    $(".icon-button[title='Profile']").on("click", function () {
        window.location.href = profileUrl;
    });
    $(".user-avatar, .user-name").css("cursor", "pointer").on("click", function () {
        window.location.href = profileUrl;
    });
}

$(document).ready(function () {
    initProfileLinks();
});
