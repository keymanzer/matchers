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
import java.util.List;
import java.util.UUID;

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

        // 중복 제거된 도시 목록
        List<String> cities = locations.stream()
                .map(Location::getCity)
                .distinct()
                .toList();
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
    public String quotationBoardList(Model model, @AuthenticationPrincipal CustomUser customUser) {
        List<QuotationBoard> list = quotationBoardService.findAllQuotationBoards(2);
        System.out.println("List size: " + list.size());
        model.addAttribute("quotationBoards", list);
        System.out.println("list = " + list);
        System.out.println(model.getAttribute("quotationBoards"));
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




