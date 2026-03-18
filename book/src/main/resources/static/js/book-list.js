/* ===== 상태 ===== */
let currentSort = 'bookKey';
let currentDirection = 'desc';
let isSearchMode = false;
let isAdvancedOpen = false;

/* ===== 전체 목록 조회 ===== */
async function loadBooks(page) {
  try {
    const response = await fetch(`/api/books?page=${page}&size=${PAGE_SIZE}&sort=${currentSort},${currentDirection}`);
    if (!response.ok) { showToast((await response.json()).message, 'error'); return; }
    const data = await response.json();
    renderBookList(data.content);
    renderPagination(data.totalPages, data.page, 'pagination', loadBooks);
    document.getElementById('resultInfo').textContent =
        `총 ${data.totalElements}건 (${data.page + 1} / ${data.totalPages} 페이지)`;
    isSearchMode = false;
    updateSearchBadge();
  } catch (e) {
    showToast('도서 목록 조회 중 오류가 발생했습니다.', 'error');
  }
}

/* ===== ES 검색 ===== */
async function doSearch(page) {
  const bookName  = document.getElementById('searchBookName').value.trim();
  const authName  = document.getElementById('searchAuthName').value.trim();
  const pubName   = document.getElementById('searchPubName').value.trim();
  const minPrice  = document.getElementById('searchMinPrice').value;
  const maxPrice  = document.getElementById('searchMaxPrice').value;
  const category  = document.getElementById('searchCategory').value;

  if (!bookName && !authName && !pubName && !minPrice && !maxPrice && !category) {
    loadBooks(0);
    return;
  }

  let url = `/api/books/search/filter?page=${page}&size=${PAGE_SIZE}`;
  if (bookName)  url += `&bookName=${encodeURIComponent(bookName)}`;
  if (authName)  url += `&authName=${encodeURIComponent(authName)}`;
  if (pubName)   url += `&pubName=${encodeURIComponent(pubName)}`;
  if (minPrice)  url += `&minPrice=${minPrice}`;
  if (maxPrice)  url += `&maxPrice=${maxPrice}`;
  if (category)  url += `&category=${encodeURIComponent(category)}`;

  try {
    const response = await fetch(url);
    if (!response.ok) { showToast((await response.json()).message, 'error'); return; }
    const data = await response.json();
    renderBookList(data.content, true);
    renderPagination(data.totalPages, data.page, 'pagination', doSearch);
    document.getElementById('resultInfo').textContent =
        `검색 결과: 총 ${data.totalElements}건 (${data.page + 1} / ${data.totalPages} 페이지)`;
    isSearchMode = true;
    updateSearchBadge();
    if (bookName) loadRecentKeywords();
  } catch (e) {
    showToast('검색 중 오류가 발생했습니다.', 'error');
  }
}

/* ===== 검색 초기화 ===== */
function clearSearch() {
  document.getElementById('searchBookName').value = '';
  document.getElementById('searchAuthName').value = '';
  document.getElementById('searchPubName').value = '';
  document.getElementById('searchMinPrice').value = '';
  document.getElementById('searchMaxPrice').value = '';
  document.getElementById('searchCategory').value = '';
  isSearchMode = false;
  updateSearchBadge();

  // 카테고리 사이드바 초기화
  document.querySelectorAll('.category-menu-item, .category-sub-item').forEach(item => item.classList.remove('active'));
  document.querySelector('.category-menu-item').classList.add('active');
  document.querySelectorAll('.category-sub-menu').forEach(m => m.style.display = 'none');
  document.querySelectorAll('.category-arrow').forEach(a => a.textContent = '▶');

  loadBooks(0);
}

function updateSearchBadge() {
  const badge = document.getElementById('searchModeBadge');
  if (isSearchMode) {
    badge.textContent = 'ES 검색 중';
    badge.classList.add('active');
  } else {
    badge.classList.remove('active');
  }
}

/* ===== 검색 드롭다운 ===== */
function showSearchDropdown() {
  document.getElementById('searchDropdown').classList.add('show');
}

function hideSearchDropdown() {
  document.getElementById('searchDropdown').classList.remove('show');
  document.getElementById('autocompleteSection').style.display = 'none';
}

/* ===== 상세검색 토글 ===== */
function toggleAdvanced() {
  isAdvancedOpen = !isAdvancedOpen;
  const el   = document.getElementById('advancedSearch');
  const text = document.getElementById('advancedToggleText');
  if (isAdvancedOpen) {
    el.style.display = 'block';
    text.textContent = '▲ 상세검색 닫기';
    loadSearchCategoryOptions();
  } else {
    el.style.display = 'none';
    text.textContent = '▼ 상세검색 (저자 / 출판사 / 가격 / 카테고리)';
  }
}

/* ===== 검색용 카테고리 로드 ===== */
async function loadSearchCategoryOptions() {
  try {
    const currentValue = document.getElementById('searchCategory').value;  // ✅ 현재 값 저장
    const res = await fetch('/api/categories');
    const categories = await res.json();
    const select = document.getElementById('searchCategory');
    select.innerHTML = '<option value="">전체 카테고리</option>';
    categories.forEach(c => {
      const option = document.createElement('option');
      option.value = c.code;
      option.textContent = c.fullName;
      select.appendChild(option);
    });
    select.value = currentValue;  // ✅ 저장했던 값 복원
  } catch (e) {}
}

/* ===== 카테고리 사이드바 ===== */
function filterByCategory(code, el) {
  // 활성화 표시
  document.querySelectorAll('.category-menu-item, .category-sub-item').forEach(item => {
    item.classList.remove('active');
  });
  if (el) el.classList.add('active');

  // 검색
  document.getElementById('searchCategory').value = code;
  if (!isAdvancedOpen) toggleAdvanced();
  doSearch(0);
}

function toggleCategoryMenu(id, code, el) {
  const submenu = document.getElementById('submenu-' + id);
  const arrow   = document.getElementById('arrow-' + id);
  const isOpen  = submenu.style.display === 'block';

  // 모든 서브메뉴 닫기
  document.querySelectorAll('.category-sub-menu').forEach(m => m.style.display = 'none');
  document.querySelectorAll('.category-arrow').forEach(a => a.textContent = '▶');

  if (!isOpen) {
    submenu.style.display = 'block';
    arrow.textContent = '▼';
  }

  // 1차 분류 자체도 검색
  filterByCategory(code, el);
}

/* ===== 최근 검색어 ===== */
async function loadRecentKeywords() {
  try {
    const res = await fetch('/api/books/search/recent');
    if (!res.ok) return;
    const keywords = await res.json();
    renderRecentKeywords(keywords);
  } catch (e) {}
}

function renderRecentKeywords(keywords) {
  const container = document.getElementById('recentKeywords');
  if (!keywords || keywords.length === 0) {
    container.innerHTML = '<span class="keyword-empty">최근 검색어가 없습니다.</span>';
    return;
  }
  container.innerHTML = keywords.map(kw =>
      `<span class="keyword-tag" onclick="selectKeyword('${kw.replace(/'/g, "\\'")}')">
            ${kw}
         </span>`
  ).join('');
}

async function clearAllRecent(e) {
  e.stopPropagation();
  try {
    await fetch('/api/books/search/recent/all', { method: 'DELETE' });
  } catch (err) {}
  document.getElementById('recentKeywords').innerHTML =
      '<span class="keyword-empty">최근 검색어가 없습니다.</span>';
}

/* ===== 인기 검색어 ===== */
async function loadPopularKeywords() {
  try {
    const res = await fetch('/api/books/search/popular');
    if (!res.ok) return;
    const keywords = await res.json();
    renderPopularKeywords(keywords);
  } catch (e) {}
}

function renderPopularKeywords(keywords) {
  const container = document.getElementById('popularKeywords');
  if (!keywords || keywords.length === 0) {
    container.innerHTML = '<span class="keyword-empty">인기 검색어가 없습니다.</span>';
    return;
  }
  container.innerHTML = keywords.map((kw, idx) =>
      `<span class="keyword-tag popular" onclick="selectKeyword('${kw.replace(/'/g, "\\'")}')">
            ${idx + 1}. ${kw}
         </span>`
  ).join('');
}

function selectKeyword(keyword) {
  document.getElementById('searchBookName').value = keyword;
  hideSearchDropdown();
  doSearch(0);
}

/* ===== 목록 렌더링 ===== */
function renderBookList(content, showActions = true) {
  const tbody = document.getElementById('bookList');
  tbody.innerHTML = '';
  updateSortHeaders();

  if (!content || content.length === 0) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="9">데이터가 없습니다.</td></tr>`;
    return;
  }

  content.forEach(book => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
            <td>${book.bookKey}</td>
            <td>${book.isbn || '-'}</td>
            <td class="td-left">${book.bookName}</td>
            <td>${book.authName || '-'}</td>
            <td>${book.pubName || '-'}</td>
            <td>${book.pubDate || '-'}</td>
            <td>${book.categoryName || '-'}</td>
            <td>${book.bookPrice ? book.bookPrice.toLocaleString() + '원' : '-'}</td>
            ${showActions ? `
            <td>
                <div class="btn-group">
                    <button class="btn-edit" onclick="openEditModal('${book.bookKey}')">수정</button>
                    <button class="btn-delete" onclick="deleteBook('${book.bookKey}')">삭제</button>
                </div>
            </td>` : '<td>-</td>'}
        `;
    tbody.appendChild(tr);
  });
}

/* ===== 정렬 ===== */
function toggleSort(field) {
  if (currentSort === field) {
    currentDirection = currentDirection === 'asc' ? 'desc' : 'asc';
  } else {
    currentSort = field;
    currentDirection = 'asc';
  }
  if (!isSearchMode) loadBooks(0);
}

function sortIcon(field) {
  if (currentSort !== field) return ' ↕';
  return currentDirection === 'asc' ? ' ↑' : ' ↓';
}

function updateSortHeaders() {
  document.querySelectorAll('.sortable').forEach(th => {
    const field = th.dataset.field;
    const label = th.dataset.label;
    th.innerHTML = label + sortIcon(field);
    th.className = 'sortable' + (currentSort === field ? ' sorted' : '');
  });
}