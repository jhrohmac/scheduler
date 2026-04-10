<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ul class="nav nav-pills flex-column" id="group_list" >
	<c:forEach var="groupInfo" items="${groupList}">
		<li class="nav-item">
			<a class="nav-link" style="cursor: pointer;" onclick="fn_selectGroupStockList('${groupInfo.getGroup_id()}')"> 
				${groupInfo.getGroup_name()}
					<span class="btn  btn-light btn-sm float-right" onclick="fn_portfolioGroupEvent('D','${groupInfo.getGroup_id()}','${groupInfo.getGroup_name()}')">
						<i class="fas fa-trash text-gray"></i>
					</span>
					<span class="btn  btn-light btn-sm float-right" onclick="fn_portfolioGroupEvent('U','${groupInfo.getGroup_id()}','${groupInfo.getGroup_name()}')">
						<i class="fas fa-edit text-gray"></i>
					</span>
					<span class="btn  btn-light btn-sm float-right" onclick="fn_portfolioGroupEvent('C','${groupInfo.getGroup_id()}','${groupInfo.getGroup_name()}')">
						<i class="fas fa-copy text-gray"></i>
					</span>
					<span class="btn  btn-light btn-sm float-right">${groupInfo.getStock_count()}
					</span>
			</a>
			<input type="hidden" class="sort_code" value="${groupInfo.getGroup_id()}">
		</li>
	</c:forEach>
</ul>
<script>

$("#group_list").sortable({
	placeholder : 'sort-placeholder',
	forcePlaceholderSize : true,
	start : function(e, ui) {
		ui.item.data('start-pos', ui.item.index() + 1);
	},
	stop : function(e, ui) {
		var list = []; // Create an empty array to store the values
		ui.item.parent().find('li.nav-item').each(
			function(idx, el) {
				var $this = $(el);
				 $this.find('.sort_code').text();
				 var value = $this.find('.sort_code').val(); // Retrieve the text value
				 list.push(value); // Add the value to the list array
			});
		var url = "/scheduler/finance/portfolioGroupSort.do";
		var param = "group_List="+list;
		var type = "json";
		fn_sendAjax(url, type, param, fn_groupSortResult);
		function fn_groupSortResult(data){
			//fn_groupList();	//그룹 조회
			fn_transform(div_finance_main,'#STOCKGROUP_LIST', 'combobox');
		}
	}
});
</script>
