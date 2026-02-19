$(function () {

  /* =========================
   * 뒤로가기 버튼
   * ========================= */
  $('#backBtn').on('click', function () {
    location.href = '/posts/performer';
  });

  /* =========================
   * 등록 버튼
   * ========================= */
  $('#submitBtn').on('click', function () {
    createPost();
  });

  /* =========================
   * 수정 버튼 (수정 페이지)
   * ========================= */
  $('#updateBtn').on('click', function () {
    const postId = $('#postId').val();
    updatePost(postId);
  });

  /* =========================
   * 수정 버튼 (상세 페이지 이동)
   * ========================= */
  $('#editBtn').on('click', function () {
    const postId = $('.app-container').data('post-id');
    moveToEditPage(postId);
  });

  /* =========================
   * 삭제 버튼
   * ========================= */
  $('#deleteBtn').on('click', function () {
    const postId = $('.app-container').data('post-id');
    deletePost(postId);
  });

});


/* =========================
 * 공통 에러 처리 함수
 * ========================= */
function handleAjaxError(xhr) {
  if (xhr.status === 401) {
    alert('로그인이 필요합니다.');
    location.href = '/auth/login';
  } else if (xhr.status === 403) {
    alert('권한이 없습니다.');
  } else {
    alert('서버 오류가 발생했습니다.');
  }
}


/* =========================
 * 게시글 등록 (POST)
 * ========================= */
function createPost() {

  const title = $('input[name="title"]').val().trim();
  const content = $('textarea[name="content"]').val().trim();

  if (!title || !content) {
    alert('제목과 내용을 입력하세요.');
    return;
  }

  $.ajax({
    url: '/api/posts/performer',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify({
      title: title,
      content: content
    }),
    success: function (res) {
      if (res.success) {
        alert('게시글이 등록되었습니다.');
        location.href = '/posts/performer';
      } else {
        alert(res.message);
      }
    },
    error: function (xhr) {
      handleAjaxError(xhr);
    }
  });
}


/* =========================
 * 수정 페이지 이동
 * ========================= */
function moveToEditPage(postId) {

  if (!postId) {
    alert('게시글 ID가 없습니다.');
    return;
  }

  location.href = `/posts/performer/${postId}/edit`;
}


/* =========================
 * 게시글 수정 (PUT)
 * ========================= */
function updatePost(postId) {

  const title = $('input[name="title"]').val().trim();
  const content = $('textarea[name="content"]').val().trim();

  if (!title || !content) {
    alert('제목과 내용을 입력하세요.');
    return;
  }

  $.ajax({
    url: `/api/posts/performer/${postId}`,
    type: 'PUT',
    contentType: 'application/json',
    data: JSON.stringify({
      title: title,
      content: content
    }),
    success: function (res) {
      if (res.success) {
        alert('게시글이 수정되었습니다.');
        location.href = `/posts/performer/${postId}`;
      } else {
        alert(res.message);
      }
    },
    error: function (xhr) {
      handleAjaxError(xhr);
    }
  });
}


/* =========================
 * 게시글 삭제 (DELETE)
 * ========================= */
function deletePost(postId) {

  if (!confirm('정말 삭제하시겠습니까?')) {
    return;
  }

  $.ajax({
    url: `/api/posts/performer/${postId}`,
    type: 'DELETE',
    success: function (res) {
      if (res.success) {
        alert('게시글이 삭제되었습니다.');
        location.href = '/posts/performer';
      } else {
        alert(res.message);
      }
    },
    error: function (xhr) {
      handleAjaxError(xhr);
    }
  });
}
