/* ===== 상수 ===== */
const PAGE_SIZE = 10;

/* ===== 탭 전환 ===== */
function switchTab(tabName) {
  document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
  document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
  document.getElementById('tab-' + tabName).classList.add('active');
  event.target.classList.add('active');
  if (tabName === 'list') {
    isSearchMode ? doSearch(0) : loadBooks(0);
  }
  if (tabName === 'register') {
    loadCategoryOptions();  // 등록 탭 열 때 카테고리 로드
  }
  if (tabName === 'stats') {
    loadStats();
  }
}

/* ===== 토스트 ===== */
function showToast(message, type = '') {
  const toast = document.getElementById('toast');
  toast.textContent = message;
  toast.className = 'toast show ' + type;
  setTimeout(() => { toast.className = 'toast'; }, 2500);
}

/* ===== 페이징 ===== */
function renderPagination(totalPages, currentPage, containerId, callback) {
  const container = document.getElementById(containerId);
  container.innerHTML = '';
  if (totalPages <= 1) return;

  const prevBtn = document.createElement('button');
  prevBtn.textContent = '이전';
  prevBtn.className = 'page-btn';
  prevBtn.disabled = currentPage === 0;
  prevBtn.onclick = () => callback(currentPage - 1);
  container.appendChild(prevBtn);

  let startPage = Math.max(0, currentPage - 2);
  let endPage   = Math.min(totalPages, startPage + 5);
  if (endPage - startPage < 5) startPage = Math.max(0, endPage - 5);

  for (let i = startPage; i < endPage; i++) {
    const btn = document.createElement('button');
    btn.textContent = i + 1;
    btn.className = 'page-btn' + (i === currentPage ? ' active' : '');
    btn.onclick = () => callback(i);
    container.appendChild(btn);
  }

  const nextBtn = document.createElement('button');
  nextBtn.textContent = '다음';
  nextBtn.className = 'page-btn';
  nextBtn.disabled = currentPage === totalPages - 1;
  nextBtn.onclick = () => callback(currentPage + 1);
  container.appendChild(nextBtn);
}

/* ===== 자동완성 ===== */
function onAutocomplete(keyword, listId) {
  if (listId === 'authAutocompleteList' || listId === 'pubAutocompleteList') {
    const list = document.getElementById(listId);
    if (!keyword || keyword.trim().length < 1) {
      list.innerHTML = ''; list.style.display = 'none'; return;
    }
    const field = listId === 'authAutocompleteList' ? 'authName' : 'pubName';
    fetch(`/api/books/search/autocomplete?keyword=${encodeURIComponent(keyword.trim())}&field=${field}`)
    .then(res => res.json())
    .then(suggestions => {
      if (!suggestions.length) { list.innerHTML = ''; list.style.display = 'none'; return; }
      list.innerHTML = suggestions.map(name =>
          `<div class="autocomplete-item" onmousedown="selectAutocomplete('${name.replace(/'/g,"\\'")}','${listId}')">${name}</div>`
      ).join('');
      list.style.display = 'block';
    })
    .catch(() => { list.innerHTML = ''; list.style.display = 'none'; });
    return;
  }

  const section = document.getElementById('autocompleteSection');
  const items   = document.getElementById('autocompleteItems');

  if (!keyword || keyword.trim().length < 1) {
    section.style.display = 'none';
    items.innerHTML = '';
    return;
  }

  fetch(`/api/books/search/autocomplete?keyword=${encodeURIComponent(keyword.trim())}&field=bookName`)
  .then(res => res.json())
  .then(suggestions => {
    if (!suggestions.length) { section.style.display = 'none'; return; }
    items.innerHTML = suggestions.map(name =>
        `<div class="autocomplete-item"
                      onmousedown="selectMainAutocomplete('${name.replace(/'/g,"\\'")}')">
                    ${name}
                 </div>`
    ).join('');
    section.style.display = 'block';
  })
  .catch(() => { section.style.display = 'none'; });
}

function selectMainAutocomplete(value) {
  document.getElementById('searchBookName').value = value;
  document.getElementById('autocompleteSection').style.display = 'none';
  hideSearchDropdown();
  doSearch(0);
}

function selectAutocomplete(value, listId) {
  if (listId === 'authAutocompleteList') document.getElementById('searchAuthName').value = value;
  if (listId === 'pubAutocompleteList')  document.getElementById('searchPubName').value  = value;
  document.getElementById(listId).style.display = 'none';
  doSearch(0);
}

/* ===== 외부 클릭 시 드롭다운 닫기 ===== */
document.addEventListener('click', (e) => {
  const wrap = document.querySelector('.main-search-wrap');
  if (wrap && !wrap.contains(e.target)) hideSearchDropdown();
  document.querySelectorAll('.autocomplete-list').forEach(list => {
    if (!e.target.closest('.autocomplete-list') && !e.target.closest('input')) {
      list.style.display = 'none';
    }
  });
});

/* ===== 페이지 로드 ===== */
window.onload = () => {
  loadBooks(0);
  loadRecentKeywords();
  loadPopularKeywords();
};