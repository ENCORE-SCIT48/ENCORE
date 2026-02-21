$(function () {

  /* =========================
   * 뒤로가기 버튼
   * ========================= */
  $('#backBtn').on('click', function () {
    location.href = '/posts/performance';
  });

  /* =========================
   * 등록 버튼
   * ========================= */
  $('#submitBtn').on('click', function () {
    createPerformancePost();
  });

  /* =========================
   * 수정 버튼 (글 수정 페이지)
   * ========================= */
  $('#updateBtn').on('click', function () {
    const postId = $('#postId').val();
    updatePerformancePost(postId);
  });

  /* =========================
   * 수정 버튼 (상세 → 수정 이동)
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
    deletePerformancePost(postId);
  });

  /* =========================
   * 신청 버튼
   * ========================= */
  $('#applyBtn').on('click', function () {
    applyToPost();
  });

});


/* =========================
 * 공연 모집글 등록 (POST)
 * ========================= */
function createPerformancePost() {

  const title = $('input[name="title"]').val().trim();
  const content = $('textarea[name="content"]').val().trim();
  const capacity = Number($('input[name="capacity"]').val());

  if (!title || !content) {
    alert('제목과 내용을 입력하세요.');
    return;
  }

  if (!capacity || capacity <= 0) {
    alert('모집 정원을 올바르게 입력하세요.');
    return;
  }

  $.ajax({
    url: '/api/posts/performance',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify({
      title: title,
      content: content,
      capacity: capacity
    }),
    success: function (res) {
      if (res.success) {
        alert('게시글이 등록되었습니다.');
        location.href = '/posts/performance';
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
 * 수정 페이지 이동
 * ========================= */
function moveToEditPage(postId) {

  if (!postId) {
    alert('게시글 ID가 없습니다.');
    return;
  }

  location.href = `/posts/performance/${postId}/edit`;
}


/* =========================
 * 공연 모집글 수정 (PUT)
 * ========================= */
function updatePerformancePost(postId) {

  const title = $('input[name="title"]').val().trim();
  const content = $('textarea[name="content"]').val().trim();
  const capacity = Number($('input[name="capacity"]').val());

  if (!title || !content) {
    alert('제목과 내용을 입력하세요.');
    return;
  }

  if (!capacity || capacity <= 0) {
    alert('모집 정원을 올바르게 입력하세요.');
    return;
  }

  $.ajax({
    url: `/api/posts/performance/${postId}`,
    type: 'PUT',
    contentType: 'application/json',
    data: JSON.stringify({
      title: title,
      content: content,
      capacity: capacity
    }),
    success: function (res) {
      if (res.success) {
        alert('게시글이 수정되었습니다.');
        location.href = `/posts/performance/${postId}`;
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
 * 공연 모집글 삭제 (DELETE)
 * ========================= */
function deletePerformancePost(postId) {

  if (!confirm('정말 삭제하시겠습니까?')) {
    return;
  }

  $.ajax({
    url: `/api/posts/performance/${postId}`,
    type: 'DELETE',
    success: function (res) {
      if (res.success) {
        alert('게시글이 삭제되었습니다.');
        location.href = '/posts/performance';
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
 * 공연 모집글 신청 (POST)
 * ========================= */
function applyToPost() {

  const postId = $('.app-container').data('post-id');

  if (!postId) {
    alert('게시글 정보가 없습니다.');
    return;
  }

  if (!confirm('이 게시글에 신청하시겠습니까?')) {
    return;
  }

  $.ajax({
    url: `/api/posts/${postId}/apply`,
    type: 'POST',
    success: function (res) {
      if (res.success) {
        alert('신청이 완료되었습니다.');
        location.reload();
      } else {
        alert(res.message);
      }
    },
    error: function (xhr) {

      if (xhr.responseJSON && xhr.responseJSON.message) {
        alert(xhr.responseJSON.message);
      } else {
        alert('서버 오류가 발생했습니다.');
      }
    }
  });
}