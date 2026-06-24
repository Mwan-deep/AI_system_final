package AI_Study_Hub.repository;

import AI_Study_Hub.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // Lấy toàn bộ lịch sử làm bài của một người dùng, bài mới nhất xếp trên cùng
    List<QuizAttempt> findByAccount_AccountIdOrderByCreatedAtDesc(Long accountId);

}