$(function(){
    // 수정 버튼 클릭
    $('#btn-edit').click(function(){
        const postId = $(this).data('post-id');
        const performanceId = $(this).data('performance-id');
        window.location.href = `/performance/${performanceId}/chat/${postId}/update`;
    });

    // 삭제 버튼 클릭
     $('#btn-delete').click(function(){
         const postId = $(this).data('post-id'); // 여기서 postId가 숫자로 들어오는지 확인!
         if(!postId){
             alert('postId를 가져오지 못했습니다!');
             return;
         }

         if(confirm('정말 삭제하시겠습니까?')){
             $.ajax({
                 url: '/chat/' + postId,
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
