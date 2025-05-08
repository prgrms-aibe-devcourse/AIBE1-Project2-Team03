package aibe.hosik;

import aibe.hosik.post.dto.PostRequestDTO;
import aibe.hosik.post.dto.PostResponseDTO;
import aibe.hosik.post.entity.PostCategory;
import aibe.hosik.post.entity.PostType;

import aibe.hosik.post.service.PostService;

import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// 이미지 등록 테스트
@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 게시글_등록_성공() throws Exception {
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("테스트 유저가 없습니다."));

        PostRequestDTO dto = new PostRequestDTO(
                "테스트 제목", "테스트 내용", 3,
                "ENFP", LocalDate.now().plusDays(7),
                PostCategory.PROJECT, PostType.ONLINE,
                List.of("Java", "Spring")
        );

        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "테스트 이미지".getBytes()
        );

        PostResponseDTO response = postService.createPost(dto, image, user);

        assertEquals("테스트 제목", response.title());
        assertNotNull(response.image()); // 이미지도 잘 올라갔는지 확인
    }
}