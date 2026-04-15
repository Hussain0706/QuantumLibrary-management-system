/**
 * api.js — QuantumLibrary Frontend ↔ Backend Bridge
 * ═══════════════════════════════════════════════════════
 *
 * Drop this file into your project and include it BEFORE script.js:
 *   <script src="api.js"></script>
 *
 * When the backend is running, all data comes from the real REST API.
 * When the backend is offline, it gracefully falls back to localStorage.
 *
 * ─── API Endpoints Available ──────────────────────────────
 *  Auth:
 *    API.auth.login(email, password)   → { token, role, name, email, id }
 *    API.auth.register(data)           → success message
 *
 *  Books:
 *    API.books.getAll({ search, genre }) → Book[]
 *    API.books.getById(id)             → Book
 *    API.books.add(book, notify?)      → Book  (Admin)
 *    API.books.update(id, book)        → Book  (Admin)
 *    API.books.delete(id)              → void  (Admin)
 *
 *  Borrow:
 *    API.borrow.borrow(bookId)         → BorrowRecord
 *    API.borrow.return(borrowId)       → BorrowRecord
 *    API.borrow.myBorrows()            → BorrowRecord[]
 *    API.borrow.all()                  → BorrowRecord[] (Admin)
 *    API.borrow.overdue()              → BorrowRecord[] (Admin)
 *
 *  Fines:
 *    API.fines.myFines()               → Fine[]
 *    API.fines.all()                   → Fine[]  (Admin)
 *    API.fines.pay(fineId)             → Fine    (Admin)
 *
 *  Admin:
 *    API.admin.stats()                 → StatsMap
 *    API.admin.members()               → User[]
 *    API.admin.deleteMember(id)        → void
 *    API.admin.toggleMember(id)        → User
 * ─────────────────────────────────────────────────────────
 */

const API_BASE = 'http://localhost:8080/api';

// ── Get stored JWT token from session storage ──
function _getToken() {
    return sessionStorage.getItem('libToken') || '';
}

// ── Build request headers (with or without auth) ──
function _headers(auth = true) {
    const h = { 'Content-Type': 'application/json' };
    if (auth) {
        const token = _getToken();
        if (token) h['Authorization'] = 'Bearer ' + token;
    }
    return h;
}

/**
 * Core fetch wrapper.
 * - Returns the `data` field from the API response body on success.
 * - Throws an Error with the API `message` on failure.
 * - Times out after 8 seconds.
 */
async function _fetch(path, options = {}) {
    const controller = new AbortController();
    const timeout    = setTimeout(() => controller.abort(), 8000);
    try {
        const res  = await fetch(API_BASE + path, {
            ...options,
            signal: controller.signal,
        });
        const json = await res.json();
        if (!json.success) {
            // Attach the errorCode so callers can show smart prompts
            const err = new Error(json.message || 'Request failed');
            err.errorCode = json.errorCode || null;
            throw err;
        }
        return json.data;
    } catch (err) {
        if (err.name === 'AbortError') throw new Error('Request timed out');
        throw err;
    } finally {
        clearTimeout(timeout);
    }
}

// ══════════════════════════════════════════════════════
//  AUTH
// ══════════════════════════════════════════════════════
const API = {

    auth: {
        /**
         * Login — stores JWT token in sessionStorage on success.
         * @returns {{ token, role, name, email, id }}
         */
        async login(email, password) {
            const data = await _fetch('/auth/login', {
                method:  'POST',
                headers: _headers(false),
                body:    JSON.stringify({ email, password }),
            });
            // Persist JWT and user info for subsequent requests
            sessionStorage.setItem('libToken', data.token);
            sessionStorage.setItem('libAuth',  'true');
            sessionStorage.setItem('libRole',  data.role === 'ROLE_ADMIN' ? 'admin' : 'member');
            sessionStorage.setItem('libUser',  data.name);
            sessionStorage.setItem('libEmail', data.email);
            sessionStorage.setItem('libId',    data.id);
            return data;
        },

        /**
         * Register a new member account.
         * @param {{ name, email, password, phone }} req
         */
        async register(req) {
            return _fetch('/auth/register', {
                method:  'POST',
                headers: _headers(false),
                body:    JSON.stringify(req),
            });
        },

        /** Clear session and redirect to login */
        logout() {
            sessionStorage.clear();
            window.location.href = 'login.html';
        }
    },

    // ══════════════════════════════════════════════════
    //  BOOKS
    // ══════════════════════════════════════════════════
    books: {
        /** Get all books — optional ?search= or ?genre= filter */
        async getAll({ search, genre } = {}) {
            let qs = '';
            if (search) qs += `?search=${encodeURIComponent(search)}`;
            else if (genre) qs += `?genre=${encodeURIComponent(genre)}`;
            return _fetch('/books' + qs, { headers: _headers(false) });
        },

        async getById(id) {
            return _fetch('/books/' + id, { headers: _headers(false) });
        },

        /** Admin: add new book to catalog */
        async add(book, notifyMembers = false) {
            return _fetch(`/books?notifyMembers=${notifyMembers}`, {
                method:  'POST',
                headers: _headers(),
                body:    JSON.stringify(book),
            });
        },

        /** Admin: update book details or stock */
        async update(id, book) {
            return _fetch('/books/' + id, {
                method:  'PUT',
                headers: _headers(),
                body:    JSON.stringify(book),
            });
        },

        /** Admin: remove a book */
        async delete(id) {
            return _fetch('/books/' + id, {
                method:  'DELETE',
                headers: _headers(),
            });
        },
    },

    // ══════════════════════════════════════════════════
    //  BORROW
    // ══════════════════════════════════════════════════
    borrow: {
        /** Member: borrow a book by ID */
        async borrow(bookId) {
            return _fetch('/borrow', {
                method:  'POST',
                headers: _headers(),
                body:    JSON.stringify({ bookId }),
            });
        },

        /** Member/Admin: return a borrowed book by its record ID */
        async return(borrowId) {
            return _fetch('/borrow/return/' + borrowId, {
                method:  'POST',
                headers: _headers(),
            });
        },

        /** Member: get own active borrows */
        async myBorrows() {
            return _fetch('/borrow/my', { headers: _headers() });
        },

        /** Member: get full borrow history (active + returned) */
        async myHistory() {
            return _fetch('/borrow/my/history', { headers: _headers() });
        },

        /** Admin: get all borrow records */
        async all() {
            return _fetch('/borrow/all', { headers: _headers() });
        },

        /** Admin: get overdue borrow records */
        async overdue() {
            return _fetch('/borrow/overdue', { headers: _headers() });
        },
    },

    // ══════════════════════════════════════════════════
    //  FINES
    // ══════════════════════════════════════════════════
    fines: {
        /** Member: get own fines */
        async myFines() {
            return _fetch('/fines/my', { headers: _headers() });
        },

        /** Admin: get all fines */
        async all() {
            return _fetch('/fines/all', { headers: _headers() });
        },

        /** Admin: mark a fine as paid and send receipt email */
        async pay(fineId) {
            return _fetch('/fines/pay/' + fineId, {
                method:  'POST',
                headers: _headers(),
            });
        },
    },

    // ══════════════════════════════════════════════════
    //  ADMIN
    // ══════════════════════════════════════════════════
    admin: {
        /**
         * Get real-time dashboard statistics.
         * Returns: { totalBooks, availableBooks, totalMembers, activeBorrows,
         *            issuedToday, overdueCount, finesCollected, finesOutstanding }
         */
        async stats() {
            return _fetch('/admin/stats', { headers: _headers() });
        },

        /** Get all members list */
        async members() {
            return _fetch('/admin/members', { headers: _headers() });
        },

        /** Get a specific member */
        async getMember(id) {
            return _fetch('/admin/members/' + id, { headers: _headers() });
        },

        /** Permanently remove a member */
        async deleteMember(id) {
            return _fetch('/admin/members/' + id, {
                method:  'DELETE',
                headers: _headers(),
            });
        },

        /** Toggle member account active/inactive */
        async toggleMember(id) {
            return _fetch('/admin/members/' + id + '/toggle', {
                method:  'PUT',
                headers: _headers(),
            });
        },
    },

    // ══════════════════════════════════════════════════
    //  UTILS
    // ══════════════════════════════════════════════════

    /** Check if the backend server is reachable */
    async isBackendOnline() {
        try {
            await fetch(API_BASE + '/books', { signal: AbortSignal.timeout(3000) });
            return true;
        } catch {
            return false;
        }
    },

    /** Get the currently logged-in user's info from session */
    currentUser() {
        return {
            token: sessionStorage.getItem('libToken'),
            role:  sessionStorage.getItem('libRole'),
            name:  sessionStorage.getItem('libUser'),
            email: sessionStorage.getItem('libEmail'),
            id:    sessionStorage.getItem('libId'),
        };
    },

    /** True if any user is logged in */
    isLoggedIn() {
        return !!sessionStorage.getItem('libAuth');
    },
};

// ── On page load: warn if backend is offline ──
(async function checkBackend() {
    const online = await API.isBackendOnline();
    if (!online) {
        console.warn(
            '⚠️  QuantumLibrary backend is not running!\n' +
            'Start it with:  cd backend && mvn spring-boot:run\n' +
            'Falling back to localStorage data...'
        );
    } else {
        console.info('✅ QuantumLibrary backend connected at', API_BASE);
    }
})();
