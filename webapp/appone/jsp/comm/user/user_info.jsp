<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="col-12 col-sm-6 col-md-4 d-flex align-items-stretch flex-column">
	<div class="card bg-light d-flex flex-fill">
    	<div class="card-header text-muted border-bottom-0">Digital Strategist</div>
		<div class="card-body pt-0">
           	<div class="row">
               	<div class="col-7">
                   	<h2 class="lead"><b>Nicole Pearson</b></h2>
                     	<p class="text-muted text-sm"><b>About: </b> Web Designer / UX / Graphic Artist / Coffee Lover </p>
                     	<ul class="ml-4 mb-0 fa-ul text-muted">
                       	<li class="small">
                       		<span class="fa-li"><i class="fas fa-lg fa-building"></i></span>
                       		 Address: Demo Street 123, Demo City 04312, NJ
                      	</li>
                       	<li class="small"><span class="fa-li"><i class="fas fa-lg fa-phone"></i></span>
                       	 Phone #: + 800 - 12 12 23 52
						</li>
					</ul>
				</div>
				<div class="col-5 text-center">
					<img src="../../dist/img/user1-128x128.jpg" alt="user-avatar" class="img-circle img-fluid">
				</div>
			</div>
		</div>
		<div class="card-footer">
           	<div class="text-right">
				<a href="#" class="btn btn-sm bg-teal">
					<i class="fas fa-comments"></i>
				</a>
				<a href="#" class="btn btn-sm btn-primary">
                	<i class="fas fa-user"></i> View Profile
				</a>
			</div>
		</div>
	</div>
</div>
<!-- <!-- 사용자 등록 / 수정 POPUP -->
<div class="modal fade" id="modal_userProfile" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h6 class="modal-title">기본 정보</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body">
				<form class="form-horizontal">
					<div class="form-group row">
						<label for="in_userId" class="col-sm-3 control-label text-right"><span>*</span>아이디</label>
						<div class="col-lg-8">
							<input type="text" id=in_userId class="form-control form-control-sm">
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userName" class="col-sm-3 control-label text-right"><span>*</span>이름</label>
						<div class="col-lg-8">
							<input type="text" id="in_userName"  name="in_userName" class="form-control form-control-sm" placeholder="User Name">
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userDesk" class="col-sm-3 control-label text-right">부서</label>
						<div class="col-lg-8 common select-nonall" id="DEPT">
							<select id="in_userDesk"  name="in_userDept" class="form-control form-control-sm"></select>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userEmail" class="col-sm-3 control-label text-right">Email</label>
						<div class="col-lg-8">
							<input type="email" id="in_userEmail"  name="in_userEmail" class="form-control form-control-sm" placeholder="Email">
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userPhone" class="col-sm-3 control-label text-right">M-blie</label>
						<div class="col-lg-8">
		                    <input type="text" id="in_userPhone"  name="in_userPhone" class="form-control form-control-sm" placeholder="M-blie"
								data-inputmask="'mask': ['999-999-9999 [x99999]', '+099 99 99 9999[9]-9999']" data-mask/>
		                  </div>
					</div>
					<div class="form-group row mb-1">
						<label for="in_MailYn" class="col-sm-3 control-label text-right">메일수신</label>
						<div class="col-lg-2">
							<label id="check_label">
			                  <input type="checkbox" id="in_MailYn" class="flat-red" >
			                </label>
						</div>
						<label class="col-sm-3 control-label text-right">Color</label>
						<div class="col-lg-2">
							<div class="user-color-picker">
  								<input type="color" id="in_userColor" value="#8888ff" />
							</div>
						</div>
					</div>
					<div class="form-group row mb-1">
						<label for="in_SmsYn" class="col-sm-3 control-label text-right">SMS수신</label>
						<div class="col-lg-2">
							<label id="check_label">
			                	<input type="checkbox" id="in_SmsYn" class="flat-red" >
			                </label>
						</div>
						<label class="col-sm-3 control-label text-right">Avatar</label>
						<div class="col-lg-2">
							<div class="btn-group">
		                    <button type="button" class="btn btn-sm dropdown-toggle pl-0" data-toggle="dropdown" data-offset="-7">
		                      <img src="/scheduler/appone/plugins/dist/img/avatar.png" id="mainAvatar"
	              						class="user-image img-circle user-img-bordered-sm" alt="Avatar Image">
		                    </button>
		                    <div class="dropdown-menu main-dropdown-menu p-0 popAvatar" role="menu">
		                    	<a class="dropdown-avatarItem">
		                    		<img src="/scheduler/appone/plugins/dist/img/avatar.png" 
	              						class="user-image img-circle user-img-bordered-sm" alt="Avatar Image">
								</a>
		                    	<a class="dropdown-avatarItem">
		                    		<img src="/scheduler/appone/plugins/dist/img/avatar2.png" 
	              						class="user-image img-circle user-img-bordered-sm" alt="Avatar Image">
								</a>
		                    	<a class="dropdown-avatarItem">
		                    		<img src="/scheduler/appone/plugins/dist/img/avatar3.png" 
	              						class="user-image img-circle user-img-bordered-sm" alt="Avatar Image">
								</a>
		                    	<a class="dropdown-avatarItem">
		                    		<img src="/scheduler/appone/plugins/dist/img/avatar4.png" 
	              						class="user-image img-circle user-img-bordered-sm" alt="Avatar Image">
								</a>
		                    	<a class="dropdown-avatarItem">
		                    		<img src="/scheduler/appone/plugins/media/images/users/leeDregon.jpeg" 
	              						class="user-image img-circle user-img-bordered-sm" alt="Avatar Image">
								</a>
		                    </div>
		                  </div>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userStartPage" class="col-sm-3 control-label text-right">시작페이지</label>
						<div class="col-lg-8" id="STARTPAGE">
							<select id="in_userStartPage" name="in_userStartPage"
								class="form-control form-control-sm"></select>
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer  justify-content-between">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<a href="#" class="btn btn-sm btn-primary">
                      <i class="fas fa-user"></i> View Profile
                    </a>
					<button type="button" class="btn btn-sm btn-success float-right" id="btn_profile">
						<i class="far fa-address-card"></i> 상세 정보
                  	</button>
				</div>
			</div>
		</div>
	</div>
</div> -->