<%@ page contentType="text/html; charset=utf-8" %>
<script type="text/javascript">
$(document).ready(function() {
    var url = "appone/jsp/${pop_url}.jsp"; // 서버에서 전달한 URL 값을 가져옵니다.
    window.open(url, "_blank"); // 새 창을 엽니다.
  });
</script>