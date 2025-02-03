package Funding.Startreum.domain.comment.service;

import Funding.Startreum.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    final private CommentRepository commentRepository;

}
