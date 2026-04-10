<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script src="/scheduler/appone/plugins/bs-custom-file-input/bs-custom-file-input.min.js"></script>
<!-- ADD: bootstrap-colorpicker (project path 규칙 준수) -->
<link rel="stylesheet" href="/scheduler/appone/plugins/bootstrap-colorpicker/css/bootstrap-colorpicker.min.css">
<script src="/scheduler/appone/plugins/bootstrap-colorpicker/js/bootstrap-colorpicker.min.js"></script>

<div class="container-fluid" id="profileView">
  <div class="row">
    <div class="col-md-3">
      <div class="card card-primary card-outline">
        <div class="card-body box-profile">
          <div class="text-center">
            <img id="pf_user_avatar"
                 class="profile-user-img img-fluid img-circle"
                 src="/scheduler/appone/plugins/dist/img/avatar.png">
          </div>
          <h3 class="profile-username text-center" id="pf_user_nm"></h3>
          <p class="text-muted text-center" id="pf_desk_div"></p>
          <ul class="list-group list-group-unbordered mb-3">
            <li class="list-group-item"><b>ID</b> <span class="float-right" id="pf_user_id"></span></li>
            <li class="list-group-item"><b>Email</b> <span class="float-right" id="pf_email"></span></li>
            <li class="list-group-item"><b>Phone</b> <span class="float-right" id="pf_hp_no"></span></li>
            <li class="list-group-item"><b>Role</b> <span class="float-right" id="pf_role"></span></li>
          </ul>
          <a href="#" id="btnOpenPwd" class="btn btn-primary btn-block"><b>비밀번호 변경</b></a>
        </div>
      </div>
    </div>

    <div class="col-md-9">
      <div class="card">
        <div class="card-header p-2">
          <ul class="nav nav-pills">
            <li class="nav-item"><a class="nav-link active" href="#tab_basic" data-toggle="tab">기본 정보</a></li>
            <li class="nav-item"><a class="nav-link" href="#tab_pwd" data-toggle="tab">비밀번호 변경</a></li>
          </ul>
        </div>
        <div class="card-body">
          <div class="tab-content">

            <div class="active tab-pane" id="tab_basic">
              <form id="frmProfile" class="form-horizontal">
                <input type="hidden" id="in_userId">
                <div class="form-group row">
                  <label class="col-sm-2 col-form-label">이름</label>
                  <div class="col-sm-10"><input type="text" class="form-control" id="in_userName" placeholder="이름"></div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-2 col-form-label">부서(Desk)</label>
                  <div class="col-sm-10"><input type="text" class="form-control" id="in_userDesk" placeholder="부서/Desk"></div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-2 col-form-label">이메일</label>
                  <div class="col-sm-10"><input type="email" class="form-control" id="in_userEmail" placeholder="email@company.com"></div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-2 col-form-label">휴대폰</label>
                  <div class="col-sm-10"><input type="text" class="form-control" id="in_userPhone" placeholder="010-0000-0000"></div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-2 col-form-label">시작페이지</label>
                  <div class="col-sm-3" id="STARTPAGE">
                    <select id="in_userStartPage" class="form-control"></select>
                  </div>
                </div>
                <!-- CHANGED: 테마색상 -> bootstrap-colorpicker 사용 (파라미터 in_userColor 유지) -->
                <div class="form-group row">
                	<label class="col-sm-2 col-form-label">테마색상</label>
                  	<div class="col-sm-2">
						<div class="user-color-picker">
							<input type="color" id="in_userColor" value="#8888ff" />
						</div>
						<small class="form-text text-muted">HEX 형식 예: #80BFF9</small>
					</div>
                </div>
                <!-- 파일 업로드 -->
                <div class="form-group row">
                  <label class="col-sm-2 col-form-label">프로필 사진</label>
                  <div class="col-sm-3">
                    <div class="custom-file">
                      <label class="custom-file-label" for="customFile">Choose file</label>
                      <input type="file" class="custom-file-input" id="in_userAvatar" accept=".png,.jpg,.jpeg,.gif,.webp">
                    </div>
                    <small class="form-text text-muted">허용: png, jpg, jpeg, gif, webp</small>
                  </div>
                </div>

                <div class="form-group row mb-0">
                  <div class="offset-sm-2 col-sm-10">
                    <button type="button" id="btnProfileSave" class="btn btn-primary">
                      <i class="fas fa-save mr-1"></i> 저장
                    </button>
                  </div>
                </div>
              </form>
            </div>

            <div class="tab-pane" id="tab_pwd">
              <form id="frmPwd" class="form-horizontal">
                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">새 비밀번호</label>
                  <div class="col-sm-9"><input type="password" class="form-control" id="in_userNewPwd" autocomplete="new-password"></div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">새 비밀번호 확인</label>
                  <div class="col-sm-9"><input type="password" class="form-control" id="in_userNewPwd2" autocomplete="new-password"></div>
                </div>
                <div class="form-group row mb-0">
                  <div class="offset-sm-3 col-sm-9">
                    <button type="button" id="btnPwdSave" class="btn btn-primary">
                      <i class="fas fa-key mr-1"></i> 변경
                    </button>
                  </div>
                </div>
              </form>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script>
var profileView = $("#profileView");

$(function(){
  fn_userStartPageList();
  loadProfile();
  
  $('#btnOpenPwd').on('click', function(e){
    e.preventDefault();
    $('.nav a[href="#tab_pwd"]').tab('show');
    $('#in_userNewPwd').focus();
  });

  $('#btnProfileSave').on('click', saveProfile);
  $('#btnPwdSave').on('click', savePwd);

  // 로컬 미리보기 (file:/// 사용 금지, Blob URL 사용)
  $('#in_userAvatar').on('change', function(e){
    var f = e.target.files && e.target.files[0];
    if(!f) return;
    try{
      var blobUrl = (window.URL||window.webkitURL).createObjectURL(f);
      $('#pf_user_avatar').attr('src', blobUrl).one('load', function(){
        (window.URL||window.webkitURL).revokeObjectURL(blobUrl);
      });
    }catch(err){
      var r = new FileReader();
      r.onload = function(ev){ $('#pf_user_avatar').attr('src', ev.target.result); };
      r.readAsDataURL(f);
    }
  });

  // init colorpicker (HEX, no alpha)
  $('#cp_in_userColor').colorpicker({
    format: 'hex',
    useAlpha: false,
    autoInputFallback: true
  }).on('colorpickerChange colorpickerCreate', function(e){
    // 동기화: input value 항상 #RRGGBB
    var hex = (e.color && e.color.toString('hex')) ? e.color.toString('hex').toUpperCase() : $('#in_userColor').val();
    if(hex && hex.charAt(0) !== '#') hex = '#'+hex;
    $('#in_userColor').val(hex);
  });
  
  if (window.bsCustomFileInput && bsCustomFileInput.init) bsCustomFileInput.init();
});
//---------- helpers ----------
function normalizeHex(v){
  if(!v) return null;
  v = (''+v).trim();
  if(v[0] !== '#') v = '#'+v;
  if(/^#([0-9a-fA-F]{6})$/.test(v)) return v.toUpperCase();
  if(/^#([0-9a-fA-F]{3})$/.test(v)) return ('#'+v[1]+v[1]+v[2]+v[2]+v[3]+v[3]).toUpperCase();
  return null;
}
/** params helper: 다양한 타입 지원 */
function addParam(params, key, val){
  if (params instanceof URLSearchParams){ params.append(key,val); return params; }
  if ($ && $.isPlainObject && $.isPlainObject(params)){ params[key]=val; return params; }
  if (typeof params === 'string'){ return params + (params ? '&':'') + encodeURIComponent(key)+'='+encodeURIComponent(val); }
  var obj={}; obj[key]=val; return obj;
}
function removeParam(params, key){
  if (params instanceof URLSearchParams){ params.delete(key); return params; }
  if ($ && $.isPlainObject && $.isPlainObject(params)){ delete params[key]; return params; }
  if (typeof params === 'string'){
    // key=... 제거
    var re = new RegExp('(?:^|&)'+encodeURIComponent(key)+'=[^&]*&?','g');
    var s = params.replace(re,'&');
    if (s[0]==='&') s=s.substring(1);
    if (s[s.length-1]==='&') s=s.substring(0,s.length-1);
    return s;
  }
  return params;
}

function loadProfile(){
  ajaxCall('/scheduler/profile/selectMyProfile.do', 'json', {}, function(res){
    if(res && res.singleData){
      var u = res.singleData;
      
      $('#pf_user_id').text(u.user_id||'');
      $('#pf_user_nm').text(u.user_nm||'');
      $('#pf_desk_div').text(u.desk_div||'');
      $('#pf_email').text(u.email||'');
      $('#pf_hp_no').text(u.hp_no||'');
      $('#pf_role').text(u.role_desc||u.role||'');
      console.log("===u.user_avatar=============="+u.user_avatar);
      $('#pf_user_avatar').attr('src', u.user_avatar);
      $('#in_userId').val(u.user_id||'');
      $('#in_userName').val(u.user_nm||'');
      $('#in_userDesk').val(u.desk_div||'');
      $('#in_userEmail').val(u.email||'');
      $('#in_userPhone').val(u.hp_no||'');
      //$('#in_userColor').val(u.user_color||'');
      $('#in_userStartPage').val(u.defaultpage||'');
      var color = normalizeHex(u.user_color || '#80BFF9') || '#80BFF9';
      $('#in_userColor').val(color);      
      // plugin value도 업데이트
      try { $('#cp_in_userColor').colorpicker('setValue', color); } catch(e) {}      
    }
  });
}

// 업로드 → 서버경로 획득 → in_userAvatar 주입(로컬 fakepath 제거) → 저장
function saveProfile(){
  var fileEl = $('#in_userAvatar')[0];
  var params = getJQParams(profileView);

  // 색상값 보정(#RRGGBB) 및 강제 세팅
  var fixedColor = normalizeHex($('#in_userColor').val()) || '#80BFF9';
  params = removeParam(params, 'in_userColor');
  params = addParam(params, 'in_userColor', fixedColor);
  
  // file input 값으로 들어간 fakepath 제거(중복 방지)
  params = removeParam(params, 'in_userAvatar');

  var doUpdate = function(finalParams){
    ajaxCall('/scheduler/profile/updateMyProfile.do', 'json', finalParams, function(res){
      if(res && res.result_code === '0000'){
        showAlert('success','프로필이 저장되었습니다.',1200, loadProfile);
      }else{
        showAlert('error', (res && res.result_msg) ? res.result_msg : '저장 실패', 1600);
      }
    });
  };

  // 파일 미선택: 바로 저장 (아바타 변경 없음)
  if(!fileEl || !fileEl.files || fileEl.files.length === 0){
    doUpdate(params);
    return;
  }

  // 파일 업로드
  var fd = new FormData();
  fd.append('file', fileEl.files[0]);
  $.ajax({
    url: '/scheduler/profile/uploadAvatar.do',
    type: 'POST',
    data: fd,
    processData: false,
    contentType: false,
    dataType: 'json',
    success: function(res){
      if(res && res.singleData && res.singleData.filePath){
        var finalParams = addParam(params, 'in_userAvatar', res.singleData.filePath);
        $('.img-circle').attr('src', res.singleData.filePath);
        doUpdate(finalParams);
      }else{
        showAlert('error','파일 업로드에 실패했습니다.',1600);
      }
    },
    error: function(){ showAlert('error','파일 업로드 중 오류가 발생했습니다.',1600); }
  });
}

function savePwd(){
  var p1=$('#in_userNewPwd').val(), p2=$('#in_userNewPwd2').val();
  if(!p1||!p2){ showAlert('warning','비밀번호를 입력하세요',1200); return; }
  if(p1!==p2){ showAlert('warning','비밀번호가 일치하지 않습니다',1200); return; }
  var params=new URLSearchParams();
  params.append('in_userNewPwd',p1);
  params.append('in_userNewPwd2',p2);
  ajaxCall('/scheduler/profile/updateMyPassword.do','json',params,function(res){
    if(res && res.result_code==='0000'){
      showAlert('success','비밀번호가 변경되었습니다.',1200,function(){
        $('#in_userNewPwd,#in_userNewPwd2').val('');
        $('.nav a[href="#tab_basic"]').tab('show');
      });
    }else{
      showAlert('error', (res && res.result_msg) ? res.result_msg : '변경 실패', 1600);
    }
  });
}

function fn_userStartPageList(){
  ajaxCall("/scheduler/user/userStartPageList.do",'html',"",function(html){
    $('#STARTPAGE select').html(html);
  });
}
</script>
