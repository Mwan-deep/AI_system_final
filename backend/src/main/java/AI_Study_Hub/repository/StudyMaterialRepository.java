package AI_Study_Hub.repository;

import AI_Study_Hub.entity.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {

    // Spring Boot quá thông minh: Bạn chỉ cần gõ tên hàm chuẩn, nó tự sinh ra câu query SQL!
    // VD: Tìm tất cả tài liệu theo ID môn học
    List<StudyMaterial> findBySubject_SubjectId(Long subjectId);

    // Tìm tất cả tài liệu public
    List<StudyMaterial> findByVisibility(String visibility);

    // Tính năng Tìm kiếm đa năng (Quét Tên tài liệu, Mô tả, Tên môn học, Mã môn học)
    @Query("SELECT s FROM StudyMaterial s WHERE " +
            ":keyword IS NULL OR " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.subject.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.subject.subjectCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<StudyMaterial> searchMaterials(@Param("keyword") String keyword);

    // Thuật toán: Lấy Top 10 tài liệu có lượt tải xuống cao nhất
    List<StudyMaterial> findTop10ByOrderByDownloadCountDesc();

    // Thuật toán: Lấy Top 10 tài liệu có lượt xem cao nhất
    List<StudyMaterial> findTop10ByOrderByViewCountDesc();

    // Bổ sung vào bên trong interface StudyMaterialRepository
    @Query("SELECT m FROM StudyMaterial m " +
            "WHERE (m.visibility = 'PUBLIC' OR m.account.accountId = :accountId) " +
            "AND (:semesterId IS NULL OR m.semester.semesterId = :semesterId) " +
            "AND (:specializationId IS NULL OR m.subject.specialization.specializationId = :specializationId) " +
            "AND (:majorId IS NULL OR m.subject.specialization.major.majorId = :majorId) " +
            "AND (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY m.createdAt DESC")
    List<StudyMaterial> filterAndSearchVisibleMaterials(
            @Param("accountId") Long accountId,
            @Param("semesterId") Long semesterId,
            @Param("majorId") Long majorId,
            @Param("specializationId") Long specializationId,
            @Param("keyword") String keyword);
}