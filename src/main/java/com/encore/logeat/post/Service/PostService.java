package com.encore.logeat.post.Service;

import com.encore.logeat.post.Dto.RequestDto.PostCreateRequestDto;
import com.encore.logeat.post.Dto.RequestDto.PostUpdateRequestDto;
import com.encore.logeat.post.Dto.ResponseDto.PostSearchResponseDto;
import com.encore.logeat.post.domain.Post;
import com.encore.logeat.post.repository.PostRepository;
import com.encore.logeat.user.domain.User;
import com.encore.logeat.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }
    @PreAuthorize("hasAuthority('USER')")
    public Post createPost(PostCreateRequestDto postCreateRequestDto) {
//        MultipartFile multipartFile = postCreateRequestDto.getPostImage();
//        String fileName = multipartFile.getOriginalFilename();
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        String[] split = name.split(":");
        long userId = Long.parseLong(split[0]);

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException());

        Post new_post = Post.builder()
                .title(postCreateRequestDto.getTitle())
                .contents(postCreateRequestDto.getContents())
                .category(postCreateRequestDto.getCategory())
                .location(postCreateRequestDto.getLocation())
                .user(user)
                .build();
        Post post = postRepository.save(new_post);
//        Path path = Paths.get("/Users/jang-eunji/Desktop/tmp", post.getId() + "_" + fileName);
//        post.setImagePath(path.toString());
//        try {
//            byte[] bytes = multipartFile.getBytes();
//            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
//        } catch (IOException e) {
//            throw new IllegalArgumentException("image not available");
//        }
        return post;
    }
    @PreAuthorize("hasAuthority('USER')")
    public Post update(Long id, PostUpdateRequestDto postUpdateRequestDto) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("not found post"));

//    MultipartFile multipartFile = postUpdateRequestDto.getPostImage();
//    String fileName = multipartFile.getOriginalFilename();
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        String[] split = name.split(":");
        long userId = Long.parseLong(split[0]);

        if(userId == post.getUser().getId()){
            post.updatePost(
                    postUpdateRequestDto.getTitle(),
                    postUpdateRequestDto.getContents(),
                    postUpdateRequestDto.getCategory(),
                    postUpdateRequestDto.getLocation()
//                ,postUpdateRequestDto.getPostImage()
            );
        }else {
            // 업데이트 하는 유저 id랑 게시글 작성한 유저 id랑 다르면 어떤 에러 쓸지 몰라서 일단 RuntimeException갑니다.
            throw new  RuntimeException("본인이 작성한 글만 수정 가능합니다.");
        }
//        Path path = Paths.get("/Users/jang-eunji/Desktop/tmp", post.getId()+"_"+fileName);
//        try{
//            byte[] bytes = multipartFile.getBytes();
//            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
//        } catch(IOException e){
//            throw new IllegalArgumentException("image not available");
//        }
        return post;
    }
    public Page<PostSearchResponseDto> postTitleSearch(String titleKeyword, Pageable pageable) {
        Page<Post> post = postRepository.findPostByTitleContaining(titleKeyword, pageable);
        return post.map(PostSearchResponseDto::toPostSearchResponseDto);
    }

    public Page<PostSearchResponseDto> postCategorySearch(String category, Pageable pageable) {
        Page<Post> post = postRepository.findPostByCategory(category, pageable);
        return post.map(PostSearchResponseDto::toPostSearchResponseDto);
    }

    public Page<PostSearchResponseDto> postUserNameSearch(String userNickname, Pageable pageable) {
        Page<Post> post = postRepository.findByUserNickname(userNickname, pageable);
        return post.map(PostSearchResponseDto::toPostSearchResponseDto);
    }

    public Page<PostSearchResponseDto> findAllAccessiblePosts(Pageable pageable) {
        Page<Post> all = postRepository.findAll(pageable);
        return all.map(PostSearchResponseDto::toPostSearchResponseDto);
    }

    public void deletePost(Long id) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        String[] split = name.split(":");
        long userId = Long.parseLong(split[0]);
        System.out.println("userId = " + userId);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("글을 찾을수 없습니다."));
        if(userId == post.getUser().getId()){
            postRepository.delete(post);
        }



    }


}
