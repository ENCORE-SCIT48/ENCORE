/**
 * 채팅방 상세 페이지의 액션 핸들러를 관리합니다.
 * - 게시글 수정 페이지 이동 및 삭제 요청 기능을 수행합니다.
 */
$(function(){

    /** 수정 버튼 클릭 시 수정 폼 페이지로 이동 */
    $('#btn-edit').click(function(){
        const Id = $(this).data('id');
        const performanceId = $(this).data('performance-id');
        window.location.href = `/performance/${performanceId}/chat/${Id}/update`;
    });

    /** 삭제 버튼 클릭 시 서버에 삭제 API 요청 */
    $('#btn-delete').click(function(){
         const id = $(this).data('id');

         if(!id){
             alert('postId를 가져오지 못했습니다!');
             return;
         }

         if(confirm('정말 삭제하시겠습니까?')){
             $.ajax({
                 url: '/chat/' + id,
                 type: 'DELETE',
                 success: function(){
                     alert('삭제되었습니다.');
                     window.history.back();
                 },
                 error: function(xhr){
                     alert('삭제 실패: ' + xhr.responseText);
                 }
             });
         }
     });
 });
