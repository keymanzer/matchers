package kr.or.kosa.quotationBoard.controller;

import kr.or.kosa.admin.service.AdminService;
import kr.or.kosa.attachedFile.dto.AttachedFile;
import kr.or.kosa.attachedFile.service.AttachedFileService;
import kr.or.kosa.board.dto.Board;
import kr.or.kosa.board.service.BoardService;
import kr.or.kosa.common.S3Service;
import kr.or.kosa.expert.dto.Category;
import kr.or.kosa.expert.dto.Location;
import kr.or.kosa.expert.service.ExpertService;
import kr.or.kosa.quotationBoard.dto.QuotationBoard;
import kr.or.kosa.quotationBoard.dto.location;
import kr.or.kosa.quotationBoard.service.QuotationBoardService;
import kr.or.kosa.user.dto.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user/quotationBoard")
public class QuotationBoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private QuotationBoardService quotationBoardService;

    @Autowired
    private AttachedFileService attachedFileService;

    // ★ 새로 추가: DB에서 지역·카테고리 가져올 서비스
    @Autowired
    private ExpertService expertService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AdminService adminService;
    // 게시판 생성 화면 요청

    @GetMapping("/create")
    public String showCreateQuotationBoardForm(Model model) {
        model.addAttribute("quotationBoard", new QuotationBoard());
        // DB에서 실제 지역 목록 가져오기
        List<Location> locations = expertService.getLocationList();
        model.addAttribute("locations", locations);
        // city 만 뽑아서 중복 제거
        List<String> cities = locations.stream()
                .map(Location::getCity)
                .distinct()
                .collect(Collectors.toList());
        model.addAttribute("cities", cities);


        // DB에서 실제 카테고리 목록 가져오기
        model.addAttribute("categories", expertService.getCategoryList());


        System.out.println("model = " + model);
        System.out.println("크리에이트 폼요청");
        return "quotationBoard/create";  // 게시판 생성 폼을 표시하는 뷰
    }

    @GetMapping("/create/{expertId}")
    public String showCreateQuotationBoardFormExpert(Model model,@PathVariable Long expertId) {
        QuotationBoard qb = new QuotationBoard();
        qb.setExpertId(expertId);
        qb.setState("진행중");

        // DB에서 실제 지역 목록 가져오기
        List<Location> locations = expertService.getLocationList();
        model.addAttribute("locations", locations);
        // city 만 뽑아서 중복 제거
        List<String> cities = locations.stream()
                .map(Location::getCity)
                .distinct()
                .collect(Collectors.toList());
        model.addAttribute("cities", cities);
        model.addAttribute("quotationBoard", qb);
        //DB에서 실제 카테고리 목록 가져오기
        model.addAttribute("categories", expertService.getCategoryList());

        return "quotationBoard/create";  // 게시판 생성 폼을 표시하는 뷰
    }


    // 게시판 생성 처리
    // 2) 게시글 + 지역 + 첨부파일 생성 처리
    @PostMapping("/create")
    @Transactional
    public String createQuotationBoard(
            @ModelAttribute QuotationBoard quotationBoard,
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam("locationIds") List<Integer> locationIds,
            @RequestParam(name = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
            @RequestParam(name="expertId", defaultValue = "0") long expertId
    ) {

        // 수동으로 state 값 설정
        if (quotationBoard.getState() == null || quotationBoard.getState().isEmpty()) {
            quotationBoard.setState("진행전");
        }
        System.out.println("State: " + quotationBoard.getState());
        // --- 2-1. Board 테이블에 삽입 ---
        Board board = new Board();
        long userId = customUser.getUserId();
        board.setTitle(quotationBoard.getTitle());
        board.setContent(quotationBoard.getContent());
        board.setUserNickname(customUser.getNickname());
        board.setUserId(userId);

        long postId = boardService.getNextBoardId();
        board.setPostId(postId);
        boardService.createBoard(board);

        // --- 2-2. QuotationBoard 테이블에 삽입 ---
        quotationBoard.setPostId(postId);
        quotationBoard.setUserNickname(customUser.getNickname());
        quotationBoard.setUserId(userId);
        quotationBoard.setExpertId(expertId);
        System.out.println("quotationBoard: 삽입전 마지막 테스트" + quotationBoard);
        quotationBoardService.createQuotationBoard(quotationBoard);

        // --- 2-3. Quotation_Location 매핑 삽입 ---
        for (Integer locId : locationIds) {
            quotationBoardService.addQuotationLocation(postId, locId);
        }


        if (uploadFiles != null && !uploadFiles.isEmpty()) {
            System.out.println("uploadFiles.size() = " + uploadFiles.size());
            for (MultipartFile file : uploadFiles) {
                if (!file.isEmpty()) {
                    // S3에 파일 업로드
                    String fileUrl = null;
                    try {
                        // S3에 업로드
                        fileUrl = s3Service.uploadFile(file, "quotation_Board"); // "post"는 파일 폴더 구분용
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue; // 업로드 실패 시, 다음 파일로 넘어감
                    }

                    // DB 메타 삽입 (S3 URL 저장)
                    AttachedFile af = new AttachedFile();
                    af.setPostId(postId);
                    af.setName(file.getOriginalFilename()); // 파일 이름 저장
                    af.setPath(fileUrl); // S3 URL 저장
                    attachedFileService.saveAttachedFileMetadata(af);
                }
            }
        }



        return "redirect:/user/quotationBoard/myrequest";
    }

    @GetMapping("/api/delete/{postId}") // PUT에서 DELETE로 변경
    @ResponseBody
    public ResponseEntity<?> deleteQuotation(@PathVariable Long postId) {
        System.out.println("deleteQuotation 호출됨 - postId: " + postId);

        try {
            // 견적 삭제를 위한 QuotationBoard 객체 생성
            QuotationBoard quotationBoard = new QuotationBoard();
            quotationBoard.setPostId(postId);
            System.out.println("업데이트 전 quotationBoard: " + quotationBoard);
            quotationBoard.setState("삭제");
            quotationBoard.setExpertId(0); // null로 설정

            quotationBoardService.updateQuotationBoard(quotationBoard);

            System.out.println("삭제 완료 - postId: " + postId);

            // JSON 응답 반환
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "견적이 삭제되었습니다.");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (Exception e) {
            System.err.println("삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "삭제 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @PostMapping("/update")
    public String updateQuotationBoard(QuotationBoard quotationBoard) {
        System.out.println(quotationBoard);
        quotationBoardService.updateQuotationBoard(quotationBoard);
        return "redirect:/user/quotationBoard/myquotation"; // 수정 후 목록 페이지 이동
    }

    @PostMapping("/delete")
    public String deleteQuotationBoard(long postId) {
        System.out.println(postId);
        quotationBoardService.deleteQuotationBoard(postId);
        return "redirect:/user/quotationBoard/list";
    }


    @GetMapping("/list")
    public String quotationBoardList(Model model,
                                     @AuthenticationPrincipal CustomUser customUser,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam (required = false)Long categoryId,
                                     @RequestParam (required = false)Integer locationId) {
        // 페이지당 항목 수
        int pageSize = 10;

        long userId = customUser.getUserId(); //로그인한 전문가 ID
        // 전체 견적 요청 목록 조회
        System.out.println("categoryID: "+categoryId+", LocationId: "+locationId);
        List<QuotationBoard> allBoards = quotationBoardService.findAllQuotationBoards(userId,categoryId,locationId);
        //customUser.getUserId(); 2로 되어있는거 이걸로 나중에 대체 userid로 대체 아직 보드가 int타입임
        List<Category> categories= quotationBoardService.findCategoriesByUserId(userId);
        List<location> locations= quotationBoardService.findLocationsByUserId(userId);

        // 전체 항목 수
        int totalItems = allBoards.size();

        // 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // 페이지 유효성 검사
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        // 현재 페이지에 해당하는 데이터만 추출
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<QuotationBoard> currentPageBoards;
        if (startIndex < totalItems) {
            currentPageBoards = allBoards.subList(startIndex, endIndex);
        } else {
            currentPageBoards = new ArrayList<>();
        }



        // 모델에 데이터 추가
        model.addAttribute("quotationBoards", currentPageBoards);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("categories", categories);
        model.addAttribute("locations", locations);


        return "quotationBoard/list";
    }


    @GetMapping("/list/{postId}")
    public String quotationBoardDetail(@PathVariable long postId, Model model) {
        QuotationBoard qb = quotationBoardService.findByPostIdWithLocations(postId);
        List<AttachedFile> attachedFiles = attachedFileService.findByPostId(postId);
        model.addAttribute("quotationBoard", qb);
        model.addAttribute("attachedFiles", attachedFiles);
        return "quotationBoard/detail";
    }
    // ★ 여기에 JSON 데이터만 반환하는 API 추가 ★
    @GetMapping(
            value = "/api/detail/{postId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public QuotationBoard getQuotationBoardDetailApi(Model model ,@PathVariable long postId) {
        // postId에 맞는 QuotationBoard 객체를 JSON으로 직렬화해서 리턴
        QuotationBoard qb = quotationBoardService.findByPostIdWithLocations(postId);
        List<AttachedFile> files = attachedFileService.findByPostId(postId);

        // 4) DTO에 세팅
        qb.setAttachedFiles(files);

        return qb;
    }




    @GetMapping("/myrequest")
    public String myRequestList(Model model, @AuthenticationPrincipal CustomUser customUser) {

        return "quotationBoard/myrequest";
    }

    // 2. 비동기 데이터 로드 (JSON 반환)
    @GetMapping("/api/myrequest")
    @ResponseBody
    public ResponseEntity<List<QuotationBoard>> getMyRequestsByStatus(
            @RequestParam String status, // 프론트에서 보낸 상태값 (pending, progress, completed)
            @AuthenticationPrincipal CustomUser customUser) {

        try {
            long userId = customUser.getUserId();

            // 프론트의 상태값을 서비스에서 사용하는 상태값으로 변환
            String serviceStatus = convertToServiceStatus(status);
            System.out.println("serviceStatus = " + serviceStatus);

            // 서비스 호출
            List<QuotationBoard> quotes = quotationBoardService.findMyRequests(userId, serviceStatus);
            System.out.println("quotes = " + quotes);

            return ResponseEntity.ok(quotes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/myquotation")
    public String myquotationList(Model model, @AuthenticationPrincipal CustomUser customUser) {

        return "quotationBoard/myquotation";
    }
    // 2. 비동기 데이터 로드 (JSON 반환)
    @GetMapping("/api/myquotation")
    @ResponseBody
    public ResponseEntity<List<QuotationBoard>> getMyQuotationsByStatus(
            @RequestParam String status, // 프론트에서 보낸 상태값 (pending, progress, completed)
            @AuthenticationPrincipal CustomUser customUser) {

        try {
            long userId = customUser.getUserId();

            // 프론트의 상태값을 서비스에서 사용하는 상태값으로 변환
            String serviceStatus = convertToServiceStatus(status);
            System.out.println("serviceStatus = " + serviceStatus);

            // 서비스 호출
            List<QuotationBoard> quotes = quotationBoardService.findMyQuotations(userId, serviceStatus);
            System.out.println("quotes = " + quotes);

            return ResponseEntity.ok(quotes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 상태값 변환 메서드 (프론트 ↔ 서비스 간 상태값이 다를 경우)
    private String convertToServiceStatus(String frontendStatus) {
        switch (frontendStatus) {
            case "pending":
                return "진행전"; // 또는 서비스에서 사용하는 실제 상태값
            case "progress":
                return "진행중";
            case "completed":
                return "완료";
            default:
                return "진행전";
        }
    }

    // QuotationBoardController 클래스에 추가
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            // 파일 정보 조회
            AttachedFile file = attachedFileService.findByAttachedFileId(fileId);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }

            String s3Url = file.getPath(); // S3 URL
            String originalFileName = file.getName(); // DB에 저장된 원본 파일명

            // 파일 이름을 URL 인코딩 처리
            String encodedFileName = URLEncoder.encode(originalFileName, "UTF-8")
                    .replaceAll("\\+", "%20"); // '+'를 공백으로 변경

            // S3 URL에서 key 추출
            String s3Key = extractS3KeyFromUrl(s3Url);

            // S3에서 파일 스트림 가져오기
            Resource resource = s3Service.downloadFile(s3Key);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .body(resource);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * S3 URL에서 key 부분을 추출
     * 예: https://inswave-2th-project-bucket.s3.ap-northeast-2.amazonaws.com/quotation_Board/파일명
     * -> quotation_Board/파일명
     */
    private String extractS3KeyFromUrl(String s3Url) {
        try {
            String[] parts = s3Url.split(".amazonaws.com/");
            if (parts.length > 1) {
                return parts[1];
            }
            throw new IllegalArgumentException("Invalid S3 URL format");
        } catch (Exception e) {
            throw new RuntimeException("S3 URL 파싱 실패: " + s3Url, e);
        }
    }

}






