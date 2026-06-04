/**
 * SimplyBank — Shared Utilities
 * Common helpers used across dashboard, transactions, and new-transaction pages.
 */

// ============================================================
// AJAX HELPER
// ============================================================

function getCsrfToken() {
    const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
}

function ajax(url, method, data) {
    const type = (method || "GET").toUpperCase();
    const opts = {
        url,
        type,
        dataType: "json",
        contentType: "application/json; charset=UTF-8"
    };
    if (data && (type === "POST" || type === "PUT")) {
        opts.data = JSON.stringify(data);
    }
    // Attach the CSRF token on state-changing, session-cookie-authenticated requests.
    // (Bearer-token API clients are exempt server-side; the UI uses the session cookie.)
    if (type !== "GET" && type !== "HEAD" && type !== "OPTIONS") {
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            opts.headers = { "X-XSRF-TOKEN": csrfToken };
        }
    }
    return $.ajax(opts);
}

// ============================================================
// FORMAT HELPERS
// ============================================================

function formatCurrency(amount, currency) {
    currency = (currency || "EUR").toUpperCase();
    const symbols = { EUR: "\u20AC", USD: "$", GBP: "\u00A3", PLN: "z\u0142 " };
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
// MANAGEMENT NAV (role-gated)
// ============================================================

function initManagementNav() {
    ajax("/api/auth/me", "GET")
        .done(function (user) {
            var role = (user.role || "").toUpperCase();
            if (role === "EMPLOYEE" || role === "ADMIN") {
                $("#navManagement").show();
                $("#navTransfer").hide();
            }
        })
        .fail(function (jqxhr) {
            console.error("[initManagementNav] GET /api/auth/me failed; management nav will stay hidden:", jqxhr);
        });
}

$(document).ready(function () {
    initManagementNav();
    initLogoutMenu();
});

// ============================================================
// LOGOUT DROPDOWN + CONFIRMATION
// ============================================================

function initLogoutMenu() {
    var $avatar = $("#headerUserAvatar");
    if (!$avatar.length || $("#logoutMenu").length) return;

    injectLogoutStyles();

    var $userMenu = $avatar.closest(".user-menu");
    $userMenu.css("position", "relative");
    $userMenu.append(
        '<div class="logout-menu" id="logoutMenu" role="menu" aria-hidden="true">' +
            '<button type="button" class="logout-menu-item" id="logoutMenuItem" role="menuitem">' +
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' +
                    '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>' +
                    '<polyline points="16 17 21 12 16 7"/>' +
                    '<line x1="21" y1="12" x2="9" y2="12"/>' +
                '</svg>' +
                '<span>Logout</span>' +
            '</button>' +
        '</div>'
    );

    $("body").append(
        '<div class="logout-confirm-overlay" id="logoutConfirmOverlay" role="dialog" aria-modal="true" aria-labelledby="logoutConfirmTitle">' +
            '<div class="logout-confirm-modal">' +
                '<h3 class="logout-confirm-title" id="logoutConfirmTitle">Log out?</h3>' +
                '<p class="logout-confirm-text">Are you sure you want to log out of SimplyBank?</p>' +
                '<div class="logout-confirm-actions">' +
                    '<button type="button" class="logout-btn logout-btn-secondary" id="logoutCancelBtn">Cancel</button>' +
                    '<button type="button" class="logout-btn logout-btn-danger" id="logoutConfirmBtn">Yes, log out</button>' +
                '</div>' +
            '</div>' +
            '<form id="logoutForm" method="post" action="/logout" style="display:none;"></form>' +
        '</div>'
    );

    $avatar.attr("aria-haspopup", "true").attr("aria-expanded", "false");

    $avatar.on("click.logoutMenu", function (e) {
        e.stopPropagation();
        toggleLogoutMenu();
    });

    $("#logoutMenuItem").on("click", function (e) {
        e.stopPropagation();
        closeLogoutMenu();
        openLogoutConfirm();
    });

    $(document).on("click.logoutMenu", function (e) {
        if (!$(e.target).closest("#logoutMenu, #headerUserAvatar").length) {
            closeLogoutMenu();
        }
    });

    $(document).on("keydown.logoutMenu", function (e) {
        if (e.key === "Escape") {
            closeLogoutMenu();
            closeLogoutConfirm();
        }
    });

    $("#logoutCancelBtn").on("click", closeLogoutConfirm);
    $("#logoutConfirmOverlay").on("click", function (e) {
        if (e.target === this) closeLogoutConfirm();
    });
    $("#logoutConfirmBtn").on("click", function () {
        // Logout is a real form POST, so it needs the CSRF token as a hidden field.
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            $("#logoutForm").html('<input type="hidden" name="_csrf">');
            $("#logoutForm").find("input[name='_csrf']").val(csrfToken);
        }
        $("#logoutForm").trigger("submit");
    });
}

function toggleLogoutMenu() {
    var $menu = $("#logoutMenu");
    if ($menu.hasClass("open")) {
        closeLogoutMenu();
    } else {
        $menu.addClass("open").attr("aria-hidden", "false");
        $("#headerUserAvatar").attr("aria-expanded", "true");
    }
}

function closeLogoutMenu() {
    $("#logoutMenu").removeClass("open").attr("aria-hidden", "true");
    $("#headerUserAvatar").attr("aria-expanded", "false");
}

function openLogoutConfirm() {
    $("#logoutConfirmOverlay").addClass("open");
}

function closeLogoutConfirm() {
    $("#logoutConfirmOverlay").removeClass("open");
}

function injectLogoutStyles() {
    if (document.getElementById("logoutMenuStyles")) return;
    var css =
        ".logout-menu{position:absolute;top:calc(100% + 8px);right:0;min-width:160px;" +
        "background:#fff;border:1px solid #e2e8f0;border-radius:8px;" +
        "box-shadow:0 10px 15px -3px rgba(0,0,0,0.1);padding:6px;z-index:200;" +
        "opacity:0;visibility:hidden;transform:translateY(-4px);transition:all 0.15s ease;}" +
        ".logout-menu.open{opacity:1;visibility:visible;transform:translateY(0);}" +
        ".logout-menu-item{display:flex;align-items:center;gap:10px;width:100%;" +
        "padding:8px 12px;background:transparent;border:none;border-radius:6px;" +
        "color:#0f172a;font-size:0.9375rem;font-weight:500;cursor:pointer;" +
        "text-align:left;font-family:inherit;transition:background 0.15s ease;}" +
        ".logout-menu-item:hover{background:#f8fafc;color:#ef4444;}" +
        ".logout-confirm-overlay{position:fixed;inset:0;background:rgba(15,23,42,0.5);" +
        "display:flex;align-items:center;justify-content:center;z-index:1000;" +
        "opacity:0;visibility:hidden;transition:opacity 0.2s ease;}" +
        ".logout-confirm-overlay.open{opacity:1;visibility:visible;}" +
        ".logout-confirm-modal{background:#fff;border-radius:12px;padding:24px;" +
        "max-width:400px;width:90%;box-shadow:0 20px 25px -5px rgba(0,0,0,0.1);" +
        "transform:scale(0.95);transition:transform 0.2s ease;}" +
        ".logout-confirm-overlay.open .logout-confirm-modal{transform:scale(1);}" +
        ".logout-confirm-title{font-size:1.25rem;font-weight:600;color:#0f172a;margin-bottom:8px;}" +
        ".logout-confirm-text{font-size:0.9375rem;color:#64748b;margin-bottom:20px;}" +
        ".logout-confirm-actions{display:flex;gap:10px;justify-content:flex-end;}" +
        ".logout-btn{padding:9px 18px;border-radius:8px;font-size:0.9375rem;font-weight:500;" +
        "cursor:pointer;border:1px solid transparent;font-family:inherit;transition:all 0.15s ease;}" +
        ".logout-btn-secondary{background:#fff;color:#0f172a;border-color:#e2e8f0;}" +
        ".logout-btn-secondary:hover{background:#f8fafc;}" +
        ".logout-btn-danger{background:#ef4444;color:#fff;}" +
        ".logout-btn-danger:hover{background:#dc2626;}";
    var style = document.createElement("style");
    style.id = "logoutMenuStyles";
    style.textContent = css;
    document.head.appendChild(style);
}

// ============================================================
// PROFILE NAVIGATION
// ============================================================

function initProfileLinks(userId) {
    var profileUrl = userId ? "/user-profile?id=" + userId : "/user-profile";
    $(".icon-button[title='Profile']").off("click.profile").on("click.profile", function () {
        window.location.href = profileUrl;
    });
}
