package br.com.academico.crud.service;

import br.com.academico.crud.config.UploadProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoStorageService {

    private static final List<String> DEFAULT_ALLOWED = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final UploadProperties props;

    public PhotoStorageService(UploadProperties props) {
        this.props = props;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        List<String> allowed = props.allowedContentTypes() != null && !props.allowedContentTypes().isEmpty()
                ? props.allowedContentTypes()
                : DEFAULT_ALLOWED;
        if (contentType == null || !allowed.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported content type: " + contentType
                    + ". Allowed: " + String.join(", ", allowed));
        }

        String ext = extractExtension(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        Path dir = Paths.get(props.dir()).toAbsolutePath().normalize();
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir)) {
            throw new IllegalArgumentException("Invalid target path");
        }

        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        return props.publicBase() + "/" + filename;
    }

    public void delete(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) return;
        String prefix = props.publicBase() + "/";
        if (!publicUrl.startsWith(prefix)) return;
        String filename = publicUrl.substring(prefix.length());
        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) return;

        Path dir = Paths.get(props.dir()).toAbsolutePath().normalize();
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir)) return;

        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }

    private String extractExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0 && dot < originalFilename.length() - 1) {
                String ext = originalFilename.substring(dot + 1).toLowerCase();
                if (ext.matches("[a-z0-9]{2,5}")) {
                    return ext;
                }
            }
        }
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            case "image/gif"  -> "gif";
            default -> "";
        };
    }
}
