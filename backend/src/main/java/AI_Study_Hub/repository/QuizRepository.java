package AI_Study_Hub.repository;

import AI_Study_Hub.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // Thêm hàm này vào QuizRepository.java
    List<Quiz> findByVisibilityOrderByCreatedAtDesc(String visibility);

    List<Quiz> findByAccount_AccountIdOrderByCreatedAtDesc(Long accountId);
}