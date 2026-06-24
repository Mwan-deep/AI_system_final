package AI_Study_Hub.service;

import AI_Study_Hub.entity.*;
import AI_Study_Hub.repository.SemesterRepository;
import AI_Study_Hub.repository.SpecializationRepository;
import AI_Study_Hub.repository.StudyMaterialRepository;
import AI_Study_Hub.repository.SubjectRepository;
import AI_Study_Hub.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyMaterialService {

    private final StudyMaterialRepository materialRepository;
    private final AccountRepository accountRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final SpecializationRepository specializationRepository;
    private final GoogleDriveService googleDriveService;
    private final DocumentAnalyzerService documentAnalyzerService;

    private final String UPLOAD_DIR = "uploads/";

    @Transactional
    public StudyMaterial uploadDocument(Long userId, Long specializationId, Long semesterId,
                                        String subjectName, String title, String description, MultipartFile file) throws IOException {

        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Semester"));

        Subject subject = subjectRepository.findBySubjectNameAndSpecialization_SpecializationId(subjectName, specializationId)
                .orElseGet(() -> {
                    Specialization spec = specializationRepository.findById(specializationId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy Chuyên ngành hẹp"));

                    Subject newSubject = new Subject();
                    newSubject.setSubjectName(subjectName);
                    newSubject.setSpecialization(spec);
                    return subjectRepository.save(newSubject);
                });

        String googleDriveFileId = googleDriveService.uploadFileToDrive(file);

        StudyMaterial material = StudyMaterial.builder()
                .account(account)
                .subject(subject)
                .semester(semester)
                .title(title)
                .description(description)
                .fileUrl(googleDriveFileId)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .visibility("PRIVATE") // Sinh viên có thể tự đổi sang PUBLIC sau (nếu bạn làm API Update)
                .viewCount(0)
                .downloadCount(0)
                .build();

        StudyMaterial savedMaterial = materialRepository.save(material);

        documentAnalyzerService.processAndSaveContext(savedMaterial, file);

        return savedMaterial;
    }

    // -------------------------------------------------------------------------
    // HÀM MỚI: Lấy danh sách, Lọc và Tìm kiếm gộp chung (Đã xử lý Phân quyền)
    // -------------------------------------------------------------------------
    public List<StudyMaterial> getFilteredMaterials(Long accountId, Long semesterId, Long majorId, Long specializationId, String keyword) {
        return materialRepository.filterAndSearchVisibleMaterials(accountId, semesterId, majorId, specializationId, keyword);
    }

    public StudyMaterial getMaterialById(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu với ID: " + materialId));
    }

    @Transactional
    public StudyMaterial getMaterialDetailWithViewCount(Long materialId) {
        StudyMaterial material = getMaterialById(materialId);
        material.setViewCount(material.getViewCount() + 1);
        return materialRepository.save(material);
    }

    @Transactional
    public StudyMaterial getMaterialForDownloadWithDownloadCount(Long materialId) {
        StudyMaterial material = getMaterialById(materialId);
        material.setDownloadCount(material.getDownloadCount() + 1);
        return materialRepository.save(material);
    }

    @Transactional
    public void deleteMaterial(Long materialId) throws IOException {
        StudyMaterial material = getMaterialById(materialId);
        String fileUrlOrId = material.getFileUrl();

        if (fileUrlOrId != null && !fileUrlOrId.isEmpty() && !fileUrlOrId.startsWith("http")) {
            googleDriveService.deleteFileFromDrive(fileUrlOrId);
        }

        materialRepository.delete(material);
    }
}