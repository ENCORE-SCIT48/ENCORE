$(function () {

  /* =========================
   * 뒤로가기 버튼
   * ========================= */
  $('#backBtn').on('click', function () {
    location.href = '/posts';
  });


  /* =========================
   * 등록 버튼
   * - 글 작성 페이지
   * ========================= */
  $('#submitBtn').on('click', function () {
    createPost();
  });

  /* =========================
   * 수정 버튼
   * - 글 수정 페이지
   * ========================= */
  $('#updateBtn').on('click', function () {
    const postId = $('#postId').val();
    updatePost(postId);
  });
  

  /* =========================
   * 수정 버튼 (페이지 이동)
   * - 글 상세 페이지
   * ========================= */
  $('#editBtn').on('click', function () {
    const postId = $('.app-container').data('post-id');
    moveToEditPage(postId);
  });


  /* =========================
   * 삭제 버튼
   * - 글 상세 페이지
   * ========================= */
  $('#deleteBtn').on('click', function () {
    const postId = $('.app-container').data('post-id');
    deletePost(postId);
  });

});


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
    url: '/api/posts',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify({
      title: title,
      content: content
    }),
    success: function (res) {
      if (res.success) {
        alert('게시글이 등록되었습니다.');
        location.href = '/posts';
      } else {
        alert(res.message);
      }
    },
    error: function () {
      alert('서버 오류가 발생했습니다.');
    }
  });
}

/* =========================
 * 수정 페이지 이동 (GET)
 * ========================= */
function moveToEditPage(postId) {
  if (!postId) {
    alert('게시글 ID가 없습니다.');
    return;
  }
  location.href = `/posts/${postId}/edit`;
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
    url: `/api/posts/${postId}`,
    type: 'PUT',
    contentType: 'application/json',
    data: JSON.stringify({
      title: title,
      content: content
    }),
    success: function (res) {
      if (res.success) {
        alert('게시글이 수정되었습니다.');
        location.href = `/posts/${postId}`;
      } else {
        alert(res.message);
      }
    },
    error: function () {
      alert('서버 오류가 발생했습니다.');
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
    url: `/api/posts/${postId}`,
    type: 'DELETE',
    success: function (res) {
      if (res.success) {
        alert('게시글이 삭제되었습니다.');
        location.href = '/posts';
      } else {
        alert(res.message);
      }
    },
    error: function () {
      alert('서버 오류가 발생했습니다.');
    }
  });
}
