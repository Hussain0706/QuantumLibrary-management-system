/**
 * script.js — QuantumLibrary Shared Utilities
 * ──────────────────────────────────────────────
 * Shared across all pages:
 *   • showToast()        — notification toasts
 *   • Page loader/fade
 *   • Scroll reveal
 *   • localStorage data layer (books, borrows)
 *   • Fine calculation (₹5/day after 14 days)
 */

/* ════════════════════════════════════
   TOAST NOTIFICATIONS
════════════════════════════════════ */
function showToast(message, type = 'success', duration = 3500) {
  const container = document.getElementById('toastContainer');
  if (!container) return;
  const icons = { success: '✅', error: '❌', info: '💡', warning: '⚠️' };
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${icons[type] || '📢'}</span><span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.animation = 'slideInRight .3s ease reverse';
    setTimeout(() => toast.remove(), 280);
  }, duration);
}

/* ════════════════════════════════════
   PAGE LOADER
════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
  const loader = document.getElementById('pageLoader');
  if (loader) setTimeout(() => loader.classList.add('hide'), 600);
});

/* ════════════════════════════════════
   SCROLL PROGRESS BAR
════════════════════════════════════ */
(function () {
  const bar = document.getElementById('scrollProgressBar');
  if (!bar) return;
  window.addEventListener('scroll', () => {
    const scrolled = window.scrollY;
    const total    = document.documentElement.scrollHeight - window.innerHeight;
    bar.style.width = total > 0 ? (scrolled / total * 100) + '%' : '0%';
  }, { passive: true });
})();

/* ════════════════════════════════════
   SCROLL REVEAL
════════════════════════════════════ */
(function initScrollReveal() {
  const revealObs = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) { entry.target.classList.add('revealed'); revealObs.unobserve(entry.target); }
    });
  }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });
  document.querySelectorAll('[data-reveal]').forEach(el => revealObs.observe(el));

  const staggerObs = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.querySelectorAll(':scope > *').forEach((child, i) => {
          setTimeout(() => child.classList.add('revealed'), i * 100);
        });
        staggerObs.unobserve(entry.target);
      }
    });
  }, { threshold: 0.08, rootMargin: '0px 0px -30px 0px' });
  document.querySelectorAll('[data-stagger]').forEach(el => staggerObs.observe(el));

  const headerObs = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) { entry.target.classList.add('revealed'); headerObs.unobserve(entry.target); }
    });
  }, { threshold: 0.3 });
  document.querySelectorAll('.section-header').forEach(el => headerObs.observe(el));
})();

/* ════════════════════════════════════
   SMOOTH PAGE EXIT
════════════════════════════════════ */
document.addEventListener('click', (e) => {
  const link = e.target.closest('a[href]');
  if (!link) return;
  const href = link.getAttribute('href');
  if (!href || href.startsWith('#') || href.startsWith('http') || href.startsWith('mailto')) return;
  e.preventDefault();
  document.body.style.opacity = '0';
  setTimeout(() => { window.location.href = href; }, 350);
});

/* ════════════════════════════════════════════════════
   ██  DATA LAYER  ██
   localStorage keys:
     lib_books   — Array<Book>
     lib_borrows — Array<BorrowRecord>
════════════════════════════════════════════════════ */

const DEFAULT_BOOKS = [
  {id:1,  title:'1984',                               author:'George Orwell',           genre:'Fiction',     year:1949, isbn:'978-0-45-128423-4', stock:3, desc:'A dystopian novel about totalitarianism, surveillance, and repression.',                            cover:'https://covers.openlibrary.org/b/id/8575708-M.jpg',        rating:5},
  {id:2,  title:'To Kill a Mockingbird',              author:'Harper Lee',              genre:'Fiction',     year:1960, isbn:'978-0-06-112008-4', stock:2, desc:'A Pulitzer Prize-winning masterwork exploring racial injustice and moral growth.',                 cover:'https://covers.openlibrary.org/b/id/8228691-M.jpg',        rating:5},
  {id:3,  title:'A Brief History of Time',            author:'Stephen Hawking',         genre:'Science',     year:1988, isbn:'978-0-55-317521-9', stock:2, desc:'Hawking explores the laws that govern our universe in an accessible masterpiece.',                 cover:'https://covers.openlibrary.org/b/id/8739161-M.jpg',        rating:5},
  {id:4,  title:'Sapiens',                            author:'Yuval Noah Harari',       genre:'History',     year:2011, isbn:'978-0-06-231609-7', stock:4, desc:'A brief history of humankind from the Stone Age to the modern era.',                               cover:'https://covers.openlibrary.org/b/id/8908901-M.jpg',        rating:5},
  {id:5,  title:'The Great Gatsby',                   author:'F. Scott Fitzgerald',     genre:'Fiction',     year:1925, isbn:'978-0-74-323356-5', stock:3, desc:'A novel about the Jazz Age, the American Dream, and obsession.',                                    cover:'https://covers.openlibrary.org/b/id/11430066-M.jpg',       rating:4},
  {id:6,  title:'The Alchemist',                      author:'Paulo Coelho',            genre:'Fiction',     year:1988, isbn:'978-0-06-231500-7', stock:5, desc:'A philosophical novel about following your dreams and listening to your heart.',                      cover:'https://covers.openlibrary.org/b/id/8229155-M.jpg',        rating:5},
  {id:7,  title:'Thinking Fast and Slow',             author:'Daniel Kahneman',         genre:'Science',     year:2011, isbn:'978-0-37-453355-7', stock:2, desc:'A groundbreaking tour of the mind and explains the two systems that drive the human thought.',       cover:'https://covers.openlibrary.org/b/id/9264855-M.jpg',        rating:4},
  {id:8,  title:'Dune',                               author:'Frank Herbert',           genre:'Fiction',     year:1965, isbn:'978-0-44-101359-7', stock:3, desc:'An epic science fiction saga set in a distant future amid a feudal interstellar empire.',            cover:'https://covers.openlibrary.org/b/id/11301554-M.jpg',       rating:5},
  {id:9,  title:'Meditations',                        author:'Marcus Aurelius',         genre:'Philosophy',  year:180,  isbn:'978-0-14-044140-6', stock:4, desc:'Personal writings of Marcus Aurelius on Stoic philosophy and self-discipline.',                      cover:'https://covers.openlibrary.org/b/id/8231856-M.jpg',        rating:5},
  {id:10, title:'The Hobbit',                         author:'J.R.R. Tolkien',          genre:'Fiction',     year:1937, isbn:'978-0-54-792822-7', stock:3, desc:'A fantasy adventure about Bilbo Baggins recruited for a quest to reclaim a treasure.',              cover:'https://covers.openlibrary.org/b/id/8406786-M.jpg',        rating:5},
  {id:11, title:'Atomic Habits',                      author:'James Clear',             genre:'Science',     year:2018, isbn:'978-0-73-521129-2', stock:5, desc:'A guide to building good habits and breaking bad ones through small incremental changes.',            cover:'https://covers.openlibrary.org/b/id/10527107-M.jpg',       rating:5},
  {id:12, title:'Clean Code',                         author:'Robert C. Martin',        genre:'Programming', year:2008, isbn:'978-0-13-235088-4', stock:3, desc:'A handbook of agile software craftsmanship. Learn how to write clean, readable code.',              cover:'https://covers.openlibrary.org/b/id/8732158-M.jpg',        rating:5},
  {id:13, title:'Effective Java',                     author:'Joshua Bloch',            genre:'Programming', year:2018, isbn:'978-0-13-468599-1', stock:2, desc:'Best-practice guide for the Java platform — the definitive Java programming resource.',             cover:'https://covers.openlibrary.org/b/isbn/9780134685991-M.jpg',rating:5},
  {id:14, title:'Python Crash Course',                author:'Eric Matthes',            genre:'Programming', year:2019, isbn:'978-1-59-327603-4', stock:4, desc:'A hands-on, project-based introduction to Python programming for beginners.',                         cover:'https://covers.openlibrary.org/b/isbn/9781593276034-M.jpg',rating:5},
  {id:15, title:'Introduction to Algorithms',         author:'Cormen et al.',           genre:'Programming', year:2009, isbn:'978-0-26-203384-8', stock:2, desc:'The definitive textbook on algorithms — data structures, analysis, and design.',                      cover:'https://covers.openlibrary.org/b/isbn/9780262033848-M.jpg',rating:5},
  {id:16, title:'Cracking the Coding Interview',      author:'Gayle L. McDowell',       genre:'Programming', year:2015, isbn:'978-0-98-478280-8', stock:5, desc:'189 programming questions and solutions — the premier interview prep book.',                         cover:'https://covers.openlibrary.org/b/isbn/9780984782857-M.jpg',rating:5},
  {id:17, title:'The C Programming Language',         author:'Kernighan & Ritchie',     genre:'Programming', year:1988, isbn:'978-0-13-110362-7', stock:3, desc:'The original C reference by the creators of the language — the definitive C guide.',                cover:'https://covers.openlibrary.org/b/isbn/9780131103627-M.jpg',rating:5},
  {id:18, title:'Homo Deus',                          author:'Yuval Noah Harari',       genre:'History',     year:2015, isbn:'978-0-06-246401-6', stock:3, desc:'A brief history of tomorrow — the next steps of humanity.',                                          cover:'https://covers.openlibrary.org/b/id/10527394-M.jpg',       rating:4},
  {id:19, title:'The Art of War',                     author:'Sun Tzu',                 genre:'Philosophy',  year:500,  isbn:'978-1-59-030760-0', stock:6, desc:'Ancient Chinese text on military strategy and tactics with timeless life wisdom.',                  cover:'https://covers.openlibrary.org/b/id/8227470-M.jpg',        rating:5},
  {id:20, title:'JavaScript: The Good Parts',         author:'Douglas Crockford',       genre:'Programming', year:2008, isbn:'978-0-59-651774-8', stock:3, desc:'Highlights the reliable and elegant JavaScript features that are actually good.',                     cover:'https://covers.openlibrary.org/b/isbn/9780596517748-M.jpg',rating:4},
];

/** Get all books from localStorage (seeds defaults on first run) */
function getBooks() {
  let raw = localStorage.getItem('lib_books');
  if (!raw) {
    localStorage.setItem('lib_books', JSON.stringify(DEFAULT_BOOKS));
    return JSON.parse(JSON.stringify(DEFAULT_BOOKS));
  }
  return JSON.parse(raw);
}

/** Save books array to localStorage */
function saveBooks(books) {
  localStorage.setItem('lib_books', JSON.stringify(books));
}

/** Get all borrow records */
function getBorrows() {
  let raw = localStorage.getItem('lib_borrows');
  if (!raw) return [];
  return JSON.parse(raw);
}

/** Save borrow records */
function saveBorrows(borrows) {
  localStorage.setItem('lib_borrows', JSON.stringify(borrows));
}

/** Add a borrow record */
function addBorrow(bookId, memberId) {
  const books = getBooks();
  const book = books.find(b => b.id === bookId);
  if (!book || book.stock <= 0) return false;

  book.stock--;
  saveBooks(books);

  const borrows = getBorrows();
  const borrowDate = new Date().toISOString();
  const dueDate = new Date(Date.now() + 14 * 86400000).toISOString();
  borrows.push({ id: Date.now(), bookId, memberId, borrowDate, dueDate, returned: false });
  saveBorrows(borrows);
  return true;
}

/** Return a borrowed book */
function returnBorrow(borrowId) {
  const borrows = getBorrows();
  const record = borrows.find(b => b.id === borrowId);
  if (!record) return false;

  record.returned = true;
  record.returnDate = new Date().toISOString();
  saveBorrows(borrows);

  const books = getBooks();
  const book = books.find(b => b.id === record.bookId);
  if (book) { book.stock++; saveBooks(books); }
  return true;
}

/** Calculate fine: ₹5 per day after due date */
function calcFine(dueDateStr) {
  const due = new Date(dueDateStr);
  const now = new Date();
  if (now <= due) return 0;
  const overdueDays = Math.ceil((now - due) / 86400000);
  return overdueDays * 5;
}

/** Get active borrows for a specific member */
function getMemberBorrows(memberId) {
  return getBorrows().filter(b => b.memberId === memberId && !b.returned);
}

/** Get all active (not returned) borrows */
function getActiveBorrows() {
  return getBorrows().filter(b => !b.returned);
}

/** Generate a new unique book ID */
function nextBookId() {
  const books = getBooks();
  return books.length > 0 ? Math.max(...books.map(b => b.id)) + 1 : 1;
}
