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
