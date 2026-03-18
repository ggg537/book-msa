/* ===== 카테고리 로드 ===== */
async function loadCategoryOptions() {
  try {
    // 1차 분류 로드
    const res = await fetch('/api/categories/parents');
    const parents = await res.json();
    const parentSelect = document.getElementById('regCategoryParent');
    parentSelect.innerHTML = '<option value="">1차 분류 선택</option>';
    parents.forEach(c => {
      const option = document.createElement('option');
      option.value = c.code;
      option.textContent = c.name;
      parentSelect.appendChild(option);
    });
    // 2차 초기화
    document.getElementById('regCategoryChild').innerHTML = '<option value="">2차 분류 선택</option>';
    document.getElementById('regCategoryChild').disabled = true;
  } catch (e) {}
}

/* ===== 1차 분류 선택 시 2차 분류 로드 ===== */
async function onParentCategoryChange(parentCode) {
  const childSelect = document.getElementById('regCategoryChild');
  if (!parentCode) {
    childSelect.innerHTML = '<option value="">2차 분류 선택</option>';
    childSelect.disabled = true;
    return;
  }
  try {
    const res = await fetch(`/api/categories/${parentCode}/children`);
    const children = await res.json();
    childSelect.innerHTML = '<option value="">2차 분류 선택</option>';
    children.forEach(c => {
      const option = document.createElement('option');
      option.value = c.code;
      option.textContent = c.name;
      childSelect.appendChild(option);
    });
    childSelect.disabled = false;
  } catch (e) {}
}

/* ===== 도서 등록 ===== */
async function registerBook() {
  const bookKey  = document.getElementById('regBookKey').value.trim();
  const isbn     = document.getElementById('regIsbn').value.trim();
  const bookName = document.getElementById('regBookName').value.trim();
  const category = document.getElementById('regCategoryChild').value
      || document.getElementById('regCategoryParent').value
      || null;

  if (!bookKey || !isbn || !bookName) {
    showToast('도서키, ISBN, 도서명은 필수 입력입니다.', 'error');
    return;
  }

  const data = {
    bookKey, isbn, bookName,
    authName:  document.getElementById('regAuthName').value.trim()  || null,
    pubName:   document.getElementById('regPubName').value.trim()   || null,
    pubDate:   document.getElementById('regPubDate').value          || null,
    bookIndex: document.getElementById('regBookIndex').value.trim() || null,
    bookPrice: document.getElementById('regBookPrice').value
        ? parseInt(document.getElementById('regBookPrice').value) : null,
    category   // ✅ 추가
  };

  try {
    const response = await fetch('/api/books', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!response.ok) { showToast((await response.json()).message, 'error'); return; }
    showToast('도서가 등록되었습니다.', 'success');
    clearRegisterForm();
  } catch (e) {
    showToast('도서 등록 중 오류가 발생했습니다.', 'error');
  }
}

/* ===== 등록 폼 초기화 ===== */
function clearRegisterForm() {
  ['regBookKey','regIsbn','regBookName','regAuthName',
    'regPubName','regPubDate','regBookIndex','regBookPrice']
  .forEach(id => document.getElementById(id).value = '');
  document.getElementById('regCategoryParent').value = '';
  document.getElementById('regCategoryChild').innerHTML = '<option value="">2차 분류 선택</option>';
  document.getElementById('regCategoryChild').disabled = true;
}