package kr.or.kosa.quotationBoard.controller;

import kr.or.kosa.attachedFile.dto.AttachedFile;
import kr.or.kosa.attachedFile.service.AttachedFileService;
import kr.or.kosa.board.dto.Board;
import kr.or.kosa.board.service.BoardService;
import kr.or.kosa.quotationBoard.dto.QuotationBoard;
import kr.or.kosa.quotationBoard.service.QuotationBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/user/quotationBoard")
public class QuotationBoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private QuotationBoardService quotationBoardService;

    @Autowired
    private AttachedFileService attachedFileService;

    // 게시판 생성 화면 요청
    @GetMapping("/create")
    public String showCreateQuotationBoardForm(Model model) {
        model.addAttribute("quotationBoard", new QuotationBoard());
        System.out.println("model = " + model);
        System.out.println("크리에이트 폼요청");
        return "quotationBoard/create";  // 게시판 생성 폼을 표시하는 뷰
    }

    // 게시판 생성 처리
    @PostMapping("/create")
    public String createQuotationBoard(QuotationBoard quotationBoard/*, List<MultipartFile> attachedFiles*/) {
        // 1. 먼저 board 테이블에 게시글 삽입
        Board board = new Board();
        System.out.println(quotationBoard);
        // 사용자가 입력한 title과 content를 BoardDTO에 설정
        board.setTitle(quotationBoard.getTitle());  // 사용자가 입력한 제목
        board.setContent(quotationBoard.getContent());  // 사용자가 입력한 본문 내용
        board.setUserNickname("defaultNickname");  // 로그인한 유저 정보로 설정 (임시값)
        board.setUserId(1);  // 로그인한 유저 ID (임시값)
        System.out.println(board);


        int postid = boardService.getNextBoardId();
        System.out.println(postid);
        board.setPostId(postid);
        // boardService 호출하여 board 테이블에 데이터 삽입
        boardService.createBoard(board);  // postId가 생성됨 (insert 후 postId 설정됨)

        // 2. 생성된 board의 postId를 quotationBoard에 설정
        quotationBoard.setPostId(postid);  // board에서 생성된 postId를 설정
        quotationBoard.setUserNickname("defaultNickname");  // 로그인한 유저 정보로 설정 (임시값)
        quotationBoard.setUserId(1);  // 로그인한 유저 ID (임시값)

        System.out.println(quotationBoard);
        // 3. 첨부파일 처리 (첨부파일 목록을 받아서 처리)
//        if (attachedFiles != null && !attachedFiles.isEmpty()) {
//            for (MultipartFile file : attachedFiles) {
//                // 첨부파일 정보 처리 (파일명, 경로, postId 등)
//                AttachedFile attachedFile = new AttachedFile();
//                attachedFile.setName(file.getOriginalFilename());  // 파일 이름
//                attachedFile.setPath("/uploads/" + file.getOriginalFilename());  // 예시 경로 (파일 저장 위치)
//                attachedFile.setPostId(board.getPostId());  // 게시글과 연결되는 postId 설정
//
//                // 파일 저장 처리 로직 (파일 시스템이나 DB에 저장)
//                // 예: 파일을 서버에 저장하고, 파일 경로를 DB에 저장
//
//                // 첨부파일 정보를 DB에 저장 (첨부파일 서비스 호출)
//                attachedFileService.saveAttachedFile(attachedFile);
//            }
//        }

        // 3. quotationBoard를 생성
        quotationBoardService.createQuotationBoard(quotationBoard);  // quotation_board 테이블에 삽입

        // 4. 생성 후 목록 페이지로 리다이렉트
        return "redirect:/quotationBoard/list";  // 성공 후 리스트 페이지로 리다이렉트
    }
}
