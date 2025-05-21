package kr.or.kosa.quotationBoard.controller;

import kr.or.kosa.attachedFile.dto.AttachedFile;
import kr.or.kosa.attachedFile.service.AttachedFileService;
import kr.or.kosa.board.dto.Board;
import kr.or.kosa.board.service.BoardService;
import kr.or.kosa.expert.dto.Location;
import kr.or.kosa.expert.service.ExpertService;
import kr.or.kosa.quotationBoard.dto.QuotationBoard;
import kr.or.kosa.quotationBoard.service.QuotationBoardService;
import kr.or.kosa.user.dto.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    @Value("${spring.servlet.multipart.location}")
    private String uploadDirectory;
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


    // 게시판 생성 처리
    // 2) 게시글 + 지역 + 첨부파일 생성 처리
    @PostMapping("/create")
    @Transactional
    public String createQuotationBoard(
            @ModelAttribute QuotationBoard quotationBoard,
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam("locationIds") List<Integer> locationIds,
            @RequestParam(name = "attachedFiles", required = false) List<MultipartFile> attachedFiles
    ) {
        // --- 2-1. Board 테이블에 삽입 ---
        Board board = new Board();
        board.setTitle(quotationBoard.getTitle());
        board.setContent(quotationBoard.getContent());
        board.setUserNickname(customUser.getNickname());
        board.setUserId(2);//customUser.getUserId()

        int postId = boardService.getNextBoardId();
        board.setPostId(postId);
        boardService.createBoard(board);

        // --- 2-2. QuotationBoard 테이블에 삽입 ---
        quotationBoard.setPostId(postId);
        quotationBoard.setUserNickname(customUser.getNickname());
        quotationBoard.setUserId(2);//customUser.getUserId()
        quotationBoardService.createQuotationBoard(quotationBoard);

        // --- 2-3. Quotation_Location 매핑 삽입 ---
        for (Integer locId : locationIds) {
            quotationBoardService.addQuotationLocation(postId, locId);
        }

        // 4) 첨부파일 저장 & 메타 삽입
        if (attachedFiles != null && !attachedFiles.isEmpty()) {
            System.out.println("attachedFiles.size() = " + attachedFiles.size());
            for (MultipartFile file : attachedFiles) {
                if (!file.isEmpty()) {
                    // 물리 저장
                    String savedName = saveFile(file);

                    // DB 메타 삽입
                    AttachedFile af = new AttachedFile();
                    af.setPostId(postId);
                    af.setName(savedName);
                    af.setPath(uploadDirectory + "/" + savedName);
                    attachedFileService.saveAttachedFileMetadata(af);
                }
            }
        }

        return "redirect:/user/quotationBoard/list";
    }

    // 파일 저장 헬퍼 (원본 파일명 그대로)
    private String saveFile(MultipartFile file) {
        try {
            // 1) 원본 파일명 꺼내기
            String originalName = file.getOriginalFilename();

            // 2) 저장 경로 생성
            Path target = Paths.get(uploadDirectory).resolve(originalName);
            Files.createDirectories(target.getParent());

            // 3) 실제 저장
            file.transferTo(target.toFile());

            // 4) 뷰에 전달할 파일명으로 원본명을 반환
            return originalName;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }


    @PostMapping("/update")
    public String updateQuotationBoard(QuotationBoard quotationBoard) {
        System.out.println(quotationBoard);
        quotationBoardService.updateQuotationBoard(quotationBoard);
        return "redirect:/user/quotationBoard/list"; // 수정 후 목록 페이지 이동
    }

    @PostMapping("/delete")
    public String deleteQuotationBoard(int postId) {
        System.out.println(postId);
        quotationBoardService.deleteQuotationBoard(postId);
        return "redirect:/user/quotationBoard/list";
    }


    @GetMapping("/list")
    public String quotationBoardList(Model model,
                                     @AuthenticationPrincipal CustomUser customUser,
                                     @RequestParam(defaultValue = "1") int page) {
        // 페이지당 항목 수
        int pageSize = 10;

        // 전체 견적 요청 목록 조회
        List<QuotationBoard> allBoards = quotationBoardService.findAllQuotationBoards(2);
        //customUser.getUserId(); 2로 되어있는거 이걸로 나중에 대체

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

        return "quotationBoard/list";
    }


    @GetMapping("/list/{postId}")
    public String quotationBoardDetail(@PathVariable int postId, Model model) {
        QuotationBoard qb = quotationBoardService.findByPostIdWithLocations(postId);
        List<AttachedFile> attachedFiles = attachedFileService.findByPostId(postId);
        model.addAttribute("quotationBoard", qb);
        model.addAttribute("attachedFiles", attachedFiles);
        return "quotationBoard/detail";
    }


    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws MalformedURLException {
        Path file = Paths.get(uploadDirectory).resolve(fileName);
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        // 파일의 MIME 타입을 추측 (실패 시 application/octet-stream)
        String contentType = null;
        try {
            contentType = Files.probeContentType(file);
        } catch (IOException e) {
            // 무시
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

}




