package com.scheduler.management.controller;

import java.io.File;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.util.SeedUtil;
import com.scheduler.comm.util.Sha256Util;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.management.dao.ProfileDao;
import com.scheduler.management.vo.UserVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.util.SeedUtil;
import com.scheduler.comm.util.Sha256Util;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.management.dao.ProfileDao;
import com.scheduler.management.vo.UserVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.util.Base64Util;
import com.scheduler.comm.util.SeedUtil;
import com.scheduler.comm.util.Sha256Util;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.management.dao.ProfileDao;
import com.scheduler.management.vo.UserVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;

/**
 * 사용자 프로파일
 * - 기존 UserManage 쿼리 및 UserVo 재사용
 * - 세션 키: UserSession.KEY("USER_SESSION")
 */
@Controller
public class ProfileController {

    private ProfileDao profileDao;

    // 수동 주입(프로젝트 컨텍스트 설정에 따라 변경)
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    /**
     * (옵션) 직접 진입이 필요한 경우 뷰만 반환
     * 메뉴 URL은 이미 JSP 직접 로딩을 사용하므로 필수는 아님.
     */
    @RequestMapping("/profile/openProfile.do")
    public ModelAndView openProfile(HttpServletRequest req, HttpServletResponse res) {
        ModelAndView mav = new ModelAndView();
        // header에서 사용하는 EL 변수 세팅(세션 동기화)
        UserSession us = (UserSession) req.getSession().getAttribute(UserSession.KEY);
        if (us != null) {
            mav.addObject("user_nm", us.user_nm);
            mav.addObject("desk_div", us.desk_div);
            mav.addObject("user_avatar", us.user_avatar);
        }
        mav.setViewName("management/profile/profile");
        return mav;
    }

    /**
     * 내 프로필 조회
     */
    @RequestMapping("/profile/selectMyProfile.do")
    public void selectMyProfile(HttpServletRequest req, HttpServletResponse res) {

        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            UserSession us = (UserSession) req.getSession().getAttribute(UserSession.KEY);
          
            // UserManage.xml의 selectUserInfo 사용 (파라미터 키: in_userId)
            map.put("in_userId", us.user_id);
            UserVo vo = profileDao.selectMyProfile(map);
            
            // HP_NO 복호화(저장 시 암호화되어 있음)
            if (vo != null && vo.getHp_no() != null && !vo.getHp_no().trim().isEmpty()) {
                try {
                    String phone = SeedUtil.invokeDecrypt("tlsfk!2016", vo.getHp_no(), "UTF-8");
                    vo.setHp_no(phone);
                } catch (Exception ignore) {}
            }
        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setSingleData(vo);
        	resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
        	resultVo.setResult_msg(ResultMsg.SUCCESS_COMPLITE);
        	
        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);            
        }
    }

    /**
     * 기본 정보/환경 저장 (이름, 부서, 이메일, 휴대폰, 아바타, 시작페이지, 컬러 등)
     * - 기존 쿼리: sql.UserManage.updateUser, sql.UserManage.insertUserHistory 재사용
     */
    @RequestMapping("/profile/updateMyProfile.do")
    public void updateMyProfile(HttpServletRequest req, HttpServletResponse res) {

        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            UserSession us = (UserSession) req.getSession().getAttribute(UserSession.KEY);
            if (us == null) {
                ResponseHandler.sendResponse(res, ResultMsg.SESSION_FAILE_CODE, ResultMsg.SESSION_FAILE_MSG, null);
                return;
            }
            
            // 필수 파라미터
            map.put("in_userId", us.user_id);
            
            // 휴대폰은 암호화 저장
            if (map.get("in_userPhone") != null && !map.get("in_userPhone").isEmpty()) {
                String encPhone = SeedUtil.invokeEncrypt("tlsfk!2016", map.get("in_userPhone"), "UTF-8");
                map.put("in_userPhone", encPhone);
            }

            // [안전장치] 아바타 로컬경로(브라우저 fakepath) 방지
            String avatar = map.get("in_userAvatar");
            if (avatar != null && !avatar.trim().isEmpty()) {
                String ctx = req.getContextPath(); // "/scheduler"
                String allowedPrefix = ctx + "/appone/plugins/dist/img/avatar/";
                String lower = avatar.toLowerCase();

                boolean isLocalPath =
                        lower.startsWith("file:") ||
                        lower.contains(":\\") ||           // C:\...
                        lower.contains("fakepath");        // fakepath

                boolean isAllowedServerPath = avatar.startsWith(allowedPrefix);

                if (isLocalPath || !isAllowedServerPath) {
                    // 로컬경로나 허용된 경로가 아니면 저장 파라미터 제거 (변경 없음 처리)
                    map.remove("in_userAvatar");
                }
            }
            
            // 이벤트 구분(이력 남김)
            map.put("in_eventDiv", (map.get("in_eventDiv") == null || map.get("in_eventDiv").isEmpty()) ? "U" : map.get("in_eventDiv"));
            map.put("in_userAvatar", (map.get("in_userAvatar") == null || map.get("in_userAvatar").isEmpty()) ? "" : map.get("in_userAvatar"));
            // 조작자
            map.put("in_loginId", us.user_id);

            System.out.println("=======================================================");
            System.out.println(map.get("in_userAvatar"));
            profileDao.updateMyProfile(map);

            // 세션 최신화 (헤더 정보와 일치하게)
            if (map.get("in_userName") != null && !map.get("in_userName").isEmpty()) us.user_nm = map.get("in_userName");
            if (map.get("in_userDesk") != null && !map.get("in_userDesk").isEmpty()) us.desk_div = map.get("in_userDesk");
            if (map.get("in_userAvatar") != null && !map.get("in_userAvatar").isEmpty()) us.user_avatar = map.get("in_userAvatar");
            if (map.get("in_userColor") != null && !map.get("in_userColor").isEmpty()) us.user_color = map.get("in_userColor");
            if (map.get("in_userStartPage") != null && !map.get("in_userStartPage").isEmpty()) us.defaultPage = map.get("in_userStartPage");

        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
        	resultVo.setResult_msg(ResultMsg.SUCCESS_COMPLITE);
        	
        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);            
        }
    }

    /**
     * 비밀번호 변경 - 기존 로직과 동일하게 SHA-256 해시 사용
     * 기존 쿼리: sql.UserManage.updatePw
     */
    @RequestMapping("/profile/updateMyPassword.do")
    public void updateMyPassword(HttpServletRequest req, HttpServletResponse res) {

        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            UserSession us = (UserSession) req.getSession().getAttribute(UserSession.KEY);
        	
            if (us == null || us.user_id == null) {
            	new RuntimeException("세션 만료 또는 사용자 정보 없음");
            }
            
            String newPwd = map.get("in_userNewPwd");
            if (newPwd == null || newPwd.trim().isEmpty()) {
            	new RuntimeException("새 비밀번호를 입력해 주세요.");
            }
            map.put("in_userId", us.user_id);
            map.put("in_userPwd", Sha256Util.sha256(newPwd));

            profileDao.updateMyPassword(map);

        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
        	resultVo.setResult_msg(ResultMsg.SUCCESS_COMPLITE);
        	
        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);            
        }
    }


    /**
     * 아바타 파일 업로드
     * - 저장(실경로): /scheduler/webapp/appone/plugins/dist/img/avatar
     * - URL(반환):    /scheduler/appone/plugins/dist/img/avatar/{파일명}
     * - 크기:        215 x 215 px로 서버에서 중앙 크롭·리사이즈 후 저장
     * - 형식:        PNG로 저장(투명도 보존)
     */
    @RequestMapping("/profile/uploadAvatar.do")
    public void uploadAvatar(HttpServletRequest req, HttpServletResponse res) {
        try {
            UserSession us = (UserSession) req.getSession().getAttribute(UserSession.KEY);
            if (us == null) {
                ResponseHandler.sendResponse(res, ResultMsg.SESSION_FAILE_CODE, ResultMsg.SESSION_FAILE_MSG, null);
                return;
            }

            if (!(req instanceof MultipartHttpServletRequest)) {
                ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, "멀티파트 요청이 아닙니다.", null);
                return;
            }

            MultipartHttpServletRequest mreq = (MultipartHttpServletRequest) req;
            MultipartFile file = mreq.getFile("file");
            if (file == null || file.isEmpty()) file = mreq.getFile("in_userAvatar");
            if (file == null || file.isEmpty()) {
                ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, "업로드할 파일이 없습니다.", null);
                return;
            }

            String original = file.getOriginalFilename();
            String lower = (original == null) ? "" : original.toLowerCase();
            // 확장자 1차 검증(서버 지원 여부는 ImageIO.read 결과로 최종 판단)
            if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
               || lower.endsWith(".gif") || lower.endsWith(".webp"))) {
                ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, "허용되지 않은 확장자입니다.", null);
                return;
            }

            // 이미지 로드 (서버가 미지원 형식인 경우 null 반환 가능)
            BufferedImage src;
            try (InputStream is = file.getInputStream()) {
                src = ImageIO.read(is);
            }
            if (src == null) {
                ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, "이미지 형식을 서버가 지원하지 않습니다.", null);
                return;
            }

            // 215x215 중앙 크롭·리사이즈
            BufferedImage out = resizeCenterCrop(src, 215, 215);

            // 저장 폴더
            String saveDirPath = req.getSession().getServletContext().getRealPath("/appone/plugins/dist/img/avatar");
            File saveDir = new File(saveDirPath);
            if (!saveDir.exists()) saveDir.mkdirs();

            // 파일명: {userId}_{timestamp}.png  (항상 PNG로 저장)
            String ts = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            String safeUser = (us.user_id == null) ? "user" : us.user_id.replaceAll("[^A-Za-z0-9_.-]", "_");
            String saveName = safeUser + "_" + ts + ".png";
            File dest = new File(saveDir, saveName);

            // PNG로 저장
            boolean ok = ImageIO.write(out, "png", dest);
            if (!ok) {
                ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, "이미지 저장 실패(포맷).", null);
                return;
            }

            // 웹 접근 경로
            String webPath = req.getContextPath() + "/appone/plugins/dist/img/avatar/" + saveName;

            HashMap<String, Object> single = new HashMap<String, Object>();
            single.put("filePath", webPath);
            single.put("fileName", saveName);
            single.put("width", 215);
            single.put("height", 215);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(single);
            resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
            resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    
    /** 원본 비율 유지로 'cover' 방식 스케일 후 중앙 크롭하여 w x h로 반환 */
    private static BufferedImage resizeCenterCrop(BufferedImage src, int w, int h) {
        int sw = src.getWidth();
        int sh = src.getHeight();
        if (sw <= 0 || sh <= 0) {
            // 비정상 입력 방어
            BufferedImage blank = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            return blank;
        }

        // cover: 대상 프레임을 가득 채우도록 더 큰 축 기준으로 스케일
        double scale = Math.max((double) w / sw, (double) h / sh);
        int tw = (int) Math.round(sw * scale);
        int th = (int) Math.round(sh * scale);

        // 먼저 고해상도 스케일
        BufferedImage scaled = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, tw, th, null);
        g.dispose();

        // 중앙 크롭하여 최종 캔버스에 드로우
        int ox = (tw - w) / 2;
        int oy = (th - h) / 2;

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(scaled, -ox, -oy, null);
        g2.dispose();

        return out;
    }
}
