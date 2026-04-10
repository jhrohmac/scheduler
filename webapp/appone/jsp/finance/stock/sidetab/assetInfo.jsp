<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="card">
	<div class="card-header">
		<h3 class="card-title">가격설정</h3>
		<div class="card-tools">
			<button type="button" class="btn btn-light btn-sm" data-card-widget="collapse">
				<i class="fas fa-minus"></i>
			</button>
		</div>
	</div>
	<div class="card-body p-2">
		<div class="row">
        	<div class="col-sm-11">
            	<div class="progress progress-sm" style="height: 8px;">
               		<div class="progress-bar bg-danger progress-bar-striped" role="progressbar"
                    	aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 60%">
               		</div>
             	</div>
			</div>
            <div class="col-sm-1">
            	<span class="badge bg-danger">60%</span>
			</div>
		</div>
		<div class="row">
			<div class="col-4">
				<div class="form-group">
	                     	<label>수량</label>
	                    		<input type="text" class="form-control is-warning" id="priceCount" placeholder="수량(주)">
	                 	</div>
			</div>
			<div class="col-8">
				<div class="form-group">
	                     	<label>총금액</label>
	                    		<input type="text" class="form-control is-warning" id="totalOrderPrice" placeholder="총금액">
	                 	</div>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-6">
				<!-- text input -->
				<div class="form-group">
					<label>시작가격</label> <input type="text" class="form-control"
						id="startprice" placeholder="시작가격">
				</div>
			</div>
			<div class="col-sm-6">
				<div class="form-group">
					<label>목표가격</label> <input type="text" class="form-control"
						id="endprice" placeholder="목표가격">
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<div id="slider-range"></div>
			</div>
		</div>
	</div>
	<div class="card-footer">
		<button type="submit" id="priceBuy" class="btn btn-danger">매수</button>
		<button type="submit" id="priceSell"class="btn btn-primary">매도</button>
	</div>
</div>

<script type="text/javascript">

</script>