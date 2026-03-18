/* ===== 수정 모달 열기 ===== */
async function openEditModal(bookKey) {
  try {
    const response = await fetch(`/api/books/${bookKey}`);
    if (!response.ok) { showToast((await response.json()).message, 'error'); return; }
    const book = await response.json();

    document.getElementById('editBookKey').value   = book.bookKey;
    document.getElementById('editBookName').value  = book.bookName  || '';
    document.getElementById('editAuthName').value  = book.authName  || '';
    document.getElementById('editPubName').value   = book.pubName   || '';
    document.getElementById('editPubDate').value   = book.pubDate   || '';
    document.getElementById('editBookIndex').value = book.bookIndex || '';
    document.getElementById('editBookPrice').value = book.bookPrice || '';

    // ✅ 카테고리 로드 후 현재 값 설정
    await loadEditCategoryOptions(book.category);

    document.getElementById('editModal').classList.add('show');
  } catch (e) {
    showToast('도서 정보 조회 중 오류가 발생했습니다.', 'error');
  }
}

/* ===== 수정 모달 카테고리 로드 ===== */
async function loadEditCategoryOptions(currentCategory) {
  try {
    // 1차 분류 로드
    const res = await fetch('/api/categories/parents');
    const parents = await res.json();
    const parentSelect = document.getElementById('editCategoryParent');
    parentSelect.innerHTML = '<option value="">1차 분류 선택</option>';
    parents.forEach(c => {
      const option = document.createElement('option');
      option.value = c.code;
      option.textContent = c.name;
      parentSelect.appendChild(option);
    });

    // 현재 카테고리가 있으면 1차 분류 선택
    if (currentCategory) {
      const parentCode = currentCategory.split('-')[0];  // "01-04" → "01"
      parentSelect.value = parentCode;
      await onEditParentCategoryChange(parentCode, currentCategory);
    } else {
      document.getElementById('editCategoryChild').innerHTML = '<option value="">2차 분류 선택</option>';
      document.getElementById('editCategoryChild').disabled = true;
    }
  } catch (e) {}
}

/* ===== 수정 모달 1차 분류 선택 시 2차 로드 ===== */
async function onEditParentCategoryChange(parentCode, selectedChild = null) {
  const childSelect = document.getElementById('editCategoryChild');
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
    if (selectedChild) childSelect.value = selectedChild;  // 현재 값 선택
  } catch (e) {}
}

/* ===== 수정 모달 닫기 ===== */
function closeEditModal() {
  document.getElementById('editModal').classList.remove('show');
}

/* ===== 도서 수정 ===== */
async function updateBook() {
  const bookKey  = document.getElementById('editBookKey').value;
  const category = document.getElementById('editCategoryChild').value
      || document.getElementById('editCategoryParent').value
      || null;

  const data = {
    bookName:  document.getElementById('editBookName').value.trim(),
    authName:  document.getElementById('editAuthName').value.trim()  || null,
    pubDate:   document.getElementById('editPubDate').value          || null,
    bookIndex: document.getElementById('editBookIndex').value.trim() || null,
    pubName:   document.getElementById('editPubName').value.trim()   || null,
    bookPrice: document.getElementById('editBookPrice').value
        ? parseInt(document.getElementById('editBookPrice').value) : null,
    category  // ✅ 추가
  };

  try {
    const response = await fetch(`/api/books/${bookKey}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!response.ok) { showToast((await response.json()).message, 'error'); return; }
    showToast('도서가 수정되었습니다.', 'success');
    closeEditModal();
    loadBooks(0);
  } catch (e) {
    showToast('도서 수정 중 오류가 발생했습니다.', 'error');
  }
}

/* ===== 도서 삭제 ===== */
async function deleteBook(bookKey) {
  if (!confirm('해당 도서를 삭제하시겠습니까?')) return;
  try {
    const response = await fetch(`/api/books/${bookKey}`, { method: 'DELETE' });
    if (!response.ok && response.status !== 204) {
      showToast((await response.json()).message, 'error');
      return;
    }
    showToast('도서가 삭제되었습니다.', 'success');
    loadBooks(0);
  } catch (e) {
    showToast('도서 삭제 중 오류가 발생했습니다.', 'error');
  }
}